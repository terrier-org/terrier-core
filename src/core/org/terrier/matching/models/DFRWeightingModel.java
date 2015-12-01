/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org 
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.ac.uk
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
 * The Original Code is WeightingModel.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Ben He <ben{a.}dcs.gla.ac.uk> 
 */
package org.terrier.matching.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.matching.models.aftereffect.AfterEffect;
import org.terrier.matching.models.basicmodel.BasicModel;
import org.terrier.matching.models.normalisation.Normalisation;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.EntryStatistics;

/**
 * This class implements a modular Divergence from Randomness weighting model. 
 * Components in the model are specified individually, with many implementations
 * provided: <a href="basicmodel/package-summary.html">org.terrier.matching.models.basicmodel</a>;
 * <a href="aftereffect/package-summary.html">org.terrier.matching.models.aftereffect</a>;
 * and <a href="normalisation/package-summary.html">org.terrier.matching.models.normalisation</a>.
 * A class name for each component should be specific in parenthesis after DFRWeightingModel.
 * Moreover, besides the basic model, the other two components can be either specified or disabled.
 * <p>
 * <b>Examples</b>:
 * <ul>
 * <li><tt>DFRWeightingModel(P,L,2)</tt> is equivalent to <tt>{@link PL2}</tt></li>
 * <li><tt>DFRWeightingModel(P,L)</tt> removes the term frequency normalisastion component from  <tt>{@link PL2}</tt></li>
 * </ul>
 * @author Ben He
 */
public class DFRWeightingModel extends WeightingModel {

	private static final long serialVersionUID = 1L;

	protected static final Logger logger = LoggerFactory.getLogger(DFRWeightingModel.class);
	/** The applied basic model for randomness. */
	protected BasicModel basicModel;
	/** The applied model for after effect (aka. first normalisation). */
	protected AfterEffect afterEffect;
	/** The applied frequency normalisation method. */
	protected Normalisation normalisation;
	/** The prefix of the package of the frequency normalisation methods. */
	protected final String NORMALISATION_PREFIX = "org.terrier.matching.models.normalisation.Normalisation";
	/** The prefix of the package of the basic models for randomness. */
	protected final String BASICMODEL_PREFIX = "org.terrier.matching.models.basicmodel.";
	/** The prefix of the package of the first normalisation methods by after effect. */
	protected final String AFTEREFFECT_PREFIX = "org.terrier.matching.models.aftereffect.";
	/** The parameter of the frequency normalisation component. */
	protected double parameter;
	/** A boolean that indicates if the frequency normalisation is enabled. */
	protected boolean ENABLE_NORMALISATION;
	/** A boolean that indicates if the first normalisation by after effect
	 * is enabled. */ 
	protected boolean ENABLE_AFTEREFFECT;
	/**
	 * The default constructor. Takes an array of strings to define the 
	 * Basic Model, the After Effect component and the Normalisation component.
	 * If the array is less than 3 items in length, then empty strings will be passed
	 * instead of the After Effect and/or Normalisation components.
	 * @param components Corresponds to the names of the 3 DFR weighting models component
	 * names, as passed to initialise().
	 */
	public DFRWeightingModel (String[] components) {
		this.initialise(
			components[0].trim(), 
			components.length > 1 ? components[1].trim() : "",
			components.length > 2 ? components[2].trim() : "");
	}
	
	
	
	@Override
	public DFRWeightingModel clone() {
		DFRWeightingModel rtr = (DFRWeightingModel) super.clone();
		rtr.basicModel = (BasicModel) this.basicModel.clone();
		rtr.afterEffect = (AfterEffect) this.afterEffect.clone();
		rtr.normalisation = (Normalisation) this.normalisation.clone();
		return rtr;
	}



	/**
	 * Initialise the components in the DFR model. For each component, if a package
	 * is not specified, then a prefix will be applied. These are BASICMODEL_PREFIX,
	 * AFTEREFFECT_PREFIX and NORMALISATION_PREFIX respectively. Note that NORMALISATION_PREFIX
	 * includes a partial class name.
	 * @param basicModelName The name of the applied basic model for randomness. This
	 * component must be specified and can NOT be an empty string.
	 * @param afterEffectName The name of the applied first normalisation by after
	 * effect. An empty string to disable this component.
	 * @param normalisationName The name of the applied frequency normalisation
	 * component. An empty string to disable this component.
	 */
	protected void initialise(String basicModelName, 
			String afterEffectName,
			String normalisationName
			){

		try{

			// --------- BASIC MODEL --------------------------------
			// initialise the basic model
			if (basicModelName.indexOf('.') < 0)
				basicModelName = this.BASICMODEL_PREFIX.concat(basicModelName);
			else if (basicModelName.startsWith("uk.ac.gla.terrier"))
				basicModelName = basicModelName.replaceAll("uk.ac.gla.terrier", "org.terrier");
			
			this.basicModel = (BasicModel)Class.forName(basicModelName.trim()).newInstance();
			/*if(logger.isInfoEnabled()){
			logger.info("basicModelName: " + basicModelName);
			}*/
			// ------------------------------------------------------


			// --------- AFTER EFFECT--------------------------------
			// check to see if we're using an after effect component
			if (afterEffectName.length() == 0){
				//afterEffectName = ApplicationSetup.getProperty("default.after.effect", "LL").trim();
				//dont use the after effect, but still load one in case it is used
				this.ENABLE_AFTEREFFECT = false;
			}
			else{
				this.ENABLE_AFTEREFFECT = true;
			}
			// initialise the after effect component
			if (afterEffectName.indexOf('.') < 0)
				afterEffectName = this.AFTEREFFECT_PREFIX.concat(afterEffectName);
			else if (afterEffectName.startsWith("uk.ac.gla.terrier"))
				afterEffectName = basicModelName.replaceAll("uk.ac.gla.terrier", "org.terrier");
			
			if (ENABLE_AFTEREFFECT)
				this.afterEffect = (AfterEffect)Class.forName(afterEffectName.trim()).newInstance();
			// ------------------------------------------------------


			// --------- NORMALISATION -----------------------------
			// check to see if we're using a frequency normalisation component
			if (normalisationName.length() == 0){
				//normalisationName = ApplicationSetup.getProperty("default.normalisation", "2").trim();
				//dont use the normalisation, but still load one in case it is used
				this.ENABLE_NORMALISATION = false;
				normalisationName = "0";
			}
			else{
				this.ENABLE_NORMALISATION = true;
				// initialise the frequency normalisation component
			}
			if (normalisationName.indexOf('.') < 0)
				normalisationName = this.NORMALISATION_PREFIX.concat(normalisationName);
			else if (normalisationName.startsWith("uk.ac.gla.terrier"))
				normalisationName = normalisationName.replaceAll("uk.ac.gla.terrier", "org.terrier");
			
			this.normalisation = (Normalisation)Class.forName(normalisationName.trim()).newInstance();
			// ------------------------------------------------------

		}
		catch(Exception e){
			logger.error("Error occured while initialising the DFR model.",e);
		}
	}
	/**
	 * Initialise the components in the DFR model. For each component, if a package
	 * is not specified, then a prefix will be applied. These are BASICMODEL_PREFIX,
	 * AFTEREFFECT_PREFIX and NORMALISATION_PREFIX respectively. Note that NORMALISATION_PREFIX
	 * includes a partial class name.
	 * @param basicModelName The name of the applied basic model for randomness. This
	 * component must be specified and can NOT be an empty string.
	 * @param afterEffectName The name of the applied first normalisation by after
	 * effect. An empty string to disable this component.
	 * @param normalisationName The name of the applied frequency normalisation
	 * component. An empty string to disable this component.
	 * @param _parameter The applied parameter value of the frequency normalisation.
	 */
	protected void initialise(String basicModelName, 
			String afterEffectName,
			String normalisationName,
			double _parameter){
		this.initialise(basicModelName.trim(), afterEffectName.trim(), normalisationName.trim());
		//	set parameter
		this.setParameter(_parameter);
	}
	
	/**
	 * Set the frequency normalisation parameter.
	 * @param value The given parameter value.
	 */
	public void setParameter(double value){
		this.parameter = value;
		this.normalisation.setParameter(parameter);
		if (this.ENABLE_AFTEREFFECT)
			this.afterEffect.setParameter(parameter);
	}

	/** Return the parameter set by setParameter()
	  * @return parameter double value */
	public double getParameter(){
		return this.parameter;
	}
	
	
	/**
	 * Returns the name of the model.
	 * @return The name of the model.
	 */
	public final String getInfo() {
		String modelName = this.basicModel.getInfo();
		if (ENABLE_AFTEREFFECT)
			modelName += this.afterEffect.getInfo();
		if (this.ENABLE_NORMALISATION)
			modelName += this.normalisation.getInfo();
		return modelName;
	}

	/**
	 * Compute a weight for a term in a document.
	 * @param tf The term frequency in the document
	 * @param docLength the document's length
	 * @return the score assigned to a document with the given 
	 *         tf and docLength, and other preset parameters
	 */
	public final double score(double tf, double docLength) {
		double tfn = tf;
		// if the frequency normalisation is enabled, do the normalisation.
		if (this.ENABLE_NORMALISATION)
			tfn = normalisation.normalise(tf, docLength, termFrequency); 
		double gain = 1;
		// if the first normalisation by after effect is enabled, compute the gain.
		if (this.ENABLE_AFTEREFFECT)
			gain = afterEffect.gain(tfn, documentFrequency, termFrequency);
		// produce the final score.
		return  gain * 
				basicModel.score(tfn, 
						documentFrequency, 
						termFrequency,
						keyFrequency,
						docLength);
	}
	
	
	@Override
	public void setCollectionStatistics(CollectionStatistics _cs) {
		super.setCollectionStatistics(_cs);
		this.basicModel.setNumberOfDocuments(_cs.getNumberOfDocuments());
		this.basicModel.setNumberOfTokens(_cs.getNumberOfTokens());
		this.afterEffect.setAverageDocumentLength(_cs.getAverageDocumentLength());
		this.normalisation.setNumberOfDocuments(_cs.getNumberOfDocuments());
		this.normalisation.setNumberOfTokens(_cs.getNumberOfTokens());
		this.normalisation.setAverageDocumentLength(_cs.getAverageDocumentLength());
		this.i.setNumberOfDocuments(_cs.getNumberOfDocuments());
	}

	/** 
	 * {@inheritDoc} 
	 */
	@Override
	public void setEntryStatistics(EntryStatistics _es) {
		super.setEntryStatistics(_es);
		this.normalisation.setDocumentFrequency(_es.getDocumentFrequency());
	}

}
