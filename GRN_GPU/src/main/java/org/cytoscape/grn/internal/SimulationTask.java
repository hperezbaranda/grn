package org.cytoscape.grn.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
//import org.cytoscape.group;


public class SimulationTask extends AbstractTask {
	
	@Tunable(description="Device", groups = {"Simulation"})
 	public ListSingleSelection chooser = new ListSingleSelection("CPU","GPU");
 		
	@Tunable(description="Numbers of simulations", groups = {"Simulation"})
	public long rnSim = 10;
	
	private CyNetworkFactory networkFactory;
	private CyNetworkManager networkManager;
	private CyNetworkNaming networkNaming;
	private Characteristics showgraph;
	private long USID;
	CySubNetwork myNet = null;
	int cont;
	
	public SimulationTask(CyNetworkFactory cnf,CyNetworkManager networkManager, 
			CyNetworkNaming name, CyNetwork network){
		
		this.networkFactory = cnf;
		this.networkManager = networkManager;
		this.networkNaming = name;
		this.showgraph = new Characteristics(cnf ,name,null);
		this.USID = 0;
		this.cont = 0;
		this.myNet = (CySubNetwork) network;
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
	
	public String ProcessThresholdLine(String id, String[] line,List<String> list) {
		String s = id+",";
		String eq = "";
		
		for(String i : line) {
//			System.out.println(i);
			if(isNum(i) ) {
				eq+=i+" ";
			}else {
				try {
//					System.out.println(list.indexOf(i));
					if(list.indexOf(i) != -1) {
						eq+=list.indexOf(i)+" ";
					}
				}catch (Exception e) {
					continue;
				}
			}
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
		System.out.print(chooser.getSelectedValue());
		taskMonitor.setTitle("Processing");
		
		try {

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
            
            String equations[] = new String[t.getAllRows().size()]; 
            for (int i = 0; i < t.getAllRows().size(); i++) {
            	String id = t.getAllRows().get(i).get("name", String.class);
            	String linenew = this.ProcessThresholdLine(id,t.getAllRows().get(i).get("eq. TLF", String.class).split(" "), listNodes);
            	int index = listNodes.indexOf(linenew.split(",")[0]);
            	equations[index]=linenew.split(",")[1];
			}
            
            taskMonitor.setProgress(0.25);
            taskMonitor.setStatusMessage("Taking any change...");

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
			ModifyText(dir,"tecnologia = " ,"tecnologia = "+chooser.getSelectedValue());	
			if(mutations) {
				ModifyText(dir, "saida = ", "saida = mutations_"+clonenumber+"_S"+rnSim+".txt");
			}
			else
			{
				ModifyText(dir, "saida = ", "saida = saida_S"+rnSim+".txt");
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
				
				System.out.println(process_result1);
				if(process_result1 !=0) {
//					JOptionPane.showMessageDialog(null, "Error reading the graph\n"+error, "Error",JOptionPane.ERROR_MESSAGE);
					throw new Exception("Error reading the graph "+error);
				}else {
					System.out.println("Process result de todo: "+ process_result1);
					
					dir = System.getProperty("user.dir");
					ProcessBuilder builder2 = new ProcessBuilder(dir+"/grn_gpu/venv/bin/python3",  dir+"/grn_gpu/load_graph.py","limpeza","3");
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
					
					dir = System.getProperty("user.dir");
					String filename = "saida_S"+rnSim+".txt"; 
					if(mutations) {
//								this.CopyFile(dir+"/grn_gpu/saida.txt",dir+"/grn_gpu/mutations_"+clonenumber+".txt");
						filename = "mutations_"+clonenumber+"_S"+rnSim+".txt";
					}
					
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
						attnetwork.getRow(first).set("name",Long.toHexString(ats)+"");
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
			        attnetwork.getRow(attnetwork).set("name",networkNaming.getSuggestedNetworkTitle("Atractors"+chooser.getSelectedValue()+"_S"+rnSim+"-"+clonenumber) );
					networkManager.addNetwork(attnetwork);
				}
			}
		}
		catch (Exception e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, "Erro na leitura e interpretação do grafo\n"+ e1.getMessage(), "Erro",JOptionPane.ERROR_MESSAGE);
		}
		
	}

	
}
