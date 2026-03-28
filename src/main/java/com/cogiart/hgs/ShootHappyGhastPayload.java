package com.cogiart.hgs;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ShootHappyGhastPayload() implements CustomPacketPayload {
	public static final Type<ShootHappyGhastPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(HappyGhastShoot.MOD_ID, "shoot_happy_ghast"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ShootHappyGhastPayload> CODEC = StreamCodec.unit(new ShootHappyGhastPayload());

	@Override
	public Type<ShootHappyGhastPayload> type() {
		return TYPE;
	}
}
