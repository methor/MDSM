package weakconsistency;

import consistencyinfrastructure.architecture.IRegisterServer;
import consistencyinfrastructure.data.kvs.Key;

import java.io.Serializable;

/**
 * Created by Mio on 2016/3/7.
 */
public enum WeakConsistencyServer implements IRegisterServer {

    INSTANCE;



    public void handleWeakConsistencyMessage(WeakConsistencyMessage msg)
    {

        Key key = msg.key;
        Serializable val = msg.val;
        KVStoreInMemory.INSTANCE.put(key, val);

    }

}
