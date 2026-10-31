package com.tomsstoragesearchcache.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.tom.storagemod.gui.AbstractStorageTerminalScreen;
import com.tomsstoragesearchcache.config.SearchCacheConfig;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.screen.EmiScreenManager;

/**
 * While the craftables sidebar is visible, EMI rebuilds its inventory snapshot every 50 ms and
 * compares it with the previous one. In a storage terminal that snapshot is the whole network,
 * while Tom's only refreshes the terminal's item list every half second or so. Only the
 * interval used inside a terminal is changed; every other screen keeps EMI's own.
 */
@Mixin(value = EmiScreenManager.class, remap = false)
public abstract class EmiScreenManagerMixin {
	@ModifyConstant(method = "updateCraftables", constant = @Constant(intValue = 50))
	private static int tomsstoragesearchcache$terminalSyncInterval(int stock) {
		return EmiApi.getHandledScreen() instanceof AbstractStorageTerminalScreen ? SearchCacheConfig.emiTerminalSyncMillis() : stock;
	}
}
