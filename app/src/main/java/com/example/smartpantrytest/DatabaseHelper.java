package com.example.smartpantrytest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles all local persistence for the app using SQLiteOpenHelper.
 *
 * Two kinds of data live here:
 *  - "pantry": fully editable by the user (CRUD requirement, Section 3.2).
 *  - "recipes" / "recipe_ingredients": a fixed cookbook of 18 recipes, seeded once
 *    the first time the database is created (Section 2.2 requires 15-20 recipes).
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    // ---- pantry table ----
    public static final String TABLE_PANTRY = "pantry";
    public static final String COL_PANTRY_ID = "_id";
    public static final String COL_PANTRY_NAME = "name";
    public static final String COL_PANTRY_QTY = "quantity";
    public static final String COL_PANTRY_UNIT = "unit";
    public static final String COL_PANTRY_EXPIRY = "expiry_date";

    // ---- recipes table ----
    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_RECIPE_ID = "_id";
    public static final String COL_RECIPE_NAME = "name";
    public static final String COL_RECIPE_STEPS = "steps";

    // ---- recipe_ingredients table ----
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COL_RI_ID = "_id";
    public static final String COL_RI_RECIPE_ID = "recipe_id";
    public static final String COL_RI_NAME = "ingredient_name";
    public static final String COL_RI_QTY = "quantity";
    public static final String COL_RI_UNIT = "unit";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " (" +
                COL_PANTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PANTRY_NAME + " TEXT NOT NULL, " +
                COL_PANTRY_QTY + " REAL NOT NULL, " +
                COL_PANTRY_UNIT + " TEXT NOT NULL, " +
                COL_PANTRY_EXPIRY + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " (" +
                COL_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RECIPE_NAME + " TEXT NOT NULL, " +
                COL_RECIPE_STEPS + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                COL_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RI_RECIPE_ID + " INTEGER NOT NULL, " +
                COL_RI_NAME + " TEXT NOT NULL, " +
                COL_RI_QTY + " REAL NOT NULL, " +
                COL_RI_UNIT + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COL_RI_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COL_RECIPE_ID + "))");

        seedRecipes(db);
        seedPantry(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // ==================== Pantry CRUD ====================

    public long addPantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(TABLE_PANTRY, null, pantryToValues(item));
    }

    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        return db.update(TABLE_PANTRY, pantryToValues(item),
                COL_PANTRY_ID + "=?", new String[]{String.valueOf(item.getId())});
    }

    public void deletePantryItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_PANTRY, COL_PANTRY_ID + "=?", new String[]{String.valueOf(id)});
    }

    public PantryItem getPantryItem(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PANTRY, null, COL_PANTRY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        PantryItem item = null;
        if (c.moveToFirst()) {
            item = cursorToPantryItem(c);
        }
        c.close();
        return item;
    }

    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PANTRY, null, null, null, null, null, COL_PANTRY_NAME + " ASC");
        while (c.moveToNext()) {
            list.add(cursorToPantryItem(c));
        }
        c.close();
        return list;
    }

    private ContentValues pantryToValues(PantryItem item) {
        ContentValues cv = new ContentValues();
        cv.put(COL_PANTRY_NAME, item.getName());
        cv.put(COL_PANTRY_QTY, item.getQuantity());
        cv.put(COL_PANTRY_UNIT, item.getUnit());
        cv.put(COL_PANTRY_EXPIRY, item.getExpiryDate());
        return cv;
    }

    private PantryItem cursorToPantryItem(Cursor c) {
        PantryItem item = new PantryItem();
        item.setId(c.getLong(c.getColumnIndexOrThrow(COL_PANTRY_ID)));
        item.setName(c.getString(c.getColumnIndexOrThrow(COL_PANTRY_NAME)));
        item.setQuantity(c.getDouble(c.getColumnIndexOrThrow(COL_PANTRY_QTY)));
        item.setUnit(c.getString(c.getColumnIndexOrThrow(COL_PANTRY_UNIT)));
        item.setExpiryDate(c.getString(c.getColumnIndexOrThrow(COL_PANTRY_EXPIRY)));
        return item;
    }

    // ==================== Recipes (read-only in the app) ====================

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RECIPES, null, null, null, null, null, COL_RECIPE_NAME + " ASC");
        while (c.moveToNext()) {
            Recipe r = new Recipe();
            r.setId(c.getLong(c.getColumnIndexOrThrow(COL_RECIPE_ID)));
            r.setName(c.getString(c.getColumnIndexOrThrow(COL_RECIPE_NAME)));
            r.setSteps(c.getString(c.getColumnIndexOrThrow(COL_RECIPE_STEPS)));
            r.setIngredients(getIngredientsForRecipe(r.getId()));
            recipes.add(r);
        }
        c.close();
        return recipes;
    }

    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RECIPES, null, COL_RECIPE_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        Recipe r = null;
        if (c.moveToFirst()) {
            r = new Recipe();
            r.setId(c.getLong(c.getColumnIndexOrThrow(COL_RECIPE_ID)));
            r.setName(c.getString(c.getColumnIndexOrThrow(COL_RECIPE_NAME)));
            r.setSteps(c.getString(c.getColumnIndexOrThrow(COL_RECIPE_STEPS)));
            r.setIngredients(getIngredientsForRecipe(r.getId()));
        }
        c.close();
        return r;
    }

    private List<RecipeIngredient> getIngredientsForRecipe(long recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RECIPE_INGREDIENTS, null, COL_RI_RECIPE_ID + "=?",
                new String[]{String.valueOf(recipeId)}, null, null, null);
        while (c.moveToNext()) {
            RecipeIngredient ri = new RecipeIngredient();
            ri.setId(c.getLong(c.getColumnIndexOrThrow(COL_RI_ID)));
            ri.setRecipeId(c.getLong(c.getColumnIndexOrThrow(COL_RI_RECIPE_ID)));
            ri.setIngredientName(c.getString(c.getColumnIndexOrThrow(COL_RI_NAME)));
            ri.setQuantity(c.getDouble(c.getColumnIndexOrThrow(COL_RI_QTY)));
            ri.setUnit(c.getString(c.getColumnIndexOrThrow(COL_RI_UNIT)));
            list.add(ri);
        }
        c.close();
        return list;
    }

    // ==================== Seed data (runs once, on first DB creation) ====================

    private void seedRecipes(SQLiteDatabase db) {
        long r1 = insertRecipe(db, "Scrambled Eggs",
                "1. Crack eggs into a bowl and whisk with milk.\n2. Melt butter in a pan on low heat.\n3. Pour in eggs, stir gently until soft and set.\n4. Season with salt and serve.");
        insertIngredient(db, r1, "egg", 3, "unit");
        insertIngredient(db, r1, "milk", 50, "ml");
        insertIngredient(db, r1, "butter", 10, "g");
        insertIngredient(db, r1, "salt", 1, "tsp");

        long r2 = insertRecipe(db, "Tomato Onion Pasta",
                "1. Boil pasta until al dente.\n2. Fry chopped onion and garlic in oil.\n3. Add chopped tomato and simmer 10 minutes.\n4. Toss pasta into the sauce and serve.");
        insertIngredient(db, r2, "pasta", 200, "g");
        insertIngredient(db, r2, "tomato", 2, "unit");
        insertIngredient(db, r2, "onion", 1, "unit");
        insertIngredient(db, r2, "garlic", 2, "unit");
        insertIngredient(db, r2, "oil", 15, "ml");

        long r3 = insertRecipe(db, "Grilled Cheese Sandwich",
                "1. Butter one side of each bread slice.\n2. Place cheese between the un-buttered sides.\n3. Grill in a pan until golden on both sides.");
        insertIngredient(db, r3, "bread", 2, "unit");
        insertIngredient(db, r3, "cheese", 2, "unit");
        insertIngredient(db, r3, "butter", 10, "g");

        long r4 = insertRecipe(db, "Chicken Rice Bowl",
                "1. Cook rice until tender.\n2. Fry chicken pieces with onion and garlic until cooked through.\n3. Serve chicken over rice.");
        insertIngredient(db, r4, "chicken", 200, "g");
        insertIngredient(db, r4, "rice", 150, "g");
        insertIngredient(db, r4, "onion", 1, "unit");
        insertIngredient(db, r4, "garlic", 1, "unit");

        long r5 = insertRecipe(db, "Garlic Butter Rice",
                "1. Cook rice until tender.\n2. Melt butter and fry chopped garlic until fragrant.\n3. Mix through the rice with a pinch of salt.");
        insertIngredient(db, r5, "rice", 150, "g");
        insertIngredient(db, r5, "butter", 20, "g");
        insertIngredient(db, r5, "garlic", 2, "unit");
        insertIngredient(db, r5, "salt", 1, "tsp");

        long r6 = insertRecipe(db, "Veggie Omelette",
                "1. Whisk eggs with a pinch of salt.\n2. Add chopped onion and tomato.\n3. Cook in a pan until set, fold and serve.");
        insertIngredient(db, r6, "egg", 3, "unit");
        insertIngredient(db, r6, "onion", 1, "unit");
        insertIngredient(db, r6, "tomato", 1, "unit");
        insertIngredient(db, r6, "salt", 1, "tsp");

        long r7 = insertRecipe(db, "Potato Salad",
                "1. Boil potatoes and eggs until cooked.\n2. Chop and mix together.\n3. Add oil and salt to taste.");
        insertIngredient(db, r7, "potato", 3, "unit");
        insertIngredient(db, r7, "egg", 2, "unit");
        insertIngredient(db, r7, "salt", 1, "tsp");
        insertIngredient(db, r7, "oil", 15, "ml");

        long r8 = insertRecipe(db, "Tomato Soup",
                "1. Fry onion and garlic in oil until soft.\n2. Add chopped tomato and a little water.\n3. Simmer 15 minutes, blend, season with salt.");
        insertIngredient(db, r8, "tomato", 4, "unit");
        insertIngredient(db, r8, "onion", 1, "unit");
        insertIngredient(db, r8, "garlic", 1, "unit");
        insertIngredient(db, r8, "oil", 15, "ml");
        insertIngredient(db, r8, "salt", 1, "tsp");

        long r9 = insertRecipe(db, "Cheese Toast",
                "1. Toast the bread.\n2. Melt cheese on top under a grill or in a pan.");
        insertIngredient(db, r9, "bread", 2, "unit");
        insertIngredient(db, r9, "cheese", 1, "unit");

        long r10 = insertRecipe(db, "Buttered Toast",
                "1. Toast the bread.\n2. Spread butter while warm.");
        insertIngredient(db, r10, "bread", 2, "unit");
        insertIngredient(db, r10, "butter", 10, "g");

        long r11 = insertRecipe(db, "Carrot Soup",
                "1. Fry onion and garlic in oil.\n2. Add chopped carrot and water, simmer until soft.\n3. Blend until smooth.");
        insertIngredient(db, r11, "carrot", 3, "unit");
        insertIngredient(db, r11, "onion", 1, "unit");
        insertIngredient(db, r11, "garlic", 1, "unit");
        insertIngredient(db, r11, "oil", 15, "ml");

        long r12 = insertRecipe(db, "Spinach Salad",
                "1. Wash spinach leaves.\n2. Dress with oil, lemon juice and a pinch of salt.");
        insertIngredient(db, r12, "spinach", 100, "g");
        insertIngredient(db, r12, "lemon", 1, "unit");
        insertIngredient(db, r12, "oil", 15, "ml");
        insertIngredient(db, r12, "salt", 1, "tsp");

        long r13 = insertRecipe(db, "Bean Rice",
                "1. Cook rice until tender.\n2. Fry onion, add beans and heat through.\n3. Mix with rice and season with salt.");
        insertIngredient(db, r13, "beans", 150, "g");
        insertIngredient(db, r13, "rice", 150, "g");
        insertIngredient(db, r13, "onion", 1, "unit");
        insertIngredient(db, r13, "salt", 1, "tsp");

        long r14 = insertRecipe(db, "Chicken Garlic Stir Fry",
                "1. Heat oil in a pan.\n2. Fry chicken until browned.\n3. Add garlic and onion, stir fry until cooked through.");
        insertIngredient(db, r14, "chicken", 200, "g");
        insertIngredient(db, r14, "garlic", 2, "unit");
        insertIngredient(db, r14, "onion", 1, "unit");
        insertIngredient(db, r14, "oil", 15, "ml");

        long r15 = insertRecipe(db, "Pancakes",
                "1. Whisk flour, egg, milk and sugar into a batter.\n2. Melt a little butter in a pan.\n3. Pour batter and cook until bubbles form, flip and cook the other side.");
        insertIngredient(db, r15, "flour", 200, "g");
        insertIngredient(db, r15, "egg", 2, "unit");
        insertIngredient(db, r15, "milk", 150, "ml");
        insertIngredient(db, r15, "sugar", 20, "g");
        insertIngredient(db, r15, "butter", 10, "g");

        long r16 = insertRecipe(db, "Sugar Cookies",
                "1. Cream butter and sugar together.\n2. Mix in egg, then flour, to form a dough.\n3. Bake at 180C for 10-12 minutes.");
        insertIngredient(db, r16, "flour", 200, "g");
        insertIngredient(db, r16, "sugar", 100, "g");
        insertIngredient(db, r16, "butter", 50, "g");
        insertIngredient(db, r16, "egg", 1, "unit");

        long r17 = insertRecipe(db, "Lemon Rice",
                "1. Cook rice until tender.\n2. Stir through oil, lemon juice and a pinch of salt.");
        insertIngredient(db, r17, "rice", 150, "g");
        insertIngredient(db, r17, "lemon", 1, "unit");
        insertIngredient(db, r17, "oil", 15, "ml");
        insertIngredient(db, r17, "salt", 1, "tsp");

        long r18 = insertRecipe(db, "Cheesy Pasta Bake",
                "1. Boil pasta until al dente.\n2. Mix with milk, butter and grated cheese.\n3. Bake at 180C until golden on top.");
        insertIngredient(db, r18, "pasta", 200, "g");
        insertIngredient(db, r18, "cheese", 2, "unit");
        insertIngredient(db, r18, "milk", 100, "ml");
        insertIngredient(db, r18, "butter", 10, "g");

        long r19 = insertRecipe(db, "Egg Fried Rice",
                "1. Scramble eggs in a hot pan, set aside.\n2. Fry onion and garlic in oil.\n3. Add rice, stir through, then fold the egg back in with salt.");
        insertIngredient(db, r19, "rice", 150, "g");
        insertIngredient(db, r19, "egg", 2, "unit");
        insertIngredient(db, r19, "onion", 1, "unit");
        insertIngredient(db, r19, "garlic", 1, "unit");
        insertIngredient(db, r19, "oil", 15, "ml");
        insertIngredient(db, r19, "salt", 1, "tsp");

        long r20 = insertRecipe(db, "Cheesy Scrambled Eggs",
                "1. Whisk eggs with milk.\n2. Cook gently in butter, folding in grated cheese near the end.\n3. Season with salt.");
        insertIngredient(db, r20, "egg", 3, "unit");
        insertIngredient(db, r20, "milk", 30, "ml");
        insertIngredient(db, r20, "butter", 10, "g");
        insertIngredient(db, r20, "cheese", 1, "unit");
        insertIngredient(db, r20, "salt", 1, "tsp");

        long r21 = insertRecipe(db, "Garlic Bread",
                "1. Mix soft butter with crushed garlic.\n2. Spread onto bread slices.\n3. Grill until golden.");
        insertIngredient(db, r21, "bread", 2, "unit");
        insertIngredient(db, r21, "butter", 20, "g");
        insertIngredient(db, r21, "garlic", 2, "unit");

        long r22 = insertRecipe(db, "Simple Lemon Chicken",
                "1. Season chicken with salt.\n2. Pan-fry in oil until cooked through.\n3. Squeeze lemon juice over before serving.");
        insertIngredient(db, r22, "chicken", 200, "g");
        insertIngredient(db, r22, "lemon", 1, "unit");
        insertIngredient(db, r22, "oil", 15, "ml");
        insertIngredient(db, r22, "salt", 1, "tsp");
    }

    /**
     * Seeds a handful of starter pantry items on first run, so the app isn't
     * empty out of the box and a few recipes are immediately suggestable
     * (useful for demoing the strict-matching rule).
     */
    private void seedPantry(SQLiteDatabase db) {
        insertPantrySeed(db, "egg", 6, "unit");
        insertPantrySeed(db, "milk", 500, "ml");
        insertPantrySeed(db, "butter", 100, "g");
        insertPantrySeed(db, "salt", 50, "g");
        insertPantrySeed(db, "onion", 3, "unit");
        insertPantrySeed(db, "garlic", 5, "unit");
        insertPantrySeed(db, "bread", 6, "unit");
    }

    private void insertPantrySeed(SQLiteDatabase db, String name, double qty, String unit) {
        ContentValues cv = new ContentValues();
        cv.put(COL_PANTRY_NAME, name);
        cv.put(COL_PANTRY_QTY, qty);
        cv.put(COL_PANTRY_UNIT, unit);
        cv.put(COL_PANTRY_EXPIRY, "");
        db.insert(TABLE_PANTRY, null, cv);
    }

    private long insertRecipe(SQLiteDatabase db, String name, String steps) {
        ContentValues cv = new ContentValues();
        cv.put(COL_RECIPE_NAME, name);
        cv.put(COL_RECIPE_STEPS, steps);
        return db.insert(TABLE_RECIPES, null, cv);
    }

    private void insertIngredient(SQLiteDatabase db, long recipeId, String name, double qty, String unit) {
        ContentValues cv = new ContentValues();
        cv.put(COL_RI_RECIPE_ID, recipeId);
        cv.put(COL_RI_NAME, name);
        cv.put(COL_RI_QTY, qty);
        cv.put(COL_RI_UNIT, unit);
        db.insert(TABLE_RECIPE_INGREDIENTS, null, cv);
    }
}