package kr.dja.aldarEconomy.trade;

import java.util.UUID;

import kr.dja.aldarEconomy.dataObject.DependType;


public class TradeTracker
{
	public static final String ARGSTYPE_SYSTEM_FORCE_ISSUANCE = "ARGSTYPE_SYSTEM_FORCE_ISSUANCE";
	public static final String ARGSTYPE_SENDMONEY_CHEST_TRADE = "ARGSTYPE_SENDMONEY_CHEST_TRADE";
	public static final String ARGSTYPE_SENDMONEY_ITEM_TRADE = "ARGSTYPE_SENDMONEY_ITEM_TRADE";
	
	public void tradeLog(UUID my, DependType myType, UUID target, DependType targetType, int amount, String argsType, String args)
	{
		
	}
	
	public void internalSystemLog(UUID target, DependType targetType, int amount, String argsType, String args)
	{
		
	}
}
