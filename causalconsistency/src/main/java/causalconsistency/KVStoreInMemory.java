
package causalconsistency;


import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import consistencyinfrastructure.data.kvs.Key;
import consistencyinfrastructure.data.kvs.VectorTimestamp;
import consistencyinfrastructure.data.kvs.kvstore.IKVStore;

/**
 * @author hms
 * @date 2013-8-10 2013-8-28
 * @description collection of key-value pairs stored;
 * 	each server replica holds its own local key-value store.
 *
 * Note: the KVS may be accessed concurrently.
 *
 * Singleton design pattern with Java enum which is simple and thread-safe
 */
public enum KVStoreInMemory implements IKVStore<Serializable, Key>
{
	INSTANCE;	// it is thread-safe

	private static final String TAG = KVStoreInMemory.class.getName();

	// Using the thread-safe ConcurrentHashMap to cope with the multi-thread concurrency.
	private final ConcurrentMap<Key, Serializable> key_val_map = new ConcurrentHashMap<>();
	private VectorTimestamp vectorTimestamp = new VectorTimestamp();
	
	/**
	 * @author hengxin
	 * @date 2013-9-2, 2014-05-15
	 * @description multiple separate locks for concurrent reads and concurrent writes
	 * 	when some write is synchronized such as in {@link #put(Key, VersionValue)} method
	 * 
	 * @see http://vanillajava.blogspot.com/2010/05/locking-concurrenthashmap-for-exclusive.html
	 * 
	 * OR, to use this method: http://stackoverflow.com/q/24732585/1833118
	 */


	public synchronized VectorTimestamp getVectorTimestamp() {
		return vectorTimestamp;
	}

	/**
	 * multi-thread access:
	 * Invariant: the sequence of timestamp values taken on by the replica on any server
	 * is nondecreasing during any execution of the algorithm
	 * To ensure the invariant, the if-then pattern should be locked.
	 *
	 * put the key-value pair into the key-value store
	 * @param key Key to identify
	 * @param val VersionValue associated with the Key
	 */
	public void put(Key key, Serializable val)
	{
		/**
		 * instead of <code>VersionValue current_vval = this.key_vval_map.get(key);</code>
		 * 
		 * maybe return {@link VersionValue#NULL_VERSIONVALUE}
		 */

		key_val_map.put(key, val);



	}

	/**
	 * Given Key, return the VersionValue associated;
	 * if no mapping for the specified key is found, return NULL_VERSIONVALUE
	 *
	 */
	public Serializable get(Key key)
	{
		Serializable val =  key_val_map.get(key);
		return val != null ? val : ReservedValue.RESERVED_VALUE;

	}

	/**
	 * remove the key and associated value from the kvs
	 * @param key key to identify and remove
	 */
	public void remove(Key key)
	{

		key_val_map.remove(key);

	}

	/**
	 * add by hms; for restoring KVStore to initial state
	 */
	public void clean()
	{
		key_val_map.clear();
		vectorTimestamp = new VectorTimestamp();
	}
}


