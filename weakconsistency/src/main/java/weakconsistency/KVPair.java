/**
 * @author hengxin
 * @creation 2013-8-28
 * @file KVPair.java
 *
 * @description
 */
package weakconsistency;


import consistencyinfrastructure.data.kvs.Key;

import java.io.Serializable;

/**
 * @author hengxin
 * @date 2013-8-28
 * @description single key-value pair
 */
public class KVPair
{
	private Key key;
	private Serializable val;

	public KVPair(Key key, Serializable val)
	{
		this.key = key;
		this.val = val;
	}

	/**
	 * @return {@link #key}: of Key
	 */
	public Key getKey()
	{
		return this.key;
	}

	/**
	 * @return {@link #val}: of VersionValue
	 */
	public Object getVal()
	{
		return this.val;
	}
	
	/**
	 * Key : key; VersionValue
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Key : ").append(this.key).append(';').append(this.val.toString());
		return sb.toString();
	}
}
