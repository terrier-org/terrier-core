package org.terrier.structures;
import org.apache.commons.cli.CommandLine;
import org.terrier.applications.CLITool;
import org.terrier.applications.CLITool.CLIParsedCLITool;
import org.terrier.querying.IndexRef;
import java.util.Arrays;
import java.io.IOException;

public class IndexStatsCommand extends CLIParsedCLITool {

    @Override
    public int run(CommandLine line) throws Exception {
        IndexRef iR = getIndexRef(line);
        IndexOnDisk.setIndexLoadingProfileAsRetrieval(false);
        Index i = IndexFactory.of(iR);
        if (i == null)
        {
            System.err.println("Index not found at " + iR);
            return 1;
        }
        System.out.println("Collection statistics:");
        System.out.println("number of indexed documents: " + i.getCollectionStatistics().getNumberOfDocuments());
        System.out.println("size of vocabulary: " +  i.getCollectionStatistics().getNumberOfUniqueTerms());
        System.out.println("number of tokens: " +  i.getCollectionStatistics().getNumberOfTokens());
        System.out.println("number of postings: " +  i.getCollectionStatistics().getNumberOfPostings());
        System.out.println("number of fields: " +  i.getCollectionStatistics().getNumberOfFields());
        System.out.println("field names: " +  Arrays.toString(i.getCollectionStatistics().getFieldNames()));
        
        Boolean blocks = null;
        if (i instanceof PropertiesIndex)
        {
            PropertiesIndex pi = (PropertiesIndex)i;
            for(String structureName : new String[]{"direct", "inverted"})
            {
                int num = pi.getIntIndexProperty("index." + structureName + ".blocks", -1);
                if (num != -1)
                {
                    blocks = num > 0;
                    break;
                }
            }
        }
        System.out.println("blocks: " + ( blocks == null ? "unknown" : blocks.toString() ));
        
        try {
            i.close();
        } catch (IOException e) {}
        return 0;
    }

    @Override
    public String commandname() {
        return "indexstats";
    }

    @Override
    public String helpsummary() {
        return "display the statistics of an index";
    }

    @Override
    public String sourcepackage() {
        return CLITool.PLATFORM_MODULE;
    }
}