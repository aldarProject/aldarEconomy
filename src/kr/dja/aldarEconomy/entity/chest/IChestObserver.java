package kr.dja.aldarEconomy.entity.chest;

import java.util.UUID;

import kr.dja.aldarEconomy.IntLocation;

public interface IChestObserver
{
	public void modifyMoney(UUID uuid, UUID player, ChestWallet wallet, int amount);
	public void appendKey(IntLocation loc, ChestEconomyChild obj);
	public void deleteKey(IntLocation loc, ChestEconomyChild obj);
}
