package org.terrier.querying;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.matching.BaseMatching;
import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.MatchingQueryTerms.MatchingTerm;
import org.terrier.matching.matchops.SingleTermOp;
import org.terrier.querying.parser.Query.QTPBuilder;
import org.terrier.structures.Index;
import org.terrier.structures.LexiconEntry;

import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * RM3 implementation. This has been closely compared to the Anserini implementation using a common index.
 * 
 * @author Nicola Tonellotto and Craig Macdonald
 */
@ProcessPhaseRequisites({ ManagerRequisite.MQT, ManagerRequisite.RESULTSET })
public class RM3 extends RM1 {

    protected static Logger logger = LoggerFactory.getLogger(RM3.class);

    protected static final float DEFAULT_LAMBDA = 0.6F;
    protected Int2FloatMap originalQueryTermScores;
    protected float lambda;

    public RM3(final int fbTerms, final int fbDocs, final Index index) {
        this(fbTerms, fbDocs, index, DEFAULT_LAMBDA);
    }

    public RM3(final int fbTerms, final int fbDocs, final Index index, final float lambda) {
        super(fbTerms, fbDocs, index);

        this.originalQueryTermScores = new Int2FloatOpenHashMap();
        this.lambda = lambda;
    }

    public RM3() {
        super();
        this.originalQueryTermScores = new Int2FloatOpenHashMap();
        this.lambda = DEFAULT_LAMBDA;
    }

    public boolean expandQuery(MatchingQueryTerms mqt, Request rq) throws IOException {
        this.index = rq.getIndex();
        computeOriginalTermScore(mqt);
        if (rq.hasControl("rm3.lambda"))
            this.lambda = Float.parseFloat(rq.getControl("rm3.lambda"));
        List<ExpansionTerm> expansions = this.expand(rq);
        mqt.clear();
        StringBuilder sQuery = new StringBuilder();
        for (ExpansionTerm et : expansions) {
            mqt.add(QTPBuilder.of(new SingleTermOp(et.text)).setTag(BaseMatching.BASE_MATCHING_TAG)
                    .setWeight(et.weight).build());
            sQuery.append(et.text + "^" + et.weight + " ");
        }
        logger.info("Reformulated query "+ mqt.getQueryId() +" @ lambda="+this.lambda+": " + sQuery.toString());
        //logger.info("Reformulated query: " + mqt.toString());
        return true;
    }

    protected void computeOriginalTermScore(final MatchingQueryTerms mqt) {
        this.originalQueryTermScores.clear();
        final float queryLength = (float) mqt.stream().map(mt -> mt.getValue().getWeight())
                .mapToDouble(Double::doubleValue).sum();
        for (MatchingTerm mt : mqt) {

            LexiconEntry le = super.index.getLexicon().getLexiconEntry(mt.getKey().toString());
            if (le == null)
                continue;
            int termid = le.getTermId();
            float termCount = (float) mt.getValue().getWeight();
            originalQueryTermScores.put(termid, termCount / queryLength);
        }
    }

    @Override
    protected void computeFeedbackTermScores() {
        super.computeFeedbackTermScores();
        super.clipTerms();
        super.normalizeFeedbackTermScores();

        for (int termid : feedbackTermScores.keySet()) {
            //System.err.println("termid " + termid + " term " + super.index.getLexicon().getLexiconEntry(termid).getKey() +" " + feedbackTermScores.get(termid));
            if (originalQueryTermScores.containsKey(termid)) {
                //System.err.println("termid " + termid + " term " + super.index.getLexicon().getLexiconEntry(termid).getKey() +" " +"not new: old weight = " +  originalQueryTermScores.get(termid) + " fbweight="  + feedbackTermScores.get(termid));

                float weight = lambda * originalQueryTermScores.get(termid)
                        + (1 - lambda) * feedbackTermScores.get(termid);
                feedbackTermScores.put(termid, weight);
            } else {
                feedbackTermScores.put(termid, (1 - lambda) * feedbackTermScores.get(termid));
                //System.err.println("termid " + termid + " term " + super.index.getLexicon().getLexiconEntry(termid).getKey() +" " + feedbackTermScores.get(termid));
            }
        }

        for (int termid : originalQueryTermScores.keySet()) {
            if (!feedbackTermScores.containsKey(termid)) {
                float weight = lambda * originalQueryTermScores.get(termid);
                feedbackTermScores.put(termid, weight);
            }
        }
    }

    @Override
    public List<ExpansionTerm> expand(Request srq) throws IOException {
        //return super.expand(srq);

        this.topLexicon.clear();
        this.topDocs.clear();
        this.feedbackTermScores.clear();

        retrieveTopDocuments(srq.getResultSet());   
        computeFeedbackTermScores();
        
        List<ExpansionTerm> rtr = new ObjectArrayList<>();
        for (int termid: feedbackTermScores.keySet())
            rtr.add(new ExpansionTerm(termid, index.getLexicon().getLexiconEntry(termid).getKey(), feedbackTermScores.get(termid)));
        return rtr;
    }
}
