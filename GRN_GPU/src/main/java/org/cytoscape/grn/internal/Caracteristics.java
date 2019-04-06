package org.cytoscape.grn.internal;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;
import org.jgrapht.alg.scoring.PageRank;


public class Caracteristics  extends AbstractCyAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private CyNetworkFactory networkFactory;
	
	private CyNetworkNaming networkNaming;
	private CyApplicationManager netAplicationManager;
//	private CyRootNetworkManager  rootNetworkMng;

	public Caracteristics(CyApplicationManager cyApplicationManager,CyNetworkFactory cnf, CyNetworkNaming name) {
		super("Graph Results");
		
		// TODO Auto-generated constructor stub
		setPreferredMenu("Apps.TLF");
		this.networkFactory = cnf;
		this.networkNaming = name;

		this.netAplicationManager=cyApplicationManager;
	}
	
	public void Chart(Object object,JFreeChart chart) {
		ChartFrame frame = new ChartFrame("GRN Estadistic-"+object, chart);
        frame.pack();
        frame.setVisible(true);
		
	}
	
	
	
	public void BaciaForAtractor(String filename)
	{
//CyNetwork myNet = (CyNetwork)networkManager.getNetworkSet().toArray()[0];
		
		String dir = System.getProperty("user.dir");
		File f1 = new File(dir+"/grn_gpu/"+filename);
					
		List<String> lines =  new ArrayList<String>();
		String line;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f1));
			while ((line = reader.readLine()) != null) {
	            lines.add(line);
			}
			reader.close();
		
		if(lines.size() >1) {
			long [] val = new long[lines.size()-1];
			long cant =0;
			for (int i = 1; i < lines.size(); i++) {
				String []tmpline =  lines.get(i).split(" ");
				val[i-1] = Long.parseLong(tmpline[tmpline.length-1]);
				cant +=val[i-1];
			}
			Arrays.sort(val);		
			
			DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
			for (int i = val.length-1; i >=0; i--) {
				double percent = (val[i]*100/cant);
				dataset1.addValue(percent, "Cuenca de Atratores", "A" + (val.length-i ));
			}
			
			//	Funciona
			BarRenderer renderer1 = null;
			CategoryPlot plot1 = null;
			
			CategoryAxis categoryAxis = new CategoryAxis("Atratores");
			ValueAxis valueAxis = new NumberAxis("% No. Estados");
			valueAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			
			renderer1 = new BarRenderer();
			
			DecimalFormat labelFormatter = new DecimalFormat("#");
			
	        renderer1.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}%", labelFormatter));
	//        renderer1.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator("{2}", new DecimalFormat("########")));
			renderer1.setBaseItemLabelsVisible(true);
			
			plot1 = new CategoryPlot(dataset1, categoryAxis, valueAxis, renderer1);
			plot1.setOrientation(PlotOrientation.VERTICAL);
			
			JFreeChart chart1 = new JFreeChart("Atratores e seus Basians", JFreeChart.DEFAULT_TITLE_FONT, plot1, true);
			// Mostrar Grafico
	        Chart(lines.get(0),chart1);
		}
		else {
			JOptionPane.showMessageDialog(null, "Não acharam-se ninhum atrator para o espaço de busca", "Informação", JOptionPane.INFORMATION_MESSAGE);
		}
		// TODO Auto-generated method stub
		
		} catch (Exception e1) {
			// TODO: handle exception
			JOptionPane.showMessageDialog(null, "Erro não existe uma simulação feita", "Erro",JOptionPane.ERROR_MESSAGE);
		}
	
	}

	public void AtractorForSize(String filename) {
		String dir = System.getProperty("user.dir");
		File f1 = new File(dir+"/grn_gpu/"+filename);
					
		List<String> lines =  new ArrayList<String>();
		String line;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f1));
			while ((line = reader.readLine()) != null) {
	            lines.add(line);
			}
			reader.close();
			if(lines.size() > 1) {
				HashMap<Integer, Integer> atforsize = new HashMap<Integer, Integer>();
				for (int i = 1; i < lines.size(); i++) {
					String []tmpline =  lines.get(i).split(" ");
					int key = Integer.parseInt(tmpline[0]);
					int value = 0;
					try {
						value = atforsize.get(key);
						
					}catch (Exception e) {
						value = 0;
//						continue;
					}
//					System.out.println(key);
					atforsize.put(key,value+=1);
				}
				
				DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
				
				for (Iterator iterator = atforsize.keySet().iterator(); iterator.hasNext();) {
					int  key = (Integer) iterator.next();
					int value = atforsize.get(key);
//					System.out.println(key+" ** "+value);
					dataset1.addValue(value, "Atratores", key+"");
				}
				BarRenderer renderer1 = null;
				CategoryPlot plot1 = null;
				
				CategoryAxis categoryAxis = new CategoryAxis("Size");
				ValueAxis valueAxis = new NumberAxis("Quantity");
				valueAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
				
				renderer1 = new BarRenderer();
				
				DecimalFormat labelFormatter = new DecimalFormat("#");

		        renderer1.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", labelFormatter));
//		        renderer1.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator("{2}", new DecimalFormat("########")));
				renderer1.setBaseItemLabelsVisible(true);
				
				plot1 = new CategoryPlot(dataset1, categoryAxis, valueAxis, renderer1);
				plot1.setOrientation(PlotOrientation.VERTICAL);
				
				JFreeChart chart1 = new JFreeChart("Quantidade de Atratores por tamanho", JFreeChart.DEFAULT_TITLE_FONT, plot1, true);
				chart1.removeLegend();
				
				// Mostrar Grafico
		        Chart(lines.get(0),chart1);
				
			}
			else {
				JOptionPane.showMessageDialog(null, "Não acharam-se ninhum atrator para o espaço de busca", "Informação", JOptionPane.INFORMATION_MESSAGE);
			}
		} catch (Exception e1) {
			// TODO: handle exception
			JOptionPane.showMessageDialog(null, "Erro não existe uma simulação feita "+ e1, "Erro",JOptionPane.ERROR_MESSAGE);
		}				
	}
	
	public long HammingDistance(String a, String b) {
		long xor = Long.parseUnsignedLong(a)^Long.parseUnsignedLong(b);
		int distance =0;
		while( xor > 0 )
		{
			distance += xor & 1;
			xor >>= 1;
		}			
		return distance;
	}
	
	public Long CalculateHmmingAtractor(ArrayList<String> att1, ArrayList<String> att2) {
		
		ArrayList<Long> dist = new ArrayList<Long>();
		for (int i = 0; i < att1.size(); i++) {
			for (int j = 0; j < att2.size(); j++) {
				dist.add(HammingDistance(att1.get(i),att2.get(j)));
			}
		}
		return Collections.min(dist);
	}
	
	
	public void HammingAtractors(String filename) {
		String dir = System.getProperty("user.dir");
		File f1 = new File(dir+"/grn_gpu/"+filename);
					
		List<String> lines =  new ArrayList<String>();
		String line;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f1));
			while ((line = reader.readLine()) != null) {
	            lines.add(line);
			}
			reader.close();
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		if(lines.size() > 1) {
			ArrayList<ArrayList<String>> atractors= new ArrayList<ArrayList<String>>();
//			ArrayList<Integer>bacians = new ArrayList<Integer>();
			
			for (int i = 1; i < lines.size(); i++) {
				String []tmpline =  lines.get(i).split(" ");
//				bacians.add(Integer.parseInt(tmpline[tmpline.length-1]));
				ArrayList<String> tmplst = new ArrayList<String>();
				for (int j = 1; j <= Integer.parseInt(tmpline[0]); j++) {
					String string = tmpline[j];
					tmplst.add(string);
				}
				atractors.add(tmplst);
			}
			
//			for (int i = 0; i < bacians.size()-1; i++) {
//				for (int j = i+1; j < atractors.size(); j++) {
//					if(bacians.get(i) < bacians.get(j)) {
//						int tmpmax = bacians.remove(j);
//						int tmpmin = bacians.remove(i);
//						bacians.set(i,tmpmax);
//						bacians.set(j, tmpmin);
//					}
//				}
//			}
			
			ArrayList<String>a1 = atractors.get(0);
			ArrayList<String>a2 = atractors.get(1);
			
			System.out.println("Distancia de Hamming");
			for (int i = 0; i < atractors.size()-1; i++) {
				System.out.println("Atract #"+(i+1));
				for (int j = i+1; j < atractors.size(); j++) {
					System.out.println(CalculateHmmingAtractor(atractors.get(i), atractors.get(j)));
				}
			}
//			System.out.print("Distancia de Hamming "+CalculateHmmingAtractor(a1,a2));
//			DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
//					
////			for (Iterator iterator = atforsize.keySet().iterator(); iterator.hasNext();) {
////				int  key = (Integer) iterator.next();
////				int value = atforsize.get(key);
//////				.println(key+" ** "+value);
////				dataset1.addValue(value, "Atratores", key+"");
////			}
//			BarRenderer renderer1 = null;
//			CategoryPlot plot1 = null;
//			
//			CategoryAxis categoryAxis = new CategoryAxis("Size");
//			ValueAxis valueAxis = new NumberAxis("Quantity");
//			valueAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
//			
//			renderer1 = new BarRenderer();
//			
//			DecimalFormat labelFormatter = new DecimalFormat("#");
//
//	        renderer1.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", labelFormatter));
////	        renderer1.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator("{2}", new DecimalFormat("########")));
//			renderer1.setBaseItemLabelsVisible(true);
//			
//			plot1 = new CategoryPlot(dataset1, categoryAxis, valueAxis, renderer1);
//			plot1.setOrientation(PlotOrientation.VERTICAL);
//			
//			JFreeChart chart1 = new JFreeChart("Quantidade de Atratores por tamanho", JFreeChart.DEFAULT_TITLE_FONT, plot1, true);
//			chart1.removeLegend();
//			
//			// Mostrar Grafico
//	        Chart(lines.get(0),chart1);
		}
		else {
			JOptionPane.showMessageDialog(null, "There's not an Atractor Graph", "Information", JOptionPane.INFORMATION_MESSAGE);
		}
			
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		try {
			CySubNetwork attNet = (CySubNetwork)netAplicationManager.getCurrentNetwork();
			if(attNet ==  null) {
				throw new Exception("There is not an atractor graph!");
			}
			CyRootNetwork rootNet =attNet.getRootNetwork();
					
			String name ="saida.txt";
//			System.out.println(attNet.getRow(attNet).get(CyNetwork.NAME, String.class).split("-").length > 1);
			if(rootNet.getRow(rootNet).get(CyNetwork.NAME, String.class).indexOf("Atractors") != -1 && attNet.getRow(attNet).get(CyNetwork.NAME, String.class).split("-").length > 1) {
				name = "mutations_"+attNet.getRow(attNet).get(CyNetwork.NAME, String.class).split("-")[1]+".txt";
			}
			System.out.println(name);
			BaciaForAtractor(name);
			AtractorForSize(name);
			HammingAtractors(name);
		}catch (Exception err) {
			JOptionPane.showMessageDialog(null, "Error reading and simulated graph\n"+err.getMessage(), "Error",JOptionPane.ERROR_MESSAGE);
		}
		
	    
	    
//	    HammingDistance("1181", "181");
	}

}
