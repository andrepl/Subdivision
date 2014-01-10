package com.norcode.bukkit.subdivision.listener;

import com.norcode.bukkit.subdivision.SubdivisionPlugin;
import com.norcode.bukkit.subdivision.flag.perm.BuildingFlag;
import com.norcode.bukkit.subdivision.flag.perm.ButtonsFlag;
import com.norcode.bukkit.subdivision.flag.perm.ContainersFlag;
import com.norcode.bukkit.subdivision.flag.perm.FarmingFlag;
import com.norcode.bukkit.subdivision.flag.perm.PVPFlag;
import com.norcode.bukkit.subdivision.flag.prot.ExplosionFlag;
import com.norcode.bukkit.subdivision.flag.prot.PistonFlag;
import com.norcode.bukkit.subdivision.flag.prot.RegionProtectionState;
import com.norcode.bukkit.subdivision.region.GlobalRegion;
import com.norcode.bukkit.subdivision.region.Region;
import com.norcode.bukkit.subdivision.rtree.Bounds;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class ProtectionListener implements Listener {

	private SubdivisionPlugin plugin;

	private static EnumSet<EntityType> ANIMALS = EnumSet.of(
			EntityType.CHICKEN, EntityType.COW, EntityType.HORSE, EntityType.MUSHROOM_COW,
			EntityType.OCELOT, EntityType.WOLF, EntityType.PIG, EntityType.SHEEP, EntityType.VILLAGER);

	private static HashSet<PotionEffectType> HARMFUL_POTIONS = new HashSet<PotionEffectType>();
	static {
		HARMFUL_POTIONS.add(PotionEffectType.POISON);
		HARMFUL_POTIONS.add(PotionEffectType.BLINDNESS);
		HARMFUL_POTIONS.add(PotionEffectType.CONFUSION);
		HARMFUL_POTIONS.add(PotionEffectType.HARM);
		HARMFUL_POTIONS.add(PotionEffectType.SLOW);
		HARMFUL_POTIONS.add(PotionEffectType.SLOW_DIGGING);
		HARMFUL_POTIONS.add(PotionEffectType.HUNGER);
		HARMFUL_POTIONS.add(PotionEffectType.WEAKNESS);
		HARMFUL_POTIONS.add(PotionEffectType.WITHER);
	}

	public static EnumSet<Material> BUTTONS = EnumSet.of(
		Material.STONE_BUTTON, Material.WOOD_BUTTON, Material.LEVER, Material.DIODE_BLOCK_OFF,
		Material.DIODE_BLOCK_ON, Material.REDSTONE_COMPARATOR_OFF, Material.REDSTONE_COMPARATOR_ON,
		Material.BEACON);

	public static EnumSet<Material> ALLOWED_FARMING_BLOCKBREAKS = EnumSet.of(
		Material.COCOA, Material.MELON_BLOCK, Material.MELON_STEM, Material.PUMPKIN_STEM, Material.PUMPKIN,
		Material.LONG_GRASS, Material.RED_MUSHROOM, Material.BROWN_MUSHROOM, Material.HUGE_MUSHROOM_2,
		Material.HUGE_MUSHROOM_1, Material.SUGAR_CANE_BLOCK, Material.CROPS, Material.CACTUS, Material.POTATO,
		Material.CARROT, Material.WHEAT, Material.NETHER_STALK
	);

	public static EnumSet<Material> ALLOWED_FARMING_BLOCKPLACES = ALLOWED_FARMING_BLOCKBREAKS.clone();
	static {
		ALLOWED_FARMING_BLOCKPLACES.remove(Material.PUMPKIN);
		ALLOWED_FARMING_BLOCKPLACES.remove(Material.MELON_BLOCK);
	}


	public ProtectionListener(SubdivisionPlugin plugin) {
		this.plugin = plugin;
	}


	private boolean isButton(Block clickedBlock) {
		return BUTTONS.contains(clickedBlock.getType());
	}

	private boolean isBreakingCrop(Material blockType) {
		return ALLOWED_FARMING_BLOCKBREAKS.contains(blockType);
	}

	public boolean isPlacingCrop(Material blockType) {
		return ALLOWED_FARMING_BLOCKPLACES.contains(blockType);
	}

	private boolean isAnimal(EntityType entityType) {
		return ANIMALS.contains(entityType);
	}

	@EventHandler
	public void onPistonExtend(BlockPistonExtendEvent event) {
		Location loc1 = event.getBlocks().get(0).getLocation();
		Location loc2 = event.getBlocks().get(event.getBlocks().size()-1).getLocation();
		Bounds blockBounds = new Bounds(loc1.getBlockX(), loc1.getBlockY(), loc1.getBlockZ(),
										loc2.getBlockX(), loc2.getBlockY(), loc2.getBlockZ());
		List<Region> regions = plugin.getRegionManager().getGlobalRegion(event.getBlock().getWorld().getUID()).search(blockBounds);
		if (regions.size() > 0) {
			Region pistonRegion = plugin.getRegionManager().getRegion(event.getBlock().getLocation());
			for (Region r: regions) {
				if (PistonFlag.flag.get(r) == RegionProtectionState.DENY) {
					if (!r.equals(pistonRegion)) {
						event.setCancelled(true);
						return;
					}
				}
			}
		}
	}

	@EventHandler
	public void onPistonRetract(BlockPistonRetractEvent event) {
		Region blockRegion = plugin.getRegionManager().getRegion(event.getBlock().getLocation());
		Region retractRegion = plugin.getRegionManager().getRegion(event.getRetractLocation());
		if (!(blockRegion instanceof GlobalRegion)) {
			if (!retractRegion.equals(blockRegion)) {
				if (PistonFlag.flag.get(blockRegion) == RegionProtectionState.DENY) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void onPlayerOpenInventory(InventoryOpenEvent event) {
		if (event.getInventory().getHolder() != null && event.getInventory().getHolder() instanceof BlockState) {
			Location loc = ((BlockState) event.getInventory().getHolder()).getLocation();
			Region rs = plugin.getRegionManager().getRegion(loc);
			if (!rs.allows(ContainersFlag.flag, (Player) event.getPlayer())) {
				event.setCancelled(true);
				((Player)event.getPlayer()).sendMessage(ChatColor.RED +
						"You do not have permission to open containers here.");
			}
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void onPlayerBreakBlock(BlockBreakEvent event) {
		Player p = event.getPlayer();
		Material blockType = event.getBlock().getType();
		Region r = plugin.getRegionManager().getRegion(event.getBlock().getLocation());
		if (r.allows(BuildingFlag.flag, p) ||
				(r.allows(FarmingFlag.flag, p) && isBreakingCrop(blockType))) {
			return;
		} else {
			event.setCancelled(true);
			p.sendMessage(ChatColor.RED + "You don't have permission to break blocks here.");
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void onPlayerPlaceBlock(BlockPlaceEvent event) {
		Player p = event.getPlayer();
		Material blockType = event.getBlock().getType();
		Region r = plugin.getRegionManager().getRegion(event.getBlock().getLocation());
		if (r.allows(BuildingFlag.flag, p) ||
				(r.allows(FarmingFlag.flag, p) && isPlacingCrop(blockType))) {
			return;
		} else {
			event.setCancelled(true);
			p.sendMessage(ChatColor.RED + "You don't have permission to place blocks here.");
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		Player attacker = null;

		if (event.getDamager() instanceof Player) {
			attacker = (Player) event.getDamager();
		} else if (event.getDamager() instanceof Projectile) {
			if (((Projectile) event.getDamager()).getShooter() instanceof Player) {
				attacker = (Player) ((Projectile) event.getDamager()).getShooter();
			}
		}
		Region r = plugin.getRegionManager().getRegion(event.getEntity().getLocation());
		if (attacker != null) {
			if (event.getEntityType() == EntityType.PLAYER) {
				if (!r.allows(PVPFlag.flag, attacker)) {
					event.setCancelled(true);
					attacker.sendMessage(ChatColor.RED + "You do not have PVP permissions here!");
				}
			} else if (isAnimal(event.getEntityType())) {
				if (event.getEntity() instanceof Tameable) {
					Tameable t = (Tameable) event.getEntity();
					// Wolves and Ocelots are protected by the farming flag if tamed,
					// but not if it's the animal's owner doing the attacking... that is allowed.
					// we protected horses tamed or not.
					if (event.getEntityType() == EntityType.WOLF || event.getEntityType() == EntityType.OCELOT) {
						if ((t.getOwner() != null && t.getOwner().getName().equals(attacker.getName())) || !t.isTamed()) {
							return;
						}
					}

				}
				if (!r.allows(FarmingFlag.flag, attacker)) {
					event.setCancelled(true);
					attacker.sendMessage(ChatColor.RED + "You do not have Farming permissions here!");
				}
			}
		}
	}

	@EventHandler(priority= EventPriority.HIGH, ignoreCancelled=true)
	public void onEntityExplode(EntityExplodeEvent event) {
		for (Block b: event.blockList()) {
			Region r = plugin.getRegionManager().getRegion(b.getLocation());
			if (ExplosionFlag.flag.get(r) == RegionProtectionState.DENY) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority=EventPriority.HIGH, ignoreCancelled = true)
	public void onHangingBreak(HangingBreakEvent event) {
		Entity remover = null;
		Region region = plugin.getRegionManager().getRegion(event.getEntity().getLocation());
		if (event instanceof HangingBreakByEntityEvent) {
			HangingBreakByEntityEvent e = (HangingBreakByEntityEvent) event;
			remover = e.getRemover();
			if (remover instanceof Projectile) {
				remover = ((Projectile) remover).getShooter();
			}
			if (remover instanceof Player) {
				Player player = (Player) remover;
				if (!region.allows(BuildingFlag.flag, player)) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onPotionSplash(PotionSplashEvent event) {

		boolean isHarmful = false;
		boolean hitPlayer = false;

		Entity thrower = event.getPotion().getShooter();

		if (thrower instanceof Player) {
			Iterator<LivingEntity> it = event.getAffectedEntities().iterator();
			while (it.hasNext()) {
				LivingEntity e = it.next();
				if (e instanceof Player && !e.equals(thrower)) {
					Region r = plugin.getRegionManager().getRegion(e.getLocation());
					if (!r.allows(PVPFlag.flag, (Player) thrower)) {
						event.setIntensity(e, 0);
					}
				}
			}
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (isButton(event.getClickedBlock())) {
			Region r = plugin.getRegionManager().getRegion(event.getClickedBlock().getLocation());
			if (!r.allows(ButtonsFlag.flag, event.getPlayer())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onHangingPlace(HangingPlaceEvent event) {
		Block hangingSpace = event.getEntity().getLocation().getBlock();
		if (event.getEntity() instanceof Painting) {
			int h = ((Painting) event.getEntity()).getArt().getBlockHeight();
			int w = ((Painting) event.getEntity()).getArt().getBlockWidth();
			List<Location> locs = new ArrayList<Location>();
			for (int y=hangingSpace.getY(); y < hangingSpace.getY()+h; y++) {
				switch (event.getBlockFace()) {
				case NORTH:
				case SOUTH:
					int x1 = Math.min(hangingSpace.getX(), hangingSpace.getX()+w);
					int x2 = Math.max(hangingSpace.getX(), hangingSpace.getX()+w);
					for (int x=x1;x<x2;x++) {
						locs.add(new Location(hangingSpace.getWorld(), x, y, hangingSpace.getZ()));
					}
					break;
				case EAST:
				case WEST:
					int z1 = Math.min(hangingSpace.getZ(), hangingSpace.getZ() + w);
					int z2 = Math.max(hangingSpace.getZ(), hangingSpace.getZ() + w);
					for (int z=z1;z<z2;z++) {
						locs.add(new Location(hangingSpace.getWorld(), hangingSpace.getX(), y, z));
					}
					break;
				}
				for (Location l: locs) {
					Region r = plugin.getRegionManager().getRegion(l);
					if (!r.allows(BuildingFlag.flag, event.getPlayer())) {
						event.setCancelled(true);
					}
				}
			}

		}
	}
}