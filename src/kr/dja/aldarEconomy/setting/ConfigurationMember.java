package kr.dja.aldarEconomy.setting;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public interface ConfigurationMember extends ConfigurationSerializable
{
	public boolean installData(ConfigurationSection section);
}
