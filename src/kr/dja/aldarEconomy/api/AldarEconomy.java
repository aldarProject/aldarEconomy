package kr.dja.aldarEconomy.api;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Container;
import org.bukkit.entity.HumanEntity;

import kr.dja.aldarEconomy.api.token.SystemID;

public interface AldarEconomy
{
	public int getPlayerInventoryMoney(OfflinePlayer player);
	public MoneyDetailResult getPlayerMoneyDetail(OfflinePlayer player);
	
	public SystemID takeAPIToken(String id);
	
	public EconomyResult depositPlayer(HumanEntity player, int amount, SystemID system, String cause, String args);
	public EconomyResult withdrawPlayer(HumanEntity player, int amount, SystemID system, String cause, String args);
	
	public EconomyResult depositChest(Container container, int amount, SystemID system, String cause, String args);
	public EconomyResult withdrawChest(Container container, int amount, SystemID system, String cause, String args);
	public EconomyResult depositItem(Location item, int amount, SystemID system, String cause, String args);
	
	public EconomyResult playerToChest(HumanEntity player, Container containr, int amount, SystemID system, String cause, String args);
	public EconomyResult chestToPlayer(Container container, HumanEntity player, int amount, SystemID system, String cause, String args);
	public EconomyResult playerToPlayer(HumanEntity source, HumanEntity target, int amount, SystemID system, String cause, String args);
}
