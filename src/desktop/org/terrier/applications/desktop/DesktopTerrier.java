/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org 
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.ac.uk/
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is DesktopTerrier.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *  Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> 
 */
package org.terrier.applications.desktop;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.applications.desktop.filehandling.FileOpener;
import org.terrier.applications.desktop.filehandling.MultiOSFileOpener;
import org.terrier.indexing.Collection;
import org.terrier.indexing.SimpleFileCollection;
import org.terrier.matching.ResultSet;
import org.terrier.querying.Manager;
import org.terrier.querying.SearchRequest;
import org.terrier.querying.parser.Query;
import org.terrier.querying.parser.QueryParser;
import org.terrier.structures.Index;
import org.terrier.structures.MetaIndex;
import org.terrier.structures.indexing.Indexer;
import org.terrier.structures.indexing.classical.BasicIndexer;
import org.terrier.structures.indexing.classical.BlockIndexer;
import org.terrier.structures.indexing.singlepass.BasicSinglePassIndexer;
import org.terrier.structures.indexing.singlepass.BlockSinglePassIndexer;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;
import org.terrier.utility.Rounding;
/**
 * An application that uses the Terrier IR platform, in order to search the
 * desktop of a user.
 * <p><b>Properties:</b>
 * <ul><li><tt>desktop.directories.spec</tt> - where is the collection.spec for the desktop. 
	Defaults to <tt>var/desktop.spec</tt></li>
 * <li><tt>desktop.directories.filelist</tt> - where the list of files associated to an index should be 
	saved. Defaults to <tt>data.filelist</tt></li>
 * <li><tt>desktop.matching</tt> - which matching class to use for desktop. Defaults to Matching.</li>
 * <li><tt>desktop.model</tt> - which weighting model to use for the desktop. Defaults to PL2.</li>
 * <li><tt>desktop.manager</tt> - which Manager class to use for the desktop. Defaults to Manager.</li>
 * <li><tt>desktop.indexing.singlepass</tt> - set to true to use the SinglePass indexer.</li>
 * <li><tt>desktopsearch.filetype.colors</tt> - mapping of file type to colour. Default value <tt>Text:(221 221 221),TeX:(221 221 221),Bib:(221 221 221),PDF:(236 67 69),HTML:(177 228 250),Word:(100 100 255),Powerpoint:(250 110 49),Excel:(38 183 78),XHTML:(177 228 250),XML:(177 228 250)</tt></li>
 * <li><tt>desktopsearch.filetype.types</tt> - comma delimited mapping of file extensions to File types.
	Default value is <tt>txt:Text,text:Text,tex:TeX,bib:Bib,pdf:PDF,html:HTML,htm:HTML,xhtml:XHTML,xml:XML,doc:Word,ppt:Powerpoint,xls:Excel</tt></li>
 * </ul>
 * @author Craig Macdonald, Vassilis Plachouras
  */
public class DesktopTerrier extends JFrame {
	private static final long serialVersionUID = 1L;

	/**
	 * logging variables
	 */
	protected static final Logger logger = LoggerFactory.getLogger(DesktopTerrier.class);
		
	// Logging variables
	PipedInputStream piErr;
	PipedOutputStream poErr;
	
	// The JTextArea at the bottom of the screen
	JTextArea jTextArea = null;
	
	//the colors used for the file types.
	private FiletypeColors filetypeColors = null;
	
	//check whether query is running
	private boolean queryRunning = false;
	
	private DesktopTerrier me = this;
	//the file opener to use
	private transient FileOpener fOpener = null;
	//the folders to index
	private IndexFolders indexFolders = null;
	//the about dialog
	private AboutTerrier aboutTerrier = null;
	
	//the help dialog
	private HelpDialog helpDialog = null;
	private String managerName = ApplicationSetup.getProperty("desktop.manager", "Manager");
	private static String mModel = ApplicationSetup.getProperty("desktop.matching","Matching");
	private static String wModel = ApplicationSetup.getProperty("desktop.model", "PL2");
	private List<String> folderList;
	private Manager queryingManager;
	private Index diskIndex;
	

	private class WinHandler extends WindowAdapter {
		public void windowClosing(WindowEvent we) {
			
			//save the used file associations
			if (reada != null)
			{
				reada.run = false;
				try{reada.interrupt();}catch (Exception e){}
			}
			fOpener.save();
			dispose(); // Frees program frame resources.
			System.exit(0); // Stops the program normally.
		} //windowClosing
	} //class WinHandler
	private javax.swing.JPanel jContentPane = null;
	private JMenuBar jJMenuBar = null;
	private JMenu jMenuFile = null;
	private JMenu jMenu = null;
	private JMenuItem jMenuItem = null;
	private JMenuItem jMenuItem1 = null;
	private JMenuItem jMenuItem2 = null;
	private JTabbedPane jTabbedPane = null;
	private JPanel searchPanel = null;
	private JPanel jPanel1 = null;
	private JTextField jTextField = null;
	private JButton jButton = null;
	private JPanel jPanel2 = null;
	private JTable jTable = null;
	private JScrollPane jScrollPane = null;
	private JButton jButton1 = null;
	private JButton jButton2 = null;
	
	/**
	 * This method initializes jJMenuBar
	 * 
	 * @return javax.swing.JMenuBar
	 */
	private JMenuBar getJJMenuBar() {
		if (jJMenuBar == null) {
			jJMenuBar = new JMenuBar();
			jJMenuBar.add(getJMenuFile());
			jJMenuBar.add(getJMenu());
		}
		return jJMenuBar;
	}
	/**
	 * This method initializes jMenu
	 * 
	 * @return javax.swing.JMenu
	 */
	private JMenu getJMenuFile() {
		if (jMenuFile == null) {
			jMenuFile = new JMenu();
			jMenuFile.setText("File");
			jMenuFile.setMnemonic('F');
			jMenuFile.add(getJMenuItem2());
		}
		return jMenuFile;
	}
	/**
	 * This method initializes jMenu
	 * 
	 * @return javax.swing.JMenu
	 */
	private JMenu getJMenu() {
		if (jMenu == null) {
			jMenu = new JMenu();
			jMenu.setText("Help");
			jMenu.setMnemonic('H');
			jMenu.add(getJMenuItem1());
			jMenu.add(getJMenuItem());
		}
		return jMenu;
	}
	/**
	 * This method initializes jMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem() {
		if (jMenuItem == null) {
			jMenuItem = new JMenuItem();
			jMenuItem.setText("About");
			jMenuItem.setMnemonic('A');
			jMenuItem.setAccelerator(KeyStroke.getKeyStroke('A', Toolkit
					.getDefaultToolkit().getMenuShortcutKeyMask()));
			jMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					aboutTerrier.setVisible(true);
				}
			});
		}
		return jMenuItem;
	}
	/**
	 * This method initializes jMenuItem1
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem1() {
		if (jMenuItem1 == null) {
			jMenuItem1 = new JMenuItem();
			jMenuItem1.setText("Desktop Search Help");
			jMenuItem1.setMnemonic('D');
			jMenuItem1.setAccelerator(KeyStroke.getKeyStroke("F1"));
			jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					helpDialog.setVisible(true);
				}
			});
		}
		return jMenuItem1;
	}
	/**
	 * This method initializes jMenuItem2
	 * 
	 * @return javax.swing.JMenuItem
	 */
	
	private JMenuItem getJMenuItem2() {
		if (jMenuItem2 == null) {
			jMenuItem2 = new JMenuItem();
			jMenuItem2.setText("Quit");
			jMenuItem2.setMnemonic('Q');
			jMenuItem2.setAccelerator(KeyStroke.getKeyStroke('Q', Toolkit
					.getDefaultToolkit().getMenuShortcutKeyMask()));
			jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
				@edu.umd.cs.findbugs.annotations.SuppressWarnings(
						value="DM_EXIT",
						justification="Seems to be no other way to end things sensibly")
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (folderList != null) {
						String dirList = ApplicationSetup.makeAbsolute(
								ApplicationSetup.getProperty(
										"desktop.directories.spec",
										"desktop.spec"),
								ApplicationSetup.TERRIER_VAR);
						save_list(new File(dirList), folderList);
					}
					//save the used file associations
					fOpener.save();
					dispose(); // Frees program frame resources.
					System.exit(0); // Stops the program normally.
				}
			});
		}
		return jMenuItem2;
	}
	/**
	 * This method initializes jTabbedPane
	 * 
	 * @return javax.swing.JTabbedPane
	 */
	private JTabbedPane getJTabbedPane() {
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane();
			jTabbedPane.addTab("Search", null, getSearchPanel(), null);
			jTabbedPane.addTab("Index", null, getJPanel1(), null);
		}
		return jTabbedPane;
	}
	/**
	 * This method initializes searchPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getSearchPanel() {
		if (searchPanel == null) {
			searchPanel = new JPanel();
			searchPanel.setLayout(new BorderLayout());
			searchPanel.add(getJPanel2(), java.awt.BorderLayout.NORTH);
			searchPanel.add(getJScrollPane(), java.awt.BorderLayout.CENTER);
		}
		return searchPanel;
	}
	/**
	 * This method initializes jPanel1
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.setLayout(new BorderLayout());
			jPanel1.add(getJPanel3(), java.awt.BorderLayout.NORTH);
			jPanel1.add(getJPanel(), java.awt.BorderLayout.CENTER);
		}
		return jPanel1;
	}
	/**
	 * This method initializes jTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextField() {
		if (jTextField == null) {
			jTextField = new JTextField();
			jTextField.setColumns(15);
			jTextField.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					runQuery_thread();
				}
			});
		}
		return jTextField;
	}
	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("Search");
			jButton.setMnemonic('S');
			String iconPath = ApplicationSetup.makeAbsolute(
					"images/terrier-desktop-search.gif",
					ApplicationSetup.TERRIER_SHARE);
			try {
				jButton.setIcon(new ImageIcon(iconPath, "Terrier icon"));
			} catch (NullPointerException npe) {
				logger.error("A NullPointerException exception occured while trying to load: "+iconPath,npe);
			}
			jButton
					.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					runQuery_thread();
				}
			});
		}
		return jButton;
	}
	/**
	 * This method initializes jPanel2
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			jPanel2 = new JPanel();
			jPanel2.add(getJTextField(), null);
			jPanel2.add(getJButton(), null);
		}
		return jPanel2;
	}
	/**
	 * This method initializes jTable
	 * 
	 * @return javax.swing.JTable
	 */
	private JTable getJTable() {
		if (jTable == null) {
			final JFrame topWindow = this;
			jTable = new JTable();
			jTable.setDoubleBuffered(true);
			jTable.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent _me) {
					JTable table = (JTable) _me.getSource();
					Point p = _me.getPoint();
					int row = table.rowAtPoint(p);
					//int col = table.columnAtPoint(p);
					String filename = null;
					if ((_me.getClickCount() == 2)) {
						try {
							// Open the file with associated viewer
							filename = "" + table.getValueAt(row, 3);
							fOpener.open(filename);
						} catch (final Exception e) {
							final String actualFilename = filename;
													
							SwingUtilities.invokeLater(new Runnable()
						      {
						        public void run()
						        {
						        	JOptionPane.showMessageDialog(topWindow, "Couldn't open " + actualFilename + ":\n" + e.getLocalizedMessage(),
							                  "Unable to open file", JOptionPane.ERROR_MESSAGE);
						        }
						      });

							logger.error(e.getMessage(),e);
						}
					}
				}
			});
			jTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		}
		return jTable;
	}
	/**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJTable());
			jScrollPane
					.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			jScrollPane
					.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			//jScrollPane.setPreferredSize(new java.awt.Dimension(20,20));
		}
		return jScrollPane;
	}
	/**
	 * This method initializes jButton1
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setText("Select Folders...");
			jButton1.setMnemonic('F');
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					indexFolders.setFolders(folderList);
					indexFolders.renderFolders();
					indexFolders.setVisible(true);
					folderList = indexFolders.getFolders();
				}
			});
		}
		return jButton1;
	}
	/**
	 * This method initializes jButton2
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton2() {
		if (jButton2 == null) {
			jButton2 = new JButton();
			jButton2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					jButton1.setEnabled(false);//disable select folders
					jButton.setEnabled(false);//disable search
					jButton2.setEnabled(false);//disable index button
					jTabbedPane.setEnabledAt(jTabbedPane.indexOfTab("Search"), false);
					(new Thread() { 
						public void run() { 
							this.setPriority(Thread.NORM_PRIORITY-1); 
							runIndex();
							jButton1.setEnabled(true);
							jButton.setEnabled(true);
							jButton2.setEnabled(true);
							jTabbedPane.setEnabledAt(jTabbedPane.indexOfTab("Search"), true);
						}
					}).start();
				}
			});
			jButton2.setText("Create Index");
			jButton2.setMnemonic('I');
		}
		return jButton2;
	}
	/**
	 * This method initializes jTextArea
	 * 
	 * @return javax.swing.JTextArea
	 */
	private JTextArea getJTextArea() {
		if (jTextArea == null) {
			jTextArea = new JTextArea();
			jTextArea.setEditable(false);
			jTextArea.setWrapStyleWord(true);
		}
		return jTextArea;
	}
	/**
	 * This method initializes jScrollPane1
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane1() {
		if (jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setViewportView(getJTextArea());
			jScrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(
					java.awt.Color.gray, 1));
			jScrollPane1.setPreferredSize(new java.awt.Dimension(2, 48));
			jScrollPane1
					.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		}
		return jScrollPane1;
	}
	/**
	 * This method initializes jSplitPane
	 * 
	 * @return javax.swing.JSplitPane
	 */
	private JSplitPane getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new JSplitPane();
			jSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
			jSplitPane.setTopComponent(getJTabbedPane());
			jSplitPane.setBottomComponent(getJScrollPane1());
			jSplitPane.setDividerLocation(200);
			jSplitPane.setPreferredSize(new java.awt.Dimension(460,300));
			jSplitPane.setDividerSize(8);
		}
		return jSplitPane;
	}
	/**
	 * This method initializes jPanel3	
	 * 	
	 * @return javax.swing.JPanel	
	 */	
	private JPanel getJPanel3() {
		if (jPanel3 == null) {
			jPanel3 = new JPanel();
			jPanel3.add(getJButton1(), null);
			jPanel3.add(getJButton2(), null);
		}
		return jPanel3;
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */	
	private JPanel getJPanel() {
		if (jPanel == null) {
			jLabel3 = new JLabel();
			jLabel2 = new JLabel();
			jLabel1 = new JLabel();
			jLabel = new JLabel();
			jPanel = new JPanel();
			jPanel.setLayout(new BorderLayout());
			if (diskIndex != null)
			{
				jLabel.setText("Number of Documents: " + diskIndex.getCollectionStatistics().getNumberOfDocuments());
				jLabel1.setText("Number of Tokens: " + diskIndex.getCollectionStatistics().getNumberOfTokens());
				jLabel2.setText("Number of Unique Terms: " + diskIndex.getCollectionStatistics().getNumberOfUniqueTerms());
				jLabel3.setText("Number of Pointers: " + diskIndex.getCollectionStatistics().getNumberOfPointers());
			}
			else
			{
				jLabel.setText("Number of Documents: 0");
				jLabel1.setText("Number of Tokens: 0");
				jLabel2.setText("Number of Unique Terms: 0");
				jLabel3.setText("Number of Pointers: 0");
			}
			jPanel.add(getJPanel4(), java.awt.BorderLayout.NORTH);
		}
		return jPanel;
	}
	/**
	 * This method initializes jPanel4	
	 * 	
	 * @return javax.swing.JPanel	
	 */	
	private JPanel getJPanel4() {
		if (jPanel4 == null) {
			GridLayout gridLayout1 = new GridLayout();
			jPanel4 = new JPanel();
			jPanel4.setLayout(gridLayout1);
			gridLayout1.setRows(4);
			jPanel4.add(jLabel, null);
			jPanel4.add(jLabel2, null);
			jPanel4.add(jLabel3, null);
			jPanel4.add(jLabel1, null);
		}
		return jPanel4;
	}
	/** Start desktop Terrier */
	 	public static void main(String[] args) {
	 	  
		DesktopTerrier dTerrier = new DesktopTerrier();
		if (args.length == 1 && args[0].equals("--runindex")) {
			dTerrier.runIndex();
		} else 
		{
			//System.setErr(dTerrier.getOutputLog());
			if (args.length == 1 && args[0].equals("--debug")) {
				dTerrier.setDebug(true);
			}
			dTerrier.makeVisible();
		}
		
	}
	protected boolean desktop_debug = false;
	
	/** Set debugging for desktop */
	public void setDebug(boolean in)
	{
		desktop_debug = in;
	}


	
	private JScrollPane jScrollPane1 = null;
	private JSplitPane jSplitPane = null;
	private JPanel jPanel3 = null;
	private JPanel jPanel = null;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JLabel jLabel3 = null;
	private JPanel jPanel4 = null;
	


	/** Shows the main window. Will ask user if they wish to index the documentation if no
	  * index can be successfully loaded. */
	public void makeVisible()
	{
		this.setVisible(true);
		if(folderList.size() == 0 && diskIndex == null)
		{
			int n = JOptionPane.showConfirmDialog(
				this,
				"It appears that this is the first time you have used Desktop Terrier.\nIf you "+
				"would like Terrier to index its own documentation, press \"Yes\".\n"+
				"You can change the folders Terrier indexes using \"Select Folders\".", 
				"Desktop Terrier",
				JOptionPane.YES_NO_OPTION);
			if (n == JOptionPane.YES_OPTION)
			{
				folderList.add(ApplicationSetup.TERRIER_HOME+
					ApplicationSetup.FILE_SEPARATOR+"doc");
				logger.info(ApplicationSetup.TERRIER_HOME+ApplicationSetup.FILE_SEPARATOR+"doc");
				jButton1.setEnabled(false);//disable select folders
				jButton.setEnabled(false);//disable search
				jButton2.setEnabled(false);//disable index button
				
				//SwingUtilities.invokeLater(
				new Thread() {
					public void run() {
						this.setPriority(Thread.NORM_PRIORITY-1);
						runIndex();
						jButton1.setEnabled(true);
						jButton.setEnabled(true);
						jButton2.setEnabled(true);
					}
				} .start();
				//);
				
			}
		}	
	}

	/*
	 * This class is responsible for reading from the PipedInputStream
	 * There was a major problem concerning Threads that died.
	 * A Pipe is broken exception and a Reader Dead End exception is
	 * thrown every time a Thread dies. 
	 * We could not pinpoint which thread dies, and since this did
	 * not affect the logging output itself we just catch the 
	 * exception and ignore it.
	 */
	
	class ReaderThread extends Thread {
		PipedInputStream pi;

		ReaderThread(PipedInputStream _pi) {
			this.pi = _pi;
		}
		
		volatile boolean run = true;

		public void run() {
			
			//try {
				while (run) {
					final byte[] buf = new byte[1024];
					int l =0;
					try{	
						try{
							l = pi.read(buf);
						}
						catch (IOException e) {
								
							// Here we catch the exceptions and ignore them.
							if (e.getMessage().contains("Pipe broken") || e.getMessage().contains("Write end dead"))
							{
								try{
									Thread.sleep(250);
								} catch (Exception e2){}		   
							}
							else
							{
								logger.error(e.getMessage(),e);
								run = false;
								break;
							}
						}
					}
					catch (Exception e){
						// When the application closes a nullpointer exception is
						// thrown at line 700. We want to ignore it since it stems
						// from our Logging fix.
						logger.debug("Expected exception at shutdown", e);
					}
					final int len = l;
					
					if (len == -1) {
						continue;
					}
					try{
						SwingUtilities./*invokeAndWait*/invokeLater(new Runnable() {
							public void run() {
								jTextArea.append(new String(buf, 0, len));
	
								// Make sure the last line is always visible
								jTextArea.setCaretPosition(jTextArea.getDocument().getLength());
	
								// Keep the text area down to a certain character size
								int idealSize = 1000;
								int maxExcess = 500;
								int excess = jTextArea.getDocument().getLength() - idealSize;
								if (excess >= maxExcess) {
									jTextArea.replaceRange("", 0, excess);
								}
							}
					});
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
		   //   System.out.println("ReaderThread died....");

		}
	}
	
	
	
	/**
	 * This is the default constructor
	 */
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(
			value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
			justification="This is current way of setting block indexing")
	public DesktopTerrier() {
		super();
		//setting properties for the application
		ApplicationSetup.BLOCK_INDEXING = true;
		//assume some defaults so qe can work
		if (( ApplicationSetup.getProperty("querying.allowed.controls", null)) == null)
		{
			ApplicationSetup.setProperty("querying.allowed.controls", "c,start,end,qe");
		}
		if ((ApplicationSetup.getProperty("querying.postprocesses.order", null)) == null)
		{
			ApplicationSetup.setProperty("querying.postprocesses.order", "QueryExpansion");
		}
		if ((ApplicationSetup.getProperty("querying.postprocesses.controls", null)) == null)
		{
			ApplicationSetup.setProperty("querying.postprocesses.controls", "qe:QueryExpansion");
		}
		ApplicationSetup.setProperty("indexer.meta.forward.keys","docno,filename");
		ApplicationSetup.setProperty("indexer.meta.forward.keylens","26,2048");
		ApplicationSetup.setProperty("indexing.max.tokens", "10000");
		ApplicationSetup.setProperty("invertedfile.processterms","25000");
		ApplicationSetup.setProperty("ignore.low.idf.terms","false");
		ApplicationSetup.setProperty("matching.dsms", "BooleanFallback");
		filetypeColors = new FiletypeColors();
		initialize();
		addWindowListener(new WinHandler());
		//load in the directory list.
		String dirList = ApplicationSetup.makeAbsolute(ApplicationSetup
				.getProperty("desktop.directories.spec", "desktop.spec"),
				ApplicationSetup.TERRIER_VAR);
		folderList = load_list(new File(dirList));
		indexFolders = new IndexFolders(folderList, me);
		aboutTerrier = new AboutTerrier(this);
		helpDialog = new HelpDialog(this);
		fOpener = new MultiOSFileOpener();
		fOpener.load();
//		
//		//deciding which file opener to use based on the operating system
//		String osName = System.getProperty("os.name").toLowerCase();
//		//System.out.println("os.name="+osName);
//		if (osName.startsWith("windows"))
//		{
//			//System.out.println("Using Windows associations");
//			fOpener = new WindowsFileOpener();
//		}
//		else if (osName.startsWith("mac"))
//		{
//			//System.out.println("using mac associations");
//			fOpener = new MacOSXFileOpener();
//		}
//		else {
//			//System.out.println("using default associations");
//			fOpener = new AssociationFileOpener();
//		}
//		fOpener.load();
		if (loadIndices()) {
			//indices were loaded successfully - focus is on the search text field
			jTabbedPane.setEnabledAt(jTabbedPane.indexOfTab("Search"), true);
			jTabbedPane.setSelectedIndex(jTabbedPane.indexOfTab("Search"));
			getJTextField().requestFocusInWindow(); 
			
		} else {
			//indices failed to load, probably because we've not indexed
			// anything yet
			jTabbedPane.setEnabledAt(jTabbedPane.indexOfTab("Search"), false);
			jTabbedPane.setSelectedIndex(jTabbedPane.indexOfTab("Index"));
		}
		
		
		/*
		 * This is the logging initialisationt.
		 * Logoutput is directed to the jTextArea JTextField
		 * 
		 */
		piErr = new PipedInputStream();


		
		//if (osName.startsWith("linux"))
		//{
				OutputStream logOut = new OutputStream(){
					
					final StringBuilder s = new StringBuilder();
					public synchronized void write (int b) {
						s.append(new String(new byte[]{(byte)b}));
						if (s.length() > 50 && ((char)b) == '\n')
						{
							final String update = s.toString();
							s.setLength(0);
							SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										jTextArea.append(update);	
										// Make sure the last line is always visible
										jTextArea.setCaretPosition(jTextArea.getDocument().getLength());
	
										// Keep the text area down to a certain character size
										final int idealSize = 3000;
										final int maxExcess = 500;
										final int excess = jTextArea.getDocument().getLength() - idealSize;
										if (excess >= maxExcess) {
											jTextArea.replaceRange("", 0, excess);
										}
									}
								}
							);
			 			} 
					} 
				};
		//}
		/*
		else if (osName.startsWith("mac") ||osName.startsWith("windows") )
		{
			 piErr = new PipedInputStream();
			try {
				poErr = new PipedOutputStream(piErr);				
			} catch (IOException e) {
			  logger.error(e.getMessage(),e);
			}
			Logger.getRootLogger().addAppender(new WriterAppender(new SimpleLayout(),
				poErr ));
			// Create reader thread
			reada = new ReaderThread(piErr);
			reada.setDaemon(true);
			reada.start();
		}*/
	}
	
	ReaderThread reada = null;
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		String iconPath = ApplicationSetup.makeAbsolute(
				"images/terrier-desktop-search.gif", ApplicationSetup.TERRIER_SHARE);
		try {
			this.setIconImage(Toolkit.getDefaultToolkit().getImage(iconPath));
			} catch (NullPointerException npe) {
			logger.error("Problem loading the file terrier-desktop-search.gif in: "+iconPath,npe);	
		}
		this.setResizable(true);
		this.setJMenuBar(getJJMenuBar());
		this.setSize(753, 410);
		this.setContentPane(getJContentPane());
		this.setTitle("Terrier Desktop Search");
		this.setLocationRelativeTo(null);
	}
	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new java.awt.BorderLayout());
			jContentPane.setPreferredSize(new java.awt.Dimension(0,0));
			jContentPane.add(getJSplitPane(), java.awt.BorderLayout.CENTER);
		}
		return jContentPane;
	}
	private void runQuery_thread() {
		getJButton().setEnabled(false);
		SwingUtilities.invokeLater(new Thread() {
			public void run() {
				if (queryRunning)
					return;
				queryRunning = true;
				this.setPriority(Thread.NORM_PRIORITY-1);
				runQuery();
				getJButton().setEnabled(true);
				getJTextField().requestFocusInWindow(); 
				queryRunning = false;
			}
		});
	}

	/**
	 * Processes the query and returns the results.
	 */
	private void runQuery() {
		String query = jTextField.getText();
		if (query == null || query.length() == 0)
			return;
		try {
			Query q = null;
			try{
				q = QueryParser.parseQuery(query);
			} catch (Exception e) {
				//century kludge!
				//remove everything except character and spaces, and retry
				q = QueryParser.parseQuery(query.replaceAll("[^a-zA-Z0-9 ]", ""));	
			}
			
			if (q == null)
			{
				//give up
				return;
			}
			if (queryingManager == null)
			{
				return;
			}
			jTextField.setText(q.toString());	
			SearchRequest srq = queryingManager.newSearchRequest();
			srq.setQuery(q);
			srq.addMatchingModel(mModel, wModel);
			srq.setControl("c", "1.0d");
			queryingManager.runPreProcessing(srq);
			queryingManager.runMatching(srq);
			queryingManager.runPostProcessing(srq);
			queryingManager.runPostFilters(srq);
			renderResults(srq.getResultSet());
		} catch (Exception e) {
			logger.error("An exception when running the query: #"+query +"# :",e);
			
		}

	}
	/**
	 * Loads and returns a list of folders already 
	 * selected for indexing, from the given file.
	 * @param file the file from which to load 
	 *		the list of folders.
	 * @return the list of folders to index.
	 */
	private List<String> load_list(File file) {
		if (file == null || !file.exists())
			return new ArrayList<String>();
		ArrayList<String> out = new ArrayList<String>();
		try {
			BufferedReader buf = Files.openFileReader(file);
			String line;
			while ((line = buf.readLine()) != null) {
				//ignore empty lines, or lines starting with # from the methods
				// file.
				if (line.startsWith("#") || line.equals(""))
					continue;
				out.add(line.trim());
			}
			buf.close();
		} catch (IOException ioe) {
		}
		return out;
	}
	/**
	 * Saves the list of selected folders to index
	 * in the file with the given name.
	 * @param file the name of the file in which to 
	 *		save the selected folders.
	 * @param list the list of selected folders to save.
	 */
	private void save_list(File file, List<String> list) {
		try {
			PrintWriter writer = new PrintWriter(
					Files.writeFileWriter(file));
			for (int i = 0; i < list.size(); i++) {
				writer.println(list.get(i));
			}
			writer.close();
		} catch (IOException ioe) {
			logger.error("Error writing to file : " + file + " : " , ioe);
			return;
		}
	}
	
	private void deleteFiles(File dir)
	{
		String[] files = dir.list();
		for (int i = 0; i < files.length; i++) {
			File f = new File(dir, files[i]);
			if (f.exists()) 
			{
				if (f.isFile())
				{
					logger.info("Deleting: "+f+": "+f.delete());
				}
				else if (f.isDirectory() && ! f.getName().equals("CVS"))
				{
					deleteFiles(f);
					logger.info("Deleting: "+f+": "+f.delete());
				}
			}
		}	
	}
	
	/**
	 * Runs the indexing process for the documents 
	 * in the selected folders.
	 */
	private void runIndex() {		
		jLabel.setText("Number of Documents: ");
		jLabel1.setText("Number of Tokens: ");
		jLabel2.setText("Number of Unique Terms: ");
		jLabel3.setText("Number of Pointers: ");
	
		if (folderList == null || folderList.size() == 0)
		{
			logger.error("No folders specified to index. Aborting indexing process.");
			return;
		}
	
		try {
			//deleting existing files
			if (diskIndex!=null) {
				diskIndex.close();
				diskIndex = null;
				
			}
			queryingManager = null;	

			//remove any existing index
			File indexPath = new File(ApplicationSetup.TERRIER_INDEX_PATH);
			if (indexPath.exists())
				deleteFiles(indexPath);
			else
				if (! indexPath.mkdirs()) //ensure that the index folder exists
				{
					logger.error("ERROR: Could not create the index folders at: "+ indexPath);
					logger.error("Aborting indexing process");
					return;
				}
	
	
			Indexer indexer;
			final boolean useSinglePass = Boolean.parseBoolean(ApplicationSetup.getProperty("desktop.indexing.singlepass", "false"));
			indexer = ApplicationSetup.BLOCK_INDEXING
				? useSinglePass 
					? new BlockSinglePassIndexer(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX)  
					: new BlockIndexer(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX)
				: useSinglePass
					? new BasicSinglePassIndexer(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX)
					: new BasicIndexer(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX);
	
			SimpleFileCollection sfc = new SimpleFileCollection(folderList, true);
	
			indexer.index(new Collection[] { sfc });
	
			
			//load in the indexes
			if (loadIndices()) {
				//indices loaded
				jTabbedPane.setEnabledAt(jTabbedPane.indexOfTab("Search"), true);
				jTabbedPane.setSelectedIndex(jTabbedPane.indexOfTab("Search"));
				getJTextField().requestFocusInWindow(); 
				
			} else { //indices failed to load, probably because we've not indexed
					 // anything yet
				jTabbedPane.setEnabledAt(jTabbedPane.indexOfTab("Search"), false);
				jTabbedPane.setSelectedIndex(jTabbedPane.indexOfTab("Index"));
			}
		
			if (diskIndex != null)	
			{
				jLabel.setText("Number of Documents: " + diskIndex.getCollectionStatistics().getNumberOfDocuments());
				jLabel1.setText("Number of Tokens: " + diskIndex.getCollectionStatistics().getNumberOfTokens());
				jLabel2.setText("Number of Unique Terms: " + diskIndex.getCollectionStatistics().getNumberOfUniqueTerms());
				jLabel3.setText("Number of Pointers: " + diskIndex.getCollectionStatistics().getNumberOfPointers());
			}
			else
			{
				jLabel.setText("Number of Documents: 0");
				jLabel1.setText("Number of Tokens: 0");
				jLabel2.setText("Number of Unique Terms: 0");
				jLabel3.setText("Number of Pointers: 0");
			}
			
		} catch(Exception e) {
			logger.error("An unexpected exception occured while indexing. Indexing has been aborted.",e);
			
		}
	}
	/**
	 * Returns true if loading the index succeeded and the system is ready for
	 * querying. If false, then the collection needs to be indexed first.
	 */
	private boolean loadIndices() {
		if (diskIndex != null)
			try{
				diskIndex.close();
			} catch (IOException ioe) {
				logger.warn("Problem closing old index", ioe);
			}
		diskIndex = null;
		diskIndex = Index.createIndex();
		
		if (diskIndex == null)
		{
			return false;
		}
		jLabel.setText("Number of Documents: " + diskIndex.getCollectionStatistics().getNumberOfDocuments());
		jLabel1.setText("Number of Tokens: " + diskIndex.getCollectionStatistics().getNumberOfTokens());
		jLabel2.setText("Number of Unique Terms: " + diskIndex.getCollectionStatistics().getNumberOfUniqueTerms());
		jLabel3.setText("Number of Pointers: " + diskIndex.getCollectionStatistics().getNumberOfPointers());
		try{
			if (managerName.indexOf('.') == -1)
				managerName = "org.terrier.querying."+managerName;
			queryingManager = (Manager) (Class.forName(managerName)
				.getConstructor(new Class[]{Index.class})
				.newInstance(new Object[]{diskIndex}));
		} catch (Exception e) {
			logger.warn("Problem loading Manager ("+managerName+"): ",e);
			return false;
		}
		if (queryingManager == null)
			return false;
		return true;
	}
	/**
	 * Render the given resultset into the tblResults.
	 * @param rs The result set to render
	 */
	private void renderResults(ResultSet rs) throws Exception {
		Vector<String> HeaderRow = new Vector<String>(5);
		MetaIndex meta = diskIndex.getMetaIndex();
		HeaderRow.add(" ");
		HeaderRow.add("File Type");
		HeaderRow.add("Filename");
		HeaderRow.add("Directory");
		HeaderRow.add("Score");
		System.err.println("INFO: RenderResults "+rs.getExactResultSize()+" "+rs.getResultSize());
		int ResultsSize = rs.getResultSize();
		int[] docids = rs.getDocids();
		double[] scores = rs.getScores();
		Vector<Vector<String>> rows = new Vector<Vector<String>>(ResultsSize);
		for (int i = 0; i < ResultsSize; i++) {
			Vector<String> thisRow = new Vector<String>(5);
			thisRow.add("" + (i + 1));
			String f = meta.getItem("filename", docids[i]);
			System.err.println("INFO: RenderResults "+f);
			//String f = (String) fileList.get(docids[i]);
			if (f == null)
				continue;
			int dotIndex = f.lastIndexOf('.');
			String extension = f.substring(dotIndex+1);
			thisRow.add(filetypeColors.getFiletype(extension));
			thisRow.add(new File(f).getName());
			thisRow.add(new File(f).getPath());
			thisRow.add(Rounding.toString(scores[i], 4));
			rows.add(thisRow);
		}
		TableModel model = new NonEditableTableModel(rows, HeaderRow);
		jTable.setModel(model);
		TableColumn col = jTable.getColumnModel().getColumn(0);
		col.setPreferredWidth(30);
		col.setMinWidth(20);
		col.setMaxWidth(100);
		col = jTable.getColumnModel().getColumn(1);
		col.setPreferredWidth(75);
		col.setMinWidth(50);
		col.setMaxWidth(100);
		col.setCellRenderer(new CustomTableCellRenderer(filetypeColors));
		col = jTable.getColumnModel().getColumn(4);
		col.setPreferredWidth(50);
		col.setMinWidth(20);
		col.setMaxWidth(100);
		jScrollPane.getViewport().setViewPosition(new Point(0,0));
		System.err.println("INFO: RenderResults done rendering");

	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
/**
 * Extends the default table model by overriding the isEditable method.
 * @author Vassilis Plachouras
 */
class NonEditableTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 1L;
	/**
	 * A constructor that calls the constructor of the super class.
	 * @param rows a vector containing the data.
	 * @param columnNames a vector containing the column names
	 */
	@SuppressWarnings({ "rawtypes" }) //super-class is not generic either
	public NonEditableTableModel(Vector rows, Vector columnNames) {
		super(rows, columnNames);
	}
	/**
	 * Makes the cells of the table non-editable, by default.
	 */
	public boolean isCellEditable(int row, int column) {
		return false;
	}
}

/**
 * Used for assigning a color to each filetype.
 * @author Vassilis Plachouras
 */
class FiletypeColors {
	
	Hashtable<String,Color> typeColors = new Hashtable<String,Color>();
	
	Hashtable<String,String> filetypes = new Hashtable<String,String>();
	/**
	 * A default constructor that reads the property
	 * <tt>desktopsearch.filetype.colors</tt> and assigns the
	 * colors used for the file types.
	 * <p><ul><li><tt>desktopsearch.filetype.colors</tt> - controls the colours for various file types. Default value is <tt>Text:(221 221 221),TeX:(221 221 221),Bib:(221 221 221),PDF:(236 67 69),HTML:(177 228 250),Word:(100 100 255),Powerpoint:(250 110 49),Excel:(38 183 78),XHTML:(177 228 250),XML:(177 228 250)</tt>. 
	 *  If you care to the extent that you want to change this, then the format is obvious.</li>
	 */
	public FiletypeColors() {
		String staticMappings = 
			ApplicationSetup.getProperty("desktopsearch.filetype.colors",
					"Text:(221 221 221),TeX:(221 221 221),Bib:(221 221 221),PDF:(236 67 69),"+
					"HTML:(177 228 250),Word:(100 100 255),Powerpoint:(250 110 49),Excel:(38 183 78),"+
					"XHTML:(177 228 250),XML:(177 228 250)");
		String staticTypes = 
			ApplicationSetup.getProperty("desktopsearch.filetype.types",
			"txt:Text,text:Text,tex:TeX,bib:Bib,pdf:PDF,html:HTML,htm:HTML,xhtml:XHTML,xml:XML,doc:Word,ppt:Powerpoint,xls:Excel");
		if (staticMappings.length() > 0) {
			String[] mappings = staticMappings.split("\\s*,\\s*");
			for(int i=0;i<mappings.length;i++)
			{
				if (mappings[i].indexOf(":") < 1)
					continue;
				String[] mapping = mappings[i].split(":");
				if (mapping.length == 2 && mapping[0].length() > 0 && mapping[1].length() > 0) {
					String[] colorComponents = mapping[1].substring(1,mapping[1].length()-1).split("\\s* \\s*");
					if (colorComponents.length==3) {
						Color c = new Color(Integer.parseInt(colorComponents[0]),
											Integer.parseInt(colorComponents[1]),
											Integer.parseInt(colorComponents[2]));
						typeColors.put(mapping[0], c);
						
					}
				}
			}				
		}
		if (staticTypes.length() > 0) {
			String[] mappings = staticTypes.split("\\s*,\\s*");
			for(int i=0;i<mappings.length;i++)
			{
				if (mappings[i].indexOf(":") < 1)
					continue;
				String[] mapping = mappings[i].split(":");
				if (mapping.length == 2 && mapping[0].length() > 0 && mapping[1].length() > 0) {
					filetypes.put(mapping[0], mapping[1]);
				}
			}		 
		}
	}
	/**
	 * Returns the color associated with a file type.
	 * @param fileType the type of the file we need to get a color for.
	 * @return Color the color associated with the file type.
	 */
	public Color getColor(String fileType) {
		Color rtrColor = (Color)typeColors.get(fileType);
		if (rtrColor==null) 
			return Color.GRAY;
		return rtrColor;
	}
	
	/**
	 * Returns the a string denoting the file type for a given extension.
	 * @param fileExtension the extension of the file.
	 * @return String the type of the file with the given extension.
	 */
	public String getFiletype(String fileExtension) {
		String rtrType = (String)filetypes.get(fileExtension);
		if (rtrType==null)
			return "Unknown";
		return rtrType;
	}
	
	
	
	
	
}

/**
 * Implements a custom renderer for the cells of the
 * table that contain the file type information.
 * @author Vassilis Plachouras
 */
class CustomTableCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1L;
	FiletypeColors filetypeColors = null;
	/**
	 * A constructor for setting the file types and associations of the cells of a table.
	 * @param filetypeColors the associated colors and types for the file extensions
	 */
	public CustomTableCellRenderer(FiletypeColors filetypeColors) {
		super();
		this.filetypeColors = filetypeColors; 
	}
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
	   boolean hasFocus, int row, int column) {
		Component cell = super.getTableCellRendererComponent
		   (table, value, isSelected, hasFocus, row, column);

		if( value instanceof String ) {
			String type = (String) value;
			cell.setBackground(filetypeColors.getColor(type));
		}

		return cell;
	}
}



 
