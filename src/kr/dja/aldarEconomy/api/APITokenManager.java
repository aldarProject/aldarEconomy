package kr.dja.aldarEconomy.api;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class APITokenManager
{
	public static final SystemID SYSTEM_TOKEN = new SystemID(UUID.fromString("44f47040-23e3-11ea-aaef-0800200c9a66"), "main");
	private Map<UUID, SystemID> idTokenMap;
	
	public APITokenManager()
	{
		this.idTokenMap = new HashMap<>();
		this.idTokenMap.put(SYSTEM_TOKEN.uuid, SYSTEM_TOKEN);
	}
	
	public SystemID registerIDToken(UUID id, String name)
	{
		SystemID idToken = new SystemID(id, name);
		this.idTokenMap.put(id, idToken);
		return idToken;
	}
	
	public SystemID getIDToken(UUID id)
	{
		SystemID idToken = this.idTokenMap.getOrDefault(id, null);
		return idToken;
	}

}
