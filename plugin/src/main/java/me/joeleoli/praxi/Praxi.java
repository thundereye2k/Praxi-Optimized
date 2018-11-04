package me.joeleoli.praxi;

import com.google.gson.JsonParser;
import java.util.Iterator;
import lombok.Getter;
import me.joeleoli.nucleus.Nucleus;
import me.joeleoli.nucleus.board.BoardManager;
import me.joeleoli.nucleus.command.CommandHandler;
import me.joeleoli.nucleus.config.ConfigCursor;
import me.joeleoli.nucleus.config.FileConfig;
import me.joeleoli.nucleus.listener.ListenerHandler;
import me.joeleoli.nucleus.settings.Settings;
import me.joeleoli.nucleus.util.InventoryUtil;
import me.joeleoli.nucleus.util.LocationUtil;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.arena.ArenaType;
import me.joeleoli.praxi.arena.SharedArena;
import me.joeleoli.praxi.arena.StandaloneArena;
import me.joeleoli.praxi.board.PracticeBoardAdapter;
import me.joeleoli.praxi.command.param.ArenaParameterType;
import me.joeleoli.praxi.command.param.ArenaTypeParameterType;
import me.joeleoli.praxi.command.param.LadderParameterType;
import me.joeleoli.praxi.command.param.QueueParameterType;
import me.joeleoli.praxi.config.ConfigItem;
import me.joeleoli.praxi.events.EventManager;
import me.joeleoli.praxi.handler.PlayerMovementHandler;
import me.joeleoli.praxi.kit.Kit;
import me.joeleoli.praxi.ladder.Ladder;
import me.joeleoli.praxi.mongo.PraxiMongo;
import me.joeleoli.praxi.player.PlayerHotbar;
import me.joeleoli.praxi.player.PracticeSetting;
import me.joeleoli.praxi.queue.Queue;
import me.joeleoli.praxi.queue.QueueThread;
import me.joeleoli.praxi.task.ExpBarCooldownTask;
import me.joeleoli.praxi.task.InventoryCleanupTask;
import me.joeleoli.praxi.task.InviteCleanupTask;
import me.joeleoli.praxi.task.RematchExpireRunnable;
import me.joeleoli.praxi.task.SaveDataTask;
import me.joeleoli.ragespigot.RageSpigot;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

@Getter
public class Praxi extends PraxiProvider {

	public static JsonParser PARSER = new JsonParser();
	@Getter
	private static Praxi instance;

	private FileConfig mainConfig, arenaConfig, ladderConfig;

	private PraxiMongo praxiMongo;
	private EventManager eventManager;

	private long startup = System.currentTimeMillis();

	@Override
	public void onEnable() {
		instance = this;

		this.mainConfig = new FileConfig(this, "config.yml");
		this.arenaConfig = new FileConfig(this, "arenas.yml");
		this.ladderConfig = new FileConfig(this, "ladders.yml");

		this.praxiMongo = new PraxiMongo();
		this.eventManager = new EventManager();
		this.eventManager.load();

		this.loadLadders();
		this.loadArenas();
		this.loadQueues();

		PlayerHotbar.init();

		Nucleus.getInstance().setBoardManager(new BoardManager(this, new PracticeBoardAdapter()));

		CommandHandler.registerParameterType(Arena.class, new ArenaParameterType());
		CommandHandler.registerParameterType(ArenaType.class, new ArenaTypeParameterType());
		CommandHandler.registerParameterType(Ladder.class, new LadderParameterType());
		CommandHandler.registerParameterType(Queue.class, new QueueParameterType());
		CommandHandler.loadCommandsFromPackage(this, "me.joeleoli.praxi.command");
		ListenerHandler.loadListenersFromPackage(this, "me.joeleoli.praxi.listener");

		Settings.register(PracticeSetting.RECEIVE_DUEL_REQUESTS, true);
		Settings.register(PracticeSetting.SHOW_SCOREBOARD, true);
		Settings.register(PracticeSetting.ALLOW_SPECTATORS, true);
		Settings.register(PracticeSetting.PING_FACTOR, false);

		RageSpigot.INSTANCE.addMovementHandler(new PlayerMovementHandler());

		new QueueThread().start();

		this.getServer().getScheduler().runTaskTimer(this, new ExpBarCooldownTask(), 2L, 2L);
		this.getServer().getScheduler().runTaskTimerAsynchronously(this, this, 0L, 10L);
		this.getServer().getScheduler()
		    .runTaskTimerAsynchronously(this, new InventoryCleanupTask(), 20L * 5, 20L * 5);
		this.getServer().getScheduler().runTaskTimerAsynchronously(this, new InviteCleanupTask(), 20L * 5, 20L * 5);
		this.getServer().getScheduler()
		    .runTaskTimerAsynchronously(this, new SaveDataTask(), 20L * 60 * 5, 20L * 60 * 5);
		this.getServer().getScheduler().runTaskTimerAsynchronously(this, new RematchExpireRunnable(), 20L * 3, 20L * 3);

		this.getServer().getWorlds().forEach(world -> {
			world.setDifficulty(Difficulty.HARD);
			world.setTime(12000);

			world.getEntities().forEach(entity -> {
				if (!(entity instanceof Player)) {
					entity.remove();
				}
			});
		});

		this.removeCrafting(Material.WORKBENCH);
		this.removeCrafting(Material.STICK);
		this.removeCrafting(Material.WOOD_PLATE);
		this.removeCrafting(Material.WOOD_BUTTON);
		this.removeCrafting(Material.SNOW_BLOCK);
	}

	private void loadLadders() {
		ConfigCursor cursor = new ConfigCursor(Praxi.getInstance().getLadderConfig(), "ladders");

		for (String key : cursor.getKeys()) {
			cursor.setPath("ladders." + key);

			Ladder ladder = new Ladder(key);

			ladder.setDisplayName(Style.translate(cursor.getString("display-name")));
			ladder.setDisplayIcon(new ConfigItem(cursor, "display-icon").toItemStack());
			ladder.setEnabled(cursor.getBoolean("enabled"));
			ladder.setBuild(cursor.getBoolean("build"));
			ladder.setSumo(cursor.getBoolean("sumo"));
			ladder.setSpleef(cursor.getBoolean("spleef"));
			ladder.setParkour(cursor.getBoolean("parkour"));
			ladder.setRegeneration(cursor.getBoolean("regeneration"));

			if (cursor.exists("hit-delay")) {
				ladder.setHitDelay(cursor.getInt("hit-delay"));
			}

			if (cursor.exists("default-kit")) {
				final ItemStack[] armor = InventoryUtil.deserializeInventory(cursor.getString("default-kit.armor"));
				final ItemStack[] contents =
						InventoryUtil.deserializeInventory(cursor.getString("default-kit.contents"));

				ladder.setDefaultKit(new Kit(armor, contents));
			}

			if (cursor.exists("kit-editor.allow-potion-fill")) {
				ladder.setAllowPotionFill(cursor.getBoolean("kit-editor.allow-potion-fill"));
			}

			if (cursor.exists("kit-editor.items")) {
				for (String itemKey : cursor.getKeys("kit-editor.items")) {
					ladder.getKitEditorItems().add(new ConfigItem(cursor, "kit-editor.items." + itemKey).toItemStack());
				}
			}

			if (cursor.exists("kb-profile")) {
				ladder.setKbProfile(cursor.getString("kb-profile"));
			}
		}
	}

	private void loadArenas() {
		ConfigCursor cursor = new ConfigCursor(this.arenaConfig, "arenas");

		if (cursor.exists()) {
			for (String arenaName : cursor.getKeys()) {
				cursor.setPath("arenas." + arenaName);

				ArenaType arenaType = ArenaType.valueOf(cursor.getString("type"));
				Location location1 = LocationUtil.deserialize(cursor.getString("cuboid.location1"));
				Location location2 = LocationUtil.deserialize(cursor.getString("cuboid.location2"));

				Arena arena;

				if (arenaType == ArenaType.STANDALONE) {
					arena = new StandaloneArena(arenaName, location1, location2);
				} else if (arenaType == ArenaType.SHARED) {
					arena = new SharedArena(arenaName, location1, location2);
				} else {
					continue;
				}

				if (cursor.exists("spawn1")) {
					arena.setSpawn1(LocationUtil.deserialize(cursor.getString("spawn1")));
				}

				if (cursor.exists("spawn2")) {
					arena.setSpawn2(LocationUtil.deserialize(cursor.getString("spawn2")));
				}

				if (cursor.exists("ladders")) {
					for (String ladderName : cursor.getStringList("ladders")) {
						arena.getLadders().add(ladderName);
					}
				}

				if (arena.getType() == ArenaType.STANDALONE && cursor.exists("duplicates")) {
					for (String duplicateId : cursor.getKeys("duplicates")) {
						cursor.setPath("arenas." + arenaName + ".duplicates." + duplicateId);

						location1 = LocationUtil.deserialize(cursor.getString("cuboid.location1"));
						location2 = LocationUtil.deserialize(cursor.getString("cuboid.location2"));
						Location spawn1 = LocationUtil.deserialize(cursor.getString("spawn1"));
						Location spawn2 = LocationUtil.deserialize(cursor.getString("spawn2"));

						Arena duplicate = new Arena(arenaName, ArenaType.DUPLICATE, location1, location2);

						duplicate.setSpawn1(spawn1);
						duplicate.setSpawn2(spawn2);
						duplicate.setLadders(arena.getLadders());

						((StandaloneArena) arena).getDuplicates().add(duplicate);

						Arena.getArenas().add(duplicate);
					}
				}

				Arena.getArenas().add(arena);
			}
		}

		this.getLogger().info("Loaded " + Arena.getArenas().size() + " arenas");
	}

	private void loadQueues() {
		for (Ladder ladder : Ladder.getLadders()) {
			if (ladder.isEnabled()) {
				new Queue(ladder, false);
				new Queue(ladder, true);
			}
		}
	}

	private void removeCrafting(Material material) {
		Iterator<Recipe> iterator = getServer().recipeIterator();

		while (iterator.hasNext()) {
			Recipe recipe = iterator.next();

			if (recipe != null && recipe.getResult().getType() == material) {
				iterator.remove();
			}
		}
	}

}
