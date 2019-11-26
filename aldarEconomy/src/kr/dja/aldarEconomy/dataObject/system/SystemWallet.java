package kr.dja.aldarEconomy.dataObject.system;

import kr.dja.aldarEconomy.api.SystemID;
import kr.dja.aldarEconomy.dataObject.Wallet;

public class SystemWallet extends Wallet<SystemID>
{
	SystemWallet(SystemID id, int money)
	{
		super(id, money);
	}
	
	void setMoney(int money)
	{
		this.money = money;
	}
}
