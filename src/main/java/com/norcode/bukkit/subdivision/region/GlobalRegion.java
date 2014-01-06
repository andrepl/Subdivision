package com.norcode.bukkit.subdivision.region;

import com.norcode.bukkit.subdivision.SubdivisionPlugin;
import com.norcode.bukkit.subdivision.datastore.RegionData;
import com.norcode.bukkit.subdivision.flag.Flag;
import com.norcode.bukkit.subdivision.flag.perm.PermissionFlag;
import com.norcode.bukkit.subdivision.flag.perm.RegionPermissionState;
import com.norcode.bukkit.subdivision.rtree.Bounds;
import com.norcode.bukkit.subdivision.rtree.Node;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class GlobalRegion extends Region {

	private Node rtree;
	private UUID worldId;

	public GlobalRegion(SubdivisionPlugin plugin, UUID worldId) {
		super(plugin, new RegionData(0, 0, 0, 0, 0, 0,
				    	worldId, null, worldId,
						new HashSet<UUID>(),
						new HashSet<UUID>(),
						new HashMap<String,String>()));
		World world = plugin.getServer().getWorld(worldId);
		rtree = Node.create();
		ConfigurationSection cfg = plugin.getConfig().getConfigurationSection("flag-defaults.global-defaults." + world.getName().toLowerCase());
		if (cfg == null) {
			cfg = plugin.getConfig().createSection("flag-defaults.global-defaults." + world.getName().toLowerCase());
		}
		for (Flag f: Flag.getAllFlags()) {
			if (f instanceof PermissionFlag) {
				RegionPermissionState state = plugin.getRegionManager().getUniversalPermissionFlag(f);
				RegionPermissionState wState = ((PermissionFlag) f).parseValue(cfg.getString(f.getName().toLowerCase()));
				if (wState == null || wState == RegionPermissionState.INHERIT) {
					setFlag(f, wState);
				} else {
					setFlag(f, state);
				}
			}
		}
	}

	public Region removeRegion(Region region) {
		return rtree.remove(region);
	}

	public List<Region> search(Bounds bounds) {
		return rtree.search(bounds);
	}

	public void addRegion(Region region) {
		rtree.insert(region);
	}

	public boolean hasParent() {
		return false;
	}

	public Region getParent() {
		return this;
	}

	public boolean allows(PermissionFlag flag, Player player) {
		RegionPermissionState state = flag.getValue(flags.get(flag));
		switch (state) {
			case OWNERS: return isOwner(player);
			case MEMBERS: return isMember(player);
		}
		return true;
	}
}
