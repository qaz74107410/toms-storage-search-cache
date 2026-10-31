package com.tomsstoragesearchcache.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class SearchCacheConfig {
	public static final long STOCK_SECONDS = 5;
	public static final long DEFAULT_SECONDS = 3600;
	public static final int DEFAULT_EMI_MILLIS = 500;

	public static final ForgeConfigSpec SPEC;
	public static final ForgeConfigSpec.LongValue CACHE_SECONDS;
	public static final ForgeConfigSpec.IntValue EMI_TERMINAL_SYNC_MILLIS;
	public static final ForgeConfigSpec.BooleanValue EMI_SKIP_UNCHANGED;

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
		b.comment("EMI craftables sidebar").push("emi");
		EMI_TERMINAL_SYNC_MILLIS = b.comment(
				"While EMI's craftables sidebar is visible, EMI rebuilds a snapshot of everything you",
				"can craft from and compares it with the last one every 50 ms. Inside a storage",
				"terminal that snapshot is the whole storage network, rebuilt 20 times a second,",
				"while the terminal itself only refreshes its item list about every 500 ms.",
				"",
				"This is the interval EMI uses inside a storage terminal. The craftables list is",
				"unchanged; it can only appear up to this long after the terminal's item list does.",
				"Every other screen keeps EMI's own interval. 50 = stock behaviour.")
				.defineInRange("terminalSyncMillis", DEFAULT_EMI_MILLIS, 50, 5000);
		EMI_SKIP_UNCHANGED = b.comment(
				"Each of those snapshots costs tens of milliseconds of render thread in a large network,",
				"even when nothing changed. When true, inside a storage terminal EMI only rebuilds it",
				"after the terminal's item list or a slot in the menu (inventory, crafting grid) has",
				"changed. The craftables list is the same. false = stock behaviour.")
				.define("skipUnchangedSnapshots", true);
		b.pop();
		SPEC = b.build();
	}

	public static long cacheSeconds() {
		return SPEC.isLoaded() ? CACHE_SECONDS.get() : DEFAULT_SECONDS;
	}

	public static int emiTerminalSyncMillis() {
		return SPEC.isLoaded() ? EMI_TERMINAL_SYNC_MILLIS.get() : DEFAULT_EMI_MILLIS;
	}

	public static boolean emiSkipUnchanged() {
		return !SPEC.isLoaded() || EMI_SKIP_UNCHANGED.get();
	}
}
