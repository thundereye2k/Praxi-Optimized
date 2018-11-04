package me.joeleoli.praxi;

import me.joeleoli.praxi.queue.Queue;

public interface PraxiAPI {

	/**
	 * Gets the amount of players that are queueing.
	 *
	 * @return The amount of players that are queueing.
	 */
	int getQueueingCount();

	/**
	 * Gets the amount of players that are fighting.
	 *
	 * @return The amount of players that are fighting.
	 */
	int getFightingCount();

	/**
	 * Gets the amount of players in matches originating from a queue.
	 *
	 * @param queue The queue.
	 *
	 * @return The amount of players in matches originating from the given queue.
	 */
	int getFightingCount(Queue queue);

}
