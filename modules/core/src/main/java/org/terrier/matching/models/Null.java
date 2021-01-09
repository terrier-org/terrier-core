package org.terrier.matching.models;

/** A weighting model that returns 0 for each match. */
public class Null extends WeightingModel {
	private static final long serialVersionUID = 1L;

	@Override
	public String getInfo() {
		return this.getClass().getSimpleName();
	}

	@Override
	public double score(double tf, double docLength) {
		return keyFrequency * 0d;
	}

}
