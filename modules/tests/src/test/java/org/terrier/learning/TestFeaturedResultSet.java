package org.terrier.learning;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class TestFeaturedResultSet {
    
	@Test public void testSorting() 
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

}
