package kr.dja.aldarEconomy.entity.itemEntity;

import java.util.UUID;

import kr.dja.aldarEconomy.entity.DependType;
import kr.dja.aldarEconomy.entity.Wallet;

public class ItemWallet extends Wallet<UUID>
{
	public final UUID itemUID;
	public final DependType ownerType;
	
	ItemWallet(UUID itemUID, UUID ownerUID, DependType ownerType)
	{
		super(ownerUID);
		this.itemUID = itemUID;
		this.ownerType = ownerType;
		
	}
	
	void setMoney(int money)
	{
		this.money = money;
	}
}
