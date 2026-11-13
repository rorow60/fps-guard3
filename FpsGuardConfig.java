package com.nicolas.fpsguard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Réglages du mode, sauvegardés dans config/fpsguard.json. */
public class FpsGuardConfig {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("fpsguard.json");

	public boolean fastGraphics = true;
	public boolean noClouds = true;
	public boolean minimalParticles = true;
	public boolean noEntityShadows = true;
	public boolean noSmoothLighting = true;
	public boolean noBiomeBlend = true;
	public boolean noMipmaps = true;
	public boolean vsyncOff = true;
	public int maxFps = 260; // 260 = illimité

	public static FpsGuardConfig load() {
		try {
			if (Files.exists(FILE)) {
				FpsGuardConfig c = GSON.fromJson(Files.readString(FILE), FpsGuardConfig.class);
				if (c != null) {
					return c;
				}
			}
		} catch (Exception ignored) {
		}
		return new FpsGuardConfig();
	}

	public void save() {
		try {
			Files.writeString(FILE, GSON.toJson(this));
		} catch (IOException ignored) {
		}
	}
}
