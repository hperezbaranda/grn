package org.cytoscape.grn.internal;

import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;

public class MyListener implements RowsSetListener
{
	private int init;
	
	public MyListener() {
		super();
		init = 2;
	}

	public void resetInit() {
		// TODO Auto-generated method stub
		init = 0;
		
	}
	
	@Override
	public void handleEvent(RowsSetEvent e) {
		
		
//		if(e.containsColumn("equation") && e.getSource().getTitle().equals("TLF root shared  node")) 
//				System.out.println("INIT: "+init);
		
//		if(e.containsColumn("equation") && e.getSource().getTitle().equals("TLF root shared  node") && init >0 ) {
//			
//			CyTable t =e.getSource();
//			
//			try {
////				Process process = builder.start();
////				process.waitFor();
//				String dir = System.getProperty("user.dir");
//				File f1 = new File(dir+"/grn_gpu/tmpInput.txt");
//				FileWriter fw = new FileWriter(f1);
//	            BufferedWriter out = new BufferedWriter(fw);
//	            String s="";
////	            Thread.sleep(1000);
//	            for (int i = 0; i < t.getAllRows().size(); i++) {
//	            	s = t.getAllRows().get(i).get("id", String.class) +" = "+t.getAllRows().get(i).get("equation", String.class);
//	            	out.write(s+System.lineSeparator());
//					System.out.println(s);
//				}
//	//            for(String s : lines)
//	//                 out.write(s+System.lineSeparator());
//	            out.flush();
//	            out.close();
////	            init++;
//	            System.out.println(init);
//	            ProcessBuilder builder = new ProcessBuilder("python3",  dir+"/grn_gpu/load_graph.py",dir+"/grn_gpu/tmpInput.txt","1");
//	            final Process process1 = builder.start();
//				JOptionPane pane = new JOptionPane("Pode demorar um tempo");
//				final JDialog dialog = pane.createDialog(null, "Processing");
//				dialog.setModal(true);
//				dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
//				SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>()
//				{
//				    @Override
//				    protected Void doInBackground()
//				    {
//				    	BufferedReader input = new BufferedReader(new InputStreamReader(process1.getInputStream()));
//			            String line = null; 
//			            try {
//			                while ((line = input.readLine()) != null) {
//			                    System.out.println(line);
//			                }
//			                
//			            } catch (IOException e) {
//			                e.printStackTrace();
//			            }
//			            return null;
//				    }
//				 
//				    @Override
//				    protected void done()
//				    {
//				        dialog.dispose();
//				    }
//				};
//				worker.execute();
//				dialog.setVisible(true);
//					
//				Object selectedValue = null;
//				selectedValue = pane.getValue();
//				
//				if (selectedValue.equals( 0) || selectedValue.equals(-1)) {
//					process1.destroy();
//				}
//				int process_result = process1.waitFor();
//				
//				if(process_result !=0) {
//					JOptionPane.showMessageDialog(null, "Erro na leitura e interpretação do grafo", "Erro",JOptionPane.ERROR_MESSAGE);
//					
//				}
//			}
//			catch (Exception e1) {
//				e1.printStackTrace();
//			}
//		}else
//		{
//			
////			System.out.println(e.containsColumn("equation") && e.getSource().getTitle().equals("TLF root shared  node") );
//			if(e.containsColumn("equation") && e.getSource().getTitle().equals("TLF root shared  node") && init ==0)
//			{
//				init++;
//			}
//		}
		
		
		
	}

	
	
	 

}
