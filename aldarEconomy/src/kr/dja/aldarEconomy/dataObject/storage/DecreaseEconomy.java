package kr.dja.aldarEconomy.dataObject.storage;

@FunctionalInterface
public interface DecreaseEconomy<Depend>
{
	public void decreaseEconomy(Depend obj, int amount, boolean isErase);
}