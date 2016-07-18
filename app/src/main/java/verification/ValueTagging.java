package verification;


import java.io.Serializable;

import consistencyinfrastructure.login.SessionManagerWrapper;

/**
 * Created by mio on 4/21/16.
 */
public class ValueTagging {

    static int sID = 0;

    public static TaggedValue valueTagging(String variable, Serializable value, int id, long time)
    {
        return new TaggedValue(String.valueOf(SessionManagerWrapper.NODEID), variable, id, time, value);
    }

    public static TaggedValue valueTagging(String variable, long time, Serializable value)
    {
        sID++;
        return new TaggedValue(String.valueOf(SessionManagerWrapper.NODEID), variable, sID, time, value);
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
