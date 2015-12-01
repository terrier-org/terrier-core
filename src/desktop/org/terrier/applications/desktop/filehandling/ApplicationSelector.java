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
 * The Original Code is ApplicationSelector.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.applications.desktop.filehandling;
import java.io.File;
import java.io.IOException;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.FlowLayout;
import java.awt.BorderLayout;
/**
 * A dialog that selects an application to use for opening
 * files with a given extension.
 * @author Vassilis Plachouras
  */
public class ApplicationSelector extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final Logger logger = LoggerFactory.getLogger(ApplicationSelector.class);
	private javax.swing.JPanel jContentPane = null;
	private JButton jButton = null;
	private JPanel jPanel = null;
	private JPanel jPanel1 = null;
	private JPanel jPanel2 = null;
	private JLabel jLabel = null;
	private JButton jButton1 = null;
	private JLabel jLabel2 = null;
	private JTextField jTextField = null;
	
	private JButton jButton2 = null;
	
	private String applicationPath = null;
	
	/**
	 * This is the default constructor
	 */
	protected ApplicationSelector() {
		super();
		initialize();
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setBounds(0, 0, 352, 196);
		this.setResizable(false);
		this.setModal(true);
		this.setTitle("Open with ...");
		this.setContentPane(getJContentPane());
	}
	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if(jContentPane == null) {
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new java.awt.BorderLayout());
			jContentPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(5,5,5,5));
			jContentPane.add(getJPanel(), java.awt.BorderLayout.NORTH);
			jContentPane.add(getJPanel1(), java.awt.BorderLayout.SOUTH);
			jContentPane.add(getJPanel2(), java.awt.BorderLayout.CENTER);
		}
		return jContentPane;
	}
	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("OK");
			jButton.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					setVisible(false);
					String appText = jTextField.getText();
					if (!appText.equals(""))
						applicationPath = appText;
					else 
						applicationPath = null;	
				}
			});
		}
		return jButton;
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel() {
		if (jPanel == null) {
			jLabel = new JLabel();
			jPanel = new JPanel();
			jPanel.setLayout(new BorderLayout());
			jLabel.setText("Extension");
			jLabel.setPreferredSize(new java.awt.Dimension(300,50));
			jLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
			jLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
			jPanel.setPreferredSize(new java.awt.Dimension(353,76));
			jPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,0,0,0));
			jPanel.add(jLabel, java.awt.BorderLayout.CENTER);
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
			flowLayout1.setAlignment(java.awt.FlowLayout.RIGHT);
			flowLayout1.setHgap(10);
			jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,0,0,0));
			jPanel1.add(getJButton(), null);
			jPanel1.add(getJButton2(), null);
		}
		return jPanel1;
	}
	/**
	 * This method initializes jPanel2	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			FlowLayout flowLayout2 = new FlowLayout();
			jLabel2 = new JLabel();
			jPanel2 = new JPanel();
			jPanel2.setLayout(flowLayout2);
			jLabel2.setText("Application");
			flowLayout2.setAlignment(java.awt.FlowLayout.LEFT);
			flowLayout2.setHgap(5);
			jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", java.awt.Font.BOLD, 12), java.awt.Color.black));
			jPanel2.add(jLabel2, null);
			jPanel2.add(getJTextField(), null);
			jPanel2.add(getJButton1(), null);
		}
		return jPanel2;
	}
	/**
	 * This method initializes jButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setText("Browse...");
			jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
			jButton1.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					selectFile();
				}
			});
		}
		return jButton1;
	}
	/**
	 * This method initializes jTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	private JTextField getJTextField() {
		if (jTextField == null) {
			jTextField = new JTextField();
			jTextField.setPreferredSize(new java.awt.Dimension(140,20));
		}
		return jTextField;
	}
	/**
	 * This method initializes jButton2	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getJButton2() {
		if (jButton2 == null) {
			jButton2 = new JButton();
			jButton2.setText("Cancel");
			jButton2.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					setVisible(false);
				}
			});
		}
		return jButton2;
	}
	
	private void selectFile() {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
	            	File file = fc.getSelectedFile();
			if (!file.isDirectory())
			{
				try{
					jTextField.setText(file.getCanonicalPath());
				}catch (IOException ioe) {
				
					logger.error("IOException when adding folder : "+ioe.getMessage(), ioe);
					
				}
			}
		}
	}
	/**
	 * This method is used to set extension
	 * @param ext
	 */
	public void setExtension(String ext) {
		jLabel.setText("<html><body><p>You have chosen to open a ."+ext+" file.</p><br>"+
				"Please, type in the textfield the full path of the application you want to use, or click 'Browse...' to select it.</body></html>");
	}
	/**
	 * This method is used to get application path
	 * @return String
	 */
	public String getApplicationPath() {
		return applicationPath;
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
