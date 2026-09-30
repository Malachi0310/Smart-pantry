package com.example.smartpantrytest;

import java.util.List;

/**
 * A recipe: a name, a method (steps) and a list of required ingredients.
 */
public class Recipe {
    private long id;
    private String name;
    private String steps;
    private List<RecipeIngredient> ingredients;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSteps() { return steps; }
    public void setSteps(String steps) { this.steps = steps; }

    public List<RecipeIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<RecipeIngredient> ingredients) { this.ingredients = ingredients; }
}
