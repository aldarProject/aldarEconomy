package kr.dja.aldarEconomy.entity;

@FunctionalInterface
public interface IEconomyObserver<EconomyDepend, WalletType extends Wallet<EconomyDepend>>
{
	public void modifyMoney(WalletType wallet, int amount);
}
