package org.terrier.matching;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.matching.dsms.DocumentScoreModifier;
import org.terrier.matching.models.WeightingModel;
import org.terrier.structures.Index;
import org.terrier.utility.ApplicationSetup;

public abstract class AbstractScoringMatching extends FilterMatching {

	/** the default namespace for the document score modifiers that are specified in the properties
	 * file. */
	protected static String dsmNamespace = "org.terrier.matching.dsms.";
	
	static final Logger logger = LoggerFactory.getLogger(AbstractScoringMatching.class);

	
	public Index index;
	protected WeightingModel wm;
	public boolean sort = true;	
	protected Predicate<Pair<String,Set<String>>> filterTerm = null;
	/** Contains the document score modifiers to be applied for a query. */
	protected List<DocumentScoreModifier> documentModifiers = new ArrayList<DocumentScoreModifier>();

	
	public AbstractScoringMatching(Index _index, Matching _parent, WeightingModel _wm, Predicate<Pair<String,Set<String>>> _filter)
	{
		this(_index, _parent, _wm);
		this.filterTerm = _filter;
	}
	
	public AbstractScoringMatching(Index _index, Matching _parent, WeightingModel _wm)
	{
		super(_parent);
		this.wm = _wm;
		this.index =_index;
		String c = ApplicationSetup.getProperty("fat.scoring.matching.model.c", null);
		if (c != null)
			this.wm.setParameter(Double.parseDouble(c));
		String defaultDSMS =  ApplicationSetup.getProperty("fat.scoring.matching.dsms", ApplicationSetup.getProperty("matching.dsms",""));
		
		try {
			for(String modifierName : defaultDSMS.split("\\s*,\\s*")) {
				if (modifierName.length() == 0)
                    continue;
				if (modifierName.indexOf('.') == -1)
					modifierName = dsmNamespace + modifierName;
				documentModifiers.add(ApplicationSetup.getClass(modifierName).asSubclass(DocumentScoreModifier.class).newInstance());
			}
		} catch(Exception e) {
			logger.error("Exception while initialising default modifiers. Please check the name of the modifiers in the configuration file.", e);
		}
	}

}
