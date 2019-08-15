/**
 * 
 */
package org.cytoscape.grn.internal;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/**
 * @author Jimmy Morzaria
 * 
 */
public class PagerankTask extends AbstractTask {

	private CyNetwork network;

	private double probability = 0.85;

	private double epsilon = 0.001;

	private double[] pageranks;

	private Map<CyNode, Integer> nodeIndicies;
	
	private Map<Integer, CyNode> nodeIndiciesMap;

	private Map<CyNode, Integer> indegreeMap;

	private Map<CyNode, Integer> outdegreeMap;

	private boolean directed = true;

	public PagerankTask(CyNetwork network) {
		this.network = network;
		calculateDegreeMap(network);
	}

	public void calculateDegreeMap(CyNetwork network) {

		indegreeMap = new IdentityHashMap<CyNode, Integer>();
		outdegreeMap = new IdentityHashMap<CyNode, Integer>();
		for(CyNode node : network.getNodeList()){
			indegreeMap.put(node,0);
			outdegreeMap.put(node, 0);
		}
		for (CyEdge edge : network.getEdgeList()) {

			if (!edge.isDirected()){
				directed = false;
				continue;
			}
			outdegreeMap.put(edge.getSource(),
						(int) outdegreeMap.get(edge.getSource()) + 1);
			indegreeMap.put(edge.getTarget(),
						(int) indegreeMap.get(edge.getTarget()) + 1);
			
		}
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// TODO Auto-generated method stub
		CyTable t = network.getDefaultNodeTable();
		List<Integer> countEdge= t.getColumn("indegree").getValues(Integer.class);
		int sum =0;
		for (int v : countEdge) {
			sum+=v;
		}
		System.out.println(sum/2);
	/*	nodeIndicies = new IdentityHashMap<CyNode, Integer>();
		nodeIndiciesMap = new IdentityHashMap<Integer, CyNode>();
		int nodeIndex = 0;
		for (CyNode node : network.getNodeList()) {
			nodeIndicies.put(node, nodeIndex);
			nodeIndiciesMap.put(nodeIndex, node);
//			System.out.println(network.getRow(node).get(CyNetwork.NAME, String.class)+"---"+nodeIndex);
			nodeIndex++;
		}
		
		taskMonitor.setTitle("Computing pagerank!");
		pageranks = calculatePagerank();
		
		//System.out.println(nodeIndicies);
		
		CyTable nodeTable = network.getDefaultNodeTable();
		try {
		nodeTable.createColumn("PageRank", Double.class, true);
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		
		for (CyNode node : network.getNodeList()) {
			
//			System.out.println(network.getRow(node).get(CyNetwork.NAME, String.class)+"===="+pageranks[nodeIndicies.get(node)]);
			CyRow row = nodeTable.getRow(node.getSUID());
			row.set("PageRank", pageranks[nodeIndicies.get(node)]);
		}
		
//		row.set("PageRank", new Double(2));
		
//		CyRow row = nodeTable.getRow("0");
//		System.out.println(row.get("equation", String.class));
		
//		int i = 0;
//		while(i < pageranks.length){
//			
//			System.out.println(i+"---"+pageranks[i++]);
//		}
*/
	}

	public double[] calculatePagerank() {

		int N = network.getNodeCount();

		double[] pagerankValues = new double[N];
		double[] temp = new double[N];

		// Set initial values
		int index = 0;
		for (CyNode node : network.getNodeList()) {
			pagerankValues[index++] = 1.0f / N;
		}

		while (true) {
			
			double r = calculateR(pagerankValues);
			boolean done = true;

			for (CyNode node : network.getNodeList()) {
				int s_index = nodeIndicies.get(node);
				temp[s_index] = updateValueForNode(node, pagerankValues, r);

				if ((temp[s_index] - pagerankValues[s_index])
						/ pagerankValues[s_index] >= epsilon) {
					done = false;
				}

			}
			
			pagerankValues = temp;
			temp = new double[N];
			if (done) {
				break;
			}
		}
		return pagerankValues;
	}

	private double calculateR(double[] pagerankValues) {
		int N = network.getNodeCount();
		double r = 0;
		for (CyNode node : network.getNodeList()) {
			int s_index = nodeIndicies.get(node);
			boolean out = outdegreeMap.get(node) > 0;
			
			if (out) {
				r += (1.0 - probability) * (pagerankValues[s_index] / N);
			} else {
				r += (pagerankValues[s_index] / N);
			}
		}
		return r;
	}

	private double updateValueForNode(CyNode node, double[] pagerankValues,
			double r) {
		double res = r;

		for (CyEdge edge : network.getAdjacentEdgeList(node,
				CyEdge.Type.INCOMING)) {
			CyNode sourcenode = edge.getSource();
			int neigh_index = nodeIndicies.get(sourcenode);
			int normalize = outdegreeMap.get(sourcenode);

			res += probability * (pagerankValues[neigh_index] / normalize);

		}
		return res;
	}
}
