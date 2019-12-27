package kr.dja.aldarEconomy.command;

import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import kr.dja.aldarEconomy.EconomyUtil;
import kr.dja.aldarEconomy.api.AldarEconomyProvider;
import kr.dja.aldarEconomy.bank.Bank;
import kr.dja.aldarEconomy.data.EconomyDataStorage;

public class CommandManager
{
	private final JavaPlugin plugin;
	private final EconomyLookupCmd lookupCmd;
	private final EconomyBankCmd bankCmd;
	
	public CommandManager(JavaPlugin plugin, EconomyDataStorage storage, AldarEconomyProvider provider)
	{
		this.plugin = plugin;
		this.lookupCmd = new EconomyLookupCmd(storage);
		this.bankCmd = new EconomyBankCmd(provider);
		
		PluginCommand cmd;
		
		cmd = this.plugin.getCommand(EconomyLookupCmd.PLAYER_MONEY_CMD);
		cmd.setExecutor(this.lookupCmd);
		cmd.setTabCompleter(this.lookupCmd);
		cmd = this.plugin.getCommand(EconomyLookupCmd.PLAYER_MONEYDETAIL_CMD);
		cmd.setExecutor(this.lookupCmd);
		cmd.setTabCompleter(this.lookupCmd);
		
		cmd = this.plugin.getCommand(EconomyBankCmd.ADMIN_ISSUANCE_PLAYERMONEY_CMD);
		cmd.setExecutor(this.bankCmd);
		cmd.setTabCompleter(this.bankCmd);
		cmd = this.plugin.getCommand(EconomyBankCmd.ADMIN_CONSUME_PLAYERMONEY_CMD);
		cmd.setExecutor(this.bankCmd);
		cmd.setTabCompleter(this.bankCmd);
		cmd = this.plugin.getCommand(EconomyBankCmd.ADMIN_ISSUANCE_CHESTMONEY_CMD);
		cmd.setExecutor(this.bankCmd);
		cmd.setTabCompleter(this.bankCmd);
		cmd = this.plugin.getCommand(EconomyBankCmd.ADMIN_CONSUME_CHESTMONEY_CMD);
		cmd.setExecutor(this.bankCmd);
		cmd.setTabCompleter(this.bankCmd);
		cmd = this.plugin.getCommand(EconomyBankCmd.ADMIN_ISSUANCE_ITEMMONEY_CMD);
		cmd.setExecutor(this.bankCmd);
		cmd.setTabCompleter(this.bankCmd);
		cmd = this.plugin.getCommand(EconomyBankCmd.ADMIN_MOVE_CHEST_TO_PLAYER);
		cmd.setExecutor(this.bankCmd);
		cmd.setTabCompleter(this.bankCmd);
		cmd = this.plugin.getCommand(EconomyBankCmd.ADMIN_MOVE_PLAYER_TO_CHEST);
		cmd.setExecutor(this.bankCmd);
		cmd.setTabCompleter(this.bankCmd);
		cmd = this.plugin.getCommand(EconomyBankCmd.ADMIN_MOVE_PLAYER_TO_PLAYER);
		cmd.setExecutor(this.bankCmd);
		cmd.setTabCompleter(this.bankCmd);
	}

}
