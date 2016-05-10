package consistencyinfrastructure.login;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mio on 2015/12/22.
 */
public class SessionManagerWrapper extends SessionManager {


    public static String NODEIP;
    public static int NODEID;
    public static String NODENAME;
    public static int NODEALGTYPE;
    public static List<Integer> OTHERID = new ArrayList<>();
    public static List<String> OTHERIP = new ArrayList<>();
    public static List<String> OTHERNAME = new ArrayList<>();

    public SessionManagerWrapper()
    {

        super(1);
    }

    @Override
    public String getNodeIp()
    {
        return NODEIP;
    }

    @Override
    public int getNodeId()
    {
        return NODEID;
    }

    public SessionManagerWrapper setNodeIp(String ip)
    {
        NODEIP = ip;
        return this;
    }

    public SessionManagerWrapper setNodeID(int id)
    {
        NODEID = id;
        return this;
    }

    public SessionManagerWrapper setNodeName(String name)
    {
        NODENAME = name;
        return this;
    }

    public SessionManagerWrapper setNodeAlgType(int type)
    {
        NODEALGTYPE = type;
        return this;
    }

    public SessionManagerWrapper setOtherID(List<Integer> list)
    {
        OTHERID = list;
        return this;
    }

    public SessionManagerWrapper setOtherIp(List<String> ip)
    {
        OTHERIP = ip;
        return this;
    }

    public SessionManagerWrapper addOtherIp(String ip)
    {
        OTHERIP.add(ip);
        return this;
    }

    public SessionManagerWrapper addOtherID(int id)
    {
        OTHERID.add(id);
        return this;
    }

    public SessionManagerWrapper addOtherName(String name)
    {
        OTHERNAME.add(name);
        return this;
    }

    public boolean isSessionAlive(String nodeIp, String otherIp, int nodeId, int otherId)
    {
        if (OTHERID == null || OTHERID.size() == 0 || nodeIp == null || otherIp == null)
            return false;
        if (!nodeIp.equals(NODEIP) || !otherIp.equals(OTHERIP) || nodeId != NODEID
                || otherId != OTHERID.get(0))
            return false;

        return true;
    }

    public static int getLeader()
    {
        int min = NODEID;
        for (int nodeID : OTHERID)
        {
            if (min > nodeID)
                min = nodeID;
        }

        return min;

    }

    public static boolean isLeader()
    {
        if (NODEID == getLeader())
            return true;
        return false;
    }

    public boolean isIpChanged(String nodeIp)
    {
        return !nodeIp.equals(NODEIP);
    }
}
