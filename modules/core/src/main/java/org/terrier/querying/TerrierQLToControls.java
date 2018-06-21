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
 * The Original Code is TerrierQLToControls.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.querying;

import org.terrier.querying.parser.Query;

@ProcessPhaseRequisites(ManagerRequisite.TERRIERQL)
public class TerrierQLToControls implements Process {

	@Override
	public void process(Manager manager, Request rq) {
		Query query = rq.getQuery();
		//get the controls
		//TODO: this is a hack
		boolean rtr = ! query.obtainControls(((LocalManager) manager).Allowed_Controls, rq.getControls());
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
