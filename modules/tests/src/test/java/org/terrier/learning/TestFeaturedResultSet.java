package org.terrier.learning;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class TestFeaturedResultSet {
    
	@Test public void testSorting_2docs() 
	{
		FeaturedResultSet r = new FeaturedQueryResultSet(2);
		r.initialise();
    
        r.getDocids()[0]	= 10;
        r.getScores()[0] = 5d;
        r.getDocids()[1]	= 9;
        r.getScores()[1] = 10d;

        r.putFeatureScores​("f0", new double[]{1d, 2d});
        assertNotNull(r.getFeatureScores​("f0"));
        //assertNotNull(r.getFeatureScores​(0));

        r.sort();
        assertEquals(9, r.getDocids()[0]);
        assertEquals(10, r.getDocids()[1]);

        assertEquals(10d, r.getScores()[0], 0.0d);
        assertEquals(5d, r.getScores()[1], 0.0d);

        assertEquals(2d, r.getFeatureScores​("f0")[0], 0.0d);
        assertEquals(1d, r.getFeatureScores​("f0")[1], 0.0d);
        // assertEquals(2d, r.getFeatureScores​(0)[0], 0.0d);
        // assertEquals(1d, r.getFeatureScores​(0)[1], 0.0d);

    }
    

    @Test public void testSorting_3docs() 
	{

        //descending score order, no resorting required
		FeaturedResultSet r1 = new FeaturedQueryResultSet(3);
		r1.initialise();
    
        r1.getDocids()[0]	= 278;
        r1.getScores()[0] = 3.648514176119459;
        r1.getDocids()[1]	= 5424;
        r1.getScores()[1] = 3.6192189795655456;
        r1.getDocids()[2]	= 507;
        r1.getScores()[2] = 3.120452021714615;

        r1.putFeatureScores​("f0", new double[]{4.0340051873554685, 4.093322930375105, 4.093229216366462});

        //docid order, sorting needed
        FeaturedResultSet r2 = new FeaturedQueryResultSet(3);
		r2.initialise();
    
        r2.getDocids()[0]	= 278;
        r2.getScores()[0] = 3.648514176119459;
        
        r2.getDocids()[1]	= 507;
        r2.getScores()[1] = 3.120452021714615;

        r2.getDocids()[2]	= 5424;
        r2.getScores()[2] = 3.6192189795655456;

        r2.putFeatureScores​("f0", new double[]{4.0340051873554685, 4.093229216366462, 4.093322930375105});
        
        for (FeaturedResultSet r : new FeaturedResultSet[]{r1, r2})
        {
            assertNotNull(r.getFeatureScores​("f0")); 
            
            r.sort();
            assertEquals(278, r.getDocids()[0]);
            assertEquals(5424, r.getDocids()[1]);
            assertEquals(507, r.getDocids()[2]);

            assertEquals(3.648514176119459, r.getScores()[0], 0.0d);
            assertEquals(3.6192189795655456, r.getScores()[1], 0.0d);
            assertEquals(3.120452021714615, r.getScores()[2], 0.0d);            

            assertEquals(4.0340051873554685, r.getFeatureScores​("f0")[0], 0.0d);
            assertEquals(4.093322930375105, r.getFeatureScores​("f0")[1], 0.0d);
            assertEquals(4.093229216366462, r.getFeatureScores​("f0")[2], 0.0d);
        }

	}

}
