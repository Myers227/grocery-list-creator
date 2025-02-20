package main.java.frontpage;

import main.java.filecontroller.DataStore;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * This is the main method for the whole application.
 */
public class DishTrackerApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            DataStore.loadDataStore(); //Loads data, if available
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainPage.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/green-theme.css").toExternalForm());

            MainPageController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);

            primaryStage.setTitle("Dish Tracker");
            primaryStage.setScene(scene);
            primaryStage.setWidth(1300);  // Set width
            primaryStage.setHeight(720); // Set height
            primaryStage.setMinWidth(1000);  // Set width
            primaryStage.setMinHeight(600); // Set height
            primaryStage.show();

            // Handle application close event
            primaryStage.setOnCloseRequest(event -> {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * When the application closes save all data.
     */
    @Override
    public void stop() throws Exception {
        DataStore.saveDataStore();
    }

    /**
     * Main method
     * */
    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
