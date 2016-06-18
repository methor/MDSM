package weakconsistency;

import java.io.Serializable;
import consistencyinfrastructure.communication.IPMessage;
import consistencyinfrastructure.data.kvs.Key;

/**
 * Created by Mio on 2016/3/7.
 */
public class WeakConsistencyMessage extends IPMessage {


    Key key = Key.RESERVED_KEY;
    Serializable val = ReservedValue.RESERVED_VALUE;


    public Key getKey() {
        return key;
    }

    public Serializable getVal() {
        return val;
    }

    public WeakConsistencyMessage(String ip, int cnt, Key key, Serializable val)
    {
        super(ip, cnt);
        this.key = key;
        this.val = val;
    }
}
