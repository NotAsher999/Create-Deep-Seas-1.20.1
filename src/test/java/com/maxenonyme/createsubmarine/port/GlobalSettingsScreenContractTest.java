package com.maxenonyme.createsubmarine.port;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalSettingsScreenContractTest {
    private static final Path CLIENT = Path.of("src", "main", "java", "com", "maxenonyme",
            "createsubmarine", "submarine", "client");
    private static final Path CONFIG = Path.of("src", "main", "java", "com", "maxenonyme",
            "createsubmarine", "submarine", "config", "SubmarineConfig.java");
    private static final Path LANG = Path.of("src", "main", "resources", "assets",
            "create_submarine", "lang");

    private static final Map<String, String> PUBLIC_SETTINGS = Map.ofEntries(
            Map.entry("DISABLE_IMPLOSION", "disableImplosion"),
            Map.entry("OXYGEN_MAX_FILL_BLOCKS", "oxygenMaxFillBlocks"),
            Map.entry("ENABLE_DEEPER_OCEANS", "enableDeeperOceans"),
            Map.entry("DEEPER_OCEANS_DEPTH", "deeperOceansDepth"),
            Map.entry("GLOBAL_MAX_DEPTH_CAP", "globalMaxDepthCap"),
            Map.entry("MAX_DEPTH_MULTIPLIER", "maxDepthMultiplier"),
            Map.entry("IMPLOSION_CHANCE_MULTIPLIER", "implosionChanceMultiplier"),
            Map.entry("BALLAST_FORCE_MULTIPLIER", "ballastForceMultiplier"),
            Map.entry("BALLAST_LIFT_PER_TANK", "ballastLiftPerTank"),
            Map.entry("FLOATER_LIFT", "floaterLift"),
            Map.entry("BALLAST_VERTICAL_SPEED", "ballastVerticalSpeed"),
            Map.entry("BALLAST_TRANSFER_RATE_MULTIPLIER", "ballastTransferRateMultiplier"),
            Map.entry("WATER_THRUSTER_POWER_MULTIPLIER", "waterThrusterPowerMultiplier"),
            Map.entry("SUBMARINE_PROPELLER_POWER_MULTIPLIER", "submarinePropellerPowerMultiplier"),
            Map.entry("PULLEY_MAX_SLIDE_SPEED", "pulleyMaxSlideSpeed"),
            Map.entry("STEEL_CABLE_MAX_LENGTH", "steelCableMaxLength"),
            Map.entry("ENABLE_BOAT_WATER_CULLING", "enableBoatWaterCulling"),
            Map.entry("DISABLE_STARTUP_SCREENS", "disableStartupScreens"));

    @Test
    void globalButtonOpensTheForgeReplacementInsteadOfReturningToTheModList() throws IOException {
        String hullScreen = Files.readString(CLIENT.resolve("HullStrengthConfigScreen.java"),
                StandardCharsets.UTF_8);
        assertTrue(hullScreen.contains("new GlobalSettingsScreen(this)"));
        assertFalse(hullScreen.contains("ModListScreen"),
                "The global-settings action must not return to Forge's mod list");

        String globalScreen = Files.readString(CLIENT.resolve("GlobalSettingsScreen.java"),
                StandardCharsets.UTF_8);
        assertTrue(globalScreen.contains("SubmarineConfig.SPEC.save()"));
        int validation = globalScreen.indexOf("setting.validate()");
        int application = globalScreen.indexOf("SettingValue::apply");
        assertTrue(validation >= 0 && application >= 0 && validation < application,
                "Every staged value must validate before any setting is applied");
    }

    @Test
    void replacementCoversEveryPublicProductionSettingAndBothPrimaryLocales() throws IOException {
        String configSource = Files.readString(CONFIG, StandardCharsets.UTF_8);
        Matcher fields = Pattern.compile(
                "public static final ForgeConfigSpec\\.(?:BooleanValue|IntValue|DoubleValue)\\s+(\\w+);")
                .matcher(configSource);
        Set<String> declaredFields = fields.results()
                .map(result -> result.group(1))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        assertEquals(PUBLIC_SETTINGS.keySet(), declaredFields,
                "New public settings must be added to the Forge replacement screen deliberately");

        String screenSource = Files.readString(CLIENT.resolve("GlobalSettingsScreen.java"),
                StandardCharsets.UTF_8);
        JsonObject english = readObject(LANG.resolve("en_us.json"));
        JsonObject chinese = readObject(LANG.resolve("zh_cn.json"));
        for (Map.Entry<String, String> setting : PUBLIC_SETTINGS.entrySet()) {
            assertTrue(screenSource.contains("SubmarineConfig." + setting.getKey()),
                    () -> "Screen does not bind " + setting.getKey());
            String translation = "create_submarine.configuration." + setting.getValue();
            assertTrue(english.has(translation) && english.has(translation + ".tooltip"),
                    () -> "English labels incomplete for " + setting.getValue());
            assertTrue(chinese.has(translation) && chinese.has(translation + ".tooltip"),
                    () -> "Chinese labels incomplete for " + setting.getValue());
        }

        assertFalse(screenSource.contains("WELCOME_SCREEN_SEEN"));
        assertFalse(screenSource.contains("IGNORED_UPDATE_VERSION"));
    }

    private static JsonObject readObject(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return new JsonParser().parse(reader).getAsJsonObject();
        }
    }
}
