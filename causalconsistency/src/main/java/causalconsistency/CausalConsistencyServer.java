package causalconsistency;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import consistencyinfrastructure.architecture.IRegisterServer;
import consistencyinfrastructure.communication.MessagingService;
import consistencyinfrastructure.data.kvs.VectorTimestamp;
import consistencyinfrastructure.login.SessionManagerWrapper;

/**
 * Created by mio on 5/7/16.
 */
public enum  CausalConsistencyServer implements IRegisterServer {

    INSTANCE;
    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    public void init()
    {
        executorService.execute(sendRunnable);
        executorService.execute(applyRunnable);
    }

    private Runnable sendRunnable = new Runnable() {
        @Override
        public void run() {
            ConcurrentLinkedDeque<CausalConsistencyMessage> outQueue = MessagingQueues.INSTANCE.getOutQueue();

            while (true)
            {
                if (!outQueue.isEmpty())
                {
                    CausalConsistencyMessage msg = outQueue.pop();
                    for (String ip : SessionManagerWrapper.OTHERIP)
                        MessagingService.CAUSAL.sendOneWay(ip, msg);

                }
            }

        }
    };
    private Runnable applyRunnable = new Runnable() {
        @Override
        public void run() {

            ConcurrentLinkedDeque<CausalConsistencyMessage> inQueue = MessagingQueues.INSTANCE.getInQueue();

            while (true)
            {
                if (!inQueue.isEmpty())
                {
                    CausalConsistencyMessage msg = inQueue.getFirst();
                    synchronized (KVStoreInMemory.INSTANCE.getVectorTimestamp())
                    {
                        VectorTimestamp selfVectorTimestamp = KVStoreInMemory.INSTANCE.getVectorTimestamp();
                        VectorTimestamp incomingVectorTimestamp = msg.getVt();

                        int index = msg.getOriginator();
                        if (incomingVectorTimestamp.getVectorTimestamp().get(index) !=
                                selfVectorTimestamp.getVectorTimestamp().get(index) + 1)
                            continue;

                        int i;
                        for (i = 0; i < selfVectorTimestamp.getVectorTimestamp().size(); i++)
                        {
                            if (i != index)
                            {
                                if (incomingVectorTimestamp.getVectorTimestamp().get(i) >
                                        selfVectorTimestamp.getVectorTimestamp().get(i))
                                    break;
                            }
                        }
                        if (i == selfVectorTimestamp.getVectorTimestamp().size())
                        {
                            inQueue.pop();
                            KVStoreInMemory.INSTANCE.put(msg.getKey(), msg.getPayload());
                            KVStoreInMemory.INSTANCE.getVectorTimestamp().increament(index);
                        }

                    }
                }
            }

        }
    };



    public void handleCausalConsistencyMessage(CausalConsistencyMessage msg)
    {

    }


}
