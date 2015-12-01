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
 * The Original Code is AboutTerrier.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.applications.desktop;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.applications.desktop.filehandling.FileOpener;
import org.terrier.applications.desktop.filehandling.MultiOSFileOpener;
import org.terrier.utility.ApplicationSetup;
/**
 * The about dialog for the desktop search application.
 * @author Vassilis Plachouras
 */
public class AboutTerrier extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** logger */
	protected static final Logger logger = LoggerFactory.getLogger(AboutTerrier.class);
	private String imagePath = null;
	private javax.swing.JPanel jContentPane = null;
	private JLabel jLabel = null;
	private JPanel jPanel = null;
	private JButton jButton = null;
	private JTextPane jTextPane = null;
	private JScrollPane jScrollPane = null;
	/**
	 * This is the default constructor
	 */
	public AboutTerrier() {
		super();
		imagePath = ApplicationSetup.TERRIER_SHARE;
		if (imagePath == null) 
			imagePath = "../share/images/";
		initialize();
	}
	/** Use the specified frame as the parent window
	 * @param parent parent window for dialog
	 */
	public AboutTerrier(JFrame parent) {
		this();
		this.setLocationRelativeTo(parent);
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setBackground(java.awt.SystemColor.window);
		this.setModal(true);
		this.setResizable(false);
		this.setTitle("About Terrier Desktop Search");
		this.setSize(276, 275);
		this.setContentPane(getJContentPane());
	}
	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel = new JLabel();
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new java.awt.BorderLayout());
			
			jLabel.setText("");
			String iconPath = ApplicationSetup.makeAbsolute("terrier-logo-web.jpg", ApplicationSetup.TERRIER_SHARE);
			try {
				jLabel.setIcon(new ImageIcon(iconPath, "Terrier logo"));
			} catch(NullPointerException npe) {
				logger.error("A NullPointerException exception occured while trying to load: "+iconPath,npe);
			}
			jLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
			jLabel.setBackground(java.awt.SystemColor.window);
			jContentPane.setBackground(java.awt.SystemColor.window);
			jContentPane.add(jLabel, java.awt.BorderLayout.NORTH);
			jContentPane.add(getJPanel(), java.awt.BorderLayout.SOUTH);
			jContentPane.add(getJScrollPane(), java.awt.BorderLayout.CENTER);
		}
		return jContentPane;
	}
	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setBackground(new java.awt.Color(204, 204, 204));
			jPanel.add(getJButton(), null);
		}
		return jPanel;
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
					//System.out.println("actionPerformed()"); // TODO
					// Auto-generated Event stub actionPerformed()
					setVisible(false);
				}
			});
		}
		return jButton;
	}
	/**
	 * This method initializes jTextPane	
	 * 	
	 * @return javax.swing.JTextPane	
	 */    
	private JTextPane getJTextPane() {
		if (jTextPane == null) {
			jTextPane = new JTextPane();
			jTextPane.setContentType("text/html");
			jTextPane.setText(
					"<html><head></head>" +
					"<body><p>Terrier Desktop Search is an application " +
					"demonstrating how to use the Terrier Information " +
					"Retrieval Platform (v"+ApplicationSetup.TERRIER_VERSION+") for desktop searching.</p> " +
					"<p>It is distributed under the terms of the " +
					"<a href=\"http://www.mozilla.org/MPL/MPL-1.1.html\">" +
					"Mozilla Public License (MPL)</a>.</p> " +
					"Homepage: <a href=\"http://terrier.org\">http://terrier.org</a><br>"+
					"<a href=\"http://ir.dcs.gla.ac.uk/\">Information Retrieval Group</a><br>"+
					"<a href=\"http://www.dcs.gla.ac.uk/\">School of Computing Science</a><br>"+
					"Copyright (C) 2004-2014 <a href=\"http://www.gla.ac.uk/\">University of Glasgow</a>. All Rights Reserved."+
					"</body></html>");
			jTextPane.setEditable(false);
			jTextPane.addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent e) {
					if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
						try{
							FileOpener opener = new MultiOSFileOpener();
							opener.open(e.getURL().toString());
						}catch (Exception ex) {
							logger.error("", ex);
						}
					}
				}
			});
			jTextPane.setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
			jTextPane.setPreferredSize(new java.awt.Dimension(1,1));
		}
		return jTextPane;
	}
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJTextPane());
			jScrollPane.setPreferredSize(new java.awt.Dimension(100,100));
		}
		return jScrollPane;
	}
  }  //  @jve:decl-index=0:visual-constraint="10,10"
