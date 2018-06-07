package org.terrier.querying;

import org.terrier.querying.parser.Query;

@ProcessPhaseRequisites(ManagerRequisite.TERRIERQL)
public class TerrierQLToControls implements Process {

	@Override
	public void process(Manager manager, Request rq) {
		Query query = rq.getQuery();
		//get the controls
		//TODO: this is a hack
		boolean rtr = ! query.obtainControls(((LocalManager) manager).Allowed_Controls, rq.getControlHashtable());
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
