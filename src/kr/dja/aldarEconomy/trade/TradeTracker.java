package kr.dja.aldarEconomy.trade;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;

import kr.dja.aldarEconomy.api.APITokenManager;
import kr.dja.aldarEconomy.dataObject.DependType;
import kr.dja.aldarEconomy.dataObject.IntLocation;


public class TradeTracker
{
	private final Logger logger;
	private final APITokenManager tokenManager;
	public TradeTracker(Logger logger, APITokenManager tokenManager)
	{
		this.logger = logger;
		this.tokenManager = tokenManager;
	}
	
	public void tradeLog(UUID my, DependType myType, UUID target, DependType targetType, int amount, String type, IntLocation loc)
	{
		String myName = null, targetName = null;
		if(myType == DependType.PLAYER) myName = Bukkit.getPlayer(my).getName();
		else if(myType == DependType.SYSTEM) myName = this.tokenManager.getIDToken(my).name;
		if(targetType == DependType.PLAYER) targetName = Bukkit.getPlayer(target).getName();
		else if(targetType == DependType.SYSTEM) targetName = this.tokenManager.getIDToken(target).name;
		Bukkit.broadcastMessage("[internal trade] my:"+myName+"("+myType+") target:"+targetName+"("+targetType+") amount:"+amount+" action:"+type+" loc:"+loc);
	}
	
	public void forceIssuance(UUID playerUID, int amount, String type, IntLocation loc)
	{
		String causeName;
		if(playerUID != null)
		{
			causeName = Bukkit.getPlayer(playerUID).getName()+"("+playerUID+")";
		}
		else
		{
			causeName = "System";
		}
		logger.log(Level.WARNING, "[force issuance] trigger:"+causeName+" action:"+type+" amount:"+amount+" loc:"+loc);
	}
}
