package kr.dja.aldarEconomy.tracker.chest;

import java.util.UUID;

public class DestroyChestResultMember
{
	public final UUID player;
	public final int discountAmount;
	
	DestroyChestResultMember(UUID player, int discountAmount)
	{
		this.player = player;
		this.discountAmount = discountAmount;
	}

}
