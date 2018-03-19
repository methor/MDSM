package dsm;


import java.io.Serializable;

import causalconsistency.CausalConsistencyClient;
import causalconsistency.CausalConsistencyMessagingService;
import causalconsistency.CausalConsistencyServer;
import causalconsistency.KVStoreInMemory;
import causalconsistency.ReservedValue;
import consistencyinfrastructure.communication.MessagingService;
import consistencyinfrastructure.data.kvs.Key;
import consistencyinfrastructure.login.SessionManager;


/**
 * Created by Mio on 2016/3/7.
 */
public class CausalDsm extends AbstractDsm<Serializable, Key, Serializable> {


    private static CausalDsm instance = null;

    private MessagingService.ServerTask serverTask;

    private CausalDsm()
    {
        MessagingService.CAUSAL.registerReceiver(CausalConsistencyMessagingService.INSTANCE);
        CausalConsistencyServer.INSTANCE.init();    // a little messy, I may refactor dsm api structure in the future
        serverTask = MessagingService.CAUSAL.new ServerTask(SessionManager.getNewInstance().getNodeIp());
        serverTask.start();
    }


    public synchronized static CausalDsm INSTANCE()
    {
        if (instance == null)
            instance = new CausalDsm();
        return instance;
    }

    @Override
    public Serializable put(Key key, Serializable val)
    {
        //// TODO: 2016/3/7
        return CausalConsistencyClient.INSTANCE.put(key, val);

    }

    @Override
    public Serializable get(Key key)
    {
        return CausalConsistencyClient.INSTANCE.get(key);
    }

    @Override
    public MessagingService getMessagingService() {
        return MessagingService.CAUSAL;
    }

    @Override
    public void onDestroy()
    {
        CausalConsistencyServer.INSTANCE.onDestroy();
        serverTask.onDestroy();
        KVStoreInMemory.INSTANCE.clean();
        instance = null;
    }

    @Override
    public Serializable getReservedValue()
    {
        return ReservedValue.RESERVED_VALUE;
    }
}
