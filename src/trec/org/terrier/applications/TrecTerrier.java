package org.terrier.applications;
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
 * The Original Code is TrecTerrier.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author) 
 */
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.terrier.applications.batchquerying.TRECQuerying;
import org.terrier.structures.outputformat.TRECDocidOutputFormat;
import org.terrier.evaluation.AdhocEvaluation;
import org.terrier.evaluation.Evaluation;
import org.terrier.evaluation.NamedPageEvaluation;
import org.terrier.structures.Index;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.LexiconUtil;
import org.terrier.structures.PostingIndexInputStream;
import org.terrier.structures.indexing.singlepass.Inverted2DirectIndexBuilder;
import org.terrier.structures.indexing.singlepass.hadoop.Inv2DirectMultiReduce;
import org.terrier.utility.ApplicationSetup;
/**
 * The text-based application that handles querying 
 * with Terrier, for TREC-like test collections.
 * <code>
TrecTerrier, indexing TREC collections with Terrier.<br>
usage: java TrecTerrier [flags in any order]<br>
<br>
  -h --help		print this message<br>
  -V --version	 print version information<br>
  -i --index	   index a collection<br>
  -id --inverted2direct		generate a direct index for an existing index 
  -r --retrieve	retrieve from an indexed collection<br>
  -e --evaluate	evaluates the results in the directory<br>
				   var/results with the specified qrels file<br>
				   in the file etc/trec.qrels<br>
<br>
If invoked with '-i', then both the direct and<br>
inverted files are build, unless it is specified which<br>
of the structures to build.<br>
  -d --direct	  creates the direct file<br>
  -v --inverted	creates the inverted file, from an already existing direct<br>
<br>
If invoked with '-r', there are the following options.<br>
  -c value		 parameter value for term frequency normalisation.<br>
				   If it is not specified, then the default value for each<br>
					weighting model is used, eg PL2 =&gt; c=1, BM25 b=&gt; 0.75<br>
  -q --queryexpand applies query expansion<br>
  --docids will only print docids in the .res file
<br>
If invoked with '-e', there is the following option.<br>
  -p --perquery	reports the average precision for each query separately.<br>
  filename.res	 restrict evaluation to filename.res only.<br>
<br>
If invoked with one of the following options, then the contents of the<br>
corresponding data structure are shown in the standard output.<br>
  --printdocid	 prints the contents of the document index<br>
  --printlexicon   prints the contents of the lexicon<br>
  --printinverted  prints the contents of the inverted file<br>
  --printdirect	prints the contents of the direct file<br>
  --printstats	 prints statistics about the indexed collection<br>
</code>
 * 
 * @author Vassilis Plachouras
 */
public class TrecTerrier {
	/** The logger used */
	private static Logger logger = LoggerFactory.getLogger(TrecTerrier.class);
	/** The unkown option*/
	protected String unknownOption;
	/** The file to evaluation, if any */
	protected String evaluationFilename = null;
	
	/** Specifies whether to apply query expansion*/
	protected boolean queryexpand;
	
	/** Specifies whether a help message is printed*/
	protected boolean printHelp;
	/** Specified whether a version message is printed*/
	protected boolean printVersion;
	
	/** Specifies whether to index a collection*/
	protected boolean indexing;
	
	/**
	 * Specifies whether to build the inverted file 
	 * from scrach, sigle pass method	
	 */
	protected boolean singlePass = false;

	/** use Hadoop indexing */
	protected boolean hadoop = false;
	
	/** Specifies whether to retrieve from an indexed collection*/
	protected boolean retrieving;
	
	/** Specifies whether to print the document index*/
	protected boolean printdocid;
	
	/** Specifies whether to print the lexicon*/
	protected boolean printlexicon;
	
	/** Specifies whether to print the inverted file*/
	protected boolean printinverted;
	
	/** Specifies whether to print the direct file*/
	protected boolean printdirect;
	
	/** Specifies whether to print the statistics file*/
	protected boolean printstats;
	
	/** whether to print the meta index */
	protected boolean printmeta;

	/**
	  * Specifies whether to perform trec_eval like evaluation,
	  * reporting only average precision for each query.
	  */
	protected boolean evaluation_per_query;
	/**
	 * Specifies if the evaluation is done for adhoc or named-page
	 * finding retrieval task. adhoc by default.
	 */
	protected String evaluation_type = "adhoc";


	/** 
	 * Specifies whether to build the inverted file
	 * from an already created direct file.
	 */
	protected boolean inverted;
	
	/**
	 * Specifies whether to build the direct file only.
	 */
	protected boolean direct;
	
	/** 
	 * The value of the term frequency 
	 * normalisation parameter.
	 */
	protected double c;
	
	/**
	 * Specifies whether to perform trec_eval like evaluation.
	 */
	protected boolean evaluation;
	
	/**
	 * Indicates whether there is a specified 
	 * value for the term frequency normalisation
	 * parameter.
	 */
	protected boolean isParameterValueSpecified;
	
	
	protected boolean inverted2direct;
	
	protected boolean docids = false;

	/**
	 * Prints the version information about Terrier
	 */
	protected void version()
	{
		System.out.println("TrecTerrier, indexing TREC collections with Terrier. Version "+ApplicationSetup.TERRIER_VERSION);
		//System.out.println("Built on ");
	}
	
	/**
	 * Prints a help message that explains the
	 * possible options.
	 */
	protected void usage() {
		System.out.println("TrecTerrier, indexing TREC collections with Terrier. Version "+ApplicationSetup.TERRIER_VERSION);
		System.out.println("usage: java TrecTerrier [flags in any order]");
		System.out.println("");
		System.out.println("  -h --help		print this message");
		System.out.println("  -V --version	 print version information");
		System.out.println("  -i --index	   index a collection");
		System.out.println("  -id --inverted2direct		generate a direct index for an existing index");
		System.out.println("  -r --retrieve	retrieve from an indexed collection");
		System.out.println("  -e --evaluate	evaluates the results in the directory");
		System.out.println("				   var/results with the specified qrels file");
		System.out.println("				   in the file etc/trec.qrels");
		System.out.println("");
		System.out.println("If invoked with \'-i\', then both the direct and");
		System.out.println("inverted files are build, unless it is specified which");
		System.out.println("of the structures to build.");
		System.out.println("  -d --direct	  creates the direct file");
		System.out.println("  -v --inverted	creates the inverted file, from an already existing direct");
		System.out.println("  -j --ifile	   creates the inverted file, from scratch, single pass");
		System.out.println("  -H --hadoop	   creates the inverted file, from scratch, using Hadoop MapReduce indexing");
		System.out.println("");
		System.out.println("If invoked with \'-r\', there are the following options.");
		System.out.println("  -c value		 parameter value for term frequency normalisation.");
		System.out.println("				   If it is not specified, then the default value for each");
		System.out.println("				   weighting model is used, eg PL2 => c=1, BM25 b=> 0.75");
		System.out.println("  -q --queryexpand applies query expansion");
		System.out.println("");
		System.out.println("If invoked with \'-e\', there is the following options.");
		System.out.println("  -p --perquery	reports the average precision for each query separately.");
		System.out.println("  -n --named		evaluates for the named-page finding task.");
		System.out.println("  filename.res	 restrict evaluation to filename.res only.");
		System.out.println("");
		System.out.println("If invoked with one of the following options, then the contents of the ");
		System.out.println("corresponding data structure are shown in the standard output.");
		System.out.println("  --printdocid	 prints the contents of the document index");
		System.out.println("  --printlexicon   prints the contents of the lexicon");
		System.out.println("  --printinverted  prints the contents of the inverted file");
		System.out.println("  --printdirect	prints the contents of the direct file");
		System.out.println("  --printstats	 prints statistics about the indexed collection");
		System.out.println("  --printmeta	 prints the contents of the meta structure");
	}
	
	/**
	 * The main method that starts the application
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		try {
			TrecTerrier trecTerrier = new TrecTerrier();
			int status = trecTerrier.processOptions(args);
			trecTerrier.applyOptions(status);
			//System.exit(0);
		} catch (Exception e) {
			System.err.println("A problem occurred: "+ e);
			e.printStackTrace();
		} catch (java.lang.OutOfMemoryError oome) {
			System.err.println(oome);
			oome.printStackTrace();
		}
		
	}
	
	/**
	 * Processes the command line arguments and
	 * sets the corresponding properties accordingly.
	 * @param args the command line arguments.
	 * @return int zero if the command line arguments are 
	 *		 processed successfully, otherwise it returns
	 *		 an error code.
	 */
	protected int processOptions(String[] args) {
		if (args.length == 0)
			return ERROR_NO_ARGUMENTS;
		
		int pos = 0;
		boolean applicationSetupUpdated = false;
		while (pos < args.length) {
			if (args[pos].startsWith("-D"))
			{
				String[] propertyKV = args[pos].replaceFirst("^-D", "").split("=");
				if (propertyKV.length ==1)
					propertyKV = new String[]{propertyKV[0], ""};
				ApplicationSetup.setProperty(propertyKV[0], propertyKV[1]);
				applicationSetupUpdated = true;
			}
			else if (args[pos].equals("-h") || args[pos].equals("--help"))
				printHelp = true;
			else if (args[pos].equals("-i") || args[pos].equals("--index"))
				indexing = true;
			else if (args[pos].equals("-j") || args[pos].equals("--ifile"))
				singlePass = true;					
			else if (args[pos].equals("-H") || args[pos].equals("--hadoop"))
				hadoop = true;
			else if (args[pos].equals("-r") || args[pos].equals("--retrieve"))
				retrieving = true;
			else if (args[pos].equals("-v") || args[pos].equals("--inverted"))
				inverted = true;
			else if (args[pos].equals("-id") || args[pos].equals("--inverted2direct"))
				inverted2direct = true;
			else if (args[pos].equals("-d") || args[pos].equals("--direct"))
				direct = true;
			else if (args[pos].equals("-q") || args[pos].equals("--queryexpand")) 
				queryexpand = true;
			else if (args[pos].equals("--printdocid"))
				printdocid = true;
			else if (args[pos].equals("-p") || args[pos].equals("--perquery"))
				evaluation_per_query = true;
			else if (args[pos].equals("--docids") )
				docids = true;
			else if (args[pos].equals("--printlexicon"))
				printlexicon = true;
			else if (args[pos].equals("--printinverted"))
				printinverted = true;
			else if (args[pos].equals("--printdirect"))
				printdirect = true;
			else if (args[pos].equals("--printstats"))
				printstats = true;
			else if (args[pos].equals("--printmeta"))
				printmeta = true;
			else if (args[pos].equals("-e") || args[pos].equals("--evaluate")){
				evaluation = true;
			}
			else if (args[pos].equals("-n") || args[pos].equals("--named")){
				evaluation_type = "named";
			}
			else if (args[pos].startsWith("-c")) {
				isParameterValueSpecified = true;
				if (args[pos].length()==2) { //the next argument is the value
					if (pos+1<args.length) { //there is another argument
						pos++;
						c = Double.parseDouble(args[pos]);
					} else 
						return ERROR_NO_C_VALUE;
				} else { //the value is in the same argument
					c = Double.parseDouble(args[pos].substring(2));
				}
			}
			else if (evaluation)
			{
				evaluationFilename= args[pos];
			} else {
				unknownOption = args[pos];
				return ERROR_UNKNOWN_OPTION;
			}
			pos++;
		}
		
		if (applicationSetupUpdated)
			ApplicationSetup.loadCommonProperties();
		
		
		if (isParameterValueSpecified && !retrieving) 
			return ERROR_GIVEN_C_NOT_RETRIEVING;
		
		if ((retrieving || queryexpand || c!=0) && (direct || inverted || indexing))
			return ERROR_CONFLICTING_ARGUMENTS;		

		if (hadoop && ! indexing)
			return ERROR_HADOOP_NOT_RETRIEVAL;
		
		if (direct && !indexing) 
			return ERROR_DIRECT_NOT_INDEXING;
		
		if (inverted && !indexing) 
			return ERROR_INVERTED_NOT_INDEXING;
		
		if (queryexpand && !retrieving)
			return ERROR_EXPAND_NOT_RETRIEVE;
		
		return ARGUMENTS_OK;
	}
	
	/**
	 * Calls the required classes from Terrier.
	 */
	public void run() throws Exception {
		if (printVersion) {
			version();
			return;
		}
		if (printHelp) { 
			usage();
			return;
		}

		long startTime = System.currentTimeMillis();
		if (indexing) {
			if (hadoop) 
			{
				if (inverted2direct) {
					String[] builderCommand = {"1", "--finish"};
					Inv2DirectMultiReduce.main(builderCommand);
				}
				
				try{
					HadoopIndexing.main(new String[]{});
				} catch (Exception e) {
					System.err.println(e);
					e.printStackTrace();
					return;
				}
			}
			else
			{
				TRECIndexing trecIndexing = new TRECIndexing();
				if(singlePass)
					trecIndexing.createSinglePass();	
				else if (direct)
					trecIndexing.createDirectFile();
				else if (inverted) 
					trecIndexing.createInvertedFile();
				else { //if none of the options is specified, build both structures
					trecIndexing.index();
				}
			}
		} else if (retrieving) {
			//if no value is given, then we use a default value
			if (docids)
				ApplicationSetup.setProperty("trec.querying.outputformat", TRECDocidOutputFormat.class.getName());
			TRECQuerying trecQuerying = new TRECQuerying(queryexpand);
			trecQuerying.processQueries(c, isParameterValueSpecified);
			trecQuerying.close();
		} else if (printdocid) {
			Index.setIndexLoadingProfileAsRetrieval(false);
			Index i = Index.createIndex();
			if (i == null)
			{
				logger.error("No such index : "+ Index.getLastIndexLoadError());				
			} else {
				IndexUtil.printDocumentIndex(i, "document");
				i.close();
			}
		} else if (printmeta) {
			Index.setIndexLoadingProfileAsRetrieval(false);
			Index i = Index.createIndex();
			if (i == null)
			{
				logger.error("No such index : "+ Index.getLastIndexLoadError());				
			}
			else
			{
				IndexUtil.printMetaIndex(i, "meta");
				i.close();
			}
		} else if (printlexicon) {
			Index.setIndexLoadingProfileAsRetrieval(false);
			Index i = Index.createIndex();
			if (i == null)
			{
				logger.error("No such index : "+ Index.getLastIndexLoadError());				
			}
			else if (! i.hasIndexStructureInputStream("lexicon"))
			{
				logger.warn("Sorry, no lexicon index structure in index");
			}
			else
			{
				LexiconUtil.printLexicon(i, "lexicon");
			}
		} else if (printdirect) {
			Index.setIndexLoadingProfileAsRetrieval(false);
			Index i = Index.createIndex();
			if (i == null)
			{
				logger.error("No such index : "+ Index.getLastIndexLoadError());				
			}
			else if (! i.hasIndexStructureInputStream("direct"))
			{
				logger.warn("Sorry, no direct index structure in index");
			}
			else
			{
				PostingIndexInputStream dirIndex = (PostingIndexInputStream)(i.getIndexStructureInputStream("direct"));
				dirIndex.print();
				dirIndex.close();
				i.close();
			}
		} else if (printinverted) {
			Index.setIndexLoadingProfileAsRetrieval(false);
			Index i = Index.createIndex();
			if (i == null)
			{
				logger.error("No such index : "+ Index.getLastIndexLoadError());				
			}
			else if (i.hasIndexStructureInputStream("inverted"))
			{
				PostingIndexInputStream invIndex = (PostingIndexInputStream)(i.getIndexStructureInputStream("inverted"));
				invIndex.print();
				invIndex.close();
				i.close();
			}
			else
			{
				logger.warn("Sorry, no inverted index inputstream structure in index");
			}
			
		} else if (printstats) {
			Index.setIndexLoadingProfileAsRetrieval(false);
			Index i = Index.createIndex();
			if (i == null)
			{
				logger.error("No such index : "+ Index.getLastIndexLoadError());				
			}
			else {
				logger.info("Collection statistics:");
				logger.info("number of indexed documents: " + i.getCollectionStatistics().getNumberOfDocuments());
				logger.info("size of vocabulary: " +  i.getCollectionStatistics().getNumberOfUniqueTerms());
				logger.info("number of tokens: " +  i.getCollectionStatistics().getNumberOfTokens());
				logger.info("number of pointers: " +  i.getCollectionStatistics().getNumberOfPointers());
				i.close();
			}
			
		} else if (evaluation) {
			Evaluation te = null;
			if (evaluation_type.equals("adhoc"))
				te = new AdhocEvaluation();
			else if (evaluation_type.equals("named"))
				te = new NamedPageEvaluation();
			String[] nomefile = null;
			if (evaluationFilename == null) {
				/* list all the result files and then evaluate them */
				File fresdirectory = new File(ApplicationSetup.TREC_RESULTS);
				nomefile = fresdirectory.list();
				if (nomefile == null)
					nomefile = new String[0];
			}
			else
			{
				nomefile = new String[]{evaluationFilename}; 
			}
			for (int i = 0; i < nomefile.length; i++) {
				if (nomefile[i].endsWith(".res")) {
					String resultFilename = ApplicationSetup.TREC_RESULTS+ "/" + nomefile[i];
					if (nomefile[i].indexOf("/") >= 0)
						resultFilename = nomefile[i];
					String evaluationResultFilename =
						resultFilename.substring(
							0,
							resultFilename.lastIndexOf('.'))
							+ ".eval";
					te.evaluate(resultFilename);
					if (evaluation_per_query)
						te.writeEvaluationResultOfEachQuery(evaluationResultFilename);
					else
						te.writeEvaluationResult(evaluationResultFilename);
				}
			}
		} else if (inverted2direct) {
			String[] builderCommand = {};
			Inverted2DirectIndexBuilder.main(builderCommand);
		}
		
		long endTime = System.currentTimeMillis();
		System.err.println("Time elapsed: " + (endTime-startTime)/1000.0d + " seconds.");
	}
	/**
	 * Apply the option resulted from processing the command line arguments
	 * @param status the status after process the command line arguments.
	 */
	public void applyOptions(int status) throws Exception {
		switch(status) {
			case ERROR_NO_ARGUMENTS : 
				usage();
				break;
			case ERROR_NO_C_VALUE :
				System.err.println("A value for the term frequency normalisation parameter");
				System.err.println("is required. Please specify it with the option '-c value'");
				break;
			case ERROR_CONFLICTING_ARGUMENTS : 
				System.err.println("There is a conclict between the specified options. For example,");
				System.err.println("option '-c' is used only in conjuction with option '-r'.");
				System.err.println("In addition, options '-v' or '-d' are used only in conjuction");
				System.err.println("with option '-i'");
				break;
			case ERROR_PRINT_DOCINDEX_FILE_NOT_EXISTS :
				System.err.println("The specified document index file does not exist.");
				break;
			case ERROR_PRINT_DIRECT_FILE_NOT_EXISTS : 
				System.err.println("The specified direct index does not exist.");
				break;
			case ERROR_UNKNOWN_OPTION : 
				System.err.println("The option '" +unknownOption+"' is not recognised");
				break;
			case ERROR_DIRECT_NOT_INDEXING : 
				System.err.println("The option '-d' or '--direct' can be used only while indexing with option '-i'.");
				break;
			case ERROR_INVERTED_NOT_INDEXING : 
				System.err.println("The option '-i' or '--inverted' can be used only while indexing with option '-i'.");
				break;
			case ERROR_EXPAND_NOT_RETRIEVE : 
				System.err.println("The option '-q' or '--queryexpand' can be used only while retrieving with option '-r'.");
				break;
			case ERROR_GIVEN_C_NOT_RETRIEVING : 
				System.err.println("A value for the parameter c can be specified only while retrieving with option '-r'.");
				break;
			case ERROR_HADOOP_NOT_RETRIEVAL : 
				System.err.println("Hadoop mode '-H' can only be used for indexing");
				break;
			case ERROR_HADOOP_ONLY_INDEX :
				System.err.println("Hadoop mode '-H' can only be used for straightforward indexing");
				break;
			case ARGUMENTS_OK : 
			default :
				run();			
		}
	}
	
	protected static final int ARGUMENTS_OK = 0;
	protected static final int ERROR_NO_ARGUMENTS = 1;
	protected static final int ERROR_NO_C_VALUE = 2;
	protected static final int ERROR_CONFLICTING_ARGUMENTS = 3;
	protected static final int ERROR_DIRECT_FILE_EXISTS = 4;
	protected static final int ERROR_DIRECT_FILE_NOT_EXISTS = 6;
	protected static final int ERROR_PRINT_DOCINDEX_FILE_NOT_EXISTS = 7;
	protected static final int ERROR_PRINT_LEXICON_FILE_NOT_EXISTS = 8;
	protected static final int ERROR_PRINT_INVERTED_FILE_NOT_EXISTS = 9;
	protected static final int ERROR_PRINT_STATS_FILE_NOT_EXISTS = 10;
	protected static final int ERROR_PRINT_DIRECT_FILE_NOT_EXISTS = 11;
	protected static final int ERROR_UNKNOWN_OPTION = 12;
	protected static final int ERROR_DIRECT_NOT_INDEXING = 13;
	protected static final int ERROR_INVERTED_NOT_INDEXING = 14;
	protected static final int ERROR_EXPAND_NOT_RETRIEVE = 15;
	protected static final int ERROR_GIVEN_C_NOT_RETRIEVING = 16;
	protected static final int ERROR_LANGUAGEMODEL_NOT_RETRIEVE = 17;
	protected static final int ERROR_HADOOP_NOT_RETRIEVAL = 18;
	protected static final int ERROR_HADOOP_ONLY_INDEX = 19;
}
