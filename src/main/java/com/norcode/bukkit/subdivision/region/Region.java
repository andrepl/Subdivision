package com.norcode.bukkit.subdivision.region;


import com.norcode.bukkit.subdivision.SubdivisionPlugin;
import com.norcode.bukkit.subdivision.datastore.DatastoreException;
import com.norcode.bukkit.subdivision.datastore.RegionData;
import com.norcode.bukkit.subdivision.flag.Flag;
import com.norcode.bukkit.subdivision.flag.perm.PermissionFlag;
import com.norcode.bukkit.subdivision.flag.perm.RegionPermissionState;
import com.norcode.bukkit.subdivision.rtree.Bounded;
import com.norcode.bukkit.subdivision.rtree.Bounds;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Region implements Bounded {
	private SubdivisionPlugin plugin;
	private int minX;
	private int minY;
	private int minZ;
	private int maxX;
	private int maxY;
	private int maxZ;
	private UUID id;
	private UUID worldId;
	private UUID parentId;
	private Set<UUID> owners;
	private Set<UUID> members;
	protected Map<Flag, Object> flags = new HashMap<Flag, Object>();

	public Region(SubdivisionPlugin plugin, RegionData data) {
		this.plugin = plugin;
		minX = data.getMinX();
		minY = data.getMinY();
		minZ = data.getMinZ();
		maxX = data.getMaxX();
		maxY = data.getMaxY();
		maxZ = data.getMaxZ();
		id = data.getId();
		parentId = data.getParentId();
		worldId = data.getWorldId();
		owners = new HashSet<UUID>(data.getOwners());
		members = new HashSet<UUID>(data.getMembers());
		for (String flagKey: data.getFlags().keySet()) {
			Flag flag = Flag.fromKey(flagKey);
			if (flag != null) {
				flags.put(flag, flag.parseValue(data.getFlags().get(flagKey)));
			}
		}
	}

	public void adjustBounds(Bounds bounds) {
		this.minX = bounds.getX1();
		this.minY = bounds.getY1();
		this.minZ = bounds.getZ1();
		this.maxX = bounds.getX2();
		this.maxY = bounds.getY2();
		this.maxZ = bounds.getZ2();
	}

	public int getWidth() {
		return 1+(this.maxX - this.minX);
	}

	public int getHeight() {
		return 1+(this.maxY - this.minY);
	}

	public int getLength() {
		return 1+(this.maxZ - this.minZ);
	}

	public int getArea() {
		return getWidth() * getLength();
	}


	public int getMinX() {
		return minX;
	}

	public int getMinY() {
		return minY;
	}

	public int getMinZ() {
		return minZ;
	}

	public int getMaxX() {
		return maxX;
	}

	public int getMaxY() {
		return maxY;
	}

	public int getMaxZ() {
		return maxZ;
	}

	public UUID getId() {
		return id;
	}

	public UUID getWorldId() {
		return worldId;
	}

	public UUID getParentId() {
		return parentId;
	}

	@Override
	public Bounds getBounds() {
		return new Bounds(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public boolean equals(Region other) {
		if (other instanceof Region) {
			Region otherRegion = (Region) other;
			return otherRegion.id == this.id;
		}
		return false;
	}

	public void addOwner(Player player) {
		owners.add(player.getUniqueId());
	}

	public RegionData getRegionData() {
		HashMap<String, String> serializedFlags = new HashMap<String, String>();
		for (Flag f: flags.keySet()) {
			serializedFlags.put(f.getName(), f.serializeValue(flags.get(f)));
		}
		return new RegionData(minX, minY, minZ, maxX, maxY, maxZ, id, parentId, worldId, owners, members, serializedFlags);
	}
	public boolean hasParent() {
		return parentId != null;
	}

	public boolean hasOwners() {
		return owners.size() > 0;
	}

	public boolean hasMembers() {
		return members.size() > 0;
	}

	public boolean isMember(Player player) {
		return members.contains(player.getUniqueId()) || owners.contains(player.getUniqueId());
	}

	public boolean isOwner(Player player) {
		return owners.contains(player.getUniqueId());
	}

	public boolean contains(Location loc) {
		if (!loc.getWorld().getUID().equals(getWorldId())) return false;
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		return minX <= x && maxX >= x &&
				minY <= y && maxY >= y &&
				minZ <= z && maxZ >= z;

	}

	public void setFlag(Flag flag, Object value) {
		flags.put(flag, value);
		plugin.getRegionManager().invalidateCache(this);
		try {
			plugin.getDatastore().saveRegion(getRegionData());
		} catch (DatastoreException e) {
			e.printStackTrace();
		}
	}

	public boolean allows(PermissionFlag flag, Player player) {
		RegionPermissionState state = flag.get(this);
		Region check = null;
		if (state == RegionPermissionState.INHERIT) {
			return getParent().allows(flag, player);
		}
		switch (state) {
			case OWNERS: return isOwner(player);
			case MEMBERS: return isMember(player);
		}
		return true;
	}

	public Set<UUID> getOwnerIds() {
		return owners;
	}

	public Set<UUID> getMemberIds() {
		return members;
	}

	public Object getFlag(Flag f) {
		return flags.get(f);
	}

	public Region getParent() {
		if (this.hasParent()) {
			return plugin.getRegionManager().getById(this.parentId);
		}
		return null;
	}
}