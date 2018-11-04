package me.joeleoli.praxi.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import java.util.Collections;
import java.util.UUID;
import me.joeleoli.nucleus.config.ConfigCursor;
import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.player.PraxiPlayer;
import org.bson.Document;

public class PraxiMongo {

	private MongoClient client;
	private MongoDatabase database;
	private MongoCollection<Document> players;
	private MongoCollection<Document> matches;

	public PraxiMongo() {
		ConfigCursor cursor = new ConfigCursor(Praxi.getInstance().getMainConfig(), "mongo");

		if (!cursor.exists("host")
		    || !cursor.exists("port")
		    || !cursor.exists("database")
		    || !cursor.exists("authentication.enabled")
		    || !cursor.exists("authentication.username")
		    || !cursor.exists("authentication.password")
		    || !cursor.exists("authentication.database")) {
			throw new RuntimeException("Missing configuration option");
		}

		if (cursor.getBoolean("authentication.enabled")) {
			final MongoCredential credential = MongoCredential.createCredential(
					cursor.getString("authentication.username"),
					cursor.getString("authentication.database"),
					cursor.getString("authentication.password").toCharArray()
			);

			this.client = new MongoClient(new ServerAddress(cursor.getString("host"), cursor.getInt("port")),
					Collections.singletonList(credential)
			);
		} else {
			this.client = new MongoClient(new ServerAddress(cursor.getString("host"), cursor.getInt("port")));
		}

		this.database = this.client.getDatabase("praxi");
		this.players = this.database.getCollection("players");
		this.matches = this.database.getCollection("matches");
	}

	public Document getPlayer(UUID uuid) {
		return this.players.find(Filters.eq("uuid", uuid.toString())).first();
	}

	public void replacePlayer(PraxiPlayer player, Document document) {
		this.players.replaceOne(Filters.eq("uuid", player.getUuid().toString()), document,
				new ReplaceOptions().upsert(true)
		);
	}

}
