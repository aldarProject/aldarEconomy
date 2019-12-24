package kr.dja.aldarEconomy;


import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import kr.dja.aldarEconomy.api.APITokenManager;
import kr.dja.aldarEconomy.command.CommandManager;
import kr.dja.aldarEconomy.data.EconomyDataStorage;
import kr.dja.aldarEconomy.eventListener.EventListener;
import kr.dja.aldarEconomy.setting.ConfigLoader;
import kr.dja.aldarEconomy.tracker.chest.ChestTracker;
import kr.dja.aldarEconomy.tracker.item.ItemTracker;
import kr.dja.aldarEconomy.trade.Bank;
import kr.dja.aldarEconomy.trade.TradeTracker;

public class AldarEconomy extends JavaPlugin
{
	public static final String SYSTEM_ID = "EconomyMaster";
	
	private Logger logger;
	private String version;
	private APITokenManager apiTokenManager;
	private PluginManager pluginManager;
	private ConfigLoader configLoader;
	private EconomyUtil util;
	private EconomyDataStorage storage;
	private EventListener eventListener;
	private TradeTracker tradeTracker;
	private ChestTracker chestTracker;
	private ItemTracker itemTracker;
	private Bank bank;
	
	private CommandManager commandManager;
	
	
	@Override
	public void onEnable()
	{
		this.logger = this.getLogger();
		this.configLoader = new ConfigLoader(this);
		this.version = this.getDescription().getVersion();
		
		this.apiTokenManager = new APITokenManager();
		this.util = new EconomyUtil(this.configLoader.getMoneyInfo());
		
		this.storage = new EconomyDataStorage(this.configLoader.getMoneyInfo(), this.logger, "aldarDefault");
		this.tradeTracker = new TradeTracker(this.logger, this.apiTokenManager);
		this.itemTracker = new ItemTracker(this, this.util, this.storage.itemEconomyStorage, this.storage.playerDependEconomy, this.tradeTracker, this.logger);
		this.chestTracker = new ChestTracker(this.itemTracker, this.util, this.storage.chestDependEconomy, this.storage.playerDependEconomy, this.storage.playerEnderChestEconomy, this.tradeTracker, this.logger);
		this.eventListener = new EventListener(this.util, this.chestTracker, this.itemTracker, this.logger);
		this.bank = new Bank(this.configLoader.getMoneyInfo(), this.util, this.storage, this.chestTracker, this.tradeTracker);
		
		this.pluginManager = this.getServer().getPluginManager();
		

		this.logger.info("\n"+this.configLoader.toString());
		this.pluginManager.registerEvents(this.eventListener, this);
		
		this.commandManager = new CommandManager(this, this.storage, this.bank);
		
		this.logger.info("Aldar Economy"+version+" enabled by camelCase");
	}
	
	@Override
	public void onDisable()
	{
		this.logger.info("Aldar Economy"+version+" disabled by camelCase");
	}
	
	

}