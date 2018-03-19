package weakconsistency;

import java.io.Serializable;

import consistencyinfrastructure.architecture.IRegisterServer;
import consistencyinfrastructure.data.kvs.Key;
import log.NetworkLog;

/**
 * Created by Mio on 2016/3/7.
 */
public enum WeakConsistencyServer implements IRegisterServer {

    INSTANCE;


    NetworkLog networkLog = null;

    public void registerNetworkLog(NetworkLog log)
    {
        this.networkLog = log;
    }



    public void handleWeakConsistencyMessage(WeakConsistencyMessage msg)
    {

        Key key = msg.key;
        Serializable val = msg.val;

        KVStoreInMemory.INSTANCE.put(key, val);

//        if (networkLog != null)
//            networkLog.logNetworkLatency(msg);

    }



}
