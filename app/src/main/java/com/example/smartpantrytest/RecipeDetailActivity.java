package com.example.smartpantrytest;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * Recipe Detail screen: shows the full ingredient list and preparation method
 * for a single recipe selected from the Suggested Recipes screen.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        Toolbar toolbar = findViewById(R.id.toolbarRecipeDetail);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = dbHelper.getRecipe(recipeId);

        TextView nameView = findViewById(R.id.textDetailRecipeName);
        TextView ingredientsView = findViewById(R.id.textDetailIngredients);
        TextView stepsView = findViewById(R.id.textDetailSteps);

        if (recipe == null) {
            nameView.setText(R.string.recipe_not_found);
            return;
        }

        setTitle(recipe.getName());
        nameView.setText(recipe.getName());

        StringBuilder ingredientsText = new StringBuilder();
        for (RecipeIngredient ri : recipe.getIngredients()) {
            ingredientsText.append("\u2022 ")
                    .append(formatQuantity(ri.getQuantity())).append(" ")
                    .append(ri.getUnit()).append(" ")
                    .append(ri.getIngredientName()).append("\n");
        }
        ingredientsView.setText(ingredientsText.toString().trim());
        stepsView.setText(recipe.getSteps());
    }

    private String formatQuantity(double q) {
        if (q == Math.floor(q)) return String.valueOf((int) q);
        return String.valueOf(q);
    }
}
