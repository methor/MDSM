package verification;

import android.os.Build;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mio on 4/21/16.
 */
public class ValueTagging {

    static Map<String, Integer> varMapSID = new HashMap<>();
    static String device = Build.SERIAL;

    public static TaggedValue valueTagging(String variable, Serializable value)
    {
        Integer sID = varMapSID.get(variable);
        sID = (sID == null) ? 1 : sID + 1;
        return new TaggedValue(device, variable, sID, value);
    }

    public static Serializable tagStripping(TaggedValue taggedValue)
    {
        return taggedValue.value;
    }
}
