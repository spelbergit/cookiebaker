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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.NonNull;

public class CookieBaker {

    private Map<Class<?>, InteractionInstance> interactionInstanceMap = new HashMap<>();

    public void provide(Object recipeInstance) {
        Class<?> recipeType = recipeInstance.getClass();
        for (Method method : recipeType.getMethods()) {
            Interaction interaction = method.getAnnotation(Interaction.class);
            if (interaction != null) {
                Class<?>[] eventTypes = interaction.value();
                for (Class<?> eventType : eventTypes) {
                    if (interactionInstanceMap.get(eventType) != null) {
                        throw new IllegalArgumentException("More than 1 @Interaction for event " + eventType);
                    }
                    interactionInstanceMap.put(eventType, new InteractionInstance(eventType, method, recipeInstance));
                }
            }
        }
    }

    public Object doe(Object event) {
        IngredientStore ingredientStore = new IngredientStore();
        return doe(ingredientStore, event);
    }

    public Object doe(IngredientStore ingredientStore, Object event) {
        while (event != null) {
            InteractionInstance interactionInstance = interactionInstanceMap.get(event.getClass());
            if (interactionInstance == null) {
                return event;
            }
            ingredientStore.grabIngredients(event);
            try {
                event = interactionInstance.handle(ingredientStore);
            } catch (IllegalArgumentException e) {
                return event;
            }
        }
        return event;
    }


    @Data
    private static class InteractionInstance {
        @NonNull
        private Class<?> eventType;
        @NonNull
        private Method method;
        @NonNull
        private Object recipeInstance;

        public Object handle(@NonNull IngredientStore ingredientStore) {
            try {
                Object[] args = new Object[method.getParameterCount()];
                Parameter[] parameters = method.getParameters();
                for (int i = 0; i < parameters.length; i++) {
                    Parameter parameter = parameters[i];
                    args[i] = provideParameterValue(ingredientStore, parameter);
                }
                Object result = method.invoke(recipeInstance, args);
                if (result != null && result.getClass().getAnnotation(Event.class) != null) {
                    ingredientStore.grabIngredients(result);
                    return result;
                } else {
                    return null;
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Unable to handle event: " + e.getMessage(), e);
            }
        }

        @NonNull
        private Object provideParameterValue(IngredientStore ingredientStore, Parameter parameter) {
            Ingredient ingredient = parameter.getAnnotation(Ingredient.class);
            if (ingredient != null) {
                IngredientInstance ingredientInstance = ingredientStore.getIngredient(ingredient.value());
                return ingredientInstance.getValue();
            } else if (parameter.getType().isAssignableFrom(IngredientStore.class)) {
                return ingredientStore;
            } else {
              throw new IllegalArgumentException("Parameter missing @Ingredient annotation and not of supported types (" + IngredientStore.class.getSimpleName() + "): " + parameter);
            }
        }
    }
}
