package dsm;

import consistencyinfrastructure.data.kvs.Key;
import consistencyinfrastructure.login.SessionManager;
import consistencyinfrastructure.communication.MessagingService;
import ics.mobilememo.sharedmemory.atomicity.AtomicityRegisterClientFactory;
import ics.mobilememo.sharedmemory.atomicity.message.AtomicityMessagingService;
import ics.mobilememo.sharedmemory.data.kvs.VersionValue;
import ics.mobilememo.sharedmemory.data.kvs.kvstore.KVStoreInMemory;

/**
 * Created by Mio on 2015/12/21.
 */
public class SWMRAtomicDsm extends AbstractDsm<String, Key, VersionValue> {

    private static SWMRAtomicDsm instance = null;
    MessagingService.ServerTask serverTask;
    public static final String TAG = SWMRAtomicDsm.class.getName();

    public SWMRAtomicDsm()
    {
        try
        {
            AtomicityRegisterClientFactory.INSTANCE.setAtomicityRegisterClient(
                    AtomicityRegisterClientFactory.SWMR_ATOMICITY);
            MessagingService.SATO.registerReceiver(AtomicityMessagingService.INSTANCE);

            serverTask = MessagingService.SATO.new ServerTask(SessionManager.getNewInstance().getNodeIp());
            serverTask.start();

        } catch (AtomicityRegisterClientFactory.NoSuchAtomicAlgorithmSupported noSuchAtomicAlgorithmSupported)
        {
            noSuchAtomicAlgorithmSupported.printStackTrace();
        }

    }

    public synchronized static SWMRAtomicDsm INSTANCE()
    {
        if (instance == null)
            instance = new SWMRAtomicDsm();
        return instance;
    }


    @Override
    public VersionValue put(Key key, String val)
    {
        return AtomicityRegisterClientFactory.INSTANCE.getAtomicityRegisterClient().put(key, val);
    }

    @Override
    public VersionValue get(Key key)
    {
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
        return VersionValue.RESERVED_VERSIONVALUE;
    }
}
