package org.terrier.matching.models.dependence;

public class pBiL2 extends pBiL {
	private static final long serialVersionUID = 1L;

	public pBiL2(int _ngramLength){
		super(_ngramLength);
		super.norm2 = true;
	}
	
	@Override
	public String getInfo() {
		return this.getClass().getSimpleName() + "c" + super.c;
	}

}
