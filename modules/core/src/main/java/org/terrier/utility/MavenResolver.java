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
 * The Original Code is AetherResolver.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.utility;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.terrier.utility.ApplicationSetup.TerrierApplicationPlugin;

/**
 * Resolves Maven dependencies specified in <tt>terrier.mvn.coords</tt> and adds
 * to classpath.
 * <p>
 * <b>Properties</b>
 * <ul>
 * <li><tt>terrier.mvn.coords</tt> - SBT-like expression of dependency. E.g.
 * <tt>com.harium.database:sqlite:1.0.5</tt></li>
 * </ul>
 * 
 * @since 5.0
 */
public class MavenResolver implements TerrierApplicationPlugin {

	public static final Set<String> PROVIDED_MODULES = new HashSet<>(Arrays.asList(
			"terrier-core", "terrier-concurrent", "terrier-retrieval-api",
			"terrier-rest-client", "terrier-rest-server",
			"terrier-batch-indexers", "terrier-batch-retrieval",
			"terrier-learning", "terrier-tests", "terrier-logging",
			"terrier-integer-compression", "terrier-website-search"));

	// TODO consider using the aether client directly, rather than the jcabi
	// wrapper, which imports other classes.
	// see
	// https://github.com/liferay/liferay-blade-cli/blob/28556e7e8560dd27d4a5153cb93196ca059ac081/com.liferay.blade.cli/src/com/liferay/blade/cli/aether/AetherClient.java

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

		public boolean isLoaded(String clz) {
			return super.findLoadedClass(clz) != null;
		}
	}


	private static final String USER_HOME = System.getProperty("user.home");
	private static final File USER_MAVEN_CONFIGURATION_HOME = new File(
			USER_HOME, ".m2");

	@Override
	public void initialise() throws Exception {
		String requestedCoords = ApplicationSetup.getProperty(
				"terrier.mvn.coords",
				ApplicationSetup.getProperty("terrier.ivy.coords", null));
		if (requestedCoords == null)
			return;
		if (requestedCoords.equals(ApplicationSetup.getProperty(
				"terrier.ivy.coords", null))) {
			System.err
					.println("WARNING to CRAIG: stop relying on terrier.ivy.coords");
		}
		// prevent more than one thread initing concurrently
		synchronized (lock) {
			if (initCoords != null && initCoords.equals(requestedCoords))
				return;
			this.initialise(requestedCoords);
			initCoords = requestedCoords;
		}
	}

	public void initialise(String coordinates) throws Exception {
		// File local = new File("/tmp/local-repository");
		
//		Collection<RemoteRepository> remotes = Arrays
//				.asList(new RemoteRepository("maven-central", "default",
//						"http://repo1.maven.org/maven2/"));

		RepositorySystem system = newRepositorySystem();
		RepositorySystemSession session = newRepositorySystemSession(system);

		List<Artifact> deps = extractMavenCoordinates(coordinates);
		Collection<File> foundDepFiles = new ArrayList<>();
		DependencyFilter df = new DependencyFilter() {

			@Override
			public boolean accept(DependencyNode node,
					List<DependencyNode> parents) {

				// we don't download ourself
				if (node.getDependency().getArtifact().getGroupId()
						.equals("org.terrier")
						&& PROVIDED_MODULES.contains(node.getDependency()
								.getArtifact().getArtifactId()))
					return false;

				// also, dont mess up scala
				if (node.getDependency().getArtifact().getArtifactId()
						.equals("scala-library"))
					return false;

				// finally, don't include any dependencies that will be provided
				// by virtue of Terrier
				// in doing so, we check that if any parent of a dependency is
				// in PROVIDED_MODULES
				if (parents.stream().anyMatch(
						d -> d.getDependency().getArtifact().getGroupId()
								.equals("org.terrier")
								&& PROVIDED_MODULES.contains(d.getDependency()
										.getArtifact().getArtifactId()))) {
					// System.err.println("ignoring: " +node +
					// " as parents include Terrier - parents="+
					// parents.toString());
					return false;
				}
				// System.err.println("adding: " +node + " parents="+
				// parents.toString());

				return true;
			}

		};
		
		DependencyFilter classpathFlter = DependencyFilterUtils.classpathFilter( JavaScopes.COMPILE );
		final List<RemoteRepository> repos = newRepositories( system, session );
		
		for (Artifact art : deps) {
			
			//first, resolve the artifact
			ArtifactRequest ar = new ArtifactRequest();
			ar.setArtifact( art );
			ar.setRepositories( repos );
	        ArtifactResult artifactResult = system.resolveArtifact(session, ar);
	        if (! artifactResult.isResolved())
	        	throw new RuntimeException("Could not resolve " + art.toString());
	        foundDepFiles.add(artifactResult.getArtifact().getFile());
	        
	        //then get its dependencies
			CollectRequest collectRequest = new CollectRequest();
	        collectRequest.setRoot( new Dependency( art, "runtime" ) );
	        collectRequest.setRepositories( repos );
	        DependencyRequest dependencyRequest = new DependencyRequest( collectRequest, DependencyFilterUtils.andFilter(classpathFlter, df) );
			List<ArtifactResult> artifactResults = system.resolveDependencies( session, dependencyRequest ).getArtifactResults();
			foundDepFiles.addAll(artifactResults.stream().map( r -> r.getArtifact().getFile()).collect(Collectors.toList()) );
		}

		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Collection<URL> newJars = foundDepFiles.stream().map(fi -> {
			try {
				assert (fi.exists());
				return fi.toURI().toURL();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}).collect(Collectors.toList());

		if (!newJars.isEmpty())
			System.out.println("Enhancing classpath with " + newJars);
		ClassLoader newCl = new MutableURLClassLoader(cl, newJars);
		// new
		// Exception("not an exception: CL replaced here").printStackTrace();
		Thread.currentThread().setContextClassLoader(newCl);
	}

	// replacement for scala require implicit
	final void require(boolean condition, String reason) {
		if (condition)
			return;
		throw new RuntimeException(reason);
	}

	/**
	 * Extracts maven coordinates from a comma-delimited string. Coordinates
	 * should be provided in the format `groupId:artifactId:version` or
	 * `groupId/artifactId:version`.
	 * 
	 * @param coordinates
	 *            Comma-delimited string of maven coordinates
	 * @return Sequence of Maven coordinates
	 */
	List<Artifact> extractMavenCoordinates(String coordinates) {
		return Arrays
				.asList(coordinates.split(","))
				.stream()
				.map(p -> {
					String[] splits = p.replace("/", ":").split(":");
					require(splits.length == 3,
							"Provided Maven Coordinates must be in the form "
									+ "'groupId:artifactId:version'. The coordinate provided is: "
									+ p);
					require(splits[0] != null && splits[0].trim().length() > 0,
							"The groupId cannot be null or "
									+ "be whitespace. The groupId provided is: "
									+ splits[0]);
					require(splits[1] != null && splits[1].trim().length() > 0,
							"The artifactId cannot be null or "
									+ "be whitespace. The artifactId provided is: "
									+ splits[1]);
					require(splits[2] != null && splits[2].trim().length() > 0,
							"The version cannot be null or "
									+ "be whitespace. The version provided is: "
									+ splits[2]);
					return new DefaultArtifact(p); //new MavenCoordinate(splits[0], splits[1], splits[2]);
				}).collect(Collectors.toList());
	}
	
	 public static DefaultRepositorySystemSession newRepositorySystemSession( RepositorySystem system )
	    {
		 File local = new File(USER_MAVEN_CONFIGURATION_HOME, "repository");
	        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

	        LocalRepository localRepo = new LocalRepository(local);
	        session.setLocalRepositoryManager( system.newLocalRepositoryManager( session, localRepo ) );

//	        session.setTransferListener( new ConsoleTransferListener() );
//	        session.setRepositoryListener( new ConsoleRepositoryListener() );

	        // uncomment to generate dirty trees
	        // session.setDependencyGraphTransformer( null );

	        return session;
	    }
	
	public static RepositorySystem newRepositorySystem()
    {
        /*
         * Aether's components implement org.eclipse.aether.spi.locator.Service to ease manual wiring and using the
         * prepopulated DefaultServiceLocator, we only need to register the repository connector and transporter
         * factories.
         */
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService( RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class );
        locator.addService( TransporterFactory.class, FileTransporterFactory.class );
        locator.addService( TransporterFactory.class, HttpTransporterFactory.class );

        locator.setErrorHandler( new DefaultServiceLocator.ErrorHandler()
        {
            @Override
            public void serviceCreationFailed( Class<?> type, Class<?> impl, Throwable exception )
            {
                exception.printStackTrace();
            }
        } );

        return locator.getService( RepositorySystem.class );
    }
	
	public static List<RemoteRepository> newRepositories( RepositorySystem system, RepositorySystemSession session )
    {
        return new ArrayList<RemoteRepository>( Arrays.asList( 
        		newCentralRepository()) );
    }

    private static RemoteRepository newCentralRepository()
    {
        return new RemoteRepository.Builder( "central", "default", "https://repo.maven.apache.org/maven2/" ).build();
    }
    
    public static void main(String[] args) throws Exception {
    	new MavenResolver().initialise(ArrayUtils.join(args, ","));
    }
}
