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
import kr.dja.aldarEconomy.IntLocation;
import kr.dja.aldarEconomy.api.AldarEconomyProvider;
import kr.dja.aldarEconomy.api.EconomyResult;
import kr.dja.aldarEconomy.api.token.APITokenManager;
import kr.dja.aldarEconomy.bank.Bank;
import kr.dja.aldarEconomy.bank.EconomyActionResult;

public class EconomyBankCmd implements CommandExecutor, TabCompleter
{
	
	public static final String ADMIN_CONSUME_PLAYERMONEY_CMD = "moneyconsume";
	public static final String ADMIN_ISSUANCE_PLAYERMONEY_CMD = "moneyissuance";
	public static final String ADMIN_CONSUME_CHESTMONEY_CMD = "moneychestconsume";
	public static final String ADMIN_ISSUANCE_CHESTMONEY_CMD = "moneychestissuance";
	public static final String ADMIN_ISSUANCE_ITEMMONEY_CMD = "moneyitemissuance";
	public static final String ADMIN_MOVE_PLAYER_TO_CHEST = "moneyp2c";
	public static final String ADMIN_MOVE_CHEST_TO_PLAYER = "moneyc2p";
	public static final String ADMIN_MOVE_PLAYER_TO_PLAYER = "moneyp2p";
	
	private static final String CMD_CAUSE = "CMD";
	
	private final AldarEconomyProvider provider;

	EconomyBankCmd(AldarEconomyProvider provider)
	{
		this.provider = provider;
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
			EconomyResult result = this.provider.withdrawPlayer(player, amount, APITokenManager.SYSTEM_TOKEN, CMD_CAUSE, null);
			if(result != EconomyResult.OK)
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
			EconomyResult result = this.provider.depositPlayer(player, amount, APITokenManager.SYSTEM_TOKEN, CMD_CAUSE, null);
			if(result != EconomyResult.OK)
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
			Container c = CommandUtil.getTargetChest(sender);
			if(c == null) return true;
			int amount = CommandUtil.getNumber(args, 0);
			EconomyResult result = this.provider.withdrawChest(c, amount, APITokenManager.SYSTEM_TOKEN, CMD_CAUSE, null);
			if(result != EconomyResult.OK)
			{
				sender.sendMessage("명령 실패: " + result);
				return true;
			}
			sender.sendMessage("명령 성공: "+new IntLocation(c.getLocation())+"위치 창고의 돈을 "+amount+"만큼 감소합니다.");
			break;
		}
			
		case ADMIN_ISSUANCE_CHESTMONEY_CMD:
		{
			if(args.length < 1) return false;
			Container c = CommandUtil.getTargetChest(sender);
			if(c == null) return true;
			int amount = CommandUtil.getNumber(args, 0);
			EconomyResult result = this.provider.withdrawChest(c, amount, APITokenManager.SYSTEM_TOKEN, CMD_CAUSE, null);
			if(result != EconomyResult.OK)
			{
				sender.sendMessage("명령 실패: " + result);
				return true;
			}
			sender.sendMessage("명령 성공: "+new IntLocation(c.getLocation())+"위치 창고의 돈을 "+amount+"만큼 증가시킵니다.");
			break;
		}
		case ADMIN_ISSUANCE_ITEMMONEY_CMD:
		{
			if(args.length < 1) return false;
			Location loc = CommandUtil.getTargetingLocation(sender);
			if(loc == null) return true;
			int amount = CommandUtil.getNumber(args, 0);
			EconomyResult result = this.provider.depositItem(loc, amount, APITokenManager.SYSTEM_TOKEN, CMD_CAUSE, null);
			if(result != EconomyResult.OK)
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
			Container c = CommandUtil.getTargetChest(sender);
			if(c == null) return true;
			HumanEntity player = CommandUtil.getPlayer(sender, args, 2);
			int amount = CommandUtil.getNumber(args, 0);
			EconomyResult result = this.provider.playerToChest(player, c, amount, APITokenManager.SYSTEM_TOKEN, CMD_CAUSE, null);
			if(result != EconomyResult.OK)
			{
				sender.sendMessage("명령 실패: " + result);
				return true;
			}
			sender.sendMessage("명령 성공: "+player.getName()+"의 돈을 "+new IntLocation(c.getLocation())+"에게 "+amount+"만큼 이동합니다.");
			break;
		}
		case ADMIN_MOVE_CHEST_TO_PLAYER:
		{
			if(args.length < 1) return false;
			Container c = CommandUtil.getTargetChest(sender);
			if(c == null) return true;
			HumanEntity player = CommandUtil.getPlayer(sender, args, 2);
			int amount = CommandUtil.getNumber(args, 0);
			EconomyResult result = this.provider.chestToPlayer(c, player, amount, APITokenManager.SYSTEM_TOKEN, CMD_CAUSE, null);
			if(result != EconomyResult.OK)
			{
				sender.sendMessage("명령 실패: " + result);
				return true;
			}
			sender.sendMessage("명령 성공: "+new IntLocation(c.getLocation())+"의 돈을 "+player.getName()+"에게 "+amount+"만큼 이동합니다.");
		}
		case ADMIN_MOVE_PLAYER_TO_PLAYER:
		{
			if(args.length < 2) return false;
			HumanEntity target = CommandUtil.getPlayer(sender, args, 2);
			if(target == null) return false;
			HumanEntity source = CommandUtil.getPlayer(sender, args, 3);
			if(source == null) return false;
			int amount = CommandUtil.getNumber(args, 0);
			if(target.getUniqueId().equals(source.getUniqueId()))
			{
				sender.sendMessage("명령 실패: 소스와 타깃이 같습니다.");
				return true;
			}
			
			EconomyResult result = this.provider.playerToPlayer(source, target, amount, APITokenManager.SYSTEM_TOKEN, CMD_CAUSE, null);
			if(result != EconomyResult.OK)
			{
				sender.sendMessage("명령 실패: " + result);
				return true;
			}
			sender.sendMessage("명령 성공: "+source.getName()+"의 돈을 "+target.getName()+"에게 "+amount+"만큼 이동합니다.");
			break;
		}
		}
		return true;
	}


}
