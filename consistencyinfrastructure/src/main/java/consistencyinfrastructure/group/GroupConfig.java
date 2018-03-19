/**
 * @author hengxin
 * @date May 9, 2014
 * @description {@link GroupConfig} is responsible for establishing and maintaining
 *  the membership information about {@link SystemNode}s (not about clients).
 */
package consistencyinfrastructure.group;

import consistencyinfrastructure.group.member.SystemNode;
import consistencyinfrastructure.login.SessionManager;
import consistencyinfrastructure.login.SessionManagerWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum GroupConfig
{
	INSTANCE;
	
	/**
	 * maintain a list of {@link SystemNode}s
	 */
	private List<SystemNode> replica_list = new ArrayList<>();
	
	/**
	 * @return the size of the group of {@link SystemNode}s
	 */
	public int getGroupSize()
	{
		return this.replica_list.size();
	}
	
	/**
	 * @return a list of {@link SystemNode}s in the group
	 */
	public List<SystemNode> getGroupMembers()
	{
		return this.replica_list;
	}
	
	/**
	 * add a new {@link SystemNode} into this group
	 * @param replica {@link SystemNode} to be added
	 */
	public void addReplica(SystemNode replica)
	{
		this.replica_list.add(replica);
		Collections.sort(replica_list);
		new SessionManagerWrapper().addOtherIp(replica.getNodeIp())
				.addOtherID(replica.getNodeId())
				.addOtherName(replica.getNodeName());
	}

	/**
	 * added by hms; distinguish others' replica from itself
	 */
	public void addSelf(SystemNode self)
	{
		this.replica_list.add(self);
		Collections.sort(replica_list);
		new SessionManagerWrapper().setNodeIp(self.getNodeIp())
				.setNodeID(self.getNodeId())
				.setNodeName(self.getNodeName());
	}

	/**
	 * add by hms; get self index
	 */
	public int getSelfIndex()
	{
		for (int i = 0; i < replica_list.size(); i++)
			if (replica_list.get(i).getNodeId() == SessionManagerWrapper.NODEID)
				return i;

		return -1;	// never reached
	}


	public void clearReplicas()
	{
		replica_list.clear();
		SessionManagerWrapper.OTHERID = new ArrayList<>();
		SessionManagerWrapper.OTHERIP = new ArrayList<>();
		SessionManagerWrapper.OTHERNAME = new ArrayList<>();
	}
	
	/**
	 * @return list of ip addresses of server replicas
	 */
	public List<String> getReplicaIPList()
	{
		List<String> replica_ip_list = new ArrayList<>();
		
		for (SystemNode replica : this.replica_list)
			replica_ip_list.add(replica.getNodeIp());
		
		return replica_ip_list;
	}
}
