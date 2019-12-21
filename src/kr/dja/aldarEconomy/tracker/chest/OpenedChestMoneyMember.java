package kr.dja.aldarEconomy.tracker.chest;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;

class OpenedChestMoneyMember
{
	public Inventory masterInven;// 현재 접근중인 인벤토리의 주 인벤토리(더블 체스트 고려)
	public int chestMoney;
	public final Map<HumanEntity, Integer> playerMoneyMap;
	
	OpenedChestMoneyMember()
	{
		this.playerMoneyMap = new HashMap<>();
	}
	
	
}