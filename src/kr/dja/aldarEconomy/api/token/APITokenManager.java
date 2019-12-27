package kr.dja.aldarEconomy.api.token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import kr.dja.aldarEconomy.setting.ConfigurationMember;

public class APITokenManager implements ConfigurationMember
{
	public static final SystemID SYSTEM_TOKEN = new SystemID(UUID.fromString("44f47040-23e3-11ea-aaef-0800200c9a66"), "main");
	private Map<UUID, SystemID> _idTokenMap;
	
	public APITokenManager()
	{
		this._idTokenMap = new HashMap<>();
		this._idTokenMap.put(SYSTEM_TOKEN.uuid, SYSTEM_TOKEN);
	}
	
	public synchronized SystemID takeOrRegisterAPIToken(String name)
	{
		for(SystemID id : this._idTokenMap.values())
		{
			if(id.name.equals(name))
			{
				return id;
			}
		}
		SystemID id = new SystemID(UUID.randomUUID(), name);
		this._idTokenMap.put(id.uuid, id);
		return id;
	}
	
	public synchronized SystemID getIDToken(UUID id)
	{
		SystemID idToken = this._idTokenMap.getOrDefault(id, null);
		return idToken;
	}

	@Override
	public synchronized Map<String, Object> serialize()
	{
		Map<String, Object> map = new HashMap<>();
		List<Map<String, Object>> maplist = new ArrayList<Map<String,Object>>();
		for(SystemID id : this._idTokenMap.values())
		{
			maplist.add(id.serialize());
		}
		map.put("tokens", maplist);
		return map;
	}

	@Override
	public synchronized boolean installData(ConfigurationSection section)
	{
		try
		{
			List<SystemID> datalist = new ArrayList<>();
			List<?> configlist = (List<?>) section.get("tokens");
			for(Object map : configlist)
			{
				@SuppressWarnings("unchecked")
				SystemID id = SystemID.deserialize((Map<String, Object>) map);
				datalist.add(id);
			}
			this._idTokenMap.clear();
			this._idTokenMap.put(SYSTEM_TOKEN.uuid, SYSTEM_TOKEN);
			Set<String> nameSet = new HashSet<>();
			Set<UUID> uidSet = new HashSet<>();
			nameSet.add(SYSTEM_TOKEN.name);
			uidSet.add(SYSTEM_TOKEN.uuid);
			for(SystemID id : datalist)
			{
				if(nameSet.contains(id.name)) continue;
				if(uidSet.contains(id.uuid)) continue;
				nameSet.add(id.name);
				uidSet.add(id.uuid);
				this._idTokenMap.put(id.uuid, id);
			}
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	@Override
	public synchronized String toString()
	{
		StringBuffer buf = new StringBuffer();
		for(SystemID id : this._idTokenMap.values())
		{
			buf.append(id.toString()+"\n");
		}
		return buf.toString();
	}
	

}
