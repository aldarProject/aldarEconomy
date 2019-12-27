package kr.dja.aldarEconomy;

import java.util.logging.Logger;

public class DBAccess
{
	private static final String KEY_DBADDR = "dbAddr";
	private static final String KEY_DBID = "dbID";
	private static final String KEY_DBPW = "dbPassword";
	

	private final Logger logger;
	
	
	public DBAccess(Logger logger)
	{
		this.logger = logger;
	}
	
}
