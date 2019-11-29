package org.terrier.realtime.multi;

import org.terrier.structures.postings.BlockPosting;
import org.terrier.structures.postings.BlockPostingImpl;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.WritablePosting;

public class BlockMultiDirectIterablePosting extends
		MultiDirectIterablePosting
		implements BlockPosting {

	BlockPosting bp;
	public BlockMultiDirectIterablePosting(IterablePosting posting,
			MultiLexicon lex, int shard) {
		super(posting, lex, shard);
		bp = (BlockPosting)posting;
	}

	@Override
	public int[] getPositions() {
		return bp.getPositions();
	}

	@Override
	public WritablePosting asWritablePosting() {
		return new BlockPostingImpl(this.getId(), this.getFrequency(), this.getPositions());
	}

}
