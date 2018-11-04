package me.joeleoli.praxi.queue;

import me.joeleoli.nucleus.NucleusAPI;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.nucleus.util.TaskUtil;
import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.match.Match;
import me.joeleoli.praxi.match.MatchPlayer;
import me.joeleoli.praxi.match.impl.SoloMatch;
import me.joeleoli.praxi.player.PracticeSetting;
import me.joeleoli.praxi.player.PraxiPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class QueueThread extends Thread {

	@Override
	public void run() {
		while (true) {
			try {
				for (Queue queue : Queue.getQueues()) {
					queue.getPlayers().forEach(QueuePlayer::tickRange);

					if (queue.getPlayers().size() < 2) {
						continue;
					}

					for (QueuePlayer firstQueuePlayer : queue.getPlayers()) {
						final Player firstPlayer = Bukkit.getPlayer(firstQueuePlayer.getPlayerUuid());

						if (firstPlayer == null) {
							continue;
						}

						final PraxiPlayer firstPraxiPlayer = PraxiPlayer.getByUuid(firstQueuePlayer.getPlayerUuid());

						for (QueuePlayer secondQueuePlayer : queue.getPlayers()) {
							if (firstQueuePlayer.equals(secondQueuePlayer)) {
								continue;
							}

							final Player secondPlayer = Bukkit.getPlayer(secondQueuePlayer.getPlayerUuid());
							final PraxiPlayer secondPraxiPlayer =
									PraxiPlayer.getByUuid(secondQueuePlayer.getPlayerUuid());

							if (secondPlayer == null) {
								continue;
							}

							if (NucleusAPI.<Boolean>getSetting(firstPlayer, PracticeSetting.PING_FACTOR) ||
							    NucleusAPI.<Boolean>getSetting(secondPlayer, PracticeSetting.PING_FACTOR)) {
								if (firstPlayer.getPing() >= secondPlayer.getPing()) {
									if (firstPlayer.getPing() - secondPlayer.getPing() >= 50) {
										continue;
									}
								} else {
									if (secondPlayer.getPing() - firstPlayer.getPing() >= 50) {
										continue;
									}
								}
							}

							if (queue.isRanked()) {
								if (!firstQueuePlayer.isInRange(secondQueuePlayer.getElo()) ||
								    !secondQueuePlayer.isInRange(firstQueuePlayer.getElo())) {
									continue;
								}
							}

							// Find arena
							final Arena arena = Arena.getRandom(queue.getLadder());

							if (arena == null) {
								continue;
							}

							// Update arena
							arena.setActive(true);

							// Remove players from queue
							queue.getPlayers().remove(firstQueuePlayer);
							queue.getPlayers().remove(secondQueuePlayer);

							final MatchPlayer firstMatchPlayer = new MatchPlayer(firstPlayer);
							final MatchPlayer secondMatchPlayer = new MatchPlayer(secondPlayer);

							if (queue.isRanked()) {
								firstMatchPlayer.setElo(firstPraxiPlayer.getStatistics().getElo(queue.getLadder()));
								secondMatchPlayer.setElo(secondPraxiPlayer.getStatistics().getElo(queue.getLadder()));
							}

							// Create match
							final Match match = new SoloMatch(queue.getUuid(), firstMatchPlayer, secondMatchPlayer,
									queue.getLadder(), arena, queue.isRanked(), false
							);

							final String[] opponentMessages =
									this.formatOpponentMessages(firstPlayer.getName(), secondPlayer.getName(),
											firstMatchPlayer.getElo(), secondMatchPlayer.getElo(), queue.isRanked()
									);

							firstPlayer.sendMessage(opponentMessages[0]);
							secondPlayer.sendMessage(opponentMessages[1]);

							TaskUtil.run(match::handleStart);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}

			try {
				Thread.sleep(200L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private String[] formatOpponentMessages(String player1, String player2, int player1Elo, int player2Elo,
			boolean ranked) {
		final String player1Format = player1 + (ranked ? Style.GRAY + " (" + player1Elo + ")" : "");
		final String player2Format = player2 + (ranked ? Style.GRAY + " (" + player2Elo + ")" : "");

		return new String[]{
				Style.YELLOW + Style.BOLD + "Found opponent: " + Style.GREEN + player1Format + Style.PINK + " vs. " +
				Style.RED + player2Format,
				Style.YELLOW + Style.BOLD + "Found opponent: " + Style.GREEN + player2Format + Style.PINK + " vs. " +
				Style.RED + player1Format
		};
	}

}
