package org.terrier.matching.models.dependence;

import org.terrier.matching.models.WeightingModel;
import org.terrier.statistics.GammaFunction;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.NgramEntryStatistics;

public class pBiL extends WeightingModel {

	private static final long serialVersionUID = 1L;
	protected static final double REC_LOG_2 = 1.0d / Math.log(2.0d);
	protected static final GammaFunction gf = GammaFunction.getGammaFunction();
	
	boolean norm2 = false;
	int ngramLength;
	
	public pBiL() {}
	
	
	public pBiL(int _ngramLength) {
		this.ngramLength = _ngramLength;
	}
	
	@Override
	public String getInfo() {
		return this.getClass().getSimpleName();
	}

	@Override
	public void setEntryStatistics(EntryStatistics _es) {
		super.setEntryStatistics(_es);
		ngramLength = ((NgramEntryStatistics)_es).getWindowSize();
	}


	@Override
	public double score(double matchingNGrams, double docLength) {
		
		if (matchingNGrams == 0)
			return 0.0d;
		if (matchingNGrams == docLength)
			matchingNGrams = docLength - 0.1d;
		
		final double numberOfNGrams = (docLength > 0 && docLength < ngramLength) ? 1
				: docLength - ngramLength + 1.0d;
		
		double score = 0.0d;
		
		// apply Norm2 to pf?
		//System.err.println("C="+ ngramC + " windows="+ numberOfNGrams + " avgDocLen="+ avgDocLen + " gf="+gf.getClass().getSimpleName());
		final double matchingNGramsNormalised = norm2 ? ((double)matchingNGrams)
				* Math.log(1.0d + super.c * averageDocumentLength / numberOfNGrams)
				* REC_LOG_2 : matchingNGrams;
		
		final double background = norm2 ? averageDocumentLength : numberOfNGrams;
		final double p = 1.0D / background;
		final double q = 1.0d - p;
		//System.err.println("background="+background + " p="+p + " q="+q);
		score = 
			- gf.compute_log(background + 1.0d) * REC_LOG_2
			+ gf.compute_log(matchingNGramsNormalised + 1.0d) * REC_LOG_2
			+ gf.compute_log(background - matchingNGramsNormalised+ 1.0d)* REC_LOG_2
			- matchingNGramsNormalised * Math.log(p) * REC_LOG_2
			- (background - matchingNGramsNormalised) * Math.log(q) * REC_LOG_2;
		score = score / (1.0d + matchingNGramsNormalised);
		return score;
	}

}
