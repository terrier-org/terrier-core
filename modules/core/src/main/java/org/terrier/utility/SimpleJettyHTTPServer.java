/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
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
 * The Original Code is SimpleJettyHTTPServer.java
 *
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.utility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;
import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.terrier.applications.CLITool;
import org.terrier.applications.CLITool.CLIParsedCLITool;


/** Class to make a simple Jetty servlet. Two arguments: port name, and webapps root path.
 * <tt>share/images</tt> is automatically added as /images.
 * @author Craig Macdonald
 * @since 3.0
 */
public class SimpleJettyHTTPServer {

	protected Server webserver;	
	
	/** Create a new server, bound to the specified IP address (optional), the specified port,
	 * and serving from the specified directory
	 * @param bindAddress - interface to bind to. Will bind to all addresses if null.
	 * @param port - port to run the Jetty server on.
	 * @param webappRoot - path location to run the webapps folder
	 * @throws IOException if problem in binding
	 */
	public SimpleJettyHTTPServer(String bindAddress, int port, String webappRoot) throws IOException 
	{
		webserver = new Server();

		ServerConnector connector= new ServerConnector(webserver);
        connector.setPort(port);
        if (bindAddress != null)
        	connector.setHost(bindAddress);
       
        webserver.setConnectors(new Connector[]{connector});
        
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/");
        webAppContext.setResourceBase(webappRoot);
        
        // see http://bengreen.eu/fancyhtml/quickreference/jettyjsp9error.html        
        webAppContext.setAttribute("javax.servlet.context.tempdir", getScratchDir());
        webAppContext.setAttribute(
				"org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
				".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/[^/]*taglibs.*\\.jar$");
		
		/*
		 * Configure the application to support the compilation of JSP files.
		 * We need a new class loader and some stuff so that Jetty can call the
		 * onStartup() methods as required.
		 */
        webAppContext.setAttribute("org.eclipse.jetty.containerInitializers", jspInitializers());
        webAppContext.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
        webAppContext.addBean(new ServletContainerInitializersStarter(webAppContext), true);
        webAppContext.setClassLoader(ApplicationSetup.clzLoader);
		
        
        ResourceHandler imageHandler = new ResourceHandler();
        imageHandler.setResourceBase(ApplicationSetup.TERRIER_SHARE + "/images/");
        ContextHandler imageContext = new ContextHandler();
        imageContext.setContextPath("/images");
        imageContext.setHandler(imageHandler);
        
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[]{webAppContext,imageContext});
        
        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[]{contexts,new DefaultHandler()});
        
       
        webserver.setHandler(handlers);
       
	}
	
	// see http://bengreen.eu/fancyhtml/quickreference/jettyjsp9error.html
	private static File getScratchDir() throws IOException {
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		File scratchDir = new File(tempDir.toString(), "embedded-jetty-jsp");

		if (!scratchDir.exists()) {
			if (!scratchDir.mkdirs()) {
				throw new IOException("Unable to create scratch directory: " + scratchDir);
			}
		}
		return scratchDir;
	}

	// see http://bengreen.eu/fancyhtml/quickreference/jettyjsp9error.html
	private static List<ContainerInitializer> jspInitializers() {
		JettyJasperInitializer sci = new JettyJasperInitializer();
		ContainerInitializer initializer = new ContainerInitializer(sci, null);
		List<ContainerInitializer> initializers = new ArrayList<ContainerInitializer>();
		initializers.add(initializer);
		return initializers;
	}

	/**
	 * start webserver
	 * @throws Exception
	 */
	public void start() throws Exception {
		webserver.start();
	}
	
	/**
	 * stop webserver
	 */
	public void stop() throws Exception {
	    webserver.stop();
	}
	
	public static class Command extends CLIParsedCLITool
	{

		@Override
		public String commandname() {
			return "http";
		}

		@Override
		public String help() {
			return "Usage: SimpleJettyHTTPServer port src/webapps/simple/";
		}

		@Override
		public String helpsummary() {
			return "runs a simple JSP webserver, to serve results";
		}

		@Override
		public int run(CommandLine line) throws Exception {
			String[] args = line.getArgs();
			if (args.length != 2)
			{
				System.err.println(help());
				return 1;
			}
			new SimpleJettyHTTPServer(null, Integer.parseInt(args[0]), args[1]).start();
			return 0;
		}

		@Override
		public String sourcepackage() {
			return CLITool.PLATFORM_MODULE;
		}
		
	}
	/**
	 * main
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		CLITool.run(Command.class, args);		
	}
	
}
