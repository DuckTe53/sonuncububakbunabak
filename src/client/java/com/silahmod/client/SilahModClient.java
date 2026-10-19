package com.silahmod.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.silahmod.Net;
import com.silahmod.SilahMod;
import com.silahmod.item.GunItem;
import com.silahmod.vehicle.CarEntity;
import com.silahmod.vehicle.Cars;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.client.KeyMapping;

public class SilahModClient implements ClientModInitializer {
	public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(SilahMod.id("silahlar"));

	public static KeyMapping reloadKey;
	public static KeyMapping menuKey;

	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(Cars.CAR, CarRenderer::new);
		reloadKey = KeyBindingHelper.registerKeyBinding(
				new KeyMapping("key.silahmod.reload", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY));
		menuKey = KeyBindingHelper.registerKeyBinding(
				new KeyMapping("key.silahmod.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, CATEGORY));

		// W/S gaz-fren, A/D direksiyon: sürüş girdilerini sunucuya gönder.
			if (client.player.getVehicle() instanceof CarEntity) {
				float throttle = (client.options.keyUp.isDown() ? 1f : 0f) - (client.options.keyDown.isDown() ? 1f : 0f);
				float turn = (client.options.keyLeft.isDown() ? 1f : 0f) - (client.options.keyRight.isDown() ? 1f : 0f);
				ClientPlayNetworking.send(new Net.CarControlPayload(throttle, turn));
			}
			// R = doldur, B = silah menusu
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.getConnection() == null) {
				return;
			}
			while (reloadKey.consumeClick()) {
				if (client.player.getMainHandItem().getItem() instanceof GunItem) {
					ClientPlayNetworking.send(new Net.ReloadPayload());
				}
			}
			while (menuKey.consumeClick()) {
				if (client.screen == null) {
					client.setScreen(new WeaponMenuScreen());
				}
			}
		});

		// Sol tik: elinde ates edilen silah varsa vanilla saldiri yerine ates et.
		//  - Yari-otomatik silah (pistol, Glock): her tik tek atis.
		//  - Otomatik silah (tufekler, SMG): sol tik basili tutuldukca her tick ates istegi gider;
		//    gercek atis hizini sunucu (GunSpec.fireDelay) sinirlar.
		ClientPreAttackCallback.EVENT.register((client, player, clickCount) -> {
			if (player.getMainHandItem().getItem() instanceof GunItem gun) {
				boolean pressed = clickCount > 0;
				boolean held = gun.automatic() && client.options.keyAttack.isDown();
				if ((pressed || held) && !player.isSpectator() && client.screen == null) {
					ClientPlayNetworking.send(new Net.ShootPayload());
				}
				return true; // blok kirma / vanilla saldiriyi engelle
			}
			return false;
		});

		// Mermi sayaci
		HudElementRegistry.attachElementAfter(VanillaHudElements.INFO_BAR, SilahMod.id("ammo"), AmmoOverlay::renderAmmo);
		// Medkit bandaj sarma animasyonu (oyuncunun kendi skinindeki kol)
		HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, SilahMod.id("medkit"), MedkitOverlay::render);
	}
}
