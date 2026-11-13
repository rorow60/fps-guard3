package com.nicolas.fpsguard;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * FPS Guard : /fps boost + menu (touche N).
 * Coupe les effets coûteux. NE TOUCHE JAMAIS à la distance de rendu ni au monde.
 * Les réglages d'origine sont remis à la désactivation et à la fermeture du jeu.
 */
public class FpsGuardClient implements ClientModInitializer {

	private FpsGuardConfig config;
	private boolean boostOn = false;
	private KeyBinding openMenuKey;

	private int oMaxFps;
	private boolean oVsync;
	private GraphicsMode oGraphics;
	private CloudRenderMode oClouds;
	private ParticlesMode oParticles;
	private boolean oShadows;
	private int oBiome;
	private int oMipmap;
	private boolean oAo;

	public FpsGuardConfig config() {
		return config;
	}

	public boolean isBoostOn() {
		return boostOn;
	}

	@Override
	public void onInitializeClient() {
		config = FpsGuardConfig.load();

		KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("fpsguard", "main"));
		openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.fpsguard.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_N, category));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openMenuKey.wasPressed()) {
				if (client.currentScreen == null) {
					client.setScreen(new FpsGuardScreen(null, this));
				}
			}
		});

		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
			if (boostOn && client.options != null) {
				restore(client.options);
				boostOn = false;
			}
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
			dispatcher.register(
				ClientCommandManager.literal("fps").then(
					ClientCommandManager.literal("boost").executes(ctx -> {
						toggleBoost();
						ctx.getSource().sendFeedback(Text.literal(boostOn
							? "§aFPS Boost activé §7- touche N pour les réglages"
							: "§cFPS Boost désactivé §7(réglages d'origine remis)"));
						return Command.SINGLE_SUCCESS;
					})
				)
			)
		);
	}

	public void toggleBoost() {
		GameOptions o = MinecraftClient.getInstance().options;
		if (boostOn) {
			restore(o);
			boostOn = false;
		} else {
			saveOriginals(o);
			boostOn = true;
			apply(o);
		}
	}

	/** Réapplique les réglages quand on change une option dans le menu. */
	public void reapply() {
		if (boostOn) {
			apply(MinecraftClient.getInstance().options);
		}
	}

	private void saveOriginals(GameOptions o) {
		oMaxFps = o.getMaxFps().getValue();
		oVsync = o.getEnableVsync().getValue();
		oGraphics = o.getGraphicsMode().getValue();
		oClouds = o.getCloudRenderMode().getValue();
		oParticles = o.getParticles().getValue();
		oShadows = o.getEntityShadows().getValue();
		oBiome = o.getBiomeBlendRadius().getValue();
		oMipmap = o.getMipmapLevels().getValue();
		oAo = o.getAo().getValue();
	}

	private void apply(GameOptions o) {
		FpsGuardConfig c = config;
		o.getMaxFps().setValue(c.maxFps);
		o.getEnableVsync().setValue(c.vsyncOff ? false : oVsync);
		o.getGraphicsMode().setValue(c.fastGraphics ? GraphicsMode.FAST : oGraphics);
		o.getCloudRenderMode().setValue(c.noClouds ? CloudRenderMode.OFF : oClouds);
		o.getParticles().setValue(c.minimalParticles ? ParticlesMode.MINIMAL : oParticles);
		o.getEntityShadows().setValue(c.noEntityShadows ? false : oShadows);
		o.getBiomeBlendRadius().setValue(c.noBiomeBlend ? 0 : oBiome);
		o.getMipmapLevels().setValue(c.noMipmaps ? 0 : oMipmap);
		o.getAo().setValue(c.noSmoothLighting ? false : oAo);
	}

	private void restore(GameOptions o) {
		o.getMaxFps().setValue(oMaxFps);
		o.getEnableVsync().setValue(oVsync);
		o.getGraphicsMode().setValue(oGraphics);
		o.getCloudRenderMode().setValue(oClouds);
		o.getParticles().setValue(oParticles);
		o.getEntityShadows().setValue(oShadows);
		o.getBiomeBlendRadius().setValue(oBiome);
		o.getMipmapLevels().setValue(oMipmap);
		o.getAo().setValue(oAo);
	}
}
