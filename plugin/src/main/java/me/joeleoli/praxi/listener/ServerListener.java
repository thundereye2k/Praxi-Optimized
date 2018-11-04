package me.joeleoli.praxi.listener;

import me.joeleoli.nucleus.Nucleus;
import me.joeleoli.nucleus.event.PreShutdownEvent;
import me.joeleoli.praxi.match.Match;
import me.joeleoli.praxi.player.PraxiPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ServerListener implements Listener {

	@EventHandler
	public void onPreShutdown(PreShutdownEvent event) {
		Nucleus.getInstance().getServer().getScheduler().runTaskAsynchronously(Nucleus.getInstance(), () -> {
			for (Player player : Nucleus.getInstance().getServer().getOnlinePlayers()) {
				PraxiPlayer.getByUuid(player.getUniqueId()).save();
			}
		});

		for (Match match : Match.getMatches()) {
			match.getPlacedBlocks().forEach(location -> location.getBlock().setType(Material.AIR));
			match.getChangedBlocks()
			     .forEach((blockState) -> blockState.getLocation().getBlock().setType(blockState.getType()));
			match.getEntities().forEach(Entity::remove);
		}
	}

}
