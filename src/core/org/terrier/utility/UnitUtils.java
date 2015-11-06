package org.terrier.utility; 

import org.apache.commons.lang.StringUtils; 

public class UnitUtils 
{ 
	private final static long Ki_FACTOR = 1024; 
	private final static long Mi_FACTOR = 1024 * Ki_FACTOR; 
	private final static long Gi_FACTOR = 1024 * Mi_FACTOR; 

	private final static long K_FACTOR = 1000; 
	private final static long M_FACTOR = 1000 * K_FACTOR; 
	private final static long G_FACTOR = 1000 * M_FACTOR; 

	public static long parseLong(String str) 
	{ 
		if (str == null) 
			throw new NullPointerException(); 
		int notNumberIndex = StringUtils.indexOfAnyBut(str, "0123456789"); 
		if (notNumberIndex == -1)
			return Long.parseLong(str);
		long ret = Long.parseLong(str.substring(0, notNumberIndex)); 
		switch (str.substring(notNumberIndex).trim()) { 
		case "G": 
			return ret * G_FACTOR; 
		case "M": 
			return ret * M_FACTOR; 
		case "K": 
			return ret * K_FACTOR; 
		case "Gi": 
			return ret * Gi_FACTOR; 
		case "Mi": 
			return ret * Mi_FACTOR; 
		case "Ki": 
			return ret * Ki_FACTOR; 
		} 
		throw new NumberFormatException(str + " can't be correctly parsed."); 
	} 

	public static int parseInt(String str) 
	{ 
		if (str == null) 
			throw new NullPointerException(); 
		final int notNumberIndex = StringUtils.indexOfAnyBut(str, "0123456789");
		if (notNumberIndex == -1)
			return Integer.parseInt(str);
		int ret = Integer.parseInt(str.substring(0, notNumberIndex)); 
		switch (str.substring(notNumberIndex).trim()) { 
		case "G": 
			return (int) (ret * G_FACTOR); 
		case "M": 
			return (int) (ret * M_FACTOR); 
		case "K": 
			return (int) (ret * K_FACTOR); 
		case "Gi": 
			return (int) (ret * Gi_FACTOR); 
		case "Mi": 
			return (int) (ret * Mi_FACTOR); 
		case "Ki": 
			return (int) (ret * Ki_FACTOR); 
		} 
		throw new NumberFormatException(str + " can't be correctly parsed."); 
	} 

	public static float parseFloat(String str) 
	{ 
		if (str == null) 
			throw new NullPointerException(); 
		int notNumberIndex = StringUtils.indexOfAnyBut(str, "0123456789"); 
		if (notNumberIndex == -1)
			return Float.parseFloat(str);
		float ret = Float.parseFloat(str.substring(0, notNumberIndex)); 
		switch (str.substring(notNumberIndex).trim()) { 
		case "G": 
			return ret * G_FACTOR; 
		case "M": 
			return ret * M_FACTOR; 
		case "K": 
			return ret * K_FACTOR; 
		case "Gi": 
			return ret * Gi_FACTOR; 
		case "Mi": 
			return ret * Mi_FACTOR; 
		case "Ki": 
			return ret * Ki_FACTOR; 
		} 
		throw new NumberFormatException(str + " can't be correctly parsed."); 
	} 

	public static double parseDouble(String str) 
	{ 
		if (str == null) 
			throw new NullPointerException(); 
		int notNumberIndex = StringUtils.indexOfAnyBut(str, "0123456789"); 
		if (notNumberIndex == -1)
			return Double.parseDouble(str);
		double ret = Double.parseDouble(str.substring(0, notNumberIndex)); 
		switch (str.substring(notNumberIndex).trim()) { 
		case "G": 
			return ret * G_FACTOR; 
		case "M": 
			return ret * M_FACTOR; 
		case "K": 
			return ret * K_FACTOR; 
		case "Gi": 
			return ret * Gi_FACTOR; 
		case "Mi": 
			return ret * Mi_FACTOR; 
		case "Ki": 
			return ret * Ki_FACTOR; 
		} 
		throw new NumberFormatException(str + " can't be correctly parsed."); 
	} 

	public static void main(String args[]) 
	{ 
		UnitUtils.parseLong("1 K"); 
		UnitUtils.parseLong("1M"); 
	} 

} 