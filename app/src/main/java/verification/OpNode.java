package verification;

import java.util.HashSet;

/**
 * Created by mio on 5/5/16.
 */
class OpNode {

    public enum OpCode {READ, WRITE};


    int device;
    int id;
    OpCode op;
    String variable;
    String value;
    HashSet<Integer> edges = new HashSet<>();
    int dictatedWrite = -1;

    public OpNode(OpCode op, String variable, String value, int device, int id) {
        this.op = op;
        this.variable = variable;
        this.value = value;
        this.device = device;
        this.id = id;
    }

    public OpNode(OpNode opNode)
    {
        this.op = opNode.op;
        this.value = opNode.value;
        this.id = opNode.id;
        this.device = opNode.device;
        this.variable = opNode.variable;
        this.dictatedWrite = opNode.dictatedWrite;
        for (Integer integer : opNode.edges)
        {
            this.edges.add(integer);
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OpNode)) return false;

        OpNode opNode = (OpNode) o;

        if (op != opNode.op) return false;
        if (!variable.equals(opNode.variable)) return false;
        if (!value.equals(opNode.value)) return false;
        return !(edges != null ? !edges.equals(opNode.edges) : opNode.edges != null);

    }

    @Override
    public int hashCode() {
        int result = op.hashCode();
        result = 31 * result + variable.hashCode();
        result = 31 * result + value.hashCode();
        //result = 31 * result + (edges != null ? edges.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "OpNode{" +
                "op=" + op +
                ",device=" + device +
                ", variable='" + variable + '\'' +
                ", value='" + value + '\'' +
                ", dictatedWrite=" + dictatedWrite +
                '}';
    }
}
