package me.joeleoli.praxi.task;

import me.joeleoli.praxi.player.PraxiPlayer;

public class SaveDataTask implements Runnable {

	@Override
	public void run() {
		for (PraxiPlayer praxiPlayer : PraxiPlayer.getPlayers().values()) {
			praxiPlayer.save();
		}
	}

}
