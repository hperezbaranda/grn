package org.cytoscape.grn.internal;

import java.util.Enumeration;
import java.util.List;
import java.util.Properties;


import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {
		CyApplicationManager cyApplicationManager = getService(bc, CyApplicationManager.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc,CyNetworkFactory.class);
		CyNetworkManager networkManager = getService(bc, CyNetworkManager.class);
		CyNetworkNaming cyNetworkNamingServiceRef = getService(bc,CyNetworkNaming.class);
		CyNetworkViewManager cyNetworkViewManager = getService(bc, CyNetworkViewManager.class);
		CyNetworkViewFactory cyNetworkViewFactory = getService(bc, CyNetworkViewFactory.class);
		CyNetworkTableManager cyNetworkTableManager  = getService(bc,CyNetworkTableManager.class);
		CyRootNetworkManager rootNetwokMng	= getService(bc,CyRootNetworkManager.class);
//		CyNetworkFactory networkFactory = getService(bc, CyNetworkFactory.class);
		
		
		
		CloneNetworkTaskFactory mutation= new CloneNetworkTaskFactory(networkManager, cyNetworkFactoryServiceRef,cyNetworkNamingServiceRef,cyApplicationManager,cyNetworkTableManager,rootNetwokMng);
		Properties cloneprops = new Properties();
		cloneprops.setProperty("preferredMenu","Apps.TLF");
		cloneprops.setProperty("title","Clone Network");
		registerService(bc,mutation,NetworkTaskFactory.class,cloneprops);
		
		SimTaskFactory simulation= new SimTaskFactory(cyNetworkFactoryServiceRef, networkManager, cyNetworkNamingServiceRef,cyNetworkViewManager,cyNetworkViewFactory);
		Properties simprop = new Properties();
		simprop.setProperty("preferredMenu","Apps.TLF");
		simprop.setProperty("title","Run Simulation");
		registerService(bc,simulation,NetworkTaskFactory.class,simprop);
		
		CharacteristicTaskFactory characteristics= new CharacteristicTaskFactory(cyNetworkFactoryServiceRef,cyNetworkNamingServiceRef);
		Properties characteristicsprops =  new Properties();
		characteristicsprops.setProperty("preferredMenu","Apps.TLF");
		characteristicsprops.setProperty("title","Show Result");
		registerService(bc,characteristics,NetworkTaskFactory.class,characteristicsprops);
		
		GRNGraphAction getGRNGraph = new GRNGraphAction(networkManager);
		registerService(bc,getGRNGraph,CyAction.class,new Properties());
				
//		MyListener nodelistener = new MyListener();
//		Properties p = new Properties();
//		registerService(bc, nodelistener, RowsSetListener.class, p);
		
		PagerankNetworkTaskFactory pagerank = new PagerankNetworkTaskFactory();
		Properties properties = new Properties();
		properties.setProperty("preferredMenu","Apps.TLF");
		properties.setProperty("title","Google Page Rank");
		registerService(bc, pagerank, NetworkTaskFactory.class, properties);
		
		
	}
}

