package kr.dja.aldarEconomy.dataObject.multiKeyStorage;

@FunctionalInterface
public interface DeleteKey<DependP, DependC>
{
	public void deleteKey(DependP key, EconomyMapChild<DependP, DependC> child);
}