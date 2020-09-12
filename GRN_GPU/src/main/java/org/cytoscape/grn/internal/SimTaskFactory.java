package org.cytoscape.grn.internal;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskIterator;

public class SimTaskFactory implements NetworkTaskFactory {

	private CyNetworkFactory cyNetworkFactory;
	private CyNetworkManager cyNetworkManager;
	private CyNetworkNaming cyNetworkNaming;
	private CyNetworkViewManager networkViewManager;
	private CyNetworkViewFactory networkViewFactory;

	
	public SimTaskFactory(CyNetworkFactory cyNetworkFactory,CyNetworkManager cyNetworkManager,CyNetworkNaming cyNetworkNaming, CyNetworkViewManager networkViewManager,CyNetworkViewFactory networkViewFactory) {
		 this.cyNetworkFactory = cyNetworkFactory;
		 this.cyNetworkManager = cyNetworkManager;
		 this.cyNetworkNaming = cyNetworkNaming;
		 this.networkViewFactory = networkViewFactory;
		 this.networkViewManager = networkViewManager;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetwork network) {
		// TODO Auto-generated method stub
		return new TaskIterator(new SimulationTask(cyNetworkFactory, cyNetworkManager, cyNetworkNaming, network,networkViewManager, networkViewFactory));
	}

	@Override
	public boolean isReady(CyNetwork network) {
		// TODO Auto-generated method stub
		return network != null;
	}

}
