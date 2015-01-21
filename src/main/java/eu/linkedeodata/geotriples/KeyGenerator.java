package eu.linkedeodata.geotriples;

import java.util.HashMap;
/**
 * Generates a key to be used as private key for shape file table.
 * Generates either on the fly(Generate), or using a hash map of strings(GenerateFromMap)
 * @author Dimitrianos Savva NKUA (d.savva@di.uoa.gr)
 */
public class KeyGenerator {
	public enum Use{
		USE_PREV,
		NEW_ONE,
	}
	private static int key=0;
	private HashMap<String,Integer> memory;
	public KeyGenerator()
	{
		memory=new HashMap<String,Integer>();
	}
	
	public int GenerateFromMap(String somekey)
	{
		if(memory.containsKey(somekey))
		{
			return memory.get(somekey);
		}
		else
		{
			memory.put(somekey, ++key);
			return key;
		}
	}
	
	public static int Generate(Use purpose) throws Exception
	{
		if(purpose.equals(Use.USE_PREV))
		{
			return key;
		}
		else if(purpose.equals(Use.NEW_ONE))
		{
			return ++key;
		}
		else
		{
			throw new Exception("Use purpose unsupported");
		}
	}
	
	public static int Generate() throws Exception
	{
		return Generate(Use.NEW_ONE);
	}
	
	public static String Generate(String prefix,Use purpose) throws Exception
	{
		if(purpose.equals(Use.USE_PREV))
		{
			return String.format("%s%d",prefix,key);
		}
		else if(purpose.equals(Use.NEW_ONE))
		{
			return String.format("%s%d",prefix,++key);
		}
		else
		{
			throw new Exception("Use purpose unsupported");
		}
	}
	public static String Generate(String prefix) throws Exception
	{
		return Generate(prefix,Use.NEW_ONE);
	}

	public static void reset() {
		key=0;
	}
}
