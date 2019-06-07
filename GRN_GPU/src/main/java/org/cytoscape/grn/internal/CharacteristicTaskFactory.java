package org.cytoscape.grn.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class CharacteristicTaskFactory implements NetworkTaskFactory{
	
	private CyNetworkFactory cyNetworkFactory;
	private CyNetworkNaming cyNetworkNaming;
	
	public CharacteristicTaskFactory(CyNetworkFactory cyNetworkFactoryServiceRef, CyNetworkNaming cyNetworkNamingServiceRef) {
		this.cyNetworkFactory = cyNetworkFactoryServiceRef;
		this.cyNetworkNaming = cyNetworkNamingServiceRef;		
	}

	@Override
	public TaskIterator createTaskIterator(CyNetwork network) {
		// TODO Auto-generated method stub
		return new TaskIterator(new Characteristics(cyNetworkFactory, cyNetworkNaming,network));
	}

	@Override
	public boolean isReady(CyNetwork network) {
		return network != null;
	}
	
	
}
