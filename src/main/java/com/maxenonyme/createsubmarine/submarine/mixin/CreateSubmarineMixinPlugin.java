package com.maxenonyme.createsubmarine.submarine.mixin;

import com.mojang.logging.LogUtils;
import foundry.veil.Veil;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class CreateSubmarineMixinPlugin implements IMixinConfigPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    private boolean embeddiumPresent;
    private boolean copycatsPresent;

    @Override
    public void onLoad(String mixinPackage) {
        this.embeddiumPresent = Veil.platform().isModLoaded("embeddium");
        this.copycatsPresent = Veil.platform().isModLoaded("copycats");
        LOGGER.info("Create Deep Seas selected the {} water-occlusion renderer hooks",
                this.embeddiumPresent ? "Embeddium" : "vanilla");
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith(".VanillaLevelRendererShaderMixin")) {
            return !this.embeddiumPresent;
        }
        if (mixinClassName.contains(".compat.Sodium")) {
            return this.embeddiumPresent;
        }
        if (mixinClassName.contains(".compat.copycat.")) {
            return this.copycatsPresent;
        }
        return true;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
