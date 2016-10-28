package com.gmail.absolutevanillahelp.AssassinsEssentials;

import java.util.*;

import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
//import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.*;

/**
 * This class main purpose it to allow players to jump off from high places
 * and "assassinate" other players by punching them with a special arrow before they land
 * without taking fall damage.
 * Also other cool kinda assassin's creed add ons may have been implemented
 * Note that other extraneous implementations may have been made.
 * @author Derek Zhang
 */
public class PlayerEventListener implements Listener {

	private final AssassinsEssentials plugin;
	private final ArrayList<UUID> deadPlayers;
	private final ArrayList<UUID> noFallDmgPlayers;

	public PlayerEventListener(AssassinsEssentials instance) {

		plugin = instance;
		deadPlayers = new ArrayList<UUID>();
		noFallDmgPlayers = new ArrayList<UUID>();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPotionSlpash(PotionSplashEvent event) {

		if (event.getPotion().getShooter() instanceof Player && event.getPotion().getItem().getDurability() == 16384) {

			Player thrower = (Player) event.getPotion().getShooter();

			for (LivingEntity livingEntity: event.getAffectedEntities()) {

				if (livingEntity instanceof Player && !thrower.getUniqueId().equals(livingEntity.getUniqueId()) && ((Player) livingEntity).getInventory().getHelmet().getType() != Material.BEACON) {

					livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5, 0, true), true);
				}
			}
			
			if (new Random().nextInt(1000) == 0) {
			
				Location location = event.getPotion().getLocation();
				event.getPotion().getLocation().getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), 2, false, false);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onWeatherChange(WeatherChangeEvent event) {

		if (event.toWeatherState()) {

			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onProjectileHit(ProjectileHitEvent event) {

		if (event.getEntity() instanceof Egg && event.getEntity().getShooter() instanceof Player) {

			Location location = event.getEntity().getLocation();
			location.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), 6, false, false);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {

		Player player = event.getPlayer();
		if (plugin.getSecuredPlayers().containsKey(player.getUniqueId()) && !plugin.getSecuredPlayers().get(player.getUniqueId())) {

			event.setCancelled(true);
		}
		else if (player.getItemInHand().getType() == Material.BEACON && player.getItemInHand().getItemMeta().getDisplayName().startsWith("F")) {

			ItemStack beacon = player.getItemInHand();
			player.setItemInHand(null);
			if (!Material.AIR.equals(player.getInventory().getHelmet()) && player.getInventory().getHelmet() != null) {

				ItemStack helmet = player.getInventory().getHelmet();
				player.getInventory().setHelmet(beacon);
				plugin.givePlayerItemMyWay(player, new ItemStack[]{helmet});
			}
			else {
				player.getInventory().setHelmet(beacon);
			}
			player.updateInventory();
			event.setCancelled(true);
		}
		else if (player.getItemInHand().getType() == Material.POTION && player.getItemInHand().getDurability() == 8192
				&& player.hasPotionEffect(PotionEffectType.JUMP) && player.hasPotionEffect(PotionEffectType.BLINDNESS) && player.hasPotionEffect(PotionEffectType.SLOW)) {

			player.setItemInHand(null);
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 0, 0, true), true);
			player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 0, 0, true), true);
			player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 0, 0, true), true);
			event.setCancelled(true);
		}
		else if (!event.getPlayer().isOp() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {

			event.setCancelled(true);
			event.getPlayer().closeInventory();
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent event) {

		if (!plugin.getAssassinations().containsKey(event.getPlayer().getUniqueId())) {

			plugin.getAssassinations().put(event.getPlayer().getUniqueId(), 0);
		}

		if (!plugin.getPlayingPlayers().contains(event.getPlayer().getUniqueId())) {

			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new PlayerRespawnToDo(event.getPlayer()), 20);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerLeave(PlayerQuitEvent event) {

		UUID playerID = event.getPlayer().getUniqueId();
		if (plugin.getSecuredPlayers().containsKey(playerID)) {

			plugin.getSecuredPlayers().put(playerID, false);
		}
	}

	//The Ark
	//	@EventHandler(priority = EventPriority.MONITOR)
	//	public void onChunkLoad(ChunkLoadEvent event) {
	//		if (event.isNewChunk()) {
	//			Chunk chunk = event.getChunk();
	//			String players = "";
	//			for (Entity e : chunk.getEntities()) {
	//				if (e instanceof Player) {
	//					players += ((Player) e).getName() + ", ";
	//				}
	//			}
	//			plugin.getLogger().warning("New Chunk Loaded(" + chunk.getWorld().getEnvironment().toString() + "): " + chunk.toString() + " Players: " + players);
	//
	//			players = "";
	//			for (Player player : plugin.getServer().getOnlinePlayers()) {
	//				players += player.getName() + ": " + player.getLocation().getChunk().toString() + ", ";
	//			}
	//			plugin.getLogger().info(players);
	//		}
	//	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent event) {

		if (event.getEntity() instanceof Player) {

			Player damaged = (Player) event.getEntity();
			if (plugin.getSecuredPlayers().containsKey(damaged.getUniqueId()) && !plugin.getSecuredPlayers().get(damaged.getUniqueId())) {

				event.setCancelled(true);
			}
			else if (event.getCause() == DamageCause.FALL) {

				if (damaged.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.HAY_BLOCK) {

					event.setCancelled(true);
				}
				else if (noFallDmgPlayers.contains(damaged.getUniqueId())) {

					noFallDmgPlayers.remove(damaged.getUniqueId());
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void on(EntityDamageByEntityEvent event) {

		if (event.getEntity() instanceof Player) {

			Player defender = (Player) event.getEntity();

			if (event.getDamager() instanceof Arrow
					&& ((Arrow) event.getDamager()).getShooter() instanceof Player && ((Player) ((Arrow) event.getDamager()).getShooter()).getUniqueId().equals(defender.getUniqueId())) {

				event.setCancelled(true);
			}
			else if (event.getDamager() instanceof Egg) {

				Egg egg = (Egg) event.getDamager();
				if (egg.getShooter() instanceof Player) {

					Player shooter = (Player) egg.getShooter();
					if (shooter.getUniqueId().equals(defender.getUniqueId())) {

						event.setCancelled(true);
					}
				}
			}
			else if (event.getDamager() instanceof Player) {

				Player attacker = (Player) event.getDamager();

				if (plugin.getSecuredPlayers().containsKey(attacker.getUniqueId()) && !plugin.getSecuredPlayers().get(attacker.getUniqueId())) {

					event.setCancelled(true);
				}
				else if (attacker.getItemInHand().getType() == Material.ARROW && !plugin.getAssassinationDelay().contains(attacker.getUniqueId())) {

					if (attacker.getFallDistance() > 5 && attacker.getFallDistance() > defender.getFallDistance()) {

						noFallDmgPlayers.add(attacker.getUniqueId());
						deadPlayers.add(defender.getUniqueId());
						defender.setHealth(0);
						plugin.getServer().broadcastMessage(defender.getName() + " was assassinated by " + attacker.getName());
						plugin.getAssassinations().put(attacker.getUniqueId(), plugin.getAssassinations().containsKey(attacker.getUniqueId()) ? plugin.getAssassinations().get(attacker.getUniqueId()) + 1 : 1);
						event.setCancelled(true);
						Objective obj = plugin.getServer().getScoreboardManager().getMainScoreboard().getObjective(DisplaySlot.SIDEBAR);
						if (obj != null) {

							Score score = obj.getScore(attacker.getName());
							score.setScore(score.getScore()+1);
						}
					}
					else if (canAssassinatePlayer(attacker, defender)) {

						deadPlayers.add(defender.getUniqueId());
						defender.setHealth(0);
						plugin.getServer().broadcastMessage(defender.getName() + " was assassinated by " + attacker.getName());
						plugin.getAssassinations().put(attacker.getUniqueId(), plugin.getAssassinations().containsKey(attacker.getUniqueId()) ? plugin.getAssassinations().get(attacker.getUniqueId()) + 1 : 1);
						event.setCancelled(true);
						Objective obj = plugin.getServer().getScoreboardManager().getMainScoreboard().getObjective(DisplaySlot.SIDEBAR);
						if (obj != null) {

							Score score = obj.getScore(attacker.getName());
							score.setScore(score.getScore()+1);
						}
					}
					else {

						attacker.sendRawMessage(ChatColor.RED + "FAIL");
					}

					plugin.getAssassinationDelay().add(attacker.getUniqueId());
					plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new AssassinationDelay(plugin, attacker.getUniqueId()), 2*60*20);
					attacker.sendRawMessage(ChatColor.GOLD + "Knife needs two minutes to resharpen!");
				}
			}
		}
	}

	private boolean canAssassinatePlayer(Player attacker, Player defender) {

		double angle = attacker.getLocation().getDirection().angle(defender.getLocation().getDirection()) * 180 / Math.PI;
		if (angle > 22.5 && angle < 112.5 && attacker.isSprinting()) {

			if (defender.isSprinting()) {

				for (PotionEffect attackerPotionEffect : attacker.getActivePotionEffects()) {

					if (attackerPotionEffect.getType() == PotionEffectType.SPEED) {

						for (PotionEffect defenderPotionEffect : defender.getActivePotionEffects()) {

							if (defenderPotionEffect.getType() == PotionEffectType.SPEED) {

								if(attackerPotionEffect.getAmplifier() > defenderPotionEffect.getAmplifier()) {

									return true;
								}
								else {
									return false;
								}
							}
						}
						return true;
					}
				}
			}
			else {
				return true;
			}
		}
		return false;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDropItem(PlayerDropItemEvent event) {


		if (plugin.getSecuredPlayers().containsKey(event.getPlayer().getUniqueId()) && !plugin.getSecuredPlayers().get(event.getPlayer().getUniqueId())) {

			event.setCancelled(true);
		}
		else if (event.getItemDrop().getItemStack().getType() == Material.ARROW) {

			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerRespawn(PlayerRespawnEvent event) {

		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new PlayerRespawnToDo(event.getPlayer()), 20);
		event.getPlayer().getInventory().clear();
		event.getPlayer().getInventory().setArmorContents(new ItemStack[]
				{new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
	}

    /**
     * Note that this method contains contains code checking for a "secure player" (a player that needs to enter a second password)
     * This method also contains code allowing only whitelisted players to "request owner" which sends a text message
     * through my other Bukkit plugin EMailNotifications (bad practice I know right?)
     */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {

		String baseCmdName = event.getMessage().split(" ")[0];

		if (plugin.getSecuredPlayers().containsKey(event.getPlayer().getUniqueId()) && !plugin.getSecuredPlayers().get(event.getPlayer().getUniqueId())) {
			if (!baseCmdName.equalsIgnoreCase("/UnlockC")) {

				event.setCancelled(true);
			}
		}
		else if (baseCmdName.equalsIgnoreCase("/requestowner")) {

			if (!event.getPlayer().isWhitelisted()) {

				event.setCancelled(true);
			}
		}
		else if (!event.getPlayer().isOp() && !plugin.getPlayingPlayers().contains(event.getPlayer().getUniqueId())) {

			if (!baseCmdName.equalsIgnoreCase("/play")) {

				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDeath(PlayerDeathEvent event) {

		Player player = event.getEntity();

		plugin.getPlayingPlayers().remove(player.getUniqueId());

		boolean isAssassinated = deadPlayers.contains(player.getUniqueId());
		if (isAssassinated) {

			deadPlayers.remove(player.getUniqueId());
			event.setDeathMessage("");
		}

		if (player.getKiller() != null || isAssassinated) {

			Player killer = player.getKiller();
			plugin.getAssassinations().put(killer.getUniqueId(), plugin.getAssassinations().containsKey(killer.getUniqueId()) ? plugin.getAssassinations().get(killer.getUniqueId()) + 1 : 1);

			ArrayList<ItemStack> contents = new ArrayList<ItemStack>();
			ListIterator<ItemStack> it = player.getInventory().iterator();
			while (it.hasNext()) {

				ItemStack item = it.next();
				if (item != null) {

					contents.add(item);
				}
			}
			if (contents.size() > 0) {

				player.getWorld().dropItem(player.getLocation(), contents.get(new Random().nextInt(contents.size())));
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerCraftItem(CraftItemEvent event) {

		event.setCancelled(true);
	}

    /**
     * Note that this was used on a server of 10 players at most
     * I allowed anybody else to join even if not whitelisted, but whitelisted people took priority if the server was full
     * @param event
     */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {

		if (plugin.getServer().getOnlinePlayers().length == 10 && Bukkit.getOfflinePlayer(event.getUniqueId()).isWhitelisted()) {

			for (Player player : plugin.getServer().getOnlinePlayers()) {

				if (!player.isWhitelisted()) {

					player.kickPlayer("Server Full (sorry)");
				}
			}
		}
	}
}
