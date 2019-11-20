package kr.dja.aldarEconomy.dao;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;
import kr.dja.aldarEconomy.api.SystemID;
import kr.dja.aldarEconomy.dataObject.multiKeyStorage.EconomyMapChild;
import kr.dja.aldarEconomy.dataObject.multiKeyStorage.MultipleEconomyMap;
import kr.dja.aldarEconomy.dataObject.storage.EconomyMap;
import kr.dja.aldarEconomy.setting.MoneyInfo;
import kr.dja.aldarEconomy.trade.TradeTracker;

public class EconomyStateStorage
{// dao

	public final MultipleEconomyMap<IntLocation, UUID> chestDependEconomy;
	public final EconomyMap<SystemID> systemDependEconomy;
	public final EconomyMap<UUID> playerDependEconomy;
	public final EconomyMap<UUID> playerEnderChestEconomy;
	public final MultipleEconomyMap<UUID, UUID> itemDependEconomyPlayer;
	public final MultipleEconomyMap<UUID, System> itemDependEconomySystem;
	public final EconomyMapMoreInfo<UUID> playerDependEconomy;
	public final EconomyMapMoreInfo<UUID> playerEnderChestEconomy;
	
	private final MoneyInfo moneyInfo;
	private final TradeTracker tradeTracker;
	private final Logger logger;
	
	public EconomyStateStorage(MoneyInfo moneyInfo, TradeTracker tradeTracker, Logger logger)
	{
		this.chestDependEconomy = new MultipleEconomyMap<>(this::onChestIncreaseEconomy, this::onChestDecreaseEconomy, this::onChestAppendKey, this::onChestDeleteKey);
		this.systemDependEconomy = new EconomyMap<>(this::onSystemIncreaseEconomy, this::onSystemDecreaseEconomy);
		this.itemDependEconomyPlayer = new MultipleEconomyMap<>();
		this.itemDependEconomySystem = new MultipleEconomyMap<>();
		this.playerDependEconomy = new EconomyMap<>();
		this.playerEnderChestEconomy = new EconomyMap<>();
		
		this.moneyInfo = moneyInfo;
		this.tradeTracker = tradeTracker;
		this.logger = logger;
	}
	
	
	private void onChestIncreaseEconomy(UUID objectUID, UUID key2, int amount, boolean isNew)
	{
		
	}
	
	private void onChestDecreaseEconomy(UUID objectUID, UUID key2, int amount, boolean isErase)
	{
		
	}
	
	private void onChestAppendKey(IntLocation key, EconomyMapChild<IntLocation, UUID> child)
	{
		
	}
	
	private void onChestDeleteKey(IntLocation key, EconomyMapChild<IntLocation, UUID> child)
	{
		
	}
	
	private void onSystemIncreaseEconomy(SystemID obj, int amount, boolean isNew)
	{
		
	}
	
	private void onSystemDecreaseEconomy(SystemID obj, int amount, boolean isErase)
	{
		
	}

	private void onItemPlayerIncreaseEconomy(UUID objectUID, UUID key2, int amount, boolean isNew)
	{
		
	}
	
	private void onItemPlayerDecreaseEconomy(UUID objectUID, UUID key2, int amount, boolean isErase)
	{
		
	}
	
	private void onItemPlayerAppendKey(IntLocation key, EconomyMapChild<IntLocation, UUID> child)
	{
		
	}
	
	private void onItemPlayerDeleteKey(IntLocation key, EconomyMapChild<IntLocation, UUID> child)
	{
		
	}
	
}