package com.example.smartpantrytest;

/**
 * One required ingredient line belonging to a Recipe (e.g. "2 unit tomato").
 */
public class RecipeIngredient {
    private long id;
    private long recipeId;
    private String ingredientName;
    private double quantity;
    private String unit;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getRecipeId() { return recipeId; }
    public void setRecipeId(long recipeId) { this.recipeId = recipeId; }

    public String getIngredientName() { return ingredientName; }
    public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
