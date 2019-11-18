package kr.dja.aldarEconomy.api;

import java.util.HashMap;
import java.util.Map;

public class APITokenManager
{
	private Map<String, SystemID> idTokenMap;
	
	public APITokenManager()
	{
		this.idTokenMap = new HashMap<>();
	}
	
	public SystemID registerIDToken(String name)
	{
		SystemID idToken = new SystemID(name);
		this.idTokenMap.put(name, idToken);
		return idToken;
	}
	
	public SystemID getIDToken(String id)
	{
		SystemID idToken = this.idTokenMap.getOrDefault(id, null);
		if(idToken == null)
		{
			idToken = new SystemID(id);
		}
		return idToken;
	}

}
