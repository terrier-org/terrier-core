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
 * The Original Code is WindowsFileOpener.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.applications.desktop.filehandling;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import org.terrier.utility.ApplicationSetup;
/**
 * This class implements the interface FileOpener for Windows, using native code
 * and JNI to call the method ShellExecuteEx.
 * 
 * @author Vassilis Plachouras
  */
public class WindowsFileOpener implements FileOpener {
	//loads the dll that contains the compiled native code
	static {
		loadLibrary();
	}
	/**
	 * Opens the file with the given name.
	 * 
	 * @param filename
	 *            the name of the file to open.
	 * @see org.terrier.applications.desktop.filehandling.FileOpener#open(java.lang.String)
	 */
	public native void open(String filename);
	/**
	 * Loads the windows native library from within a jar file.
	 * @return true if the native library has loaded
	 */
	private static boolean loadLibrary() {
		try {
			InputStream inputStream = WindowsFileOpener.class.getResource(
					"/winfileopen.dll").openStream();
			File temporaryDll = new File(ApplicationSetup.makeAbsolute("winfileopen.dll", ApplicationSetup.TERRIER_VAR));
			temporaryDll.deleteOnExit();
			
			FileOutputStream outputStream = new FileOutputStream(temporaryDll);
			byte[] array = new byte[17824];
			for (int i = inputStream.read(array); i != -1; i = inputStream.read(array)) {
				outputStream.write(array, 0, i);
			}
			outputStream.close();
			
			System.load(temporaryDll.getPath());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	/** Does nothing, not used */
	public void load(){}
	/** Does nothing, not used */
	public void save(){}
}
