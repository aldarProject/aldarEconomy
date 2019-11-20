package kr.dja.aldarEconomy.dataObject.storage;

@FunctionalInterface
public interface IncreaseEconomy<Depend>
{
	public void increaseEconomy(Depend obj, int amount, boolean isNew);
}
