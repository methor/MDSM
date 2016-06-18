package log;

import java.io.Serializable;

/**
 * Created by mio on 6/17/16.
 */
public interface NetworkLog {

    public void logNetworkLatency(Serializable receiveMsg);
}
