package com.tomsstoragesearchcache.client;

import com.tomsstoragesearchcache.TomsStorageSearchCache;
import com.tomsstoragesearchcache.config.SearchCacheConfig;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

/**
 * Config screen for Mods -> Config, built with Cloth Config. Only loaded on the client when
 * Cloth Config is installed. Component.m_237115_ is Component.translatable; this mod is compiled
 * against the SRG-named client jar, so the runtime name is used directly.
 */
public class ConfigScreen {
	private static final String KEY = TomsStorageSearchCache.MODID + ".config.";

	public static void register() {
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
				() -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> create(parent)));
	}

	private static Screen create(Screen parent) {
		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(text("title"))
				.setSavingRunnable(SearchCacheConfig.SPEC::save);
		ConfigEntryBuilder entries = builder.entryBuilder();

		builder.getOrCreateCategory(text("search"))
				.addEntry(entries.startLongField(text("cacheSeconds"), SearchCacheConfig.CACHE_SECONDS.get())
						.setDefaultValue(SearchCacheConfig.DEFAULT_SECONDS)
						.setMin(SearchCacheConfig.STOCK_SECONDS)
						.setMax(86400)
						.setTooltip(lines("cacheSeconds", 3))
						.requireRestart()
						.setSaveConsumer(SearchCacheConfig.CACHE_SECONDS::set)
						.build());

		builder.getOrCreateCategory(text("emi"))
				.addEntry(entries.startIntField(text("terminalSyncMillis"), SearchCacheConfig.EMI_TERMINAL_SYNC_MILLIS.get())
						.setDefaultValue(SearchCacheConfig.DEFAULT_EMI_MILLIS)
						.setMin(50)
						.setMax(5000)
						.setTooltip(lines("terminalSyncMillis", 3))
						.setSaveConsumer(SearchCacheConfig.EMI_TERMINAL_SYNC_MILLIS::set)
						.build());

		return builder.build();
	}

	private static Component text(String key) {
		return Component.m_237115_(KEY + key);
	}

	private static Component[] lines(String key, int count) {
		Component[] lines = new Component[count];
		for (int i = 0; i < count; i++) lines[i] = text(key + ".tooltip." + i);
		return lines;
	}
}
