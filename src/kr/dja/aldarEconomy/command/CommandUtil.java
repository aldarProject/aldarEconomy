package kr.dja.aldarEconomy.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;

public class CommandUtil
{
	public static HumanEntity getPlayer(CommandSender sender, String[] args, int index)
	{
		HumanEntity player = null;
		if(args.length == index - 1)
		{
			if(sender instanceof HumanEntity)
			{
				player = (HumanEntity)sender;
			}
		}
		else if(args.length > index - 1)
		{
			player = Bukkit.getPlayer(args[index - 1]);
		}
		return player;
	}
	
	public static List<String> tabCompletePlayer(int index, String[] args)
	{
		List<String> result = new ArrayList<>();
		if(args.length < index)
		{
			for(HumanEntity p : Bukkit.getOnlinePlayers())
			{
				result.add(p.getName());	
			}
		}
		else
		{
			for(HumanEntity p : Bukkit.getOnlinePlayers())
			{
				String name = p.getName();
				if(name.matches(args[index - 1]))
				{
					result.add(name);
				}
			}
		}
		return result;
	}
}
