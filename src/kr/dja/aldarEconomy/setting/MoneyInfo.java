package kr.dja.aldarEconomy.setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MoneyInfo
{
	public static final List<MoneyMetadata> dftMoneyMetadataInfo;
	static
	{
		dftMoneyMetadataInfo = new ArrayList<>();
		ItemStack money10 = new ItemStack(Material.IRON_INGOT, 1);
		ItemStack money100 = new ItemStack(Material.GOLD_INGOT, 1);
		ItemStack money1000 = new ItemStack(Material.DIAMOND, 1);
		
		dftMoneyMetadataInfo.add(new MoneyMetadata("iron", money10, 10));
		dftMoneyMetadataInfo.add(new MoneyMetadata("gold", money100, 100));
		dftMoneyMetadataInfo.add(new MoneyMetadata("dia", money1000, 1000));
	}
	

	public final List<MoneyMetadata> moneyList;
	public final int count;

	MoneyInfo(List<MoneyMetadata> moneyMetadataInfo)
	{
		Collections.sort(moneyMetadataInfo);
		this.count = moneyMetadataInfo.size();
		this.moneyList = Collections.unmodifiableList(moneyMetadataInfo);
	}
}
