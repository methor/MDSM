package causalconsistency;

import java.io.Serializable;

import consistencyinfrastructure.communication.IPMessage;
import consistencyinfrastructure.data.kvs.Key;
import consistencyinfrastructure.data.kvs.VectorTimestamp;

/**
 * Created by mio on 5/7/16.
 */
public class CausalConsistencyMessage extends IPMessage {

    private VectorTimestamp vt;
    private Key key;
    private Serializable payload;
    private int originator;

    public CausalConsistencyMessage(String ip, int cnt, VectorTimestamp vt,
                                    Key key, Serializable payload, int originator) {
        super(ip, cnt);
        this.vt = vt;
        this.key = key;
        this.payload = payload;
        this.originator = originator;
    }

    public VectorTimestamp getVt() {
        return vt;
    }

    public Key getKey() {
        return key;
    }

    public Serializable getPayload() {
        return payload;
    }

    public int getOriginator() {
        return originator;
    }
}
