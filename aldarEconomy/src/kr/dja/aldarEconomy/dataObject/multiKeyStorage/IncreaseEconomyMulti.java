package kr.dja.aldarEconomy.dataObject.multiKeyStorage;

import java.util.UUID;

@FunctionalInterface
public interface IncreaseEconomyMulti<DependC>
{
	public void increaseEconomy(UUID objectUID, DependC key2, int amount, boolean isNew);
}
