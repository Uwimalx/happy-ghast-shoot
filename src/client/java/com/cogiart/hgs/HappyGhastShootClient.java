package com.cogiart.hgs;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;

import com.mojang.blaze3d.platform.InputConstants;

public class HappyGhastShootClient implements ClientModInitializer {
	private static final KeyMapping SHOOT_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
		"key.happy-ghast-shoot.shoot",
		InputConstants.KEY_G,
		KeyMapping.Category.GAMEPLAY
	));

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(HappyGhastShootClient::handleShootInput);
	}

	private static void handleShootInput(Minecraft client) {
		while (SHOOT_KEY.consumeClick()) {
			if (client.player == null || client.screen != null) {
				continue;
			}

			if (!(client.player.getVehicle() instanceof HappyGhast)) {
				continue;
			}

			ClientPlayNetworking.send(new ShootHappyGhastPayload());
		}
	}
}
