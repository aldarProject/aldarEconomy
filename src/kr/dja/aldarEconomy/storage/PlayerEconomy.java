package kr.dja.aldarEconomy.storage;

import java.util.UUID;

public class PlayerEconomy
{
	public final UUID playerUUID;
	long money;
	
	PlayerEconomy(UUID playerUUID)
	{
		this.playerUUID = playerUUID;
	}
	
	public long getMoney()
	{
		return this.money;
	}
}
