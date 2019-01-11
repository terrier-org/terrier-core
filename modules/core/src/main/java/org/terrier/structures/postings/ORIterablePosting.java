/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org 
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.ac.uk/
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is ORIterablePosting.java.
 *
 * The Original Code is Copyright (C) 2004-2019 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.structures.postings;

import java.io.Serializable;
import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;

/** An IterablePosting that combines more than one IterablePosting into a single IterablePosting.
 * By doing so, multiple term's posting lists can appear as a singe posting list.
 * @since 3.5
 * @author Craig Macdonald
 * @see FieldORIterablePosting
 * @see BlockORIterablePosting
 * @see BlockFieldORIterablePosting
 */
public class ORIterablePosting extends IterablePostingImpl {

	/** Factory method to create an appropriate ORIterablePosting from the specified IterablePostings.
	 * Four types of ORIterablePosting maybe returned, based on the lowest common denominator type 
	 * of the specified IterablePosting classes:
	 * <ul>
	 * <li>BlockFieldORIterablePosting - if the all posting lists are both a BlockPosting and FieldPosting</li>
	 * <li>BlockORIterablePosting - if the all posting lists are a BlockPosting but not all a FieldPosting</li>
	 * <li>FieldORIterablePosting - if the all posting lists are a FieldPosting but not all a BlockPosing</li>
	 * <li>ORIterablePosting - otherwise</li>
	 * </ul>
	 */
	public static ORIterablePosting mergePostings(IterablePosting[] ips) throws IOException
	{
		boolean blocks = true;
		boolean fields = true;
		for(IterablePosting ip : ips)
		{
			if (! (ip instanceof BlockPosting))
			{
				blocks = false;
				break;
			}
		}
		for(IterablePosting ip : ips)
		{
			if (! (ip instanceof FieldPosting))
			{
				fields = false;
				break;
			}
		}
		if (blocks && fields)
			return new BlockFieldORIterablePosting(ips);
		if (blocks)
			return new BlockORIterablePosting(ips);
		if (fields)
			return new FieldORIterablePosting(ips);
		return new ORIterablePosting(ips);
	}
	
	final static class IterablePostingIdComparator implements Comparator<IterablePosting>, Serializable
	{
		private static final long serialVersionUID = 1L;
		@Override
		public int compare(IterablePosting o1, IterablePosting o2) {
			return o1.getId() - o2.getId();
		}		
	}
	
	final PriorityQueue<IterablePosting> postingQueue;
	
	int frequency = 0;
	int id = -1;
	int doclen = 0;
	
	/** Create a Basic ORIterablePosting from the specified postings */
	public ORIterablePosting(IterablePosting[] ips) throws IOException
	{
		postingQueue = new PriorityQueue<IterablePosting>(ips.length, new IterablePostingIdComparator());
		for(IterablePosting ip : ips)
		{
			if (ip.next() != IterablePosting.EOL)
				postingQueue.add(ip);
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean endOfPostings() {
		return postingQueue.size() > 0;
	}
	
	
	protected void firstPosting(Posting p)
	{
		doclen = p.getDocumentLength();
		frequency = p.getFrequency();
	}
	
	protected void addPosting(Posting p)
	{
		frequency += p.getFrequency();
	}

	/** {@inheritDoc} */
	@Override
	public int next() throws IOException {
		IterablePosting ip = postingQueue.poll();
		if (ip == null)
			return id = IterablePosting.EOL;
		id = ip.getId();
		firstPosting(ip);
		if (ip.next() != IterablePosting.EOL)
			postingQueue.add(ip);
		while(postingQueue.size() > 0 && postingQueue.peek().getId() == id)
		{
			ip = postingQueue.poll();
			addPosting(ip);
			if (ip.next() != IterablePosting.EOL)
				postingQueue.add(ip);
		}
		return id;
	}
	
	

	@Override
	public int next(int target) throws IOException {
		final IterablePosting ips[] = postingQueue.toArray(new IterablePosting[postingQueue.size()]);
		postingQueue.clear(); boolean OK = false;
		for(IterablePosting ip : ips)
		{
			if (ip.getId() >= target || (ip.next(target) != IterablePosting.EOL))
			{
				postingQueue.add(ip);
				OK = true;
			}
		}
		if (! OK)
			return id = IterablePosting.EOL;
		IterablePosting ip = postingQueue.poll();
		id = ip.getId();
		firstPosting(ip);
		if (ip.next() != IterablePosting.EOL)
			postingQueue.add(ip);
		
		while(postingQueue.size() > 0 && postingQueue.peek().getId() == id)
		{
			ip = postingQueue.poll();
			addPosting(ip);
			if (ip.next() != IterablePosting.EOL)
				postingQueue.add(ip);
		}
		return id;
	}

	/** {@inheritDoc} */
	@Override
	public WritablePosting asWritablePosting() {
		return new BasicPostingImpl(id, frequency);
	}

	/** {@inheritDoc} */
	@Override
	public int getDocumentLength() {
		return doclen;
	}

	/** {@inheritDoc} */
	@Override
	public int getFrequency() {
		return frequency;
	}

	/** {@inheritDoc} */
	@Override
	public int getId() {
		return id;
	}

	/** {@inheritDoc} */
	@Override
	public void setId(int _id) {
		this.id = _id;
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws IOException {
		
	}

}
