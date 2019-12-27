package kr.dja.aldarEconomy.api.token;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class SystemID implements ConfigurationSerializable
{
	public final UUID uuid;
	public final String name;
	
	SystemID(UUID uuid, String name)
	{
		this.uuid = uuid;
		this.name = name;
	}
	
	@Override
	public int hashCode()
	{
	    int result = 1;
	    result = 31 * uuid.hashCode();
	    return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(super.equals(obj))
		{
			if(obj instanceof SystemID)
			{
				SystemID id = (SystemID)obj;
				if(id.uuid.equals(this.uuid)) return true;
			}
		}
		return false;
	}
	
	public static SystemID deserialize(Map<String, Object> args)
	{
		String name = (String) args.get("name");
		UUID uuid = UUID.fromString((String)args.get("uuid"));
		
		return new SystemID(uuid, name);
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> map = new HashMap<>();
		map.put("name", this.name);
		map.put("uuid", this.uuid.toString());
		return map;
	}
	
	@Override
	public String toString()
	{
		return "name:"+this.name+" uuid:"+this.uuid;
	}
}
