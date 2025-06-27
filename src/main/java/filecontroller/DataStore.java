package main.java.filecontroller;

import main.java.grocerylist.GroceryList;
import main.java.ingredients.Ingredient;
import main.java.dishes.Dish;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all the logic of saving data.
 */
public class DataStore implements Serializable {
    private static final long serialVersionUID = 1L;

    private static DataStore instance;

    private List<Ingredient> allIngredientsList = new ArrayList<>();
    private List<Dish> allDishesList = new ArrayList<>();
    private List<GroceryList> groceryLists = new ArrayList<>(); // List to track grocery lists

    private DataStore() {}

    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    // Serialize DataStore to a file
    public static void saveDataStore() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("datastore.ser"))) {
            out.writeObject(instance);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Deserialize DataStore from a file
    public static void loadDataStore() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("datastore.ser"))) {
            instance = (DataStore) in.readObject();
        } catch (FileNotFoundException e) {
            instance = new DataStore(); // Create a new instance if file doesn't exist
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Method to save grocery lists to DataStore
    public void setGroceryLists(List<GroceryList> groceryLists1) {
        groceryLists = new ArrayList<>(groceryLists1);
    }

    // Method to retrieve grocery lists from DataStore
    public List<GroceryList> getGroceryLists() {
        return new ArrayList<>(groceryLists); // Return a copy of the grocery lists
    }

    // Getters and setters for other lists
    public List<Ingredient> getAllIngredientsList() {
        return new ArrayList<>(allIngredientsList);
    }
    public List<Dish> getAddedDishList() {
        return new ArrayList<>(allDishesList);
    }
    public void setAllIngredientsList(List<Ingredient> ingredients) {
        allIngredientsList = new ArrayList<>(ingredients);
    }
    public void setDishList(List<Dish> dishes) {
        allDishesList = new ArrayList<>(dishes);
    }

    /**
     * Loads and merge data into the DataStore
     */
    public static void loadAndMergeDataStore(String filePath) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            DataStore newDataStore = (DataStore) in.readObject();
            getInstance().mergeDataStore(newDataStore);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Merger data into DataStore
     */
    public void mergeDataStore(DataStore newDataStore) {
        // Merge ingredients
        for (Ingredient ingredient : newDataStore.getAllIngredientsList()) {
            boolean isDuplicate = false;
            for (Ingredient existingIngredient : allIngredientsList) {
                if (existingIngredient.getName().equalsIgnoreCase(ingredient.getName()) &&
                        existingIngredient.getCategory().equalsIgnoreCase(ingredient.getCategory())) {
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate) {
                allIngredientsList.add(ingredient);
            }
        }

        // Merge dishes
        for (Dish dish : newDataStore.getAddedDishList()) {
            if (!allDishesList.contains(dish)) {
                allDishesList.add(dish);
            }
        }

        // Merge grocery lists
        for (GroceryList groceryList : newDataStore.getGroceryLists()) {
            if (!groceryLists.contains(groceryList)) {
                groceryLists.add(groceryList);
            }
        }
    }
}
