package verification;

import java.io.Serializable;

import model.Ball;

/**
 * Created by mio on 4/21/16.
 */
public class TaggedValue implements Serializable{

    private static final long serialVersionUID = -650241136137512535L;

    String device;
    String variable;
    int sID;
    long time;
    Serializable value;

    public TaggedValue(String device, String variable, int sID, long time, Serializable value) {
        this.device = device;
        this.variable = variable;
        this.sID = sID;
        this.time = time;
        this.value = value;
    }

    @Override
    public String toString() {
        return "TaggedValue{" +
                "device='" + device + '\'' +
                ", variable='" + variable + '\'' +
                ", sID=" + sID +
                ", time=" + time +
                ", value=" + value +
                '}';
    }

    public String getTag()
    {
        return device + " " + variable + " " + sID;
    }

    public static TaggedValue getTaggedValueFromTag(String tag)
    {
        String[] items = tag.split(" ");
        if (items.length != 3)
            return null;
        return new TaggedValue(items[0], items[1], Integer.valueOf(items[2]), 0, null);
    }

    public long getTime()
    {
        return time;
    }

    public void setValue(Serializable val)
    {
        this.value = val;
    }

    public String getVariable()
    {
        return variable;
    }
}
