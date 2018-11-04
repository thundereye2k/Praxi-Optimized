package me.joeleoli.praxi.player;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import me.joeleoli.praxi.ladder.Ladder;

@Getter
public class PlayerStatistics {

	private Map<String, LadderStatistics> ladders;

	public PlayerStatistics() {
		this.ladders = new HashMap<>();

		for (Ladder ladder : Ladder.getLadders()) {
			this.ladders.put(ladder.getName(), new LadderStatistics());
		}
	}

	public int getElo(Ladder ladder) {
		if (!this.ladders.containsKey(ladder.getName())) {
			return 1000;
		}

		return this.ladders.get(ladder.getName()).getElo();
	}

	public LadderStatistics getLadderStatistics(Ladder ladder) {
		LadderStatistics ladderStatistics = this.ladders.get(ladder.getName());

		if (ladderStatistics == null) {
			ladderStatistics = new LadderStatistics();
			this.ladders.put(ladder.getName(), ladderStatistics);
		}

		return ladderStatistics;
	}

	public int getWins() {
		int wins = 0;

		for (LadderStatistics stats : this.ladders.values()) {
			wins += stats.getWon();
		}

		return wins;
	}

	public double getWinRatio() {
		int wins = 0;
		int losses = 0;

		for (LadderStatistics ladder : this.ladders.values()) {
			wins += ladder.getWon();
			losses += ladder.getLost();
		}

		if (losses == 0) {
			return 100.0;
		} else if (wins == 0 && losses > 0) {
			return 0.0;
		} else {
			return (wins / (wins + losses)) * 100;
		}
	}

}
