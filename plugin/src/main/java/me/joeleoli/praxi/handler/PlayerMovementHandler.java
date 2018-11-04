package me.joeleoli.praxi.handler;

import me.joeleoli.nucleus.util.PlayerUtil;
import me.joeleoli.praxi.events.Event;
import me.joeleoli.praxi.events.EventState;
import me.joeleoli.praxi.match.Match;
import me.joeleoli.praxi.player.PraxiPlayer;
import me.joeleoli.ragespigot.handler.MovementHandler;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PlayerMovementHandler implements MovementHandler {

	@Override
	public void handleUpdateLocation(Player player, Location from, Location to, PacketPlayInFlying packetPlayInFlying) {
		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

		if (praxiPlayer.isInLobby() || praxiPlayer.isInQueue()) {
			if (player.getGameMode() != GameMode.CREATIVE) {
				if (to.getX() >= 200 || to.getX() <= -200 || to.getZ() >= 200 || to.getZ() <= -200) {
					PlayerUtil.spawn(player);
				}
			}
		} else if (praxiPlayer.isInMatch()) {
			if (praxiPlayer.getMatch().getLadder().isSumo() || praxiPlayer.getMatch().getLadder().isSpleef()) {
				final Match match = praxiPlayer.getMatch();

				if (match.isFighting()) {
					if (player.getLocation().getBlock().getType() == Material.WATER ||
					    player.getLocation().getBlock().getType() == Material.STATIONARY_WATER) {
						Player killer = player.getKiller();

						if (killer == null) {
							if (match.isSoloMatch()) {
								killer = praxiPlayer.getMatch().getOpponentPlayer(player);
							}
						}

						match.handleDeath(player, killer, false);
					}
				}
			}
		} else if (praxiPlayer.isInEvent()) {
			final Event event = praxiPlayer.getEvent();

			if (event.isSumo()) {
				if (event.getState() == EventState.ROUND_FIGHTING) {
					if (event.isFighting(player.getUniqueId())) {
						if (player.getLocation().getBlock().getType() == Material.WATER ||
						    player.getLocation().getBlock().getType() == Material.STATIONARY_WATER) {
							event.handleDeath(player);
						}
					}
				}
			}
		}
	}

	@Override
	public void handleUpdateRotation(Player player, Location from, Location to, PacketPlayInFlying packetPlayInFlying) {

	}

}
