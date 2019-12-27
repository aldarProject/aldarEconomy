package kr.dja.aldarEconomy.tracker.item;

import org.bukkit.entity.Item;

import kr.dja.aldarEconomy.coininfo.CoinMetadata;

public class MoneyItemInfo
{
	public final Item item;
	public final int amount;
	public final CoinMetadata moneyMeta;
	
	public MoneyItemInfo(Item item, int amount, CoinMetadata moneyMeta)
	{
		this.item = item;
		this.amount = amount;
		this.moneyMeta = moneyMeta;
	}

}
