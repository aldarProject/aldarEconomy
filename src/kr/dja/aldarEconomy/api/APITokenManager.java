package kr.dja.aldarEconomy.api;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class APITokenManager
{
	private Map<UUID, SystemID> idTokenMap;
	
	public APITokenManager()
	{
		this.idTokenMap = new HashMap<>();
	}
	
	public SystemID registerIDToken(UUID id)
	{
		SystemID idToken = new SystemID(id);
		this.idTokenMap.put(id, idToken);
		return idToken;
	}
	
	public SystemID getIDToken(UUID id)
	{
		SystemID idToken = this.idTokenMap.getOrDefault(id, null);
		if(idToken == null)
		{
			idToken = new SystemID(id);
		}
		return idToken;
	}

}
