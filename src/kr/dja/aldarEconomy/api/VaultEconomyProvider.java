package kr.dja.aldarEconomy.api;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import kr.dja.aldarEconomy.AldarEconomy;
import kr.dja.aldarEconomy.EconomyUtil;
import kr.dja.aldarEconomy.api.token.APITokenManager;
import kr.dja.aldarEconomy.api.token.SystemID;
import kr.dja.aldarEconomy.bank.Bank;
import kr.dja.aldarEconomy.data.EconomyDataStorage;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

public class VaultEconomyProvider implements Economy
{
	private static final String CAUSE_API_ACCESS = "valut_api_access";
	
	private final Plugin plugin;
	private AldarEconomyProvider provider;
	
	private final BukkitScheduler scheduler;
	
	private boolean isEnable;
	
	private final SystemID valutToken;
	
	
	public VaultEconomyProvider(APITokenManager tokenManager, Plugin plugin, AldarEconomyProvider provider)
	{
		this.valutToken = tokenManager.takeOrRegisterAPIToken("vault");
		this.plugin = plugin;
		this.provider = provider;
		this.scheduler = Bukkit.getScheduler();
	}
	
	public void setState(boolean state)
	{
		this.isEnable = state;
	}
	
	 /**
     * Checks if economy method is enabled.
     * @return Success or Failure
     */
	@Override
    public boolean isEnabled()
    {
    	return this.isEnable;
    }

    /**
     * Gets name of economy method
     * @return Name of Economy Method
     */
	@Override
    public String getName()
    {
    	return AldarEconomy.PLUGIN_NAME;
    }

    /**
     * Returns true if the given implementation supports banks.
     * @return true if the implementation supports banks
     */
	@Override
    public boolean hasBankSupport()
    {
    	return false;
    }

    /**
     * Some economy plugins round off after a certain number of digits.
     * This function returns the number of digits the plugin keeps
     * or -1 if no rounding occurs.
     * @return number of digits after the decimal point kept
     */
	@Override
    public int fractionalDigits()
    {
    	return 1;
    }

    /**
     * Format amount into a human readable String This provides translation into
     * economy specific formatting to improve consistency between plugins.  
     *
     * @param amount to format
     * @return Human readable string describing amount
     */
	@Override
    public String format(double amount)
    {
    	return Integer.toString((int)amount);
    }

    /**
     * Returns the name of the currency in plural form.
     * If the economy being used does not support currency names then an empty string will be returned.
     * 
     * @return name of the currency (plural)
     */
	@Override
    public String currencyNamePlural()
    {
    	return "";
    }


    /**
     * Returns the name of the currency in singular form.
     * If the economy being used does not support currency names then an empty string will be returned.
     * 
     * @return name of the currency (singular)
     */
	@Override
    public String currencyNameSingular()
    {
    	return "";
    }

    /**
     * 
     * @deprecated As of VaultAPI 1.4 use {@link #hasAccount(OfflinePlayer)} instead.
     */
    @Deprecated
    @Override
    public boolean hasAccount(String playerName)
    {
    	return true;
    }

    /**
     * Checks if this player has an account on the server yet
     * This will always return true if the player has joined the server at least once
     * as all major economy plugins auto-generate a player account when the player joins the server
     * 
     * @param player to check
     * @return if the player has an account
     */
    @Override
    public boolean hasAccount(OfflinePlayer player)
    {
    	return true;
    }
    
    /**
     * @deprecated As of VaultAPI 1.4 use {@link #hasAccount(OfflinePlayer, String)} instead.
     */
    @Deprecated
    @Override
    public boolean hasAccount(String playerName, String worldName)
    {
    	return true;
    }

    /**
     * Checks if this player has an account on the server yet on the given world
     * This will always return true if the player has joined the server at least once
     * as all major economy plugins auto-generate a player account when the player joins the server
     * 
     * @param player to check in the world
     * @param worldName world-specific account
     * @return if the player has an account
     */
    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName)
    {
    	return true;
    }

    /**
     * @deprecated As of VaultAPI 1.4 use {@link #getBalance(OfflinePlayer)} instead.
     */
    @Deprecated
    @Override
    public double getBalance(String playerName)
    {
    	OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
    	return this.getBalance(player);
    }
    /**
     * Gets balance of a player
     * 
     * @param player of the player
     * @return Amount currently held in players account
     */
    public double getBalance(OfflinePlayer player)
    {
    	return this.provider.getPlayerInventoryMoney(player);
    }

    /**
     * @deprecated As of VaultAPI 1.4 use {@link #getBalance(OfflinePlayer, String)} instead.
     */
    @Deprecated
    public double getBalance(String playerName, String world)
    {
    	return this.getBalance(playerName);
    }
    
    /**
     * Gets balance of a player on the specified world.
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     * @param player to check
     * @param world name of the world
     * @return Amount currently held in players account
     */
    public double getBalance(OfflinePlayer player, String world)
    {
    	return this.getBalance(player);
    }
    
    /**
     * @deprecated As of VaultAPI 1.4 use {@link #has(OfflinePlayer, double)} instead.
     */
    @Deprecated
    public boolean has(String playerName, double amount)
    {
    	OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
    	return this.has(player, amount);
    }

    /**
     * Checks if the player account has the amount - DO NOT USE NEGATIVE AMOUNTS
     * 
     * @param player to check
     * @param amount to check for
     * @return True if <b>player</b> has <b>amount</b>, False else wise
     */
    public boolean has(OfflinePlayer player, double amount)
    {
    	int playerAmount = this.provider.getPlayerInventoryMoney(player);
    	if(amount <= playerAmount) return true;
    	return false;
    }
    
    /**
     * @deprecated As of VaultAPI 1.4 use @{link {@link #has(OfflinePlayer, String, double)} instead.
     */
    @Deprecated
    public boolean has(String playerName, String worldName, double amount)
    {
    	return this.has(playerName, amount);
    }
    
    /**
     * Checks if the player account has the amount in a given world - DO NOT USE NEGATIVE AMOUNTS
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     * 
     * @param player to check
     * @param worldName to check with
     * @param amount to check for
     * @return True if <b>player</b> has <b>amount</b>, False else wise
     */
    public boolean has(OfflinePlayer player, String worldName, double amount)
    {
    	return this.has(player, amount);
    }

    /**
     * @deprecated As of VaultAPI 1.4 use {@link #withdrawPlayer(OfflinePlayer, double)} instead.
     */
    @Deprecated
    public EconomyResponse withdrawPlayer(String playerName, double amount)
    {
    	OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
    	return this.withdrawPlayer(player, amount);
    }
    
    /**
     * Withdraw an amount from a player - DO NOT USE NEGATIVE AMOUNTS
     * 
     * @param player to withdraw from
     * @param amount Amount to withdraw
     * @return Detailed response of transaction
     */
    public EconomyResponse withdrawPlayer(OfflinePlayer op, double amount)
    {
    	if(!this.isEnable) return new EconomyResponse(0, 0, ResponseType.FAILURE, null);
    	if(op == null) return new EconomyResponse(0, 0, ResponseType.FAILURE, null);
    	HumanEntity player = Bukkit.getPlayer(op.getUniqueId());
    	int intAmount = (int)amount;
    	EconomyResult result = this.provider.withdrawPlayer(player, intAmount, this.valutToken, CAUSE_API_ACCESS, "withdrawPlayer");
    	if(result == EconomyResult.OK)
    	{
    		int playerMoney = this.provider.getPlayerInventoryMoney(op);
    		return new EconomyResponse(-intAmount, playerMoney, ResponseType.SUCCESS, null);
    	}

    	return new EconomyResponse(0, 0, ResponseType.FAILURE, result.toString());
    }

    /**
     * @deprecated As of VaultAPI 1.4 use {@link #withdrawPlayer(OfflinePlayer, String, double)} instead.
     */
    @Deprecated
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount)
    {
    	OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
    	return this.withdrawPlayer(player, amount);
    }
    
    /**
     * Withdraw an amount from a player on a given world - DO NOT USE NEGATIVE AMOUNTS
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     * @param player to withdraw from
     * @param worldName - name of the world
     * @param amount Amount to withdraw
     * @return Detailed response of transaction
     */
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount)
    {
    	return this.withdrawPlayer(player, amount);
    }

    /**
     * @deprecated As of VaultAPI 1.4 use {@link #depositPlayer(OfflinePlayer, double)} instead.
     */
    @Deprecated
    public EconomyResponse depositPlayer(String playerName, double amount)
    {
    	OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
    	return this.depositPlayer(player, amount);
    }

    /**
     * Deposit an amount to a player - DO NOT USE NEGATIVE AMOUNTS
     * 
     * @param player to deposit to
     * @param amount Amount to deposit
     * @return Detailed response of transaction
     */
    public EconomyResponse depositPlayer(OfflinePlayer op, double amount)
    {
    	if(!this.isEnable) return new EconomyResponse(0, 0, ResponseType.FAILURE, null);
    	if(op == null) return new EconomyResponse(0, 0, ResponseType.FAILURE, null);
    	HumanEntity player = Bukkit.getPlayer(op.getUniqueId());
    	int intAmount = (int)amount;
    	EconomyResult result = this.provider.depositPlayer(player, intAmount, this.valutToken, CAUSE_API_ACCESS, "depositPlayer");
    	if(result == EconomyResult.OK)
    	{
    		int playerMoney = this.provider.getPlayerInventoryMoney(op);
    		return new EconomyResponse(intAmount, playerMoney, ResponseType.SUCCESS, null);
    	}

    	return new EconomyResponse(0, 0, ResponseType.FAILURE, result.toString());
    }

    /**
     * @deprecated As of VaultAPI 1.4 use {@link #depositPlayer(OfflinePlayer, String, double)} instead.
     */
    @Deprecated
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount)
    {
    	OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
    	return this.depositPlayer(player, amount);
    }
   
    /**
     * Deposit an amount to a player - DO NOT USE NEGATIVE AMOUNTS
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     * 
     * @param player to deposit to
     * @param worldName name of the world
     * @param amount Amount to deposit
     * @return Detailed response of transaction
     */
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount)
    {
    	return this.depositPlayer(player, amount);
    }

    /**
     * @deprecated As of VaultAPI 1.4 use {{@link #createBank(String, OfflinePlayer)} instead.
     */
    @Deprecated
    public EconomyResponse createBank(String name, String player)
    {
    	OfflinePlayer op = Bukkit.getOfflinePlayer(player);
    	return this.createBank(name, op);
    }

    /**
     * Creates a bank account with the specified name and the player as the owner
     * @param name of account
     * @param player the account should be linked to
     * @return EconomyResponse Object
     */
    public EconomyResponse createBank(String name, OfflinePlayer player)
    {
    	return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, null);
    }

    /**
     * Deletes a bank account with the specified name.
     * @param name of the back to delete
     * @return if the operation completed successfully
     */
    public EconomyResponse deleteBank(String name)
    {
    	return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, null);
    }

    /**
     * Returns the amount the bank has
     * @param name of the account
     * @return EconomyResponse Object
     */
    public EconomyResponse bankBalance(String name)
    {
    	return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, null);
    }

    /**
     * Returns true or false whether the bank has the amount specified - DO NOT USE NEGATIVE AMOUNTS
     * 
     * @param name of the account
     * @param amount to check for
     * @return EconomyResponse Object
     */
    public EconomyResponse bankHas(String name, double amount)
    {
    	return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, null);
    }

    /**
     * Withdraw an amount from a bank account - DO NOT USE NEGATIVE AMOUNTS
     * 
     * @param name of the account
     * @param amount to withdraw
     * @return EconomyResponse Object
     */
    public EconomyResponse bankWithdraw(String name, double amount)
    {
    	return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, null);
    }

    /**
     * Deposit an amount into a bank account - DO NOT USE NEGATIVE AMOUNTS
     * 
     * @param name of the account
     * @param amount to deposit
     * @return EconomyResponse Object
     */
    public EconomyResponse bankDeposit(String name, double amount)
    {
    	return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, null);
    }
    
    /**
     * @deprecated As of VaultAPI 1.4 use {{@link #isBankOwner(String, OfflinePlayer)} instead.
     */
    @Deprecated
    public EconomyResponse isBankOwner(String name, String playerName)
    {
    	OfflinePlayer op = Bukkit.getOfflinePlayer(playerName);
    	return this.isBankOwner(name, op);
    }
    
    /**
     * Check if a player is the owner of a bank account
     * 
     * @param name of the account
     * @param player to check for ownership
     * @return EconomyResponse Object
     */
    public EconomyResponse isBankOwner(String name, OfflinePlayer player)
    {
    	return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, null);
    }

    /**
     * @deprecated As of VaultAPI 1.4 use {{@link #isBankMember(String, OfflinePlayer)} instead.
     */
    @Deprecated
    public EconomyResponse isBankMember(String name, String playerName)
    {
    	OfflinePlayer op = Bukkit.getOfflinePlayer(playerName);
    	return this.isBankMember(name, op);
    }
    
    /**
     * Check if the player is a member of the bank account
     * 
     * @param name of the account
     * @param player to check membership
     * @return EconomyResponse Object
     */
    public EconomyResponse isBankMember(String name, OfflinePlayer player)
    {
    	return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, null);
    }

    /**
     * Gets the list of banks
     * @return the List of Banks
     */
    public List<String> getBanks()
    {
    	return null;
    }

    /**
     * @deprecated As of VaultAPI 1.4 use {{@link #createPlayerAccount(OfflinePlayer)} instead.
     */
    @Deprecated
    public boolean createPlayerAccount(String playerName)
    {
    	return true;
    }
    
    /**
     * Attempts to create a player account for the given player
     * @param player OfflinePlayer
     * @return if the account creation was successful
     */
    public boolean createPlayerAccount(OfflinePlayer player)
    {
    	return true;
    }
    
    /**
     * @deprecated As of VaultAPI 1.4 use {{@link #createPlayerAccount(OfflinePlayer, String)} instead.
     */
    @Deprecated
    public boolean createPlayerAccount(String playerName, String worldName)
    {
    	return true;
    }
    
    /**
     * Attempts to create a player account for the given player on the specified world
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this then false will always be returned.
     * @param player OfflinePlayer
     * @param worldName String name of the world
     * @return if the account creation was successful
     */
    public boolean createPlayerAccount(OfflinePlayer player, String worldName)
    {
    	return true;
    }
    
}
