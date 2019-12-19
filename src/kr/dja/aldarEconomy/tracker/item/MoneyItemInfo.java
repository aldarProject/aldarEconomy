package kr.dja.aldarEconomy.tracker.item;

import org.bukkit.entity.Item;

import kr.dja.aldarEconomy.setting.MoneyMetadata;

public class MoneyItemInfo
{
	public final Item item;
	public final int amount;
	public final MoneyMetadata moneyMeta;
	
	public MoneyItemInfo(Item item, int amount, MoneyMetadata moneyMeta)
	{
		this.item = item;
		this.amount = amount;
		this.moneyMeta = moneyMeta;
	}

}
