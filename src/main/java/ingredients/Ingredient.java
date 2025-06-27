package main.java.ingredients;

import java.io.Serializable;

public class Ingredient implements Serializable {
    private String name;
    private double price;
    private double quantity;
    private String category;

    public Ingredient(String name, double price, double quantity, String category) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
    }

    public Ingredient(String name, double price) {
        this(name, price, 1, "Misc");
    }

    public void setQuantity(Double quantity1) {
        quantity = quantity1;
    }


    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantityToAdd) {
        quantity = quantityToAdd;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category1) {
        category = category1;
    }


    @Override
    public String toString() {
        return name + " ($" + String.format("%.2f", price) + ")";
    }
}
