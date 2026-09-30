package com.example.smartpantrytest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs the strict-matching rule (assignment Section 2.3) against the current pantry
 * and shows only the recipes the user can make right now ("You Can Make Now"),
 * plus an optional "Almost There" section for recipes missing exactly one ingredient
 * (bonus stretch goal - Section 8).
 */
public class SuggestedRecipesActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerReady, recyclerAlmost;
    private TextView emptyStateText, almostHeader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        Toolbar toolbar = findViewById(R.id.toolbarSuggestions);
        setSupportActionBar(toolbar);
        setTitle(R.string.title_suggested_recipes);
        toolbar.setNavigationOnClickListener(v -> finish());

        dbHelper = new DatabaseHelper(this);
        recyclerReady = findViewById(R.id.recyclerReadyRecipes);
        recyclerAlmost = findViewById(R.id.recyclerAlmostRecipes);
        emptyStateText = findViewById(R.id.textSuggestionsEmpty);
        almostHeader = findViewById(R.id.textAlmostThereHeader);

        recyclerReady.setLayoutManager(new LinearLayoutManager(this));
        recyclerReady.setNestedScrollingEnabled(false);
        recyclerAlmost.setLayoutManager(new LinearLayoutManager(this));
        recyclerAlmost.setNestedScrollingEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        runMatching();
    }

    private void runMatching() {
        List<PantryItem> pantry = dbHelper.getAllPantryItems();
        List<Recipe> allRecipes = dbHelper.getAllRecipes();

        List<Recipe> readyRecipes = new ArrayList<>();
        List<Recipe> almostRecipes = new ArrayList<>();

        for (Recipe recipe : allRecipes) {
            MatchingUtil.MatchResult result = MatchingUtil.checkRecipe(recipe.getIngredients(), pantry);
            if (result.fullMatch) {
                readyRecipes.add(recipe);
            } else if (result.missingCount == 1) {
                almostRecipes.add(recipe);
            }
        }

        recyclerReady.setAdapter(new RecipeAdapter(readyRecipes, this));
        recyclerAlmost.setAdapter(new RecipeAdapter(almostRecipes, this));

        emptyStateText.setVisibility(readyRecipes.isEmpty() ? View.VISIBLE : View.GONE);
        almostHeader.setVisibility(almostRecipes.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
