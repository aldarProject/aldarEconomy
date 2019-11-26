package kr.dja.aldarEconomy.dataObject.container.chest;

import java.util.UUID;

import kr.dja.aldarEconomy.dataObject.container.IntLocation;

public interface IChestObserver
{
	public void increaseEconomy(UUID uuid, UUID player, ChestWallet wallet, boolean isNew);
	public void decreaseEconomy(UUID uuid, UUID player, ChestWallet wallet, boolean isErase);
	public void appendKey(IntLocation loc, ChestEconomyChild obj);
	public void deleteKey(IntLocation loc, ChestEconomyChild obj);
}
