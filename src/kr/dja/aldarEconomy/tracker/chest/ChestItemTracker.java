package kr.dja.aldarEconomy.tracker.chest;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import kr.dja.aldarEconomy.EconomyUtil;
import kr.dja.aldarEconomy.setting.MoneyMetadata;

public class ChestItemTracker
{// 창고에서 뱉는 아이템 추적
	private final Plugin plugin;
	private final EconomyUtil util;
	
	private Queue<Integer> breakChestMoneyQueue;
	private Stack<MoneyItemInfo> breakChestItemStack;
	private int breakChestMoneyRemain;
	private final Runnable breakChestItemNextTick;
	private boolean hasChestItemTask;

	
	public ChestItemTracker(Plugin plugin, EconomyUtil util)
	{
		this.plugin = plugin;
		this.util = util;
		this.breakChestMoneyQueue = new LinkedList<>();
		this.breakChestItemStack = new Stack<>();
		this.breakChestItemNextTick = this::nextTick;
		this.hasChestItemTask = false;
	}

	private void nextTick()
	{
		
		Bukkit.getServer().broadcastMessage("nextTick");
		this.hasChestItemTask = false;
		//this.breakChestMoneyQueue.clear();
	}
	
	public void onChestBreak(int discountAmount)
	{
		this.breakChestMoneyQueue.add(discountAmount);
		this.breakChestMoneyRemain = 0;
		if(!this.hasChestItemTask)
		{
			Bukkit.getScheduler().runTask(this.plugin, this.breakChestItemNextTick);
			this.hasChestItemTask = true;
		}
	}

	public void onChestBreakItemSpawn(Item item, MoneyMetadata moneyMeta)
	{
		ItemStack itemStack = item.getItemStack();
		if(this.breakChestMoneyQueue.isEmpty()) return;
		
		if(this.breakChestMoneyRemain == 0)
		{
			this.breakChestMoneyRemain = this.breakChestMoneyQueue.poll();
			
			//Bukkit.getServer().broadcastMessage("꺼냈다!!" + remain + " " + beforeThread.equals(Thread.currentThread()));
		}
		int amount = moneyMeta.value * itemStack.getAmount();
		this.breakChestMoneyRemain -= amount;
		this.breakChestItemStack.add(new MoneyItemInfo(item, amount, moneyMeta));
		Bukkit.getServer().broadcastMessage("itemdrop!!");
		if(this.breakChestMoneyRemain == 0)
		{
			Bukkit.getServer().broadcastMessage("다찾았다!!" + this.breakChestMoneyQueue.size());
			for(MoneyItemInfo info : this.breakChestItemStack)
			{
				Bukkit.getServer().broadcastMessage(String.format("item:%s, type:%s, amount:%s", info.item.getUniqueId(), info.moneyMeta.name, info.amount));
			}
			this.breakChestItemStack.clear();
		}
	}
}
