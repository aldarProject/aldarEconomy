package kr.dja.aldarEconomy.command;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;

import kr.dja.aldarEconomy.api.APITokenManager;
import kr.dja.aldarEconomy.bank.Bank;
import kr.dja.aldarEconomy.bank.BankActionData;
import kr.dja.aldarEconomy.bank.BankActionResult;

public class EconomyBankCmd implements CommandExecutor, TabCompleter
{
	private final Bank bank;
	public static final String ADMIN_CONSUME_PLAYERMONEY_CMD = "moneyconsume";
	public static final String ADMIN_ISSUANCE_PLAYERMONEY_CMD = "moneyissuance";
	EconomyBankCmd(Bank bank)
	{
		this.bank = bank;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
	{
		switch(cmd.getName())
		{
		case ADMIN_CONSUME_PLAYERMONEY_CMD:
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
			int amount = 0;
			try
			{
				amount = Integer.parseInt(args[0]);
			}
			catch (NumberFormatException e)
			{
				return false;
			}
			BankActionData result = this.bank.consumeMoneyPlayer(APITokenManager.SYSTEM_TOKEN, "command", player, amount);
			if(result.result != BankActionResult.OK)
			{
				sender.sendMessage("명령 실패: " + result.result);
				return true;
			}
			result.execute();
			sender.sendMessage("명령 성공: " + player.getName() + "의 돈을 " + amount + "만큼 감소시킵니다.");
			break;
		}
		case ADMIN_ISSUANCE_PLAYERMONEY_CMD:
		{
			if(args.length < 1) return false;
			HumanEntity player = CommandUtil.getPlayer(sender, args, 2);
			if(player == null) return false;
			int amount = 0;
			try
			{
				amount = Integer.parseInt(args[0]);
			}
			catch (NumberFormatException e)
			{
				return false;
			}
			BankActionData result = this.bank.issuanceMoneyPlayer(APITokenManager.SYSTEM_TOKEN, "command", player, amount);
			if(result.result != BankActionResult.OK)
			{
				sender.sendMessage("명령 실패: " + result.result);
				return true;
			}
			result.execute();
			sender.sendMessage("명령 성공: " + player.getName() + "의 돈을 " + amount + "만큼 증가시킵니다.");
		}
		
		}
		return true;
	}
	

}
