package org.terrier.rest;

import org.terrier.querying.IndexRef;
import org.terrier.querying.Manager;
import org.terrier.querying.ManagerFactory;
import org.terrier.querying.SearchRequest;

public class TestRest {

//	@Before public void setup()
//	{
//		
//	}
	
	public static void main(String[] args) {
		Manager m = ManagerFactory.from(IndexRef.of("http://localhost:8080/"));
		SearchRequest srq = m.newSearchRequestFromQuery("word");
		m.runSearchRequest(srq);
		System.out.println(srq.getNumberOfDocumentsAfterFiltering());
	}
	
}
