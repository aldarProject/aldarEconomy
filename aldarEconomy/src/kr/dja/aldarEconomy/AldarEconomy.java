package kr.dja.aldarEconomy;


import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import kr.dja.aldarEconomy.dataStorage.EconomyDataStorage;
import kr.dja.aldarEconomy.economyTracker.ChestTracker;
import kr.dja.aldarEconomy.eventListener.EventListener;
import kr.dja.aldarEconomy.setting.ConfigLoader;

public class AldarEconomy extends JavaPlugin
{
	public static final String SYSTEM_ID = "EconomyMaster";
	
	private String version;
	private PluginManager pluginManager;
	private ConfigLoader configLoader;
	private ConstraintChecker constraintChecker;
	private EconomyDataStorage storage;
	private EventListener eventListener;
	private ChestTracker chestTracker;
	
	
	
	@Override
	public void onEnable()
	{
		this.configLoader = new ConfigLoader(this);
		
		this.constraintChecker = new ConstraintChecker(this.configLoader.getMoneyInfo());
		this.version = this.getDescription().getVersion();
		this.storage = new EconomyDataStorage(this.configLoader.getMoneyInfo(), null, this.getLogger());
		this.chestTracker = new ChestTracker(this.constraintChecker, this.storage, this.getLogger());
		this.eventListener = new EventListener(this.constraintChecker, this.chestTracker, this.getLogger());

		this.pluginManager = this.getServer().getPluginManager();
	

		this.getLogger().info("\n"+this.configLoader.toString());
		this.pluginManager.registerEvents(this.eventListener, this);
		
		this.getLogger().info("Aldar Economy"+version+" enabled by camelCase");
	}
	
	@Override
	public void onDisable(){
		this.getLogger().info("Aldar Economy"+version+" disabled by camelCase");
	}
	
	

}
