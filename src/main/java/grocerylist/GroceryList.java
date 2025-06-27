package main.java.grocerylist;

import main.java.dishes.Dish;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a grocery list and holds Dishes.
 */
public class GroceryList implements Serializable {
    private String name; // Name of the grocery list
    private List<Dish> items; // All the dishes in the grocery list

    // Constructor
    public GroceryList(String name) {
        this.name = name;
        this.items = new ArrayList<>();
    }

    // Gets the name
    public String getName() {
        return name;
    }

    // Sets the name
    public void setName(String name1) {
        name = name1;
    }

    // Gets all the dishes
    public List<Dish> getItems() {
        return items;
    }

    // Adds a dish
    public void addItem(Dish item) {
        items.add(item);
    }

    // Removes a dish
    public void removeItem(Dish item) {
        items.remove(item);
    }
}
