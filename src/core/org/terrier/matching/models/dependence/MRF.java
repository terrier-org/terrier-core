package org.terrier.matching.models.dependence;

import static org.terrier.matching.models.WeightingModelLibrary.log;

import org.terrier.matching.models.WeightingModel;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.NgramEntryStatistics;

public class MRF extends WeightingModel {

	private static final long serialVersionUID = 1L;
	int ngramLength;
	double defaultDf;
	double defaultCf;
	
	public MRF(){}

	public MRF(int _ngramLength) {
		this.ngramLength = _ngramLength;
	}
	
	@Override
	public void prepare() {
		super.prepare();
		//these statistics are as used by Ivory system, of which Don Metzler was one of the authors
		defaultDf = ((double) cs.getNumberOfDocuments())  / 100.0d;
		defaultCf = defaultDf * 2;
	}

	@Override
	public String getInfo() {
		return this.getClass().getSimpleName() + "_mu" + this.c;
	}
	
	@Override
	public void setEntryStatistics(EntryStatistics _es) {
		super.setEntryStatistics(_es);
		ngramLength = ((NgramEntryStatistics)_es).getWindowSize();
	}

	@Override
	public double score(double matchingNGrams, double _docLength) {
		final double mu = this.c;
		double docLength = (double)_docLength;
		double tf = (double)matchingNGrams;
		return (log(1 + (tf/(mu * (defaultCf / super.numberOfTokens)))) + log(mu/(docLength+mu)));
	}

}
