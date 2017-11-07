package org.terrier.utility;

import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.LogOptions;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultExcludeRule;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.ExcludeRule;
import org.apache.ivy.core.module.id.ArtifactId;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.matcher.GlobPatternMatcher;
import org.apache.ivy.plugins.repository.file.FileRepository;
import org.apache.ivy.plugins.resolver.ChainResolver;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.apache.ivy.plugins.resolver.FileSystemResolver;
import org.apache.ivy.plugins.resolver.IBiblioResolver;
import org.terrier.utility.ApplicationSetup.TerrierApplicationPlugin;



/** Resolves Maven dependencies specified in <tt>terrier.ivy.coords</tt> 
 * and adds to classpath.
 * <p><b>Properties</b>
 * <ul><li><tt>terrier.ivy.coords</tt> - SBT-like expression of dependency. 
 * 	E.g. <tt>com.harium.database:sqlite:1.0.5</tt></li>
 * <ul>
 * @since 5.0
 */
public class IvyResolver implements TerrierApplicationPlugin {
	
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
	
	File m2Path = new File(System.getProperty("user.home"), ".m2" + File.separator + "repository");
			//: File {
//			if (Utils.isTesting) {
//			// test builds delete the maven cache, and this can cause flakiness
//			new File("dummy", ".m2" + File.separator + "repository")
//			} else {
//			new 
//			}
//			}

	PrintStream printStream = System.err;
	
	//TODO fix, is this needed?
//	List<String> IVY_DEFAULT_EXCLUDES = Arrays.asList(/*"catalyst_", "core_", "graphx_", "launcher_", "mllib_",
//			"mllib-local_", "network-common_", "network-shuffle_", "repl_", "sketch_", "sql_", "streaming_",
//			"tags_", "unsafe_"*/);
	
	@Override
	public void initialise() throws Exception {
		String requestedCoords = ApplicationSetup.getProperty("terrier.ivy.coords", null);
		if (requestedCoords == null)
			return;
		//prevent more than one thread initing concurrently
		synchronized (lock) {
			if (initCoords != null && initCoords.equals(requestedCoords))
				return;
			this.initialise(requestedCoords);
			initCoords = requestedCoords;
		}	
	}
	
	public void initialise(String coordinates) throws Exception {
		IvySettings ivySettings = buildIvySettings(null, null);
		List<String> classpath = resolveMavenCoordinates(coordinates, ivySettings, null, false);
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Collection<URL> newJars = classpath.stream().map( f -> {
			try{
				File fi = new File(f);
				assert (fi.exists());
				return fi.toURI().toURL();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}}).collect(Collectors.toList());
		System.out.println(newJars);
		ClassLoader newCl = new MutableURLClassLoader(cl, newJars);
		Thread.currentThread().setContextClassLoader(newCl);
	}
	
	DefaultModuleDescriptor getModuleDescriptor() {
		return DefaultModuleDescriptor.newDefaultInstance(
			ModuleRevisionId.newInstance("org.terrier", "terrier-core", ApplicationSetup.TERRIER_VERSION));
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
	
	/**
	* Resolves any dependencies that were supplied through maven coordinates
	* @param coordinates Comma-delimited string of maven coordinates
	* @param ivySettings An IvySettings containing resolvers to use
	* @param exclusions Exclusions to apply when resolving transitive dependencies
	* @return The comma-delimited path to the jars of the given maven artifacts including their
	* transitive dependencies
	*/
	@SuppressWarnings({ "rawtypes", "unchecked" })//this is Ivy's fault. getReports() returns unsafe List
	List<String> resolveMavenCoordinates(
		String coordinates,
		IvySettings ivySettings,
		String[] exclusions,//could be null or empty
		boolean isTest
		) 
	{
		//System.out.println("coordinates="+coordinates);
		if (coordinates == null || coordinates.trim().length() == 0)
			return Arrays.asList();

		PrintStream sysOut = System.out;
		List<String> rtr = Arrays.asList();
		try {
			// To prevent ivy from logging to system out
			System.setOut(printStream);
			List<MavenCoordinate> artifacts = extractMavenCoordinates(coordinates);
			//System.out.println("artfiacts="+artifacts);
			// Directories for caching downloads through ivy and storing the jars when maven coordinates
			// are supplied to spark-submit
			File packagesDirectory  = new File(ivySettings.getDefaultIvyUserDir(), "jars");
			// scalastyle:off println
			printStream.println(
					"Ivy Default Cache set to: "+ivySettings.getDefaultCache().getAbsolutePath());
			printStream.println(
					"The jars for the packages stored in: " + packagesDirectory);
			// scalastyle:on println
			Ivy ivy = Ivy.newInstance(ivySettings);
			// Set resolve options to download transitive dependencies as well
			ResolveOptions resolveOptions = new ResolveOptions();
			resolveOptions.setTransitive(true);
			RetrieveOptions retrieveOptions = new RetrieveOptions();
			// Turn downloading and logging off for testing
			if (isTest) {
				resolveOptions.setDownload(false);
				resolveOptions.setLog(LogOptions.LOG_QUIET);
				retrieveOptions.setLog(LogOptions.LOG_QUIET);
			} else {
				resolveOptions.setDownload(true);
			}
			// Default configuration name for ivy
			String ivyConfName = "default";
			// A Module descriptor must be specified. Entries are dummy strings
			DefaultModuleDescriptor md = getModuleDescriptor();
			// clear ivy resolution from previous launches. The resolution file is usually at
			// ~/.ivy2/org.apache.spark-spark-submit-parent-default.xml. In between runs, this file
			// leads to confusion with Ivy when the files can no longer be found at the repository
			// declared in that file/
			ModuleRevisionId mdId = md.getModuleRevisionId();
			File previousResolution = new File(ivySettings.getDefaultCache(),
					mdId.getOrganisation() + "-" + mdId.getName() + "-" + ivyConfName + ".xml");
			//		s"${mdId.getOrganisation}-${mdId.getName}-$ivyConfName.xml")
			if (previousResolution.exists()) 
				previousResolution.delete();
			md.setDefaultConf(ivyConfName);
			// Add exclusion rules for Spark and Scala Library
			addExclusionRules(ivySettings, ivyConfName, md);
			// add all supplied maven artifacts as dependencies
			addDependenciesToIvy(md, artifacts, ivyConfName);
			if (exclusions != null)
				for(String e : exclusions)
					md.addExcludeRule(createExclusion(e + ":*", ivySettings, ivyConfName));
			
			// resolve dependencies
			ResolveReport rr  = ivy.resolve(md, resolveOptions);
			if (rr.hasError()) {
				throw new RuntimeException(rr.getAllProblemMessages().toString());
			}
			// retrieve all resolved dependencies
			ivy.retrieve(rr.getModuleDescriptor().getModuleRevisionId(),
					packagesDirectory.getAbsolutePath() + File.separator +
				"[organization]_[artifact]-[revision].[ext]",
				retrieveOptions.setConfs(new String[]{ivyConfName}));
			
			List x = rr.getArtifacts();
			Artifact[] y = (Artifact[]) x.toArray(new Artifact[x.size()]);
			//System.out.println(Arrays.toString(y));
			rtr = resolveDependencyPaths( y, packagesDirectory);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			System.setOut(sysOut);
		}
		return rtr;
	}
	
	/**
	* Output a comma-delimited list of paths for the downloaded jars to be added to the classpath
	* (will append to jars in SparkSubmit).
	* @param artifacts Sequence of dependencies that were resolved and retrieved
	* @param cacheDirectory directory where jars are cached
	* @return a comma-delimited list of paths for the dependencies
	*/
	List<String> resolveDependencyPaths(
			Artifact[] artifacts,
			File cacheDirectory)
	{
		return Arrays.asList(artifacts).stream().map( artifactInfo -> {
			ModuleRevisionId artifact = artifactInfo.getModuleRevisionId();
			return cacheDirectory.getAbsolutePath() + File.separator +
				artifact.getOrganisation() + "_" + artifact.getName() + "-" + artifact.getRevision() + ".jar";
		}).collect(Collectors.toList());
	}
	
	void addDependenciesToIvy(
			DefaultModuleDescriptor md,
			List<MavenCoordinate> artifacts,
			String ivyConfName)
	{
		artifacts.stream().forEach(mvn -> {
			ModuleRevisionId ri = ModuleRevisionId.newInstance(mvn.groupId, mvn.artifactId, mvn.version);
			DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(ri, false, false);
			dd.addDependencyConfiguration(ivyConfName, ivyConfName + "(runtime)");
			// scalastyle:off println
			printStream.println(dd.getDependencyId() + " added as a dependency");
			// scalastyle:on println
			md.addDependency(dd);
			});
	}
	
	/** Add exclusion rules for dependencies already included in the spark-assembly */
	void addExclusionRules(
			IvySettings ivySettings,
			String ivyConfName,
			DefaultModuleDescriptor md)
	{
		// Add scala exclusion rule
		md.addExcludeRule(createExclusion("*:scala-library:*", ivySettings, ivyConfName));
		md.addExcludeRule(createExclusion("org.terrier:terrier-core:*", ivySettings, ivyConfName));
//		IVY_DEFAULT_EXCLUDES.stream().forEach( comp ->
//			{
//				md.addExcludeRule(createExclusion("org.apache.spark:spark-"+comp+"*:*", ivySettings, ivyConfName));
//			}
//		);
	}
	
	ExcludeRule createExclusion(
			String coords,
			IvySettings ivySettings,
			String ivyConfName)
	{
		MavenCoordinate c = extractMavenCoordinates(coords).get(0);
		ArtifactId id = new ArtifactId(new ModuleId(c.groupId, c.artifactId), "*", "*", "*");
		DefaultExcludeRule rule = new DefaultExcludeRule(id, ivySettings.getMatcher("glob"), null);
		rule.addConfiguration(ivyConfName);
		return rule;
	}

	/**
	* Build Ivy Settings using options with default resolvers
	* @param remoteRepos Comma-delimited string of remote repositories other than maven central
	* @param ivyPath The path to the local ivy repository
	* @return An IvySettings object
	*/
	IvySettings buildIvySettings(String remoteRepos, String ivyPath) {
		IvySettings ivySettings = new IvySettings();
		processIvyPathArg(ivySettings, ivyPath);
		// create a pattern matcher
		ivySettings.addMatcher(new GlobPatternMatcher());
		// create the dependency resolvers
		DependencyResolver repoResolver = createRepoResolvers(ivySettings.getDefaultIvyUserDir());
		ivySettings.addResolver(repoResolver);
		ivySettings.setDefaultResolver(repoResolver.getName());
		processRemoteRepoArg(ivySettings, remoteRepos);
		return ivySettings;
	}
	
	void processRemoteRepoArg(IvySettings ivySettings, String remoteRepos) {
		if (remoteRepos == null || (remoteRepos = remoteRepos.trim()).isEmpty())
			return;
		ChainResolver cr = new ChainResolver();
		cr.setName("user-list");
		// add current default resolver, if any
		if (ivySettings.getDefaultResolver() != null)
		{
			cr.add(ivySettings.getDefaultResolver());
		}
		String[] repositoryList = remoteRepos.split(",");
		for(int i=0;i<repositoryList.length;i++)
		{
			String repo = repositoryList[i];
			// add additional repositories, last resolution in chain takes precedence
			IBiblioResolver brr = new IBiblioResolver();
			brr.setM2compatible(true);
			brr.setUsepoms(true);
			brr.setRoot(repo);
			brr.setName(String.format("repo-%d", i+1));
			cr.add(brr);
			//printStream.println(s"$repo added as a remote repository with the name: ${brr.getName}")

		}
		ivySettings.addResolver(cr);
		ivySettings.setDefaultResolver(cr.getName());
	}
	
	void processIvyPathArg(IvySettings ivySettings, String ivyPath) {
		if (ivyPath == null)
			return;
		if ( (ivyPath = ivyPath.trim()).isEmpty())
			return;
		ivySettings.setDefaultIvyUserDir(new File(ivyPath));
		ivySettings.setDefaultCache(new File(ivyPath, "cache"));
	}
	
	DependencyResolver createRepoResolvers(File defaultIvyUserDir)
	{
		ChainResolver cr = new ChainResolver();
		cr.setName("spark-list");
		IBiblioResolver localM2 = new IBiblioResolver();
		localM2.setM2compatible(true);
		localM2.setRoot(m2Path.toURI().toString());
		localM2.setUsepoms(true);
		localM2.setName("local-m2-cache");
		cr.add(localM2);
		FileSystemResolver localIvy = new FileSystemResolver();
		File localIvyRoot = new File(defaultIvyUserDir, "local");
		localIvy.setLocal(true);
		localIvy.setRepository(new FileRepository(localIvyRoot));
		String ivyPattern = ArrayUtils.join( new String[]{localIvyRoot.getAbsolutePath(), "[organisation]", "[module]", "[revision]",
				"ivys", "ivy.xml"}, File.separator);
		localIvy.addIvyPattern(ivyPattern);
		String artifactPattern = ArrayUtils.join( new String[]{localIvyRoot.getAbsolutePath(), "[organisation]", "[module]",
				"[revision]", "[type]s", "[artifact](-[classifier]).[ext]"},File.separator);
		localIvy.addArtifactPattern(artifactPattern);
		localIvy.setName("local-ivy-cache");
		cr.add(localIvy);
		// the biblio resolver resolves POM declared dependencies
		IBiblioResolver br = new IBiblioResolver();
		br.setM2compatible(true);
		br.setUsepoms(true);
		br.setName("central");
		cr.add(br);
//		IBiblioResolver sp = new IBiblioResolver();
//		sp.setM2compatible(true);
//		sp.setUsepoms(true);
//		sp.setRoot("http://dl.bintray.com/spark-packages/maven");
//		sp.setName("spark-packages");
//		cr.add(sp);
		return cr;
	}

}
