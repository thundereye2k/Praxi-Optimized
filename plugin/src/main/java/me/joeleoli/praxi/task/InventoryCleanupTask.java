package me.joeleoli.praxi.task;

import me.joeleoli.praxi.match.MatchSnapshot;

public class InventoryCleanupTask implements Runnable {

	@Override
	public void run() {
		MatchSnapshot.getCache().entrySet()
		             .removeIf(entry -> System.currentTimeMillis() - entry.getValue().getCreated() >= 45_000);
	}

}
