package me.joeleoli.praxi.task;

import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.player.PraxiPlayer;
import org.bukkit.entity.Player;

public class RematchExpireRunnable implements Runnable {

	@Override
	public void run() {
		for (Player player : Praxi.getInstance().getServer().getOnlinePlayers()) {
			final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

			praxiPlayer.refreshHotbar();
		}
	}

}
