package kr.dja.aldarEconomy.setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigLoader
{
	public static final String KEY_MONEYMETA = "moneyMetadata";
	
	public static final String KEY_DBADDR = "dbAddr";
	public static final String KEY_DBID = "dbID";
	public static final String KEY_DBPW = "dbPassword";
	
	private final JavaPlugin plugin;
	private final FileConfiguration config;
	
	private MoneyInfo moneyInfo;
	
	public ConfigLoader(JavaPlugin plugin)
	{
		this.plugin = plugin;
		this.config = plugin.getConfig();
		this.loadConfig();
		this.moneyInfo = new MoneyInfo();
	}
	
	public void saveConfig()
	{
		List<Map<String, Object>> moneyMetaMapList = new ArrayList<Map<String,Object>>();
		for(MoneyMetadata moneyMeta : this.moneyInfo.moneyList)
		{
			moneyMetaMapList.add(moneyMeta.serialize());
		}
		this.config.set(KEY_MONEYMETA, moneyMetaMapList);
		
		this.plugin.saveConfig();
	}
	
	public void loadConfig()
	{
		
		List<MoneyMetadata> moneyMetaList = new ArrayList<>();
		try
		{
			List<?> moneyMetaObjList = this.config.getList(KEY_MONEYMETA);
			for(Object moneyMetaMap : moneyMetaObjList)
			{
				@SuppressWarnings("unchecked")
				MoneyMetadata moneyMeta = MoneyMetadata.deserialize((Map<String, Object>) moneyMetaMap);
				
				moneyMetaList.add(moneyMeta);
			}
		}
		catch(Exception e)
		{
			moneyMetaList = null;
			this.plugin.getLogger().log(Level.WARNING, "moneymeta load error", e);
			
		}
		
		if(moneyMetaList != null && moneyMetaList.size() != 0)
			this.moneyInfo = new MoneyInfo(moneyMetaList);
		this.saveConfig();
	}
	
	public MoneyInfo getMoneyInfo()
	{
		return this.moneyInfo;
	}
	
	@Override
	public String toString()
	{
		StringBuffer strbuf = new StringBuffer();
		strbuf.append("MoneyMetadata:\n");
		for(MoneyMetadata moneyMeta : this.moneyInfo.moneyList)
		{
			strbuf.append("------\n"+moneyMeta+"\n");
		}
		return strbuf.toString();
	}
}
