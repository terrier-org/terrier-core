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
 * The Original Code is JForestsCommand.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.learning;

import org.terrier.applications.CLITool;

public class JForestsCommand extends CLITool {

	@Override
	public String commandname() {
		return "jforests";
	}

	@Override
	public String helpsummary() {
		return "runs the Jforests LambdaMART LTR implementation";
	}

	@Override
	public int run(String[] args) throws Exception {
		edu.uci.jforests.applications.Runner.main(args);
		return 0;
	}

	@Override
	public String help() {
		return "For information on running jforests, see https://github.com/yasserg/jforests or http://terrier.org/docs/current/learning.html"; 
	}

}
