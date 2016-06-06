package causalconsistency;

import consistencyinfrastructure.communication.IPMessage;
import consistencyinfrastructure.communication.IReceiver;
import consistencyinfrastructure.communication.MessagingService;
import consistencyinfrastructure.login.SessionManagerWrapper;

/**
 * Created by mio on 5/7/16.
 */
public enum CausalConsistencyMessagingService implements IReceiver {

    INSTANCE;

    @Override
    public void onReceive(IPMessage msg) {

        CausalConsistencyMessage cmsg = (CausalConsistencyMessage)msg;
        System.out.println("Receive " + (CausalConsistencyMessage) msg);
         MessagingQueues.INSTANCE.addInQueueTask(cmsg);
//        KVStoreInMemory.INSTANCE.put(cmsg.getKey(), cmsg.getPayload());
    }
}
