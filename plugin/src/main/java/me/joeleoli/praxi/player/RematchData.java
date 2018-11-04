package me.joeleoli.praxi.player;

import java.util.UUID;
import lombok.Getter;
import me.joeleoli.nucleus.NucleusAPI;
import me.joeleoli.nucleus.chat.ChatComponentBuilder;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.ladder.Ladder;
import me.joeleoli.praxi.match.Match;
import me.joeleoli.praxi.match.MatchPlayer;
import me.joeleoli.praxi.match.impl.SoloMatch;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.entity.Player;

@Getter
public class RematchData {

	private static final HoverEvent HOVER_EVENT = new HoverEvent(
			HoverEvent.Action.SHOW_TEXT,
			new ChatComponentBuilder(Style.YELLOW + "Click to accept this rematch invite.").create()
	);
	private static final ClickEvent CLICK_EVENT = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rematch");

	private UUID key;
	private UUID sender;
	private UUID target;
	private Ladder ladder;
	private Arena arena;
	private boolean sent;
	private boolean receive;
	private long timestamp = System.currentTimeMillis();

	public RematchData(UUID key, UUID sender, UUID target, Ladder ladder, Arena arena) {
		this.key = key;
		this.sender = sender;
		this.target = target;
		this.ladder = ladder;
		this.arena = arena;
	}

	public void request() {

		final Player sender = Praxi.getInstance().getServer().getPlayer(this.sender);
		final Player target = Praxi.getInstance().getServer().getPlayer(this.target);

		if (sender == null || target == null) {
			return;
		}

		final PraxiPlayer senderPraxiPlayer = PraxiPlayer.getByUuid(sender.getUniqueId());
		final PraxiPlayer targetPraxiPlayer = PraxiPlayer.getByUuid(target.getUniqueId());

		if (senderPraxiPlayer.getRematchData() == null || targetPraxiPlayer.getRematchData() == null ||
		    !senderPraxiPlayer.getRematchData().getKey().equals(targetPraxiPlayer.getRematchData().getKey())) {
			return;
		}

		if (senderPraxiPlayer.isBusy()) {
			sender.sendMessage(Style.RED + "You cannot duel right now.");
			return;
		}

		sender.sendMessage(Style.translate(
				"&eYou sent a rematch request to &d" + target.getName() + " &eon arena &d" + this.arena.getName() +
				"&e."));
		target.sendMessage(Style.translate(
				"&d" + sender.getName() + " &ehas sent you a rematch request on arena &d" + this.arena.getName() +
				"&e."));
		target.sendMessage(new ChatComponentBuilder("").parse("&6Click here or type &b/rematch &6to accept the invite.")
		                                               .attachToEachPart(HOVER_EVENT).attachToEachPart(CLICK_EVENT)
		                                               .create());

		this.sent = true;
		targetPraxiPlayer.getRematchData().receive = true;

		senderPraxiPlayer.refreshHotbar();
		targetPraxiPlayer.refreshHotbar();
	}

	public void accept() {
		final Player sender = Praxi.getInstance().getServer().getPlayer(this.sender);
		final Player target = Praxi.getInstance().getServer().getPlayer(this.target);

		if (sender == null || target == null || !sender.isOnline() || !target.isOnline()) {
			return;
		}

		final PraxiPlayer senderPraxiPlayer = PraxiPlayer.getByUuid(sender.getUniqueId());
		final PraxiPlayer targetPraxiPlayer = PraxiPlayer.getByUuid(target.getUniqueId());

		if (senderPraxiPlayer.getRematchData() == null || targetPraxiPlayer.getRematchData() == null ||
		    !senderPraxiPlayer.getRematchData().getKey().equals(targetPraxiPlayer.getRematchData().getKey())) {
			return;
		}

		if (senderPraxiPlayer.isBusy()) {
			sender.sendMessage(Style.RED + "You cannot duel right now.");
			return;
		}

		if (targetPraxiPlayer.isBusy()) {
			sender.sendMessage(NucleusAPI.getColoredName(target) + Style.RED + " is currently busy.");
			return;
		}

		Arena arena = this.arena;

		if (arena.isActive()) {
			arena = Arena.getRandom(this.ladder);
		}

		if (arena == null) {
			sender.sendMessage(Style.RED + "Tried to start a match but there are no available arenas.");
			return;
		}

		arena.setActive(true);

		Match match = new SoloMatch(new MatchPlayer(sender), new MatchPlayer(target), this.ladder, arena, false, true);

		match.handleStart();
	}

}
