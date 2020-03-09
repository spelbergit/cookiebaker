package nl.cookiebaker.core;

import lombok.Data;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CookieBakerTest {

    private CookieBaker cookieBaker;

    private String result;

    @BeforeEach
    void setUp() {
        cookieBaker = new CookieBaker();
        result = "-";
    }

    @Test
    void testNone() {
        // setup
        cookieBaker.provide(new CookieBakeRecipe());

        // execute

        // verify
        assertEquals("-", result);
    }

    @Test
    void testBake() {
        // setup
        cookieBaker.provide(new CookieBakeRecipe());

        // execute
        cookieBaker.doe(new DoughRisesEvent());

        // verify
        assertEquals("- Dough Rises & Cookie Baked from Fresh Flower", result);
    }


    @Test
    void testQuickBake() {
        // setup
        cookieBaker.provide(new CookieBakeRecipe());

        // execute
        cookieBaker.doe(new QuickBakeEvent("flubby"));

        // verify
        assertEquals("- & Cookie Baked from flubby", result);
    }



    @Recipe
    public class CookieBakeRecipe {

        @Interaction(DoughRisesEvent.class)
        public DoughRisenEvent riseDough(IngredientStore ingredientStore) {
            ingredientStore.storeIngredient("dough", "Fresh Flower");
            result += " Dough Rises";
            return new DoughRisenEvent();
        }

        @Interaction({DoughRisenEvent.class, QuickBakeEvent.class})
        public void bakeCookie(@Ingredient("dough") String dough) {
            result += " & Cookie Baked from " + dough;
        }

    }

    @Data
    @Event
    private static class DoughRisesEvent {
    }

    @Data
    @Event
    private static class DoughRisenEvent {
    }

    @Data
    @Event
    private static class QuickBakeEvent {
        @Ingredient("dough")
        @NonNull
        String dough;
    }
}