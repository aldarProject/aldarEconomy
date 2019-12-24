package kr.dja.aldarEconomy.trade;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

import kr.dja.aldarEconomy.api.SystemID;
import kr.dja.aldarEconomy.setting.MoneyMetadata;

public class ConsumeMoneyCheckResult
{
	public final ConsumeMoneyResultType result;
	final ItemStack[] invContents;
	final ModifyInventoryInfo[] modifyInfo;
	
	final SystemID id;
	final String args;
	final HumanEntity player;
	final int amount;
	
	ConsumeMoneyCheckResult(ConsumeMoneyResultType result)
	{
		this.result = result;
		this.invContents = null;
		this.modifyInfo = null;
		this.id = null;
		this.args = null;
		this.player = null;
		this.amount = 0;
	}
	
	ConsumeMoneyCheckResult(ConsumeMoneyResultType result, ItemStack[] invContents, ModifyInventoryInfo[] modifyInfo, SystemID id, String args, HumanEntity player, int amount)
	{
		this.result = result;
		this.invContents = invContents;
		this.modifyInfo = modifyInfo;
		this.id = id;
		this.args = args;
		this.player = player;
		this.amount = amount;
	}
}

class ModifyInventoryInfo
{
	int actionModify;
	int actionMeta;
	
	ModifyInventoryInfo()
	{
		this.actionModify = 0;
		this.actionMeta = -1;
	}
}