package me.joeleoli.praxi.listener;

import me.joeleoli.praxi.player.PraxiPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class ProjectileListener implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		if (event.getEntity() instanceof ThrownPotion) {
			if (event.getEntity().getShooter() instanceof Player) {
				final Player shooter = (Player) event.getEntity().getShooter();
				final PraxiPlayer shooterData = PraxiPlayer.getByUuid(shooter.getUniqueId());

				if (shooterData.isInMatch() && shooterData.getMatch().isFighting()) {
					shooterData.getMatch().getMatchPlayer(shooter).incrementPotionsThrown();
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onProjectileHit(ProjectileHitEvent event) {
		if (event.getEntity() instanceof Arrow) {
			if (event.getEntity().getShooter() instanceof Player) {
				final Player shooter = (Player) event.getEntity().getShooter();
				final PraxiPlayer shooterData = PraxiPlayer.getByUuid(shooter.getUniqueId());

				if (shooterData.isInMatch()) {
					shooterData.getMatch().getEntities().add(event.getEntity());
					shooterData.getMatch().getMatchPlayer(shooter).handleHit();
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPotionSplash(PotionSplashEvent event) {
		if (event.getPotion().getShooter() instanceof Player) {
			final Player shooter = (Player) event.getPotion().getShooter();
			final PraxiPlayer shooterData = PraxiPlayer.getByUuid(shooter.getUniqueId());

			if (shooterData.isInMatch() && shooterData.getMatch().isFighting()) {
				if (event.getIntensity(shooter) <= 0.5D) {
					shooterData.getMatch().getMatchPlayer(shooter).incrementPotionsMissed();
				}
			}
		}
	}

}
