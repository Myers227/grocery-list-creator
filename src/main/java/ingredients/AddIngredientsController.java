package main.java.ingredients;

import main.java.filecontroller.DataStore;
import main.java.frontpage.MainPageController;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;

public class AddIngredientsController {
    @FXML
    private TextField ingredientNameInput;
    @FXML
    private TextField ingredientPriceInput;
    @FXML
    private ListView<Ingredient> allIngredientsListView;
    @FXML
    private Button backButton;
    @FXML
    private Accordion categoryAccordion;
    @FXML
    private ComboBox<String> categoryComboBox;

    private ObservableList<Ingredient> allIngredientsList;
    private Map<String, ListView<Ingredient>> categoryPaneMap = new HashMap<>();

    @FXML
    public void initialize() {
        loadData();
        initializeCategories();
        populateTitledPanes();
        //test();
    }

    private void loadData() {
        DataStore dataStore = DataStore.getInstance();
        allIngredientsList = FXCollections.observableArrayList(dataStore.getAllIngredientsList());
        allIngredientsListView.setItems(allIngredientsList);
        allIngredientsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void initializeCategories() {
        // Define the list of categories
        List<String> categories = new ArrayList<>(List.of("Dairy", "Meat", "Bread", "Baking", "Pasta", "Canned Goods", "Misc", "Produce", "Freezer", "Vegan", "International", "Snacks", "Beverages"));

        // Populate the ComboBox with categories
        ObservableList<String> categoryList = FXCollections.observableArrayList(categories);
        categoryList.sort(Comparator.naturalOrder());
        categoryComboBox.setItems(categoryList);

        // Clear existing panes to avoid duplicates
        categoryAccordion.getPanes().clear();

        categories.sort(Comparator.naturalOrder());

        // Create and add TitledPanes to the Accordion based on categories
        for (String category : categories) {
            TitledPane titledPane = new TitledPane();
            titledPane.setText(category);
            ListView<Ingredient> listView = new ListView<>();
            titledPane.setContent(listView);

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
                    // When expanded, ensure no scaling effect is active
                    titledPane.setScaleX(1);
                    titledPane.setScaleY(1);
                }
            });

            // Add TitledPane to Accordion
            categoryAccordion.getPanes().add(titledPane);

            // Map category to ListView
            categoryPaneMap.put(category, listView);
        }
    }

    private void populateTitledPanes() {
        for (Ingredient ingredient : allIngredientsList) {
            String category = ingredient.getCategory();
            ListView<Ingredient> ingredientListView = categoryPaneMap.get(category);

            if (ingredientListView != null) {
                ObservableList<Ingredient> items = ingredientListView.getItems();
                if (items == null) {
                    items = FXCollections.observableArrayList();
                }
                if (!items.contains(ingredient)) {
                    items.add(ingredient);
                }
                ingredientListView.setItems(items);
            }
        }
    }

    @FXML
    private void handleAddIngredient() {
        String ingredientName = ingredientNameInput.getText();
        String ingredientPriceText = ingredientPriceInput.getText();
        String ingredientCategory = categoryComboBox.getValue();

        if (ingredientName.isEmpty() || ingredientPriceText.isEmpty() || ingredientCategory == null) {
            showAlert(AlertType.ERROR, "Invalid Input", "Please enter name, price, and select a category for the ingredient.");
            return;
        }

        // Check if a dish with the same name already exists
        Ingredient existingIngredient = null;
        for (Ingredient ingredient : allIngredientsList) {
            if (ingredient.getName().equalsIgnoreCase(ingredientName) && Objects.equals(ingredient.getCategory(), ingredientCategory)) {
                existingIngredient = ingredient;
                break;
            }
        }

        // If found, remove the existing dish
        if (existingIngredient != null) {
            // Remove selected ingredient from each category ListView
            ListView<Ingredient> categoryListView = categoryPaneMap.get(ingredientCategory);
            if (categoryListView != null) {
                // Remove the ingredient from the category-specific ListView
                ObservableList<Ingredient> categoryItems = categoryListView.getItems();
                if (categoryItems != null) {
                    categoryItems.remove(existingIngredient);
                }
            }
            allIngredientsList.remove(existingIngredient);
        }

        try {
            double ingredientPrice = Double.parseDouble(ingredientPriceText);
            Ingredient newIngredient = new Ingredient(ingredientName, ingredientPrice, 1, ingredientCategory);
            System.out.println(newIngredient.getCategory());
            allIngredientsList.add(newIngredient);
            DataStore.getInstance().getAllIngredientsList().add(newIngredient);

            // Get the corresponding ListView for the category
            ListView<Ingredient> ingredientListView = categoryPaneMap.get(ingredientCategory);
            if (ingredientListView != null) {
                ObservableList<Ingredient> items = ingredientListView.getItems();
                if (items == null) {
                    items = FXCollections.observableArrayList();
                }
                items.add(newIngredient);
                ingredientListView.setItems(items);
            } else {
                showAlert(AlertType.ERROR, "Category Not Found", "The selected category does not exist.");
            }

            // Clear input fields
            ingredientNameInput.clear();
            ingredientPriceInput.clear();
            categoryComboBox.getSelectionModel().clearSelection();

            saveData(); // Save DataStore after adding ingredient
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Invalid Price", "Please enter a valid number for the price.");
        }
    }

    @FXML
    private void handleRemoveSelected() {
        // Get selected ingredients
        ObservableList<Ingredient> selectedIngredients = FXCollections.observableArrayList(allIngredientsListView.getSelectionModel().getSelectedItems());

        if (selectedIngredients.isEmpty()) {
            showAlert(AlertType.WARNING, "No Selection", "Please select an ingredient to remove.");
            return;
        }

        // Remove selected ingredients from the DataStore first
        DataStore.getInstance().getAllIngredientsList().removeAll(selectedIngredients);

        // Remove selected ingredients from the main allIngredientsList
        allIngredientsList.removeAll(selectedIngredients);

        // Remove selected ingredients from each category ListView
        for (Ingredient selectedIngredient : selectedIngredients) {
            String category = selectedIngredient.getCategory();
            ListView<Ingredient> categoryListView = categoryPaneMap.get(category);

            if (categoryListView != null) {
                // Remove the ingredient from the category-specific ListView
                ObservableList<Ingredient> categoryItems = categoryListView.getItems();
                if (categoryItems != null) {
                    categoryItems.remove(selectedIngredient);
                }
            }
        }

        // Clear selection after removal to avoid errors
        allIngredientsListView.getSelectionModel().clearSelection();

        // Save DataStore after removing ingredients
        saveData();
    }


    @FXML
    private void handleBackButton() throws IOException {
        // Save current lists to DataStore
        DataStore dataStore = DataStore.getInstance();
        dataStore.setAllIngredientsList(allIngredientsList);

        // Switch to another page
        Stage stage = (Stage) backButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../resources/fxml/MainPage.fxml"));
        Scene mainPageScene = new Scene(loader.load());

        MainPageController mainPageController = loader.getController();
        mainPageController.setPrimaryStage(stage);

        stage.setScene(mainPageScene);
    }

    private void saveData() {
        // Save current lists to DataStore
        DataStore dataStore = DataStore.getInstance();
        dataStore.setAllIngredientsList(allIngredientsList);
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void test() {
        allIngredientsList.add(new Ingredient("Rice", 3, 1, "Pasta"));
        allIngredientsList.add(new Ingredient("Chicken", 9, 1, "Meat"));
        allIngredientsList.add(new Ingredient("Beef", 7, 1, "Meat"));
        allIngredientsList.add(new Ingredient("Pork", 7, 1, "Meat"));
        allIngredientsList.add(new Ingredient("Shells", 3, 1, "Pasta"));
        allIngredientsList.add(new Ingredient("Noodles", 3, 1, "Pasta"));
        allIngredientsList.add(new Ingredient("Orzo", 3, 1, "Pasta"));
        allIngredientsList.add(new Ingredient("Sliced Bread", 5, 1, "Bread"));
        allIngredientsList.add(new Ingredient("Sub Rolls", 6, 1, "Bread"));
        allIngredientsList.add(new Ingredient("Tortilla", 3, 1, "Bread"));
        allIngredientsList.add(new Ingredient("Milk", 3, 1, "Dairy"));
        allIngredientsList.add(new Ingredient("Sour Cream", 3, 1, "Dairy"));
        allIngredientsList.add(new Ingredient("Butter", 3, 1, "Dairy"));
        allIngredientsList.add(new Ingredient("Cheese", 5, 1, "Dairy"));
        allIngredientsList.add(new Ingredient("Lettuce", 4, 1, "Produce"));
        allIngredientsList.add(new Ingredient("Tomato", 1, 1, "Produce"));
        allIngredientsList.add(new Ingredient("Lemon", 1, 1, "Produce"));
        allIngredientsList.add(new Ingredient("Potato", 2, 1, "Produce"));
        saveData(); // Save DataStore after adding ingredient
    }
}
