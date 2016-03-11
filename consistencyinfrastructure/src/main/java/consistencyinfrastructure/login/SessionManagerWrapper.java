package consistencyinfrastructure.login;

import java.net.InetAddress;
import java.util.List;

/**
 * Created by Mio on 2015/12/22.
 */
public class SessionManagerWrapper extends SessionManager {


    public static String NODEIP;
    public static int NODEID;
    public static String NODENAME;
    public static int NODEALGTYPE;
    public static List<Integer> OTHERID;
    public static String OTHERIP;

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

    public SessionManagerWrapper setOtherIp(String ip)
    {
        OTHERIP = ip;
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

    public boolean isIpChanged(String nodeIp)
    {
        return !nodeIp.equals(NODEIP);
    }
}
