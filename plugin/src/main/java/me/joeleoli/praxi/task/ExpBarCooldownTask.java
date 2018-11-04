package me.joeleoli.praxi.task;

import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.player.PraxiPlayer;
import org.bukkit.entity.Player;

public class ExpBarCooldownTask implements Runnable {

	@Override
	public void run() {
		for (Player player : Praxi.getInstance().getServer().getOnlinePlayers()) {
			final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

			if ((praxiPlayer.isInMatch() || praxiPlayer.isInEvent()) && !praxiPlayer.getEnderpearlCooldown().hasExpired()) {
				int seconds = Math.round(praxiPlayer.getEnderpearlCooldown().getRemaining()) / 1_000;

				player.setLevel(seconds);
				player.setExp(praxiPlayer.getEnderpearlCooldown().getRemaining() / 16_000.0F);
			} else {
				if (!praxiPlayer.getEnderpearlCooldown().isNotified()) {
					player.sendMessage(Style.PINK + "Your pearl cooldown has expired.");
					praxiPlayer.getEnderpearlCooldown().setNotified(true);
				}

				if (player.getLevel() > 0) {
					player.setLevel(0);
				}

				if (player.getExp() > 0.0F) {
					player.setExp(0.0F);
				}
			}
		}
	}

}
