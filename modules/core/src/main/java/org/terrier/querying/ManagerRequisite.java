package org.terrier.querying;

/**
 * 
 * @since 5.0
 */
public enum ManagerRequisite {

	/** the original query must have been set  */
	RAWQUERY,
	/** a TerrierQL parsed query have been set */
	TERRIERQL,
	/** MatchingQueryTerms has been populated */
	MQT,
	/** A ResultSet has been obtained */
	RESULTSET
	
}
