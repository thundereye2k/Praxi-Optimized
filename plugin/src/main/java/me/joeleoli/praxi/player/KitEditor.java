package me.joeleoli.praxi.player;

import lombok.Getter;
import lombok.Setter;
import me.joeleoli.praxi.kit.NamedKit;
import me.joeleoli.praxi.ladder.Ladder;

@Setter
public class KitEditor {

	@Getter
	private boolean active;
	private boolean rename;
	@Getter
	private PlayerState previousState;
	@Getter
	private Ladder selectedLadder;
	@Getter
	private NamedKit selectedKit;

	public boolean isRenaming() {
		return this.active && this.rename && this.selectedKit != null;
	}

}
