package kr.dja.aldarEconomy.coininfo;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

public class CoinMetadata implements ConfigurationSerializable, Comparable<CoinMetadata>
{
	public final String name;
	public final ItemStack itemStack;
	public final int value;
	public final int maxStack;
	
	CoinMetadata(String name, ItemStack itemStack, int value)
	{
		this.name = name;
		this.itemStack = itemStack;
		this.value = value;
		this.maxStack = this.itemStack.getMaxStackSize();
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		map.put("name", this.name);
		map.put("itemStack", this.itemStack.serialize());
		map.put("value", this.value);
		return map;
	}
	
	@Override
	public String toString()
	{
		StringBuffer strbuf = new StringBuffer();
		strbuf.append("name: " + name + "\n");
		strbuf.append("itemStack: " + itemStack.toString() + "\n");
		strbuf.append("value: " + value);
		return strbuf.toString();
		
	}

	public static CoinMetadata deserialize(Map<String, Object> args)
	{
		String name = (String) args.get("name");
		
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) args.get("itemStack");
		ItemStack itemStack = ItemStack.deserialize(map);
		
		int value = (Integer) args.get("value");
		
		return new CoinMetadata(name, itemStack, value);
	}
	
	@Override
	public int compareTo(CoinMetadata o)
	{
		return this.value - o.value;
	}
	
}
