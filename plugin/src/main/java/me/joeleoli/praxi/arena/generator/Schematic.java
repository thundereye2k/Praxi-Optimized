package me.joeleoli.praxi.arena.generator;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.world.DataException;
import java.io.File;
import java.io.IOException;
import lombok.Getter;
import me.joeleoli.nucleus.Nucleus;
import org.bukkit.World;

@Getter
public class Schematic {

	private CuboidClipboard clipBoard;

	public Schematic(File file) throws IOException {
		SchematicFormat format = SchematicFormat.MCEDIT;

		try {
			this.clipBoard = format.load(file);
		} catch (DataException e) {
			Nucleus.getInstance().getLogger().severe("Failed to load schematic...");
		}
	}

	public void pasteSchematic(World world, int x, int y, int z) {
		Vector pastePos = new Vector(x, y, z);
		EditSession editSession = new EditSession(new BukkitWorld(world), 999999);

		try {
			this.clipBoard.place(editSession, pastePos, true);
		} catch (MaxChangedBlocksException e) {
			e.printStackTrace();
		}
	}

}
