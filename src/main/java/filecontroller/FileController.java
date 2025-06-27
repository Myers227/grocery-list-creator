package main.java.filecontroller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

/**
 * Handles all File logic
 */
public class FileController {

    private Stage primaryStage;

    // Method to export datastore.ser to user-specified location
    public void exportDataStore() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Data Store");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Data Files", "*.ser"));
        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            File dataFile = new File("datastore.ser");
            if (dataFile.exists()) {
                try {
                    // Copy datastore.ser to the specified location
                    java.nio.file.Files.copy(dataFile.toPath(), file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Imports a data file and merge it with existing data
     */
    public void importDataStore() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Data Store");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Data Files", "*.ser"));
        File file = fileChooser.showOpenDialog(primaryStage);

        if (file != null) {
            // Pass the file path as a string to loadAndMergeDataStore
            DataStore.getInstance().loadAndMergeDataStore(file.getAbsolutePath());
        }
    }
}
