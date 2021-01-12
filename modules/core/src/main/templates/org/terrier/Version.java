package org.terrier;
/** 
 * Maven automatically populates this with the version number, etc.
 * See http://stackoverflow.com/questions/2469922/generate-a-version-java-file-in-maven 
 * @since 4.1.
 */
public class Version {

	public static final String VERSION = "${project.version}";
	public static final String BUILD_DATE = "${timestamp}";
	public static final String BUILD_USER = "${user.name}";

	public static int getMajorVersion()
	{
		return Integer.parseInt(VERSION.split("\\.", 2)[0]);
	}

	public static void main(String[] args) {
		System.out.println(VERSION);
	}
}
