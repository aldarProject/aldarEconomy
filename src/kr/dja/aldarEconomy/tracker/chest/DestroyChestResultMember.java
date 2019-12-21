package kr.dja.aldarEconomy.tracker.chest;

import java.util.UUID;

import kr.dja.aldarEconomy.dataObject.DependType;

public class DestroyChestResultMember
{
	public final UUID owner;
	public final DependType type;
	public final int discountAmount;
	
	DestroyChestResultMember(UUID owner, DependType type, int discountAmount)
	{
		this.owner = owner;
		this.type = type;
		this.discountAmount = discountAmount;
	}

}
