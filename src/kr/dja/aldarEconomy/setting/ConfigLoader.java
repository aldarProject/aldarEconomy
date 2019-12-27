package kr.dja.aldarEconomy.setting;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigLoader
{
	private final JavaPlugin plugin;
	private final Logger logger;
	private final FileConfiguration config;
	
	private final Map<String, ConfigurationMember> module;
	
	public ConfigLoader(JavaPlugin plugin, Logger logger)
	{
		this.plugin = plugin;
		this.logger = logger;
		this.config = plugin.getConfig();
		this.module = new HashMap<>();
	}
	
	public void registerModule(String key, ConfigurationMember module)
	{
		this.module.put(key, module);
	}
	
	public void saveConfig()
	{
		for(String key : this.module.keySet())
		{
			ConfigurationMember module = this.module.get(key);
			this.config.set(key, module.serialize());
		}
		
		this.plugin.saveConfig();
	}
	
	public void loadConfig()
	{
		for(String key : this.module.keySet())
		{
			ConfigurationMember module = this.module.get(key);
			logger.log(Level.INFO, "[config]"+key+" load...");
			try
			{
				ConfigurationSection section = this.config.getConfigurationSection(key);
				if(module.installData(section)) continue;
			} catch(Exception e) {}
			logger.log(Level.WARNING, "[config]"+key+" load fail");
		}
		this.saveConfig();
	}

	@Override
	public String toString()
	{
		StringBuffer strbuf = new StringBuffer();
		for(String key : this.module.keySet())
		{
			ConfigurationMember module = this.module.get(key);
			strbuf.append(key+":\n");
			strbuf.append(module.toString()+"\n");
		}
		
		return strbuf.toString();
	}
}
