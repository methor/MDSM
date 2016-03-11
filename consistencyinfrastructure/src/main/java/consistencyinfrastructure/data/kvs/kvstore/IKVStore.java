/**
 * @author hengxin
 * @date May 8, 2014
 * @description interface for kvstore supporting put, get, and remove methods.
 *  for its concrete classes, you can store the data in memory, in file, or in database.  
 */
package consistencyinfrastructure.data.kvs.kvstore;


import java.io.Serializable;

public interface IKVStore<T extends Serializable, K extends Serializable>
{
	/**
	 * put {@link Key} + {@link VersionValue} into the kvstore
	 * @param key specified {@link Key}
	 * @param vval {@link VersionValue} associated with the {@link Key}
	 */
	public void put(K key, T vval);
	
	/**
	 * return the {@link VersionValue} associated with the specified {@link Key}
	 * @param key {@link Key} to query
	 * @return {@link VersionValue} associated with the specified {@link Key}
	 */
	public T get(K key);
	
	/**
	 * remove the {@link VersionValue} associated with the specified {@link Key}
	 * @param key {@link Key} specified
	 */
	public void remove(K key);

	public void clean();
}
