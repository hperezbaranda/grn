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
	private SimulationTask sim;
	private CyNetworkManager networkManager;
	private String error = "";
	
	public GetGRNGraph(CyNetworkManager networkManager){
		super("Load Boolean Graph");
		this.networkManager = networkManager;
		setPreferredMenu("Apps.TLF");
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
	
		System.out.println(getClass().getProtectionDomain().getCodeSource().getLocation().toString());
		System.out.println(System.getProperty("user.dir"));
		fc = new JFileChooser();
		int value = fc.showOpenDialog(null);
		System.out.println(value);
			
		if(value == JFileChooser.APPROVE_OPTION) {
			
			System.out.println("open_file: "+fc.getSelectedFile());
			
			String source = getClass().getProtectionDomain().getCodeSource().getLocation().toString().split(File.pathSeparator)[1];
			String dest = System.getProperty("user.dir");
			ProcessBuilder builder = new ProcessBuilder("jar", "xf", source, "grn_gpu");				
			try {
				Process process = builder.start();
				process.waitFor();
				System.out.println("python "+  dest+"/grn_gpu/load_graph.py "+fc.getSelectedFile().toString());
				builder = new ProcessBuilder(dest+"/grn_gpu/venv/bin/python3",  dest+"/grn_gpu/load_graph.py",fc.getSelectedFile().toString(),"0");
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
				System.out.println("Process result: "+ process_result);
				
								
			} catch (Exception e2) {
				System.out.println(e2.getMessage());
				e2.printStackTrace();
			} 
			
		}
		
		if(System.getProperty("os.name").toLowerCase().startsWith("windows")) {
			System.out.println("Proximamente");
		}else if(System.getProperty("os.name").toLowerCase().startsWith("linux")) {
		}
		
	}
}
