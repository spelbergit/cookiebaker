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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import lombok.Data;
import lombok.NonNull;

@Data
public class InteractionResult {

    @NonNull
    private final List<Object> events;
    @NonNull
    private final Set<IngredientInstance> ingredients;

    public static Builder builder() {
        return new Builder();
    }

    @Data
    public static class Builder {
        private final List<Object> events = new ArrayList<>();
        private final Set<IngredientInstance> ingredients = new TreeSet<>();

        public Builder event(Object event) {
            Optional.of(event.getClass().getAnnotation(Event.class)).orElseThrow(() -> new IllegalArgumentException("Missing @Event on " + event.getClass()));
            this.events.add(event);
            return this;
        }

        public Builder ingredient(IngredientInstance ingredientInstance) {
            this.ingredients.add(ingredientInstance);
            return this;
        }

        public InteractionResult build() {
            return new InteractionResult(events, ingredients);
        }
    }

}
