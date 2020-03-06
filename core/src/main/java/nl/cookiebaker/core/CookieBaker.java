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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import lombok.Data;
import lombok.NonNull;

public class CookieBaker {

    private Map<Class<?>, InteractionDefinition> eventMethods = new HashMap<>();

    private CookieBakerState state = new CookieBakerState();

    public void provide(Object recipeInstance) {
        Class<?> recipeType = recipeInstance.getClass();
        for (Method method : recipeType.getMethods()) {
            Interaction interaction = method.getAnnotation(Interaction.class);
            if (interaction != null) {
                Class<?>[] eventTypes = interaction.value();
                for (Class<?> eventType : eventTypes) {
                    eventMethods.put(eventType, new InteractionDefinition(state, eventType, method, recipeInstance));
                }
            }
        }
    }

    public Object doe(Object event) {
        while (event != null) {
            InteractionDefinition interactionDefinition = eventMethods.get(event.getClass());
            if (interactionDefinition == null) {
                return event;
            }
            state.grabIngredients(event);
            try {
                event = interactionDefinition.handle();
            } catch (IllegalArgumentException e) {
                return event;
            }
        }
        return event;
    }


    @Data
    private static class CookieBakerState {
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
                        ingredients.put(ingredientName, new IngredientInstance(ingredientName, ingredientValue));
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("Unable to grab ingredient from event: " + e.getMessage(), e);
                    }
                }
            }
        }
    }

    @Data
    private static class InteractionDefinition {
        @NonNull
        private CookieBakerState cookieBakerState;
        @NonNull
        private Class<?> eventType;
        @NonNull
        private Method method;
        @NonNull
        private Object recipeInstance;

        public Object handle() {
            try {
                Object[] args = new Object[method.getParameterCount()];
                Parameter[] parameters = method.getParameters();
                for (int i = 0; i < parameters.length; i++) {
                    Parameter parameter = parameters[i];
                    Ingredient ingredient = parameter.getAnnotation(Ingredient.class);
                    IngredientInstance ingredientInstance = cookieBakerState.getIngredient(ingredient.value());
                    args[i] = ingredientInstance.getValue();
                }
                Object result = method.invoke(recipeInstance, args);
                if (result != null && result.getClass().getAnnotation(Event.class) != null) {
                    cookieBakerState.grabIngredients(result);
                    return result;
                } else {
                    return null;
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Unable to handle event: " + e.getMessage(), e);
            }
        }
    }
}
