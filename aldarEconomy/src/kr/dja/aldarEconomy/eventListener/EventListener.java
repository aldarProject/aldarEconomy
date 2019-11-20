package kr.dja.aldarEconomy.eventListener;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftHopper;
import org.bukkit.entity.Damageable;
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
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemMergeEvent;
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
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;

import kr.dja.aldarEconomy.ConstraintChecker;
import kr.dja.aldarEconomy.setting.MoneyMetadata;
import kr.dja.aldarEconomy.tracker.ChestTracker;
import kr.dja.aldarEconomy.tracker.ItemTracker;



public class EventListener implements Listener
{
	private final ConstraintChecker checker;
	private final ChestTracker chestTracker;
	private final ItemTracker itemTracker;
	private final Logger logger;
	
	private final Set<HumanEntity> closeChestItemDropCheck;
	private final Set<Item> destroyCheck;

	public EventListener(ConstraintChecker checker, ChestTracker chestTracker, ItemTracker itemTracker, Logger logger)
	{
		this.checker = checker;
		this.chestTracker = chestTracker;
		this.itemTracker = itemTracker;
		this.logger = logger;
		this.closeChestItemDropCheck = new HashSet<>();
		this.destroyCheck = new HashSet<>();
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryOpen(InventoryOpenEvent e)
	{
		if(e.isCancelled()) return;
		Inventory top = e.getView().getTopInventory();
		if(top == null) return;
		if(top.getType() != InventoryType.CHEST) return;
		long before = System.nanoTime();
		this.chestTracker.openChest(top, e.getPlayer());
		Bukkit.getServer().broadcastMessage("time:" + ((System.nanoTime() - before) / 1000) + "μs");
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryClose(InventoryCloseEvent e)
	{
		Inventory top = e.getView().getTopInventory();
		if(top == null) return;
		if(top.getType() != InventoryType.CHEST) return;
		long before = System.nanoTime();
		HumanEntity player = e.getPlayer();
		if(this.checker.isMoney(player.getItemOnCursor()) != null)
		{
			this.closeChestItemDropCheck.add(player);
		}
		this.chestTracker.closeChest(top, player);
		Bukkit.getServer().broadcastMessage("time:" + ((System.nanoTime() - before) / 1000) + "μs");
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityExplode(EntityExplodeEvent e)
	{
		if(e.isCancelled()) return;
		for(Block b : e.blockList())
		{
			this.chestTracker.destroyBlock(b);
		}

	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockBreak(BlockBreakEvent e)
	{
		if(e.isCancelled()) return;
		long before = System.nanoTime();
		Block b = e.getBlock();
		this.chestTracker.destroyBlock(b);
		Bukkit.getServer().broadcastMessage("time:" + ((System.nanoTime() - before) / 1000) + "μs");
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityRemoveFromWorld(EntityRemoveFromWorldEvent e)
	{// 아이템 소멸onEntityDamage
		if(!(e.getEntity() instanceof Item)) return;
		Item item = (Item) e.getEntity();
		MoneyMetadata money = this.checker.isMoney(item.getItemStack());
		if(money == null) return;
		if(this.destroyCheck.contains(item))
		{
			this.destroyCheck.remove(item);
			return;
		}
		//Bukkit.getServer().broadcastMessage("돈소멸" + money.toString());
		
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityPickupItem(EntityPickupItemEvent e)
	{// 엔티티가 아이템을 먹었을 때
		if(e.isCancelled()) return;
		ItemStack stack = e.getItem().getItemStack();
		MoneyMetadata money = this.checker.isMoney(stack);
		if(money == null) return;
		if(e.getEntityType() != EntityType.PLAYER)
		{
			e.setCancelled(true);
			return;
		}
		LivingEntity entity = e.getEntity();
		if(entity instanceof HumanEntity)
		{
			this.chestTracker.gainMoney((HumanEntity)e.getEntity(), money.value * stack.getAmount());
			this.destroyCheck.add(e.getItem());
			//Bukkit.getServer().broadcastMessage("돈픽업");
		}
		
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDropItem(PlayerDropItemEvent e)
	{// 플레이어가 아이템을 버렸을 때
		if(e.isCancelled()) return;
		ItemStack stack = e.getItemDrop().getItemStack();
		MoneyMetadata money = this.checker.isMoney(stack);
		if(money == null) return;
		Bukkit.getServer().broadcastMessage("플레이어아이템버림");
		Player p = e.getPlayer();
		if(this.closeChestItemDropCheck.contains(p))
		{
			this.closeChestItemDropCheck.remove(p);
			return;
		}
		this.chestTracker.dropMoney(p, money.value * stack.getAmount());
		//this.itemTracker.playerDropMoney(p, e.getItemDrop());
    }
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryPickupItem(InventoryPickupItemEvent e)
	{// 호퍼나 마인카트가 아이템을 먹었을 때
		if(e.isCancelled()) return;
		MoneyMetadata money = this.checker.isMoney(e.getItem().getItemStack());
		if(money == null) return;
		e.setCancelled(true);
		e.getItem().remove();//돈 삭제(돈 삭제 이벤트 호출됨)
		
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryMoveItem(InventoryMoveItemEvent e)
	{// 호퍼 아이템 이동
		if(e.isCancelled()) return;
		ItemStack item = e.getItem();
		MoneyMetadata money = this.checker.isMoney(item);
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
		MoneyMetadata money = this.checker.isMoney(e.getPlayer().getInventory().getItemInMainHand());
		if(money == null) return;
		e.setCancelled(true);
	}
	
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onItemMerge(ItemMergeEvent e)
	{
		if(e.isCancelled()) return;
		MoneyMetadata money = this.checker.isMoney(e.getEntity().getItemStack());
		if(money == null) return;

		this.destroyCheck.add(e.getEntity());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryClick(InventoryClickEvent e)
	{// 인벤토리클릭이벤트
		if(e.isCancelled()) return;
		if(e.getSlotType() == SlotType.OUTSIDE) return;
		//e.getInventory().getType() 연 인벤토리
		//e.getClickedInventory().getType() 클릭한 인벤토리

		MoneyMetadata currentMoney = this.checker.isMoney(e.getCurrentItem());
		MoneyMetadata cursorMoeny = this.checker.isMoney(e.getCursor());
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
		MoneyMetadata cursorMoeny = this.checker.isMoney(e.getOldCursor());
		if(cursorMoeny == null) return;
		StringBuffer buf = new StringBuffer();
		Inventory top = e.getView().getTopInventory();
		if(top != null && !this.checker.isAllowdInventory(top))
		{
			int topInvSize = top.getSize();
			for(int slot : e.getRawSlots())
			{
				buf.append(slot+",");
				if(topInvSize > slot)
				{
					e.setCancelled(true);
					return;
				}
			}
		}
	}
	
	
}
