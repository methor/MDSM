package consistencyinfrastructure.data.kvs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

import consistencyinfrastructure.group.GroupConfig;
import consistencyinfrastructure.login.SessionManagerWrapper;

/**
 * Created by mio on 5/7/16.
 */
public class VectorTimestamp implements Serializable {

    private static final long serialVersionUID = -4014056350794547921L;

    public enum Relation {LESS, GREAT, EQUAL, CONCURRENT};
    private ArrayList<Integer> vectorTimestamp = new ArrayList<>();


    public VectorTimestamp()
    {
        int size = GroupConfig.INSTANCE.getGroupSize();
        for (int i = 0; i < size; i++)
            vectorTimestamp.add(0);
    }

    public VectorTimestamp(VectorTimestamp vt)
    {
        for (Integer timestamp : vt.vectorTimestamp)
            vectorTimestamp.add(new Integer(timestamp));
    }


    public synchronized ArrayList<Integer> getVectorTimestamp() {
        return vectorTimestamp;
    }


    public VectorTimestamp selfIncreament()
    {
        int index = GroupConfig.INSTANCE.getSelfIndex();
        vectorTimestamp.set(index, vectorTimestamp.get(index) + 1);
        return this;
    }

    public void increament(int index)
    {
        vectorTimestamp.set(index, vectorTimestamp.get(index) + 1);
    }

    public void setVectorTimestamp(ArrayList<Integer> vectorTimestamp) {
        this.vectorTimestamp = vectorTimestamp;
    }

    public Relation compareTo(VectorTimestamp vt)
    {
        return Relation.LESS;
    }

    @Override
    public String toString() {
        return "VectorTimestamp{" +
                "vectorTimestamp=" + vectorTimestamp +
                '}';
    }
}
