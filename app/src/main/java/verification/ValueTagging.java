package verification;

import android.os.Build;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import consistencyinfrastructure.login.SessionManagerWrapper;

/**
 * Created by mio on 4/21/16.
 */
public class ValueTagging {

    static int sID = 0;

    public static TaggedValue valueTagging(String variable, Serializable value, int id)
    {
        return new TaggedValue(String.valueOf(SessionManagerWrapper.NODEID), variable, id, value);
    }

    public static TaggedValue valueTagging(String variable, Serializable value)
    {
        sID++;
        return new TaggedValue(String.valueOf(SessionManagerWrapper.NODEID), variable, sID, value);
    }

    public static Serializable tagStripping(TaggedValue taggedValue)
    {
        return taggedValue.value;
    }

    public static void reset()
    {
        sID = 0;
    }
}
