package kr.dja.aldarEconomy.dataObject;

@FunctionalInterface
public interface IEconomyObserver<EconomyDepend, WalletType extends Wallet<EconomyDepend>>
{
	public void modifyMoney(WalletType wallet, boolean isNew);
}
