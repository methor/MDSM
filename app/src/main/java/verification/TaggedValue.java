package verification;

import java.io.Serializable;

/**
 * Created by mio on 4/21/16.
 */
public class TaggedValue implements Serializable{

    private static final long serialVersionUID = -650241136137512535L;
    String device;
    String variable;
    int sID;
    Serializable value;

    public TaggedValue(String device, String variable, int sID, Serializable value) {
        this.device = device;
        this.variable = variable;
        this.sID = sID;
        this.value = value;
    }

    @Override
    public String toString() {
        return "TaggedValue{" +
                "device='" + device + '\'' +
                ", variable='" + variable + '\'' +
                ", sID=" + sID +
                ", value=" + value +
                '}';
    }
}
