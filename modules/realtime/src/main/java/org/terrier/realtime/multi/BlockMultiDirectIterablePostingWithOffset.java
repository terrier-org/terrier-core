package org.terrier.realtime.multi;

import org.terrier.structures.postings.BlockPosting;
import org.terrier.structures.postings.IterablePosting;

public class BlockMultiDirectIterablePostingWithOffset extends
		MultiDirectIterablePostingWithOffset
		implements BlockPosting {

	BlockPosting bp;
	public BlockMultiDirectIterablePostingWithOffset(IterablePosting posting,
			int idoffset) {
		super(posting, idoffset);
		bp = (BlockPosting)posting;
	}

	@Override
	public int[] getPositions() {
		return bp.getPositions();
	}

}
