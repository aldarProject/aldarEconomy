package kr.dja.aldarEconomy.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import kr.dja.aldarEconomy.EconomyUtil;
import net.minecraft.server.v1_12_R1.BlockState;

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
	
	
	public static Container getTargetChest(CommandSender sender)
	{
		if(!(sender instanceof LivingEntity))
		{
			sender.sendMessage("명령 실패: 잘못된 시전자.");
			return null;
		}
		Block b = ((LivingEntity)sender).getTargetBlock(null, 100);
		if(!(b != null && b.getState() instanceof Container))
		{
			sender.sendMessage("명령 실패: 창고가 아닙니다. " + b.getType());
			return null;
		}
		
		return (Container)b.getState();
	}
	
	public static Location getTargetingLocation(CommandSender sender)
	{
		if(!(sender instanceof LivingEntity))
		{
			sender.sendMessage("명령 실패: 잘못된 시전자.");
			return null;
		}
		Block b = ((LivingEntity)sender).getTargetBlock(null, 100);
		if(b.getType() == Material.AIR)
		{
			sender.sendMessage("명령 실패: 잘못된 위치.");
			return null;
		}
		Location loc = b.getLocation();
		loc.setY(loc.getY() + 1);
		return loc;
		
	}
	
	public static int getNumber(String[] args, int index)
	{
		int amount = -1;
		try
		{
			amount = Integer.parseInt(args[0]);
		}
		catch (NumberFormatException e)
		{
		}
		return amount;
	}
}
