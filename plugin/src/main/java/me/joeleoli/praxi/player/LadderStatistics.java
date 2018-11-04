package me.joeleoli.praxi.player;

import lombok.Data;

@Data
public class LadderStatistics {

	private int elo = 1000;
	private int won, lost;

	public void incrementWon() {
		this.won++;
	}

	public void incrementLost() {
		this.lost++;
	}

}
