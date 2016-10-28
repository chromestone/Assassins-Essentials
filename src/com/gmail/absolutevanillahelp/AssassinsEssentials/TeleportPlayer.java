package com.gmail.absolutevanillahelp.AssassinsEssentials;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class TeleportPlayer implements Runnable {

	private final UUID playerID;
	private final Location location;
	
	public TeleportPlayer(UUID id, Location location) {
		playerID = id;
		this.location = location;
	}

	@Override
	public void run() {
		Bukkit.getPlayer(playerID).teleport(location);
	}
}