package kr.dja.aldarEconomy.dataObject.multiKeyStorage;

import java.util.UUID;

@FunctionalInterface
public interface DecreaseEconomyMulti<DependC>
{
	public void decreaseEconomy(UUID objectUID, DependC key2, int amount, boolean isErase);
}
