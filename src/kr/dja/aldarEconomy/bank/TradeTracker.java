package kr.dja.aldarEconomy.bank;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;

import kr.dja.aldarEconomy.IntLocation;
import kr.dja.aldarEconomy.api.token.APITokenManager;
import kr.dja.aldarEconomy.api.token.SystemID;
import kr.dja.aldarEconomy.entity.DependType;


public class TradeTracker
{
	public static final String SYSTEM_CAUSE = "system";
	
	private final Logger logger;
	private final APITokenManager tokenManager;
	
	public TradeTracker(Logger logger, APITokenManager tokenManager)
	{
		this.logger = logger;
		this.tokenManager = tokenManager;
	}
	
	public void tradeLog(UUID my, DependType myType, UUID target, DependType targetType, int amount, IntLocation loc, String cause, String args)
	{
		String myName = null, targetName = null;
		if(myType == DependType.PLAYER) myName = Bukkit.getPlayer(my).getName();
		else if(myType == DependType.SYSTEM) myName = this.tokenManager.getIDToken(my).name;
		if(targetType == DependType.PLAYER) targetName = Bukkit.getPlayer(target).getName();
		else if(targetType == DependType.SYSTEM) targetName = this.tokenManager.getIDToken(target).name;
		Bukkit.broadcastMessage("[internal trade] my:"+myName+"("+myType+") target:"+targetName+"("+targetType+") amount:"+amount+" loc:"+loc+" cause:"+cause+" args:"+args);
	}
	
	public void forceRebalancing(UUID playerUID, int amount, String type, IntLocation loc)
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
		logger.log(Level.WARNING, "[force rebalancing] trigger:"+causeName+" action:"+type+" amount:"+amount+" loc:"+loc);
	}
	
	public void normalConsume(SystemID system, UUID depend, DependType type, int amount, IntLocation loc, String cause, String args)
	{
		String playerName;
		if(type == DependType.PLAYER) playerName = Bukkit.getPlayer(depend).getName();
		else playerName = system.name + "(sys)";
		logger.log(Level.INFO, "[consume] system:"+system.name+" owner:"+playerName+" amount:"+amount+" location:"+loc+" cause:"+cause+" args:"+args);
	}
	
	public void normalIssuance(SystemID system, int amount, IntLocation loc, String cause, String args)
	{
		logger.log(Level.INFO, "[issuance] system:"+system.name+" amount:"+amount+" location:"+loc+" cause:"+amount+" args:"+args);
	}
}
