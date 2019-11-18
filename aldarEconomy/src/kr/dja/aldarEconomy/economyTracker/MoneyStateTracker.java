package kr.dja.aldarEconomy.economyTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import kr.dja.aldarEconomy.api.SystemID;
import kr.dja.aldarEconomy.dataStorage.EconomyDataStorage;


public class MoneyStateTracker
{// 돈의 상태 변화를 추적합니다
	private final EconomyDataStorage dataStorage;
	
	public MoneyStateTracker(EconomyDataStorage dataStorage)
	{
		this.dataStorage = dataStorage;
	}
	

	
	
/*
	public boolean registerEconomy(int amount, String systemID, Player owner)
	{
		if(systemID == null) return false;
		EconomySystemDependPlayerOwned e = new EconomySystemDependPlayerOwned(amount, systemID, owner);
		this.systemDependEconomy.add(e);
		return true;
	}
	
	public EconomyItemDepend itemizationEconomy(Item i, EconomySystemDependPlayerOwned e, int amount)
	{// 새 이코노미 오브젝트를 만들고 아이템화 합니다.
		
		EconomySystemDependPlayerOwned ne = new EconomySystemDependPlayerOwned(amount, e.systemID, e.owner);
		EconomyItemDepend id = new EconomyItemDepend(i, ne);
		e.decreaseMoney(amount);
		this.itemDependEconomy.put(i, id);
		return id;
	}
	
	public EconomyItemDepend itemizationEconomy(Item i, EconomySystemDependPlayerOwned e)
	{
		EconomyItemDepend id = new EconomyItemDepend(i, e);
		this.systemDependEconomy.remove(e);
		this.itemDependEconomy.put(i, id);
		return id;
	}
	
	public EconomyPlayerDepend playerEconomyPickup(EconomyItemDepend id, Player p)
	{
		EconomyPlayerDepend pd = this.playerDependEconomy.getOrDefault(p, null);
		if(pd == null)
		{
			pd = new EconomyPlayerDepend(p, id.e);
		}
		
		
		this.itemDependEconomy.remove(id.item);
		this.playerDependEconomy.put(p, pd);
		return pd;
	}
	
	public EconomyItemDepend playerItemDrop(EconomyPlayerDepend pd, Item i)
	{
		EconomyItemDepend id = new EconomyItemDepend(i, pd.e);
		this.playerDependEconomy.remove(pd.player);
		this.itemDependEconomy.put(i, id);
		return id;
	}
	
	public EconomySystemDependPlayerOwned playerEconomyToSystem(EconomyPlayerDepend pd)
	{
		this.playerDependEconomy.remove(pd.player);
		this.systemDependEconomy.add(pd.e);
		return pd.e;
	}
	
	public EconomyBlockDependObj playerEconomyToBlock(EconomyPlayerDepend pd, int value, Block b)
	{
		Location bloc = b.getLocation();
		EconomyBlockDependObj bd = this.blockDependEconomy.getOrDefault(bloc, null);
		this.playerDependEconomy.remove(pd.player);
		if(bd == null)
		{
			bd = new EconomyBlockDependObj(b);
			this.blockDependEconomy.put(bloc, bd);
		}
		
		
		return bd;
		
	}
	
	public EconomyPlayerDepend blockEconomyToPlayer(EconomyBlockDependObj bd, Player p)
	{
		Location bloc = bd.block.getLocation();
		this.blockDependEconomy.remove(bd);
		this.blockLocMap.get(bloc);
		
		return null;
		
	}
	*/
	
}
