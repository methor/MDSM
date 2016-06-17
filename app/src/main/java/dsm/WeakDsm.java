package dsm;


import consistencyinfrastructure.communication.MessagingService;
import consistencyinfrastructure.data.kvs.Key;
import consistencyinfrastructure.login.SessionManager;
import weakconsistency.KVStoreInMemory;
import weakconsistency.ReservedValue;
import weakconsistency.WeakConsistencyClient;
import weakconsistency.WeakConsistencyMessagingService;

import java.io.Serializable;

/**
 * Created by Mio on 2016/3/7.
 */
public class WeakDsm extends AbstractDsm<Serializable, Key, Serializable> {


    private static WeakDsm instance = null;

    private MessagingService.ServerTask serverTask;

    private WeakDsm()
    {
        MessagingService.WEAK.registerReceiver(WeakConsistencyMessagingService.INSTANCE);
        serverTask = MessagingService.WEAK.new ServerTask(SessionManager.getNewInstance().getNodeIp());
        serverTask.start();
    }

    public MessagingService getMessagingService()
    {
        return MessagingService.WEAK;
    }


    public synchronized static WeakDsm INSTANCE()
    {
        if (instance == null)
            instance = new WeakDsm();
        return instance;
    }

    @Override
    public Serializable put(Key key, Serializable val)
    {
        //// TODO: 2016/3/7
        return WeakConsistencyClient.INSTANCE.put(key, val);

    }

    @Override
    public Serializable get(Key key)
    {
        return WeakConsistencyClient.INSTANCE.get(key);
    }

    @Override
    public void onDestroy()
    {
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
