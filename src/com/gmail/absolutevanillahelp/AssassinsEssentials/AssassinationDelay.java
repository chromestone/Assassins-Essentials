package com.gmail.absolutevanillahelp.AssassinsEssentials;

import java.util.UUID;

public class AssassinationDelay implements Runnable {

	private final AssassinsEssentials plugin;
	private final UUID playerID;
	
	public AssassinationDelay(AssassinsEssentials instance, UUID id) {
	
		plugin = instance;
		playerID = id;
	}
	
	@Override
	public void run() {

		plugin.getAssassinationDelay().remove(playerID);
	}
}
