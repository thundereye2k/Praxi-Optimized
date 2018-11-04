package me.joeleoli.praxi.command.param;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import me.joeleoli.nucleus.command.param.ParameterType;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.queue.Queue;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QueueParameterType implements ParameterType<Queue> {

	public Queue transform(CommandSender sender, String source) {
		try {
			Queue queue = Queue.getByUuid(UUID.fromString(source));

			if (queue == null) {
				sender.sendMessage(Style.RED + "A queue with that ID does not exist.");
				return null;
			}

			return queue;
		} catch (Exception e) {
			sender.sendMessage(Style.RED + "A queue with that ID does not exist.");
			return null;
		}
	}

	public List<String> tabComplete(Player sender, Set<String> flags, String source) {
		return Collections.emptyList();
	}

}