package me.joeleoli.praxi.duel;

import java.text.MessageFormat;
import lombok.Data;
import me.joeleoli.nucleus.chat.ChatComponentBuilder;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.ladder.Ladder;
import me.joeleoli.praxi.player.PraxiPlayer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;

@Data
public class DuelProcedure {

	private static final HoverEvent ACCEPT_HOVER = new HoverEvent(
			HoverEvent.Action.SHOW_TEXT,
			new ChatComponentBuilder(Style.YELLOW + "Click to accept this duel invite.").create()
	);

	private Player sender;
	private Player target;
	private Ladder ladder;
	private Arena arena;

	public void send() {
		if (!this.sender.isOnline() || !this.target.isOnline()) {
			return;
		}

		final DuelRequest request = new DuelRequest(this.sender.getUniqueId());

		request.setLadder(this.ladder);
		request.setArena(this.arena);

		final PraxiPlayer senderData = PraxiPlayer.getByUuid(this.sender.getUniqueId());

		senderData.setDuelProcedure(null);
		senderData.getSentDuelRequests().put(this.target.getUniqueId(), request);

		final ClickEvent click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duel accept " + this.sender.getName());
		final String ladderContext = StringUtils.startsWithIgnoreCase(this.ladder.getName(), "u") ? "an " : "a ";

		this.sender.sendMessage(Style.translate(new MessageFormat("&eYou sent a duel request to &d{0} &eon arena &d{1}&e.")
				.format(new Object[]{ this.target.getName(), this.arena.getName() })));
		this.target.sendMessage(Style.translate(
				new MessageFormat("&d{0} &esent you {1} &d{2} &eduel request on arena &d{3}&e.").format(new Object[]{
						this.sender.getName(), ladderContext, this.ladder.getName(), this.arena.getName()
				})));
		this.target.sendMessage(new ChatComponentBuilder("")
				.parse("&6Click here or type &b/duel accept " + this.sender.getName() + " &6to accept the invite.")
				.attachToEachPart(click).attachToEachPart(ACCEPT_HOVER).create());
	}

}
