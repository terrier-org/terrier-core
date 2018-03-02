package org.terrier.querying;

import org.terrier.querying.parser.Query;

@ProcessPhaseRequisites(ManagerRequisite.TERRIERQL)
public class TerrierQLToControls implements Process {

	@Override
	public void process(Manager manager, SearchRequest q) {
		Request rq = (Request)q;
		Query query = rq.getQuery();
		//System.out.println(query);
		//get the controls
		boolean rtr = ! query.obtainControls(manager.Allowed_Controls, rq.getControlHashtable());
		//we check that there is still something left in the query
		if (! rtr)
		{
			rq.setEmpty(true);
		}
	}

	@Override
	public String getInfo() {
		return this.getClass().getSimpleName();
	}

}
