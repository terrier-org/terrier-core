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
 * The Original Code is HelpDialog.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.applications.desktop;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.applications.desktop.filehandling.FileOpener;
import org.terrier.applications.desktop.filehandling.MultiOSFileOpener;
/**
 * Shows a simple help dialog for the desktop application.
 * @author Vassilis Plachouras
  */
public class HelpDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final Logger logger = LoggerFactory.getLogger(HelpDialog.class);
	private javax.swing.JPanel jContentPane = null;
	private JPanel jPanel = null;
	private JButton jButton = null;
	private JScrollPane jScrollPane = null;
	private JTextPane jTextPane = null;
	/**
	 * This is the default constructor
	 */
	public HelpDialog(JFrame parent) {
		super();
		initialize(parent);
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize(JFrame parent) {
		this.setTitle("Desktop Search Help");
		this.setSize(510, 334);
		this.setContentPane(getJContentPane());
		this.setLocationRelativeTo(parent);
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
			jButton.setText("Close");
			jButton.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					setVisible(false);
				}
			});
		}
		return jButton;
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
		}
		return jScrollPane;
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
			jTextPane.setText(helpText);
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
			jTextPane.setEditable(false);			
		}
		return jTextPane;
	}
	/** The help text shown in the dialog.*/
	private static final String helpText = 				
		"<html>"+
		"<head><title>Terrier Desktop Search</title></head>"+
		"<body><h1>Terrier Desktop Search</h1>"+
		"<h2>0. Contents</h2>"+
		"<ul>"+
		"<li>1. Overview</li>"+
		"<li>2. Indexing</li>"+
		"<li>3. Searching</li>"+
		"<li>4. Advanced command-line options"+ 
		"</ul>"+
		"<h2>1. Overview</h2>" +
		"<p>Terrier Desktop Search is an application demonstrating how the Terrier "+
		"Information Retrieval platform can be used for desktop search. It can " +
		"index various types of documents, including HTML and PDF documents, as "+
		"well as Microsoft Word, Excel and Powerpoint documents.</p>" +
		"<p>The graphical user interface of the application consists of an indexing "+
		"and searching tab. When the application runs for the first time, the indexing "+
		"tab appears. When you run the application after having indexed a set of " +
		"documents, the searching tab will appear.</p>" +
		"<p>Additional information, you" +
		"can find under the directory doc/ of the distribution and more particularly in the" +
		"file desktop_search.txt.</p><br>"+
		"<h2>2. Indexing</h2>" +
		"<p>In the indexing tab, there are two buttons. The first opens a dialog in order "+
		"to select the folders you wish to index and the second initiates the indexing "+
		"process.</p>"+
		"<p>When you click the &quot;Select Folders&quot; button, a dialog opens, where "+
		"you can select a set of folders to index. The application will look into these "+
		"folders recursively, and will index the documents from which it can extract " +
		"meaningful text. According to the extension of " +
		"the files, the application uses the corresponding parser. If there is no parser "+
		"for a particular file type, then the application assumes that it cannot extract the" +
		" text from the file and ignores it. The association between file extensions and " +
		"parsers is set with the property <tt>indexing.simplefilecollection.extensionsparsers</tt>, " +
		"which defaults to the value:<br>" +
		"<tt>txt:FileDocument,text:FileDocument,tex:FileDocument,bib:FileDocument," +
		"pdf:PDFDocument,html:HTMLDocument,htm:HTMLDocument,xhtml:HTMLDocument,xml:HTMLDocument," +
		"doc:MSWordDocument,ppt:MSPowerpointDocument,xls:MSExcelDocument</tt>" +
		"<br>" +
		"<p>All the parsers, apart from the ones that handle html and text files, depend on external " +
		"libraries, which are described in the file doc/included_jars.txt." +
		"</p>"+
		"<p>Once you have selected the folders to index, you may click the " +
		"&quot;Index&quot; button in order to start the indexing process. The progress "+
		"of the indexing is shown in the lower part of the window, where the output of the "+
		"indexing is shown. When the indexing is over, the focus moves to the search tab " +
		"automatically.</p><br>"+
		
		"<h2>3. Searching</h2>"+
		"<p>In the searching tab, you can enter a query in the text field and press the "+
		"button &quot;Search&quot; to obtain the retrieval results. The results are "+
		"shown in the table below the search field, as a ranked list of documents. The "+
		"table has four columns. The first one contains the rank of a document, the second "+
		"one contains the file name of a document. The third one contains the full path to "+
		"the document and finally the fourth one contains the score of the document.</p>"+
		"<p>To formualte a query, you can incorporate the "+
		"<a href=\"http://terrier.orgdoc/querylanguage.html\">query language "+ 
		"of Terrier</a>. For example:</p>"+
		"<ul>"+
		"<li>the query <tt>&quot;information retrieval&quot;</tt>"+
		"should retrieve documents where the two query terms are either in the same, or in "+
		"consecutive blocks.</li> "+
		"<li>the query <tt>&quot;information retrieval&quot;~5</tt> "+
		"should retrieve documents in which the query terms appear within 5 blocks, "+
		"irrespectively of the query term order.</li> "+
		"<li>With the operators plus or minus, we may specify that a term should, or "+ 
		"shouldn't appear in the retrieved documents. For example, for the query "+
		"<tt>information retrieval +book</tt>, the retrieved documents should at least "+ 
		"contain the term book.</li> "+
		"<li>in the query <tt>information retrieval^2.5</tt>, the query term retrieval has a 2.5 times "+
		"higher weight that the term information.</li> "+
		"<li>the query <tt>information retrieval c:7</tt> will perform retrieval for the query terms "+
		"<tt>information</tt> and <tt>retrieval</tt>, setting the value of the term frequency normalisation "+
		"parameter c equal to 7.</li>"+
		"</ul>"+
		
		"<p>In order to open one of the retrieved documents, you may double-click on " +
		"its filename, i.e. the corresponding cell of the second column. Opening the "+
		"retrieved files is a platform-dependent function. In Windows environments, " +
		"the application uses the file associations used by the operating system, " +
		"while in other environments, such as Linux or Mac OS X, the file associations " +
		"need to be set by the user. In these cases, the associations are saved in " +
		"a file with the default name desktop.fileassoc in the var directory of "+
		"your installation.</p>"+
		"<p>If there is already an application associated with the file, then this " +
		"application will start and open the file you double-clicked on. In the "+
		"case when there is no application associated, a dialog will appear, in " +
		"order to assist you with selecting an appropriate application.</p><br>"+
		
		"<h2>4. Advanced command-line options</h2>"+
		"<p>Should you have trouble using Desktop Terrier, ie if the application " +
		"inexplicably disappears, the start Terrier using the --debug option. eg: </p>"+
		"For Unix/Linux/Mac OS X<br>" + 
		"bin/desktop_terrier.sh --debug<br>" + 
		"For Windows<br>" + 
		"bin\\desktop_terrier.bat --debug<br>" +
		"<p>If you use Desktop Terrier regularly, you may wish to have Terrier re-index " +  
		"your documents automatically at set times. You can do this by scheduling " + 
		"Terrier to run with the --runindex option. eg:</p>" +
		"For Unix/Linux/Mac OS X<br>" + 
		"bin/desktop_terrier.sh --reindex<br>"+
		"For Windows<br>"+
		"bin\\desktop_terrier.bat --reindex<br>"+
		"<p>You need to schedule this command line. On Unix use the crontab utility. " +  
		"On Windows use Scheduled Tasks, which can be found in the Control Panel.</p>"+
		"<hr>"+
		"Terrier is distributed under the terms of the <a href=\"http://www.mozilla.org/MPL/MPL-1.1.html\">Mozilla Public License (MPL)</a>."+
		"<br>"+
		"Homepage: <a href=\"http://terrier.org\">http://terrier.org</a><br>"+
		"<a href=\"http://ir.dcs.gla.ac.uk/\">Information Retrieval Group</a><br>"+
		"<a href=\"http://www.dcs.gla.ac.uk/\">School of Computing Science</a><br>"+
		"Copyright (C) 2004-2008 <a href=\"http://www.gla.ac.uk/\">University of Glasgow</a>. All Rights Reserved."+
		"</body></html>";
}  //  @jve:decl-index=0:visual-constraint="10,10"
