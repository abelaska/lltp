package me.belaska.lltp.core.cluster;

public interface ClusterPeer {

	boolean isMaster();
	
	boolean isReplica();
}
