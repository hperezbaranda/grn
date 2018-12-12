package org.cytoscape.grn.internal;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
	
	public int HammingDistance(String a, String b) {
	
		char[] ar =a.toCharArray(); 
		for (int i = 0; i < ar.length; i++) {
			System.out.println(ar[i]);
		}
		
		
		return 0;
	}
	
//	public int AtractorHammingDistance(int a ) {
//		return 0;
//		
//	}
	
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
			ArrayList<ArrayList<Integer>> atractors= new ArrayList<ArrayList<Integer>>();
			ArrayList<Integer>bacians = new ArrayList<Integer>();
			
			for (int i = 1; i < lines.size(); i++) {
				String []tmpline =  lines.get(i).split(" ");
				bacians.add(Integer.parseInt(tmpline[tmpline.length-1]));
				ArrayList<Integer> tmplst = new ArrayList<Integer>();
				for (int j = 1; j <= Integer.parseInt(tmpline[0]); j++) {
					String string = tmpline[j];
					tmplst.add(Integer.parseInt(string));
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
			
			ArrayList<Integer>a1 = atractors.get(0);
			ArrayList<Integer>a2 = atractors.get(1);
			
			for (Iterator iterator = a1.iterator(); iterator.hasNext();) {
				Integer integer = (Integer) iterator.next();
//				System.out.println(integer);
			}
			
			
			DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
					
//			for (Iterator iterator = atforsize.keySet().iterator(); iterator.hasNext();) {
//				int  key = (Integer) iterator.next();
//				int value = atforsize.get(key);
////				System.out.println(key+" ** "+value);
//				dataset1.addValue(value, "Atratores", key+"");
//			}
			BarRenderer renderer1 = null;
			CategoryPlot plot1 = null;
			
			CategoryAxis categoryAxis = new CategoryAxis("Size");
			ValueAxis valueAxis = new NumberAxis("Quantity");
			valueAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			
			renderer1 = new BarRenderer();
			
			DecimalFormat labelFormatter = new DecimalFormat("#");

	        renderer1.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", labelFormatter));
//	        renderer1.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator("{2}", new DecimalFormat("########")));
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
			
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		try {
			CySubNetwork attNet = (CySubNetwork)netAplicationManager.getCurrentNetwork();
			if(attNet ==  null) {
				throw new Exception("Nao existe um grafo de atratores");
			}
			CyRootNetwork rootNet =attNet.getRootNetwork();
			
		
			String name ="saida.txt";
			System.out.println(rootNet.getRow(rootNet).get(CyNetwork.NAME, String.class).split("-").length > 1);
			if(rootNet.getRow(rootNet).get(CyNetwork.NAME, String.class).indexOf("Atratores") != -1 && rootNet.getRow(rootNet).get(CyNetwork.NAME, String.class).split("-").length > 1) {
				name = "mutations_"+rootNet.getRow(rootNet).get(CyNetwork.NAME, String.class).split("-")[1]+".txt";
			}
			BaciaForAtractor(name);	
		}catch (Exception err) {
			JOptionPane.showMessageDialog(null, "Erro na leitura da simulação do grafo\n"+err.getMessage(), "Erro",JOptionPane.ERROR_MESSAGE);
		}
		
//	    AtractorForSize("saida.txt");
//	    HammingAtractors("saida.txt");
	    HammingDistance("hec", "hac");
	}

}
