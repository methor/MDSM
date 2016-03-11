package weakconsistency;

import consistencyinfrastructure.architecture.IRegisterClient;
import consistencyinfrastructure.communication.MessagingService;
import consistencyinfrastructure.data.kvs.Key;
import consistencyinfrastructure.group.GroupConfig;
import consistencyinfrastructure.group.member.SystemNode;
import consistencyinfrastructure.login.SessionManagerWrapper;

import java.io.Serializable;

/**
 * Created by Mio on 2016/3/7.
 */
public enum WeakConsistencyClient implements IRegisterClient<Serializable, Key, Serializable> {

    INSTANCE;

    int cnt = 0;

    public Serializable put(Key key, Serializable val)
    {
        String ip = SessionManagerWrapper.NODEIP;
        WeakConsistencyMessage weakConsistencyMessage = new WeakConsistencyMessage(ip, cnt, key, val);
        for (SystemNode systemNode : GroupConfig.INSTANCE.getGroupMembers())
        {
            MessagingService.ATO.sendOneWay(systemNode.getNodeIp(), weakConsistencyMessage);
        }

        return weakConsistencyMessage;

    }

    public Serializable get(Key key)
    {
        return KVStoreInMemory.INSTANCE.get(key);


    }


    public Serializable getReservedValue()
    {
        return ReservedValue.RESERVED_VALUE;
    }
}
