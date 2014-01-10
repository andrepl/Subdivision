package com.norcode.bukkit.subdivision;

import com.norcode.bukkit.metalcore.MetalCorePlugin;
import com.norcode.bukkit.subdivision.command.DebugCommand;
import com.norcode.bukkit.subdivision.command.RegionCommand;
import com.norcode.bukkit.subdivision.command.SelectCommand;
import com.norcode.bukkit.subdivision.datastore.Datastore;
import com.norcode.bukkit.subdivision.datastore.DatastoreException;
import com.norcode.bukkit.subdivision.flag.Flag;
import com.norcode.bukkit.subdivision.listener.PlayerListener;
import com.norcode.bukkit.subdivision.listener.ProtectionListener;
import com.norcode.bukkit.subdivision.listener.SelectionListener;
import com.norcode.bukkit.subdivision.region.Region;
import com.norcode.bukkit.subdivision.region.RegionManager;
import com.norcode.bukkit.subdivision.selection.CuboidSelection;
import com.norcode.bukkit.subdivision.selection.RenderManager;
import com.norcode.bukkit.subdivision.selection.SelectionMode;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

public class SubdivisionPlugin extends MetalCorePlugin {

	private Datastore datastore;
	private RegionManager regionManager;

	private static boolean debugMode = false;

	// Commands
	private DebugCommand debugCommand;
	private RegionCommand regionCommand;
	private SelectCommand selectCommand;


	// Event Listeners
	private PlayerListener playerListener;
	private ProtectionListener protectionListener;
	private SelectionListener selectionListener;

	private HashMap<Flag, Object> universalFlagDefaults;
	private RenderManager renderManager;
	private BukkitTask renderTask;

	@Override
	public void onEnable() {

		regionManager = new RegionManager(this);

		if (!loadConfig()) {
			this.getServer().getPluginManager().disablePlugin(this);
		}
		setupEvents();
		if (!setupDatastore()) {
			this.getServer().getPluginManager().disablePlugin(this);
		}
		setupCommands();


	}

	private void setupEvents() {
		this.playerListener = new PlayerListener(this);
		this.selectionListener = new SelectionListener(this);
		this.protectionListener = new ProtectionListener(this);
		Flag.setupFlags(this);
	}

	private void setupCommands() {
		this.debugCommand = new DebugCommand(this);
		this.regionCommand = new RegionCommand(this);
		this.selectCommand = new SelectCommand(this);
	}

	private boolean loadConfig() {
		saveDefaultConfig();
		debugMode = getConfig().getBoolean("debug-mode", false);
		renderManager = new RenderManager(this);
		renderTask = getServer().getScheduler().runTaskTimer(this, renderManager, 1, 1);
		return true;
	}

	private boolean setupDatastore() {
		try {
			datastore = Datastore.create(this);
		} catch (DatastoreException e) {
			e.printStackTrace();
			return false;
		}
		return datastore.enable();
	}

	@Override
	public void onDisable() {
		datastore.disable();
		if (renderTask != null) {
			renderTask.cancel();
		}
	}

	public static void debug(String s) {
		if (debugMode) {
			Bukkit.getServer().getLogger().info(s);
		}
	}

	public Region getRegion(Player player) {
		return (Region) player.getMetadata(MetaKey.ACTIVE_REGION).get(0).value();
	}

	public RegionManager getRegionManager() {
		return regionManager;
	}

	public WorldEditPlugin getWorldEdit() {
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldEdit");
		if (plugin == null || !(plugin instanceof WorldEditPlugin))
			return null;
		return (WorldEditPlugin) plugin;
	}

	public CuboidSelection getPlayerSelection(Player player) {
		if (player.hasMetadata(MetaKey.SELECTION)) {
			return (CuboidSelection) player.getMetadata(MetaKey.SELECTION).get(0).value();
		}
		if (!player.hasMetadata(MetaKey.SELECTION_MODE)) {
			player.setMetadata(MetaKey.SELECTION_MODE, new FixedMetadataValue(this, SelectionMode.CUBOID));
		}
		SelectionMode mode = (SelectionMode) player.getMetadata(MetaKey.SELECTION_MODE).get(0).value();

		CuboidSelection sel = mode.createNewSelection();
		player.setMetadata(MetaKey.SELECTION, new FixedMetadataValue(this, sel));
		return sel;
	}

	public Region getFirstClaim(Player p) {
		UUID regionId = null;
		if (!p.hasMetadata(MetaKey.FIRST_CLAIM_ID)) {
			ConfigurationSection cfg = getPlayerData(p);
			String regionIdStr = cfg.getString(MetaKey.FIRST_CLAIM_ID);
			if (regionIdStr != null) {
				regionId = UUID.fromString(regionIdStr);
			}
			p.setMetadata(MetaKey.FIRST_CLAIM_ID, new FixedMetadataValue(this, regionId));
		}
		regionId = (UUID) p.getMetadata(MetaKey.FIRST_CLAIM_ID).get(0).value();
		if (regionId != null) {
			return regionManager.getById(regionId);
		}
		return null;
	}

	public void setFirstClaim(Player player, UUID regionId) {
		player.setMetadata(MetaKey.FIRST_CLAIM_ID, new FixedMetadataValue(this, regionId));
		ConfigurationSection cfg = getPlayerData(player);
		cfg.set(MetaKey.FIRST_CLAIM_ID, regionId);
	}

	public int getClaimAllowance(Player p) {
		if (!p.hasMetadata(MetaKey.CLAIM_ALLOWANCE)) {
			ConfigurationSection cfg = getPlayerData(p);
			int amt = cfg.getInt(MetaKey.CLAIM_ALLOWANCE, 225);
			p.setMetadata(MetaKey.CLAIM_ALLOWANCE, new FixedMetadataValue(this, amt));
		}
		return p.getMetadata(MetaKey.CLAIM_ALLOWANCE).get(0).asInt();
	}

	public void setClaimAllowance(Player p, int amt) {
		p.setMetadata(MetaKey.CLAIM_ALLOWANCE, new FixedMetadataValue(this, amt));
		ConfigurationSection cfg = getPlayerData(p);
		cfg.set(MetaKey.CLAIM_ALLOWANCE, amt);

	}

	public boolean hasFirstClaim(Player p) {
		return getFirstClaim(p) != null;
	}

	public Datastore getStore() {
		return datastore;
	}

	public RenderManager getRenderManager() {
		return renderManager;
	}

}

