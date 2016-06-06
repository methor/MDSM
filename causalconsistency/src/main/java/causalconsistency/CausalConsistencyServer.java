package causalconsistency;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import consistencyinfrastructure.architecture.IRegisterServer;
import consistencyinfrastructure.communication.MessagingService;
import consistencyinfrastructure.data.kvs.VectorTimestamp;
import consistencyinfrastructure.login.SessionManagerWrapper;

/**
 * Created by mio on 5/7/16.
 */
public enum CausalConsistencyServer implements IRegisterServer {

    INSTANCE;
    private ExecutorService executorService;

    public void init() {
        executorService = Executors.newFixedThreadPool(2);
        executorService.execute(sendRunnable);
        executorService.execute(applyRunnable);
    }

    private Runnable sendRunnable = new Runnable() {
        @Override
        public void run() {
//            ConcurrentLinkedDeque<CausalConsistencyMessage> outQueue = MessagingQueues.INSTANCE.getOutQueue();
//
//            while (!Thread.interrupted()) {
//                try {
//                    CausalConsistencyMessage msg = outQueue.pop();
//                    for (String ip : SessionManagerWrapper.OTHERIP)
//                        MessagingService.CAUSAL.sendOneWay(ip, msg);
//                } catch (NoSuchElementException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    Thread.sleep(5);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                    break;
//                }
//            }
//            MessagingQueues.INSTANCE.clearOutQueue();
//            System.out.println("SendThread exits successfully");

        }
    };
    private Runnable applyRunnable = new Runnable() {
        @Override
        public void run() {

            ConcurrentLinkedDeque<CausalConsistencyMessage> inQueue = MessagingQueues.INSTANCE.getInQueue();

            while (!Thread.interrupted()) {
                try {
                    if (!inQueue.isEmpty()) {
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
//                        CausalConsistencyMessage msg = inQueue.pop();
//                        KVStoreInMemory.INSTANCE.put(msg.getKey(), msg.getPayload());
                    }
                } catch (NoSuchElementException e) {
                    e.printStackTrace();
                }
            }

            MessagingQueues.INSTANCE.clearInQueue();
            System.out.println("ApplyThread exits successfully");

        }
    };


    public void handleCausalConsistencyMessage(CausalConsistencyMessage msg) {

    }


    public void onDestroy() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(50, TimeUnit.MILLISECONDS))
                executorService.shutdownNow();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
