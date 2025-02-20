package main.java.dishes;

import main.java.ingredients.Ingredient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the information for a Dish.
 */
public class Dish implements Serializable {
    private String name; // Name of the dish
    private List<Ingredient> ingredients = new ArrayList<>();
    private double numberOfMeals;
    private String imagePath; // Add imagePath field
    private String instructions;

    // Constructor
    public Dish(String name) {
        this.name = name;
    }

    // Add single ingredient
    public void addIngredient(Ingredient ingredient) {
        ingredients.add(ingredient);
    }

    // Add multiple ingredients
    public void addIngredients(List<Ingredient> ingredients) {
        this.ingredients.addAll(ingredients);
    }

    // Get name of dish
    public String getName() {
        return name;
    }

    // Get list of ingredients in dish
    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    // Get the path of the image of the dish
    public String getImagePath() {
        return imagePath;
    }

    // Set the path of the image for the dish
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    // Set the number of meals for the dish
    public void setNumberOfMeals(double numMeals) {this.numberOfMeals = numMeals;}

    // Get the number of meals for the dish
    public double getNumberOfMeals() {return numberOfMeals;}

    // Set the instructions of the dish
    public void setInstructions(String instructions) {this.instructions = instructions;}

    // Get the instructions of the dish
    public String getInstructions() {return instructions;}

    @Override
    public String toString() {
        return name;
    }
}
