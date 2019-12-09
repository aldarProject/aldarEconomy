package kr.dja.aldarEconomy.command;

import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import kr.dja.aldarEconomy.data.EconomyDataStorage;

public class CommandManager
{
	private final JavaPlugin plugin;
	private final EconomyLookupCommand economyCmd;
	
	public CommandManager(JavaPlugin plugin, EconomyDataStorage storage)
	{
		this.plugin = plugin;
		this.economyCmd = new EconomyLookupCommand(storage);
		PluginCommand cmd;
		cmd = this.plugin.getCommand(EconomyLookupCommand.PLAYER_MONEY_CMD);
		cmd.setExecutor(this.economyCmd);
		cmd.setTabCompleter(this.economyCmd);
		cmd = this.plugin.getCommand(EconomyLookupCommand.PLAYER_MONEYDETAIL_CMD);
		cmd.setExecutor(this.economyCmd);
		cmd.setTabCompleter(this.economyCmd);
	}

}
