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

}
