package kr.dja.aldarEconomy.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;

import kr.dja.aldarEconomy.setting.ConfigurationMember;

public class DBAccess implements ConfigurationMember
{
	private static final String KEY_DBADDR = "dbAddr";
	private static final String KEY_DBPORT = "dbPort";
	private static final String KEY_DBID = "dbID";
	private static final String KEY_DBPW = "dbPassword";
	private static final String KEY_DBNAME = "dbName";
	
	private final Logger logger;
	
	private String dbAddr;
	private int dbPort;
	private String dbID;
	private String dbPW;
	private String dbName;
	
	private Connection connection;
	
	public DBAccess(Logger logger)
	{
		this.logger = logger;
		
		this.dbAddr = "dja.kr";
		this.dbPort = 3306;
		this.dbID = "";
		this.dbPW = "";
		this.connection = null;
	}
	
	public synchronized void connect()
	{
		if(this.connection != null)
		{
			this.logger.log(Level.WARNING, "db접속을 먼저 끊으세요");
			return;
		}
		
		String url = "jdbc:mariadb://"+this.dbAddr+":"+this.dbPort+"/"+this.dbName;
		
		try
		{
			Connection con = DriverManager.getConnection(url, this.dbID, this.dbPW);
			this.connection = con;
		}
		catch (SQLException e)
		{
			this.logger.log(Level.WARNING, "DB접속 실패:"+e.getMessage());
		}
		
	}
	
	public synchronized void disconnect()
	{
		if(this.connection == null)
		{
			this.logger.log(Level.WARNING, "db연결전 종료를 시도함.");
		}
		try
		{
			this.connection.close();
		} catch (SQLException e)
		{
			this.logger.log(Level.WARNING, "DB접속 종료중 오류:"+e.getMessage());
		}
		this.connection = null;
		
	}
	
	public synchronized boolean isConnect()
	{
		if(this.connection == null)
		{
			return false;
		}
		return true;
	}


	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> map = new HashMap<>();
		map.put(KEY_DBADDR, this.dbAddr);
		map.put(KEY_DBPORT, this.dbPort);
		map.put(KEY_DBID, this.dbID);
		map.put(KEY_DBPW, this.dbPW);
		map.put(KEY_DBNAME, this.dbName);
		return map;
	}


	@Override
	public boolean installData(ConfigurationSection section)
	{
		try
		{
			String addr = section.getString(KEY_DBADDR);
			int port = section.getInt(KEY_DBPORT);
			String id = section.getString(KEY_DBID);
			String pw = section.getString(KEY_DBPW);
			String name = section.getString(KEY_DBNAME);
			this.dbAddr = addr;
			this.dbPort = port;
			this.dbID = id;
			this.dbPW = pw;
			this.dbName = name;
		} catch(Exception e)
		{
			return false;
		}
		return true;
	}
	
	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append("addr:"+this.dbAddr+":"+this.dbPort+"\n");
		buf.append("id:"+this.dbID+"\n");
		buf.append("dbName:"+this.dbName);
		return buf.toString();
	}
	
}
