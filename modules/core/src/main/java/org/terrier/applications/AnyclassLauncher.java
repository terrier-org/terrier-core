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
 * The Original Code is AnyclassLauncher.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.applications;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.terrier.utility.ApplicationSetup;

public class AnyclassLauncher {

	@SuppressWarnings("unused")
	public static void main(String[] args) throws Throwable {
		//this forces the ivy classloader to be in place
		//org.terrier.utility.ApplicationSetup.bootstrapInitialisation();
		String x = ApplicationSetup.TERRIER_VERSION;
		Class<?> clz = Class.forName(args[0], true, Thread.currentThread().getContextClassLoader());
		Method thisMethod = clz.getDeclaredMethod("main",String[].class);
		assert clz.getClassLoader().equals(Thread.currentThread().getContextClassLoader());
		try{
		thisMethod.invoke(null, (Object) Arrays.copyOfRange(args, 1, args.length));
		} catch (InvocationTargetException ite) {
			if (ite.getCause() != null)
				throw ite.getCause();
			throw ite;
		}
	}
	
}
