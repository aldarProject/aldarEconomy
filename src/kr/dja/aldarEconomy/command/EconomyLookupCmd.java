package kr.dja.aldarEconomy.command;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;

import kr.dja.aldarEconomy.data.EconomyDataStorage;
import kr.dja.aldarEconomy.dataObject.Wallet;
import kr.dja.aldarEconomy.dataObject.chest.ChestEconomyChild;
import kr.dja.aldarEconomy.dataObject.chest.ChestWallet;
import kr.dja.aldarEconomy.dataObject.itemEntity.ItemEconomyChild;
import kr.dja.aldarEconomy.dataObject.itemEntity.ItemWallet;

public class EconomyLookupCmd implements CommandExecutor, TabCompleter
{
	public static final String PLAYER_MONEY_CMD = "money";
	public static final String PLAYER_MONEYDETAIL_CMD = "moneydetail";
	private final EconomyDataStorage storage;
	
	EconomyLookupCmd(EconomyDataStorage storage)
	{
		this.storage = storage;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		switch(cmd.getName())
		{
		case PLAYER_MONEY_CMD:
		{
			HumanEntity player = CommandUtil.getPlayer(sender, args, 1);
			if(player == null) return false;
			UUID id = player.getUniqueId();
			long money = this.storage.getPlayerMoney(id);
			player.sendMessage(String.format("%s님의 돈은 %d", player.getName(), money));
			break;
		}
		case PLAYER_MONEYDETAIL_CMD:
		{
			HumanEntity player = CommandUtil.getPlayer(sender, args, 1);
			if(player == null) return false;
			UUID id = player.getUniqueId();
			long money = this.storage.getPlayerMoney(id);
			long playerInvMoney = this.storage.playerDependEconomy.getMoney(id);
			long chestMoney = 0;
			long itemMoney = 0;
			for(ChestEconomyChild child : this.storage.chestDependEconomy.childSet)
			{
				ChestWallet w = child.eMap.get(id);
				if(w == null) continue;
				chestMoney += w.getMoney();
			}
			for(ItemEconomyChild child : this.storage.itemEconomyStorage.eMap.values())
			{
				ItemWallet w = child.eMap.get(id);
				if(w == null) continue;
				itemMoney += w.getMoney();
			}
			int enderChestMoney = this.storage.playerEnderChestEconomy.getMoney(id);
			player.sendMessage(String.format("%s님의 돈은 total:%d inv:%d chest:%d item:%d enderChest:%d", player.getName(), money, playerInvMoney, chestMoney, itemMoney, enderChestMoney));
			break;
		}
			
		}
		
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
	{
		switch(cmd.getName())
		{
		case PLAYER_MONEY_CMD:
		case PLAYER_MONEYDETAIL_CMD:
			return CommandUtil.tabCompletePlayer(1, args);
		}
		return null;
	}

}
