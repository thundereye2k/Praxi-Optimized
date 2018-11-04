package me.joeleoli.praxi.match;

import me.joeleoli.nucleus.team.Team;

public class MatchTeam extends Team<MatchPlayer> {

	public MatchTeam(MatchPlayer matchPlayer) {
		super(matchPlayer);
	}

	public int getDisconnectedCount() {
		int disconnected = 0;

		for (MatchPlayer matchPlayer : this.getTeamPlayers()) {
			if (matchPlayer.isDisconnected()) {
				disconnected++;
			}
		}

		return disconnected;
	}

}
