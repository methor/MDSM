/**
 * @author hengxin
 * @creation 2013-8-8; 2014-05-07
 * @file IAtomicRegisterClient.java
 *
 * @description interface for the "client" part of the "client/server" architecture
 *  of the simulated register system model;
 *  it is responsible for handling the invocations of operations on simulated register
 */
package consistencyinfrastructure.architecture;


import java.io.Serializable;

public interface IRegisterClient<P extends Serializable, K extends Serializable, V extends Serializable>
{
	/**
	 * "get" operation invocation
	 * @param key key to get
	 * @return Versioned value associated with the key
	 */
	public V get(K key);

	/**
	 * "put" operation invocation
	 * @param key to put
	 * @param val non-versioned value associated with the key
	 * @return value to put associated with the key
	 */
	public V put(K key, P val);

	public V getReservedValue();

}
