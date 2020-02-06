package org.cytoscape.grn.internal;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.VisualPropertyValue;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
//import org.cytoscape.group;


public class SimulationTask extends AbstractTask {
	
	public String sArch[] = {"CPU","GPU"};
	
	@Tunable(description="Device", groups = {"Architecture"})
 	public ListSingleSelection<String> chooser = new ListSingleSelection<String>(" CPU     "," GPU     ");
	
		
	@Tunable(description="Type Simulation", groups={"Simulation"}, xorChildren=true)
 	public ListSingleSelection<String> simType = new ListSingleSelection<String>("Static","Random");
	
	@Tunable(description="start", groups={"Simulation","Static"}, xorKey="Static")
 	public long start = 0;
 	
 	@Tunable(description="end", groups={"Simulation","Static"}, xorKey="Static")
 	public long end = 9;
 
 	@Tunable(description="Numbers of simulations", groups={"Simulation","Random"}, xorKey="Random")
 	public long rnSim = 10;
 
	   
	private CyNetworkFactory networkFactory;
	private CyNetworkManager networkManager;
	private CyNetworkNaming networkNaming;
	private CharacteristicsTask showgraph;
	private CyNetworkViewManager networkViewManager;
	private CyNetworkViewFactory networkViewFactory;
	private long USID;
	CySubNetwork myNet = null;
	int cont;
	
	
	public SimulationTask(CyNetworkFactory cnf,CyNetworkManager networkManager, 
			CyNetworkNaming name, CyNetwork network, CyNetworkViewManager networkViewManager,CyNetworkViewFactory networkViewFactory){
		
		this.networkFactory = cnf;
		this.networkManager = networkManager;
		this.networkNaming = name;
		this.showgraph = new CharacteristicsTask(cnf ,name,null);
		this.USID = 0;
		this.cont = 0;
		this.myNet = (CySubNetwork) network;
		this.networkViewFactory = networkViewFactory;
		this.networkViewManager = networkViewManager;
		
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
	
	public String ProcessThresholdLine(String id, String tline,List<String> list, int value) throws Exception {
		String s = id+",";
		String eq = "";
		System.out.println("ID node source "+id);
//		System.out.print(tline == null);
		if(tline != null) {
			String[] line = tline.split(" "); 
			for(String i : line) {
				System.out.println("Linea "+i);
				if(isNum(i) ) {
					eq+=i+" ";
				}else {
					try {
						System.out.println(list.indexOf(i));
						if(list.indexOf(i) != -1) {
							eq+=list.indexOf(i)+" ";
						}
					}catch (Exception e) {
						throw new Exception("Error processing graph "+e.getMessage());
//						continue;
					}
				}
			}
		}else {
			System.out.println("aqui no eq");
			if(value == 0)
				eq+='0'+" "+'1'; 
			else
				eq+='1'+" "+'0';
		}
		
		return s+eq;
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
	
	public static boolean isNum(String strNum) {
	    boolean ret = true;
	    try {

	        Double.parseDouble(strNum);

	    }catch (NumberFormatException e) {
	        ret = false;
	    }
	    return ret;
	}

	
	boolean done =false;
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		String arch = "CPU";
		if(chooser.getSelectedValue().equals(" GPU     "))
		{
			arch = "GPU";
		}
		
		if(simType.getSelectedValue().equals("Static")) {
			rnSim = (end-start)+1;
		}
		
		taskMonitor.setTitle("Processing");
		
//		try {

			CyRootNetwork rootNet =myNet.getRootNetwork();
			
			boolean mutations = false;
			String clonenumber ="";
			if(rootNet.getRow(rootNet).get(CyNetwork.NAME, String.class) == "Mutations") {
				mutations = true;
				clonenumber = myNet.getRow(myNet).get(CyNetwork.NAME, String.class).split("_")[1];
			}

			String dir = System.getProperty("user.dir");      
            CyTable t = myNet.getDefaultNodeTable();
            List<String>listNodes = t.getColumn("id").getValues(String.class);
            Collections.sort(listNodes);
            
//            System.out.println("VACIO "+t.getAllRows().get(14).get("eq. TLF", String.class));
            
            String equations[] = new String[t.getAllRows().size()]; 
            for (int i = 0; i < t.getAllRows().size(); i++) {
            	int node_value = 0;
            	String id = t.getAllRows().get(i).get("name", String.class);
            	boolean fixed = t.getAllRows().get(i).get("fixed", Boolean.class);
            	String eqline = t.getAllRows().get(i).get("eq. TLF", String.class);
            	if(fixed) {
            		eqline = null;
            		node_value = Integer.parseInt(t.getAllRows().get(i).get("value", String.class));
            	}
            	String linenew="";
            	
            	linenew = this.ProcessThresholdLine(id,eqline, listNodes,node_value);
            	
            	
            	int index = listNodes.indexOf(linenew.split(",")[0]);
            	equations[index]=linenew.split(",")[1];
			}
            
            taskMonitor.setProgress(0.25);
            taskMonitor.setStatusMessage("Taking any modification...");

            String dir1 = System.getProperty("user.dir");
			File f11 = new File(dir1+"/grn_gpu/pesosTabela.txt");
			FileWriter fw1= new FileWriter(f11);
            BufferedWriter out1 = new BufferedWriter(fw1);
            
            out1.write( t.getAllRows().size()+System.lineSeparator());
            
            for (String string : equations) {
            	
            	out1.write(((string.split(" ").length-1)/2)+" ");
				System.out.println(string+"----"+ (string.split(" ").length-1)/2);
			}
            out1.write(System.lineSeparator());
            for (String string : equations) {
            	 out1.write(string+System.lineSeparator());
			}
           
            out1.flush();
            out1.close();

			dir = System.getProperty("user.dir")+"/grn_gpu/makefile";
			ModifyText(dir,"tecnologia = " ,"tecnologia = "+arch+"s");	
			if(mutations) {
				ModifyText(dir, "saida = ", "saida = mutations_"+arch+"_S"+rnSim+"-"+clonenumber+".txt");
			}
			else
			{
				ModifyText(dir, "saida = ", "saida = saida_"+arch+"_S"+rnSim+".txt");
			}
			int numNodes = myNet.getNodeCount();
			dir = System.getProperty("user.dir")+"/grn_gpu/multicore-tlf-tabela.cu";
			ModifyText(dir,"#define TAMANHO_VETOR" ,"#define TAMANHO_VETOR "+numNodes);
			dir = System.getProperty("user.dir")+"/grn_gpu/makefile";
			
//			para verificar los cambios
			
			long  nSim = (long) (Math.pow(2, numNodes));
						
//			String rnSim = JOptionPane.showInputDialog("How many simulation?",nSim);
			long real=0;
//			if(rnSim != null)
//			{
			real = rnSim > nSim ? nSim :rnSim;
//			}
				
			if(real > 0) {
				ModifyText(dir,"simN =" ,"simN ="+(real));
//				dir = System.getProperty("user.dir")+"/grn_gpu/multicore-cpu-tlf.cpp";
//				ModifyText(dir,"#define simN" ,"#define simN "+(real));
				
		        ProcessBuilder builder1 = new ProcessBuilder("make","-C","grn_gpu/");
					
				final Process process = builder1.start();
				
				taskMonitor.setProgress(0.50);
	            taskMonitor.setStatusMessage("Making a Simulation...");
				
//	            final boolean done = false ;
//				JOptionPane pane1 = new JOptionPane("Making a simulation...");
//				final JDialog dialog1 = pane1.createDialog(null, "Processing");
//				dialog1.setModal(true);
//				dialog1.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				SwingWorker<Void,Void> worker1 = new SwingWorker<Void,Void>()
				{
				    @Override
				    protected Void doInBackground()
				    {
				    	BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
			            String line = null; 
			            try {
			                while ((line = input.readLine()) != null) {
//			                    System.out.println(line);
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
//				        dialog1.dispose();
				    	done = true;
				    }
				};
				worker1.execute();

				int process_result1 = process.waitFor();
				
				System.out.println("Result de simulacio: "+process_result1);
				if(process_result1 !=0) {
//					JOptionPane.showMessageDialog(null, "Error reading the graph\n"+error, "Error",JOptionPane.ERROR_MESSAGE);
					throw new Exception("Error reading the graph "+error);
				}else {					
					
					dir = System.getProperty("user.dir");
					String filename = "saida_"+arch+"_S"+rnSim+".txt"; 
					if(mutations) {
//								this.CopyFile(dir+"/grn_gpu/saida.txt",dir+"/grn_gpu/mutations_"+clonenumber+".txt");
						filename = "mutations_"+arch+"_S"+rnSim+"-"+clonenumber+".txt";
					}
					
					dir = System.getProperty("user.dir");
					ProcessBuilder builder2 = new ProcessBuilder(dir+"/grn_gpu/venv/bin/python3",  dir+"/grn_gpu/load_graph.py",dir+"/grn_gpu/"+filename,"3");
		            final Process process2 = builder2.start();
//					JOptionPane pane2 = new JOptionPane("Processing to thresholding graph...");
//					final JDialog dialog2 = pane2.createDialog(null, "Processing");
//					dialog2.setModal(true);
//					dialog2.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
					taskMonitor.setProgress(0.75);
					taskMonitor.setStatusMessage("Cleaning the output...");
					
					SwingWorker<Void,Void> worker2 = new SwingWorker<Void,Void>()
					{
					    @Override
					    protected Void doInBackground()
					    {
					    	BufferedReader input = new BufferedReader(new InputStreamReader(process2.getInputStream()));
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
					        done= true;
					    }
					};
					worker2.execute();
//					dialog2.setVisible(true);
					int process_result2 = process2.waitFor();
					
					taskMonitor.setProgress(1);
					taskMonitor.setStatusMessage("Processing graphics...");
					
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
					
					lines.remove(0);
					Collections.sort(lines);
					
					System.out.println("AQUIIII");
					for (int i = 0; i < lines.size(); i++) {
						System.out.println(lines.get(i));
					}
					
//-------------------------------------------------------------------------------------------------------------------------------------------------------
			      
					CySubNetwork attnetwork = null;
					CyRootNetwork rootNet1 = null;
					
					for (CyNetwork net : networkManager.getNetworkSet()) {
						CyRootNetwork tmpRootNet = ((CySubNetwork)net).getRootNetwork();
//						final CyRootNetwork tmpRootNet = rootNetworkMang.getRootNetwork(net);
						if(tmpRootNet.getRow(tmpRootNet).get(CyNetwork.NAME, String.class) == "Atractors") {
							rootNet1 = tmpRootNet;
						}
					}
					if(rootNet1 == null) {
						attnetwork = (CySubNetwork) networkFactory.createNetwork();
						CyRootNetwork tmpRootNet = attnetwork.getRootNetwork();
						tmpRootNet.getRow(tmpRootNet).set(CyNetwork.NAME, "Atractors");
					}
					else {
						attnetwork = rootNet1.addSubNetwork();
					}
								        
			        for (int i = 0; i < lines.size(); i++) {
						String []tmpline =  lines.get(i).split(" ");
						int cant = Integer.parseInt(tmpline[0]);
						long tmpuid =0;
						CyNode first = attnetwork.addNode();
						long ats = Long.parseUnsignedLong(tmpline[1]);
						attnetwork.getRow(first).set("name","(A"+(i+1)+")\n"+Long.toHexString(ats));
						attnetwork.getRow(first).set("selected", true);
						if(cant > 1) {
							for (int j = 0; j < cant-1; j++) {
								CyNode node =  attnetwork.addNode();
								long atd = Long.parseUnsignedLong(tmpline[j+2]);
								attnetwork.getRow(node).set("name",Long.toHexString(atd)+"");
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
			        if(clonenumber == "")
			        	attnetwork.getRow(attnetwork).set("name",networkNaming.getSuggestedNetworkTitle("Atractors_"+arch+"_S"+rnSim) );
			        else
			        	attnetwork.getRow(attnetwork).set("name",networkNaming.getSuggestedNetworkTitle("Atractors_"+arch+"_S"+rnSim+"-"+clonenumber) );
					networkManager.addNetwork(attnetwork);
					System.out.println("ID NET: "+attnetwork.getSUID());
					
					// ----------------------Testing create de view--------------------------------					
					
					final Collection<CyNetworkView> views = networkViewManager.getNetworkViews(myNet);
					CyNetworkView myView = null;
					
					if(views.size() != 0)
						myView = views.iterator().next();
//					System.out.println(myView.toString());
					if (myView == null) {
						// create a new view for my network
						myView = networkViewFactory.createNetworkView(attnetwork);
						myView.setVisualProperty(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT, Color.BLUE );
						networkViewManager.addNetworkView(myView);
					} else {
						System.out.println("networkView already existed.");
						
					}

					// Set the variable destroyView to true, the following snippet of code
					// will destroy a view
					boolean destroyView = false;
					if (destroyView) {
						networkViewManager.destroyNetworkView(myView);
					}
				}
			}
		
//		catch (Exception e1) {
//			e1.printStackTrace();
//			JOptionPane.showMessageDialog(null, "Erro na leitura e interpretação do grafo\n"+ e1.getMessage(), "Erro",JOptionPane.ERROR_MESSAGE);
//		}
		
	}

	
}
