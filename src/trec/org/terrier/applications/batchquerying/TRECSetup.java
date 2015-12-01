/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org 
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.ac.gla.uk
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
 * The Original Code is TRECSetup.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.applications.batchquerying;
import java.io.*;
/**
 * This method is for setting the correct file and directory names
 * in the files etc/collection.spec, etc/terrier.properties, etc.
 * terrier.properties.
 * @author Vassilis Plachouras
 */
public class TRECSetup {
	
	/**
	 * Starts the application. It takes arguments. The first is
	 * the directory in which the file README is (it is assumed that
	 * the program java is executed from this directory) and the 
	 * second is a directory that contains the files to be indexed.
	 * @param args an array of command-line arguments
	 */
	public static void main(java.lang.String[] args) {
		if (args.length < 1 || args.length > 2) {
			System.err.println("usage : TRECSetup {-debug} <install dir> ");//<collection dir>");
			System.err.println("where <install dir> is the directory where the system is installed");
			//System.err.println("and <collection dir> is the directory under which there are all the");
			//System.err.println("files to be indexed.");
			System.err.println("Exiting");
			return;
		}
		boolean debug = false;
		int arg=0;
		if (args[arg].equals("-debug"))
		{
			debug = true;
		}
		String installDirectory = args[arg];
		//String collectionDirectory = args[1];
		//remove any trailing file separators from the given paths
		if (installDirectory.endsWith(File.separator))
			installDirectory = installDirectory.substring(0, installDirectory.length()-1);
		
		//if (collectionDirectory.endsWith(File.separator))
		//	collectionDirectory = collectionDirectory.substring(0, collectionDirectory.length()-1);
		
		
		if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
			StringBuilder tmpInstallDirectory = new StringBuilder();
			for (int i=0; i<installDirectory.length(); i++) {
				char c = installDirectory.charAt(i);
				switch(c) {
					case '\\' : tmpInstallDirectory.append("\\\\"); break;
					default   : tmpInstallDirectory.append(c); break;
				}
			}
			installDirectory = tmpInstallDirectory.toString();
		}
	
		String ETC_Dir = System.getProperty("terrier.etc", installDirectory + File.separator + "etc" ) + File.separator;
		try {	
			//creating an collection specification file
			//with only a comment line
			PrintWriter adCollection = new PrintWriter(new FileWriter( ETC_Dir + "collection.spec"));
			System.out.println("Creating collection.spec file.");
			adCollection.println("#add the files to index");
			adCollection.close();

	
			//creating a terrier-log.xml file
			PrintWriter terrierlog = new PrintWriter(new FileWriter(ETC_Dir+ "logback.xml"));
			System.out.println("Creating logging configuration (logback.xml) file in "+ETC_Dir);
			terrierlog.println("<configuration>");
			terrierlog.println("  <appender name=\"STDOUT\" class=\"ch.qos.logback.core.ConsoleAppender\">");
			terrierlog.println("      <!-- encoders are assigned the type");
			terrierlog.println("                    ch.qos.logback.classic.encoder.PatternLayoutEncoder by default -->");
			terrierlog.println("     <encoder>");
			terrierlog.println("          <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>");
			terrierlog.println("     </encoder>");
			terrierlog.println("  </appender>");
			if (debug)
				terrierlog.println("  <root level=\"debug\">");
			else
				terrierlog.println("  <root level=\"info\">");
			terrierlog.println("      <appender-ref ref=\"STDOUT\" />");
			terrierlog.println("  </root>");
			terrierlog.println("</configuration>");
			terrierlog.close();
			
			//creating the terrier.properties file
			PrintWriter propertiesWriter = new PrintWriter(new FileWriter(ETC_Dir+ "terrier.properties"));
			System.out.println("Creating terrier.properties file.");		
			propertiesWriter.println("#default controls for query expansion");
			propertiesWriter.println("querying.postprocesses.order=QueryExpansion");
			propertiesWriter.println("querying.postprocesses.controls=qe:QueryExpansion");
			propertiesWriter.println("#default controls for the web-based interface. SimpleDecorate");
			propertiesWriter.println("#is the simplest metadata decorator. For more control, see Decorate.");
			propertiesWriter.println("querying.postfilters.order=SimpleDecorate,SiteFilter,Scope");
			propertiesWriter.println("querying.postfilters.controls=decorate:SimpleDecorate,site:SiteFilter,scope:Scope");
			propertiesWriter.println();
			propertiesWriter.println("#default and allowed controls");
			propertiesWriter.println("querying.default.controls=");
			propertiesWriter.println("querying.allowed.controls=scope,qe,qemodel,start,end,site,scope");
			propertiesWriter.println();
			propertiesWriter.println("#document tags specification");
			propertiesWriter.println("#for processing the contents of");
			propertiesWriter.println("#the documents, ignoring DOCHDR");
			propertiesWriter.println("TrecDocTags.doctag=DOC");
			propertiesWriter.println("TrecDocTags.idtag=DOCNO");
			propertiesWriter.println("TrecDocTags.skip=DOCHDR");
			propertiesWriter.println("#set to true if the tags can be of various case");
			propertiesWriter.println("TrecDocTags.casesensitive=false");

			propertiesWriter.println();
			propertiesWriter.println("#query tags specification");
			propertiesWriter.println("TrecQueryTags.doctag=TOP");
			propertiesWriter.println("TrecQueryTags.idtag=NUM");
			propertiesWriter.println("TrecQueryTags.process=TOP,NUM,TITLE");
			propertiesWriter.println("TrecQueryTags.skip=DESC,NARR");
			propertiesWriter.println();
			propertiesWriter.println("#stop-words file");
			propertiesWriter.println("stopwords.filename=stopword-list.txt");
			propertiesWriter.println();
			propertiesWriter.println("#the processing stages a term goes through");
			propertiesWriter.println("termpipelines=Stopwords,PorterStemmer");
			propertiesWriter.println();
			
			propertiesWriter.close();
		} catch(IOException ioe) {
			System.err.println("Exception while creating the default configuration files for Terrier: "+ioe);
			System.err.println("Exiting ...");
			ioe.printStackTrace();	
		}
			
	}
}
