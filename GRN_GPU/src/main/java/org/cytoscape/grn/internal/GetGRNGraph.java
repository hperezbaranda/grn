package org.cytoscape.grn.internal;

import java.awt.Color;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryStepRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;


public class GetGRNGraph extends AbstractCyAction {
	
	private static final long serialVersionUID = 1L;
	private JFileChooser fc;
	private MyListener resetListener;
	private Simulation sim;
	private CyNetworkManager networkManager;
	private String error = "";
	
	public GetGRNGraph(CyApplicationManager cyApplicationManager,CyNetworkFactory cnf,CyNetworkManager networkManager, CyNetworkNaming name, 
			   								CyNetworkViewManager netViewManag, CyNetworkViewFactory networkViewFactory){
		super("Load Boolean Graph");
//		this.sim = new Simulation(cyApplicationManager, cnf, networkManager, name, netViewManag, networkViewFactory);
//		ImageIcon icon = new ImageIcon(getClass().getResource("/images/tiger.jpg"));
//		ImageIcon icon = new ImageIcon(getClass().getResource("/images/tiger.jpg"));
//		System.out.println("ENTREEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
//		putValue(LARGE_ICON_KEY, icon);
		this.networkManager = networkManager;
		setPreferredMenu("Apps.TLF");
//		resetListener = new MyListener();
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
	
		System.out.println(getClass().getProtectionDomain().getCodeSource().getLocation().toString());
		System.out.println(System.getProperty("user.dir"));
//		resetListener.resetInit();
		fc = new JFileChooser();
		int value = fc.showOpenDialog(null);
		System.out.println(value);
			
		if(value == JFileChooser.APPROVE_OPTION) {
			
			System.out.println("open_file: "+fc.getSelectedFile());
			
			String source = getClass().getProtectionDomain().getCodeSource().getLocation().toString().split(File.pathSeparator)[1];
			String dest = System.getProperty("user.dir");
			
//			ProcessBuilder builder = new ProcessBuilder("sh",  "teste.sh");
			ProcessBuilder builder = new ProcessBuilder("jar", "xf", source, "grn_gpu");		
//			ProcessBuilder builder = new ProcessBuilder("jar", "xf", "/home/hector/eclipse-workspace/parent/sample-get-selected-nodes/target/grn_gpu-1.0.jar", "grn_gpu");
			
			try {
				Process process = builder.start();
				process.waitFor();
				System.out.println("python "+  dest+"/grn_gpu/load_graph.py"+fc.getSelectedFile().toString());
				builder = new ProcessBuilder("python3",  dest+"/grn_gpu/load_graph.py",fc.getSelectedFile().toString(),"0");
//				builder = new ProcessBuilder("sh",  dest+"/grn_gpu/tlf.sh");
				final Process process1 = builder.start();
				JOptionPane pane = new JOptionPane("Loading Graph..");
				final JDialog dialog = pane.createDialog(null, "Processing");
				dialog.setModal(true);
				dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				
				SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>()
				{
					
				    @Override
				    protected Void doInBackground()
				    {
				    	BufferedReader input = new BufferedReader(new InputStreamReader(process1.getInputStream()));
			            String line = null; 
			            try {
			                while ((line = input.readLine()) != null) {
			                    System.out.println(line);
			                    error = line;
			                }
			                
			            } catch (IOException e) {
			            	error = e.getMessage();
			                e.printStackTrace();
			            }
			            return null;
				    }
				 
				    @Override
				    protected void done()
				    {
				        dialog.dispose();
				    }
				};
				worker.execute();
				dialog.setVisible(true);
					
				Object selectedValue = null;
				selectedValue = pane.getValue();
				
				if (selectedValue.equals( 0) || selectedValue.equals(-1)) {
					process1.destroy();
				}
				int process_result = process1.waitFor();
				
				if(process_result !=0) {
					JOptionPane.showMessageDialog(null, "Error reading graph "+ error, "Erro",JOptionPane.ERROR_MESSAGE);
					
				}
				System.out.println("Process result1: "+ process_result);
				
							
//				System.out.println("Que fue del msg: "+selectedValue);
//				builder = new ProcessBuilder("rm", "-r", dest+"/grn_gpu/");
//				builder.start();
				
								
			} catch (Exception e2) {
				System.out.println(e2.getMessage());
				e2.printStackTrace();
			} 
			
		}
		
		if(System.getProperty("os.name").toLowerCase().startsWith("windows")) {
			System.out.println("Proximamente");
		}else if(System.getProperty("os.name").toLowerCase().startsWith("linux")) {
//			System.out.println(dest);
		}
		
		
	
//		System.out.println(System.getProperty("user.dir"));
//		Funciona
		
		/*if (cyApplicationManager.getCurrentNetwork() == null){			
			JOptionPane.showMessageDialog(null, "Not network selected ");
			return;
		}

		//Get the selected nodes
		List<CyNode> nodes = CyTableUtil.getNodesInState(cyApplicationManager.getCurrentNetwork(),"selected",true);

		CyNetwork myNet  = cyApplicationManager.getCurrentNetwork();
		myNet.getRow(myNet).set(CyNetwork.NAME, networkNaming.getSuggestedNetworkTitle("Example"));
		CyNode node1 = myNet.addNode();
		CyNode node2 = myNet.addNode();
		
		myNet.getRow(node1).set(CyNetwork.NAME, "Node11");
		myNet.getRow(node2).set(CyNetwork.NAME, "Node12");
		
		myNet.addEdge(node1, node2, true);
		
		networkManager.addNetwork(myNet);
		
		JOptionPane.showMessageDialog(null, "Number of selected nodes are "+nodes.size());*/
		
		
//		Funciona
				
		
		
        
		
		
		
		
		
		
	}
}
