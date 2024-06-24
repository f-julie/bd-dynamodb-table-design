package com.amazon.ata.dynamodbtabledesign.converter;

import com.amazon.ata.dynamodbtabledesign.model.Ingredient;
import com.amazon.ata.dynamodbtabledesign.model.Recipe;

import java.util.LinkedList;
import java.util.Queue;

public final class RecipeConverter {
    private RecipeConverter() {
    }

    public static Queue<Ingredient> fromRecipeToIngredientQueue(Recipe recipe) {
        return new LinkedList<>(recipe.getIngredients());
    }
}
