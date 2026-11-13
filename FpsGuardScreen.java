package com.nicolas.fpsguard;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.net.URI;

/** Menu de réglages (touche N). Style Minecraft classique. */
public class FpsGuardScreen extends Screen {

	private final Screen parent;
	private final FpsGuardClient mod;

	public FpsGuardScreen(Screen parent, FpsGuardClient mod) {
		super(Text.literal("FPS Guard"));
		this.parent = parent;
		this.mod = mod;
	}

	private static String onOff(boolean v) {
		return v ? "§aOUI" : "§cNON";
	}

	@Override
	protected void init() {
		FpsGuardConfig c = mod.config();
		int w = 200;
		int h = 20;
		int x = this.width / 2 - w / 2;
		int y = this.height / 2 - 100;
		int gap = 22;

		addDrawableChild(ButtonWidget.builder(
			Text.literal("FPS Boost : " + (mod.isBoostOn() ? "§aACTIVÉ" : "§cDÉSACTIVÉ")),
			b -> {
				mod.toggleBoost();
				clearAndInit();
			}).dimensions(x, y, w, h).build());
		y += gap + 6;

		addToggle(x, y, w, h, "Graphismes rapides", c.fastGraphics, v -> c.fastGraphics = v);
		y += gap;
		addToggle(x, y, w, h, "Couper les nuages", c.noClouds, v -> c.noClouds = v);
		y += gap;
		addToggle(x, y, w, h, "Particules minimales", c.minimalParticles, v -> c.minimalParticles = v);
		y += gap;
		addToggle(x, y, w, h, "Sans ombres d'entités", c.noEntityShadows, v -> c.noEntityShadows = v);
		y += gap;
		addToggle(x, y, w, h, "Sans lumière douce", c.noSmoothLighting, v -> c.noSmoothLighting = v);
		y += gap;
		addToggle(x, y, w, h, "Sans fondu des biomes", c.noBiomeBlend, v -> c.noBiomeBlend = v);
		y += gap;
		addToggle(x, y, w, h, "Sans mipmaps", c.noMipmaps, v -> c.noMipmaps = v);
		y += gap;
		addToggle(x, y, w, h, "Couper la V-Sync", c.vsyncOff, v -> c.vsyncOff = v);
		y += gap;

		addDrawableChild(ButtonWidget.builder(
			Text.literal("FPS max : " + (c.maxFps >= 260 ? "illimité" : String.valueOf(c.maxFps))),
			b -> {
				c.maxFps = c.maxFps == 144 ? 240 : (c.maxFps == 240 ? 260 : 144);
				changed();
				clearAndInit();
			}).dimensions(x, y, w, h).build());
		y += gap + 6;

		addDrawableChild(ButtonWidget.builder(Text.literal("Télécharger Sodium"), b -> {
			try {
				Util.getOperatingSystem().open(URI.create("https://modrinth.com/mod/sodium"));
			} catch (Exception ignored) {
			}
		}).dimensions(x, y, w / 2 - 2, h).build());

		addDrawableChild(ButtonWidget.builder(Text.literal("Terminé"), b -> close())
			.dimensions(x + w / 2 + 2, y, w / 2 - 2, h).build());
	}

	private void addToggle(int x, int y, int w, int h, String label, boolean value,
						   java.util.function.Consumer<Boolean> setter) {
		addDrawableChild(ButtonWidget.builder(Text.literal(label + " : " + onOff(value)), b -> {
			setter.accept(!value);
			changed();
			clearAndInit();
		}).dimensions(x, y, w, h).build());
	}

	private void changed() {
		mod.config().save();
		mod.reapply();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 130, 0xFFFFFF);
		boolean sodium = FabricLoader.getInstance().isModLoaded("sodium");
		context.drawCenteredTextWithShadow(this.textRenderer,
			Text.literal(sodium ? "Sodium : §ainstallé" : "Sodium : §cnon installé §7(recommandé)"),
			this.width / 2, this.height / 2 - 116, 0xAAAAAA);
	}

	@Override
	public void close() {
		this.client.setScreen(parent);
	}
}
