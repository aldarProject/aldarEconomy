package kr.dja.aldarEconomy.dataStorage.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EconomyMapChild<DependP, Depend> extends EconomyMap<Depend>
{
	public final UUID uuid;
	public final List<DependP> parents;
	
	public EconomyMapChild()
	{
		super();
		this.uuid = UUID.randomUUID();
		this.parents = new ArrayList<>();
	}
	
}
