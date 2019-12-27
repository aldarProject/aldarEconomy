package kr.dja.aldarEconomy.coininfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import kr.dja.aldarEconomy.setting.ConfigurationMember;

public class CoinInfo implements ConfigurationMember
{
	private static final List<CoinMetadata> dftMoneyMetadataInfo;
	static
	{
		dftMoneyMetadataInfo = new ArrayList<>();
		ItemStack money10 = new ItemStack(Material.IRON_INGOT, 1);
		ItemStack money100 = new ItemStack(Material.GOLD_INGOT, 1);
		ItemStack money1000 = new ItemStack(Material.DIAMOND, 1);
		
		dftMoneyMetadataInfo.add(new CoinMetadata("iron", money10, 10));
		dftMoneyMetadataInfo.add(new CoinMetadata("gold", money100, 100));
		dftMoneyMetadataInfo.add(new CoinMetadata("dia", money1000, 1000));
	}
	

	private final List<CoinMetadata> _moneyList;
	public final List<CoinMetadata> moneyList;
	public final int count;

	public CoinInfo()
	{
		Collections.sort(dftMoneyMetadataInfo);
		this.count = dftMoneyMetadataInfo.size();
		this._moneyList = new ArrayList<>();
		this.moneyList = Collections.unmodifiableList(this._moneyList);
		this._moneyList.addAll(dftMoneyMetadataInfo);
	}


	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> map = new HashMap<>();
		List<Map<String, Object>> moneyMetaMapList = new ArrayList<Map<String,Object>>();
		for(CoinMetadata moneyMeta : this.moneyList)
		{
			moneyMetaMapList.add(moneyMeta.serialize());
		}
		map.put("coins", moneyMetaMapList);
		return map;
	}
	
	@Override
	public boolean installData(ConfigurationSection section)
	{
		try
		{
			List<CoinMetadata> moneyMetaList = new ArrayList<>();
			List<?> moneyMetaObjList = (List<?>) section.get("coins");
			for(Object moneyMetaMap : moneyMetaObjList)
			{
				@SuppressWarnings("unchecked")
				CoinMetadata moneyMeta = CoinMetadata.deserialize((Map<String, Object>) moneyMetaMap);
				moneyMetaList.add(moneyMeta);
			}
			Collections.sort(moneyMetaList);
			this._moneyList.clear();
			this._moneyList.addAll(moneyMetaList);
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		for(CoinMetadata meta : this.moneyList)
		{
			buf.append("----------\n");
			buf.append(meta.toString());
			buf.append("\n");
		}
		return buf.toString();
	}
	
}
