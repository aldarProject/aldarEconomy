package kr.dja.aldarEconomy.command;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import kr.dja.aldarEconomy.EconomyUtil;
import kr.dja.aldarEconomy.api.APITokenManager;
import kr.dja.aldarEconomy.bank.Bank;
import kr.dja.aldarEconomy.bank.BankActionResult;
import kr.dja.aldarEconomy.dataObject.IntLocation;

public class EconomyBankCmd implements CommandExecutor, TabCompleter
{
	
	public static final String ADMIN_CONSUME_PLAYERMONEY_CMD = "moneyconsume";
	public static final String ADMIN_ISSUANCE_PLAYERMONEY_CMD = "moneyissuance";
	public static final String ADMIN_CONSUME_CHESTMONEY_CMD = "moneychestconsume";
	public static final String ADMIN_ISSUANCE_CHESTMONEY_CMD = "moneychestissuance";
	public static final String ADMIN_ISSUANCE_ITEMMONEY_CMD = "moneyitemissuance";
	public static final String ADMIN_MOVE_PLAYER_TO_CHEST = "moneyp2c";
	public static final String ADMIN_MOVE_CHEST_TO_PLAYER = "moneyc2p";
	
	private final Bank bank;
	private final EconomyUtil util;
	
	EconomyBankCmd(Bank bank, EconomyUtil util)
	{
		this.bank = bank;
		this.util = util;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
	{
		switch(cmd.getName())
		{
		case ADMIN_CONSUME_PLAYERMONEY_CMD:
		case ADMIN_ISSUANCE_PLAYERMONEY_CMD:
			return CommandUtil.tabCompletePlayer(1, args);
		}
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		switch(cmd.getName())
		{
		case ADMIN_CONSUME_PLAYERMONEY_CMD:
		{
			if(args.length < 1) return false;
			HumanEntity player = CommandUtil.getPlayer(sender, args, 2);
			if(player == null) return false;
			int amount = CommandUtil.getNumber(args, 0);
			if(amount <= 0) return false;
			BankActionResult result = this.bank.consumeFromPlayer(APITokenManager.SYSTEM_TOKEN, player, amount, "command", null);
			if(result != BankActionResult.OK)
			{
				sender.sendMessage("명령 실패: " + result);
				return true;
			}
			sender.sendMessage("명령 성공: " + player.getName() + "의 돈을 " + amount + "만큼 감소시킵니다.");
			break;
		}
		case ADMIN_ISSUANCE_PLAYERMONEY_CMD:
		{
			if(args.length < 1) return false;
			HumanEntity player = CommandUtil.getPlayer(sender, args, 2);
			if(player == null) return false;
			int amount = CommandUtil.getNumber(args, 0);
			if(amount <= 0) return false;
			BankActionResult result = this.bank.issuanceToPlayer(APITokenManager.SYSTEM_TOKEN, player, amount, "command", null);
			if(result != BankActionResult.OK)
			{
				sender.sendMessage("명령 실패: " + result);
				return true;
			}
			sender.sendMessage("명령 성공: " + player.getName() + "의 돈을 " + amount + "만큼 증가시킵니다.");
			break;
		}
		case ADMIN_CONSUME_CHESTMONEY_CMD:
		{
			if(args.length < 1) return false;
			Inventory i = CommandUtil.getTargetChest(this.util, sender);
			if(i == null) return true;
			int amount = CommandUtil.getNumber(args, 0);
			if(amount <= 0) return false;
			BankActionResult result = this.bank.consumeFromChest(APITokenManager.SYSTEM_TOKEN, i, amount, "command", null);
			if(result != BankActionResult.OK)
			{
				sender.sendMessage("명령 실패: " + result);
				return true;
			}
			sender.sendMessage("명령 성공: "+new IntLocation(i.getLocation())+"위치 창고의 돈을 "+amount+"만큼 감소합니다.");
			break;
		}
			
		case ADMIN_ISSUANCE_CHESTMONEY_CMD:
		{
			if(args.length < 1) return false;
			Inventory i = CommandUtil.getTargetChest(this.util, sender);
			if(i == null) return true;
			int amount = CommandUtil.getNumber(args, 0);
			if(amount <= 0) return false;
			BankActionResult result = this.bank.issuanceToChest(APITokenManager.SYSTEM_TOKEN, i, amount, "command", null);
			if(result != BankActionResult.OK)
			{
				sender.sendMessage("명령 실패: " + result);
				return true;
			}
			sender.sendMessage("명령 성공: "+new IntLocation(i.getLocation())+"위치 창고의 돈을 "+amount+"만큼 증가시킵니다.");
			break;
		}
		case ADMIN_ISSUANCE_ITEMMONEY_CMD:
		{
			if(args.length < 1) return false;
			Location loc = CommandUtil.getTargetingLocation(sender);
			if(loc == null) return true;
			int amount = CommandUtil.getNumber(args, 0);
			if(amount <= 0) return false;
			BankActionResult result = this.bank.issuanceToItem(APITokenManager.SYSTEM_TOKEN, loc, amount, "command", null);
			if(result != BankActionResult.OK)
			{
				sender.sendMessage("명령 실패: " + result);
				return true;
			}
			sender.sendMessage("명령 성공: "+new IntLocation(loc)+"에 돈을 "+amount+"만큼 아이템으로 발급합니다.");
			break;
		}
		
		case ADMIN_MOVE_PLAYER_TO_CHEST:
		{
			if(args.length < 1) return false;
			Inventory i = CommandUtil.getTargetChest(this.util, sender);
			if(i == null) return true;
			HumanEntity player = CommandUtil.getPlayer(sender, args, 2);
			int amount = CommandUtil.getNumber(args, 0);
			if(amount <= 0) return false;
			BankActionResult result = this.bank.movePlayerMoneyToChest(player, i, amount);
			if(result != BankActionResult.OK)
			{
				sender.sendMessage("명령 실패: " + result);
				return true;
			}
			sender.sendMessage("명령 성공: "+player.getName()+"의 돈을 "+new IntLocation(i.getLocation())+"에게 "+amount+"만큼 이동합니다.");
			break;
		}
		case ADMIN_MOVE_CHEST_TO_PLAYER:
		{
			if(args.length < 1) return false;
			Inventory i = CommandUtil.getTargetChest(this.util, sender);
			if(i == null) return true;
			HumanEntity player = CommandUtil.getPlayer(sender, args, 2);
			int amount = CommandUtil.getNumber(args, 0);
			if(amount <= 0) return false;
			BankActionResult result = this.bank.moveChestMoneyToPlayer(i, player, amount);
			if(result != BankActionResult.OK)
			{
				sender.sendMessage("명령 실패: " + result);
				return true;
			}
			sender.sendMessage("명령 성공: "+new IntLocation(i.getLocation())+"의 돈을 "+player.getName()+"에게 "+amount+"만큼 이동합니다.");
		}
		}
		return true;
	}


}
