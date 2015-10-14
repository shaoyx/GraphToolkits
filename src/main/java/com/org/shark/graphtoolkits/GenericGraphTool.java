package com.org.shark.graphtoolkits;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

public interface GenericGraphTool {

	void registerOptions(Options options);
	void run(CommandLine cmd);
	boolean verifyParameters(CommandLine cmd);
	
}
