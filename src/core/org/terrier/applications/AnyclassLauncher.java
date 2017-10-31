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
		try{
		thisMethod.invoke(null, (Object) Arrays.copyOfRange(args, 1, args.length));
		} catch (InvocationTargetException ite) {
			if (ite.getCause() != null)
				throw ite.getCause();
			throw ite;
		}
	}
	
}
