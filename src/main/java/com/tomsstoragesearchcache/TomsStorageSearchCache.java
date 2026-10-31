package com.tomsstoragesearchcache;

import com.tomsstoragesearchcache.client.ConfigScreen;
import com.tomsstoragesearchcache.config.SearchCacheConfig;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(TomsStorageSearchCache.MODID)
public class TomsStorageSearchCache {
	public static final String MODID = "tomsstoragesearchcache";

	public TomsStorageSearchCache() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SearchCacheConfig.SPEC);
		if (FMLEnvironment.dist.isClient() && ModList.get().isLoaded("cloth_config")) {
			ConfigScreen.register();
		}
	}
}
