package org.terrier.utility;

import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.terrier.utility.ApplicationSetup.TerrierApplicationPlugin;

import com.jcabi.aether.Aether;



/** Resolves Maven dependencies specified in <tt>terrier.ivy.coords</tt> 
 * and adds to classpath.
 * <p><b>Properties</b>
 * <ul><li><tt>terrier.ivy.coords</tt> - SBT-like expression of dependency. 
 * 	E.g. <tt>com.harium.database:sqlite:1.0.5</tt></li>
 * <ul>
 * @since 5.0
 */
public class AetherResolver implements TerrierApplicationPlugin {
	
	volatile static String initCoords = null;
	final static Object lock = new Object();
	
	public static class MutableURLClassLoader extends URLClassLoader {
	    public MutableURLClassLoader(ClassLoader parent, URL... urls) {
	        super(urls, parent);
	    }

	    public MutableURLClassLoader(ClassLoader parent, Collection<URL> urls) {
	        super(urls.toArray(new URL[urls.size()]), parent);
	    }
	    
	    @Override
	    public void addURL(URL url) {
	        super.addURL(url);
	    }

	    public void addURLs(Iterable<URL> urls) {
	        for (URL url : urls) {
	            addURL(url);
	        }
	    }
	}
	
	public static class MavenCoordinate {

		String artifactId;
		String groupId;
		String version;
		
		MavenCoordinate(String g, String a, String v)
		{
			groupId = g;
			artifactId = a;
			version = v;
		}
		
		@Override
		public String toString()
		{
			return groupId + ":" + artifactId + ":" + version;
		}
	}

	PrintStream printStream = System.err;
	
	
	@Override
	public void initialise() throws Exception {
		String requestedCoords = ApplicationSetup.getProperty("terrier.mvn.coords", ApplicationSetup.getProperty("terrier.ivy.coords", null));
		if (requestedCoords == null)
			return;
		if (requestedCoords.equals(ApplicationSetup.getProperty("terrier.ivy.coords", null)))
		{
			System.err.println("WARNING to CRAIG: stop relying on terrier.ivy.coords");
		}
		//prevent more than one thread initing concurrently
		synchronized (lock) {
			if (initCoords != null && initCoords.equals(requestedCoords))
				return;
			this.initialise(requestedCoords);
			initCoords = requestedCoords;
		}	
	}
	
	public void initialise(String coordinates) throws Exception {
		File local = new File("/tmp/local-repository");
		Collection<RemoteRepository> remotes = Arrays.asList(
			      new RemoteRepository(
			        "maven-central",
			        "default",
			        "http://repo1.maven.org/maven2/"
			      )
			    );
		Aether aether = new Aether(remotes, local);
		List<MavenCoordinate> deps = extractMavenCoordinates(coordinates);
		Collection<Artifact> foundDepFiles = new ArrayList<Artifact>();
		DependencyFilter df = new DependencyFilter(){

			@Override
			public boolean accept(DependencyNode node, List<DependencyNode> parents) {
				//we don't download ourself
				if (node.getDependency().getArtifact().getGroupId().equals("org.terrier") &&
						node.getDependency().getArtifact().getArtifactId().equals("terrier-core"))
					return false;
				//also, dont mess up scala
				if (node.getDependency().getArtifact().getArtifactId().equals("scala-library"))
					return false;
				return true;
			}
			
		};
		for(MavenCoordinate mvnc : deps)
		{
			foundDepFiles.addAll(aether.resolve(
					new DefaultArtifact(mvnc.groupId, mvnc.artifactId, "", "jar", mvnc.version ), "runtime", df));
		}
		
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Collection<URL> newJars = foundDepFiles.stream().map( f -> {
			try{
				File fi = f.getFile();
				assert (fi.exists());
				return fi.toURI().toURL();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}}).collect(Collectors.toList());
		System.out.println(newJars);
		ClassLoader newCl = new MutableURLClassLoader(cl, newJars);
		Thread.currentThread().setContextClassLoader(newCl);
	}
	
	//replacement for scala require implicit
	final void require(boolean condition, String reason)
	{
		if (condition)
			return;
		throw new RuntimeException(reason);
	}
	
	/**
	* Extracts maven coordinates from a comma-delimited string. Coordinates should be provided
	* in the format `groupId:artifactId:version` or `groupId/artifactId:version`.
	* @param coordinates Comma-delimited string of maven coordinates
	* @return Sequence of Maven coordinates
	*/
	List<MavenCoordinate> extractMavenCoordinates(String coordinates) {
	return Arrays.asList(coordinates.split(",")).stream().map(p -> {
			String[] splits = p.replace("/", ":").split(":");
			require(splits.length == 3, "Provided Maven Coordinates must be in the form " +
				"'groupId:artifactId:version'. The coordinate provided is: " + p);
			require(splits[0] != null && splits[0].trim().length() > 0, "The groupId cannot be null or " +
					"be whitespace. The groupId provided is: " + splits[0]);
			require(splits[1] != null && splits[1].trim().length() > 0, "The artifactId cannot be null or " +
					"be whitespace. The artifactId provided is: " + splits[1]);
			require(splits[2] != null && splits[2].trim().length() > 0, "The version cannot be null or " +
					"be whitespace. The version provided is: "+ splits[2]);
			return new MavenCoordinate(splits[0], splits[1], splits[2]);
		}
		).collect(Collectors.toList());
	}
}
