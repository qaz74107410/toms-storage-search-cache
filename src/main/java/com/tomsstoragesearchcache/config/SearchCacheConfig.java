package com.tomsstoragesearchcache.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class SearchCacheConfig {
	public static final long STOCK_SECONDS = 5;
	public static final long DEFAULT_SECONDS = 3600;

	public static final ForgeConfigSpec SPEC;
	public static final ForgeConfigSpec.LongValue CACHE_SECONDS;

	static {
		ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
		b.comment("Tom's Simple Storage search cache").push("search");
		CACHE_SECONDS = b.comment(
				"A plain terminal search (no @, # or $ prefix) first matches item names. For every",
				"stored item whose name does not match, it then builds that item's full tooltip -",
				"which fires the tooltip event for every tooltip mod in the pack - and matches",
				"against that. Tom's Simple Storage caches those tooltips, but drops each one 5",
				"seconds after it was last used, so after a short pause, or as the query narrows,",
				"the terminal rebuilds hundreds of tooltips on the render thread on one keystroke.",
				"",
				"This is how long an unused tooltip (and the tag and NBT text used by # and $",
				"searches) is kept. Search results are identical; only the cache lifetime changes.",
				"The cache key includes the item's NBT, so a changed item gets a fresh tooltip.",
				"5 = stock behaviour.")
				.defineInRange("cacheSeconds", DEFAULT_SECONDS, 5, 86400);
		b.pop();
		SPEC = b.build();
	}

	public static long cacheSeconds() {
		return SPEC.isLoaded() ? CACHE_SECONDS.get() : DEFAULT_SECONDS;
	}
}
