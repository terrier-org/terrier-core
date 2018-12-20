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
import org.terrier.structures.Index;
import org.terrier.structures.IndexFactory;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.Lexicon;
import org.terrier.structures.MetaIndex;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.postings.BlockPosting;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.utility.ApplicationSetup;

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
		PostingIndex<?> di = index.hasIndexStructure("direct") ? index.getDirectIndex() : null;
		DocumentIndex doi = index.getDocumentIndex();
		MetaIndex meta = index.getMetaIndex();
		boolean printmeta = line.hasOption("meta");
		boolean blocks = di != null && (di.getPostings(doi.getDocumentEntry(0)) instanceof BlockPosting);
		for(String docno : line.getArgs())
		{
			int docid = -1;
			if (line.hasOption("docid"))
			{
				docid = Integer.parseInt(docno);
			} 
			else {
				docid = meta.getDocument("docno", docno);
				if (docid < 0)
				{
					System.err.println("No such docno " + docno);
					continue;
				}
			}
			DocumentIndexEntry die = doi.getDocumentEntry(docid);
			if (di != null)
			{
				IterablePosting ip = di.getPostings(die);
				System.out.println("Contents: ");
				if (blocks)
					System.out.println(getContentsBlocks(ip, index.getLexicon()));
				else
					System.out.println(getContentsNoBlocks(ip, index.getLexicon(), true));
			} else {
				System.err.println("No direct index data structure");
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
		return 0;
	}

	@Override
	protected Options getOptions() {
		Options o = super.getOptions();
		o.addOption(Option.builder("docid").longOpt("docid").build());
		o.addOption(Option.builder("meta").longOpt("meta").build());
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
