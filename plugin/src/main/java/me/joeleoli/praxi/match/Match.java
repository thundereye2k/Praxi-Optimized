package me.joeleoli.praxi.match;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import me.joeleoli.fairfight.FairFight;
import me.joeleoli.nucleus.chat.ChatComponentBuilder;
import me.joeleoli.nucleus.nametag.NameTagHandler;
import me.joeleoli.nucleus.util.PlayerUtil;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.nucleus.util.TaskUtil;
import me.joeleoli.nucleus.util.TimeUtil;
import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.ladder.Ladder;
import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.player.PraxiPlayer;
import me.joeleoli.praxi.player.RematchData;
import me.joeleoli.ragespigot.RageSpigot;
import me.joeleoli.ragespigot.knockback.KnockbackProfile;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.server.v1_8_R3.EntityLightning;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityWeather;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

@Getter
public abstract class Match {

	protected static final BaseComponent[] HOVER_TEXT =
			new ChatComponentBuilder(Style.GRAY + "Click to view this player's inventory.").create();
	@Getter
	protected static List<Match> matches = new ArrayList<>();
	private UUID matchId = UUID.randomUUID();
	@Setter
	private MatchState state = MatchState.STARTING;
	private UUID queueId;
	@Setter
	private Ladder ladder;
	private Arena arena;
	private boolean ranked;
	private List<MatchSnapshot> snapshots = new ArrayList<>();
	private List<UUID> spectators = new ArrayList<>();
	private List<Entity> entities = new ArrayList<>();
	private List<Location> placedBlocks = new ArrayList<>();
	private List<BlockState> changedBlocks = new ArrayList<>();
	@Setter
	private long startTimestamp;

	public Match(Ladder ladder, Arena arena, boolean ranked) {
		this(null, ladder, arena, ranked);
	}

	public Match(UUID queueId, Ladder ladder, Arena arena, boolean ranked) {
		this.queueId = queueId;
		this.ladder = ladder;
		this.arena = arena;
		this.ranked = ranked;

		matches.add(this);
	}

	public boolean isMatchMakingMatch() {
		return this.queueId != null;
	}

	public boolean isStarting() {
		return this.state == MatchState.STARTING;
	}

	public boolean isFighting() {
		return this.state == MatchState.FIGHTING;
	}

	public boolean isEnding() {
		return this.state == MatchState.ENDING;
	}

	public void setupPlayers() {
		if (this.isSoloMatch()) {
			final MatchPlayer matchPlayerA = this.getMatchPlayerA();
			final MatchPlayer matchPlayerB = this.getMatchPlayerB();

			matchPlayerA.setAlive(true);
			matchPlayerB.setAlive(true);

			final Player playerA = matchPlayerA.toPlayer();
			final Player playerB = matchPlayerB.toPlayer();

			playerA.showPlayer(playerB);
			playerB.showPlayer(playerA);

			if (this.arena.getSpawn1().getBlock().getType() == Material.AIR) {
				playerA.teleport(this.arena.getSpawn1());
			} else {
				playerA.teleport(this.arena.getSpawn1().add(0, 2, 0));
			}

			if (this.arena.getSpawn2().getBlock().getType() == Material.AIR) {
				playerB.teleport(this.arena.getSpawn2());
			} else {
				playerB.teleport(this.arena.getSpawn2().add(0, 2, 0));
			}

			NameTagHandler.addToTeam(playerA, playerB, ChatColor.RED, this.ladder.isBuild());
			NameTagHandler.addToTeam(playerB, playerA, ChatColor.RED, this.ladder.isBuild());

			PlayerUtil.reset(playerA);
			PlayerUtil.reset(playerB);

			playerA.setMaximumNoDamageTicks(this.ladder.getHitDelay());
			playerB.setMaximumNoDamageTicks(this.ladder.getHitDelay());

			if (this.ladder.isSumo()) {
				FairFight.getInstance().getPlayerDataManager().getPlayerData(playerA).setAllowTeleport(true);
				FairFight.getInstance().getPlayerDataManager().getPlayerData(playerB).setAllowTeleport(true);

				PlayerUtil.denyMovement(playerA);
				PlayerUtil.denyMovement(playerB);
			} else {
				for (ItemStack itemStack : PraxiPlayer.getByUuid(playerA.getUniqueId()).getKitItems(this.ladder)) {
					playerA.getInventory().addItem(itemStack);
				}

				for (ItemStack itemStack : PraxiPlayer.getByUuid(playerB.getUniqueId()).getKitItems(this.ladder)) {
					playerB.getInventory().addItem(itemStack);
				}
			}

			if (this.ladder.getKbProfile() != null) {
				final KnockbackProfile profile =
						RageSpigot.INSTANCE.getConfig().getKbProfileByName(this.ladder.getKbProfile());

				if (profile != null) {
					playerA.setKnockbackProfile(profile);
					playerB.setKnockbackProfile(profile);
				}
			}
		} else if (this.isTeamMatch()) {
			final MatchTeam teamA = this.getTeamA();
			final MatchTeam teamB = this.getTeamB();

			for (MatchPlayer matchPlayer : teamA.getTeamPlayers()) {
				if (!matchPlayer.isDisconnected()) {
					matchPlayer.setAlive(true);
				}

				final Player player = matchPlayer.toPlayer();

				if (player == null || !player.isOnline()) {
					continue;
				}

				player.teleport(this.arena.getSpawn1());

				for (Player member : teamA.getPlayers()) {
					NameTagHandler.addToTeam(player, member, ChatColor.GREEN, this.ladder.isBuild());
				}

				for (Player enemy : teamB.getPlayers()) {
					NameTagHandler.addToTeam(player, enemy, ChatColor.RED, this.ladder.isBuild());
				}
			}

			for (MatchPlayer matchPlayer : teamB.getTeamPlayers()) {
				if (!matchPlayer.isDisconnected()) {
					matchPlayer.setAlive(true);
				}

				final Player player = matchPlayer.toPlayer();

				if (player == null || !player.isOnline()) {
					continue;
				}

				player.teleport(this.arena.getSpawn2());

				for (Player member : teamB.getPlayers()) {
					NameTagHandler.addToTeam(player, member, ChatColor.GREEN, this.ladder.isBuild());
				}

				for (Player enemy : teamA.getPlayers()) {
					NameTagHandler.addToTeam(player, enemy, ChatColor.RED, this.ladder.isBuild());
				}
			}

			final List<Player> players = this.getPlayers();

			for (Player first : players) {
				PlayerUtil.reset(first);

				first.setMaximumNoDamageTicks(this.ladder.getHitDelay());

				if (this.ladder.isSumo()) {
					FairFight.getInstance().getPlayerDataManager().getPlayerData(first).setAllowTeleport(true);

					PlayerUtil.denyMovement(first);
				} else {
					for (ItemStack itemStack : PraxiPlayer.getByUuid(first.getUniqueId()).getKitItems(this.ladder)) {
						first.getInventory().addItem(itemStack);
					}
				}

				if (this.ladder.getKbProfile() != null) {
					final KnockbackProfile profile =
							RageSpigot.INSTANCE.getConfig().getKbProfileByName(this.ladder.getKbProfile());

					if (profile != null) {
						first.setKnockbackProfile(profile);
					}
				}

				for (Player second : players) {
					if (first.getUniqueId().equals(second.getUniqueId())) {
						continue;
					}

					first.showPlayer(second);
					second.showPlayer(first);
				}
			}
		}
	}

	public void handleStart() {
		this.setupPlayers();

		this.state = MatchState.STARTING;
		this.startTimestamp = -1;
		this.arena.setActive(true);

		this.onStart();

		this.getPlayers().forEach(player -> {
			player.sendMessage(Style.YELLOW + "You are playing on arena " + Style.PINK + this.arena.getName() + Style.YELLOW + ".");
		});

		TaskUtil.runTimer(new MatchStartTask(this), 20L, 20L);
	}

	private void handleEnd() {
		this.state = MatchState.ENDING;

		this.onEnd();

		if (this.isSoloMatch()) {
			final Player playerA = this.getPlayerA();
			final Player playerB = this.getPlayerB();

			playerA.hidePlayer(playerB);
			playerB.hidePlayer(playerA);

			for (MatchPlayer matchPlayer : new MatchPlayer[]{ this.getMatchPlayerA(), this.getMatchPlayerB() }) {
				if (matchPlayer.isAlive()) {
					final Player player = matchPlayer.toPlayer();

					if (player != null) {
						player.setFireTicks(0);
						player.updateInventory();

						if (matchPlayer.isAlive()) {
							MatchSnapshot snapshot = new MatchSnapshot(matchPlayer);

							snapshot.setSwitchTo(this.getOpponentMatchPlayer(player));

							this.snapshots.add(snapshot);
						}
					}
				}
			}
		} else if (this.isTeamMatch()) {
			for (MatchPlayer firstMatchPlayer : this.getMatchPlayers()) {
				if (firstMatchPlayer.isDisconnected()) {
					continue;
				}

				final Player player = firstMatchPlayer.toPlayer();

				if (player != null) {
					for (MatchPlayer secondMatchPlayer : this.getMatchPlayers()) {
						if (secondMatchPlayer.isDisconnected()) {
							continue;
						}

						if (secondMatchPlayer.getUuid().equals(player.getUniqueId())) {
							continue;
						}

						final Player secondPlayer = secondMatchPlayer.toPlayer();

						if (secondPlayer == null) {
							continue;
						}

						player.hidePlayer(secondPlayer);
					}

					player.setFireTicks(0);
					player.updateInventory();

					if (firstMatchPlayer.isAlive()) {
						this.snapshots.add(new MatchSnapshot(firstMatchPlayer));
					}
				}
			}
		}

		this.getSpectators().forEach(this::removeSpectator);
		this.entities.forEach(Entity::remove);
		this.snapshots.forEach(matchInventory -> {
			matchInventory.setCreated(System.currentTimeMillis());
			MatchSnapshot.getCache().put(matchInventory.getMatchPlayer().getUuid(), matchInventory);
		});

		new MatchResetTask(this).runTask(Praxi.getInstance());

		TaskUtil.runLater(() -> {
			if (this.isSoloMatch()) {
				final UUID rematchKey = UUID.randomUUID();
				final Player playerA = this.getPlayerA();
				final Player playerB = this.getPlayerB();

				if (playerA != null && playerB != null) {
					NameTagHandler.removeFromTeams(playerA, playerB);
					NameTagHandler.removeFromTeams(playerB, playerA);
					NameTagHandler.removeHealthDisplay(playerA);
					NameTagHandler.removeHealthDisplay(playerB);
				}

				for (MatchPlayer matchPlayer : new MatchPlayer[]{ this.getMatchPlayerA(), this.getMatchPlayerB() }) {
					final Player player = matchPlayer.toPlayer();
					final Player opponent = this.getOpponentPlayer(player);

					if (player != null) {
						player.setKnockbackProfile(null);

						final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

						if (opponent != null) {
							praxiPlayer.setRematchData(
									new RematchData(rematchKey, player.getUniqueId(), opponent.getUniqueId(),
											this.getLadder(), this.getArena()
									));
						}

						praxiPlayer.setState(PlayerState.IN_LOBBY);
						praxiPlayer.setMatch(null);
						praxiPlayer.loadHotbar();

						PlayerUtil.spawn(player);
					}
				}
			} else if (this.isTeamMatch()) {
				this.getPlayers().forEach(player -> {
					NameTagHandler.removeHealthDisplay(player);
					this.getPlayers().forEach(otherPlayer -> NameTagHandler.removeFromTeams(player, otherPlayer));
				});

				for (MatchPlayer matchPlayer : this.getMatchPlayers()) {
					if (matchPlayer.isDisconnected()) {
						continue;
					}

					final Player player = matchPlayer.toPlayer();

					if (player != null) {
						final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

						praxiPlayer.setState(PlayerState.IN_LOBBY);
						praxiPlayer.setMatch(null);
						praxiPlayer.loadHotbar();

						player.setKnockbackProfile(null);

						PlayerUtil.spawn(player);
					}
				}
			}
		}, 20L * 3);

		matches.remove(this);
	}

	public void handleRespawn(Player player) {
		player.spigot().respawn();
		player.setVelocity(new Vector());

		this.onRespawn(player);
	}

	public void handleDeath(Player deadPlayer, Player killerPlayer, boolean disconnected) {
		final MatchPlayer matchPlayer = this.getMatchPlayer(deadPlayer);

		if (!matchPlayer.isAlive()) {
			return;
		}

		matchPlayer.setAlive(false);
		matchPlayer.setDisconnected(disconnected);

		final List<Player> involvedPlayers = new ArrayList<>();

		if (this.isSoloMatch()) {
			involvedPlayers.add(this.getPlayerA());
			involvedPlayers.add(this.getPlayerB());
		} else {
			involvedPlayers.addAll(this.getPlayers());
		}

		involvedPlayers.addAll(this.getSpectators());

		EntityLightning is =
				new EntityLightning(((CraftWorld) deadPlayer.getWorld()).getHandle(), deadPlayer.getLocation().getX(),
						deadPlayer.getLocation().getY(), deadPlayer.getLocation().getZ()
				);
		PacketPlayOutSpawnEntityWeather lightningPacket = new PacketPlayOutSpawnEntityWeather(is);

		involvedPlayers.forEach(other -> {
			if (other != null) {
				other.playSound(deadPlayer.getLocation(), Sound.AMBIENCE_THUNDER, 1.0F, 1.0F);
				((CraftPlayer) other).getHandle().playerConnection.sendPacket(lightningPacket);
			}
		});

		for (Player involved : involvedPlayers) {
			String deadName = Style.RED + deadPlayer.getName();

			if (this.isSoloMatch()) {
				// Todo: fix NPE here, idk where but it says the line right below...
				// DEBUG FOR NOW:

				if (deadPlayer == null) {
					System.out.println("DEBUG: DEAD PLAYER NULL");
					continue;
				}

				if (involved == null) {
					System.out.println("DEBUG: INVOLVED PLAYER NULL");
					continue;
				}

				if (deadPlayer.getUniqueId().equals(involved.getUniqueId())) {
					deadName = Style.GREEN + deadPlayer.getName();
				}
			} else {
				final MatchTeam matchTeam = this.getTeam(involved);

				if (matchTeam != null && matchTeam.containsPlayer(deadPlayer)) {
					deadName = Style.GREEN + deadPlayer.getName();
				}
			}

			if (matchPlayer.isDisconnected()) {
				involved.sendMessage(deadName + Style.YELLOW + " has disconnected.");
				continue;
			}

			String killerName = null;

			if (killerPlayer != null) {
				killerName = Style.RED + killerPlayer.getName();

				if (this.isSoloMatch()) {
					if (killerPlayer.getUniqueId().equals(involved.getUniqueId())) {
						killerName = Style.GREEN + killerPlayer.getName();
					}
				} else {
					final MatchTeam matchTeam = this.getTeam(involved);

					if (matchTeam != null && matchTeam.containsPlayer(killerPlayer)) {
						killerName = Style.GREEN + killerPlayer.getName();
					}
				}
			}

			if (killerName == null) {
				involved.sendMessage(deadName + Style.YELLOW + " has died.");
			} else {
				involved.sendMessage(deadName + Style.YELLOW + " was killed by " + killerName + Style.YELLOW + ".");
			}
		}

		this.onDeath(deadPlayer, killerPlayer);

		if (this.canEnd()) {
			this.handleEnd();
		}
	}

	public String getDuration() {
		if (this.isStarting()) {
			return "00:00";
		} else if (this.isEnding()) {
			return "Ending";
		} else {
			return TimeUtil.millisToTimer(this.getElapsedDuration());
		}
	}

	public long getElapsedDuration() {
		return System.currentTimeMillis() - this.startTimestamp;
	}

	public void broadcastMessage(String message) {
		this.getPlayers().forEach(player -> player.sendMessage(message));
		this.getSpectators().forEach(player -> player.sendMessage(message));
	}

	public void broadcastSound(Sound sound) {
		this.getPlayers().forEach(player -> player.playSound(player.getLocation(), sound, 1.0F, 1.0F));
		this.getSpectators().forEach(player -> player.playSound(player.getLocation(), sound, 1.0F, 1.0F));
	}

	public List<UUID> getInvolvedPlayers() {
		List<UUID> toReturn = new ArrayList<>();

		toReturn.addAll(this.spectators);

		if (this.isSoloMatch()) {
			toReturn.add(this.getMatchPlayerA().getUuid());
			toReturn.add(this.getMatchPlayerB().getUuid());
		} else if (this.isTeamMatch()) {
			this.getMatchPlayers().forEach(matchPlayer -> toReturn.add(matchPlayer.getUuid()));
		}

		return toReturn;
	}

	protected List<Player> getSpectators() {
		return PlayerUtil.convertUUIDListToPlayerList(this.spectators);
	}

	public void addSpectator(Player player, Player target) {
		this.spectators.add(player.getUniqueId());

		if (this.isSoloMatch()) {
			final Player playerA = this.getPlayerA();
			final Player playerB = this.getPlayerB();

			if (playerA != null) {
				player.showPlayer(playerA);

				NameTagHandler.addToTeam(player, playerA, ChatColor.AQUA, this.ladder.isBuild());
			}

			if (playerB != null) {
				player.showPlayer(playerB);

				NameTagHandler.addToTeam(player, playerB, ChatColor.LIGHT_PURPLE, this.ladder.isBuild());
			}
		} else if (this.isTeamMatch()) {
			this.getTeamA().getPlayers().forEach(teamPlayer -> {
				player.showPlayer(teamPlayer);
				teamPlayer.hidePlayer(player);
				NameTagHandler.addToTeam(player, teamPlayer, ChatColor.AQUA, this.ladder.isBuild());
			});

			this.getTeamB().getPlayers().forEach(teamPlayer -> {
				player.showPlayer(teamPlayer);
				teamPlayer.hidePlayer(player);
				NameTagHandler.addToTeam(player, teamPlayer, ChatColor.LIGHT_PURPLE, this.ladder.isBuild());
			});
		}

		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

		praxiPlayer.setMatch(this);
		praxiPlayer.setState(PlayerState.SPECTATE_MATCH);
		praxiPlayer.loadHotbar();

		player.setAllowFlight(true);
		player.setFlying(true);
		player.updateInventory();
		player.teleport(target.getLocation().clone().add(0, 2, 0));
		player.sendMessage(Style.YELLOW + "You are spectating " + Style.PINK + target.getName() + Style.YELLOW + ".");

		if (this.isSoloMatch()) {
			for (Player matchPlayer : new Player[]{ this.getPlayerA(), this.getPlayerB() }) {
				if (!player.hasPermission("praxi.spectate.hidden")) {
					matchPlayer.sendMessage(Style.PINK + player.getName() + Style.YELLOW + " is now spectating.");

				} else if (matchPlayer.hasPermission("praxi.spectate.hidden")) {
					matchPlayer.sendMessage(Style.GRAY + "[Silent] " + Style.PINK + player.getName() + Style.YELLOW +
					                        " is now spectating.");
				}
			}
		} else if (this.isTeamMatch()) {
			for (Player matchPlayer : this.getPlayers()) {
				if (!player.hasPermission("praxi.spectate.hidden")) {
					matchPlayer.sendMessage(Style.PINK + player.getName() + Style.YELLOW + " is now spectating.");

				} else if (matchPlayer.hasPermission("praxi.spectate.hidden")) {
					matchPlayer.sendMessage(Style.GRAY + "[Silent] " + Style.PINK + player.getName() + Style.YELLOW +
					                        " is now spectating.");
				}
			}
		}
	}

	public void removeSpectator(Player player) {
		this.spectators.remove(player.getUniqueId());

		if (this.isSoloMatch()) {
			player.hidePlayer(this.getPlayerA());
			player.hidePlayer(this.getPlayerB());

			NameTagHandler.removeFromTeams(player, this.getPlayerA());
			NameTagHandler.removeFromTeams(player, this.getPlayerB());
			NameTagHandler.removeHealthDisplay(player);
		} else if (this.isTeamMatch()) {
			this.getPlayers().forEach(other -> {
				player.hidePlayer(other);
				other.hidePlayer(player);
				NameTagHandler.removeFromTeams(player, other);
			});
		}

		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

		if (this.state != MatchState.ENDING) {
			final String toSend = Style.PINK + player.getName() + Style.YELLOW + " is no longer spectating your match.";

			if (this.isSoloMatch()) {
				for (Player matchPlayer : new Player[]{ this.getPlayerA(), this.getPlayerB() }) {
					if (!player.hasPermission("praxi.spectate.hidden")) {
						matchPlayer.sendMessage(toSend);
					} else if (matchPlayer.hasPermission("praxi.spectate.hidden")) {
						matchPlayer.sendMessage(Style.GRAY + "[Silent] " + toSend);
					}
				}
			} else if (this.isTeamMatch()) {
				for (Player matchPlayer : this.getPlayers()) {
					if (!player.hasPermission("praxi.spectate.hidden")) {
						matchPlayer.sendMessage(toSend);
					} else if (matchPlayer.hasPermission("praxi.spectate.hidden")) {
						matchPlayer.sendMessage(Style.GRAY + "[Silent] " + toSend);
					}
				}
			}
		}

		praxiPlayer.setState(PlayerState.IN_LOBBY);
		praxiPlayer.setMatch(null);
		praxiPlayer.loadHotbar();

		PlayerUtil.spawn(player);
	}

	public abstract boolean isDuel();

	public abstract boolean isSoloMatch();

	public abstract boolean isTeamMatch();

	public abstract void onStart();

	public abstract void onEnd();

	public abstract void onDeath(Player player, Player killer);

	public abstract void onRespawn(Player player);

	public abstract boolean canEnd();

	public abstract Player getWinningPlayer();

	public abstract MatchTeam getWinningTeam();

	public abstract MatchPlayer getMatchPlayerA();

	public abstract MatchPlayer getMatchPlayerB();

	public abstract List<MatchPlayer> getMatchPlayers();

	public abstract Player getPlayerA();

	public abstract Player getPlayerB();

	public abstract List<Player> getPlayers();

	public abstract MatchTeam getTeamA();

	public abstract MatchTeam getTeamB();

	public abstract MatchTeam getTeam(MatchPlayer matchPlayer);

	public abstract MatchTeam getTeam(Player player);

	public abstract MatchPlayer getMatchPlayer(Player player);

	public abstract int getOpponentsLeft(Player player);

	public abstract MatchTeam getOpponentTeam(MatchTeam matchTeam);

	public abstract MatchTeam getOpponentTeam(Player player);

	public abstract MatchPlayer getOpponentMatchPlayer(Player player);

	public abstract Player getOpponentPlayer(Player player);

	public abstract int getTotalRoundWins();

	public abstract int getRoundWins(MatchPlayer matchPlayer);

	public abstract int getRoundWins(MatchTeam matchTeam);

	public abstract int getRoundsNeeded(MatchPlayer matchPlayer);

	public abstract int getRoundsNeeded(MatchTeam matchTeam);

}
