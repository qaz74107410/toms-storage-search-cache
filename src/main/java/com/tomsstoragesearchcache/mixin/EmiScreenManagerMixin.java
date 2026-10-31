package com.tomsstoragesearchcache.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.tom.storagemod.gui.AbstractStorageTerminalScreen;
import com.tomsstoragesearchcache.config.SearchCacheConfig;
import com.tomsstoragesearchcache.emi.TerminalSnapshotGate;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/**
 * updateCraftables rebuilds an EmiPlayerInventory and compares it with the previous one every 50 ms
 * while the craftables sidebar is visible, 400 ms otherwise. In a storage terminal that inventory is
 * the whole network, and building and comparing it costs tens of milliseconds of render thread per
 * pass. Inside a terminal the pass is skipped while nothing it reads has changed, and its interval is
 * raised. Every other screen keeps EMI's own behaviour.
 */
@Mixin(value = EmiScreenManager.class, remap = false)
public abstract class EmiScreenManagerMixin {
	@Shadow private static EmiPlayerInventory lastPlayerInventory;

	@Inject(method = "updateCraftables", at = @At("HEAD"), cancellable = true)
	private static void tomsstoragesearchcache$skipUnchanged(CallbackInfo ci) {
		if (lastPlayerInventory != null && SearchCacheConfig.emiSkipUnchanged()
				&& EmiApi.getHandledScreen() instanceof AbstractStorageTerminalScreen<?> screen
				&& TerminalSnapshotGate.unchangedSinceSnapshot(screen)) {
			ci.cancel();
		}
	}

	@Inject(method = "updateCraftables", at = @At(value = "INVOKE",
			target = "Ldev/emi/emi/api/recipe/EmiPlayerInventory;of(Lnet/minecraft/world/entity/player/Player;)Ldev/emi/emi/api/recipe/EmiPlayerInventory;"))
	private static void tomsstoragesearchcache$snapshotTaken(CallbackInfo ci) {
		AbstractContainerScreen<?> screen = EmiApi.getHandledScreen();
		if (screen instanceof AbstractStorageTerminalScreen<?>) TerminalSnapshotGate.snapshotTaken(screen);
	}

	@ModifyConstant(method = "updateCraftables", constant = @Constant(intValue = 50))
	private static int tomsstoragesearchcache$terminalSyncInterval(int stock) {
		return EmiApi.getHandledScreen() instanceof AbstractStorageTerminalScreen ? SearchCacheConfig.emiTerminalSyncMillis() : stock;
	}
}
