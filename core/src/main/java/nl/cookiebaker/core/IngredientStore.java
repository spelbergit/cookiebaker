/*
Copyright 2020 Spelberg IT

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package nl.cookiebaker.core;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;
import lombok.Data;
import lombok.NonNull;

@Data
public class IngredientStore {

    @NonNull
    private final Map<String, IngredientInstance> ingredients = new TreeMap<>();

    public IngredientInstance getIngredient(String name) {
        IngredientInstance ingredientInstance = ingredients.get(name);
        if (ingredientInstance == null) {
            throw new IllegalArgumentException("Ingredient '" + name + "' is not available");
        }
        return ingredientInstance;
    }

    public void grabIngredients(Object event) {
        for (Field field : event.getClass().getDeclaredFields()) {
            Ingredient ingredient = field.getAnnotation(Ingredient.class);
            if (ingredient != null) {
                try {
                    String ingredientName = ingredient.value();
                    field.setAccessible(true);
                    Object ingredientValue = field.get(event);
                    storeIngredient(ingredientName, ingredientValue);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Unable to grab ingredient from event: " + e.getMessage(), e);
                }
            }
        }
    }

    public void storeIngredient(String name, Object value) {
        ingredients.put(name, new IngredientInstance(name, value));
    }
}
