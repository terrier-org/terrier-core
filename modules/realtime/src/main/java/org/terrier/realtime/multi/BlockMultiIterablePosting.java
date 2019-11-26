package org.terrier.realtime.multi;

import org.terrier.structures.postings.BlockPosting;
import org.terrier.structures.postings.IterablePosting;

public class BlockMultiIterablePosting extends MultiIterablePosting implements BlockPosting {

	BlockPosting[] bps;
	public BlockMultiIterablePosting(IterablePosting[] constituentIPs, int[] offsets) {
		super(constituentIPs, offsets);
		bps = new BlockPosting[constituentIPs.length];
		for(int i=0;i<constituentIPs.length;i++)
			bps[i] = (BlockPosting) constituentIPs[i];
	}

	@Override
	public int[] getPositions() {
		return bps[currentChild].getPositions();
	}

}
