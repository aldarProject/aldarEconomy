package kr.dja.aldarEconomy.tracker.chest;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DestroyChestResult
{
	public final int totalAmount;
	public final List<DestroyChestResultMember> members;
	
	DestroyChestResult(int totalAmount, List<DestroyChestResultMember> members)
	{
		this.totalAmount = totalAmount;
		this.members = Collections.unmodifiableList(members);
	}

}
