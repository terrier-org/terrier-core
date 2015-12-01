/*
 * Terrier - Terabyte Retriever
 * Webpage: http://terrier.org
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.ac.uk
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
 * The Original Code is FileFind.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.applications;
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
/** 
 * Takes a list of paths and filenames on the 
 * command line, and finds all the files in
 * those directories, displaying only absolute 
 * filenames on Standard Output. Used by trec_setup.bat 
 * (on Windows) as Windows doesn't have a find file 
 * equivalent like find in Unix.<br/>
 * <pre>java org.terrier.applications.FileFind c:\</pre>
 * @author Craig Macdonald
 */
public class FileFind {
		
	/**
	 * Take 1 directory from the front of dirList, and add all files and
	 * directories it finds in that directory. Recursively calls itself.
	 * @param fileList List of files found so far.
	 * @param dirList List of directories remaining to be processed
	 */
	public static void findFiles(List<String> fileList, LinkedList<String> dirList)
	{
		if (dirList.size() == 0)
			return;
		File dir = new File(dirList.removeFirst());
		if (dir.exists())
		{
			File[] contents = dir.listFiles();
			if (contents != null)
			{
				for(int i=0;i<contents.length;i++)
				{
					if (contents[i].isDirectory())
						dirList.add(contents[i].getAbsolutePath());
					else if (contents[i].exists() && contents[i].canRead())
						fileList.add(contents[i].getAbsolutePath());
				}
			}
		}
		findFiles(fileList, dirList);
	}
	/** 
	 * Takes a list of command line parameters of filenames 
	 * and directories. Finds all files and directories in 
	 * those, and displays all files on Standard Output as
	 * as absolute paths.
	 * @param args command line arguments - list of directories
	 */
	public static void main(String[] args)
	{
		LinkedList<String> fileList = new LinkedList<String>();
		LinkedList<String> dirList = new LinkedList<String>();
		for(int i=0;i<args.length;i++)
		{
			File F = new File(args[i]);
			if (F.isDirectory())
			{
				dirList.add(F.getAbsolutePath());
			}
			else if (F.exists() && F.canRead())
			{
				fileList.add(F.getAbsolutePath());
			}
		}
		findFiles(fileList, dirList);
		Collections.sort(fileList);
		for(String file : fileList)
		{
			System.out.println(file);	
		}
		//System.exit(fileList.size() > 0 ? 0 : 1);
	}
}
