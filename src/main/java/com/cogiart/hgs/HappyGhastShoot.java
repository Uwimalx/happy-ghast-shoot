package com.cogiart.hgs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HappyGhastShoot implements ModInitializer {
	public static final String MOD_ID = "happy-ghast-shoot";
	private static final int FIREBALL_COOLDOWN_TICKS = 20;
	private static final int FIREBALL_CHARGE_TICKS = 10;
	private static final int FIREBALL_EXPLOSION_POWER = 3;
	private static final Map<UUID, Long> LAST_SHOT_TICKS = new HashMap<>();
	private static final Map<UUID, PendingShot> PENDING_SHOTS = new HashMap<>();

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.serverboundPlay().register(ShootHappyGhastPayload.TYPE, ShootHappyGhastPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ShootHappyGhastPayload.TYPE, (payload, context) -> context.server().execute(() -> shootMountedHappyGhast(context.player())));
		ServerTickEvents.END_SERVER_TICK.register(server -> tickPendingShots());
	}

	private static void shootMountedHappyGhast(ServerPlayer player) {
		Entity vehicle = player.getVehicle();
		if (!(vehicle instanceof HappyGhast happyGhast)) {
			return;
		}

		if (happyGhast.getControllingPassenger() != player) {
			return;
		}

		if (!(player.level() instanceof ServerLevel serverLevel)) {
			return;
		}

		long gameTime = serverLevel.getGameTime();
		Long lastShotTick = LAST_SHOT_TICKS.get(player.getUUID());
		if (lastShotTick != null && gameTime - lastShotTick < FIREBALL_COOLDOWN_TICKS) {
			return;
		}

		PENDING_SHOTS.put(player.getUUID(), new PendingShot(player.getUUID(), serverLevel, gameTime + FIREBALL_CHARGE_TICKS));
		HitResult hitResult = player.pick(128.0, 1.0F, false);
		LAST_SHOT_TICKS.put(player.getUUID(), gameTime);
		serverLevel.playSound(null, happyGhast.getX(), happyGhast.getY(), happyGhast.getZ(), SoundEvents.GHAST_WARN, SoundSource.HOSTILE, 2.0F, 1.0F);
	}

	private static void tickPendingShots() {
		Iterator<PendingShot> iterator = PENDING_SHOTS.values().iterator();
		while (iterator.hasNext()) {
			PendingShot pendingShot = iterator.next();
			if (pendingShot.level().getGameTime() < pendingShot.fireAtTick()) {
				continue;
			}

			if (!(pendingShot.level().getPlayerByUUID(pendingShot.playerUuid()) instanceof ServerPlayer player)) {
				iterator.remove();
				continue;
			}
			iterator.remove();

			Entity vehicle = player.getVehicle();
			if (!(vehicle instanceof HappyGhast happyGhast) || happyGhast.getControllingPassenger() != player) {
				continue;
			}

			HitResult hitResult = player.pick(128.0, 1.0F, false);
			Vec3 origin = getFireballSpawnPosition(happyGhast);
			Vec3 target = hitResult.getLocation();
			Vec3 direction = target.subtract(origin);

			if (direction.lengthSqr() < 1.0E-7) {
				direction = player.getViewVector(1.0F);
			}

			direction = direction.normalize();

			LargeFireball fireball = new LargeFireball(pendingShot.level(), (LivingEntity) happyGhast, direction, FIREBALL_EXPLOSION_POWER);
			fireball.setPos(origin);
			pendingShot.level().addFreshEntity(fireball);
			pendingShot.level().playSound(null, happyGhast.getX(), happyGhast.getY(), happyGhast.getZ(), SoundEvents.GHAST_SHOOT, SoundSource.HOSTILE, 1.5F, 1.0F);
		}
	}

	private record PendingShot(UUID playerUuid, ServerLevel level, long fireAtTick) {
	}

	private static Vec3 getFireballSpawnPosition(HappyGhast happyGhast) {
		Vec3 look = happyGhast.getViewVector(1.0F);
		return new Vec3(
			happyGhast.getX() + look.x * 4.0,
			happyGhast.getY(0.5) + 0.5,
			happyGhast.getZ() + look.z * 4.0
		);
	}
}
