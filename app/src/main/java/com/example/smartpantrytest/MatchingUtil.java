package com.example.smartpantrytest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central place for the assignment's "strict matching" business logic (Section 2.3).
 *
 * A recipe is only "suggested" if EVERY required ingredient is present in the pantry
 * in at least the required quantity. This class also makes the matching reasonably
 * robust to simple real-world messiness:
 *   - singular vs plural ingredient names ("tomato" vs "tomatoes")
 *   - different but compatible units ("1000 g" pantry item covers a "1 kg" requirement)
 */
public class MatchingUtil {

    private enum Category { WEIGHT, VOLUME, COUNT, UNKNOWN }

    /** Result of checking one recipe against the current pantry contents. */
    public static class MatchResult {
        public boolean fullMatch;
        public int missingCount;
        public List<String> missingIngredients = new ArrayList<>();
    }

    /** Normalises an ingredient name so simple plurals match their singular form. */
    public static String normalizeName(String rawName) {
        if (rawName == null) return "";
        String s = rawName.trim().toLowerCase();
        if (s.endsWith("es") && s.length() > 3) {
            s = s.substring(0, s.length() - 2);      // tomatoes -> tomato
        } else if (s.endsWith("s") && !s.endsWith("ss") && s.length() > 2) {
            s = s.substring(0, s.length() - 1);       // onions -> onion
        }
        return s;
    }

    private static Category categoryOf(String unit) {
        if (unit == null) return Category.UNKNOWN;
        switch (unit.trim().toLowerCase()) {
            case "g": case "kg": case "oz": case "lb":
                return Category.WEIGHT;
            case "ml": case "l": case "tsp": case "tbsp": case "cup": case "fl_oz":
                return Category.VOLUME;
            case "unit": case "pcs": case "piece": case "":
                return Category.COUNT;
            default:
                return Category.UNKNOWN;
        }
    }

    /** Converts a quantity into a common base unit (grams for weight, ml for volume). */
    private static double toBaseQuantity(double quantity, String unit) {
        if (unit == null) return quantity;
        switch (unit.trim().toLowerCase()) {
            case "g": return quantity;
            case "kg": return quantity * 1000;
            case "oz": return quantity * 28.35;
            case "lb": return quantity * 453.6;
            case "ml": return quantity;
            case "l": return quantity * 1000;
            case "tsp": return quantity * 5;
            case "tbsp": return quantity * 15;
            case "cup": return quantity * 240;
            default: return quantity; // "unit" / "pcs" / unknown -> compare as-is
        }
    }

    /** Returns true if a pantry item has at least the required quantity for a recipe. */
    public static boolean pantryCovers(PantryItem pantryItem, double requiredQty, String requiredUnit) {
        Category pantryCat = categoryOf(pantryItem.getUnit());
        Category recipeCat = categoryOf(requiredUnit);

        if (pantryCat == recipeCat && pantryCat != Category.UNKNOWN) {
            double pantryBase = toBaseQuantity(pantryItem.getQuantity(), pantryItem.getUnit());
            double recipeBase = toBaseQuantity(requiredQty, requiredUnit);
            return pantryBase >= recipeBase;
        }
        // Units are not directly comparable - fall back to a best-effort raw
        // quantity comparison rather than blocking the match entirely.
        return pantryItem.getQuantity() >= requiredQty;
    }

    /**
     * Checks a recipe's full ingredient list against the pantry.
     * fullMatch is only true if every single ingredient is covered.
     */
    public static MatchResult checkRecipe(List<RecipeIngredient> requiredIngredients, List<PantryItem> pantryItems) {
        Map<String, PantryItem> pantryByName = new HashMap<>();
        for (PantryItem p : pantryItems) {
            pantryByName.put(normalizeName(p.getName()), p);
        }

        MatchResult result = new MatchResult();
        for (RecipeIngredient ri : requiredIngredients) {
            String key = normalizeName(ri.getIngredientName());
            PantryItem match = pantryByName.get(key);
            boolean covered = match != null && pantryCovers(match, ri.getQuantity(), ri.getUnit());
            if (!covered) {
                result.missingIngredients.add(ri.getIngredientName());
            }
        }
        result.missingCount = result.missingIngredients.size();
        result.fullMatch = result.missingCount == 0;
        return result;
    }
}
