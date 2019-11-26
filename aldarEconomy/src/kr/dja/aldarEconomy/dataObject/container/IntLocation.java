package kr.dja.aldarEconomy.dataObject.container;

import org.bukkit.Location;

public class IntLocation
{
	public final String world;
	public final int x, y, z;
	public IntLocation(String world, int x, int y, int z)
	{
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public IntLocation(Location l)
	{
		this.world = l.getWorld().getName();
		this.x = l.getBlockX();
		this.y = l.getBlockY();
		this.z = l.getBlockZ();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(!(obj instanceof IntLocation)) return false;
		IntLocation target = (IntLocation)obj;
		if(target.x != this.x) return false;
		if(target.y != this.y) return false;
		if(target.z != this.z) return false;
		return true;
	}
	
	@Override
	public int hashCode()
	{
		int hashCode = this.world.hashCode();
		hashCode = 31 * hashCode + this.x;
		hashCode = 31 * hashCode + this.y;
		hashCode = 31 * hashCode + this.z;
		return hashCode;
	}
	
	@Override
	public String toString()
	{
		return String.format("w:%s x:%d y:%d z:%d", this.world, this.x, this.y, this.z);
	}

}
