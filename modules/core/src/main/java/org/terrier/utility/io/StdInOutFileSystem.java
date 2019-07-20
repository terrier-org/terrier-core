package org.terrier.utility.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.terrier.utility.Files;

public class StdInOutFileSystem implements FileSystem {

	public static final String SCHEME = "stdinout";
	
	@Override
	public String name() {
		return this.getClass().getSimpleName();
	}

	@Override
	public byte capabilities() {
		return Files.FSCapability.READ | Files.FSCapability.WRITE;
	}

	@Override
	public String[] schemes() {
		return new String[]{SCHEME};
	}

	@Override
	public boolean exists(String filename) throws IOException {
		return "-".equals(filename);
	}

	@Override
	public boolean canRead(String filename) throws IOException {
		return "-".equals(filename);
	}

	@Override
	public boolean canWrite(String filename) throws IOException {
		return "-".equals(filename);
	}

	@Override
	public InputStream openFileStream(String filename) throws IOException {
		if (filename.equals("-"))
			return System.in; 
		throw new FileNotFoundException(filename +" not compatible with this FS");
	}

	@Override
	public RandomDataInput openFileRandom(String filename) throws IOException {
		throw new FileNotFoundException(filename +" not compatible with this FS");
	}

	@Override
	public OutputStream writeFileStream(String filename) throws IOException {
		if (filename.equals("-"))
			return System.out; 
		throw new FileNotFoundException(filename +" not compatible with this FS");
	}

	@Override
	public RandomDataOutput writeFileRandom(String filename) throws IOException {
		throw new FileNotFoundException(filename +" not compatible with this FS");
	}

	@Override
	public boolean delete(String filename) throws IOException {
		return false;
	}

	@Override
	public boolean deleteOnExit(String pathname) throws IOException {
		return false;
	}

	@Override
	public boolean mkdir(String filename) throws IOException {
		return false;
	}

	@Override
	public long length(String filename) throws IOException {
		return 0;
	}

	@Override
	public boolean isDirectory(String path) throws IOException {
		return false;
	}

	@Override
	public boolean rename(String source, String destination) throws IOException {
		return false;
	}

	@Override
	public String getParent(String path) throws IOException {
		return null;
	}

	@Override
	public String[] list(String path) throws IOException {
		return new String[0];
	}

}
