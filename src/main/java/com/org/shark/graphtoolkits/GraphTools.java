package com.org.shark.graphtoolkits;

import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import com.org.shark.graphtoolkits.utils.AnnotationUtils;
import com.org.shark.graphtoolkits.utils.GraphAnalyticTool;

public class GraphTools {

	private static Options OPTIONS;
	  static {
		  OPTIONS = new Options();
		  OPTIONS.addOption("h", "help", false, "Help");
		  OPTIONS.addOption("lt", "listTools", false, "List supported tools");
		  OPTIONS.addOption("tc", "toolClass", true, "Specify the tool class");
		  OPTIONS.addOption("i", "input", true, "Path of the input graph");

		  OPTIONS.addOption("th", "threshold", true, "Threshold for edge weight");
		  OPTIONS.addOption("op", "outputPath", true, "The path of output files");
		  OPTIONS.addOption("sv", "startVertex", true, "The start vertex for 2-hop search");

		  OPTIONS.addOption("iter", "iteration", true, "The limitation of iteration");
		  OPTIONS.addOption("cSize", "clusterSize", true, "The limitation of the number of cluster size");
		  OPTIONS.addOption("vcSize", "vertexClusterSize", true, "The limitation of the number of vertices in a cluster");
		  OPTIONS.addOption("vccSize", "vertexClusterCandidateSize", true, "The limitation of the number of candidate clusters");
		  OPTIONS.addOption("fb", "boundaryFactor", true, "The factor for boundary edges");
		  OPTIONS.addOption("gf", "groupFile", true, "The path of group file");
		  OPTIONS.addOption("pr", "prune", false, "Whether to prune the results");

		  OPTIONS.addOption("mg", "mergeGlobal", false, "Merge global results");
		  OPTIONS.addOption("mf", "mergeFinalResults", false, "Merge final results");
		  OPTIONS.addOption("mf2", "mergeFinalResults", false, "Merge final results");
	  }
	

	private static void run(CommandLine cmd) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		if(!cmd.hasOption("tc") || !cmd.hasOption("i")){
			printHelp();
			return;
		}
		
		String className = cmd.getOptionValue("tc");
		GenericGraphTool graphTool = (GenericGraphTool) Class.forName(className).newInstance();
		
		if(!graphTool.verifyParameters(cmd)){
			printHelp();
			return;
		}
		long startTime = System.currentTimeMillis();
		graphTool.run(cmd);
		System.out.println("Runtime: "+ (System.currentTimeMillis() - startTime)+" ms");
	}

	private static void printHelp() {
	    HelpFormatter formatter = new HelpFormatter();
	    formatter.printHelp(GraphTools.class.getName(), OPTIONS, true);
		
	}

	private static void listTools() { 
		List<Class<?>> classes = AnnotationUtils.getAnnotatedClasses(
		      GraphAnalyticTool.class, "com.org.shark.graphtoolkits");
		    System.out.print("  Supported tools:\n");
		    for (Class<?> clazz : classes) {
		    	GraphAnalyticTool tool = clazz.getAnnotation(GraphAnalyticTool.class);
		        StringBuilder sb = new StringBuilder();
		        sb.append(tool.name()).append(" - ").append(clazz.getName())
		            .append("\n");
		        if (!tool.description().equals("")) {
		          sb.append("    ").append(tool.description()).append("\n");
		        }
		        System.out.print(sb.toString());
		    }
	}

	public static void main(String[] args) throws Exception{
		
		/* 1. parse the args */
	    CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(OPTIONS, args);
		
		if(cmd.hasOption("h")){
			printHelp();
			return ;
		}
		
		if(cmd.hasOption("lt")){
			listTools();
			return ;
		}

		/* 2. run the proper tool */
		run(cmd);
	}
}
