package com.maxperf;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Files;
import java.nio.file.Path;

public class MaxPerfClient implements ClientModInitializer {

    private static final String MOD_ID = "maxperf";

    private static boolean nightVisionOn = true;
    private static KeyMapping toggleKey;

    // Keep the duration above 200 ticks so vanilla's "about to expire" flicker never triggers.
    private static final int NV_DURATION = 1200;
    private static final int NV_REFRESH_BELOW = 600;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(MOD_ID, "main"));

        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.maxperf.nightvision",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                category));

        // Apply tuned settings once (first launch only) so we never fight the user's own changes.
        ClientLifecycleEvents.CLIENT_STARTED.register(MaxPerfClient::applyPerformanceDefaults);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) {
                nightVisionOn = !nightVisionOn;
                if (client.player != null) {
                    client.player.sendOverlayMessage(
                            Component.literal("Night Vision: " + (nightVisionOn ? "ON" : "OFF")));
                }
            }
            tickNightVision(client);
        });
    }

    private static void tickNightVision(Minecraft client) {
        if (client.player == null) return;
        MobEffectInstance current = client.player.getEffect(MobEffects.NIGHT_VISION);

        if (nightVisionOn) {
            if (current == null || current.getDuration() < NV_REFRESH_BELOW) {
                // ambient=false, visible(particles)=false, showIcon=false -> invisible, client-side only
                client.player.addEffect(new MobEffectInstance(
                        MobEffects.NIGHT_VISION, NV_DURATION, 0, false, false, false));
            }
        } else if (current != null && !current.showIcon() && current.getAmplifier() == 0) {
            // Only remove the hidden one we added, never a real potion effect.
            client.player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }

    private static void applyPerformanceDefaults(Minecraft client) {
        try {
            Path marker = FabricLoader.getInstance().getConfigDir().resolve("maxperf.applied");
            if (Files.exists(marker)) return;

            Options o = client.options;
            o.renderDistance().set(10);
            o.simulationDistance().set(8);
            o.entityDistanceScaling().set(0.75);
            o.particles().set(ParticleStatus.MINIMAL);
            o.biomeBlendRadius().set(0);
            o.entityShadows().set(false);
            o.cloudStatus().set(CloudStatus.OFF);
            o.mipmapLevels().set(2);
            o.enableVsync().set(false);
            o.framerateLimit().set(260);
            o.save();

            Files.writeString(marker, "Delete this file to re-apply MaxPerf video settings on next launch.");
        } catch (Exception e) {
            System.err.println("[MaxPerf] Could not apply defaults: " + e);
        }
    }
}
