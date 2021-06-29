
package org.terrier.matching.models;

public abstract class DFRNorm2BaseModel extends WeightingModel {
    /** The parameter c. This defaults to 1.0, but should be set using in the constructor
	  * of each child weighting model to the sensible default for that weighting model. */
	protected double c = 1.0d;

    @Override 
	public void prepare() {
		if (rq != null) {
			if (rq.hasControl("c")) {
				c = Double.parseDouble(rq.getControl("c")); 
			}
		}
		super.prepare();
	}

    /**
	 * Sets the c value
	 * @param _c the term frequency normalisation parameter value.
	 */
	public void setParameter(double _c) {
		this.c = _c;
	}

	/**
	 * Returns the parameter as set by setParameter()
	 */
	public double getParameter() {
		return this.c;
	}
}
