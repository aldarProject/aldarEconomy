package kr.dja.aldarEconomy.entity;

public abstract class Wallet<Depend> implements Comparable<Wallet<Depend>>
{
	protected int money;
	public final Depend depend;
	
	public Wallet(Depend depend)
	{
		this.money = 0;
		this.depend = depend;
	}
	
	public int getMoney()
	{
		return this.money;
	}
	
	@Override
	public int compareTo(Wallet<Depend> target)
	{
		return this.money - target.money;
	}
}
