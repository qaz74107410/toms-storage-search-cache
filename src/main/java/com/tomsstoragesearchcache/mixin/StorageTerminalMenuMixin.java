package com.tomsstoragesearchcache.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.tom.storagemod.gui.StorageTerminalMenu;
import com.tomsstoragesearchcache.emi.TerminalSnapshotGate;

import net.minecraft.nbt.CompoundTag;

/** Every terminal packet from the server may have replaced the client's item list. */
@Mixin(value = StorageTerminalMenu.class, remap = false)
public abstract class StorageTerminalMenuMixin {
	@Inject(method = "receiveClientNBTPacket", at = @At("TAIL"))
	private void tomsstoragesearchcache$itemListUpdated(CompoundTag message, CallbackInfo ci) {
		TerminalSnapshotGate.itemListUpdated();
	}
}
