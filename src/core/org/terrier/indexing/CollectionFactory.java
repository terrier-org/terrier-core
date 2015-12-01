
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
 * The Original Code is CollectionFactory.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald <craigm{a.}dcs.gla.ac.uk
 */

package org.terrier.indexing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.indexing.Collection;
import org.terrier.utility.ApplicationSetup;

/** 
  * Implements a factory for Collection objects. Pass the name of the desired 
  * Collection(s) to loadCollection() or loadCollections(). loadCollection can take
  * a comma separated list of collections, while loadCollections takes a String array
  * of Collection names, in the same order. The package name <tt>org.terrier.indexing.</tt>
  * is prepended to any Collections named without any explicit packages.
  * <p>
  * The bottom Collection is specified last, and this is loaded first. Ie Collections
  * are loaded right to left. 
  * For instance: <tt>PassageCollection,TRECCollection</tt> would instantiate a PassageCollection
  * which has as its only constructor parameter, a TREC Collection. Ie, the correponding code would
  * be <tt>new PassageCollection(new TRECCollection());</tt>
  * If the optional constructor parameters types and values are specified, then these will be used
  * to instantiate the innermost class.
  * @author  Craig Macdonald craigm{a.}dcs.gla.ac.uk
  * @since 1.1.0, 14/01/2007
  */
public class CollectionFactory
{
	/** logger for this class */
	protected static final Logger logger = LoggerFactory.getLogger(CollectionFactory.class);

	/** Use the default property <tt>trec.collection.class</tt>, or it's default value TRECCollection */
	public static Collection loadCollections()
	{
		return loadCollection(ApplicationSetup.getProperty("trec.collection.class", "TRECCollection"));
	}

	/** Load collection(s) of the specified name. Uses empty constructor of innermost Collection. Returns null on error */
	public static Collection loadCollection(String CollectionName)
	{
		return loadCollections( CollectionName.split("\\s*,\\s*") );
	}
	/** Load collection(s) of the specified name. Types and values of the construcor as specified. Returns null on error */
	public static Collection loadCollection(String CollectionName, Class<?>[] contructorTypes, Object[] constructorValues)
	{
		return loadCollections( CollectionName.split("\\s*,\\s*"), contructorTypes, constructorValues);
	}
	
	/** Load collection(s) of the specified name. Types and values of the construcor as specified. */
	public static Collection loadCollections(final String[] collNames, Class<?>[] contructorTypes, Object[] constructorValues)
		{
		final int collCount = collNames.length;
		String constructor = "requested";
		if (contructorTypes == null)
		{
			contructorTypes = new Class[0];
			constructor = "default";
		}
		if (constructorValues == null)
			constructorValues = new Object[0];

		/* first load the innermost collection, ie the one that does the reading */	
		Collection rtr = null;
		final String firstCollectionName = normaliseCollectionName(collNames[collCount-1]);
		try{
			Class<? extends Collection> collectionClass = Class.forName(firstCollectionName).asSubclass(Collection.class);
			rtr = collectionClass.getConstructor(contructorTypes).newInstance(constructorValues); //collectionClass.newInstance();
		} catch (ClassNotFoundException e) {
			logger.error("ERROR: First Collection class named "+ firstCollectionName + " not found", e);
			return null;
		} catch (NoSuchMethodException e) {
			logger.error("ERROR: First Collection class named "+ firstCollectionName + " - "+constructor+" constructor not found", e);
		} catch (Exception e) {
			logger.error("ERROR: First Collection class named "+ firstCollectionName + "  - cannot be instantiated", e);
            return null;
        }

		/* now load any wrapper collections requested */
		int i = collCount-2;
		if (collCount>1)
		try{
			for(;i>=0;i--)
			{
				Collection newColl = Class.forName(normaliseCollectionName(collNames[i]))
					.asSubclass(Collection.class)
					.getConstructor(new Class[]{Collection.class})
         	       .newInstance(new Object[]{rtr});	
				rtr = newColl;
			}
        } catch (Exception e) {
            logger.error("ERROR: Subsequent Collection class named "+ collNames[i] + " not found", e);
			rtr = null;
        }
		/* return the outermost collection */
		return rtr;
		
	}

	/** Load collection(s) of the specified name. Uses default constructor of innermost Collection. Returns null on error */
	public static Collection loadCollections(final String[] collNames)
	{
		return loadCollections(collNames, null, null);
	}

	/** prepends <tt>org.terrier.indexing.</tt> to any Collections without a package */
	protected static String normaliseCollectionName(String collectionName)
	{
		if (collectionName.indexOf('.') == -1)
            collectionName = "org.terrier.indexing."+collectionName;
		else if (collectionName.startsWith("uk.ac.gla.terrier"))
			collectionName = collectionName.replaceAll("uk.ac.gla.terrier", "org.terrier");
		return collectionName;
	}
}
