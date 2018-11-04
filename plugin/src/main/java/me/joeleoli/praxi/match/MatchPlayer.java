package me.joeleoli.praxi.match;

import lombok.Getter;
import lombok.Setter;
import me.joeleoli.nucleus.team.TeamPlayer;
import org.bukkit.entity.Player;

@Getter
@Setter
public class MatchPlayer extends TeamPlayer {

	private boolean alive = true;
	private boolean disconnected;
	private int elo, potionsThrown, potionsMissed, hits, combo, longestCombo;

	public MatchPlayer(Player player) {
		super(player.getUniqueId(), player.getName());
	}

	public double getPotionAccuracy() {
		if (this.potionsMissed == 0) {
			return 100.0;
		} else if (this.potionsThrown == this.potionsMissed) {
			return 50.0;
		}

		return Math.round(100.0D - (((double) this.potionsMissed / (double) this.potionsThrown) * 100.0D));
	}

	public void incrementPotionsThrown() {
		this.potionsThrown++;
	}

	public void incrementPotionsMissed() {
		this.potionsMissed++;
	}

	public void handleHit() {
		this.hits++;
		this.combo++;

		if (this.combo > this.longestCombo) {
			this.longestCombo = this.combo;
		}
	}

	public void resetCombo() {
		this.combo = 0;
	}

}
