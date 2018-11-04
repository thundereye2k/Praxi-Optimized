package me.joeleoli.praxi.party;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import me.joeleoli.nucleus.chat.ChatComponentBuilder;
import me.joeleoli.nucleus.team.Team;
import me.joeleoli.nucleus.team.TeamPlayer;
import me.joeleoli.nucleus.util.ObjectUtil;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.player.PraxiPlayer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.entity.Player;

@Getter
public class Party extends Team<TeamPlayer> {

	@Getter
	private static List<Party> parties = new ArrayList<>();

	private PartyState state = PartyState.CLOSED;
	private Map<UUID, Long> invited;
	@Setter
	private PartyEvent selectedEvent;

	public Party(Player player) {
		super(new TeamPlayer(player.getUniqueId(), player.getName()));

		this.invited = new HashMap<>();

		parties.add(this);
	}

	public void setState(PartyState state) {
		this.state = state;

		this.broadcast(Style.YELLOW + "The party state has been changed to: " + Style.RESET + this.state.name());
	}

	public boolean canInvite(Player player) {
		for (UUID uuid : this.invited.keySet()) {
			if (uuid.equals(player.getUniqueId())) {
				if (System.currentTimeMillis() - this.invited.get(uuid) >= 30_000) {
					this.invited.remove(uuid);
					return true;
				}

				return false;
			}
		}

		return true;
	}

	public boolean isInvited(Player player) {
		for (UUID uuid : this.invited.keySet()) {
			if (uuid.equals(player.getUniqueId())) {
				if (System.currentTimeMillis() - this.invited.get(uuid) >= 30_000) {
					this.invited.remove(uuid);
					return false;
				}

				return true;
			}
		}

		return false;
	}

	public void invite(Player target) {
		this.invited.put(target.getUniqueId(), System.currentTimeMillis());

		final HoverEvent hoverEvent = new HoverEvent(
				HoverEvent.Action.SHOW_TEXT,
				new ChatComponentBuilder(Style.YELLOW + "Click to join the party.").create()
		);
		final ClickEvent clickEvent =
				new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party join " + this.getLeader().getName());

		this.broadcast(Style.RESET + target.getDisplayName() + " " + Style.YELLOW + "has been invited to the party.");

		target.sendMessage(
				Style.YELLOW + "You have been invited to join " + Style.RESET + this.getLeader().getDisplayName() +
				Style.YELLOW + "'s party.");
		target.sendMessage(new ChatComponentBuilder("").parse(Style.GOLD + "Click here to join the party.")
		                                               .attachToEachPart(clickEvent).attachToEachPart(hoverEvent)
		                                               .create());
	}

	public void join(Player player) {
		this.getTeamPlayers().add(new TeamPlayer(player.getUniqueId(), player.getName()));
		this.invited.keySet().removeIf(uuid -> uuid.equals(player.getUniqueId()));
		this.broadcast(Style.RESET + player.getDisplayName() + Style.YELLOW + " has joined the party.");

		PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

		praxiPlayer.setParty(this);
		praxiPlayer.loadHotbar();
	}

	public void leave(Player player, boolean kick) {
		this.broadcast(
				Style.RESET + player.getDisplayName() + Style.YELLOW + " has " + (kick ? "been kicked" : "left") +
				" the party.");
		this.getTeamPlayers().removeIf(playerInfo -> playerInfo.getUuid().equals(player.getUniqueId()));

		PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

		praxiPlayer.setParty(null);
		praxiPlayer.loadHotbar();
	}

	public void disband() {
		parties.remove(this);

		this.broadcast(Style.YELLOW + "The party has been disbanded.");

		this.getPlayers().forEach(player -> {
			PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

			praxiPlayer.setParty(null);

			if (praxiPlayer.isInLobby()) {
				praxiPlayer.loadHotbar();
			}
		});
	}

	public void sendInformation(Player player) {
		StringBuilder builder = new StringBuilder();

		for (Player member : this.getPlayers()) {
			builder.append(member.getName());
			builder.append(", ");
		}

		final String[] lines = new String[]{
				Style.getBorderLine(),
				Style.GOLD + "Party of " + this.getLeader().getName(),
				Style.YELLOW + "State: " + Style.GRAY + ObjectUtil.toReadable(this.state),
				Style.YELLOW + "Members: " + Style.GRAY +
				builder.toString().substring(0, builder.toString().length() - 2),
				Style.getBorderLine()
		};

		player.sendMessage(lines);
	}

}
