package org.terrier.applications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/** Abstract class for all code that set up the batch indexers */
public abstract class BatchIndexing {

	/** The logger used */
	protected static Logger logger = LoggerFactory.getLogger(BatchIndexing.class);
	protected final String path;
	protected final String prefix;

	public BatchIndexing(String _path, String _prefix) {
		super();
		this.path = _path;
		this.prefix = _prefix;
	}

	public abstract void index();
	
}