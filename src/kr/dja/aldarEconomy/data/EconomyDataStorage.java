package kr.dja.aldarEconomy.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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

public class EconomyDataStorage
{// dao
	private final MoneyInfo moneyInfo;
	private final TradeTracker tradeTracker;
	private final Logger logger;
	private final Map<UUID, PlayerEconomy> _playerEconomyMap;
	public final Map<UUID, PlayerEconomy> playerEconomyMap;
	
	public final ChestEconomyStorage chestDependEconomy;
	public final SystemEconomyStorage systemDependEconomy;
	public final PlayerEconomyStorage playerDependEconomy;
	public final EnderChestEconomyStorage playerEnderChestEconomy;
	public final ItemPlayerEconomyStorage itemDependEconomyPlayer;
	public final ItemSystemEconomyStorage itemDependEconomySystem;
	
	public EconomyDataStorage(MoneyInfo moneyInfo, TradeTracker tradeTracker, Logger logger)
	{
		this.moneyInfo = moneyInfo;
		this.tradeTracker = tradeTracker;
		this.logger = logger;
		this._playerEconomyMap = new HashMap<>();
		this.playerEconomyMap = Collections.unmodifiableMap(this._playerEconomyMap);
		
		this.chestDependEconomy = new ChestEconomyStorage(new ChestCallback());
		this.systemDependEconomy = new SystemEconomyStorage(this::onModifySystemMoney);
		this.playerDependEconomy = new PlayerEconomyStorage(this::onModifyPlayerMoney);
		this.playerEnderChestEconomy = new EnderChestEconomyStorage(this::onModifyEnderChestMoney);
		this.itemDependEconomySystem = new ItemSystemEconomyStorage(this::onModifyItemSystemMoney);
		this.itemDependEconomyPlayer = new ItemPlayerEconomyStorage(this::onModifyItemPlayerMoney);
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
			EconomyDataStorage.this.modifyPlayerMoney(player, amount);

			Bukkit.getServer().broadcastMessage(String.format("modifyEconomy %s %s", Bukkit.getPlayer(player).getName(), wallet.getMoney()));
		}

		@Override
		public void appendKey(IntLocation loc, ChestEconomyChild obj)
		{
			Bukkit.getServer().broadcastMessage(String.format("appendKey %s", loc));
		}

		@Override
		public void deleteKey(IntLocation loc, ChestEconomyChild obj)
		{
			Bukkit.getServer().broadcastMessage(String.format("deleteKey %s", loc));
			
		}
	}
	
	private void onModifySystemMoney(SystemWallet wallet, int amount)
	{
		
	}
	
	private void onModifyPlayerMoney(PlayerWallet wallet, int amount)
	{
		this.modifyPlayerMoney(wallet.depend, amount);
	}
	
	private void onModifyEnderChestMoney(EnderChestWallet wallet, int amount)
	{
		this.modifyPlayerMoney(wallet.player, amount);
	}
	
	private void onModifyItemSystemMoney(ItemSystemWallet wallet, int amount)
	{
		
	}
	
	private void onModifyItemPlayerMoney(ItemPlayerWallet wallet, int amount)
	{
		this.modifyPlayerMoney(wallet.player, amount);
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
		
	}
}