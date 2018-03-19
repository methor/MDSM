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
    private ConcurrentLinkedDeque<CausalConsistencyMessage> inQueue = null;
    private ConcurrentLinkedDeque<CausalConsistencyMessage> outQueue = null;


    public boolean addInQueueTask(CausalConsistencyMessage msg)
    {
        if (inQueue != null)
            return inQueue.add(msg);
        return false;
    }

    public synchronized ConcurrentLinkedDeque<CausalConsistencyMessage> getInQueue() {
        if (inQueue == null)
            inQueue = new ConcurrentLinkedDeque<>();
        return inQueue;
    }

    public synchronized ConcurrentLinkedDeque<CausalConsistencyMessage> getOutQueue() {
        if (outQueue == null)
            outQueue = new ConcurrentLinkedDeque<>();
        return outQueue;
    }

    public boolean addOutQueueTask(CausalConsistencyMessage msg)
    {
        if (outQueue != null)
            return outQueue.add(msg);
        return false;
    }

    public void clearInQueue()
    {
        inQueue = null;
    }
    public void clearOutQueue()
    {
        outQueue = null;
    }

}
