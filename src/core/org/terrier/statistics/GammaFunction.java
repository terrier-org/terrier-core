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
 * The Original Code is GammaFunction.java.
 *
 * The Original Code is Copyright (C) 2010-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.statistics;

import java.io.Serializable;

import org.terrier.utility.ApplicationSetup;
/** Base class for implementations of the Gamma function.
 * Use getGammaFunction() to obtain an instance. The exact
 * instance can be controlled by property <tt>gamma.function</tt>
 * 
 * For consistency when computing logarithms of the gamma function, all
 * implementations assume positive input values. In practice, as the
 * gamma function can generate large values, normal usage should use
 * compute_log() anyway.
 * 
 * <p><b>Properties:</b></p>
 * &lt;ul&gt;
 * <li><tt>gamma.function</tt> - class name of the Gamma function implementation.
 * Defaults to use WikipediaLanczosGammaFunction 
 * @since 3.0
 * @author Craig Macdonald */
public abstract class GammaFunction implements Serializable {
	static final boolean DEBUG = false;
	
	static class DebugGammaFunction extends GammaFunction
	{

		private static final long serialVersionUID = -4278880773160450823L;
		
		GammaFunction p;
		public DebugGammaFunction(GammaFunction _p)
		{
			p = _p;
		}

		public double compute(double number)
		{
			double rtr = p.compute(number);
			System.out.println(p.getClass().getSimpleName()+".compute("+number+")="+rtr);
			return rtr;
		}

		public double compute_log(double number)
        {
            double rtr = p.compute_log(number);
            System.out.println(p.getClass().getSimpleName()+".compute_log("+number+")="+rtr);
            return rtr;
        }
	}

	private static final long serialVersionUID = 1L;

	/** Get the value of the gamma function for the specified number.
	 * @param number for which is required
	 * @return (n-1)!
	 */
	public abstract double compute(double number);
	/** Get the value of the log of gamma function for the specified number.
	 * @param number for which is required
	 * @return log(n-1)!
	 */
	public abstract double compute_log(double number);
	
	/** Obtain an instance of GammaFunction */
	public static final GammaFunction getGammaFunction()
	{
		String className = ApplicationSetup.getProperty("gamma.function", WikipediaLanczosGammaFunction.class.getName());
		try{
			Class<? extends GammaFunction> clz = Class.forName(className).asSubclass(GammaFunction.class);
			return DEBUG ? new DebugGammaFunction(clz.newInstance()) : clz.newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static void main(String[] args)
	{
		System.out.println(getGammaFunction().compute_log(Double.parseDouble(args[0])));
	}
	
	/** Compute factorial of n, for 0 &lt; n &lt 21.
	 * @param n number to compute for
	 * @return factorial of n
	 */
	public static final long factorial(long n) {
        if      (n <  0) throw new RuntimeException("Underflow error in factorial");
        else if (n > 20) throw new RuntimeException("Overflow error in factorial");
        else if (n == 0) return 1;
        else             return n * factorial(n-1);
    }
	
	/** This implementation of the Lanczos approximation of the Gamma function
	 * is described on the Wikipedia page: 
	 * <a href="http://en.wikipedia.org/wiki/Lanczos_approximation">http://en.wikipedia.org/wiki/Lanczos_approximation</a>
	 * @author Transcribed from Python by Craig Macdonald
	 * @since 3.0
	 */
	static class WikipediaLanczosGammaFunction extends GammaFunction {		
		private static final long serialVersionUID = 1129349228998597260L;
		final static int g = 7;
		final static double p[] = new double[]{ 0.99999999999980993, 676.5203681218851, -1259.1392167224028,
			771.32342877765313, -176.61502916214059, 12.507343278686905,
			-0.13857109526572012, 9.9843695780195716e-6, 1.5056327351493116e-7};
		
		@Override
		public double compute_log(double z) {
			//Reflection formula
			if (z < 0.5d) {
		        return Math.log(Math.PI) - Math.log(Math.sin(Math.PI*z)) - compute_log(1.0d-z);
			}
		    else {
		        z -= 1.0d;
		        double x = p[0];
		        for(int i=1;i<g+2;i++)
		        	x += p[i]/(z+(double)i);
		        double t = z + (double)g + 0.5;
		        return Math.log(Math.sqrt(2*Math.PI)) + (z+0.5) * Math.log(t) -t + Math.log(x);
		    }
		}

		@Override
		public double compute(double z) {
			return Math.exp(compute_log(z));
		}
	}

}
