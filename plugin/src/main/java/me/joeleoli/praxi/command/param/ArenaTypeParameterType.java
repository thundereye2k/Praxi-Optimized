package me.joeleoli.praxi.command.param;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import me.joeleoli.nucleus.command.param.ParameterType;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.arena.ArenaType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaTypeParameterType implements ParameterType<ArenaType> {

	public ArenaType transform(CommandSender sender, String source) {
		ArenaType type;

		try {
			type = ArenaType.valueOf(source);
		} catch (Exception e) {
			sender.sendMessage(Style.RED + "That is not a valid arena type.");
			return null;
		}

		return type;
	}

	public List<String> tabComplete(Player sender, Set<String> flags, String source) {
		List<String> completions = new ArrayList<>();

		for (ArenaType type : ArenaType.values()) {
			completions.add(type.name());
		}

		return completions;
	}

}