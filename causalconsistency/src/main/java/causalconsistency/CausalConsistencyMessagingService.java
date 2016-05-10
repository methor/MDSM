package causalconsistency;

import consistencyinfrastructure.communication.IPMessage;
import consistencyinfrastructure.communication.IReceiver;

/**
 * Created by mio on 5/7/16.
 */
public enum CausalConsistencyMessagingService implements IReceiver {

    INSTANCE;

    @Override
    public void onReceive(IPMessage msg) {

        MessagingQueues.INSTANCE.addInQueueTask((CausalConsistencyMessage)msg);
    }
}
