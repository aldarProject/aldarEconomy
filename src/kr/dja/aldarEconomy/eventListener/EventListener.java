package kr.dja.aldarEconomy.eventListener;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftHopper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;

import kr.dja.aldarEconomy.EconomyUtil;
import kr.dja.aldarEconomy.coininfo.CoinMetadata;
import kr.dja.aldarEconomy.tracker.chest.ChestTracker;
import kr.dja.aldarEconomy.tracker.item.ItemTracker;


public class EventListener implements Listener
{
	private final EconomyUtil checker;
	private final ChestTracker chestTracker;
	private final ItemTracker itemTracker;
	private final Logger logger;

	private final Set<Item> destroyCheck;
	private final Set<Item> createCheck;

	public EventListener(EconomyUtil checker, ChestTracker chestTracker, ItemTracker itemTracker, Logger logger)
	{
		this.checker = checker;
		this.chestTracker = chestTracker;
		this.itemTracker = itemTracker;
		this.logger = logger;
		this.destroyCheck = new HashSet<>();
		this.createCheck = new HashSet<>();
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryOpen(InventoryOpenEvent e)
	{
		if(e.isCancelled()) return;
		Inventory top = e.getView().getTopInventory();
		if(top == null) return;
		if(!(top.getType() == InventoryType.CHEST || top.getType() == InventoryType.ENDER_CHEST)) return;
		this.chestTracker.onOpenEconomyChest(top, e.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryClose(InventoryCloseEvent e)
	{
		Inventory top = e.getView().getTopInventory();
		if(top == null) return;
		if(!(top.getType() == InventoryType.CHEST || top.getType() == InventoryType.ENDER_CHEST)) return;
		
		HumanEntity player = e.getPlayer();
		this.chestTracker.onCloseEconomyChest(top, player);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityExplode(EntityExplodeEvent e)
	{
		if(e.isCancelled()) return;
		for(Block b : e.blockList())
		{
			BlockState bs = b.getState();
			if(!(bs instanceof Container)) continue;
			Container c = (Container)bs;
			if(!this.checker.isAllowdInventory(c.getInventory())) continue;
			this.chestTracker.onDestroyBlock(c);
		}
	}
	

	@EventHandler(priority = EventPriority.MONITOR)
	public void onItemSpawn(ItemSpawnEvent e)
	{
		if(e.isCancelled()) return;
		Item item = e.getEntity();
		
		CoinMetadata money = this.checker.isMoney(item.getItemStack());
		if(money == null) return;
		
		if(this.createCheck.contains(item))
		{
			this.createCheck.remove(item);
			return;
		}

		this.itemTracker.onItemSpawn(item, money);
		
	}

	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockBreak(BlockBreakEvent e)
	{
		if(e.isCancelled()) return;
		Block b = e.getBlock();
		BlockState bs = b.getState();
		if(!(bs instanceof Container)) return;
		Container c = (Container)bs;
		if(!this.checker.isAllowdInventory(c.getInventory())) return;
		this.chestTracker.onDestroyBlock(c);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityRemoveFromWorld(EntityRemoveFromWorldEvent e)
	{// 아이템 소멸onEntityDamage
		if(!(e.getEntity() instanceof Item)) return;
		Item item = (Item) e.getEntity();
		ItemStack stack = item.getItemStack();
		CoinMetadata money = this.checker.isMoney(stack);
		if(money == null) return;
		if(this.destroyCheck.contains(item))
		{
			this.destroyCheck.remove(item);
			return;
		}
		this.itemTracker.onMoneyDespawn(item, money.value * stack.getAmount());
		//Bukkit.getServer().broadcastMessage("돈소멸" + money.toString());
		
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityPickupItem(EntityPickupItemEvent e)
	{// 엔티티가 아이템을 먹었을 때
		if(e.isCancelled()) return;
		ItemStack stack = e.getItem().getItemStack();
		CoinMetadata money = this.checker.isMoney(stack);
		if(money == null) return;
		if(e.getEntityType() != EntityType.PLAYER)
		{
			e.setCancelled(true);
			return;
		}
		LivingEntity entity = e.getEntity();
		if(entity instanceof HumanEntity)
		{
			HumanEntity p = (HumanEntity)e.getEntity();
			this.chestTracker.onPlayerGainMoney((HumanEntity)e.getEntity(), money.value * stack.getAmount());
			this.itemTracker.onPlayerGainMoney(p, e.getItem(), money.value * stack.getAmount());
			if(e.getRemaining() == 0) this.destroyCheck.add(e.getItem());
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDropItem(PlayerDropItemEvent e)
	{// 플레이어가 아이템을 버렸을 때
		if(e.isCancelled()) return;
		Item itemDrop = e.getItemDrop();
		ItemStack stack = itemDrop.getItemStack();
		CoinMetadata money = this.checker.isMoney(stack);
		if(money == null) return;
		this.createCheck.add(itemDrop);
		Player p = e.getPlayer();
		int amount = money.value * stack.getAmount();
		this.chestTracker.onPlayerDropMoney(p, amount);
		this.itemTracker.onPlayerDropMoney(p, itemDrop, amount);
    }
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryPickupItem(InventoryPickupItemEvent e)
	{// 호퍼나 마인카트가 아이템을 먹었을 때
		if(e.isCancelled()) return;
		CoinMetadata money = this.checker.isMoney(e.getItem().getItemStack());
		if(money == null) return;
		e.setCancelled(true);
		e.getItem().remove();//돈 삭제(돈 삭제 이벤트 호출됨)
		
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryMoveItem(InventoryMoveItemEvent e)
	{// 호퍼 아이템 이동
		if(e.isCancelled()) return;
		ItemStack item = e.getItem();
		CoinMetadata money = this.checker.isMoney(item);
		if(money == null) return;
		e.setCancelled(true);
		InventoryHolder holder = e.getDestination().getHolder();
		//TODO 아이템화 코드를 추가하세요
		if(holder instanceof CraftHopper)
		{
			CraftHopper b = (CraftHopper)holder;
			b.getBlock().setType(Material.AIR);
		}
		else if(holder instanceof Entity)
		{
			Entity en = (Entity)holder;
			en.remove();
		}

	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryInteract(InventoryInteractEvent e)
	{// ???
		if(e.isCancelled()) return;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerEntityInteract(PlayerInteractEntityEvent e)
	{// 플레이어엔티티인터렉트 액자와같은
		if(e.isCancelled()) return;
		CoinMetadata money = this.checker.isMoney(e.getPlayer().getInventory().getItemInMainHand());
		if(money == null) return;
		e.setCancelled(true);
	}
	
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onItemMerge(ItemMergeEvent e)
	{
		if(e.isCancelled()) return;
		CoinMetadata money = this.checker.isMoney(e.getEntity().getItemStack());
		if(money == null) return;
		this.destroyCheck.add(e.getEntity());
		this.itemTracker.onMoneyMerge(e.getTarget(), e.getEntity());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryClick(InventoryClickEvent e)
	{// 인벤토리클릭이벤트
		if(e.isCancelled()) return;
		if(e.getSlotType() == SlotType.OUTSIDE) return;
		//e.getInventory().getType() 연 인벤토리
		//e.getClickedInventory().getType() 클릭한 인벤토리

		CoinMetadata currentMoney = this.checker.isMoney(e.getCurrentItem());
		CoinMetadata cursorMoeny = this.checker.isMoney(e.getCursor());
		if(currentMoney == null && cursorMoeny == null) return;
		Inventory top = e.getView().getTopInventory();
		Inventory clickedInv = e.getClickedInventory();
		if(top != null && !this.checker.isAllowdInventory(top))
		{// 돈이면 허용된 인벤토리에만 집어넣을수 있음.
			if(top == clickedInv)
			{
				switch(e.getAction())
				{
				case PLACE_ALL:
				case PLACE_ONE:
				case PLACE_SOME:
					e.setCancelled(true);
					return;
				default:
					break;
				}
			}
			else if(e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)
			{
				e.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryDrag(InventoryDragEvent e)
	{// 인벤토리드레그이벤트
		if(e.isCancelled()) return;
		CoinMetadata cursorMoeny = this.checker.isMoney(e.getOldCursor());
		if(cursorMoeny == null) return;
		Inventory top = e.getView().getTopInventory();
		if(top != null && !this.checker.isAllowdInventory(top))
		{
			int topInvSize = top.getSize();
			for(int slot : e.getRawSlots())
			{
				if(topInvSize > slot)
				{
					e.setCancelled(true);
					return;
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDeath(EntityDeathEvent e)
	{
		int dropMoney = 0;
		for(ItemStack stack : e.getDrops())
		{
			CoinMetadata money = this.checker.isMoney(stack);
			if(money == null) continue;
			dropMoney += money.value * stack.getAmount();
		}
		Entity entity = e.getEntity();
		if(entity instanceof HumanEntity)
		{
			if(dropMoney != 0) this.itemTracker.onPlayerDeathDropMoney((HumanEntity)entity, dropMoney);
		}
		
	}
	
}