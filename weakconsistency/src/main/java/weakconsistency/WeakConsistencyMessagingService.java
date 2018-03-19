package weakconsistency;

import consistencyinfrastructure.communication.IPMessage;
import consistencyinfrastructure.communication.IReceiver;

/**
 * Created by Mio on 2016/3/9.
 */
public enum WeakConsistencyMessagingService implements IReceiver {

    INSTANCE;
    @Override
    public void onReceive(IPMessage msg)
    {
        WeakConsistencyMessage weakConsistencyMessage = ((WeakConsistencyMessage) msg);

        WeakConsistencyServer.INSTANCE.handleWeakConsistencyMessage(weakConsistencyMessage);
    }

}
