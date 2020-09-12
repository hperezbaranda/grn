package org.cytoscape.grn.internal;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyNetworkManager;


public class GRNGraphAction extends AbstractCyAction {

	private static final long serialVersionUID = 1L;
	private JFileChooser fc;
	private SimulationTask sim;
	private CyNetworkManager networkManager;
	private String error = "";

	public GRNGraphAction(CyNetworkManager networkManager){
		super("Load Boolean Graph");
		this.networkManager = networkManager;
		setPreferredMenu("Apps.TLF");
		setMenuGravity(2.0f);
	}


	@Override
	public void actionPerformed(ActionEvent e) {

		System.out.println(getClass().getProtectionDomain().getCodeSource().getLocation());
		System.out.println(System.getProperty("user.dir"));
		fc = new JFileChooser();
		int value = fc.showOpenDialog(null);
		System.out.println(value);

		if(value == JFileChooser.APPROVE_OPTION) {

//			System.out.println("open_file: "+fc.getSelectedFile());
//			String arch = this.getClass().getResource("/grn_gpu/makefile").toString();
//			System.out.println(arch);
//
//			//--------------CODE WORKING (OPEN FILE AND COPY)----------------------------
//			
//			String line;
//			try {
//				InputStreamReader inputStream = new InputStreamReader(getClass().getResourceAsStream("/grn_gpu/makefile"),"UTF-8");
//				BufferedReader reader = new BufferedReader(inputStream);
//				File tempFile = File.createTempFile("script_makefile" + UUID.randomUUID().toString(), ".sh");
//				String lines = reader.lines().collect(Collectors.joining("\n"));
//				System.out.println(tempFile.getAbsolutePath());
//				FileWriter fw = new FileWriter(tempFile);
//				BufferedWriter out = new BufferedWriter(fw);
//				out.write(lines);
//				out.flush();
//				out.close();
//				reader.close();
//
//			} catch (Exception e1) {
////				 TODO Auto-generated catch block
//				e1.printStackTrace();
//			}

			// -------------------------------------------


			String source = getClass().getProtectionDomain().getCodeSource().getLocation().toString().split(File.pathSeparator)[1];
			String dest = System.getProperty("user.dir");
			
			
							
			try {
				ProcessBuilder builder = new ProcessBuilder("jar", "xf", source, "grn_gpu");
				Process process = builder.start();
				process.waitFor();
				
				builder = new ProcessBuilder("tar", "xzf", dest+"/grn_gpu/pyenv.tar.gz", "-C", dest+"/grn_gpu/.pyenv");
				System.out.println(builder.command());
				process = builder.start();
				process.waitFor();
				
				System.out.println("python "+  dest+"/grn_gpu/load_graph.py "+fc.getSelectedFile().toString());
				
				builder = new ProcessBuilder(dest+"/grn_gpu/.pyenv/bin/python3",  dest+"/grn_gpu/load_graph.py",fc.getSelectedFile().toString(),"0");
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
