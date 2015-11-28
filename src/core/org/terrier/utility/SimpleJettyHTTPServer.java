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
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.utility;

import java.io.IOException;

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
	/**
	 * main
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		if (args.length != 2)
		{
			System.err.println("Usage: SimpleJettyHTTPServer port src/webapps/simple/");
			return;
		}
		new SimpleJettyHTTPServer(null, Integer.parseInt(args[0]), args[1]).start();
	}
	
}
