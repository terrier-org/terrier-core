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
 * The Original Code is KillHandler.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Rodrygo Santos <rodrygo{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.utility;

import java.util.HashMap;
import java.util.Map;

/** Utility class for when releasing resources when the runtime system is shutting down.
 * @since 3.0 */
public class KillHandler extends Thread {
	static final Runtime runtime = Runtime.getRuntime();
	static final Map<Killable, KillHandler> enabledHandlers = new HashMap<Killable, KillHandler>();
	
	/** Add a Killable objects to the shutdown list which are invoked when the runtime system
	 * is terminating. 
	 * @param object the object with the kill handler
	 */
	public static void addKillhandler(Killable object)
    {
		final KillHandler k = new KillHandler(object);
		enabledHandlers.put(object, k);
    	runtime.addShutdownHook(k);
    }
	
	/** Remove a Killable objects from the shutdown list which are invoked when the runtime system
	 * is terminating. 
	 * @param object the object with the kill handler
	 */
	public static void removeKillhandler(Killable object)
	{
		final KillHandler k = enabledHandlers.get(object);
		if (k == null)
			return;
		try{
			runtime.removeShutdownHook(k);
		} catch (IllegalStateException ise) {
			/* suppress, probably "Shutdown in progress" */
		}
		enabledHandlers.remove(object);
	}
	
	/** Interface denoting an object which can be killed by the KillHandler */
	public static interface Killable {
		/** Called when the JVM is shutting down */
		void kill();		
	}
	
	protected final Killable killable;
	/**
	 * constructor
	 * @param killable
	 */
	KillHandler(Killable _killable) {
		this.killable = _killable;
	}
	/** 
	 * {@inheritDoc} 
	 */
    public void run() {
    	killable.kill();
    }
    
    
}
