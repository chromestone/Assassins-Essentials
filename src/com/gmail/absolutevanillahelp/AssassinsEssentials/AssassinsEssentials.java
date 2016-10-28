package com.gmail.absolutevanillahelp.AssassinsEssentials;

import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AssassinsEssentials extends JavaPlugin {

	private final HashMap<UUID, Integer> assassinations;
	private final ArrayList<UUID> playingPlayers;
	private final ArrayList<Location> spawnpoints;
	private final ArrayList<UUID> assassinationDelay;
	private final HashMap<UUID, Boolean> securedPlayers;
	private final ItemStack[] goldenApples;
	private final ItemStack[] diamondArmor;
	private final ItemStack diamondSword;
	private final ItemStack infinityBow;
	private final ItemStack arrowKnife;
	private AEConfiguration dataConfig;

	public AssassinsEssentials() {

		assassinations = new HashMap<UUID, Integer>();
		playingPlayers = new ArrayList<UUID>();
		spawnpoints = new ArrayList<Location>();
		assassinationDelay = new ArrayList<UUID>();
		securedPlayers = new HashMap<UUID, Boolean>();

		ItemStack i = new ItemStack(Material.GOLDEN_APPLE, 64);
		goldenApples = new ItemStack[]{i, i, i, i, i, i, i, i, i, i, i, i, i, i, i, i, i, i};

		diamondArmor = new ItemStack[]{new ItemStack(Material.DIAMOND_BOOTS), new ItemStack(Material.DIAMOND_LEGGINGS),
				new ItemStack(Material.DIAMOND_CHESTPLATE), new ItemStack(Material.DIAMOND_HELMET)};
		for (ItemStack armor : diamondArmor) {
			armor.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
		}

		diamondSword = new ItemStack(Material.DIAMOND_SWORD);
		diamondSword.addUnsafeEnchantment(Enchantment.DURABILITY, 10);

		infinityBow = new ItemStack(Material.BOW);
		infinityBow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
		infinityBow.addUnsafeEnchantment(Enchantment.DURABILITY, 10);

		arrowKnife = new ItemStack(Material.ARROW);
		ItemMeta meta = arrowKnife.getItemMeta();
		meta.setDisplayName("Knife");
		ArrayList<String> lore = new ArrayList<String>();
		lore.add(ChatColor.BLUE + "Combat");
		lore.add("");
		lore.add(ChatColor.BLUE + "KO Attack Damage Under Right Conditions");
		meta.setLore(lore);
		arrowKnife.setItemMeta(meta);
	}

	@Override
	public void onEnable() {

		dataConfig = new AEConfiguration(this, "data.yml");

		if (dataConfig.getConfig().isSet("money")) {

			Set<String> playerList =  dataConfig.getConfig().getConfigurationSection("money").getKeys(false);

			Set<UUID> playerIDs = new HashSet<UUID>();
			for (String ID : playerList) {

				playerIDs.add(UUID.fromString(ID));
			}

			for (UUID playerID : playerIDs) {

				Integer money = dataConfig.getConfig().getInt("money." + playerID.toString());
				if (money != null) {

					assassinations.put(playerID, money);
				}
			}
		}

		if (dataConfig.getConfig().isSet("spawnpoints")) {

			List<String> locationList = dataConfig.getConfig().getStringList("spawnpoints");
			for (String location : locationList) {
				String[] coordinates = location.split("\\|");
				if (coordinates.length >= 3) {
					try {
						spawnpoints.add(new Location(getServer().getWorlds().get(0),
								Double.parseDouble(coordinates[0]),
								Double.parseDouble(coordinates[1]),
								Double.parseDouble(coordinates[2])));
					}
					catch (Exception e) {}
				}
			}
		}

		if (dataConfig.getConfig().isSet("playing")) {

			List<String> idList =  dataConfig.getConfig().getStringList("playing");
			for (String id : idList) {

				playingPlayers.add(UUID.fromString(id));
			}
		}

		if (dataConfig.getConfig().isSet("Secure")) {
			for (String name : dataConfig.getConfig().getConfigurationSection("Secure").getKeys(false)) {
				securedPlayers.put(UUID.fromString(name), false);
			}
		}

		getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);
	}

	public HashMap<UUID, Integer> getAssassinations() {

		return assassinations;
	}

	public ArrayList<UUID> getPlayingPlayers() {

		return playingPlayers;
	}

	public ArrayList<UUID> getAssassinationDelay() {

		return assassinationDelay;
	}

	public HashMap<UUID, Boolean> getSecuredPlayers() {
		return securedPlayers;
	}

	public void givePlayerItemMyWay(Player player, ItemStack[] items) {

		HashMap<Integer, ItemStack> leftOver = player.getInventory().addItem(items);
		if (leftOver.size() > 0) {

			leftOver = player.getEnderChest().addItem(leftOver.values().toArray(new ItemStack[0]));
			player.sendRawMessage(ChatColor.RED + "Not enough room in inventory, sending excess items to enderchest (/enderchest for access).");
			if (leftOver.size() > 0) {

				for (ItemStack item: leftOver.values()) {

					player.getWorld().dropItem(player.getLocation(), item);
				}
				player.sendRawMessage(ChatColor.RED + "Not enough room in enderchest, dropping excess items on ground.");
			}
		}
	}

	private boolean teleportPlayerSafely(Player player, Location location) {

		for (Player p : getServer().getOnlinePlayers()) {

			if (p.getLocation().distanceSquared(location) <= 100) {

				return false;
			}
		}

		player.teleport(location);
		playingPlayers.add(player.getUniqueId());

		return true;
	}

	private void givePlayerStartingGear(Player player) {

		PlayerInventory inv = player.getInventory();
		inv.addItem(new ItemStack[]{diamondSword, infinityBow, arrowKnife});
		inv.setArmorContents(diamondArmor);
		inv.addItem(goldenApples);
	}

	private String numberToRomanNumeral(int num) {

		if (num == 1) {

			return "I";
		}
		else if (num == 2) {

			return "II";
		}
		else if (num == 3) {

			return "III";
		}
		else if (num == 4) {

			return "IV";
		}
		else if (num == 5) {

			return "V";
		}
		else if (num == 6) {

			return "VI";
		}
		else if (num == 7) {

			return "VII";
		}
		else if (num == 8) {

			return "VIII";
		}
		else if (num == 9) {

			return "IX";
		}
		else if (num == 10) {

			return "X";
		}
		else {
			return Integer.toString(num);
		}
	}

    /**
     * Note that I have hardcoded a menu into the game (bad practice I know right?)
     *
     * @param sender
     * @param cmd
     * @param label
     * @param args
     */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		String cmdName = cmd.getName();
		if (cmdName.equals("SecureCheckC") && args.length > 0) {
			if (securedPlayers.containsKey(UUID.fromString(args[0]))) {
				if (securedPlayers.get(UUID.fromString(args[0]))) {
					sender.sendMessage(ChatColor.GREEN + args[0] + "'s identity is valid.");
				}
				else {
					sender.sendMessage(ChatColor.RED + args[0] + "'s identity hasn't been confirmed!");
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + args[0] + " hasn't been listed in the Secured section!");
			}
			return true;
		}
		else if (sender instanceof Player) {

			Player player = (Player) sender;
			UUID playerID = player.getUniqueId();
			if (cmdName.equals("spawn")) {

				player.teleport(getServer().getWorlds().get(0).getSpawnLocation());
				return true;
			}
			else if (cmdName.equals("shop") && args.length >= 1) {

				if (!assassinations.containsKey(playerID)) {

					assassinations.put(playerID, 0);
				}

				if (args[0].equalsIgnoreCase("buy")) {

					if (args.length >= 2) {

						int choice;
						if (StringUtils.isNumeric(args[1]) && (choice = Integer.parseInt(args[1])) <= 16 && choice > 0) {

							int money = assassinations.get(playerID);
							if (choice <= 6 && money >= 1) {

								assassinations.put(playerID, money-1);
								if (choice == 1) {

									givePlayerItemMyWay(player, goldenApples);
									player.sendRawMessage(ChatColor.GREEN + "You purchased 18 stacks of Golden Apples for 1 money.");
								}
								else if (choice == 2)  {

									givePlayerItemMyWay(player, diamondArmor);
									player.sendRawMessage(ChatColor.GREEN + "You purchased a full set of Diamond Armor for 1 money.");
								}
								else if (choice == 3) {

									givePlayerItemMyWay(player, new ItemStack[]{diamondSword});
									player.sendRawMessage(ChatColor.GREEN + "You purchased a Diamond Sword for 1 money.");
								}
								else if (choice == 4) {

									givePlayerItemMyWay(player, new ItemStack[]{infinityBow});
									player.sendRawMessage(ChatColor.GREEN + "You purchased an Infinity Bow for 1 money.");
								}
								else if (choice == 5) {

									ItemStack health2Potion = new ItemStack(Material.POTION);
									health2Potion.setDurability((short) 16421);
									givePlayerItemMyWay(player, new ItemStack[]{health2Potion});
									player.sendRawMessage(ChatColor.GREEN + "You purchased a Health II Splash Potion for 1 money.");
								}
								else {

									ItemStack speed1Potion = new ItemStack(Material.POTION);

									PotionMeta meta = (PotionMeta) speed1Potion.getItemMeta();
									meta.setDisplayName(ChatColor.WHITE + "Potion of Swiftness");
									meta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 30 * 20, 0), true);

									List<String> lore = new ArrayList<String>();
									lore.add(ChatColor.GRAY + "Speed " + "(0:30)");
									lore.add("");
									lore.add(ChatColor.DARK_PURPLE + "When Applied:");
									lore.add(ChatColor.BLUE + "+20% Speed");
									lore.add(ChatColor.BLUE + "Increases Assassination Chance");
									meta.setLore(lore);

									speed1Potion.setItemMeta(meta);

									givePlayerItemMyWay(player, new ItemStack[]{speed1Potion});
									player.sendRawMessage(ChatColor.GREEN + "You purchased a Speed I Potion for 1 money.");
								}
							}
							else if (choice >= 7 && choice <= 15 && money >= choice-5) {

								assassinations.put(playerID, money-(choice-5));
								ItemStack speedXPotion = new ItemStack(Material.POTION);

								PotionMeta meta = (PotionMeta) speedXPotion.getItemMeta();
								meta.setDisplayName(ChatColor.WHITE + "Potion of Swiftness");
								meta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 30 * 20, choice-6), true);

								List<String> lore = new ArrayList<String>();
								lore.add(ChatColor.GRAY + "Speed " + numberToRomanNumeral(choice-5) + " (0:30)");
								lore.add("");
								lore.add(ChatColor.DARK_PURPLE + "When Applied:");
								lore.add(ChatColor.BLUE + "+" + (choice-5)*20 + "% Speed");
								lore.add(ChatColor.BLUE + "Increases Assassination Chance");
								meta.setLore(lore);

								speedXPotion.setItemMeta(meta);

								givePlayerItemMyWay(player, new ItemStack[]{speedXPotion});
								player.sendRawMessage(ChatColor.GREEN + "You purchased a Speed " + numberToRomanNumeral(choice-5) + " Potion for " + (choice-5) + " money.");
							}
							else if (choice == 16 && money >= 5) {

								assassinations.put(playerID, money-5);
								ItemStack egg = new ItemStack(Material.EGG);

								ItemMeta meta = egg.getItemMeta();
								meta.setDisplayName("Bomb");
								ArrayList<String> lore = new ArrayList<String>();
								lore.add(ChatColor.BLUE + "Combat");
								lore.add("");
								lore.add(ChatColor.BLUE + "Explodes Upon Impact With 150% TNT Force");
								meta.setLore(lore);

								egg.setItemMeta(meta);

								givePlayerItemMyWay(player, new ItemStack[]{egg});
								player.sendRawMessage(ChatColor.GREEN + "You purchased a Bomb for 5 money.");
							}
							else if (choice == 17 && money >= 4) {

								assassinations.put(playerID, money-4);
								ItemStack flashPotion = new ItemStack(Material.POTION);
								flashPotion.setDurability((short) 16384);

								PotionMeta meta = (PotionMeta) flashPotion.getItemMeta();
								meta.setDisplayName(ChatColor.WHITE + "Flashbang");

								List<String> lore = new ArrayList<String>();
								lore.add(ChatColor.GRAY + "Blindness (0:05)");
								lore.add("");
								lore.add(ChatColor.DARK_PURPLE + "When Applied:");
								lore.add(ChatColor.RED + "Blindness to all players, within 5 blocks, except thrower");
								meta.setLore(lore);

								flashPotion.setItemMeta(meta);
								
								givePlayerItemMyWay(player, new ItemStack[]{flashPotion});
								player.sendRawMessage(ChatColor.GREEN + "You purchased a Flashbang for 4 money.");
							}
							else if (choice == 19 && money >= 8) {
							
								assassinations.put(playerID, money-8);
								ItemStack beacon = new ItemStack(Material.BEACON);
								
								ItemMeta meta = beacon.getItemMeta();
								meta.setDisplayName(ChatColor.WHITE + "Flash Resistant Goggles");
								
								givePlayerItemMyWay(player, new ItemStack[]{beacon});
								player.sendRawMessage(ChatColor.GREEN + "You purchased a Flashbang for 8 money.");
							}
							else {
								player.sendRawMessage(ChatColor.RED + "You can not afford item " + choice + ".");
							}
						}
						else {
							player.sendRawMessage(ChatColor.RED + "Please enter a valid purchase number.");
						}
					}
					else {
						player.sendRawMessage(ChatColor.GREEN + "You have " + assassinations.get(playerID) + " money.");
						player.sendRawMessage(ChatColor.BLUE + "To buy, do /shop buy [number].");
						player.sendRawMessage(ChatColor.GOLD + "1) 18 Stacks of Golden Apples; Price: 1 money.");
						player.sendRawMessage(ChatColor.YELLOW + "2) Full Set of Diamond Armor; Price: 1 money.");
						player.sendRawMessage(ChatColor.GOLD + "3) Diamond Sword; Price: 1 money.");
						player.sendRawMessage(ChatColor.YELLOW + "4) Infinity Bow; Price: 1 money.");
						player.sendRawMessage(ChatColor.GOLD + "5) Health 2 Potion; Price: 1 money.");
						player.sendRawMessage(ChatColor.YELLOW + "6) Speed 1 Potion; Price: 1 money.");
						player.sendRawMessage(ChatColor.GOLD + "7) Speed 2 Potion; Price: 2 money.");
						player.sendRawMessage(ChatColor.YELLOW + "8) Speed 3 Potion; Price: 3 money.");
						player.sendRawMessage(ChatColor.GOLD + "9) Speed 4 Potion; Price: 4 money.");
						player.sendRawMessage(ChatColor.YELLOW + "10) Speed 5 Potion; Price: 5 money.");
						player.sendRawMessage(ChatColor.GOLD + "11) Speed 6 Potion; Price: 6 money.");
						player.sendRawMessage(ChatColor.YELLOW + "12) Speed 7 Potion; Price: 7 money.");
						player.sendRawMessage(ChatColor.GOLD + "13) Speed 8 Potion; Price: 8 money.");
						player.sendRawMessage(ChatColor.YELLOW + "14) Speed 9 Potion; Price: 9 money.");
						player.sendRawMessage(ChatColor.GOLD + "15) Speed 10 Potion; Price: 10 money.");
						player.sendRawMessage(ChatColor.YELLOW + "16) Thrown Bomb (150% TNT force upon impact); Price: 5 money.");
						player.sendRawMessage(ChatColor.GOLD + "17) Flashbang (5 seconds of blindness); Price: 4 money.");
						player.sendRawMessage(ChatColor.YELLOW + "18) 10 Use Blowdart Gun (\"Knocks out\" a player 15 seconds); Price: 10 money.");
						player.sendRawMessage(ChatColor.GOLD + "19) Flash Resistant Goggles (infinite uses); Price: 8");
						player.sendRawMessage(ChatColor.YELLOW + "20) Antidote (\"wakes you up\"); Price: Price: 2");
					}
					return true;
				}
				else if (args[0].equalsIgnoreCase("balance")) {

					player.sendRawMessage(ChatColor.GREEN + "You have " + assassinations.get(playerID) + " money.");
					return true;
				}
				else if (args[0].equalsIgnoreCase("enchant")) {

					if (args.length >= 3) {

						int level;
						if (StringUtils.isNumeric(args[2]) && (level = Integer.parseInt(args[2])) > 0 && level <= 5) {

							int money = assassinations.get(playerID);
							if (money >= level) {

								if (args[1].equalsIgnoreCase("Sharpness")) {

									assassinations.put(playerID, money-level);
									player.getItemInHand().addUnsafeEnchantment(Enchantment.DAMAGE_ALL, level);
									player.sendRawMessage(ChatColor.GREEN + "You enchanted your" + player.getItemInHand().getType().toString() + " with Sharpness " + level + ".");
								}
								else if (args[1].equalsIgnoreCase("Power")) {

									assassinations.put(playerID, money-level);
									player.getItemInHand().addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, level);
									player.sendRawMessage(ChatColor.GREEN + "You enchanted your" + player.getItemInHand().getType().toString() + " with Power " + level + ".");
								}
								else if (level <= 4) {

									if (args[1].equalsIgnoreCase("Protection")) {

										assassinations.put(playerID, money-level);
										player.getItemInHand().addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, level);
										player.sendRawMessage(ChatColor.GREEN + "You enchanted your" + player.getItemInHand().getType().toString() + " with Protection " + level + ".");
									}
									else if (args[1].equalsIgnoreCase("Feather Falling")) {

										assassinations.put(playerID, money-level);
										player.getItemInHand().addUnsafeEnchantment(Enchantment.PROTECTION_FALL, level);
										player.sendRawMessage(ChatColor.GREEN + "You enchanted your" + player.getItemInHand().getType().toString() + " with Feather Falling " + level + ".");
									}
									else {
										player.sendRawMessage(ChatColor.RED + "Please enter a valid enchantment.");
									}
								}
								else {
									player.sendRawMessage(ChatColor.RED + "Please enter a valid enchantment + level combination.");
								}
							}
							else {
								player.sendRawMessage(ChatColor.RED + "You cannot afford this enchantment.");
							}
						}
						else {
							player.sendRawMessage(ChatColor.RED + "Please enter a valid enchantment level.");
						}
					}
					else {
						player.sendRawMessage(ChatColor.GREEN + "You have " + assassinations.get(playerID) + " money.");
						player.sendRawMessage(ChatColor.BLUE + "To enchant, do /shop enchant [enchantment (level)].");
						player.sendRawMessage(ChatColor.GOLD + "Sharpness; Price = level(1-5).");
						player.sendRawMessage(ChatColor.YELLOW + "Power (bow); Price = level(1-5).");
						player.sendRawMessage(ChatColor.GOLD + "Protection; Price = level(1-4).");
						player.sendRawMessage(ChatColor.YELLOW + "Feather Falling; Price = level(1-4).");
					}
					return true;
				}
			}
			else if (cmdName.equals("enderchest")) {

				player.openInventory(player.getEnderChest());
				return true;
			}
			else if (cmdName.equals("play")) {

				if (!playingPlayers.contains(playerID)) {

					if (spawnpoints.size() > 0) {

						Random rand = new Random();
						int startIndex = rand.nextInt(spawnpoints.size());
						for (int i = startIndex; i < spawnpoints.size(); i++) {

							if (teleportPlayerSafely(player, spawnpoints.get(i))) {

								givePlayerStartingGear(player);
								player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
								player.removePotionEffect(PotionEffectType.WEAKNESS);
								player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 5*20, 255));
								return true;
							}
						}
						for (int i = 0; i < startIndex; i++) {

							if (teleportPlayerSafely(player, spawnpoints.get(i))) {

								givePlayerStartingGear(player);
								player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
								player.removePotionEffect(PotionEffectType.WEAKNESS);
								player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 5*20, 255));
								return true;
							}
						}
						player.sendRawMessage(ChatColor.RED + "Error, too many players suspected spawn camping.");
					}
					else {
						player.sendRawMessage(ChatColor.RED + "Error, please contact administrator.");
					}
				}
				else {
					player.sendRawMessage(ChatColor.RED + "You are already playing!");
				}
				return true;
			}
			else if (cmdName.equals("setwaypoint")) {

				spawnpoints.add(player.getLocation());
				return true;
			}
			else if (cmdName.equals("listwaypoints")) {

				for (int i = 0; i < spawnpoints.size(); i++) {

					player.sendRawMessage(ChatColor.GOLD + Integer.toString(i+1) + ") " + spawnpoints.get(i).toString());
				}
				return true;
			}
			else if (cmd.getName().equals("UnlockC") && args.length > 0) {

				if (securedPlayers.containsKey(playerID)) {
					if (!securedPlayers.get(playerID)) {
						if (args[0].equals(dataConfig.getConfig().getString("Secure." + playerID))) {
							securedPlayers.put(playerID, true);
							player.sendRawMessage(ChatColor.GREEN + "Your account has been successfully unlocked, have a nice day.");
						}
						else {
							player.sendRawMessage(ChatColor.RED + "Password not recognized, try again.");
						}
					}
					else {
						player.sendRawMessage(ChatColor.GREEN + "Your account will be unlocked until you log off, have a nice day.");
					}
				}
				else {
					player.sendRawMessage(ChatColor.GREEN + "Your account hasn't been listed in the Secured section, no worries! Have a nice day.");
				}
				return true;
			}
			else if (cmd.getName().equals("SSG") || cmd.getName().equals("SevenSuperGirls")) {

				Wolf wolf = (Wolf) player.getWorld().spawnEntity(player.getLocation(), EntityType.WOLF);
				wolf.playEffect(EntityEffect.WOLF_HEARTS);
				wolf.remove();
				return true;
			}
		}
		return false;
	}

	@Override
	public void onDisable() {

		for (Entry<UUID, Integer> entry : assassinations.entrySet()){
			dataConfig.getConfig().set("money." + entry.getKey().toString(), entry.getValue());
		}

		ArrayList<String> spawnpoint = new ArrayList<String>();
		for (Location location : spawnpoints) {

			spawnpoint.add(location.getX() + "|" + location.getY() + "|" + location.getZ());
		}
		dataConfig.getConfig().set("spawnpoints", spawnpoint);

		ArrayList<String> playingPlayer = new ArrayList<String>();
		for (UUID playerID : playingPlayers) {

			playingPlayer.add(playerID.toString());
		}
		dataConfig.getConfig().set("playing", playingPlayer);

		dataConfig.saveConfig();
		//		assassinations.clear();
	}
}
