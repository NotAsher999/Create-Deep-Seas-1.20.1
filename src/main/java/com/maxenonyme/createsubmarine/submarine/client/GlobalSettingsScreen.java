package com.maxenonyme.createsubmarine.submarine.client;

import com.maxenonyme.createsubmarine.submarine.config.SubmarineConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Forge 1.20.1 replacement for NeoForge's generic {@code ConfigurationScreen}.
 *
 * <p>The upstream screen is not available on Forge 47, so this screen exposes
 * the same public {@link SubmarineConfig} values without adding a GUI-library
 * dependency. Values remain staged until Save is pressed; Cancel and Escape do
 * not mutate the loaded config.</p>
 */
public class GlobalSettingsScreen extends Screen {
    private static final String TRANSLATION_PREFIX = "create_submarine.configuration.";

    private final Screen parentScreen;
    private final List<SettingValue> settings;
    private SettingsList settingsList;
    private Component statusMessage = Component.empty();

    public GlobalSettingsScreen(Screen parentScreen) {
        super(Component.translatable("create_submarine.screen.global_settings.title"));
        this.parentScreen = parentScreen;
        this.settings = createSettings();
    }

    private static List<SettingValue> createSettings() {
        List<SettingValue> values = new ArrayList<>();

        values.add(new BooleanSetting("gameplay", "disableImplosion",
                SubmarineConfig.DISABLE_IMPLOSION));
        values.add(new IntegerSetting("gameplay", "oxygenMaxFillBlocks",
                SubmarineConfig.OXYGEN_MAX_FILL_BLOCKS));
        values.add(new BooleanSetting("gameplay", "enableDeeperOceans",
                SubmarineConfig.ENABLE_DEEPER_OCEANS));
        values.add(new IntegerSetting("gameplay", "deeperOceansDepth",
                SubmarineConfig.DEEPER_OCEANS_DEPTH));

        values.add(new IntegerSetting("hullStrength", "globalMaxDepthCap",
                SubmarineConfig.GLOBAL_MAX_DEPTH_CAP));
        values.add(new DoubleSetting("hullStrength", "maxDepthMultiplier",
                SubmarineConfig.MAX_DEPTH_MULTIPLIER));
        values.add(new DoubleSetting("hullStrength", "implosionChanceMultiplier",
                SubmarineConfig.IMPLOSION_CHANCE_MULTIPLIER));

        values.add(new DoubleSetting("mechanics", "ballastForceMultiplier",
                SubmarineConfig.BALLAST_FORCE_MULTIPLIER));
        values.add(new DoubleSetting("mechanics", "ballastLiftPerTank",
                SubmarineConfig.BALLAST_LIFT_PER_TANK));
        values.add(new DoubleSetting("mechanics", "floaterLift",
                SubmarineConfig.FLOATER_LIFT));
        values.add(new DoubleSetting("mechanics", "ballastVerticalSpeed",
                SubmarineConfig.BALLAST_VERTICAL_SPEED));
        values.add(new DoubleSetting("mechanics", "ballastTransferRateMultiplier",
                SubmarineConfig.BALLAST_TRANSFER_RATE_MULTIPLIER));
        values.add(new DoubleSetting("mechanics", "waterThrusterPowerMultiplier",
                SubmarineConfig.WATER_THRUSTER_POWER_MULTIPLIER));
        values.add(new DoubleSetting("mechanics", "submarinePropellerPowerMultiplier",
                SubmarineConfig.SUBMARINE_PROPELLER_POWER_MULTIPLIER));
        values.add(new DoubleSetting("mechanics", "pulleyMaxSlideSpeed",
                SubmarineConfig.PULLEY_MAX_SLIDE_SPEED));
        values.add(new IntegerSetting("mechanics", "steelCableMaxLength",
                SubmarineConfig.STEEL_CABLE_MAX_LENGTH));

        values.add(new BooleanSetting("experimental", "enableBoatWaterCulling",
                SubmarineConfig.ENABLE_BOAT_WATER_CULLING));
        values.add(new BooleanSetting("client", "disableStartupScreens",
                SubmarineConfig.DISABLE_STARTUP_SCREENS));

        return List.copyOf(values);
    }

    @Override
    protected void init() {
        super.init();
        this.settingsList = new SettingsList(this.minecraft, this.width, this.height, 30,
                this.height - 54, 36);
        this.settingsList.setRenderBackground(false);
        this.settingsList.setRenderTopAndBottom(false);
        this.addRenderableWidget(this.settingsList);

        String currentCategory = null;
        for (SettingValue setting : this.settings) {
            if (!setting.category.equals(currentCategory)) {
                currentCategory = setting.category;
                this.settingsList.addListEntry(new CategoryEntry(currentCategory));
            }
            this.settingsList.addListEntry(new SettingEntry(setting));
        }

        int buttonWidth = Math.min(100, Math.max(70, (this.width - 40) / 3));
        int gap = 5;
        int totalWidth = buttonWidth * 3 + gap * 2;
        int left = (this.width - totalWidth) / 2;
        int buttonY = this.height - 27;

        this.addRenderableWidget(Button.builder(Component.translatable("create_submarine.ui.button.reset_defaults"),
                        button -> resetDefaults())
                .bounds(left, buttonY, buttonWidth, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("create_submarine.ui.button.save"),
                        button -> saveAndClose())
                .bounds(left + buttonWidth + gap, buttonY, buttonWidth, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("create_submarine.ui.button.cancel"),
                        button -> closeWithoutSaving())
                .bounds(left + (buttonWidth + gap) * 2, buttonY, buttonWidth, 20)
                .build());
    }

    @Override
    public void tick() {
        super.tick();
        this.settings.forEach(SettingValue::tick);
    }

    private void resetDefaults() {
        this.settings.forEach(SettingValue::resetToDefault);
        this.statusMessage = Component.translatable("create_submarine.configuration.defaults_staged")
                .withStyle(ChatFormatting.YELLOW);
    }

    private void saveAndClose() {
        for (SettingValue setting : this.settings) {
            Optional<Component> validationError = setting.validate();
            if (validationError.isPresent()) {
                this.statusMessage = validationError.get().copy().withStyle(ChatFormatting.RED);
                return;
            }
        }

        this.settings.forEach(SettingValue::apply);
        SubmarineConfig.SPEC.save();
        this.minecraft.setScreen(this.parentScreen);
    }

    private void closeWithoutSaving() {
        this.minecraft.setScreen(this.parentScreen);
    }

    @Override
    public void onClose() {
        closeWithoutSaving();
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFFFF);
        if (!this.statusMessage.getString().isEmpty()) {
            graphics.drawCenteredString(this.font, this.statusMessage, this.width / 2,
                    this.height - 48, 0xFFFFFFFF);
        }
    }

    private final class SettingsList extends ContainerObjectSelectionList<SettingsEntry> {
        private SettingsList(Minecraft minecraft, int width, int height, int top, int bottom,
                int itemHeight) {
            super(minecraft, width, height, top, bottom, itemHeight);
        }

        private void addListEntry(SettingsEntry entry) {
            this.addEntry(entry);
        }

        @Override
        public int getRowWidth() {
            return Math.min(560, this.width - 36);
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getLeft() + this.width - 10;
        }
    }

    private abstract static class SettingsEntry extends ContainerObjectSelectionList.Entry<SettingsEntry> {
    }

    private final class CategoryEntry extends SettingsEntry {
        private final Component label;

        private CategoryEntry(String category) {
            this.label = Component.translatable(TRANSLATION_PREFIX + category)
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        }

        @Override
        public void render(@NotNull GuiGraphics graphics, int index, int top, int left, int width,
                int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
            graphics.drawCenteredString(font, this.label, left + width / 2, top + 13, 0xFFFFFFFF);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of();
        }
    }

    private final class SettingEntry extends SettingsEntry {
        private final SettingValue setting;
        private final AbstractWidget widget;

        private SettingEntry(SettingValue setting) {
            this.setting = setting;
            this.widget = setting.createWidget(font);
            this.widget.setTooltip(Tooltip.create(setting.tooltip()));
        }

        @Override
        public void render(@NotNull GuiGraphics graphics, int index, int top, int left, int width,
                int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
            int controlWidth = Math.min(150, Math.max(90, width / 3));
            this.widget.setX(left + width - controlWidth - 4);
            this.widget.setY(top + 7);
            this.widget.setWidth(controlWidth);
            this.widget.render(graphics, mouseX, mouseY, partialTick);

            int labelWidth = Math.max(20, this.widget.getX() - left - 12);
            String label = font.plainSubstrByWidth(this.setting.label().getString(), labelWidth);
            graphics.drawString(font, label, left + 4, top + 13, 0xFFFFFFFF, false);
            if (hovered && mouseX < this.widget.getX()) {
                GlobalSettingsScreen.this.setTooltipForNextRenderPass(this.setting.tooltip());
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(this.widget);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(this.widget);
        }
    }

    private abstract static class SettingValue {
        private final String category;
        private final String key;

        private SettingValue(String category, String key) {
            this.category = category;
            this.key = key;
        }

        final Component label() {
            return Component.translatable(TRANSLATION_PREFIX + this.key);
        }

        final Component tooltip() {
            return Component.translatable(TRANSLATION_PREFIX + this.key + ".tooltip");
        }

        abstract AbstractWidget createWidget(Font font);

        abstract Optional<Component> validate();

        abstract void apply();

        abstract void resetToDefault();

        void tick() {
        }
    }

    private static final class BooleanSetting extends SettingValue {
        private final boolean defaultValue;
        private final Consumer<Boolean> saver;
        private boolean value;
        private Button button;

        private BooleanSetting(String category, String key, ForgeConfigSpec.BooleanValue configValue) {
            super(category, key);
            this.value = configValue.get();
            this.defaultValue = configValue.getDefault();
            this.saver = configValue::set;
        }

        @Override
        AbstractWidget createWidget(Font font) {
            this.button = Button.builder(valueLabel(), button -> {
                this.value = !this.value;
                button.setMessage(valueLabel());
            }).bounds(0, 0, 150, 20).build();
            return this.button;
        }

        private Component valueLabel() {
            return Component.translatable(this.value ? "options.on" : "options.off");
        }

        @Override
        Optional<Component> validate() {
            return Optional.empty();
        }

        @Override
        void apply() {
            this.saver.accept(this.value);
        }

        @Override
        void resetToDefault() {
            this.value = this.defaultValue;
            if (this.button != null) {
                this.button.setMessage(valueLabel());
            }
        }
    }

    private abstract static class NumberSetting extends SettingValue {
        private final double minimum;
        private final double maximum;
        private final String defaultValue;
        private String value;
        private EditBox editBox;

        private NumberSetting(String category, String key, String value, String defaultValue,
                double minimum, double maximum) {
            super(category, key);
            this.value = value;
            this.defaultValue = defaultValue;
            this.minimum = minimum;
            this.maximum = maximum;
        }

        @Override
        AbstractWidget createWidget(Font font) {
            this.editBox = new EditBox(font, 0, 0, 150, 20, this.label());
            this.editBox.setMaxLength(32);
            this.editBox.setFilter(this::isPotentialValue);
            this.editBox.setValue(this.value);
            this.editBox.setResponder(text -> this.value = text);
            return this.editBox;
        }

        protected abstract boolean isPotentialValue(String text);

        protected abstract Number parse(String text) throws NumberFormatException;

        protected abstract void save(Number number);

        @Override
        Optional<Component> validate() {
            final Number parsed;
            try {
                parsed = parse(this.value);
            } catch (NumberFormatException exception) {
                return Optional.of(Component.translatable(
                        "create_submarine.configuration.error.invalid_number", this.label()));
            }
            double numericValue = parsed.doubleValue();
            if (!Double.isFinite(numericValue) || numericValue < this.minimum || numericValue > this.maximum) {
                return Optional.of(Component.translatable(
                        "create_submarine.configuration.error.out_of_range", this.label(),
                        formatRange(this.minimum), formatRange(this.maximum)));
            }
            return Optional.empty();
        }

        @Override
        void apply() {
            save(parse(this.value));
        }

        @Override
        void resetToDefault() {
            this.value = this.defaultValue;
            if (this.editBox != null) {
                this.editBox.setValue(this.value);
            }
        }

        @Override
        void tick() {
            if (this.editBox != null) {
                this.editBox.tick();
            }
        }

        private static String formatRange(double value) {
            if (value == Math.rint(value)) {
                return Long.toString((long) value);
            }
            return Double.toString(value);
        }
    }

    private static final class IntegerSetting extends NumberSetting {
        private final Consumer<Integer> saver;

        private IntegerSetting(String category, String key, ForgeConfigSpec.IntValue configValue) {
            super(category, key, Integer.toString(configValue.get()),
                    Integer.toString(configValue.getDefault()), minimum(configValue), maximum(configValue));
            this.saver = configValue::set;
        }

        @Override
        protected boolean isPotentialValue(String text) {
            return text.matches("[-+]?\\d*");
        }

        @Override
        protected Number parse(String text) throws NumberFormatException {
            return Integer.parseInt(text);
        }

        @Override
        protected void save(Number number) {
            this.saver.accept(number.intValue());
        }
    }

    private static final class DoubleSetting extends NumberSetting {
        private final Consumer<Double> saver;

        private DoubleSetting(String category, String key, ForgeConfigSpec.DoubleValue configValue) {
            super(category, key, Double.toString(configValue.get()),
                    Double.toString(configValue.getDefault()), minimum(configValue), maximum(configValue));
            this.saver = configValue::set;
        }

        @Override
        protected boolean isPotentialValue(String text) {
            return text.matches("[-+]?(?:\\d*\\.?\\d*)");
        }

        @Override
        protected Number parse(String text) throws NumberFormatException {
            return Double.parseDouble(text);
        }

        @Override
        protected void save(Number number) {
            this.saver.accept(number.doubleValue());
        }
    }

    private static double minimum(ForgeConfigSpec.ConfigValue<? extends Number> configValue) {
        return range(configValue).getMin().doubleValue();
    }

    private static double maximum(ForgeConfigSpec.ConfigValue<? extends Number> configValue) {
        return range(configValue).getMax().doubleValue();
    }

    @SuppressWarnings("unchecked")
    private static ForgeConfigSpec.Range<? extends Number> range(
            ForgeConfigSpec.ConfigValue<? extends Number> configValue) {
        ForgeConfigSpec.ValueSpec valueSpec = SubmarineConfig.SPEC.getSpec().get(configValue.getPath());
        ForgeConfigSpec.Range<?> range = valueSpec.getRange();
        if (range == null || !(range.getMin() instanceof Number) || !(range.getMax() instanceof Number)) {
            throw new IllegalStateException("Missing numeric range for " + String.join(".", configValue.getPath()));
        }
        return (ForgeConfigSpec.Range<? extends Number>) range;
    }
}
