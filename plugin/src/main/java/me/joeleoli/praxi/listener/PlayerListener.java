package me.joeleoli.praxi.listener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import me.joeleoli.nucleus.command.CommandHandler;
import me.joeleoli.nucleus.cooldown.Cooldown;
import me.joeleoli.nucleus.player.gui.ViewPlayerMenu;
import me.joeleoli.nucleus.util.PlayerUtil;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.nucleus.util.TaskUtil;
import me.joeleoli.nucleus.util.TimeUtil;
import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.kit.Kit;
import me.joeleoli.praxi.kit.NamedKit;
import me.joeleoli.praxi.kit.gui.KitManagementMenu;
import me.joeleoli.praxi.kit.gui.SelectLadderKitMenu;
import me.joeleoli.praxi.match.Match;
import me.joeleoli.praxi.party.gui.OtherPartiesMenu;
import me.joeleoli.praxi.party.gui.PartyEventSelectEventMenu;
import me.joeleoli.praxi.player.PlayerHotbar;
import me.joeleoli.praxi.player.PraxiPlayer;
import me.joeleoli.praxi.queue.Queue;
import me.joeleoli.praxi.queue.gui.QueueJoinMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerListener implements Listener {

	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		if (event.getItem().getType() == Material.GOLDEN_APPLE) {
			if (event.getItem().hasItemMeta() &&
			    event.getItem().getItemMeta().getDisplayName().contains("Golden Head")) {
				final Player player = event.getPlayer();

				player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));
				player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0));
				player.setFoodLevel(Math.min(player.getFoodLevel() + 6, 20));
			}
		}
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		final Player player = event.getPlayer();
		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

		if (praxiPlayer.isSpectating() && event.getRightClicked() instanceof Player && player.getItemInHand() != null) {
			final Player target = (Player) event.getRightClicked();

			if (PlayerHotbar.fromItemStack(player.getItemInHand()) == PlayerHotbar.HotbarItem.VIEW_INVENTORY) {
				new ViewPlayerMenu(target).openMenu(player);
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(event.getPlayer().getUniqueId());

		if (event.getMessage().startsWith("@") || event.getMessage().startsWith("!")) {
			if (praxiPlayer.getParty() != null) {
				event.setCancelled(true);
				praxiPlayer.getParty().broadcast(
						Style.GOLD + "[Party]" + Style.RESET + " " + event.getPlayer().getDisplayName() + Style.RESET +
						": " + Style.strip(event.getMessage().substring(1)));
				return;
			}
		}

		if (praxiPlayer.getKitEditor().isRenaming()) {
			event.setCancelled(true);

			if (event.getMessage().length() > 16) {
				event.getPlayer().sendMessage(Style.RED + "A kit name cannot be more than 16 characters long.");
				return;
			}

			if (!praxiPlayer.isInMatch()) {
				new KitManagementMenu(praxiPlayer.getKitEditor().getSelectedLadder()).openMenu(event.getPlayer());
			}

			praxiPlayer.getKitEditor().getSelectedKit().setName(event.getMessage());
			praxiPlayer.getKitEditor().setActive(false);
			praxiPlayer.getKitEditor().setRename(false);
			praxiPlayer.getKitEditor().setSelectedKit(null);
		}
	}

	@EventHandler
	public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
		PraxiPlayer praxiPlayer = new PraxiPlayer(event.getUniqueId(), null);

		praxiPlayer.setName(event.getName());
		praxiPlayer.load();

		if (!praxiPlayer.isLoaded()) {
			event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
			event.setKickMessage(ChatColor.RED + "Failed to load your profile. Try again later.");
			return;
		}

		PraxiPlayer.getPlayers().put(event.getUniqueId(), praxiPlayer);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.setJoinMessage(null);

		event.getPlayer().sendMessage(new String[]{
				Style.getBorderLine(),
				"",
				Style.center(Style.YELLOW + "Welcome to " + Style.PINK + Style.BOLD + "MineXD Practice" +
				             Style.YELLOW + "!"),
				"",
				Style.center(Style.YELLOW + "Follow our twitter " + Style.PINK + "@MineXD" + Style.YELLOW +
				             " for updates and giveaways."),
				"",
				Style.getBorderLine()
		});

		PlayerUtil.spawn(event.getPlayer());

		PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(event.getPlayer().getUniqueId());
		praxiPlayer.loadHotbar();

		Bukkit.getOnlinePlayers().forEach(player -> {
			player.hidePlayer(event.getPlayer());
			event.getPlayer().hidePlayer(player);
		});
	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		if (event.getReason() != null) {
			if (event.getReason().contains("Flying is not enabled")) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuit(PlayerQuitEvent event) {
		event.setQuitMessage(null);

		final PraxiPlayer praxiPlayer = PraxiPlayer.getPlayers().remove(event.getPlayer().getUniqueId());

		if (praxiPlayer != null) {
			if (praxiPlayer.getParty() != null) {
				if (praxiPlayer.getParty().isLeader(event.getPlayer().getUniqueId())) {
					praxiPlayer.getParty().disband();
				} else {
					praxiPlayer.getParty().leave(event.getPlayer(), false);
				}
			}

			if (praxiPlayer.getRematchData() != null) {
				final Player target =
						Praxi.getInstance().getServer().getPlayer(praxiPlayer.getRematchData().getTarget());

				if (target != null && target.isOnline()) {
					PraxiPlayer.getByUuid(target.getUniqueId()).refreshHotbar();
				}
			}

			TaskUtil.runAsync(praxiPlayer::save);

			if (praxiPlayer.isInMatch()) {
				praxiPlayer.getMatch().handleDeath(event.getPlayer(), null, true);
			} else if (praxiPlayer.isInQueue()) {
				Queue queue = Queue.getByUuid(praxiPlayer.getQueuePlayer().getQueueUuid());

				if (queue == null) {
					return;
				}

				queue.removePlayer(praxiPlayer.getQueuePlayer());
			} else if (praxiPlayer.isInEvent()) {
				praxiPlayer.getEvent().handleLeave(event.getPlayer());
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getItem() != null && event.getAction().name().contains("RIGHT")) {
			final Player player = event.getPlayer();
			final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

			if (praxiPlayer.isInMatch()) {
				if (event.getItem().hasItemMeta() && event.getItem().getItemMeta().hasDisplayName()) {
					if (event.getItem().equals(Kit.DEFAULT_KIT)) {
						event.setCancelled(true);

						final Kit kit = praxiPlayer.getMatch().getLadder().getDefaultKit();

						player.getInventory().setArmorContents(kit.getArmor());
						player.getInventory().setContents(kit.getContents());
						player.updateInventory();
						player.sendMessage(
								Style.YELLOW + "You have been given the" + Style.AQUA + " Default " + Style.YELLOW +
								"kit.");
						return;
					}
				}

				if (event.getItem().hasItemMeta() && event.getItem().getItemMeta().hasDisplayName()) {
					final String displayName = ChatColor.stripColor(event.getItem().getItemMeta().getDisplayName());

					if (displayName.startsWith("Kit: ")) {
						final String kitName = displayName.replace("Kit: ", "");

						for (NamedKit kit : praxiPlayer.getKits(praxiPlayer.getMatch().getLadder())) {
							if (kit != null) {
								if (ChatColor.stripColor(kit.getName()).equals(kitName)) {
									event.setCancelled(true);

									player.getInventory().setArmorContents(kit.getArmor());
									player.getInventory().setContents(kit.getContents());
									player.updateInventory();
									player.sendMessage(
											Style.YELLOW + "You have been given the " + Style.AQUA + kit.getName() +
											Style.YELLOW + " kit.");
									return;
								}
							}
						}
					}
				}

				if (event.getItem().getType() == Material.ENDER_PEARL ||
				    (event.getItem().getType() == Material.POTION && event.getItem().getDurability() >= 16_000)) {
					if (praxiPlayer.isInMatch() && praxiPlayer.getMatch().isStarting()) {
						event.setCancelled(true);
						return;
					}
				}

				if (event.getItem().getType() == Material.ENDER_PEARL && event.getClickedBlock() == null) {
					if (!praxiPlayer.isInMatch() || (praxiPlayer.isInMatch() && !praxiPlayer.getMatch().isFighting())) {
						event.setCancelled(true);
						return;
					}

					if (praxiPlayer.getMatch().isStarting()) {
						event.setCancelled(true);
						return;
					}

					if (!praxiPlayer.getEnderpearlCooldown().hasExpired()) {
						final String time =
								TimeUtil.millisToSeconds(praxiPlayer.getEnderpearlCooldown().getRemaining());
						final String context = "second" + (time.equalsIgnoreCase("1.0") ? "s" : "");

						event.setCancelled(true);
						player.sendMessage(
								Style.YELLOW + "You are on pearl cooldown for " + Style.PINK + time + " " + context +
								Style.YELLOW + ".");
					} else {
						praxiPlayer.setEnderpearlCooldown(new Cooldown(16_000));
					}
				}
			} else {
				PlayerHotbar.HotbarItem hotbarItem = PlayerHotbar.fromItemStack(event.getItem());

				if (hotbarItem == null) {
					return;
				}

				event.setCancelled(true);

				switch (hotbarItem) {
					case QUEUE_JOIN_RANKED: {
						if (praxiPlayer.isInLobby()) {
							new QueueJoinMenu(true).openMenu(event.getPlayer());
						}
					}
					break;
					case QUEUE_JOIN_UNRANKED: {
						if (praxiPlayer.isInLobby()) {
							new QueueJoinMenu(false).openMenu(event.getPlayer());
						}
					}
					break;
					case QUEUE_LEAVE: {
						if (praxiPlayer.isInQueue()) {
							Queue queue = Queue.getByUuid(praxiPlayer.getQueuePlayer().getQueueUuid());

							if (queue != null) {
								queue.removePlayer(praxiPlayer.getQueuePlayer());
							}
						}
					}
					break;
					case SPECTATE_STOP: {
						CommandHandler.executeCommand(event.getPlayer(), "stopspectate");
					}
					break;
					case PARTY_CREATE: {
						CommandHandler.executeCommand(event.getPlayer(), "party create");
					}
					break;
					case PARTY_DISBAND: {
						CommandHandler.executeCommand(event.getPlayer(), "party disband");
					}
					break;
					case PARTY_INFORMATION: {
						CommandHandler.executeCommand(event.getPlayer(), "party info");
					}
					break;
					case PARTY_LEAVE: {
						CommandHandler.executeCommand(event.getPlayer(), "party leave");
					}
					break;
					case PARTY_EVENTS: {
						new PartyEventSelectEventMenu().openMenu(player);
					}
					break;
					case OTHER_PARTIES: {
						new OtherPartiesMenu().openMenu(event.getPlayer());
					}
					break;
					case KIT_EDITOR: {
						if (praxiPlayer.isInLobby() || praxiPlayer.isInQueue()) {
							new SelectLadderKitMenu().openMenu(event.getPlayer());
						}
					}
					break;
					case SETTINGS: {
						CommandHandler.executeCommand(event.getPlayer(), "settings");
					}
					break;
					case REMATCH_REQUEST:
					case REMATCH_ACCEPT: {
						CommandHandler.executeCommand(event.getPlayer(), "rematch");
					}
					break;
					case EVENT_JOIN: {
						CommandHandler.executeCommand(event.getPlayer(), "event join");
					}
					break;
					case EVENT_LEAVE: {
						CommandHandler.executeCommand(event.getPlayer(), "event leave");
					}
					break;
				}
			}
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		event.setDeathMessage(null);

		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(event.getEntity().getUniqueId());

		if (praxiPlayer.isInMatch()) {
			final List<Item> entities = new ArrayList<>();

			event.getDrops().forEach(itemStack -> {
				entities.add(event.getEntity().getLocation().getWorld()
				                  .dropItemNaturally(event.getEntity().getLocation(), itemStack));
			});
			event.getDrops().clear();

			praxiPlayer.getMatch().getEntities().addAll(entities);
			praxiPlayer.getMatch().handleDeath(event.getEntity(), event.getEntity().getKiller(), false);
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		event.setRespawnLocation(event.getPlayer().getLocation());

		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(event.getPlayer().getUniqueId());

		if (praxiPlayer.isInMatch()) {
			praxiPlayer.getMatch().handleRespawn(event.getPlayer());
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(event.getPlayer().getUniqueId());

		if (praxiPlayer.isInMatch()) {
			final Match match = praxiPlayer.getMatch();

			if (match.getLadder().isBuild() && praxiPlayer.getMatch().isFighting()) {
				if (match.getLadder().isSpleef()) {
					event.setCancelled(true);
					return;
				}

				final Arena arena = match.getArena();
				final int x = (int) event.getBlockPlaced().getLocation().getX();
				final int y = (int) event.getBlockPlaced().getLocation().getY();
				final int z = (int) event.getBlockPlaced().getLocation().getZ();

				if (y > arena.getMaxBuildHeight()) {
					event.getPlayer().sendMessage(Style.RED + "You have reached the maximum build height.");
					event.setCancelled(true);
					return;
				}

				if (x >= arena.getX1() && x <= arena.getX2() && y >= arena.getY1() && y <= arena.getY2() &&
				    z >= arena.getZ1() && z <= arena.getZ2()) {
					match.getPlacedBlocks().add(event.getBlock().getLocation());
				} else {
					event.setCancelled(true);
				}
			} else {
				event.setCancelled(true);
			}
		} else {
			if (event.getPlayer().getGameMode() != GameMode.CREATIVE || !event.getPlayer().isOp()) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBucketEmpty(PlayerBucketEmptyEvent event) {
		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(event.getPlayer().getUniqueId());

		if (praxiPlayer.isInMatch()) {
			final Match match = praxiPlayer.getMatch();

			if (match.getLadder().isBuild() && praxiPlayer.getMatch().isFighting()) {
				final Arena arena = match.getArena();
				final Block block = event.getBlockClicked().getRelative(event.getBlockFace());
				final int x = (int) block.getLocation().getX();
				final int y = (int) block.getLocation().getY();
				final int z = (int) block.getLocation().getZ();

				if (y > arena.getMaxBuildHeight()) {
					event.getPlayer().sendMessage(Style.RED + "You have reached the maximum build height.");
					event.setCancelled(true);
					return;
				}

				if (x >= arena.getX1() && x <= arena.getX2() && y >= arena.getY1() && y <= arena.getY2() &&
				    z >= arena.getZ1() && z <= arena.getZ2()) {
					match.getPlacedBlocks().add(block.getLocation());
				} else {
					event.setCancelled(true);
				}
			} else {
				event.setCancelled(true);
			}
		} else {
			if (event.getPlayer().getGameMode() != GameMode.CREATIVE || !event.getPlayer().isOp()) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(event.getPlayer().getUniqueId());

		if (praxiPlayer.isInMatch()) {
			final Match match = praxiPlayer.getMatch();

			if (match.getLadder().isBuild() && praxiPlayer.getMatch().isFighting()) {
				if (match.getLadder().isSpleef()) {
					if (event.getBlock().getType() == Material.SNOW_BLOCK ||
					    event.getBlock().getType() == Material.SNOW) {
						match.getChangedBlocks().add(event.getBlock().getState());

						event.getBlock().setType(Material.AIR);
						event.getPlayer().getInventory().addItem(new ItemStack(Material.SNOW_BALL, 4));
						event.getPlayer().updateInventory();
					} else {
						event.setCancelled(true);
					}
				} else if (!match.getPlacedBlocks().remove(event.getBlock().getLocation())) {
					event.setCancelled(true);
				}
			} else {
				event.setCancelled(true);
			}
		} else {
			if (event.getPlayer().getGameMode() != GameMode.CREATIVE || !event.getPlayer().isOp()) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(event.getPlayer().getUniqueId());

		if (praxiPlayer.isInMatch()) {
			if (!praxiPlayer.getMatch().getMatchPlayer(event.getPlayer()).isAlive()) {
				event.setCancelled(true);
				return;
			}

			Iterator<Entity> entityIterator = praxiPlayer.getMatch().getEntities().iterator();

			while (entityIterator.hasNext()) {
				Entity entity = entityIterator.next();

				if (entity instanceof Item && entity.equals(event.getItem())) {
					entityIterator.remove();
					return;
				}
			}

			event.setCancelled(true);
		} else if (praxiPlayer.isSpectating()) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(event.getPlayer().getUniqueId());

		if (event.getItemDrop().getItemStack().getType() == Material.BOOK ||
		    event.getItemDrop().getItemStack().getType() == Material.ENCHANTED_BOOK) {
			event.getItemDrop().remove();
			return;
		}

		if (praxiPlayer.isInMatch()) {
			if (praxiPlayer.getMatch() != null) {
				if (event.getItemDrop().getItemStack().getType() == Material.GLASS_BOTTLE) {
					event.getItemDrop().remove();
					return;
				}

				praxiPlayer.getMatch().getEntities().add(event.getItemDrop());
			}
		} else {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			final Player player = (Player) event.getWhoClicked();

			if (event.getClickedInventory() != null && event.getClickedInventory() instanceof CraftingInventory) {
				if (player.getGameMode() != GameMode.CREATIVE) {
					event.setCancelled(true);
					return;
				}
			}

			final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

			if (!praxiPlayer.isInMatch() && player.getGameMode() == GameMode.SURVIVAL) {
				final Inventory clicked = event.getClickedInventory();

				if (praxiPlayer.getKitEditor().isActive()) {
					if (clicked == null) {
						event.setCancelled(true);
						event.setCursor(null);
						player.updateInventory();
					} else if (clicked.equals(player.getOpenInventory().getTopInventory())) {
						if (event.getCursor().getType() != Material.AIR &&
						    event.getCurrentItem().getType() == Material.AIR ||
						    event.getCursor().getType() != Material.AIR &&
						    event.getCurrentItem().getType() != Material.AIR) {
							event.setCancelled(true);
							event.setCursor(null);
							player.updateInventory();
						}
					}
				} else {
					if (clicked != null && clicked.equals(player.getInventory())) {
						event.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerItemDamage(PlayerItemDamageEvent event) {
		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(event.getPlayer().getUniqueId());

		if (praxiPlayer.isInLobby()) {
			event.setCancelled(true);
		}
	}

}
