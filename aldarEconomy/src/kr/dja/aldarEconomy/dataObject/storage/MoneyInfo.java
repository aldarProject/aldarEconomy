package kr.dja.aldarEconomy.dataObject.storage;

public class MoneyInfo
{
	int money;
	private DataObject obj;
	
	public MoneyInfo(DataObject obj)
	{
		this.obj = obj;
	}
	public int getMoney()
	{
		return this.money;
	}
}
