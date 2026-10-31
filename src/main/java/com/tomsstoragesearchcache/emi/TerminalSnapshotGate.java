package com.tomsstoragesearchcache.emi;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Decides whether EMI's inventory snapshot inside a storage terminal can be skipped.
 *
 * Tom's EMI handler builds the snapshot from two things: the menu's slots (player inventory and
 * crafting grid) and the terminal's item list. The item list is only replaced when the server sends
 * a terminal packet, which is counted here. The slots are fingerprinted by stack identity and count:
 * the client replaces the ItemStack object on every slot update from the server, and its own click
 * handling either moves stack objects or changes their count. If neither has changed since the
 * last snapshot EMI actually built, a new snapshot would be identical.
 *
 * Minecraft members are written with their SRG names, which is what they are called at runtime:
 * m_6262_ = getMenu, f_38839_ = slots, m_7993_ = getItem, m_41613_ = getCount.
 */
public class TerminalSnapshotGate {
	private static int itemListVersion;

	private static Object snapshotScreen;
	private static int snapshotVersion;
	private static int snapshotSlots;

	public static void itemListUpdated() {
		itemListVersion++;
	}

	public static boolean unchangedSinceSnapshot(AbstractContainerScreen<?> screen) {
		return screen == snapshotScreen && itemListVersion == snapshotVersion && slotFingerprint(screen) == snapshotSlots;
	}

	public static void snapshotTaken(AbstractContainerScreen<?> screen) {
		snapshotScreen = screen;
		snapshotVersion = itemListVersion;
		snapshotSlots = slotFingerprint(screen);
	}

	private static int slotFingerprint(AbstractContainerScreen<?> screen) {
		int h = 1;
		for (Slot slot : screen.m_6262_().f_38839_) {
			ItemStack stack = slot.m_7993_();
			h = 31 * h + System.identityHashCode(stack);
			h = 31 * h + stack.m_41613_();
		}
		return h;
	}
}
