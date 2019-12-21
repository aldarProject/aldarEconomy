package kr.dja.aldarEconomy.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import kr.dja.aldarEconomy.dataObject.DependType;
import kr.dja.aldarEconomy.dataObject.IntLocation;
import kr.dja.aldarEconomy.dataObject.chest.ChestEconomyChild;
import kr.dja.aldarEconomy.dataObject.chest.ChestEconomyStorage;
import kr.dja.aldarEconomy.dataObject.chest.ChestWallet;
import kr.dja.aldarEconomy.dataObject.chest.IChestObserver;
import kr.dja.aldarEconomy.dataObject.enderChest.EnderChestEconomyStorage;
import kr.dja.aldarEconomy.dataObject.enderChest.EnderChestWallet;
import kr.dja.aldarEconomy.dataObject.itemEntity.ItemEconomyStorage;
import kr.dja.aldarEconomy.dataObject.itemEntity.ItemWallet;
import kr.dja.aldarEconomy.dataObject.player.PlayerEconomyStorage;
import kr.dja.aldarEconomy.dataObject.player.PlayerWallet;
import kr.dja.aldarEconomy.setting.MoneyInfo;
import kr.dja.aldarEconomy.trade.TradeTracker;

public class EconomyDataStorage
{// dao
	private final MoneyInfo moneyInfo;

	private final Logger logger;
	private final Map<UUID, PlayerEconomy> _playerEconomyMap;
	public final Map<UUID, PlayerEconomy> playerEconomyMap;
	
	public final ChestEconomyStorage chestDependEconomy;
	public final PlayerEconomyStorage playerDependEconomy;
	public final EnderChestEconomyStorage playerEnderChestEconomy;
	public final ItemEconomyStorage itemEconomyStorage;
	
	public EconomyDataStorage(MoneyInfo moneyInfo, Logger logger)
	{
		this.moneyInfo = moneyInfo;

		this.logger = logger;
		this._playerEconomyMap = new HashMap<>();
		this.playerEconomyMap = Collections.unmodifiableMap(this._playerEconomyMap);
		
		this.chestDependEconomy = new ChestEconomyStorage(new ChestCallback());
		this.playerDependEconomy = new PlayerEconomyStorage(this::onModifyPlayerMoney);
		this.playerEnderChestEconomy = new EnderChestEconomyStorage(this::onModifyEnderChestMoney);
		this.itemEconomyStorage = new ItemEconomyStorage(this::onModifyItemMoney);
	}
	
	public long getPlayerMoney(UUID player)
	{
		Player p = Bukkit.getPlayer(player);
		if(p == null)
		{
			this.logger.log(Level.WARNING, String.format("존재하지 않는 플레이어 (%s)",player));
			return 0;
		}
		PlayerEconomy e = this._playerEconomyMap.get(player);
		if(e == null) return 0;
		return e.getMoney();
	}
	
	private class ChestCallback implements IChestObserver
	{
		@Override
		public void modifyMoney(UUID uuid, UUID player, ChestWallet wallet, int amount)
		{
			if(wallet.ownerType == DependType.PLAYER)
			{
				EconomyDataStorage.this.modifyPlayerMoney(player, amount);
				
			}
		}

		@Override
		public void appendKey(IntLocation loc, ChestEconomyChild obj)
		{
			//Bukkit.getServer().broadcastMessage(String.format("appendKey %s", loc));
		}

		@Override
		public void deleteKey(IntLocation loc, ChestEconomyChild obj)
		{
			//Bukkit.getServer().broadcastMessage(String.format("deleteKey %s", loc));
		}
	}

	
	private void onModifyPlayerMoney(PlayerWallet wallet, int amount)
	{
		this.modifyPlayerMoney(wallet.depend, amount);
	}
	
	private void onModifyEnderChestMoney(EnderChestWallet wallet, int amount)
	{
		
		this.modifyPlayerMoney(wallet.depend, amount);
	}
	
	private void onModifyItemMoney(ItemWallet wallet, int amount)
	{
		if(wallet.ownerType == DependType.PLAYER)
		{
			this.modifyPlayerMoney(wallet.depend, amount);
		}
	}

	private void modifyPlayerMoney(UUID player, int amount)
	{
		PlayerEconomy playerEconomy = this._playerEconomyMap.get(player);
		if(playerEconomy == null)
		{
			playerEconomy = new PlayerEconomy(player);
			this._playerEconomyMap.put(player, playerEconomy);
		}
		playerEconomy.money += amount;
		if(playerEconomy.money <= 0)
		{
			this._playerEconomyMap.remove(player);
		}
		Bukkit.getServer().broadcastMessage(String.format("modifyEconomy %s %s", Bukkit.getPlayer(player).getName(), amount));
	}
}