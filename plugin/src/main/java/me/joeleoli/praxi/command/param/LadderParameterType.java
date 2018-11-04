package me.joeleoli.praxi.command.param;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import me.joeleoli.nucleus.command.param.ParameterType;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.ladder.Ladder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LadderParameterType implements ParameterType<Ladder> {

	public Ladder transform(CommandSender sender, String source) {
		Ladder ladder = Ladder.getByName(source);

		if (ladder == null) {
			sender.sendMessage(Style.RED + "That is not a valid ladder type.");
			return null;
		}

		return ladder;
	}

	public List<String> tabComplete(Player sender, Set<String> flags, String source) {
		List<String> completions = new ArrayList<>();

		for (Ladder ladder : Ladder.getLadders()) {
			completions.add(ladder.getName());
		}

		return completions;
	}

}