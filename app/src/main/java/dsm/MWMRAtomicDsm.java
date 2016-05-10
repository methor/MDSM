package dsm;

import consistencyinfrastructure.communication.MessagingService;
import consistencyinfrastructure.data.kvs.Key;
import consistencyinfrastructure.login.SessionManager;
import ics.mobilememo.sharedmemory.atomicity.AtomicityRegisterClientFactory;
import ics.mobilememo.sharedmemory.atomicity.message.*;
import ics.mobilememo.sharedmemory.data.kvs.VersionValue;
import ics.mobilememo.sharedmemory.data.kvs.kvstore.KVStoreInMemory;

/**
 * Created by Mio on 2015/12/21.
 */
public class MWMRAtomicDsm extends AbstractDsm<String, Key, VersionValue> {

    private static MWMRAtomicDsm instance = null;
    MessagingService.ServerTask serverTask;
    public static final String TAG = MWMRAtomicDsm.class.getName();

    private MWMRAtomicDsm()
    {
        try
        {
            AtomicityRegisterClientFactory.INSTANCE.setAtomicityRegisterClient(
                    AtomicityRegisterClientFactory.MWMR_ATOMICITY);
            MessagingService.MATO
                    .registerReceiver(AtomicityMessagingService.INSTANCE);

            serverTask = MessagingService.MATO.new ServerTask(SessionManager.getNewInstance().getNodeIp());
            serverTask.start();

        } catch (AtomicityRegisterClientFactory.NoSuchAtomicAlgorithmSupported noSuchAtomicAlgorithmSupported)
        {
            noSuchAtomicAlgorithmSupported.printStackTrace();
        }

    }

    public synchronized static MWMRAtomicDsm INSTANCE()
    {
        if (instance == null)
            instance = new MWMRAtomicDsm();
        return instance;

    }


    @Override
    public VersionValue put(Key key, String val)
    {
//        Log.d(TAG, "issue put");
        return AtomicityRegisterClientFactory.INSTANCE.getAtomicityRegisterClient().put(key, val);
    }

    @Override
    public VersionValue get(Key key)
    {
//        Log.d(TAG, "issue get");
        return AtomicityRegisterClientFactory.INSTANCE.getAtomicityRegisterClient().
                get(key);
    }

    @Override
    public void onDestroy()
    {
        serverTask.onDestroy();
        KVStoreInMemory.INSTANCE.clean();
        instance = null;
    }


    @Override
    public VersionValue getReservedValue()
    {
        return AtomicityRegisterClientFactory.INSTANCE.getAtomicityRegisterClient().getReservedValue();
    }


}
