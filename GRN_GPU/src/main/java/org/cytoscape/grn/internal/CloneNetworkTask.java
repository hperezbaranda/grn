package org.cytoscape.grn.internal;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.VirtualColumnInfo;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class CloneNetworkTask extends AbstractTask {
	
	@Tunable(description="Numbers of simulation")
	
	
	public int num;
	
	private Map<CyNode, CyNode> orig2NewNodeMap;
	private Map<CyNode, CyNode> new2OrigNodeMap;
	private Map<CyEdge, CyEdge> new2OrigEdgeMap;
	private Map<CyEdge, CyEdge> orig2NewEdgeMap;

	private final CyNetworkManager netmgr;
	private final CyNetworkFactory netFactory;

	private final CyNetworkNaming naming;
	private final CyApplicationManager appMgr;
	private final CyNetworkTableManager netTableMgr;
	private final CyRootNetworkManager rootNetMgr;
	private final CyNetwork network;
	
	
	private CyNetworkView result = null;

	public CloneNetworkTask(
		
							final CyNetworkManager netmgr,
							
							
							final CyNetworkFactory netFactory,
							
							final CyNetworkNaming naming,
							final CyApplicationManager appMgr,
							final CyNetworkTableManager netTableMgr,
							final CyRootNetworkManager rootNetMgr,
							final CyNetwork network
							
							) {	
		this.netmgr = netmgr;
		this.netFactory = netFactory;
	
		this.naming = naming;
		this.appMgr = appMgr;
		this.netTableMgr = netTableMgr;
		this.rootNetMgr = rootNetMgr;
		this.network = network;
	}

	public void run(TaskMonitor tm) throws Exception{		
		tm.setProgress(0.0);
//		network.getRow(network).get(CyNetwork.NAME, String.class);
		CySubNetwork clone = null;
		CyRootNetwork rootNet = null;
		
		for (CyNetwork net : netmgr.getNetworkSet()) {
			final CyRootNetwork tmpRootNet = rootNetMgr.getRootNetwork(net);
			if(tmpRootNet.getRow(tmpRootNet).get(CyNetwork.NAME, String.class) == "Mutations") {
				rootNet = tmpRootNet;
			}
		}
		if(rootNet == null) {
			clone = (CySubNetwork) netFactory.createNetwork();
			CyRootNetwork tmpRootNet = clone.getRootNetwork();
			tmpRootNet.getRow(tmpRootNet).set(CyNetwork.NAME, "Mutations");
		}
		else {
			clone = rootNet.addSubNetwork(network.getSavePolicy());
		}
		
		final CyNetwork newNet =  cloneNetwork(network,clone);
		tm.setProgress(0.5);
		netmgr.addNetwork(newNet);	
		tm.setProgress(1.0);
	}

	public Object getResults(Class type) {
		if (result == null) return null;
		if (type.equals(String.class))
			return result.toString();
		if (type.equals(CyNetwork.class))
			return result.getModel();
		return result;
	}

	private CyNetwork cloneNetwork(final CyNetwork origNet, final CySubNetwork newNet ) {
//		final CyNetwork newNet = netFactory.createNetwork(origNet.getSavePolicy());
		
		// copy default columns
		addColumns(origNet, newNet, CyNetwork.class, CyNetwork.LOCAL_ATTRS);
		addColumns(origNet, newNet, CyNode.class, CyNetwork.LOCAL_ATTRS);
		addColumns(origNet, newNet, CyEdge.class, CyNetwork.LOCAL_ATTRS);

		cloneNodes(origNet, newNet);
		cloneEdges(origNet, newNet);

		

		newNet.getRow(newNet).set(CyNetwork.NAME, 
				naming.getSuggestedNetworkTitle(origNet.getRow(origNet).get(CyNetwork.NAME, String.class)));
		
		return newNet;
	}
	
	private void cloneNodes(final CyNetwork origNet, final CyNetwork newNet) {
		orig2NewNodeMap = new WeakHashMap<CyNode, CyNode>();
		new2OrigNodeMap = new WeakHashMap<CyNode, CyNode>();
		
		for (final CyNode origNode : origNet.getNodeList()) {
			cloneNode(origNet, newNet, origNode);
		}
	}

	private CyNode cloneNode(final CyNetwork origNet, final CyNetwork newNet, final CyNode origNode) {
		if (orig2NewNodeMap.containsKey(origNode))
			return orig2NewNodeMap.get(origNode);

		final CyNode newNode = newNet.addNode();
		orig2NewNodeMap.put(origNode, newNode);
		new2OrigNodeMap.put(newNode, origNode);
		cloneRow(newNet, CyNode.class, origNet.getRow(origNode, CyNetwork.LOCAL_ATTRS), newNet.getRow(newNode, CyNetwork.LOCAL_ATTRS));
		cloneRow(newNet, CyNode.class, origNet.getRow(origNode, CyNetwork.HIDDEN_ATTRS), newNet.getRow(newNode, CyNetwork.HIDDEN_ATTRS));
		
		
		return newNode;
	}

	private void cloneEdges(final CyNetwork origNet, final CyNetwork newNet) {
		new2OrigEdgeMap = new WeakHashMap<CyEdge, CyEdge>();
		orig2NewEdgeMap = new WeakHashMap<CyEdge, CyEdge>();
		
		for (final CyEdge origEdge : origNet.getEdgeList()) {
			cloneEdge(origNet, newNet, origEdge);
		}
	}

	private CyEdge cloneEdge(final CyNetwork origNet, final CyNetwork newNet, final CyEdge origEdge) {
		if (orig2NewEdgeMap.containsKey(origEdge))
			return orig2NewEdgeMap.get(origEdge);

		final CyNode newSource = orig2NewNodeMap.get(origEdge.getSource());
		final CyNode newTarget = orig2NewNodeMap.get(origEdge.getTarget());
		final boolean newDirected = origEdge.isDirected();
		final CyEdge newEdge = newNet.addEdge(newSource, newTarget, newDirected);
		new2OrigEdgeMap.put(newEdge, origEdge);
		orig2NewEdgeMap.put(origEdge, newEdge);
		cloneRow(newNet, CyEdge.class, origNet.getRow(origEdge, CyNetwork.LOCAL_ATTRS), newNet.getRow(newEdge, CyNetwork.LOCAL_ATTRS));
		cloneRow(newNet, CyEdge.class, origNet.getRow(origEdge, CyNetwork.HIDDEN_ATTRS), newNet.getRow(newEdge, CyNetwork.HIDDEN_ATTRS));
		return newEdge;
	}

	

	private void addColumns(final CyNetwork origNet,
							final CyNetwork newNet, 
							final Class<? extends CyIdentifiable> tableType,
							final String namespace) {
		final CyTable from = origNet.getTable(tableType, namespace); 
		final CyTable to = newNet.getTable(tableType, namespace); 
		final CyRootNetwork origRoot = rootNetMgr.getRootNetwork(origNet);
		final CyRootNetwork newRoot = rootNetMgr.getRootNetwork(newNet);
		final Map<String, CyTable> origRootTables = netTableMgr.getTables(origRoot, tableType);
		
		for (final CyColumn col : from.getColumns()){
			final String name = col.getName();
			
			if (to.getColumn(name) == null){
				final VirtualColumnInfo info = col.getVirtualColumnInfo();
				
				if (info.isVirtual()) {
					if (origRootTables.containsValue(info.getSourceTable())) {
						// If the virtual column is from a root-network table, do NOT set this virtual column directly to
						// the new table:
						// Get the original column (not the virtual one!)
						final CyColumn origCol = info.getSourceTable().getColumn(info.getSourceColumn());
						// Copy the original column to the root-network's table first
						final CyTable newRootTable = newRoot.getTable(tableType, namespace);
						
						if (newRootTable.getColumn(origCol.getName()) == null)
							copyColumn(origCol, newRootTable);
					
						// Now we can add the new "root" column as a virtual one to the new network's table
						to.addVirtualColumn(name, origCol.getName(), newRootTable, CyIdentifiable.SUID, col.isImmutable());
					} else {
						// Otherwise (e.g. virtual column from a global table) just add the virtual column directly
						addVirtualColumn(col, to);
					}
				} else {
					// Not a virtual column, so just copy it to the new network's table
					copyColumn(col, to);
				}
			}
		}
	}

	private void addVirtualColumn(CyColumn col, CyTable subTable){
		VirtualColumnInfo colInfo = col.getVirtualColumnInfo();
		CyColumn checkCol= subTable.getColumn(col.getName());
		
		if (checkCol == null)
			subTable.addVirtualColumn(col.getName(), colInfo.getSourceColumn(), colInfo.getSourceTable(), colInfo.getTargetJoinKey(), true);
		else if (!checkCol.getVirtualColumnInfo().isVirtual() ||
					!checkCol.getVirtualColumnInfo().getSourceTable().equals(colInfo.getSourceTable()) ||
					!checkCol.getVirtualColumnInfo().getSourceColumn().equals(colInfo.getSourceColumn()))
			subTable.addVirtualColumn(col.getName(), colInfo.getSourceColumn(), colInfo.getSourceTable(), colInfo.getTargetJoinKey(), true);
	}

	private void copyColumn(CyColumn col, CyTable subTable) {
		if (List.class.isAssignableFrom(col.getType()))
			subTable.createListColumn(col.getName(), col.getListElementType(), false);
		else
			subTable.createColumn(col.getName(), col.getType(), false);	
	}
	
	private void cloneRow(final CyNetwork newNet, final Class<? extends CyIdentifiable> tableType, final CyRow from,
			final CyRow to) {
		final CyRootNetwork newRoot = rootNetMgr.getRootNetwork(newNet);
		Map<String, CyTable> rootTables = netTableMgr.getTables(newRoot, tableType);
		
		for (final CyColumn col : to.getTable().getColumns()){
			final String name = col.getName();
			
			if (name.equals(CyIdentifiable.SUID))
				continue;
			
			final VirtualColumnInfo info = col.getVirtualColumnInfo();
			
			// If it's a virtual column whose source table is assigned to the new root-network,
			// then we have to set the value, because the rows of the new root table may not have been copied yet
			if (!info.isVirtual() || rootTables.containsValue(info.getSourceTable()))
				to.set(name, from.getRaw(name));
		}
	}
}