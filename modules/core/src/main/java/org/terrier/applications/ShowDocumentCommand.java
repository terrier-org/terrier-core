package org.terrier.applications;

import gnu.trove.TIntObjectHashMap;

import java.util.Arrays;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.terrier.applications.CLITool.CLIParsedCLITool;
import org.terrier.querying.IndexRef;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.FieldDocumentIndexEntry;
import org.terrier.structures.Index;
import org.terrier.structures.IndexFactory;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.Lexicon;
import org.terrier.structures.MetaIndex;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.postings.BlockPosting;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;

import com.google.common.collect.Sets;

/** Utility command to display the contents of a document to stdout,
 * as obtained from the direct index.
 * Instantiated using bin/terrier showdocument
 * @since 5.1
 */
public class ShowDocumentCommand extends CLIParsedCLITool {

	@Override
	public int run(CommandLine line) throws Exception {
		@SuppressWarnings("deprecation")
		IndexRef ref = IndexRef.of(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX);
		Index index = IndexFactory.of(ref);
		if (index == null)
		{
			System.err.println("No such index: " + ref.toString());
			return 1;
		}
		PostingIndex<?> di = index.hasIndexStructure("direct") ? index.getDirectIndex() : null;
		DocumentIndex doi = index.getDocumentIndex();
		MetaIndex meta = index.getMetaIndex();
		boolean printmeta = line.hasOption("meta");
		boolean blocks = false;
		if (line.hasOption('i'))
		{
			if ( di != null && (di.getPostings(doi.getDocumentEntry(0)) instanceof BlockPosting))
			{
				blocks = true;
			} else {
				if (di == null)
				{
					System.err.println("Index does not have a direct index");
				} else {
					System.err.println("Index does not have a direct index with positions");
				}
			}
		}
		String metaKey = "docno";
		if (line.hasOption('k'))
			metaKey = line.getOptionValue('k');
		for(String docno : line.getArgs())
		{
			int docid = -1;
			if (line.hasOption("docid"))
			{
				docid = Integer.parseInt(docno);
			} 
			else {
				if (! ArrayUtils.contains(meta.getReverseKeys(), metaKey))
				{
					System.err.println("Sorry, reverse lookups on meta key " + metaKey + " are not supported by this index. "
							+" Perhaps you needed to configure property indexer.meta.reverse.keys="+metaKey+" at indexing time?");
					break;
				}
				
				docid = meta.getDocument("docno", docno);
				if (docid < 0)
				{
					System.err.println("No such docno " + docno);
					continue;
				}
			}
			DocumentIndexEntry die = doi.getDocumentEntry(docid);
			System.err.println("Document Length: " + die.getDocumentLength());
			if (die instanceof FieldDocumentIndexEntry)
				System.err.println("Field Lengths: " + ArrayUtils.join(((FieldDocumentIndexEntry)die).getFieldLengths(), ","));
			if (die.getNumberOfEntries() > 0)
				System.err.println("Document Uniq Terms: " + die.getNumberOfEntries());
			
			if (di != null)
			{
				IterablePosting ip = di.getPostings(die);
				System.out.println("Contents: ");
				if (blocks)
					System.out.println(getContentsBlocks(ip, index.getLexicon()));
				else
					System.out.println(getContentsNoBlocksFreq(ip, index.getLexicon()));
			} else {
				System.err.println("No direct index data structure");
				break;
			}
			if (printmeta)
			{
				String[] keys = meta.getKeys();
				String[] values = meta.getAllItems(docid);
				for (int i=0;i<keys.length;i++){
					System.out.println(keys[i] + ": " + values[i]);
				}
			}
			
		}
		IndexUtil.close(di);
		index.close();
		return 0;
	}

	@Override
	protected Options getOptions() {
		Options o = super.getOptions();
		o.addOption(Option.builder("docid")
				.longOpt("docid")
				.desc("lookup based on docid rather than docno")
				.build());
		o.addOption(Option.builder("k")
				.longOpt("metakey")
				.hasArg()
				.desc("lookup based on named metaindex key than docno")
				.build());
		o.addOption(Option.builder("meta")
				.longOpt("meta")
				.desc("print the metadata for the document")
				.build());
		o.addOption(Option.builder("i")
				.longOpt("inorder")
				.desc("print the contents of the document in original order of occurrence (index must have position information).")
				.build());
		return o;
	}

	@Override
	public Set<String> commandaliases() {
		return Sets.newHashSet("sd");
	}

	@Override
	public String commandname() {
		return "showdocument";
	}

	@Override
	public String helpsummary() {
		return "displays the contents of a document";
	}
	
	String getContentsNoBlocksFreq(IterablePosting ip, Lexicon<String> lex) throws Exception {
		StringBuilder rtr = new StringBuilder();
		int termid;
		while( (termid = ip.next()) != IterablePosting.END_OF_LIST){
			String term = lex.getLexiconEntry(termid).getKey();
			rtr.append(term);
			rtr.append(':');
			rtr.append(ip.getFrequency());
			rtr.append(' ');
		}
		return rtr.toString();
	}
	
	String getContentsNoBlocks(IterablePosting ip, Lexicon<String> lex, boolean expandFreq) throws Exception {
		StringBuilder rtr = new StringBuilder();
		int termid;
		while( (termid = ip.next()) != IterablePosting.END_OF_LIST){
			String term = lex.getLexiconEntry(termid).getKey();
			for(int i=0;i< (expandFreq ? ip.getFrequency() : 1); i++)
			{
				rtr.append(term);
				rtr.append(' ');
			}			
		}
		return rtr.toString();
	}
	
	String getContentsBlocks(IterablePosting ip, Lexicon<String> lex) throws Exception {
		//this assumes block.size is 1.
		StringBuilder rtr = new StringBuilder();
		int termid;
		TIntObjectHashMap<String> pos2term = new TIntObjectHashMap<String>();
		BlockPosting bp = (BlockPosting) ip;
		while( (termid = ip.next()) != IterablePosting.END_OF_LIST){
			String term = lex.getLexiconEntry(termid).getKey();
			int[] positions = bp.getPositions();
			for(int pos : positions) {
				pos2term.put(pos, term);
			}
		}
		int[] positions = pos2term.keys();
		Arrays.sort(positions);
		for(int pos : positions){
			rtr.append(pos2term.get(pos));
			rtr.append(' ');
		}
		return rtr.toString();
	}

}
