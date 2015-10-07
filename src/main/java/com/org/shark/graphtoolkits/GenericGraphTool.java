package com.org.shark.graphtoolkits;

import org.apache.commons.cli.CommandLine;

public interface GenericGraphTool {
	
	void run(CommandLine cmd);
	
	boolean verifyParameters(CommandLine cmd);
	
}
