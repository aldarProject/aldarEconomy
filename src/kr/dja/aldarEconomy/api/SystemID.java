package kr.dja.aldarEconomy.api;

public class SystemID
{
	public final String name;
	
	SystemID(String uniqueName)
	{
		this.name = uniqueName;
	}
	
	@Override
	public int hashCode()
	{
	    int result = 1;
	    result = 31 * ((name == null) ? 0 : name.hashCode());
	    return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(super.equals(obj))
		{
			if(obj instanceof SystemID)
			{
				SystemID id = (SystemID)obj;
				if(id.name.equals(this.name)) return true;
			}
		}
		return false;
	}
}
