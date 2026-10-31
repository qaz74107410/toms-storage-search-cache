package com.tomsstoragesearchcache;

import com.tomsstoragesearchcache.config.SearchCacheConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(TomsStorageSearchCache.MODID)
public class TomsStorageSearchCache {
	public static final String MODID = "tomsstoragesearchcache";

	public TomsStorageSearchCache() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SearchCacheConfig.SPEC);
	}
}
