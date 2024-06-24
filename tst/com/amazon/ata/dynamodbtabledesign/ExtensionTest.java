package com.amazon.ata.dynamodbtabledesign;

import com.amazon.ata.dynamodbtabledesign.exception.CartonCreationFailedException;
import com.amazon.ata.dynamodbtabledesign.dao.RecipeDao;
import com.amazon.ata.dynamodbtabledesign.dependency.DaggerIceCreamParlorServiceComponent;
import com.amazon.ata.dynamodbtabledesign.dependency.IceCreamParlorServiceComponent;
import com.amazon.ata.dynamodbtabledesign.model.Carton;
import com.amazon.ata.dynamodbtabledesign.model.Flavor;
import com.amazon.ata.dynamodbtabledesign.model.Ingredient;
import com.amazon.ata.dynamodbtabledesign.model.Sundae;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ExtensionTest {
    private static final IceCreamParlorServiceComponent DAGGER = DaggerIceCreamParlorServiceComponent.create();

    private IceCreamParlorService serviceWithMocks;

    @Spy
    private IceCreamParlorService serviceSpy;

    @Spy
    private IceCreamMaker iceCreamMakerSpy;

    @Mock
    private IceCreamMaker iceCreamMakerMock;

    @Captor
    private ArgumentCaptor<List<Carton>> buildSundaeCaptor;

    @Captor
    private ArgumentCaptor<Supplier<Ingredient>> ingredientSupplierCaptor;

    @Captor
    private ArgumentCaptor<List<Queue<Ingredient>>> makeIceCreamCartonsCaptor;

    private RecipeDao recipeDao;

    @BeforeEach
    private void setup() {
        iceCreamMakerSpy = new IceCreamMaker();
        recipeDao = new RecipeDao();
        serviceSpy = DAGGER.provideIceCreamParlorService();
        initMocks(this);
        serviceWithMocks = new IceCreamParlorService(recipeDao, null, iceCreamMakerMock);
    }

    @Test
    void prepareFlavors_withAnInvalidFlavor_throwsException() {
        // GIVEN
        // Valid flavor + invalid flavor
        List<String> flavors = ImmutableList.of("Vanilla", "Garlic");

        // WHEN + THEN - exception thrown
        assertThrows(
            CartonCreationFailedException.class,
            () -> serviceSpy.prepareFlavors(flavors),
            "Expected exception when preparing a list with invalid flavor: " + flavors);
    }

    @Test
    void getSundae_withNonexistentFlavor_ignoresFlavor() {
        // GIVEN
        List<String> flavorNames = ImmutableList.of("Strawberry", "Licorice");
        List<Flavor> expectedFlavors = ImmutableList.of(Flavor.STRAWBERRY);

        // WHEN
        serviceSpy.getSundae(flavorNames);

        // THEN
        verify(serviceSpy).buildSundae(buildSundaeCaptor.capture());
        List<Carton> cartons = buildSundaeCaptor.getValue();
        List<Flavor> flavors = cartons.stream().map(Carton::getFlavor).collect(Collectors.toList());
        assertEquals(expectedFlavors, flavors, "buildSundae did not receive expected flavors!");
    }

    @Test
    void getSundae_withFlavorOutOfStock_removesFlavor() {
        // GIVEN
        List<String> flavorNames = ImmutableList.of("Vanilla", "Chocolate", "Vanilla", "Strawberry");
        List<Flavor> expectedFlavors = ImmutableList.of(Flavor.VANILLA, Flavor.VANILLA, Flavor.STRAWBERRY);

        // WHEN
        serviceSpy.getSundae(flavorNames);

        // THEN
        verify(serviceSpy).buildSundae(buildSundaeCaptor.capture());
        List<Carton> cartons = buildSundaeCaptor.getValue();
        List<Flavor> flavors = cartons.stream().map(Carton::getFlavor).collect(Collectors.toList());
        assertEquals(expectedFlavors, flavors, "buildSundae did not receive expected flavors!");
    }

    @Test
    void getSundae_withInStockFlavors_returnsSundaeWithAllFlavors() {
        // GIVEN
        List<String> flavorNames = ImmutableList.of("Vanilla", "Strawberry", "Vanilla");
        List<Flavor> expectedFlavors = ImmutableList.of(Flavor.VANILLA, Flavor.STRAWBERRY, Flavor.VANILLA);

        // WHEN
        Sundae result = serviceSpy.getSundae(flavorNames);

        // THEN
        assertEquals(expectedFlavors, result.getScoops(), "Sundae didn't contain expected flavors!");
    }

    @Test
    void getSundae_withEmptyFlavor_returnsSundaeWithoutEmptyFlavor() {
        // GIVEN
        List<String> flavorNames = ImmutableList.of("Chocolate", "Strawberry", "Chocolate", "Vanilla", "Strawberry");
        List<Flavor> expectedFlavors = ImmutableList.of(Flavor.STRAWBERRY, Flavor.VANILLA,Flavor. STRAWBERRY);

        // WHEN
        Sundae result = serviceSpy.getSundae(flavorNames);

        // THEN
        assertEquals(expectedFlavors, result.getScoops(), "Sundae didn't contain expected flavors!");
    }

    @Test
    void prepareFlavors_withOneFlavor_makeIceCreamCartonsReceivesCorrectIngredients() {
        // GIVEN
        List<String> flavors = ImmutableList.of("Vanilla");
        doReturn(flavors.size()).when(serviceSpy).makeIceCreamCartons(any());

        // WHEN
        serviceSpy.prepareFlavors(flavors);

        // THEN
        // we receive the right number of ingredients queues
        verify(serviceSpy, description("Missing call to makeIceCreamCartons"))
            .makeIceCreamCartons(makeIceCreamCartonsCaptor.capture());
        List<Queue<Ingredient>> ingredientsQueues = makeIceCreamCartonsCaptor.getValue();
        assertEquals(
            flavors.size(),
            ingredientsQueues.size(),
            String.format("Expected ingredient queue size to be %d but got: %s", flavors.size(), ingredientsQueues)
        );

        // The ingredients are the ones we expect
        Queue<Ingredient> ingredients = makeIceCreamCartonsCaptor.getValue().get(0);
        assertEquals(
            Ingredient.CREAM,
            ingredients.remove(),
            "Expected makeIceCreamCartons to receive a " + Ingredient.CREAM);
        assertEquals(
            Ingredient.SUGAR,
            ingredients.remove(),
            "Expected makeIceCreamCartons to receive a " + Ingredient.SUGAR);
        assertEquals(
            Ingredient.EGGS,
            ingredients.remove(),
            "Expected makeIceCreamCartons to receive a " + Ingredient.EGGS);
        assertEquals(
            Ingredient.VANILLA,
            ingredients.remove(),
            "Expected makeIceCreamCartons to receive a " + Ingredient.VANILLA);
        assertTrue(ingredients.isEmpty(), "Expected ingredients queue to be empty at this point");
    }

    @Test
    void prepareFlavors_withFlavors_makeIceCreamCartonsReceivesCorrectIngredients() {
        // GIVEN
        List<String> flavors = ImmutableList.of("Vanilla", "Strawberry");
        doReturn(flavors.size()).when(serviceSpy).makeIceCreamCartons(any());

        // WHEN
        serviceSpy.prepareFlavors(flavors);

        // THEN
        // we receive the right number of ingredients queues
        verify(serviceSpy).makeIceCreamCartons(makeIceCreamCartonsCaptor.capture());
        List<Queue<Ingredient>> ingredientsQueues = makeIceCreamCartonsCaptor.getValue();
        assertEquals(
            flavors.size(),
            ingredientsQueues.size(),
            String.format("Expected ingredient queue size to be %d but got: %s", flavors.size(), ingredientsQueues)
        );

        // The strawberry ingredients are the ones we expect
        Queue<Ingredient> ingredients = makeIceCreamCartonsCaptor.getValue().get(1);
        assertEquals(
            Ingredient.CREAM,
            ingredients.remove(),
            "Expected makeIceCreamCartons to receive a " + Ingredient.CREAM);
        assertEquals(
            Ingredient.SUGAR,
            ingredients.remove(),
            "Expected makeIceCreamCartons to receive a " + Ingredient.SUGAR);
        assertEquals(
            Ingredient.EGGS,
            ingredients.remove(),
            "Expected makeIceCreamCartons to receive a " + Ingredient.EGGS);
        assertEquals(
            Ingredient.STRAWBERRIES,
            ingredients.remove(),
            "Expected makeIceCreamCartons to receive a " + Ingredient.STRAWBERRIES);
        assertTrue(ingredients.isEmpty(), "Expected ingredients queue to be empty at this point");
    }

    @Test
    void iceCreamMaker_prepareIceCreamCarton_callsFunctionalInterface() {
        // GIVEN
        Queue<Ingredient> ingredients = new LinkedList<>();
        ingredients.add(Ingredient.CHOCOLATE);
        ingredients.add(Ingredient.EGGS);
        Supplier<Ingredient> ingredientSupplierSpy = spy(new SupplierSpy(ingredients));

        // WHEN
        iceCreamMakerSpy.prepareIceCreamCarton(ingredientSupplierSpy);

        // THEN
        verify(
            ingredientSupplierSpy,
            times(ingredients.size() + 1).description("Expected num ingredients + 1 calls to functional interface")
        ).get();
    }

    @Test
    void prepareFlavors_oneFlavor_prepareIceCreamCartonReceivesCorrectIngredients() {
        // GIVEN
        List<String> flavors = ImmutableList.of("Chocolate");
        when(iceCreamMakerMock.prepareIceCreamCarton(ingredientSupplierCaptor.capture())).thenReturn(true);

        // WHEN
        serviceWithMocks.prepareFlavors(flavors);

        // THEN
        Supplier<Ingredient> supplier = ingredientSupplierCaptor.getValue();
        assertEquals(
            Ingredient.CREAM,
            supplier.get(),
            "Expected functional interface to provide a " + Ingredient.CREAM);
        assertEquals(
            Ingredient.SUGAR,
            supplier.get(),
            "Expected functional interface to provide a " + Ingredient.SUGAR);
        assertEquals(
            Ingredient.EGGS,
            supplier.get(),
            "Expected functional interface to provide a " + Ingredient.EGGS);
        assertEquals(
            Ingredient.CHOCOLATE,
            supplier.get(),
            "Expected functional interface to provide a " + Ingredient.CHOCOLATE);
        assertNull(supplier.get(), "Expected functional interface to return a null when empty");
    }

    @Test
    void prepareFlavors_multipleFlavors_returnsCorrectNumberOfCartons() {
        // GIVEN
        List<String> flavors = ImmutableList.of("Chocolate", "Vanilla", "Strawberry");

        // WHEN
        int result = serviceSpy.prepareFlavors(flavors);

        // THEN
        assertEquals(flavors.size(), result, "Expected number of cartons created to match flavors count");
    }

    static class SupplierSpy implements Supplier<Ingredient> {
        private Queue<Ingredient> ingredients;

        public SupplierSpy(Collection<Ingredient> ingredients) {
            this.ingredients = new LinkedList<>(ingredients);
        }

        public Ingredient get() {
            return ingredients.poll();
        }
    }
}
