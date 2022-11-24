package me.belaska.lltp.core.cluster;

import org.jgroups.JChannel;
import org.jgroups.Receiver;
import org.jgroups.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractClusterPeer implements Receiver, ClusterPeer {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractClusterPeer.class);

	protected JChannel channel;

	private String configFile = "target/classes/jgroups-replica.xml";

	private String clusterName = "lltp-example";

	private boolean master = false;

	@Override
	public boolean isMaster() {
		return master;
	}

	@Override
	public boolean isReplica() {
		return master;
	}

	public void joinCluster() throws Exception {
		channel = new JChannel(configFile);
		channel.setReceiver(this);
		channel.setDiscardOwnMessages(true);
		channel.connect(clusterName);
	}

	@Override
	public void viewAccepted(View view) {
		boolean newMaster = channel.getAddress().equals(channel.getView().getMembers().get(0));
		boolean becameMaster = !master && newMaster;
		boolean stateChanged = master != newMaster;

		master = newMaster;

		LOG.info("{} node '{}' {}, cluster nodes '{}'", new Object[] { master ? "MASTER" : "SLAVE", channel.getName(),
				stateChanged ? "activated" : "active", channel.getView().getMembers() });

		if (becameMaster) {
			onBecameMaster(channel);
		}
	}

	public abstract void onBecameMaster(JChannel channel);
}
