package me.joeleoli.praxi.listener;

import me.joeleoli.nucleus.Nucleus;
import me.joeleoli.nucleus.util.BukkitUtil;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.events.Event;
import me.joeleoli.praxi.match.Match;
import me.joeleoli.praxi.match.MatchTeam;
import me.joeleoli.praxi.player.PraxiPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class EntityListener implements Listener {

	@EventHandler
	public void onEntityRegainHealth(EntityRegainHealthEvent event) {
		if (event.getEntity() instanceof Player) {
			if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) {
				final Player player = (Player) event.getEntity();
				final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

				if (praxiPlayer.isInMatch()) {
					if (!praxiPlayer.getMatch().getLadder().isRegeneration()) {
						event.setCancelled(true);
					}
				} else {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			final Player player = (Player) event.getEntity();
			final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

			if (praxiPlayer.isInMatch()) {
				if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
					praxiPlayer.getMatch().handleDeath(player, null, false);
					return;
				}

				if (!praxiPlayer.getMatch().isFighting()) {
					event.setCancelled(true);
					return;
				}

				if (praxiPlayer.getMatch().isTeamMatch()) {
					if (!praxiPlayer.getMatch().getMatchPlayer(player).isAlive()) {
						event.setCancelled(true);
						return;
					}
				}

				if (praxiPlayer.getMatch().getLadder().isSumo() || praxiPlayer.getMatch().getLadder().isSpleef()) {
					event.setDamage(0);
					player.setHealth(20.0);
					player.updateInventory();
				}
			} else if (praxiPlayer.isInEvent()) {
				if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
					praxiPlayer.getEvent().handleDeath(player);
					return;
				}

				if (praxiPlayer.getEvent().isSumo()) {
					if (!praxiPlayer.getEvent().isFighting() || !praxiPlayer.getEvent().isFighting(player.getUniqueId())) {
						event.setCancelled(true);
						return;
					}

					event.setDamage(0);
					player.setHealth(20.0);
					player.updateInventory();
				}
			} else {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		final Player attacker = BukkitUtil.getDamager(event);

		if (attacker != null && event.getEntity() instanceof Player) {
			final Player damaged = (Player) event.getEntity();
			final PraxiPlayer damagedData = PraxiPlayer.getByUuid(damaged.getUniqueId());
			final PraxiPlayer attackerData = PraxiPlayer.getByUuid(attacker.getUniqueId());

			if (attackerData.isSpectating() || damagedData.isSpectating()) {
				event.setCancelled(true);
				return;
			}

			if (damagedData.isInMatch() && attackerData.isInMatch()) {
				final Match match = attackerData.getMatch();


				if (!damagedData.getMatch().getMatchId().equals(attackerData.getMatch().getMatchId())) {
					event.setCancelled(true);
					return;
				}

				if (!match.getMatchPlayer(damaged).isAlive() || !match.getMatchPlayer(attacker).isAlive()) {
					event.setCancelled(true);
					return;
				}

				if (match.isSoloMatch()) {
					attackerData.getMatch().getMatchPlayer(attacker).handleHit();
					damagedData.getMatch().getMatchPlayer(damaged).resetCombo();

					if (event.getDamager() instanceof Arrow) {
						double health = Math.ceil(damaged.getHealth() - event.getFinalDamage()) / 2.0D;

						attacker.sendMessage(Style.formatArrowHitMessage(damaged.getName(), health));
					}
				} else if (match.isTeamMatch()) {
					final MatchTeam attackerTeam = match.getTeam(attacker);
					final MatchTeam damagedTeam = match.getTeam(damaged);

					if (attackerTeam == null || damagedTeam == null) {
						event.setCancelled(true);
					} else {
						if (attackerTeam.equals(damagedTeam)) {
							event.setCancelled(true);
						} else {
							attackerData.getMatch().getMatchPlayer(attacker).handleHit();
							damagedData.getMatch().getMatchPlayer(damaged).resetCombo();

							if (event.getDamager() instanceof Arrow) {
								double health = Math.ceil(damaged.getHealth() - event.getFinalDamage()) / 2.0D;

								attacker.sendMessage(Style.formatArrowHitMessage(damaged.getName(), health));
							}
						}
					}
				}
			} else if (damagedData.isInEvent() && attackerData.isInEvent()) {
				final Event praxiEvent = damagedData.getEvent();

				if (!praxiEvent.isFighting() || !praxiEvent.isFighting(damaged.getUniqueId()) || !praxiEvent.isFighting(attacker.getUniqueId())) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player) {
			final Player player = (Player) event.getEntity();
			final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

			if (praxiPlayer.isInMatch() && praxiPlayer.getMatch().isFighting()) {
				if (event.getFoodLevel() >= 20) {
					event.setFoodLevel(20);
					player.setSaturation(20);
				} else {
					event.setCancelled(Nucleus.RANDOM.nextInt(100) > 25);
				}
			} else {
				event.setCancelled(true);
			}
		}
	}

}
