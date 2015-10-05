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
		    OPTIONS.addOption("tc", "toolClass", true, "Specifiy the tool class");
		    OPTIONS.addOption("i", "input", true, "Path of the input graph");
		    
		  }
	

	private static void run(CommandLine cmd) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		if(cmd.hasOption("tc") == false || cmd.hasOption("i") == false){
			printHelp();
			return;
		}
		
		String className = cmd.getOptionValue("tc");
		GenericGraphTool graphTool = (GenericGraphTool) Class.forName(className).newInstance();
		
		if(graphTool.verifyParameters(cmd) == false){
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
