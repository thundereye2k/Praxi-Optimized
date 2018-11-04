package me.joeleoli.praxi.match;

import com.boydti.fawe.util.EditSessionBuilder;
import com.boydti.fawe.util.TaskManager;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import lombok.AllArgsConstructor;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.scheduler.BukkitRunnable;

@AllArgsConstructor
public class MatchResetTask extends BukkitRunnable {

	private Match match;

	@Override
	public void run() {
		if (this.match.getLadder().isBuild() && this.match.getPlacedBlocks().size() > 0) {
			TaskManager.IMP.async(() -> {
				EditSession editSession = new EditSessionBuilder(this.match.getArena().getSpawn1().getWorld().getName())
						.fastmode(true)
						.allowedRegionsEverywhere()
						.autoQueue(false)
						.limitUnlimited()
						.build();

				for (Location location : this.match.getPlacedBlocks()) {
					try {
						editSession.setBlock(
								new Vector((double) location.getBlockX(), (double) location.getBlockY(),
										location.getZ()
								), new BaseBlock(0));
					} catch (Exception ex) {
					}
				}

				editSession.flushQueue();

				TaskManager.IMP.task(() -> {
					this.match.getPlacedBlocks().clear();
					this.match.getArena().setActive(false);
					this.cancel();
				});
			});
		} else if (this.match.getLadder().isBuild() && this.match.getChangedBlocks().size() > 0) {
			TaskManager.IMP.async(() -> {
				EditSession editSession = new EditSessionBuilder(this.match.getArena().getSpawn1().getWorld().getName())
						.fastmode(true)
						.allowedRegionsEverywhere()
						.autoQueue(false)
						.limitUnlimited()
						.build();

				for (BlockState blockState : this.match.getChangedBlocks()) {
					try {
						editSession.setBlock(
								new Vector(blockState.getLocation().getBlockX(), blockState.getLocation().getBlockY(),
										blockState.getLocation().getZ()
								), new BaseBlock(blockState.getTypeId(), blockState.getRawData()));
					} catch (Exception ex) {
					}
				}

				editSession.flushQueue();

				TaskManager.IMP.task(() -> {
					if (this.match.getLadder().isBuild()) {
						this.match.getChangedBlocks().clear();
						this.match.getArena().setActive(false);
					}

					this.cancel();
				});
			});
		} else {
			this.cancel();
		}
	}

}
