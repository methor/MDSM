package causalconsistency;

import java.io.Serializable;

import consistencyinfrastructure.architecture.IRegisterClient;
import consistencyinfrastructure.communication.MessagingService;
import consistencyinfrastructure.data.kvs.Key;
import consistencyinfrastructure.data.kvs.VectorTimestamp;
import consistencyinfrastructure.group.GroupConfig;
import consistencyinfrastructure.login.SessionManagerWrapper;

/**
 * Created by Mio on 2016/3/7.
 */
public enum CausalConsistencyClient implements IRegisterClient<Serializable, Key, Serializable> {

    INSTANCE;

    int cnt = 0;

    public Serializable put(Key key, Serializable val) {
        VectorTimestamp vectorTimestamp = null;

        vectorTimestamp = KVStoreInMemory.INSTANCE.getVectorTimestamp().increament(GroupConfig.INSTANCE.getSelfIndex());

        KVStoreInMemory.INSTANCE.put(key, val);
        String ip = SessionManagerWrapper.NODEIP;
        CausalConsistencyMessage msg = new CausalConsistencyMessage(ip, cnt,
                new VectorTimestamp(vectorTimestamp),
                key, val, GroupConfig.INSTANCE.getSelfIndex());

        System.out.println("send " + msg);
        MessagingQueues.INSTANCE.addOutQueueTask(msg);

//        for (String ipo : SessionManagerWrapper.OTHERIP)
//            MessagingService.CAUSAL.sendOneWay(ipo, msg);

        return msg;

    }

    public Serializable get(Key key) {
        cnt++;
        return KVStoreInMemory.INSTANCE.get(key);


    }


    public Serializable getReservedValue() {
        return ReservedValue.RESERVED_VALUE;
    }
}
