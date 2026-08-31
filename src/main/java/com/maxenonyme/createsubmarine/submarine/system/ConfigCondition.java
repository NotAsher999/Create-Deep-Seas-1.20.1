package com.maxenonyme.createsubmarine.submarine.system;

import com.google.gson.JsonObject;
import com.maxenonyme.createsubmarine.CreateSubmarine;
import com.maxenonyme.createsubmarine.submarine.config.SubmarineConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public final class ConfigCondition implements ICondition {
    public static final ResourceLocation NAME = new ResourceLocation(CreateSubmarine.MOD_ID, "config_enabled");
    public static final IConditionSerializer<ConfigCondition> SERIALIZER = new IConditionSerializer<>() {
        @Override
        public void write(JsonObject json, ConfigCondition condition) {
            json.addProperty("config_key", condition.configKey);
        }

        @Override
        public ConfigCondition read(JsonObject json) {
            return new ConfigCondition(json.get("config_key").getAsString());
        }

        @Override
        public ResourceLocation getID() {
            return NAME;
        }
    };

    private final String configKey;

    public ConfigCondition(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigKey() {
        return configKey;
    }

    @Override
    public ResourceLocation getID() {
        return NAME;
    }

    @Override
    public boolean test(IContext context) {
        if (configKey.equalsIgnoreCase("enableAbyssDimension")) {
            return !net.minecraftforge.fml.loading.FMLEnvironment.production;
        }
        if (!SubmarineConfig.SPEC.isLoaded()) {
            return false;
        }
        if (configKey.equalsIgnoreCase("enableDeeperOceans")) {
            return SubmarineConfig.ENABLE_DEEPER_OCEANS.get();
        }
        return false;
    }
}
