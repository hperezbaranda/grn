package org.cytoscape.grn.internal;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskIterator;

public class SimTaskFactory implements NetworkTaskFactory {

	private CyNetworkFactory cyNetworkFactory;
	private CyNetworkManager cyNetworkManager;
	private CyNetworkNaming cyNetworkNaming;
	private CyRootNetworkManager cyRootNetworkManager;
	
	public SimTaskFactory(CyNetworkFactory cyNetworkFactory,CyNetworkManager cyNetworkManager,CyNetworkNaming cyNetworkNaming) {
		 this.cyNetworkFactory = cyNetworkFactory;
		 this.cyNetworkManager = cyNetworkManager;
		 this.cyNetworkNaming = cyNetworkNaming;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetwork network) {
		// TODO Auto-generated method stub
		return new TaskIterator(new SimulationTask(cyNetworkFactory, cyNetworkManager, cyNetworkNaming, network));
	}

	@Override
	public boolean isReady(CyNetwork network) {
		// TODO Auto-generated method stub
		return network != null;
	}

}
