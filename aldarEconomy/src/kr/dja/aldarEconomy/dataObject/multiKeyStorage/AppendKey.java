package kr.dja.aldarEconomy.dataObject.multiKeyStorage;

@FunctionalInterface
public interface AppendKey<DependP, DependC>
{
	public void appendKey(DependP key, EconomyMapChild<DependP, DependC> child);
}