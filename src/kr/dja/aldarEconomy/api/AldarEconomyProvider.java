package kr.dja.aldarEconomy.api;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import kr.dja.aldarEconomy.EconomyUtil;
import kr.dja.aldarEconomy.IntLocation;
import kr.dja.aldarEconomy.api.token.SystemID;
import kr.dja.aldarEconomy.bank.Bank;
import kr.dja.aldarEconomy.bank.EconomyActionResult;
import kr.dja.aldarEconomy.data.EconomyDataStorage;
import kr.dja.aldarEconomy.dataObject.chest.ChestEconomyChild;
import kr.dja.aldarEconomy.dataObject.chest.ChestWallet;
import kr.dja.aldarEconomy.dataObject.itemEntity.ItemEconomyChild;
import kr.dja.aldarEconomy.dataObject.itemEntity.ItemWallet;

public class AldarEconomyProvider implements AldarEconomy
{
	private final Plugin plugin;
	private final EconomyDataStorage storage;
	private final Bank bank;
	private final EconomyUtil util;
	
	private final BukkitScheduler scheduler;
	
	public AldarEconomyProvider(Plugin plugin, EconomyDataStorage storage, Bank bank, EconomyUtil util)
	{
		this.plugin = plugin;
		this.storage = storage;
		this.bank = bank;
		this.util = util;
		
		this.scheduler = Bukkit.getScheduler();
	}
	

	@Override
	public SystemID takeAPIToken(String id)
	{
		
		return null;
	}
	
	@Override
	public int getPlayerInventoryMoney(OfflinePlayer player)
	{
		if(player == null) return 0;
		return this.storage.playerDependEconomy.getMoney(player.getUniqueId());
	}

	@Override
	public MoneyDetailResult getPlayerMoneyDetail(OfflinePlayer player)
	{
		if(player == null) return null;
		UUID id = player.getUniqueId();
		long playerTotal = this.storage.getPlayerMoneyTotal(id);
		int playerInvMoney = this.storage.playerDependEconomy.getMoney(id);
		int chestMoney = 0;
		int itemMoney = 0;
		Map<IntLocation, Integer> chestMoneyMap = new HashMap<>();
		for(ChestEconomyChild child : this.storage.chestDependEconomy.childSet)
		{
			ChestWallet w = child.eMap.get(id);
			if(w == null) continue;
			IntLocation intLoc = child.getLocation();
			chestMoneyMap.put(intLoc, w.getMoney());
			chestMoney += w.getMoney();
		}
		Map<IntLocation, Integer> itemMoneyMap = new HashMap<>();
		for(ItemEconomyChild child : this.storage.itemEconomyStorage.eMap.values())
		{
			ItemWallet w = child.eMap.get(id);
			if(w == null) continue;
			Entity entity = Bukkit.getEntity(child.parent);
			IntLocation intLoc = new IntLocation(entity.getLocation());
			int mapMoney = itemMoneyMap.getOrDefault(intLoc, 0);
			itemMoneyMap.put(intLoc, mapMoney + w.getMoney());
			itemMoney += w.getMoney();
		}
		int enderChestMoney = this.storage.playerEnderChestEconomy.getMoney(id);
		return new MoneyDetailResult(player, playerTotal, chestMoney, chestMoneyMap, playerInvMoney, enderChestMoney, itemMoney, itemMoneyMap);
	}

	@Override
	public EconomyResult depositPlayer(HumanEntity player, int amount, SystemID system, String cause, String args)
	{
		return this.syncAction(()->
		{
			if(player == null || amount < 0 || system == null) return EconomyResult.invalidArguments;
			
			if(amount == 0) return EconomyResult.OK;
			
			EconomyActionResult actionResult = this.bank.issuanceToPlayer(system, player, amount, cause, args);

			return this.convertResult(actionResult);
		});
	}
	
	@Override
	public EconomyResult withdrawPlayer(HumanEntity player, int amount, SystemID system, String cause, String args)
	{
		return this.syncAction(()->
		{
			if(player == null || amount < 0 || system == null) return EconomyResult.invalidArguments;
			if(amount == 0) return EconomyResult.OK;
			
			EconomyActionResult actionResult = this.bank.consumeFromPlayer(system, player, amount, cause, args);

			return this.convertResult(actionResult);
		});
		
	}

	@Override
	public EconomyResult depositChest(Container container, int amount, SystemID system, String cause, String args)
	{
		return this.syncAction(()->
		{
			if(container == null || amount < 0 || system == null) return EconomyResult.invalidArguments;
			Inventory inv = container.getInventory();
			if(!this.util.isSystemModifiableChest(inv)) return EconomyResult.notavalidChest;
			if(amount == 0) return EconomyResult.OK;
			EconomyActionResult actionResult = this.bank.issuanceToChest(system, inv, amount, cause, args);
			return this.convertResult(actionResult);
		});
	}
	
	@Override
	public EconomyResult withdrawChest(Container container, int amount, SystemID system, String cause, String args)
	{
		return this.syncAction(()->
		{
			if(container == null || amount < 0 || system == null) return EconomyResult.invalidArguments;
			Inventory inv = container.getInventory();
			if(!this.util.isSystemModifiableChest(inv)) return EconomyResult.notavalidChest;
			if(amount == 0) return EconomyResult.OK;
			EconomyActionResult actionResult = this.bank.consumeFromChest(system, inv, amount, cause, args);
			return this.convertResult(actionResult);
		});
	}

	@Override
	public EconomyResult depositItem(Location loc, int amount, SystemID system, String cause, String args)
	{
		return this.syncAction(()->
		{
			if(loc == null || amount < 0 || system == null) return EconomyResult.invalidArguments;
			if(amount == 0) return EconomyResult.OK;
			EconomyActionResult actionResult = this.bank.issuanceToItem(system, loc, amount, cause, args);
			return this.convertResult(actionResult);
		});
	}

	@Override
	public EconomyResult playerToChest(HumanEntity player, Container container, int amount, SystemID system,
			String cause, String args)
	{
		return this.syncAction(()->
		{
			if(player == null || container == null || amount < 0 || system == null) return EconomyResult.invalidArguments;
			Inventory inv = container.getInventory();
			if(!this.util.isSystemModifiableChest(inv)) return EconomyResult.notavalidChest;
			if(amount == 0) return EconomyResult.OK;
			EconomyActionResult actionResult = this.bank.movePlayerMoneyToChest(player, inv, amount);
			return this.convertResult(actionResult);
		});
	}

	@Override
	public EconomyResult chestToPlayer(Container container, HumanEntity player, int amount, SystemID system,
			String cause, String args)
	{
		return this.syncAction(()->
		{
			if(player == null || container == null || amount < 0 || system == null) return EconomyResult.invalidArguments;
			Inventory inv = container.getInventory();
			if(!this.util.isSystemModifiableChest(inv)) return EconomyResult.notavalidChest;
			if(amount == 0) return EconomyResult.OK;
			EconomyActionResult actionResult = this.bank.moveChestMoneyToPlayer(inv, player, amount);
			return this.convertResult(actionResult);
		});
	}

	@Override
	public EconomyResult playerToPlayer(HumanEntity source, HumanEntity target, int amount, SystemID system,
			String cause, String args)
	{
		return this.syncAction(()->
		{
			if(source == null || target == null || amount < 0 || system == null) return EconomyResult.invalidArguments;
			if(amount == 0) return EconomyResult.OK;
			EconomyActionResult actionResult = this.bank.movePlayerMoneyToPlayer(source, target, amount, cause, args);
			return this.convertResult(actionResult);
		});
	}
	
	private EconomyResult syncAction(Supplier<EconomyResult> func)
	{
		if(Bukkit.isPrimaryThread())
    	{
    		return func.get();
    	}
    	else
    	{
    		Future<EconomyResult> f = this.scheduler.callSyncMethod(this.plugin,()->{return func.get();});
    		EconomyResult r = EconomyResult.unknownError;
    		try {
    			r = f.get();
    		} catch (InterruptedException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (ExecutionException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        	return r;
    	}
	}
	
	private EconomyResult convertResult(EconomyActionResult actionResult)
	{
		switch(actionResult)
		{
		case OK:
			return EconomyResult.OK;
		case insufficientMoney:
			return EconomyResult.insufficientMoney;
		case insufficientSpace:
			return EconomyResult.insufficientSpace;
		case unitTooSmall:
			return EconomyResult.unitTooSmall;
		default:
			return EconomyResult.unknownError;
		}
	}

}
