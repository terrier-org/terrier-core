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
 * The Original Code is IndexFolders.java.
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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * The dialog used for selecting the folders to index in the desktop terrier application.
 * @author Craig Macdonald, Vassilis Plachouras
  */
public class IndexFolders extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final Logger logger = LoggerFactory.getLogger(IndexFolders.class);
	private List<String> folders = null;
	private Vector<String> workingFolders = null;
	
	private javax.swing.JPanel jContentPane = null;
	private JPanel jPanel = null;
	private JPanel jPanel1 = null;
	private JButton jButton = null;
	private JButton jButton1 = null;
	private JButton jButton2 = null;
	private JScrollPane jScrollPane = null;
	private JList<String> jList1 = null;
	private JButton jButton3 = null;
	
	private JPanel jPanel2 = null;
	private JLabel jLabel = null;
	private JPanel jPanel3 = null;
	/**
	 * This is the default constructor
	 */
	public IndexFolders(JFrame parent) {
		super(parent, true);
		this.setLocationRelativeTo(parent);
		initialize();
	}
	/**
	 * Sets the list of selected folders.
	 * @param v the list of selected folders.
	 */
	public void setFolders(List<String> v) {
		folders = v;
		workingFolders = new Vector<String>(v);
	}
	
	/**
	 * A constructor with a given list of 
	 * selected folders.
	 * @param v the list of selected folders.
	 */
	public IndexFolders(List<String> v, JFrame parent) {
		this(parent);
		folders = v;
		workingFolders = new Vector<String>(v);
	}
	
	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setModal(true);
		this.setTitle("Select Folders");
		this.setResizable(true);
		this.setSize(400, 278);
		this.setContentPane(getJContentPane());
	}
	/**
	 * This method initializes jContentPane
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if(jContentPane == null) {
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(5,5,5,5));
			jContentPane.add(getJPanel(), java.awt.BorderLayout.CENTER);
			jContentPane.add(getJPanel1(), java.awt.BorderLayout.SOUTH);
			jContentPane.add(getJPanel2(), java.awt.BorderLayout.NORTH);
		}
		return jContentPane;
	}
	
	/**
	 * This method initializes jPanel	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(new BorderLayout());
			jPanel.setName("jPanel");
			jPanel.setMinimumSize(new Dimension(300,142));
			jPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray,1), "Selected Folders", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
			jPanel.add(getJScrollPane(), java.awt.BorderLayout.NORTH);
			jPanel.add(getJPanel3(), java.awt.BorderLayout.SOUTH);
		}
		return jPanel;
	}
	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			FlowLayout flowLayout1 = new FlowLayout();
			jPanel1 = new JPanel();
			jPanel1.setLayout(flowLayout1);
			jPanel1.setName("jPanel1");
			jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,0,0,0));
			flowLayout1.setHgap(6);
			flowLayout1.setAlignment(java.awt.FlowLayout.RIGHT);
			jPanel1.add(getJButton2(), null);
			jPanel1.add(getJButton3(), null);
		}
		return jPanel1;
	}
	/**
	 * This method initializes jList	
	 * 	
	 * @return javax.swing.JList	
	 */    
	/*private JList getJList() {
		if (jList1 == null) {
			jList1 = new JList();
			jList1.setPreferredSize(new java.awt.Dimension(100,80));
			jList1.setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
			jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		}
		return jList1;
	}*/
	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("Add...");
			jButton.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					selectDirectory();
				}
			});
		}
		return jButton;
	}
	/**
	 * This method initializes jButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setText("Remove");
			jButton1.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					removeDirectory();
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
			jButton2.setText("OK");
			jButton2.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					folders = new ArrayList<String>(workingFolders);
					setVisible(false);
				}
			});
		}
		return jButton2;
	}
	
	/** Perform the rendering of the data */
	public void renderFolders() {
		jList1.setListData(workingFolders);
	}
	/**
	 * Selects a file, using the component JFileChooser.
	 */
	private void selectDirectory() {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fc.showDialog(this, "Select folder");
		if (returnVal == JFileChooser.APPROVE_OPTION) {
                	File file = fc.getSelectedFile();
			if (file.isDirectory())
			{
				try{
					workingFolders.add(file.getCanonicalPath());
				}catch (IOException ioe) {
					logger.error("IOException when adding folder : "+ioe.getMessage(),ioe);
					
				}
				renderFolders();
			}
		}
	}
	
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setPreferredSize(new java.awt.Dimension(290,80));
			jScrollPane.setViewportView(getJList1());
		}
		return jScrollPane;
	}
	/**
	 * This method initializes jList1	
	 * 	
	 * @return javax.swing.JList	
	 */    
	private JList<String> getJList1() {
		if (jList1 == null) {
			jList1 = new JList<String>();
		}
		return jList1;
	}
	
	/**
	 * Returns the list of selected folders.
	 * @return the list of selected folders.
	 */
	public List<String> getFolders() {
		return folders;
	}
	
	/** 
	 * Removes a folder from the list of 
	 * selected folders.
	 */
	private void removeDirectory() {
		String remove = (String)jList1.getSelectedValue();
		workingFolders.remove(remove);
		renderFolders();
	}
	
	/**
	 * This method initializes jButton3	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getJButton3() {
		if (jButton3 == null) {
			jButton3 = new JButton();
			jButton3.setText("Cancel");
			jButton3.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					setVisible(false);
					
				}
			});
		}
		return jButton3;
	}
	/**
	 * This method initializes jPanel2	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			jLabel = new JLabel();
			jPanel2 = new JPanel();
			jPanel2.setLayout(new BorderLayout());
			jLabel.setText("<html><body><p>Terrier can search folders of your computer. Please add the folders you want Terrier to scan for documents.</p></body></html>");
			jLabel.setPreferredSize(new java.awt.Dimension(350,50));
			jLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
			jLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
			jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(5,5,5,5));
			jPanel2.add(jLabel, java.awt.BorderLayout.CENTER);
		}
		return jPanel2;
	}
	/**
	 * This method initializes jPanel3	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel3() {
		if (jPanel3 == null) {
			jPanel3 = new JPanel();
			jPanel3.add(getJButton(), null);
			jPanel3.add(getJButton1(), null);
		}
		return jPanel3;
	}
  }  //  @jve:decl-index=0:visual-constraint="10,10"
