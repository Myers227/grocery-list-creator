package main.java.grocerylist;

import main.java.filecontroller.DataStore;
import main.java.dishes.Dish;
import main.java.ingredients.Ingredient;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Transition;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class AddToGroceryListController {

    @FXML
    private FlowPane dishFlowPane; // FlowPane to hold the dish cards

    @FXML
    private ScrollPane groceryListScrollPane; // The ScrollPane that holds the groceryListContainer

    @FXML
    private VBox groceryListContainer; // Reference to the VBox that holds grocery lists

    @FXML
    private VBox selectedGroceryListContainer; // VBox to display the selected grocery list items

    @FXML
    private TextField groceryListNameText; // TextField for the grocery list name
    @FXML
    private Label priceTextBox; // Label for the price of the groceryList

    private List<GroceryList> groceryLists; // List to track all grocery lists
    private GroceryList selectedGroceryList; // Currently selected grocery list

    /**
     * Constructor
     */
    public AddToGroceryListController() {
        groceryLists = new ArrayList<>();
    }

    @FXML
    public void initialize() {
        // Initialize the FlowPane for dishes
        dishFlowPane.setHgap(10);
        dishFlowPane.setVgap(10);
        dishFlowPane.setPadding(new Insets(10));

        // Load all dishes
        loadDishes();
        loadGroceryList();

        // Adjust gaps when the FlowPane width changes
        dishFlowPane.widthProperty().addListener((_, _, newWidth) -> adjustGaps(newWidth.doubleValue()));
    }

    /**
     * Adjust the gapes of the flowpane when resizing window
     */
    private void adjustGaps(double flowPaneWidth) {
        final int cardWidth = 250; // Width of each dish card
        final int minGap = 10; // Minimum gap between cards

        int numColumns = Math.max(1, (int) (flowPaneWidth / (cardWidth + minGap)));
        double remainingSpace = flowPaneWidth - (numColumns * cardWidth);
        double newGap = remainingSpace / (numColumns + 1);

        dishFlowPane.setHgap(Math.max(minGap, newGap));
    }

    /**
     * Adds a plus button to the card.
     */
    private void addPlusButton() {
        StackPane plusCard = createGroceryListCard("+", true);
        plusCard.setTranslateY(10);

        // Scale transition for hover
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), plusCard);
        scaleUp.setToX(1.1); // Scale up to 110%
        scaleUp.setToY(1.1);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), plusCard);
        scaleDown.setToX(1.0); // Scale down to original size
        scaleDown.setToY(1.0);

        // Add mouse entered and exited events
        plusCard.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> scaleUp.play());
        plusCard.addEventHandler(MouseEvent.MOUSE_EXITED, event -> scaleDown.play());

        // Scale down on press
        plusCard.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            ScaleTransition scalePress = new ScaleTransition(Duration.millis(100), plusCard);
            scalePress.setToX(0.95); // Scale down to 95%
            scalePress.setToY(0.95);
            scalePress.play();
        });

        // Scale back up on release
        plusCard.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            ScaleTransition scaleRelease = new ScaleTransition(Duration.millis(100), plusCard);
            scaleRelease.setToX(1.0); // Return to original size
            scaleRelease.setToY(1.0);
            scaleRelease.play();
        });

        groceryListContainer.getChildren().add(0, plusCard); // Add the plus button at the top

        plusCard.setOnMouseClicked(event -> {
            // Create a new grocery list
            String newListName = String.valueOf((groceryLists.size() + 1));
            GroceryList newList = new GroceryList(newListName);
            groceryLists.add(newList);

            // Create a new card for the grocery list and set its click event to select it
            StackPane newCard = createGroceryListCard(newListName, false);
            newCard.setOnMouseClicked(e -> selectGroceryList(newList)); // Set click action to select the new list

            // Add the new list card at the top, right below the plus button
            groceryListContainer.getChildren().add(1, newCard);

            // Select the newly created list
            selectGroceryList(newList);
            saveGroceryList();
        });

        saveGroceryList();
    }

    /**
     * Load dishes into the page.
     */
    private void loadDishes() {
        DataStore dataStore = DataStore.getInstance();
        List<Dish> dishes = dataStore.getAddedDishList();

        // Size of the card
        final int cardWidth = 300;
        final int cardHeight = 400;

        // Logic when clicking on dish
        for (Dish dish : dishes) {
            StackPane dishCard = createDishCard(dish, cardWidth, cardHeight);
            dishCard.setOnMouseClicked(event -> addDishToSelectedGroceryList(dish)); // Add dish to selected list
            dishFlowPane.getChildren().add(dishCard);
        }
    }

    /**
     * Creates the card for the dish.
     */
    private StackPane createDishCard(Dish dish, int width, int height) {
        StackPane cardContainer = new StackPane();

        // Front Side of the Card (Dish Details)
        VBox frontCard = new VBox();
        frontCard.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #000000; -fx-border-width: 1; " +
                "-fx-border-radius: 5; -fx-padding: 0;");
        frontCard.setPrefSize(width, height);
        frontCard.setMinSize(width, height);

        ScrollPane frontScrollPane = new ScrollPane();
        frontScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        frontScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        frontScrollPane.setFitToWidth(true);

        // Scale down on press
        frontScrollPane.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            ScaleTransition scalePress = new ScaleTransition(Duration.millis(100), frontScrollPane);
            scalePress.setToX(0.95); // Scale down to 95%
            scalePress.setToY(0.95);
            scalePress.play();
        });

        // Scale back up on release
        frontScrollPane.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            ScaleTransition scaleRelease = new ScaleTransition(Duration.millis(100), frontScrollPane);
            scaleRelease.setToX(1.0); // Return to original size
            scaleRelease.setToY(1.0);
            scaleRelease.play();
        });

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(width, height);

        Image dishImage;
        try {
            File file = new File(dish.getImagePath());
            dishImage = new Image(file.toURI().toString());
        } catch (Exception e) {
            e.printStackTrace();
            dishImage = new Image("src/main/resources/images/default-image.jpg");
        }

        ImageView imageView = new ImageView(dishImage);
        imageView.setPreserveRatio(false);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);

        Text dishName = new Text(dish.getName());
        dishName.setStyle("-fx-font-size: 55px; -fx-fill: #FFFFFF; -fx-background-color: rgba(0, 0, 0, 0.5); " +
                "-fx-font-weight: bold; -fx-font-family: 'Comic Sans MS';");
        dishName.setWrappingWidth(width);
        dishName.setTextAlignment(TextAlignment.CENTER);

        // Add shadow to card
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.BLACK);
        dropShadow.setOffsetX(4.0);
        dropShadow.setOffsetY(4.0);
        dropShadow.setRadius(7.0);
        dishName.setEffect(dropShadow);

        // Add border to card
        dishName.setStrokeWidth(2);
        dishName.setStroke(Color.BLACK);
        dishName.setVisible(false);

        imageContainer.getChildren().addAll(imageView, dishName);
        StackPane.setAlignment(dishName, Pos.CENTER);

        // Adds button to flip card
        Button imageButton = new Button();
        Image image = new Image(getClass().getResource("/icons/FlipIcon.png").toExternalForm());
        ImageView flipImageView = new ImageView(image);
        flipImageView.setFitWidth(50);
        flipImageView.setFitHeight(50);

        imageButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0;");

        Button flipButton = new Button();
        flipButton.setGraphic(flipImageView);
        flipButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0;");
        flipButton.setTranslateX(-10);

        // Back Side of the Card (Instructions)
        StackPane backCard = new StackPane();
        backCard.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #000000; -fx-border-width: 1; -fx-border-radius: 5; -fx-padding: 10;");
        backCard.setPrefSize(width, height);
        backCard.setMinSize(width, height);
        backCard.setVisible(false);

        // Add scrollpane to the back of the card.
        ScrollPane backScrollPane = new ScrollPane();
        backScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        backScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        backScrollPane.setFitToWidth(true);
        backScrollPane.setPrefHeight(backCard.getHeight());

        Button imageButton2 = new Button();
        ImageView flipImageView2 = new ImageView(image);

        // Size
        flipImageView2.setFitWidth(50);
        flipImageView2.setFitHeight(50);

        imageButton2.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        // Flip Button/Icon
        Button flipButton2 = new Button();
        flipButton2.setGraphic(flipImageView);

        // Adds button to flip back to the front of the card.
        Button flipBackButton = new Button();
        flipBackButton.setGraphic(flipImageView2);
        flipBackButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0;");
        flipBackButton.setTranslateX(-10);

        // Creates the flipping animation
        flipBackButton.setOnAction(event -> {
            RotateTransition frontFlip = new RotateTransition(Duration.millis(300), cardContainer);
            frontFlip.setAxis(Rotate.Y_AXIS);
            frontFlip.setFromAngle(0);
            frontFlip.setToAngle(90);

            RotateTransition backFlip = new RotateTransition(Duration.millis(300), cardContainer);
            backFlip.setAxis(Rotate.Y_AXIS);
            backFlip.setFromAngle(270);
            backFlip.setToAngle(360);

            frontFlip.setOnFinished(e -> {
                backCard.setVisible(false);
                frontCard.setVisible(true);
                backFlip.play();
            });
            frontFlip.play();
        });

        // Create a WebView for the instructions
        WebView webView = new WebView();
        webView.setPrefSize(width, height - 20);
        String htmlContent = dish.getInstructions();
        String nonEditableContent = htmlContent.replaceAll("contenteditable=\"true\"", "");
        webView.getEngine().loadContent(nonEditableContent); // Load HTML content

        // Add the flip button to the image container
        StackPane.setAlignment(flipBackButton, Pos.TOP_RIGHT);
        backScrollPane.setContent(webView);
        backCard.getChildren().addAll( backScrollPane, flipBackButton);

        // Set action for flip button on the front card
        flipButton.setOnAction(event -> {
            RotateTransition frontFlip = new RotateTransition(Duration.millis(300), cardContainer);
            frontFlip.setAxis(Rotate.Y_AXIS);
            frontFlip.setFromAngle(0);
            frontFlip.setToAngle(90);

            RotateTransition backFlip = new RotateTransition(Duration.millis(300), cardContainer);
            backFlip.setAxis(Rotate.Y_AXIS);
            backFlip.setFromAngle(270);
            backFlip.setToAngle(360);

            frontFlip.setOnFinished(e -> {
                frontCard.setVisible(false);
                backCard.setVisible(true);
                backFlip.play();
            });
            frontFlip.play();
        });

        // Add the flip button to the image container
        StackPane.setAlignment(flipButton, Pos.TOP_RIGHT);
        StackPane.setMargin(flipButton, new Insets(10, 10, 0, 0));

        // Creates a place to display the number of meals.
        StackPane mealsVbox = new StackPane();
        mealsVbox.setPadding(new Insets(15,0,0,15));

        // Number of meals
        Text mealsText = new Text();
        mealsText.setText(String.valueOf(dish.getNumberOfMeals()));
        // Set a larger font size
        mealsText.setFont(new Font("Arial", 24));
        // Add a black border around the text
        mealsText.setStyle("-fx-text-fill: white; -fx-stroke: black; -fx-stroke-width: 1px;");
        System.out.println("This is meals: " + dish.getNumberOfMeals());
        StackPane.setAlignment(mealsText, Pos.TOP_LEFT);
        mealsVbox.getChildren().add(mealsText);

        imageContainer.getChildren().add(mealsVbox);
        imageContainer.getChildren().add(flipButton);

        // Box to hold price and ingredients.
        VBox frontContent = new VBox();
        frontContent.setSpacing(10);
        frontContent.getChildren().add(imageContainer);

        // Displays the total price of the dish
        Text totalPrice = new Text("Total Price: $" + String.format("%.2f", totalPrice(dish)));
        totalPrice.setStyle("-fx-font-size: 20px; -fx-fill: #000000; -fx-background-color: rgba(0, 0, 0, 0.5);");
        totalPrice.setUnderline(true);
        totalPrice.setWrappingWidth(width);
        totalPrice.setTextAlignment(TextAlignment.CENTER);
        frontContent.getChildren().add(totalPrice);

        // Add ingredients to the frontContent VBox
        List<Ingredient> ingredientsList = dish.getIngredients();
        for (Ingredient ingredient : ingredientsList) {
            Text dishIngredients = new Text(ingredient.toString() + " x" + ingredient.getQuantity());
            dishIngredients.setStyle("-fx-font-size: 20px; -fx-fill: #000000; -fx-background-color: rgba(0, 0, 0, 0.5);");
            dishIngredients.setWrappingWidth(width);
            dishIngredients.setTextAlignment(TextAlignment.CENTER);
            frontContent.getChildren().add(dishIngredients);
        }

        frontScrollPane.setContent(frontContent);

        // Add a blur effect when scrolling down
        GaussianBlur blur = new GaussianBlur(0);
        imageView.setEffect(blur);

        FadeTransition fadeInText = new FadeTransition(Duration.millis(300), dishName);
        fadeInText.setFromValue(0);
        fadeInText.setToValue(1);

        FadeTransition fadeOutText = new FadeTransition(Duration.millis(300), dishName);
        fadeOutText.setFromValue(1);
        fadeOutText.setToValue(0);

        // Blurs in
        Transition blurIn = new Transition() {
            {
                setCycleDuration(Duration.millis(300));
            }

            @Override
            protected void interpolate(double frac) {
                blur.setRadius(frac * 10);
            }
        };

        // Blurs out
        Transition blurOut = new Transition() {
            {
                setCycleDuration(Duration.millis(300));
            }

            @Override
            protected void interpolate(double frac) {
                blur.setRadius(10 - (frac * 10));
            }
        };

        // Add the animations to the front of the card.
        frontCard.setOnMouseEntered(event -> {
            fadeInText.play();
            blurIn.play();
            dishName.setVisible(true);
        });
        frontCard.setOnMouseExited(event -> {
            fadeOutText.play();
            blurOut.play();
        });

        frontCard.getChildren().add(frontScrollPane);

        cardContainer.getChildren().addAll(frontCard, backCard);
        StackPane.setAlignment(frontCard, Pos.CENTER);
        StackPane.setAlignment(backCard, Pos.CENTER);

        return cardContainer;
    }

    /**
     * Creates the grocery list card.
     */
    private StackPane createGroceryListCard(String listName, boolean isPlusButton) {
        // This is the card
        StackPane cardContainer = new StackPane();
        cardContainer.setPadding(new Insets(0, -5, 10, 30)); // top, right, bottom, left
        cardContainer.setTranslateY(10);

        StackPane card = new StackPane();
        card.setStyle("-fx-background-color: #388e3c; -fx-background-radius: 15; -fx-padding: 10;");

        // Increase the preferred width and height calculations to make the card bigger
        card.prefWidthProperty().bind(Bindings.createDoubleBinding(() ->
                        Math.min(groceryListContainer.getWidth(), groceryListScrollPane.getHeight() * 0.7),
                groceryListContainer.widthProperty(), groceryListScrollPane.heightProperty()));

        card.prefHeightProperty().bind(card.prefWidthProperty()); // Maintain aspect ratio

        // Increase the minimum size of the card
        card.setMinWidth(150);
        card.setMinHeight(180);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #2e7d32; -fx-background-radius: 15; -fx-padding: 10;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #388e3c; -fx-background-radius: 15; -fx-padding: 10;"));

        Rectangle rectangle = new Rectangle();
        rectangle.setArcWidth(15);
        rectangle.setArcHeight(15);
        rectangle.setFill(isPlusButton ? Color.LIGHTGREEN : Color.LIGHTBLUE);
        rectangle.widthProperty().bind(card.prefWidthProperty());
        rectangle.heightProperty().bind(card.prefHeightProperty());

        // Sets the text of the grocery list card.
        Text text = new Text(listName);
        text.setStyle("-fx-font-size: 34px; -fx-fill: #FFFFFF;");
        StackPane.setAlignment(text, Pos.TOP_LEFT);
        if (Objects.equals(text.getText(), "+")) {
            text.setStyle("-fx-font-size: 60px; -fx-fill: #FFFFFF;");
            StackPane.setAlignment(text, Pos.CENTER);
        }

        // Add the text to the card
        card.getChildren().add(text);

        // If the card is not a plus card. Meaning the card is not the card used to create a new list.
        if (!isPlusButton) {
            // Create a delete button for each list card (excluding the plus button)
            Button deleteButton = new Button();
            Image image = new Image(getClass().getResource("/icons/trashIcon.png").toExternalForm());
            ImageView deleteImageView = new ImageView(image);

            // Set image properties if needed
            deleteImageView.setFitWidth(40);
            deleteImageView.setFitHeight(40);
            deleteImageView.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0;");

            deleteButton.setGraphic(deleteImageView);
            deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");

            // Set the click event to remove the list
            deleteButton.setOnAction(event -> {
                // Create an alert for confirmation
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation");
                alert.setHeaderText("Are you sure you want to delete this grocery list?");
                alert.setContentText("This action cannot be undone.");

                // Add "Yes" and "No" buttons
                ButtonType yesButton = new ButtonType("Yes");
                ButtonType noButton = new ButtonType("No");
                alert.getButtonTypes().setAll(yesButton, noButton);

                // Show the alert and wait for a response
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == yesButton) {
                    // User chose "Yes"
                    removeGroceryList(listName, cardContainer);
                }
            });

            StackPane.setAlignment(deleteButton, Pos.BOTTOM_RIGHT);

            // Scale transition for hover
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), deleteButton);
            scaleUp.setToX(1.1); // Scale up to 110%
            scaleUp.setToY(1.1);

            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), deleteButton);
            scaleDown.setToX(1.0); // Scale down to original size
            scaleDown.setToY(1.0);

            // Add mouse entered and exited events
            deleteButton.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> scaleUp.play());
            deleteButton.addEventHandler(MouseEvent.MOUSE_EXITED, event -> scaleDown.play());

            // Scale down on press
            deleteButton.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
                ScaleTransition scalePress = new ScaleTransition(Duration.millis(100), deleteButton);
                scalePress.setToX(0.95); // Scale down to 95%
                scalePress.setToY(0.95);
                scalePress.play();
            });

            // Scale back up on release
            deleteButton.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
                ScaleTransition scaleRelease = new ScaleTransition(Duration.millis(100), deleteButton);
                scaleRelease.setToX(1.0); // Return to original size
                scaleRelease.setToY(1.0);
                scaleRelease.play();
            });

            // Add the delete button below the text
            card.getChildren().add(deleteButton);
        }


        // Scale transition for hover
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), cardContainer);
        scaleUp.setToX(1.1); // Scale up to 110%
        scaleUp.setToY(1.1);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), cardContainer);
        scaleDown.setToX(1.0); // Scale down to original size
        scaleDown.setToY(1.0);

        // Add mouse entered and exited events
        cardContainer.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> scaleUp.play());
        cardContainer.addEventHandler(MouseEvent.MOUSE_EXITED, event -> scaleDown.play());

        // Scale down on press
        cardContainer.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            ScaleTransition scalePress = new ScaleTransition(Duration.millis(100), cardContainer);
            scalePress.setToX(0.95); // Scale down to 95%
            scalePress.setToY(0.95);
            scalePress.play();
        });

        // Scale back up on release
        cardContainer.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            ScaleTransition scaleRelease = new ScaleTransition(Duration.millis(100), cardContainer);
            scaleRelease.setToX(1.0); // Return to original size
            scaleRelease.setToY(1.0);
            scaleRelease.play();
        });

        cardContainer.getChildren().add(card);
        return cardContainer;
    }

    // Selects the grocery list
    private void selectGroceryList(GroceryList list) {
        selectedGroceryList = list;
        updateSelectedGroceryListUI();
    }

    /**
     * Once the grocery list is selected the UI is updated.
     */
    private void updateSelectedGroceryListUI() {
        // Clear the previous content
        selectedGroceryListContainer.getChildren().clear();
        groceryListNameText.setText(selectedGroceryList.getName());
        double totalPrice = 0;
        for (Dish dish : selectedGroceryList.getItems()) {
            totalPrice += totalPrice(dish);
        }
        priceTextBox.setText("Total Price: $" + String.format("%.2f", totalPrice));

        // Display the items in the selected grocery list
        if (selectedGroceryList != null) {
            for (Dish dish : selectedGroceryList.getItems()) {
                Text dishNameText = new Text(dish.getName());
                Button removeButton = new Button("Remove");
                removeButton.setOnAction(e -> removeDishFromSelectedGroceryList(dish)); // Remove the dish on button press

                VBox dishBox = new VBox(dishNameText, removeButton);
                selectedGroceryListContainer.getChildren().add(dishBox);
            }
        }
    }

    /**
     * When the user click on a dish it is added to the grocery list.
     */
    private void addDishToSelectedGroceryList(Dish dish) {
        if (selectedGroceryList != null) {
            selectedGroceryList.addItem(dish);
            updateSelectedGroceryListUI(); // Refresh the grocery list display
            saveGroceryList();
        }
    }

    /**
     * Removes the dish from the grocery list.
     */
    private void removeDishFromSelectedGroceryList(Dish dish) {
        if (selectedGroceryList != null) {
            selectedGroceryList.removeItem(dish);
            updateSelectedGroceryListUI(); // Refresh the grocery list display
            saveGroceryList();
        }
    }

    /**
     * Shows all the ingredients needed to complete the specified list.
     */
    @FXML
    private void showIngredientsPopup() {
        if (selectedGroceryList == null || selectedGroceryList.getItems().isEmpty()) {
            System.out.println("No grocery list selected or the list is empty.");
            return;
        }

        // Creates a new stage for the popup.
        Stage popupStage = new Stage();
        popupStage.setWidth(1000);
        popupStage.setHeight(1000);
        popupStage.setTitle("Ingredients List");

        VBox mainLayout = new VBox(10);

        // Ingredients grid
        GridPane ingredientsGrid = new GridPane();
        ingredientsGrid.setPadding(new Insets(10));
        ingredientsGrid.setHgap(20);
        ingredientsGrid.setVgap(10);

        // Map to hold ingredients and their total quantities by category
        Map<String, Map<String, Integer>> ingredientsByCategory = new HashMap<>();

        // Aggregate ingredients with their total quantities across all dishes
        for (Dish dish : selectedGroceryList.getItems()) {
            for (Ingredient ingredient : dish.getIngredients()) {
                String category = ingredient.getCategory();
                String ingredientKey = ingredient.getName();

                // Initialize the category if it doesn't exist
                ingredientsByCategory.putIfAbsent(category, new HashMap<>());

                // Add the quantity of the ingredient, summing it if it already exists
                ingredientsByCategory.get(category).merge(ingredientKey, (int) ingredient.getQuantity(), Integer::sum);
            }
        }

        // Populate the ingredients grid
        int rowIndex = 0;
        for (String category : ingredientsByCategory.keySet()) {
            Text categoryText = new Text(category);
            categoryText.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
            ingredientsGrid.add(categoryText, 0, rowIndex, 2, 1);
            rowIndex++;

            Map<String, Integer> ingredients = ingredientsByCategory.get(category);
            List<Text> ingredientTexts = new ArrayList<>();

            for (Map.Entry<String, Integer> entry : ingredients.entrySet()) {
                String ingredientName = entry.getKey();
                int totalQuantity = entry.getValue();
                String displayText = ingredientName + " (x" + totalQuantity + ")";
                ingredientTexts.add(new Text("- " + displayText));
            }

            // Add ingredients to the grid in two columns
            for (int i = 0; i < ingredientTexts.size(); i++) {
                int columnIndex = i % 2;
                ingredientsGrid.add(ingredientTexts.get(i), columnIndex, rowIndex + i / 2);
            }

            // Adjust row index for next category
            rowIndex += (ingredientTexts.size() + 1) / 2;
        }

        ScrollPane ingredientsScrollPane = new ScrollPane();
        ingredientsScrollPane.setContent(ingredientsGrid);
        ingredientsScrollPane.setFitToWidth(true);

        // Create an export button
        Button exportButton = new Button("Export to Text File");
        exportButton.setOnAction(event -> exportIngredientsToFile(ingredientsByCategory));

        // Create HBox for dishes
        HBox dishesLayout = new HBox(10);
        VBox leftDishesBox = new VBox(10);
        VBox rightDishesBox = new VBox(10);

        // Populate dishes
        List<Dish> dishes = selectedGroceryList.getItems();
        int halfSize = (dishes.size() + 1) / 2;

        for (int i = 0; i < dishes.size(); i++) {
            Dish dish = dishes.get(i);
            // Add dish name in bold
            Text dishText = new Text(dish.getName());
            dishText.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

            VBox dishVBox = new VBox(5);
            dishVBox.getChildren().add(dishText);

            // Add ingredients for each dish
            for (Ingredient ingredient : dish.getIngredients()) {
                Text ingredientText = new Text("- " + ingredient.toString());
                dishVBox.getChildren().add(ingredientText);
            }

            // Add the dish VBox to the appropriate column
            if (i < halfSize) {
                leftDishesBox.getChildren().add(dishVBox);
            } else {
                rightDishesBox.getChildren().add(dishVBox);
            }
        }

        dishesLayout.getChildren().addAll(leftDishesBox, rightDishesBox);
        ScrollPane dishesScrollPane = new ScrollPane(dishesLayout);
        dishesScrollPane.setFitToWidth(true);

        // Add both ScrollPanes to the main layout
        mainLayout.getChildren().addAll(ingredientsScrollPane, exportButton, dishesScrollPane);

        Scene scene = new Scene(mainLayout, 600, 600);
        popupStage.setScene(scene);
        popupStage.show();
    }

    /**
     * Used to export all the ingredients of the grocery list to a .txt file.
     */
    private void exportIngredientsToFile(Map<String, Map<String, Integer>> ingredientsByCategory) {
        // Prompts user to save file.
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Ingredients List");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (String category : ingredientsByCategory.keySet()) {
                    writer.write(category + "\n");
                    Map<String, Integer> ingredients = ingredientsByCategory.get(category);
                    for (Map.Entry<String, Integer> entry : ingredients.entrySet()) {
                        String ingredient = entry.getKey();
                        int count = entry.getValue();
                        writer.write("- " + ingredient.toString() + (count > 1 ? " (x" + count + ")" : "") + "\n");
                    }
                    writer.write("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Used to remove grocery list.
     */
    private void removeGroceryList(String listName, StackPane cardContainer) {
        GroceryList listToRemove = groceryLists.stream()
                .filter(list -> list.getName().equals(listName))
                .findFirst()
                .orElse(null);

        if (listToRemove != null) {
            groceryLists.remove(listToRemove); // Remove from the list
            groceryListContainer.getChildren().remove(cardContainer); // Remove the card from the UI
            saveGroceryList(); // Save the updated list to the DataStore
        }

        // Clear the selected list UI if it was the one being removed
        if (selectedGroceryList != null && selectedGroceryList.equals(listToRemove)) {
            selectedGroceryListContainer.getChildren().clear();
            selectedGroceryList = null;
        }
    }

    // Used to change the name of the grocery list.
    @FXML
    private void changeGroceryListName() {
        selectedGroceryList.setName(groceryListNameText.getText());
        loadGroceryList();
    }

    // Gets the total price of the grocery list.
    private double totalPrice(Dish dish) {
        double totalPrice = 0;
        for (Ingredient ingredient : dish.getIngredients()) {
            totalPrice += ingredient.getPrice() * ingredient.getQuantity();
        }
        return totalPrice;
    }

    // Save the grocery list.
    private void saveGroceryList() {
        System.out.println("Saving grocery lists: " + groceryLists);
        DataStore.getInstance().setGroceryLists(groceryLists);
    }

    // Loads the grocery lists.
    private void loadGroceryList() {
        groceryLists = DataStore.getInstance().getGroceryLists();
        groceryListContainer.getChildren().clear();
        // Populate the grocery list UI with loaded lists
        for (int i = groceryLists.size() - 1; i >= 0; i--) {
            GroceryList list = groceryLists.get(i);
            System.out.println(list);
            StackPane listCard = createGroceryListCard(list.getName(), false);
            listCard.setOnMouseClicked(e -> selectGroceryList(list));
            groceryListContainer.getChildren().add(listCard);
        }

        // Add the plus button to create new grocery lists
        addPlusButton();

        System.out.println("Finished");
    }
}
