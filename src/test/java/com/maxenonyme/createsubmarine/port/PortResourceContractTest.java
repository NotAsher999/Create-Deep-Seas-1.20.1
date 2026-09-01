package com.maxenonyme.createsubmarine.port;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortResourceContractTest {
    private static final Path RESOURCES = Path.of("src", "main", "resources");
    private static final Path RECIPES = RESOURCES.resolve(Path.of("data", "create_submarine", "recipes"));

    private static final Set<String> EXPECTED_RECIPES = Set.of(
            "ballast_tank", "ballast_vent", "barometer", "copper_pressurizer", "electrolyzer",
            "floater_black", "floater_blue", "floater_brown", "floater_cyan", "floater_gray",
            "floater_green", "floater_light_blue", "floater_light_gray", "floater_lime",
            "floater_magenta", "floater_orange", "floater_pink", "floater_purple", "floater_red",
            "floater_white", "floater_yellow", "iron_pressurizer", "oxygen_diffuser",
            "phycological_membrane", "pulley", "steel_cable", "submarine_propeller",
            "underwater_mine", "water_thruster");

    private static final Map<String, Integer> FLOATER_MODEL_DATA = Map.ofEntries(
            Map.entry("white", 1), Map.entry("orange", 2), Map.entry("magenta", 3),
            Map.entry("light_blue", 4), Map.entry("yellow", 5), Map.entry("lime", 6),
            Map.entry("pink", 7), Map.entry("gray", 8), Map.entry("light_gray", 9),
            Map.entry("cyan", 10), Map.entry("purple", 11), Map.entry("blue", 12),
            Map.entry("brown", 13), Map.entry("green", 14), Map.entry("red", 15),
            Map.entry("black", 16));

    @Test
    void everyBundledJsonResourceParses() throws IOException {
        try (Stream<Path> files = Files.walk(RESOURCES)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
                try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                    assertNotNull(new JsonParser().parse(reader), () -> "Empty JSON resource: " + file);
                }
            }
        }
    }

    @Test
    void resourcesUseForge1201NamespacesAndModelContracts() throws IOException {
        try (Stream<Path> files = Files.walk(RESOURCES)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
                String json = Files.readString(file, StandardCharsets.UTF_8);
                assertFalse(json.contains("neoforge:"),
                        () -> "NeoForge resource identifier remains in " + file);
            }
        }

        Path models = RESOURCES.resolve(Path.of("assets", "create_submarine", "models", "block"));
        for (Path model : List.of(
                models.resolve("pulley.json"),
                models.resolve("pulley_core.json"),
                models.resolve(Path.of("rockcutting_wheel", "item.json")),
                models.resolve("underwater_mine.json"))) {
            assertEquals("forge:obj", readObject(model).get("loader").getAsString(),
                    () -> "Wrong Forge 1.20.1 OBJ loader in " + model);
        }

        JsonObject knot = readObject(models.resolve(Path.of("steel_cable", "connector_steel_knot.json")));
        assertEquals("minecraft:block/block", knot.get("parent").getAsString());
    }

    @Test
    void productionMixinConfigsBindTheGeneratedRefmap() throws IOException {
        for (String config : List.of("create_submarine.mixins.json", "create_abyss.mixins.json")) {
            assertEquals("createdeepseas.refmap.json",
                    readObject(RESOURCES.resolve(config)).get("refmap").getAsString(),
                    () -> config + " must remap named injection points in production");
        }
    }

    @Test
    void creativeSectionBannerIsRegisteredInTheBlockAtlas() throws IOException {
        JsonObject section = readObject(RESOURCES.resolve(Path.of(
                "assets", "create_submarine", "simulated", "sections", "submarine.json")));
        String spriteId = section.get("sprite").getAsString();
        assertEquals("create_submarine:banner", spriteId);

        JsonArray sources = readObject(RESOURCES.resolve(Path.of(
                "assets", "minecraft", "atlases", "blocks.json"))).getAsJsonArray("sources");
        List<JsonObject> bannerSources = StreamSupport.stream(sources.spliterator(), false)
                .map(JsonElement::getAsJsonObject)
                .filter(source -> spriteId.equals(source.get("sprite").getAsString()))
                .toList();
        assertEquals(1, bannerSources.size(), "The section sprite must have one atlas source");
        JsonObject bannerSource = bannerSources.get(0);
        assertEquals("single", bannerSource.get("type").getAsString());
        assertEquals("create_submarine:gui/sprites/banner",
                bannerSource.get("resource").getAsString());

        Path banner = RESOURCES.resolve(Path.of(
                "assets", "create_submarine", "textures", "gui", "sprites", "banner.png"));
        BufferedImage image = ImageIO.read(banner.toFile());
        assertNotNull(image, "The creative section banner must be a readable PNG");
        assertEquals(162, image.getWidth());
        assertEquals(0, image.getHeight() % 18, "Banner frames must be 18 pixels high");

        Path metadata = banner.resolveSibling(banner.getFileName() + ".mcmeta");
        JsonObject animation = readObject(metadata).getAsJsonObject("animation");
        assertEquals(18, animation.get("height").getAsInt());
    }

    @Test
    void recipesUseTheForge1201LayoutAndItemStackSchema() throws IOException {
        assertFalse(Files.exists(RESOURCES.resolve(Path.of("data", "create_submarine", "recipe"))));
        assertFalse(Files.exists(RESOURCES.resolve(Path.of("data", "create_submarine", "loot_table"))));
        assertFalse(Files.exists(RESOURCES.resolve(Path.of("data", "minecraft", "tags", "block"))));

        final Set<String> actualRecipes;
        try (Stream<Path> files = Files.list(RECIPES)) {
            actualRecipes = files.filter(path -> path.toString().endsWith(".json"))
                    .map(path -> path.getFileName().toString().replaceFirst("\\.json$", ""))
                    .collect(Collectors.toUnmodifiableSet());
        }
        assertEquals(EXPECTED_RECIPES, actualRecipes);

        for (String recipeId : EXPECTED_RECIPES) {
            JsonObject recipe = readObject(RECIPES.resolve(recipeId + ".json"));
            if (recipe.has("result")) {
                assert1201ItemStack(recipe.getAsJsonObject("result"), recipeId);
            }
            if (recipe.has("results")) {
                for (JsonElement result : recipe.getAsJsonArray("results")) {
                    assert1201ItemStack(result.getAsJsonObject(), recipeId);
                }
            }
        }
    }

    @Test
    void floaterRecipesPreserveEveryUpstreamColorVariant() throws IOException {
        for (Map.Entry<String, Integer> variant : FLOATER_MODEL_DATA.entrySet()) {
            JsonObject result = readObject(RECIPES.resolve("floater_" + variant.getKey() + ".json"))
                    .getAsJsonObject("result");
            assertEquals("create_submarine:floater", result.get("item").getAsString());
            JsonObject nbt = result.getAsJsonObject("nbt");
            assertNotNull(nbt, () -> "Missing Forge 1.20 ItemStack NBT for " + variant.getKey());
            assertEquals(variant.getKey(), nbt.getAsJsonObject("BlockStateTag").get("color").getAsString());
            assertEquals(variant.getValue().intValue(), nbt.get("CustomModelData").getAsInt());
        }
    }

    @Test
    void aquaticEntityTagMatchesTheUpstream121Semantics() throws IOException {
        JsonArray values = readObject(RESOURCES.resolve(
                Path.of("data", "minecraft", "tags", "entity_types", "aquatic.json")))
                .getAsJsonArray("values");
        List<String> actual = StreamSupport.stream(values.spliterator(), false)
                .map(JsonElement::getAsString)
                .toList();
        assertEquals(List.of(
                "minecraft:turtle", "minecraft:axolotl", "minecraft:guardian",
                "minecraft:elder_guardian", "minecraft:cod", "minecraft:pufferfish",
                "minecraft:salmon", "minecraft:tropical_fish", "minecraft:dolphin",
                "minecraft:squid", "minecraft:glow_squid", "minecraft:tadpole"), actual);
    }

    private static void assert1201ItemStack(JsonObject stack, String recipeId) {
        assertTrue(stack.has("item"), () -> recipeId + " must use the Forge 1.20.1 item key");
        assertFalse(stack.has("id"), () -> recipeId + " still uses the 1.21 item-stack id key");
        assertFalse(stack.has("components"), () -> recipeId + " still uses 1.21 data components");
    }

    private static JsonObject readObject(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return new JsonParser().parse(reader).getAsJsonObject();
        }
    }
}
