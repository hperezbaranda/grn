package org.cytoscape.grn.internal;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.management.AttributeNotFoundException;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
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


public class Simulation extends AbstractCyAction {
	
	private CyNetworkFactory networkFactory;
	private CyNetworkManager networkManager;
	private CyNetworkNaming networkNaming;
	private CyApplicationManager networkAppManager;
	private Caracteristics showgraph;
	private long USID;
	CySubNetwork myNet = null;
	int cont;
	
	public Simulation(CyApplicationManager cyApplicationManager,CyNetworkFactory cnf,CyNetworkManager networkManager, 
			CyNetworkNaming name){
		super("Run Simulation");				
		setPreferredMenu("Apps.TLF");
		this.networkFactory = cnf;
		this.networkManager = networkManager;
		this.networkNaming = name;
		this.networkAppManager = cyApplicationManager;
		this.showgraph = new Caracteristics(cyApplicationManager, cnf ,name);
		this.USID = 0;
		this.cont = 0;
	}
	
	public long GetUSIDnetwork() {
		return USID;
	}
	
	public void Chart(Object object,JFreeChart chart) {
		ChartFrame frame = new ChartFrame("GRN Estadistic-"+object, chart);
        frame.pack();
        frame.setVisible(true);
		
	}
	
	public void ModifyText(String dir, String oldText, String newText) {
		File f1 = new File(dir);
		List<String> lines =  new ArrayList<String>();
		String line;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f1));
			
			
			while ((line = reader.readLine()) != null) {
				if (line.contains(oldText)) {
                    line = newText;
//                    System.out.println("AQUIIII");
				}
                lines.add(line);
			}
			reader.close();
			
			FileWriter fw = new FileWriter(f1);
            BufferedWriter out = new BufferedWriter(fw);
            for(String s : lines)
                 out.write(s+System.lineSeparator());
            out.flush();
            out.close();
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public void CopyFile(String oldText, String newText) {
		File f1 = new File(oldText);
		File f2 = new File(newText);
		List<String> lines =  new ArrayList<String>();
		String line;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f1));
			FileWriter fw = new FileWriter(f2);
            BufferedWriter out = new BufferedWriter(fw);
			
			while ((line = reader.readLine()) != null) {
				 out.write(line+System.lineSeparator());
			}
			reader.close();            
            out.flush();
            out.close();
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	
	String error="";
	
		

	@Override
	public void actionPerformed(ActionEvent e) {
		/*
//		Bar
		
		double[] run = new double[]{10.5, 6, 2.8, 4, 7.3, 2, 8, 12, 9, 4 };
		DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
		for (int i = 0; i < run.length; i++) {
			  dataset1.addValue(run[i], "Basian", "A" + (i + 1));
	   }
		
		//	Funciona
		BarRenderer renderer1 = null;
		CategoryPlot plot1 = null;
		
		CategoryAxis categoryAxis = new CategoryAxis("Atratores");
		ValueAxis valueAxis = new NumberAxis("Run");
//		valueAxis.setFixedAutoRange(15.0);
		
		renderer1 = new BarRenderer();
		plot1 = new CategoryPlot(dataset1, categoryAxis, valueAxis, renderer1);
		plot1.setOrientation(PlotOrientation.VERTICAL);
		
		JFreeChart chart1 = new JFreeChart("Atratores e seus Basians", JFreeChart.DEFAULT_TITLE_FONT, plot1, true);
		
		// Mostrar Grafico
        Chart(chart1);
		
		
//       *-------------------------------------------------------------------------------------------------------------------------------------------
         //StepChart
        
        DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();
        
        double[] variacao = new double[]{0,0,1,1,0,1,0,1,0,1,0,0,1,1,1,1,1,1,1,0,1};
        for (int i = 0; i < variacao.length; i++) {
			  dataset2.addValue(variacao[i], "Nodo X", "T" +i );
	   }     
        ValueAxis yAxix = new NumberAxis("Valor");
        yAxix.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        yAxix.setRange(-0.5, 1.5);
        CategoryAxis xAxis =  new CategoryAxis("Iteração");
        CategoryStepRenderer render2 = new CategoryStepRenderer();
        CategoryPlot plot2 = new CategoryPlot(dataset2,xAxis,yAxix,render2);
        
        JFreeChart chart2 = new JFreeChart("Variação",JFreeChart.DEFAULT_TITLE_FONT,plot2,true);
        
        Chart(chart2);    
                    
//        ----------------------------------------------------------------------------------------------------------------------------------------------

        //StackerBar
        
        String legend[]=new String[] {"0","1"};
        double[][]run2 = new double[][]{{10, 6, 2, 4, 7, 2, 8, 12, 9, 4 },{1,2,3,4,5,6,7,8,9,9}};
		DefaultCategoryDataset dataset3 = new DefaultCategoryDataset();
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < run2[i].length; j++) {
				  dataset3.addValue(run2[i][j], legend[i], "N" + (j + 1));
		   }
		}
		
		//	Funciona
		StackedBarRenderer renderer3 = null;
		CategoryPlot plot3 = null;
		
		CategoryAxis categoryAxis3 = new CategoryAxis("Nodos");
		ValueAxis valueAxis3 = new NumberAxis("Cant");
		valueAxis3.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		
		renderer3 = new StackedBarRenderer();
		renderer3.setSeriesPaint(1, new Color(37, 137, 16));
		renderer3.setSeriesPaint(0, Color.RED);
		
		DecimalFormat labelFormatter = new DecimalFormat("#########");

        renderer3.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", labelFormatter));
        renderer3.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator("{2}", new DecimalFormat("########")));
		renderer3.setBaseItemLabelsVisible(true);		
		
		plot3 = new CategoryPlot(dataset3, categoryAxis3, valueAxis3, renderer3);
		plot3.setOrientation(PlotOrientation.VERTICAL);
		
		JFreeChart chart3 = new JFreeChart("Vezes que mudaram os nodos", JFreeChart.DEFAULT_TITLE_FONT, plot3, true);
		
		// Mostrar Grafico
		Chart(chart3);
        
//        ----------------------------------------------------------------------------------------------------------------------------------------------
		*/
//		
//		if(myNet == null )
//		{
//			
//		}
		
//		System.out.println("Numeros de nodos de rede 0 "+((CyNetwork)networkManager.getNetworkSet().toArray()[0]).getNodeCount());
//		System.out.println("Numeros de N redes "+ myNet.getNodeCount());
		
		
		try {
			myNet = (CySubNetwork)networkAppManager.getCurrentNetwork();
			if(myNet ==  null) {
				throw new Exception("Nao existe um grafo para simular");
			}
			CyRootNetwork rootNet =myNet.getRootNetwork();
			
			boolean mutations = false;
			String clonenumber ="";
			if(rootNet.getRow(rootNet).get(CyNetwork.NAME, String.class) == "Mutations") {
				mutations = true;
				clonenumber = myNet.getRow(myNet).get(CyNetwork.NAME, String.class).split("_")[1];
			}
//			myNet = (CyNetwork)networkManager.getNetworkSet().toArray()[0];
			String dir = System.getProperty("user.dir");
			File f1 = new File(dir+"/grn_gpu/tmpInput.txt");
			FileWriter fw = new FileWriter(f1);
            BufferedWriter out = new BufferedWriter(fw);
            String s="";
            
            CyTable t = myNet.getDefaultNodeTable();
            for (int i = 0; i < t.getAllRows().size(); i++) {
            	s = t.getAllRows().get(i).get("id", String.class) +" = "+t.getAllRows().get(i).get("equation", String.class);
            	if(t.getAllRows().get(i).get("equation", String.class) != null) {
            		out.write(s+System.lineSeparator());
            	}else {
            		out.write(t.getAllRows().get(i).get("id", String.class)+System.lineSeparator());			
            	}
            	
				System.out.println(s);
			}
//            for(String s : lines)
//                 out.write(s+System.lineSeparator());
            out.flush();
            out.close();
//            init++;
          
            ProcessBuilder builder = new ProcessBuilder("python3",  dir+"/grn_gpu/load_graph.py",dir+"/grn_gpu/tmpInput.txt","1");
            final Process process1 = builder.start();
			JOptionPane pane = new JOptionPane("Pode demorar um tempo");
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
				JOptionPane.showMessageDialog(null, "Erro na leitura e interpretação do grafo\n"+ error, "Erro",JOptionPane.ERROR_MESSAGE);
				return;
			}	
			Object[] options = { "CPU", "GPU"};
			JComboBox<String> optionList = new JComboBox(options);
			optionList.setSelectedIndex(0);
			JOptionPane.showMessageDialog(null, optionList,"Ferramenta para rodar simulação", JOptionPane.QUESTION_MESSAGE);
			dir = System.getProperty("user.dir")+"/grn_gpu/makefile";
			ModifyText(dir,"tecnologia = " ,"tecnologia =  "+optionList.getSelectedItem());				
			int numNodes = myNet.getNodeCount();
			dir = System.getProperty("user.dir")+"/grn_gpu/multicore-tlf-tabela.cu";
			ModifyText(dir,"#define TAMANHO_VETOR" ,"#define TAMANHO_VETOR "+numNodes);
			dir = System.getProperty("user.dir")+"/grn_gpu/makefile";
			
//			para verificar los cambios
			
			int  nSim = (int) (Math.pow(2, numNodes));
					
			String rnSim = JOptionPane.showInputDialog("Quantas simulações vai rodar?",nSim);
			int real=0;
			if(rnSim != null)
			{
				real =(Integer.parseInt(rnSim) > nSim) ? nSim : Integer.parseInt(rnSim);
			}
				
			if(real > 0) {
				ModifyText(dir,"simN =" ,"simN ="+(real));
				dir = System.getProperty("user.dir")+"/grn_gpu/multicore-cpu-tlf.cpp";
				ModifyText(dir,"#define simN" ,"#define simN "+(real));
				
		        ProcessBuilder builder1 = new ProcessBuilder("make","-C","grn_gpu/");
					
						final Process process = builder1.start();
						
						JOptionPane pane1 = new JOptionPane("Pode demorar um tempo");
						final JDialog dialog1 = pane1.createDialog(null, "Processing");
						dialog1.setModal(true);
						dialog1.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
						SwingWorker<Void,Void> worker1 = new SwingWorker<Void,Void>()
						{
						    @Override
						    protected Void doInBackground()
						    {
						    	BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
					            String line = null; 
					            try {
					                while ((line = input.readLine()) != null) {
					                    System.out.println(line);
					                    error = line;
					                }
					                
					            } catch (IOException e) {
					                e.printStackTrace();
					            }
					            return null;
						    }				 
						    @Override
						    protected void done()
						    {
						        dialog1.dispose();
						    }
						};
						worker1.execute();
						dialog1.setVisible(true);
							
						Object selectedValue1 = null;
						selectedValue1 = pane.getValue();

						if (selectedValue1.equals( 0) || selectedValue1.equals(-1)) {
							process.destroy();
						}
						int process_result1 = process.waitFor();
						
						System.out.println(process_result1);
						if(process_result1 !=0) {
							JOptionPane.showMessageDialog(null, "Erro na leitura e interpretação do grafo\n"+error, "Erro",JOptionPane.ERROR_MESSAGE);				
							return;
						}else {
							System.out.println("Process result: "+ process_result1);
							
							dir = System.getProperty("user.dir");
							String filename = "saida.txt"; 
							if(mutations) {
								this.CopyFile(dir+"/grn_gpu/saida.txt",dir+"/grn_gpu/mutations_"+clonenumber+".txt");
								filename = "mutations_"+clonenumber+".txt";
							}
								
							showgraph.BaciaForAtractor(filename);
							showgraph.AtractorForSize(filename);
							
							File f2 = new File(dir+"/grn_gpu/"+filename);
							List<String> lines =  new ArrayList<String>();
							String line;
							BufferedReader reader = new BufferedReader(new FileReader(f2));
							while ((line = reader.readLine()) != null) {
				                lines.add(line);
							}
							reader.close();
							
//					         -------------------------------------------------------------------------------------------------------------------------------------------------------
					      
					        CyNetwork attnetwork = networkFactory.createNetwork();
					        
					        for (int i = 1; i < lines.size(); i++) {
								String []tmpline =  lines.get(i).split(" ");
								int cant = Integer.parseInt(tmpline[0]);
								long tmpuid =0;
								CyNode first = attnetwork.addNode();
								long ats = Long.parseLong(tmpline[1]);
								attnetwork.getRow(first).set("name",Long.toHexString(ats)+"");
//								attnetwork.getRow(first).set("name",tmpline[1]);
								attnetwork.getRow(first).set("selected", true);
								if(cant > 1) {
									for (int j = 0; j < cant-1; j++) {
										CyNode node =  attnetwork.addNode();
										long atd = Long.parseLong(tmpline[j+2]);
										attnetwork.getRow(node).set("name",Long.toHexString(atd)+"");
//										attnetwork.getRow(node).set("name",tmpline[1]);
										if(j==0) {
											attnetwork.addEdge(attnetwork.getNode(first.getSUID()), attnetwork.getNode(node.getSUID()), true);
										}else {
											attnetwork.addEdge(attnetwork.getNode(tmpuid), attnetwork.getNode(node.getSUID()), true);
										}
										tmpuid = node.getSUID();
									}
									attnetwork.addEdge(attnetwork.getNode(tmpuid), attnetwork.getNode(first.getSUID()), true);
								}
								else {
									attnetwork.addEdge(attnetwork.getNode(first.getSUID()), attnetwork.getNode(first.getSUID()), true);
								}			
							}
					        attnetwork.getRow(attnetwork).set("name",networkNaming.getSuggestedNetworkTitle("Atratores"+optionList.getSelectedItem()+"-"+clonenumber) );
							networkManager.addNetwork(attnetwork);
					}
			}
			
			
			
			
		}
		catch (Exception e1) {
//			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, "Erro na leitura e interpretação do grafo\n"+ e1.getMessage(), "Erro",JOptionPane.ERROR_MESSAGE);
		}
		
		
		
			
		if(System.getProperty("os.name").toLowerCase().startsWith("windows")) {
			System.out.println("Proximamente");
		}else if(System.getProperty("os.name").toLowerCase().startsWith("linux")) {
//			System.out.println(dest);
		}	
		
	}

	
}
