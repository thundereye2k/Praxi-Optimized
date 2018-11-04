package me.joeleoli.praxi.player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.joeleoli.nucleus.util.ItemBuilder;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.Praxi;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class PlayerHotbar {

	@Getter
	private static Map<HotbarItem, ItemStack> items = new HashMap<>();

	// Utility class - cannot be instantiated
	private PlayerHotbar() {
	}

	public static void init() {
		items.put(
				HotbarItem.QUEUE_JOIN_UNRANKED,
				new ItemBuilder(Material.IRON_SWORD).name(Style.GRAY + Style.BOLD + "Unranked Queue")
				                                    .lore(Style.YELLOW + "Right-click to join an unranked queue.")
				                                    .build()
		);
		items.put(
				HotbarItem.QUEUE_JOIN_RANKED,
				new ItemBuilder(Material.DIAMOND_SWORD).name(Style.GREEN + Style.BOLD + "Ranked Queue")
				                                       .lore(Style.YELLOW + "Right-click to join a ranked queue.")
				                                       .build()
		);
		items.put(
				HotbarItem.QUEUE_LEAVE,
				new ItemBuilder(Material.INK_SACK).durability(1).name(Style.RED + Style.BOLD + "Leave Queue")
				                                  .lore(Style.YELLOW + "Right-click to leave your queue.").build()
		);
		items.put(
				HotbarItem.PARTY_EVENTS,
				new ItemBuilder(Material.DIAMOND_SWORD).name(Style.GREEN + Style.BOLD + "Party Events")
				                                       .lore(Style.YELLOW + "Right-click to start a party event.")
				                                       .build()
		);
		items.put(
				HotbarItem.PARTY_CREATE,
				new ItemBuilder(Material.NAME_TAG).name(Style.YELLOW + Style.BOLD + "Create Party")
				                                  .lore(Style.YELLOW + "Right-click to create a party.").build()
		);
		items.put(
				HotbarItem.PARTY_DISBAND,
				new ItemBuilder(Material.INK_SACK).durability(1).name(Style.RED + Style.BOLD + "Disband Party")
				                                  .lore(Style.YELLOW + "Right-click to disband your party.").build()
		);
		items.put(
				HotbarItem.PARTY_LEAVE,
				new ItemBuilder(Material.INK_SACK).durability(1).name(Style.RED + Style.BOLD + "Leave Party")
				                                  .lore(Style.YELLOW + "Right-click to leave your party.").build()
		);
		items.put(
				HotbarItem.PARTY_INFORMATION,
				new ItemBuilder(Material.SKULL_ITEM).durability(3).name(Style.YELLOW + Style.BOLD + "Party Information")
				                                    .lore(Style.YELLOW +
				                                          "Right-click to show your party's information.").build()
		);
		items.put(
				HotbarItem.OTHER_PARTIES,
				new ItemBuilder(Material.CHEST).name(Style.BLUE + Style.BOLD + "Other Parties")
				                               .lore(Style.YELLOW + "Right-click to show other parties.").build()
		);
		items.put(HotbarItem.SETTINGS, new ItemBuilder(Material.WATCH).name(Style.PINK + Style.BOLD + "Settings")
		                                                              .lore(Style.YELLOW +
		                                                                    "Right-click to open your settings.")
		                                                              .build());
		items.put(HotbarItem.KIT_EDITOR, new ItemBuilder(Material.BOOK).name(Style.RED + Style.BOLD + "Kit Editor")
		                                                               .lore(Style.YELLOW +
		                                                                     "Right-click to open the kit editor.")
		                                                               .build());
		items.put(
				HotbarItem.SPECTATE_STOP,
				new ItemBuilder(Material.INK_SACK).durability(1).name(Style.RED + Style.BOLD + "Stop Spectating")
				                                  .lore(Style.YELLOW + "Right-click to stop spectating.").build()
		);
		items.put(
				HotbarItem.VIEW_INVENTORY,
				new ItemBuilder(Material.BOOK).name(Style.GOLD + Style.BOLD + "View Inventory")
				                              .lore(Style.YELLOW + "Right-click a player to view their inventory.")
				                              .build()
		);
		items.put(
				HotbarItem.EVENT_JOIN,
				new ItemBuilder(Material.NETHER_STAR).name(Style.AQUA + Style.BOLD + "Join Event")
				                                     .lore(Style.YELLOW + "Right-click to join the event.").build()
		);
		items.put(
				HotbarItem.EVENT_LEAVE,
				new ItemBuilder(Material.NETHER_STAR).name(Style.RED + Style.BOLD + "Leave Event")
				                                     .lore(Style.YELLOW + "Right-click to leave the event.").build()
		);
		items.put(
				HotbarItem.REMATCH_REQUEST,
				new ItemBuilder(Material.EMERALD).name(Style.DARK_GREEN + Style.BOLD + "Request Rematch")
				                                 .lore(Style.YELLOW + "Right-click to request a rematch.").build()
		);
		items.put(
				HotbarItem.REMATCH_ACCEPT,
				new ItemBuilder(Material.DIAMOND).name(Style.AQUA + Style.BOLD + "Accept Rematch")
				                                 .lore(Style.YELLOW + "Right-click to accept a rematch.").build()
		);
	}

	public static ItemStack[] getLayout(HotbarLayout layout, PraxiPlayer praxiPlayer) {
		final ItemStack[] toReturn = new ItemStack[9];

		Arrays.fill(toReturn, null);

		switch (layout) {
			case LOBBY: {
				if (praxiPlayer.getParty() == null) {
					toReturn[0] = items.get(HotbarItem.QUEUE_JOIN_UNRANKED);
					toReturn[1] = items.get(HotbarItem.QUEUE_JOIN_RANKED);

					if (praxiPlayer.getRematchData() != null) {
						if (praxiPlayer.getRematchData().isReceive()) {
							toReturn[2] = items.get(HotbarItem.REMATCH_ACCEPT);
						} else {
							toReturn[2] = items.get(HotbarItem.REMATCH_REQUEST);
						}

						toReturn[4] = items.get(HotbarItem.PARTY_CREATE);

						if (Praxi.getInstance().getEventManager().getActiveEvent() != null && Praxi.getInstance().getEventManager().getActiveEvent().isWaiting()) {
							toReturn[6] = items.get(HotbarItem.EVENT_JOIN);
						}
					} else {
						if (Praxi.getInstance().getEventManager().getActiveEvent() != null && Praxi.getInstance().getEventManager().getActiveEvent().isWaiting()) {
							toReturn[3] = items.get(HotbarItem.EVENT_JOIN);
							toReturn[5] = items.get(HotbarItem.PARTY_CREATE);
						} else {
							toReturn[4] = items.get(HotbarItem.PARTY_CREATE);
						}
					}

					toReturn[7] = items.get(HotbarItem.SETTINGS);
					toReturn[8] = items.get(HotbarItem.KIT_EDITOR);
				} else {
					if (praxiPlayer.getParty().isLeader(praxiPlayer.getUuid())) {
						toReturn[0] = items.get(HotbarItem.PARTY_EVENTS);
						toReturn[2] = items.get(HotbarItem.PARTY_INFORMATION);
						toReturn[3] = items.get(HotbarItem.OTHER_PARTIES);
						toReturn[5] = items.get(HotbarItem.PARTY_DISBAND);
						toReturn[7] = items.get(HotbarItem.SETTINGS);
						toReturn[8] = items.get(HotbarItem.KIT_EDITOR);
					} else {
						toReturn[0] = items.get(HotbarItem.PARTY_INFORMATION);
						toReturn[2] = items.get(HotbarItem.OTHER_PARTIES);
						toReturn[4] = items.get(HotbarItem.PARTY_LEAVE);
						toReturn[7] = items.get(HotbarItem.SETTINGS);
						toReturn[8] = items.get(HotbarItem.KIT_EDITOR);
					}
				}
			}
			break;
			case QUEUE: {
				toReturn[0] = items.get(HotbarItem.QUEUE_LEAVE);
				toReturn[7] = items.get(HotbarItem.SETTINGS);
				toReturn[8] = items.get(HotbarItem.KIT_EDITOR);
			}
			break;
			case EVENT_SPECTATE: {
				toReturn[0] = items.get(HotbarItem.EVENT_LEAVE);
				toReturn[8] = items.get(HotbarItem.SETTINGS);
			}
			break;
			case MATCH_SPECTATE: {
				toReturn[0] = items.get(HotbarItem.SPECTATE_STOP);

				if (!praxiPlayer.getMatch().isRanked()) {
					toReturn[5] = items.get(HotbarItem.VIEW_INVENTORY);
				}

				toReturn[8] = items.get(HotbarItem.SETTINGS);
			}
			break;
		}

		return toReturn;
	}

	public static HotbarItem fromItemStack(ItemStack itemStack) {
		for (Map.Entry<HotbarItem, ItemStack> entry : PlayerHotbar.getItems().entrySet()) {
			if (entry.getValue() != null && entry.getValue().equals(itemStack)) {
				return entry.getKey();
			}
		}

		return null;
	}

	@AllArgsConstructor
	public enum HotbarItem {
		QUEUE_JOIN_RANKED,
		QUEUE_JOIN_UNRANKED,
		QUEUE_LEAVE,
		PARTY_EVENTS,
		PARTY_CREATE,
		PARTY_DISBAND,
		PARTY_LEAVE,
		PARTY_INFORMATION,
		OTHER_PARTIES,
		SETTINGS,
		KIT_EDITOR,
		SPECTATE_STOP,
		VIEW_INVENTORY,
		EVENT_JOIN,
		EVENT_LEAVE,
		REMATCH_REQUEST,
		REMATCH_ACCEPT
	}

	public enum HotbarLayout {
		LOBBY,
		QUEUE,
		MATCH_SPECTATE,
		EVENT_SPECTATE,
	}

}
