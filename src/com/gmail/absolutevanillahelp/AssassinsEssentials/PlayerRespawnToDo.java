package com.gmail.absolutevanillahelp.AssassinsEssentials;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerRespawnToDo implements Runnable {

	UUID playerID;
	
	public PlayerRespawnToDo(Player player) {
		playerID = player.getUniqueId();
	}
	
	@Override
	public void run() {
		Player player = Bukkit.getPlayer(playerID);
		if (player != null) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 1000000*20, 255, true), true);
			player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 1000000*20, 255, true), true);
		}
	}
}
