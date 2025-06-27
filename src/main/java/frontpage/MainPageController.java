package main.java.frontpage;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MainPageController {

    public VBox contentArea;
    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private ComboBox<String> themeComboBox;

    // Goes to the add ingredients page.
    @FXML
    private void handleAddIngredients() throws IOException {
        loadPage("AddIngredientsPage.fxml");
    }

    // Goes to the add dishes page.
    @FXML
    private void handleAddDishes() throws IOException {
        loadPage("AddDishesPage.fxml");
    }

    // Goes to the add to grocery list page.
    @FXML
    private void handleAddToGroceryList() throws IOException {
        loadPage("AddToGroceryListPage.fxml");
    }

    // Goes to the file controller page.
    @FXML
    private void handleViewFileController() throws IOException {
        loadPage("FileControllerPage.fxml");
    }

    /**
     * Loads the page.
     */
    private void loadPage(String fxmlFile) {
        try {
            URL resource = getClass().getResource("/fxml/" + fxmlFile);

            if (resource == null) {
                System.out.println("FXML file not found: " + fxmlFile);
                return;
            }

            // Load the FXML
            FXMLLoader loader = new FXMLLoader(resource);
            Parent newContent = loader.load();

            // Replace the content in the contentArea
            contentArea.getChildren().clear();
            contentArea.getChildren().add(newContent);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}