package kr.dja.aldarEconomy.data;

import java.util.UUID;
import java.util.logging.Logger;

import kr.dja.aldarEconomy.dataObject.container.IntLocation;
import kr.dja.aldarEconomy.dataObject.container.chest.ChestEconomyChild;
import kr.dja.aldarEconomy.dataObject.container.chest.ChestEconomyStorage;
import kr.dja.aldarEconomy.dataObject.container.chest.ChestWallet;
import kr.dja.aldarEconomy.dataObject.container.chest.IChestObserver;
import kr.dja.aldarEconomy.dataObject.container.enderChest.EnderChestEconomyStorage;
import kr.dja.aldarEconomy.dataObject.container.enderChest.EnderChestWallet;
import kr.dja.aldarEconomy.dataObject.itemEntity.player.ItemPlayerEconomyStorage;
import kr.dja.aldarEconomy.dataObject.itemEntity.player.ItemPlayerWallet;
import kr.dja.aldarEconomy.dataObject.itemEntity.system.ItemSystemEconomyStorage;
import kr.dja.aldarEconomy.dataObject.itemEntity.system.ItemSystemWallet;
import kr.dja.aldarEconomy.dataObject.player.PlayerEconomyStorage;
import kr.dja.aldarEconomy.dataObject.player.PlayerWallet;
import kr.dja.aldarEconomy.dataObject.system.SystemEconomyStorage;
import kr.dja.aldarEconomy.dataObject.system.SystemWallet;
import kr.dja.aldarEconomy.setting.MoneyInfo;
import kr.dja.aldarEconomy.trade.TradeTracker;

public class EconomyStateStorage
{// dao

	public final ChestEconomyStorage chestDependEconomy;
	public final SystemEconomyStorage systemDependEconomy;
	public final PlayerEconomyStorage playerDependEconomy;
	public final EnderChestEconomyStorage playerEnderChestEconomy;
	public final ItemPlayerEconomyStorage itemDependEconomyPlayer;
	public final ItemSystemEconomyStorage itemDependEconomySystem;
	
	private final MoneyInfo moneyInfo;
	private final TradeTracker tradeTracker;
	private final Logger logger;
	
	public EconomyStateStorage(MoneyInfo moneyInfo, TradeTracker tradeTracker, Logger logger)
	{
		this.moneyInfo = moneyInfo;
		this.tradeTracker = tradeTracker;
		this.logger = logger;
		
		this.chestDependEconomy = new ChestEconomyStorage(new ChestCallback());
		this.systemDependEconomy = new SystemEconomyStorage(this::modifySystemMoney);
		this.playerDependEconomy = new PlayerEconomyStorage(this::modifyPlayerMoney);
		this.playerEnderChestEconomy = new EnderChestEconomyStorage(this::modifyEnderChestMoney);
		this.itemDependEconomySystem = new ItemSystemEconomyStorage(this::modifyItemSystemMoney);
		this.itemDependEconomyPlayer = new ItemPlayerEconomyStorage(this::modifyItemPlayerMoney);
	}
	
	private class ChestCallback implements IChestObserver
	{
		@Override
		public void increaseEconomy(UUID uuid, UUID player, ChestWallet wallet, boolean isNew)
		{
			
		}

		@Override
		public void decreaseEconomy(UUID uuid, UUID player, ChestWallet wallet, boolean isErase)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void appendKey(IntLocation loc, ChestEconomyChild obj)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void deleteKey(IntLocation loc, ChestEconomyChild obj)
		{
			// TODO Auto-generated method stub
			
		}
	}
	
	public void modifySystemMoney(SystemWallet wallet, boolean isNew)
	{
		
	}
	
	public void modifyPlayerMoney(PlayerWallet wallet, boolean isNew)
	{
		
	}
	
	public void modifyEnderChestMoney(EnderChestWallet wallet, boolean isNew)
	{
		
	}
	
	public void modifyItemSystemMoney(ItemSystemWallet wallet, boolean isNew)
	{
		
	}
	
	public void modifyItemPlayerMoney(ItemPlayerWallet wallet, boolean isNew)
	{
		
	}
	

	
}