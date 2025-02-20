package main.java.dishes;

import main.java.filecontroller.DataStore;
import main.java.ingredients.Ingredient;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.web.HTMLEditor;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Add dishes to the program. Used by AddDishesPage.fxml
 */
public class AddDishesController {
    @FXML private TextField dishNameInput;
    @FXML private TextField dishNumberOfMeals;
    @FXML private ListView<Ingredient> addedIngredientsListView;
    @FXML private ListView<Dish> dishListView;

    @FXML private ImageView dishImageView;
    @FXML private HTMLEditor instructionsEditor;

    private ObservableList<Ingredient> ingredientList;
    private ObservableList<Ingredient> addedIngredientList;
    private ObservableList<Dish> dishList = FXCollections.observableArrayList();
    @FXML private Accordion categoryAccordion;
    @FXML private Button addDishButton;


    @FXML
    public void initialize() {
        DataStore dataStore = DataStore.getInstance(); // Gets saved data

        // Initialize ingredientList if not already initialized
        if (ingredientList == null) {
            ingredientList = FXCollections.observableArrayList(dataStore.getAllIngredientsList());
        }

        // Clear existing panes
        categoryAccordion.getPanes().clear();

        // Group ingredients by category
        Map<String, List<Ingredient>> ingredientsByCategory = dataStore.getAllIngredientsList().stream()
                .collect(Collectors.groupingBy(Ingredient::getCategory));

        // Sort categories alphabetically
        List<String> sortedCategories = new ArrayList<>(ingredientsByCategory.keySet());
        sortedCategories.sort(String::compareTo);  // Sort alphabetically

        // Iterate through the sorted categories and create TitledPanes for each one
        for (String category : sortedCategories) {
            List<Ingredient> ingredients = ingredientsByCategory.get(category);

            // Create ListView for each category
            ListView<Ingredient> ingredientListView = new ListView<>();
            ObservableList<Ingredient> ingredientList = FXCollections.observableArrayList(ingredients);
            ingredientListView.setItems(ingredientList);

            // Set custom cell factory for ingredient ListView
            ingredientListView.setCellFactory(param -> new ListCell<Ingredient>() {
                @Override
                protected void updateItem(Ingredient ingredient, boolean empty) {
                    super.updateItem(ingredient, empty);
                    if (empty || ingredient == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        Button addButton = new Button("Add");
                        addButton.setOnAction(event -> {
                            // Create a new Ingredient with quantity 1
                            Ingredient newIngredient = new Ingredient(ingredient.getName(), ingredient.getPrice(),
                                    1, ingredient.getCategory());
                            if (!addedIngredientList.contains(newIngredient)) {
                                addedIngredientList.add(newIngredient);
                            }
                        });

                        HBox hBox = new HBox(5, addButton);
                        setText(ingredient.getName() + " ($" + String.format("%.2f", ingredient.getPrice()) + ")");
                        setGraphic(hBox);
                    }
                }
            });

            // Create TitledPane for the category and add the ListView inside it
            TitledPane titledPane = new TitledPane(category, ingredientListView);

            // Add hover effect only if the TitledPane is not expanded
            titledPane.setOnMouseEntered(event -> {
                if (!titledPane.isExpanded()) {
                    ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), titledPane);
                    scaleTransition.setToX(1.05);
                    scaleTransition.setToY(1.05);
                    scaleTransition.play();
                }
            });
            titledPane.setOnMouseExited(event -> {
                if (!titledPane.isExpanded()) {
                    ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), titledPane);
                    scaleTransition.setToX(1);
                    scaleTransition.setToY(1);
                    scaleTransition.play();
                }
            });

            // Update the style and prevent animation when the TitledPane is expanded
            titledPane.expandedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    titledPane.setScaleX(1);
                    titledPane.setScaleY(1);
                }
            });

            categoryAccordion.getPanes().add(titledPane);
        }

        // Initialize addedIngredientList
        addedIngredientList = FXCollections.observableArrayList();
        addedIngredientsListView.setItems(addedIngredientList);

        // Set custom cell factory for addedIngredientsListView
        addedIngredientsListView.setCellFactory(param -> new ListCell<Ingredient>() {
            @Override
            protected void updateItem(Ingredient ingredient, boolean empty) {
                super.updateItem(ingredient, empty);
                if (empty || ingredient == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Button decreaseButton = new Button("-");
                    Button increaseButton = new Button("+");
                    Label quantityLabel = new Label(String.valueOf(ingredient.getQuantity()));

                    decreaseButton.setOnAction(event -> {
                        if (ingredient.getQuantity() > 1) {
                            ingredient.setQuantity(ingredient.getQuantity() - 1);
                            quantityLabel.setText(String.valueOf(ingredient.getQuantity()));
                        }
                    });

                    increaseButton.setOnAction(event -> {
                        ingredient.setQuantity(ingredient.getQuantity() + 1);
                        quantityLabel.setText(String.valueOf(ingredient.getQuantity()));
                    });

                    Button removeButton = new Button("Remove");
                    removeButton.setOnAction(event -> addedIngredientList.remove(ingredient));

                    HBox hBox = new HBox(5, decreaseButton, quantityLabel, increaseButton, removeButton);
                    setText(ingredient.getName() + " ($" + String.format("%.2f", ingredient.getPrice()) + ")");
                    setGraphic(hBox);
                }
            }
        });

        // Initialize dishList
        dishList.setAll(dataStore.getAddedDishList());
        dishListView.setItems(dishList);
        //test();

        // Set up double-click event handler
        dishListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Check for double-click
                handleDishDoubleClick();
            }
        });

        // Set up the image view shadow effect
        ImageView imageView = (ImageView) addDishButton.getGraphic();
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.BLACK);
        dropShadow.setOffsetX(4.0);
        dropShadow.setOffsetY(4.0);
        dropShadow.setRadius(5.0);
        imageView.setEffect(dropShadow);

        // Scale transition for hover
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), addDishButton);
        scaleUp.setToX(1.1); // Scale up to 110%
        scaleUp.setToY(1.1);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), addDishButton);
        scaleDown.setToX(1.0); // Scale down to original size
        scaleDown.setToY(1.0);

        // Add mouse entered and exited events
        addDishButton.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> scaleUp.play());
        addDishButton.addEventHandler(MouseEvent.MOUSE_EXITED, event -> scaleDown.play());

        // Scale down on press
        addDishButton.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            ScaleTransition scalePress = new ScaleTransition(Duration.millis(100), addDishButton);
            scalePress.setToX(0.95); // Scale down to 95%
            scalePress.setToY(0.95);
            scalePress.play();
        });

        // Scale back up on release
        addDishButton.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            ScaleTransition scaleRelease = new ScaleTransition(Duration.millis(100), addDishButton);
            scaleRelease.setToX(1.0); // Return to original size
            scaleRelease.setToY(1.0);
            scaleRelease.play();
        });
    }


    /**
     * Handles the logic for when user double-clicks on a dish.
     */
    private void handleDishDoubleClick() {
        // Get selected dish
        Dish selectedDish = dishListView.getSelectionModel().getSelectedItem();
        if (selectedDish != null) {
            dishNameInput.setText(selectedDish.getName());
            dishNumberOfMeals.setText(String.valueOf(selectedDish.getNumberOfMeals()));

            // Set the instructions in the editor
            instructionsEditor.setHtmlText(selectedDish.getInstructions());


            // Clear and update the addedIngredientsListView
            addedIngredientList.clear();
            for (Ingredient ingredient : selectedDish.getIngredients()) {
                Ingredient newIngredient = new Ingredient(ingredient.getName(), ingredient.getPrice(),
                        ingredient.getQuantity(), ingredient.getCategory());
                addedIngredientList.add(newIngredient);
            }

            // Load and display the dish image if available
            String imagePath = selectedDish.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                System.out.println("In: " + imagePath);
                selectedFile = new File(selectedDish.getImagePath());
                Image image = new Image(new File(imagePath).toURI().toString());
                dishImageView.setImage(image);
            } else {
                dishImageView.setImage(null);  // Clear image if no image is available
            }
        }
        System.out.println(selectedDish.getImagePath());
    }


    /**
     * Handles the logic for when the user click on add dish
     */
    @FXML
    private void handleAddDish() {
        // Gets the name the user put.
        String dishName = dishNameInput.getText();
        if (dishName.isEmpty()) {
            showAlert(AlertType.ERROR, "Invalid Input", "Please enter a name for the dish.");
            return;
        }

        // Gets all the ingredients to add to the dish
        List<Ingredient> selectedIngredients = new ArrayList<>();
        for (Ingredient ingredient : addedIngredientsListView.getItems()) {
            // Create a new Ingredient with the quantity
            Ingredient newIngredient = new Ingredient(ingredient.getName(), ingredient.getPrice(),
                    ingredient.getQuantity(), ingredient.getCategory());
            selectedIngredients.add(newIngredient);
        }

        // Check if a dish with the same name already exists
        Dish existingDish = null;
        for (Dish dish : dishList) {
            if (dish.getName().equalsIgnoreCase(dishName)) {
                existingDish = dish;
                break;
            }
        }

        // If found, remove the existing dish
        if (existingDish != null) {
            dishList.remove(existingDish);
        }

        // Create the new dish
        Dish newDish = new Dish(dishName);
        newDish.addIngredients(selectedIngredients);

        // Get the number of meals for the dish
        String numberOfMeals = dishNumberOfMeals.getText();
        if (numberOfMeals.isEmpty()) {
            showAlert(AlertType.ERROR, "Invalid Input", "Please enter the number of meals.");
            return;
        }
        newDish.setNumberOfMeals(Double.parseDouble(numberOfMeals));

        // Handle image upload
        if (selectedFile != null) {
            try {
                // Copy the image to the application's directory
                File appDir = new File("src/main/resources/images");
                if (!appDir.exists()) {
                    appDir.mkdirs(); // Create the directory if it doesn't exist
                }
                File destFile = new File(appDir, selectedFile.getName());

                // Copy the selected file to the destination
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Set the image path in the dish object
                newDish.setImagePath(destFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Image Upload Error", "Failed to upload image.");
                return;
            }
        } else {
            // Use a default image if no file is selected
            newDish.setImagePath("src/main/resources/images/default-image.jpg");
        }

        // Gets the recipe information
        String dishInstructions = instructionsEditor.getHtmlText();
        newDish.setInstructions(dishInstructions);

        // Add the new dish to the list
        dishList.add(newDish);
        dishNameInput.clear();

        // Save the data (including the dish and image)
        saveData();
    }

    /**
     * Selects all ingredients
     */
    @FXML
    private void handleSelectAllAdded() {
        addedIngredientsListView.getSelectionModel().selectAll();
    }

    /**
     * Removes selected ingredients
     */
    @FXML
    private void handleRemoveSelected() {
        ObservableList<Ingredient> selectedIngredients = addedIngredientsListView.getSelectionModel().getSelectedItems();
        addedIngredientList.removeAll(selectedIngredients);
    }

    /**
     * Removed selected dish
     */
    @FXML
    private void handleRemoveSelectedDish() {
        Dish selectedDish = dishListView.getSelectionModel().getSelectedItem();
        if (selectedDish != null) {
            dishList.remove(selectedDish);
        } else {
            showAlert(AlertType.ERROR, "No Selection", "Please select a dish to remove.");
        }
        saveData();
    }

    /**
     * Creates a basic alert
     */
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private File selectedFile; // Add this to hold the selected image file

    /**
     * Handles the logic to upload an image into the application
     */
    @FXML
    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        selectedFile = fileChooser.showOpenDialog(null); // Save the selected file to this variable
        if (selectedFile != null) {
            try {
                Image image = new Image(selectedFile.toURI().toString());
                dishImageView.setImage(image);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Image file selection was canceled.");
        }
    }

    /**
     * Saves all the data
     */
    private void saveData() {
        DataStore dataStore = DataStore.getInstance();

        // Ensure ingredientList is not null before saving
        if (ingredientList == null) {
            ingredientList = FXCollections.observableArrayList();
        }

        dataStore.setAllIngredientsList(ingredientList);
        dataStore.setDishList(dishList); // Save the list of dishes
    }

    /**
     * Used to quickly add ingredients for testing purposes.
     */
    private void test() {
        // Define a common image path for all dishes
        String defaultImagePath = "src/main/resources/images/default-image.jpg";

        List<Ingredient> allIngredients = new ArrayList<>();

        // Add ingredients to the list
        allIngredients.add(new Ingredient("Rice", 3, 1, "Pasta"));
        allIngredients.add(new Ingredient("Chicken", 9, 1, "Meat"));
        allIngredients.add(new Ingredient("Beef", 7, 1, "Meat"));
        allIngredients.add(new Ingredient("Pork", 7, 1, "Meat"));
        allIngredients.add(new Ingredient("Shells", 3, 1, "Pasta"));
        allIngredients.add(new Ingredient("Noodles", 3, 1, "Pasta"));
        allIngredients.add(new Ingredient("Orzo", 3, 1, "Pasta"));
        allIngredients.add(new Ingredient("Sliced Bread", 5, 1, "Bread"));
        allIngredients.add(new Ingredient("Sub Rolls", 6, 1, "Bread"));
        allIngredients.add(new Ingredient("Tortilla", 3, 1, "Bread"));
        allIngredients.add(new Ingredient("Milk", 3, 1, "Dairy"));
        allIngredients.add(new Ingredient("Sour Cream", 3, 1, "Dairy"));
        allIngredients.add(new Ingredient("Butter", 3, 1, "Dairy"));
        allIngredients.add(new Ingredient("Cheese", 5, 1, "Dairy"));
        allIngredients.add(new Ingredient("Lettuce", 4, 1, "Produce"));
        allIngredients.add(new Ingredient("Tomato", 1, 1, "Produce"));
        allIngredients.add(new Ingredient("Lemon", 1, 1, "Produce"));
        allIngredients.add(new Ingredient("Potato", 2, 1, "Produce"));

        // Create dishes
        Dish chickenPasta = new Dish("Chicken Pasta");
        chickenPasta.addIngredients(List.of(
                new Ingredient("Chicken", 9, 1, "Meat"),
                new Ingredient("Noodles", 3, 1, "Pasta"),
                new Ingredient("Butter", 3, 1, "Dairy")
        ));
        chickenPasta.setImagePath(defaultImagePath);

        Dish beefTacos = new Dish("Beef Tacos");
        beefTacos.addIngredients(List.of(
                new Ingredient("Beef", 7, 1, "Meat"),
                new Ingredient("Tortilla", 3, 1, "Bread"),
                new Ingredient("Cheese", 5, 1, "Dairy"),
                new Ingredient("Tomato", 1, 1, "Produce"),
                new Ingredient("Lettuce", 4, 1, "Produce")
        ));
        beefTacos.setImagePath(defaultImagePath);

        Dish porkAndRice = new Dish("Pork and Rice");
        porkAndRice.addIngredients(List.of(
                new Ingredient("Pork", 7, 1, "Meat"),
                new Ingredient("Rice", 3, 1, "Pasta"),
                new Ingredient("Lemon", 1, 1, "Produce")
        ));
        porkAndRice.setImagePath(defaultImagePath);

        Dish cheeseSandwich = new Dish("Cheese Sandwich");
        cheeseSandwich.addIngredients(List.of(
                new Ingredient("Sliced Bread", 5, 2, "Bread"),
                new Ingredient("Cheese", 5, 1, "Dairy"),
                new Ingredient("Butter", 3, 1, "Dairy")
        ));
        cheeseSandwich.setImagePath(defaultImagePath);

        // Add dishes to the dishList
        dishList.addAll(chickenPasta, beefTacos, porkAndRice, cheeseSandwich);
    }
}
