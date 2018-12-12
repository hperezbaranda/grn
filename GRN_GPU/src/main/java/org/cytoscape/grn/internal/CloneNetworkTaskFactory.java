package org.cytoscape.grn.internal;

import org.cytoscape.application.CyApplicationManager;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;

import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.task.NetworkTaskFactory;

public class CloneNetworkTaskFactory implements NetworkTaskFactory{
	private final CyNetworkManager netmgr;
	private final CyNetworkFactory netFactory;

	private final CyNetworkNaming naming;
	private final CyApplicationManager appMgr;
	private final CyNetworkTableManager netTableMgr;
	private final CyRootNetworkManager rootNetMgr;

    public CloneNetworkTaskFactory(
    		final CyNetworkManager netmgr,
    		final CyNetworkFactory netFactory,			
			final CyNetworkNaming naming,
			final CyApplicationManager appMgr,
			final CyNetworkTableManager netTableMgr,
			final CyRootNetworkManager rootNetMgr) {
    	
    	this.netmgr = netmgr;
    	this.netFactory = netFactory;
		this.naming = naming;
		this.appMgr = appMgr;
		this.netTableMgr = netTableMgr;
		this.rootNetMgr = rootNetMgr;
    }

	@Override
	public TaskIterator createTaskIterator(CyNetwork network) {
		return new TaskIterator(new CloneNetworkTask(netmgr,netFactory,naming,appMgr,netTableMgr,rootNetMgr,network));
	}

	@Override
	public boolean isReady(CyNetwork network) {
		// TODO Auto-generated method stub
		return network != null;
	}
}
