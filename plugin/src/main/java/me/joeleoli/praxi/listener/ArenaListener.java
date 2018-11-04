package me.joeleoli.praxi.listener;

import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.arena.ArenaType;
import me.joeleoli.praxi.arena.selection.Selection;
import me.joeleoli.praxi.match.Match;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ArenaListener implements Listener {

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!(event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			return;
		}

		Player player = event.getPlayer();
		ItemStack item = event.getItem();

		Block clicked = event.getClickedBlock();
		int location = 0;

		if (item == null || !item.equals(Selection.SELECTION_WAND)) {
			return;
		}

		Selection selection = Selection.createOrGetSelection(player);

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			selection.setPoint2(clicked.getLocation());
			location = 2;
		} else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			selection.setPoint1(clicked.getLocation());
			location = 1;
		}

		event.setCancelled(true);
		event.setUseItemInHand(Event.Result.DENY);
		event.setUseInteractedBlock(Event.Result.DENY);

		String message = Style.AQUA + (location == 1 ? "First" : "Second") +
		                 " location " + Style.YELLOW + "(" + Style.GREEN +
		                 clicked.getX() + Style.YELLOW + ", " + Style.GREEN +
		                 clicked.getY() + Style.YELLOW + ", " + Style.GREEN +
		                 clicked.getZ() + Style.YELLOW + ")" + Style.AQUA + " has been set!";

		if (selection.isFullObject()) {
			message += Style.RED + " (" + Style.YELLOW + selection.getCuboid().volume() + Style.AQUA + " blocks" +
			           Style.RED + ")";
		}

		player.sendMessage(message);
	}

	@EventHandler
	public void onBlockFromTo(BlockFromToEvent event) {
		final int x = event.getBlock().getX();
		final int y = event.getBlock().getY();
		final int z = event.getBlock().getZ();

		Arena foundArena = null;

		for (Arena arena : Arena.getArenas()) {
			if (!(arena.getType() == ArenaType.STANDALONE || arena.getType() == ArenaType.DUPLICATE)) {
				continue;
			}

			if (!arena.isActive()) {
				continue;
			}

			if (x >= arena.getX1() && x <= arena.getX2() && y >= arena.getY1() && y <= arena.getY2() &&
			    z >= arena.getZ1() && z <= arena.getZ2()) {
				foundArena = arena;
				break;
			}
		}

		if (foundArena == null) {
			return;
		}

		for (Match match : Match.getMatches()) {
			if (match.getArena().equals(foundArena)) {
				if (match.isFighting()) {
					match.getPlacedBlocks().add(event.getToBlock().getLocation());
				}

				break;
			}
		}
	}

}
