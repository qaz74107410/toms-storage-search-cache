package com.tomsstoragesearchcache.mixin;

import java.util.concurrent.TimeUnit;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tomsstoragesearchcache.config.SearchCacheConfig;

/**
 * The three search caches are built in the static initializer with expireAfterAccess(5s).
 * Each one is wrapped in a cache with a longer lifetime whose loader is the stock cache,
 * so Tom's own loader still computes every value and the results are unchanged.
 */
@Mixin(targets = "com.tom.storagemod.gui.AbstractStorageTerminalScreen", remap = false)
public abstract class AbstractStorageTerminalScreenMixin {
	@Shadow @Final @Mutable private static LoadingCache<Object, Object> tooltipCache;
	@Shadow @Final @Mutable private static LoadingCache<Object, Object> nbtCache;
	@Shadow @Final @Mutable private static LoadingCache<Object, Object> tagCache;

	@Inject(method = "<clinit>", at = @At("RETURN"))
	private static void tomsstoragesearchcache$extendCaches(CallbackInfo ci) {
		long seconds = SearchCacheConfig.cacheSeconds();
		if (seconds <= SearchCacheConfig.STOCK_SECONDS) return;
		tooltipCache = tomsstoragesearchcache$wrap(tooltipCache, seconds);
		nbtCache = tomsstoragesearchcache$wrap(nbtCache, seconds);
		tagCache = tomsstoragesearchcache$wrap(tagCache, seconds);
	}

	private static LoadingCache<Object, Object> tomsstoragesearchcache$wrap(LoadingCache<Object, Object> stock, long seconds) {
		return CacheBuilder.newBuilder().expireAfterAccess(seconds, TimeUnit.SECONDS).build(CacheLoader.from(stock::getUnchecked));
	}
}
