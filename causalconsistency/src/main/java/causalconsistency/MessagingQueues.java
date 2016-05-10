package causalconsistency;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import consistencyinfrastructure.communication.MessagingService;
import consistencyinfrastructure.data.kvs.VectorTimestamp;
import consistencyinfrastructure.group.GroupConfig;
import consistencyinfrastructure.login.SessionManagerWrapper;

/**
 * Created by mio on 5/9/16.
 */
public enum  MessagingQueues {

    INSTANCE;
    private ConcurrentLinkedDeque<CausalConsistencyMessage> inQueue = new ConcurrentLinkedDeque<>();
    private ConcurrentLinkedDeque<CausalConsistencyMessage> outQueue = new ConcurrentLinkedDeque<>();


    public boolean addInQueueTask(CausalConsistencyMessage msg)
    {
        return inQueue.add(msg);
    }

    public ConcurrentLinkedDeque<CausalConsistencyMessage> getInQueue() {
        return inQueue;
    }

    public ConcurrentLinkedDeque<CausalConsistencyMessage> getOutQueue() {
        return outQueue;
    }

    public boolean addOutQueueTask(CausalConsistencyMessage msg)
    {
        return outQueue.add(msg);
    }

}
