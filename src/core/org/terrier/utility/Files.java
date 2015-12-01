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
 * The Original Code is Files.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald craigm{a}dcs.gla.ac.uk (original author)
 */
package org.terrier.utility;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.sf.samtools.util.BlockCompressedInputStream;
import net.sf.samtools.util.BlockCompressedOutputStream;

import org.apache.hadoop.io.compress.BZip2Codec;
import org.terrier.utility.io.FileSystem;
import org.terrier.utility.io.HTTPFileSystem;
import org.terrier.utility.io.LocalFileSystem;
import org.terrier.utility.io.RandomDataInput;
import org.terrier.utility.io.RandomDataOutput;
import org.terrier.utility.io.WrappedIOException;

/** Utililty class for opening readers/writers and input/output streams to files. Handles gzipped and bzipped files
  * on the fly, ie if a file ends in ".gz" or ".GZ", then it will be opened using a GZipInputStream/GZipOutputStream. ".bz2" 
  * files are handled in a similar fashion. All returned Streams, Readers, Writers etc are Buffered. If a charset encoding is not specified, then the system default is used.
  * New interfaces are used to descibe random data access.
  * <br><br><b>FileSystem plugsin</b>
  * <p>Additional file systems can be plugged into this module, by calling the addFileSystemCapability() method. FileSystems have 
  * read and/or write capabilities, as specified using the FSCapability constants. Files using these external file systems should be denoted
  * by scheme prefixes - eg ftp://, http:// etc. NB: file:// is the default scheme
  * </p>
  * <br><br><b>Additional Compression Support</b>
  * <p>
  * Support for additional stream compression & decompression can be plugged in by calling addFilterInputStreamMapping().
  * </p>
  * <br><br><b>File Caching</b>
  * <p>Terrier can cache files which will see heavy IO activity. In particular, files mentioned in the <tt>files.to.cache</tt> property
  * will be cached to the default temporary folder. There are also API method to populate the cache with files. For all methods,
  * <tt>java.io.tmpdir</tt> is the default temporary directory. An IOException will occur if caching fails for some reason.
  */
public class Files
{
	/** constants declaring which capabilites a file system has */
	public interface FSCapability {
		/** FS can read files */
		/* byte 0 */ byte READ = 1;
		/** FS can write files */
		/* byte 1 */ byte WRITE = 2;
		/** FS can read file in a random fashion */
		/* byte 2 */ byte RANDOM_READ =4;
		/** FS can write to files in a random fashion */
		/* byte 3 */ byte RANDOM_WRITE = 8;
		/** FS can list the content of a directory */
		/* byte 4 */ byte LS_DIR = 16;
		/** FS can determine properties of a file or directory */
		/* byte 5 */ byte STAT = 32;
		/** FS can mark a file or directory to be deleted on exit */
		/* byte 6 */ byte DEL_ON_EXIT = 64;
		/* byte 7 */ 
	}
	
	static final Map<Pattern,Class<? extends InputStream>> inputStreamMap = new HashMap<Pattern,Class<? extends InputStream>>();
	static final Map<Pattern,Class<? extends OutputStream>> outputStreamMap = new HashMap<Pattern,Class<? extends OutputStream>>();
	
	static BZip2Codec bzipCodec = null;
	/** decompressing inputstream for .bz2 files. */
	static class BZip2InputStream extends FilterInputStream
	{		
		public BZip2InputStream(InputStream in) throws IOException {
			super((bzipCodec == null ? (bzipCodec = new BZip2Codec()) : bzipCodec).createInputStream(in));
		}		
	}
	
	/** compressing outputstream for .bz2 files. */
	static class BZip2OutputStream extends FilterOutputStream
	{		
		public BZip2OutputStream(OutputStream in) throws IOException {
			super((bzipCodec == null ? (bzipCodec = new BZip2Codec()) : bzipCodec).createOutputStream(in));
		}
	}
	
	/** Add a filter mapping to the Files layes. This is the method used to implement
	 * stream decompression. For example:
	 * <pre>
	 * addFilterInputStreamMapping(".+\\.gz$", GZIPInputStream.class, GZIPOutputStream.class);
	 * addFilterInputStreamMapping(".+\\.GZ$", GZIPInputStream.class, GZIPOutputStream.class);
	 * </pre>
	 * @param regex Regular expression that the filename must match to require the filter stream
	 * @param inputStreamClass Class extending InputStream that decompresses the file
	 * @param outputStreamClass Class extending OutputStream that compresses the file
	 */
	public static void addFilterInputStreamMapping(String regex, Class<? extends InputStream> inputStreamClass, Class<? extends OutputStream> outputStreamClass)
	{
		final Pattern p = Pattern.compile(regex);
		inputStreamMap.put(p, inputStreamClass);
		outputStreamMap.put(p, outputStreamClass);		
	}
	
	/** a search regex and a replacement for path transformations */
	protected static class PathTransformation {
		/** pattern for a transformation */
		protected Pattern matches;
		/** what the pattern should be replaced with */
		protected String replacement;
		/** create a new transformation with the pattern and replacement */
		public PathTransformation(String find, String replace)
		{
			matches = Pattern.compile(find);
			replacement = replace;
		}

		/** change a path if it matches this transformation */
		public String transform(String path)
		{
			Matcher m = matches.matcher(path);
			if (m.matches())
				return m.replaceAll(replacement);
			return path;
		}
	}

	/** map of scheme to FileSystem implementation */
	protected static final Map<String,FileSystem> fileSystems = new HashMap<String,FileSystem>();
	/** transformations to apply to a path */
	protected static final List<PathTransformation> pathTransformations = new LinkedList<PathTransformation>();
	/** default scheme */
	protected static final String DEFAULT_SCHEME = ApplicationSetup.getProperty("files.default.scheme", "file");

	static{ 
		addFileSystemCapability(new LocalFileSystem());
		addFileSystemCapability(new HTTPFileSystem());
		intialise_transformations();
		initialise_mappings();
		initialise_static_cache();		
	}
	
	/** we may have been specified some files to cache immediately */
	protected static void initialise_static_cache()
	{
		String[] filesToCache = ArrayUtils.parseCommaDelimitedString(ApplicationSetup.getProperty("files.to.cache", ""));
		for (String filename : filesToCache)
		{
			try{
				System.err.println("Caching file " + filename);
				cacheFile(filename);
			} catch (Exception e) {
				System.err.println("Could not cache file "+ filename +" because " + e);
				e.printStackTrace();
			}
		}
	}
	
	/** initialise the transformations from Application property */
	protected static void intialise_transformations()
	{
		final String[] transforms = ApplicationSetup.getProperty("files.transforms","").split("\\s*,\\s*");
		for(String transform: transforms)
		{
			if (transform.length() == 0)
				continue;
			String parts[] = transform.split("\\s*>\\s*");
			addPathTransormation(parts[0], parts[1]);
		}
	}
	
	/** initialise the default compression mappings */
	protected static void initialise_mappings()
	{
		addFilterInputStreamMapping(".+\\.gz$", GZIPInputStream.class, GZIPOutputStream.class);
		addFilterInputStreamMapping(".+\\.GZ$", GZIPInputStream.class, GZIPOutputStream.class);

		addFilterInputStreamMapping(".+\\.bz2", BZip2InputStream.class, BZip2OutputStream.class);
		addFilterInputStreamMapping(".+\\.BZ2$", BZip2InputStream.class, BZip2OutputStream.class);
		
		addFilterInputStreamMapping(".+\\.bgz", BlockCompressedInputStream.class, BlockCompressedOutputStream.class);
		addFilterInputStreamMapping(".+\\.BGZ$", BlockCompressedInputStream.class, BlockCompressedOutputStream.class);
		
		// new BlockCompressedInputStream(new File(filename));
	}
	
	/** Cache to the temporary directory specified by <tt>java.io.tmpdir</tt> System property. */
	public static void cacheFile(String filename) throws IOException
	{
		cacheFile(filename, System.getProperty("java.io.tmpdir"));
	}
	
	/** Cache file to specified temporary folder */
	public static void cacheFile(String filename, String temporaryFolder) throws IOException
	{
		String localFile = temporaryFolder + "/" + new File(filename).getName();
		Files.copyFile(filename, localFile);
		addPathTransormation(filename, localFile);
		new File(localFile).deleteOnExit();
	}

	/** add a static transformation to apply to a path. Find and replace are both regular expressions */
	public static void addPathTransormation(String find, String replace)
	{
		pathTransformations.add(new PathTransformation(find, replace));
	}
	
	/** Add a file system to Terrier. File systems are denoted by URI scheme prefixes (e.g. http). The underlying file system
	  * is represented by an FileSystem */
	public static void addFileSystemCapability(FileSystem fs)
	{
		for (String scheme : fs.schemes())
		{
			fileSystems.put(scheme, fs);
		}
	}

	/** apply any transformations to the specified filename */
	protected static String transform(String filename)
	{
		//apply the static transformations
		for (PathTransformation pt : pathTransformations)
			filename = pt.transform(filename);
		return filename;
	}

	/** derive the file system to use that is associated with the scheme in the specified filename.
	  * @param filename
	  */
	protected static FileSystem getFileSystem(String filename)
	{
		//check to see if filename is in a URI form
		if (! filename.matches("^\\w+:.*$"))
			return fileSystems.get(DEFAULT_SCHEME);
		//identify scheme component of filename
		final int colonPos = filename.indexOf(":"); 
		final String scheme = filename.substring(0, colonPos).toLowerCase();
		//obtain the filesystem representing the scheme
		FileSystem rtr = fileSystems.get(scheme);
		//if (rtr ==  null)
		//{	
			//throw new RuntimeException("FileSystem for "+filename +"(scheme '"+scheme+"') not found. Configured schemes are: "
			//		+ ArrayUtils.join(fileSystems.keySet().toArray(new String[0]), ", "));
		//}
		return rtr;
	}

	/** Get the name of the file system that would be used to access a given file or directory.
	  * @param path
	  * @return name Name of the file system, or null if no filesystem found
	  */
	public static String getFileSystemName(String path) 
	{
		path = transform(path);
		final FileSystem fs = getFileSystem(path);
		if (fs == null)
			return null;
		return fs.name();
	}

	/** Opens an OutputStream to a file called Filename, processing all
	 * allowed writable file systems named in writeFileSystemPrefixes
	 * @param filename Filename of file to open
	 * @throws IOException
	 */
	protected static InputStream openFile(String filename) throws IOException
	{
		filename = transform(filename);
		final FileSystem fs = getFileSystem(filename);
		if (fs == null)
			throw new FileNotFoundException("No file system for "+filename);
		if ((fs.capabilities() & FSCapability.READ) == 0)
			throw new IOException("File system not supporting reads for "+ filename);
				
		InputStream rtr = fs.openFileStream(filename);
		for(Pattern regex : inputStreamMap.keySet())
		{
			if (regex.matcher(filename).matches())
			{
				Class<? extends InputStream> filterClass = inputStreamMap.get(regex);
				try{
					rtr = filterClass.getConstructor(InputStream.class).newInstance(rtr);
				} catch (Exception e) {
					throw new WrappedIOException(e);
				}
			}
		}
		return rtr;
	}

	/** Opens an OutputStream to a file called filename, using the filesystem
	 * named in the scheme component of the filename.
	 * @param filename Filename of file to open, optionally including scheme
	 * @throws IOException
	 */
	protected static OutputStream writeFile(String filename) throws IOException
	{
		filename = transform(filename);
		final FileSystem fs = getFileSystem(filename);
		if (fs == null)
			throw new FileNotFoundException("No file system for "+filename);
		if ((fs.capabilities() & FSCapability.WRITE) == 0)
			throw new IOException("File system not supporting writes for "+ filename);
		OutputStream rtr = fs.writeFileStream(filename);
		for(Pattern regex : outputStreamMap.keySet())
		{
			if (regex.matcher(filename).matches())
			{
				Class<? extends OutputStream> filterClass = outputStreamMap.get(regex);				
				try{
					//System.err.println(filterClass.getName());
					if (filterClass.getName().endsWith("BlockCompressedOutputStream")) {
						rtr = filterClass.getConstructor(OutputStream.class, File.class).newInstance(rtr, null);
					} else rtr = filterClass.getConstructor(OutputStream.class).newInstance(rtr);
				} catch (Exception e) {
					throw new WrappedIOException(e);
				}
			}
		}
		return rtr;
	}

	/** Returns a RandomAccessFile implementation accessing the specificed file */
	public static RandomDataInput openFileRandom(String filename) throws IOException
	{
		filename = transform(filename);
		final FileSystem fs = getFileSystem(filename);
		if (fs == null)
			throw new FileNotFoundException("No file system for "+filename);
		if ((fs.capabilities() & FSCapability.RANDOM_READ) == 0)
			throw new IOException("File system not supporting random reads for "+ filename);
		return fs.openFileRandom(filename);
	}

	/** Returns a RandomAccessFile implementation accessing the specificed file */
	public static RandomDataOutput writeFileRandom(String filename) throws IOException
	{
		filename = transform(filename);
		final FileSystem fs = getFileSystem(filename);
		if (fs == null)
			throw new FileNotFoundException("No file system for "+filename);
		if ((fs.capabilities() & FSCapability.RANDOM_WRITE) == 0)
			throw new IOException("File system not supporting random writes for "+ filename);
		return fs.writeFileRandom(filename);
	}

	/** Delete the named file. Returns false if the scheme of filename cannot
	  * be recognised, the filesystem doesnt have write capability, or the underlying
	  * filesystem could not delete the file
	  * @param filename path to file to delete
	  */
	public static boolean delete(String filename)
	{
		filename = transform(filename);
		final FileSystem fs = getFileSystem(filename);
		if (fs == null)
			return false;
		if ((fs.capabilities() & FSCapability.WRITE) == 0)
			return false;
		try{
			return fs.delete(filename);
		} catch (IOException ioe) {
			return false;
		}
	}

	/** Mark the named path as to be deleted on exit. Returns false if the
	  * scheme of the filename cannot be recognised, the filesystem does not
	  * have write capability, or the file system does not have deleteOnExit
	  * capability */
	public static boolean deleteOnExit(String path)
	{
		path = transform(path);
        final FileSystem fs = getFileSystem(path);
        if (fs == null)
            return false;
		if ((fs.capabilities() & FSCapability.WRITE) == 0)
            return false;
		if ((fs.capabilities() & FSCapability.DEL_ON_EXIT) == 0)
			return false;	
		try{
            return fs.deleteOnExit(path);
        } catch (IOException ioe) {
            return false;
        }
	}

	/** returns true iff the path is really a path */
	public static boolean exists(String path)
	{
		path = transform(path);
		final FileSystem fs = getFileSystem(path);
		if (fs == null)
			return false;
		//System.err.printf("Cap: %d Stat: %d check: %d\n", fs.capabilities() , FSCapability.STAT, (fs.capabilities() & FSCapability.STAT));
		if ((fs.capabilities() & FSCapability.STAT) == 0)
			return true;
		try{
			return fs.exists(path);
		} catch (IOException ioe) {
			return false;
		}
	}

	/** returns true iff path can be read */
	public static boolean canRead(String filename)
	{
		filename = transform(filename);
		final FileSystem fs = getFileSystem(filename);
		if (fs == null)
			return false;
		if ((fs.capabilities() & (FSCapability.READ | FSCapability.STAT)) == 0)
			return true;
		try{
			return fs.canRead(filename);
		} catch (IOException ioe) {
			return false;
		}
	}

	/** returns true iff path can be read */
	public static boolean canWrite(String filename)
	{
		filename = transform(filename);
		final FileSystem fs = getFileSystem(filename);
		if (fs == null)
			return false;
		if ((fs.capabilities() & (FSCapability.WRITE | FSCapability.STAT )) == 0)
			return false;
		try{
			return fs.canWrite(filename);
		} catch (IOException ioe) {
			return false;
		}
	}

	/** returns true if the specificed path can be made as a directory */
	public static boolean mkdir(String path)
	{
		path = transform(path);
		final FileSystem fs = getFileSystem(path);
		if (fs == null)
			return false;
		if ((fs.capabilities() & FSCapability.WRITE) == 0)
			return false;
		try{
			return fs.mkdir(path);
		} catch (IOException ioe) {
			return false;
		}
	}

	/** returns the length of the file, or 0L if cannot be found etc */
	public static long length(String filename)
	{
		filename = transform(filename);
		final FileSystem fs = getFileSystem(filename);
		if (fs == null)
			return 0L;
		if ((fs.capabilities() & FSCapability.STAT) == 0)
			return 0L;
		try{
			return fs.length(filename);
		} catch (IOException ioe) {
			return 0L;
		}
	}

	/** return true if path is a directory */
	public static boolean isDirectory(String path)
	{
		path = transform(path);
        final FileSystem fs = getFileSystem(path);
        if (fs == null)
            return false;
        if ((fs.capabilities() & FSCapability.STAT) == 0)
            return false;
        try{
            return fs.isDirectory(path);
        } catch (IOException ioe) {
            return false;
        }
	}

	/** rename a file or directory. If the two are on different file systems, it is assumed to be a file */
	public static boolean rename(String sourceFilename, String destFilename)
	{
		sourceFilename = transform(sourceFilename);
		destFilename = transform(destFilename);
		final FileSystem destFS = getFileSystem(destFilename);
		final FileSystem sourceFS = getFileSystem(sourceFilename);
		try{
			if (destFS == sourceFS)//yes, that's object equals
			{
				return sourceFS.rename(sourceFilename, destFilename);
			}
			else
			{
				copyFile(sourceFS.openFileStream(sourceFilename), destFS.writeFileStream(destFilename));
				sourceFS.delete(sourceFilename);
				return true;
			}
		} catch (IOException ioe) {
			return false;
		}
	}

	/** What is the parent path to the specified path? */
	public static String getParent(String path)
	{
		path = transform(path);
        final FileSystem fs = getFileSystem(path);
        if (fs == null)
            return null;
        if ((fs.capabilities() & FSCapability.STAT) == 0)
            return null;
        try{
            return fs.getParent(path);
        } catch (IOException ioe) {
            return null;
        }	
	}
	
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	/** List the contents of a directory */
	public static String[] list(String path)
	{
		path = transform(path);
        final FileSystem fs = getFileSystem(path);
        if (fs == null)
            return EMPTY_STRING_ARRAY;
        if ((fs.capabilities() & FSCapability.LS_DIR) == 0)
            return EMPTY_STRING_ARRAY;
        try{
            return fs.list(path);
        } catch (IOException ioe) {
            return EMPTY_STRING_ARRAY;
        }

	}

	/* ------------------------------------------------------------------------------------------------- */
	// everything from here down is sytactic sugar for ensuring you always have the method your looking for
	// all file system independence magic is done above this point

	/** Opens a reader to the file called file. Provided for easy overriding for encoding support etc in 
	  * child classes. Called from openNextFile().
	  * @param file File to open.
	  * @return BufferedReader of the file
	  */
	public static BufferedReader openFileReader(File file) throws IOException
	{
		return openFileReader(file.getPath(),null);
	}

	/** Opens a reader to the file called filename. Provided for easy overriding for encoding support etc in
	  * child classes. Called from openNextFile().
	  * @param file File to open.
	  * @param charset Character set encoding of file. null for system default.
	  * @return BufferedReader of the file
	  */
	public static BufferedReader openFileReader(File file, String charset) throws IOException
	{
		return openFileReader(file.getPath(), charset);
	}

	/** Opens a reader to the file called filename. Provided for easy overriding for encoding support etc in
	  * child classes. Called from openNextFile().
	  * @param filename File to open.
	  * @return BufferedReader of the file
	  */
	public static BufferedReader openFileReader(String filename) throws IOException
	{
		return openFileReader(filename,null);
	}
	

	/** Opens a reader to the file called filename. Provided for easy overriding for encoding support etc in 
	  * child classes. Called from openNextFile().
	  * @param filename File to open.
	  * @param charset Character set encoding of file. null for system default.
	  * @return BufferedReader of the file
	  */
	public static BufferedReader openFileReader(String filename, String charset) throws IOException
	{
		return new BufferedReader(
				charset == null 
					? new InputStreamReader(openFile(filename))
					: new InputStreamReader(openFile(filename), charset)
				);
	}

	/** Opens an InputStream to a file called file. 
	  * @param file File to open.
	  * @return InputStream of the file
	  */
	public static InputStream openFileStream(File file) throws IOException
	{
		return openFileStream(file.getPath());
	}

	/** Open a file for random access reading */
	public static RandomDataInput openFileRandom(File file) throws IOException
	{
		return openFileRandom(file.getPath());
	}

	/** Opens an InputStream to a file called filename.
	  * @param filename File to open.
	  * @return InputStream of the file
	  */
	public static InputStream openFileStream(String filename) throws IOException
	{
		return new BufferedInputStream(openFile(filename));
	}

	/** Opens an OutputStream to a file called file.
	  * @param file File to open.
	  * @return OutputStream of the file
	  */
	public static OutputStream writeFileStream(File file) throws IOException
	{
		return writeFileStream(file.getPath());
	}

	/** Open a file for random access writing and reading */
	public static RandomDataOutput writeFileRandom(File file) throws IOException
	{
		return writeFileRandom(file.getPath());
	}

	/** Opens an OutputStream to a file called filename.
	  * @param filename File to open.
	  * @return OutputStream of the file
	  */
	public static OutputStream writeFileStream(String filename) throws IOException
	{
		return new BufferedOutputStream(writeFile(filename));
	}

	/** Opens an Writer to a file called file. System default encoding will be used.
	  * @param file File to open.
	  * @return Writer of the file
	  */
	public static Writer writeFileWriter(File file) throws IOException
	{
		return writeFileWriter(file.getPath(), null);
	}

	/** Opens an Writer to a file called file.
	  * @param file File to open.
	  * @param charset Character set encoding of file. null for system default.
	  * @return Writer of the file
	  */
	public static Writer writeFileWriter(File file, String charset) throws IOException
	{
		return writeFileWriter(file.getPath(), charset);
	}
	/** Opens an Writer to a file called file. System default encoding will be used.
	  * @param filename File to open.
	  * @return Writer of the file
	  */
	public static Writer writeFileWriter(String filename) throws IOException
	{
		return writeFileWriter(filename, null);
	}

	/** Opens an Writer to a file called file.
	  * @param filename File to open.
	  * @param charset Character set encoding of file. null for system default.
	  * @return Writer of the file
	  */
	public static Writer writeFileWriter(String filename, String charset) throws IOException
	{
		return new BufferedWriter(
				charset == null
					? new OutputStreamWriter(writeFile(filename))
					: new OutputStreamWriter(writeFile(filename), charset)
				);
	}

	//from: http://schmidt.devlib.org/java/copy-file.html
	/** buffer size for copying files */
	private static final int bufferSize = 4 * 1024;
	/** CRC32 the file while being copied, to allow
	  * integrity to be verified */
	private static final boolean verify = false;

	/** Copy a file from srcFile to destFile.
	  * @return null if OK
	  * @throws IOException if there was a problem copying */
	public static Long copyFile(String srcFilename, String destFilename)
		throws IOException {
		return copyFile(openFileStream(srcFilename), writeFileStream(destFilename));
	}
	/** Copy a file from srcFile to destFile.
	  * @return null if OK
	  * @throws IOException if there was a problem copying */	
	public static Long copyFile(File srcFile, File destFile)
		throws IOException {
		return copyFile(openFileStream(srcFile), writeFileStream(destFile));
	}

	/** Copy all bytes from in to out
 	  * @return null if OK
 	  * throws IOException if there was a problem copying */
	public static Long copyFile(InputStream in, OutputStream out) throws IOException
	{
		final CRC32 checksum = verify ? new CRC32() : null;
		if (verify) {
			checksum.reset();
		}
		byte[] buffer = new byte[bufferSize];
		int bytesRead;
		if (verify)
			while ((bytesRead = in.read(buffer)) >= 0) {
				checksum.update(buffer, 0, bytesRead);
				out.write(buffer, 0, bytesRead);
			}
		else
			while ((bytesRead = in.read(buffer)) >= 0)
				out.write(buffer, 0, bytesRead);	
		out.close();
		in.close();
		if (verify) {
			return new Long(checksum.getValue());
		} else {
			return null;
		}
	}


	/** Returns the CRC checksum of denoted file */
	public static Long createChecksum(File file) throws IOException {
		final InputStream in = openFileStream(file);
		final CRC32 checksum = new CRC32();
		checksum.reset();
		final byte[] buffer = new byte[bufferSize];
		int bytesRead;
		while ((bytesRead = in.read(buffer)) >= 0) {
			checksum.update(buffer, 0, bytesRead);
		}
		in.close();
		return Long.valueOf(checksum.getValue());
	}

	/** returns the length of file f */
	public static long length(File f)
	{
		return length(f.getPath());
	}
	
	/** Check that the a specified file exists as per Terrier's file system abstraction layer */
	public static void main(String args[])
	{
		System.out.println("Exists: " + args[0] + Files.exists(args[0]));
	}

}
