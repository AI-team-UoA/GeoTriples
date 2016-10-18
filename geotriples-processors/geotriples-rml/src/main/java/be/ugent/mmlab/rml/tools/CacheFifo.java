package be.ugent.mmlab.rml.tools;

import java.util.LinkedHashMap;
import java.util.Map;

public class CacheFifo<K, V> extends LinkedHashMap<K, V> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int cacheSize;
	public CacheFifo(int cacheSize) {
		super(16,  0.75f, true);
		this.cacheSize = cacheSize;
	}
	
	@Override
	public V put(K key, V value) {
		V result = super.put(key, value);
		return result;
	}
	
	
	@Override
	public V get(Object key) {
		V result = super.get(key);
		return result;
	}
	
	@Override
	public boolean containsKey(Object key) {
		boolean result=super.containsKey(key);
		return result;
	}
	
	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() >= cacheSize;
	}
}
