package com.org.shark.graphtoolkits.graph;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class Graph {
	protected static final Pattern SEPERATOR =  Pattern.compile("[\t ]");

	private HashMap<Integer, Vertex> vertexSet;
	private HashMap<Integer, Integer> degreeList;
	private HashMap<Integer, ArrayList<Edge>> edgeList;
	private int edgeSize;
	private int vertexSize;

    private double edgeWeightThreshold;
	
	public Graph(String graphFilePath){
		vertexSet = new HashMap<Integer, Vertex>();
		degreeList = new HashMap<Integer,Integer>();
		edgeList = new HashMap<Integer, ArrayList<Edge>>();
		edgeSize = 0;
		vertexSize = 0;
		loadGraphFromEdge(graphFilePath);
	}

    public Graph(String graphFilePath, double threshold){
        vertexSet = new HashMap<Integer, Vertex>();
        degreeList = new HashMap<Integer,Integer>();
        edgeList = new HashMap<Integer, ArrayList<Edge>>();
        edgeSize = 0;
        vertexSize = 0;
        this.edgeWeightThreshold = threshold;
        loadGraphFromEdge(graphFilePath);
    }

	public Graph(String graphFilePath, boolean isEdge){
		vertexSet = new HashMap<Integer, Vertex>();
		degreeList = new HashMap<Integer,Integer>();
		edgeList = new HashMap<Integer, ArrayList<Edge>>();
		edgeSize = 0;
		vertexSize = 0;
		if(isEdge)
			loadGraphFromEdge(graphFilePath);
		else
			loadGraph(graphFilePath);
	}
	
	public int getDegree(int vid){
		return degreeList.get(vid);
	}
	
	public HashMap<Integer, Vertex> getVertexSet(){
		return vertexSet;
	}
	
	public int getVertexSize(){
		return vertexSize;
	}
	
	public int getEdgeSize(){
		return edgeSize;
	}

	public Vertex getVertexById(int vid) {
		return vertexSet.get(vid);
	}
	
	public ArrayList<Edge> getNeighbors(int vid){
		return edgeList.get(vid);
	}
	
	private void loadGraphFromEdge(String graphFilePath){
		try {
			FileInputStream fin = new FileInputStream(graphFilePath);
			BufferedReader fbr = new BufferedReader(new InputStreamReader(fin));
			String line;
            int lineNum = 0;
            int filterEdgeNum = 0;
			while((line = fbr.readLine()) != null){
                lineNum++;
				if(line.startsWith("#")) continue;
				String [] values = SEPERATOR.split(line);
				if(values.length < 3) {
                    System.out.println("Edge Format Required. Error Line: " + line + ". parsed value size = " + values.length);
                    continue;
                }

                for(int i = 0; i < 3; i++) {
                    if(values[i] == null || values[i].length() == 0) {
                       System.out.println("Error line " + lineNum + ": " + line);
                    }
                }

				int sv = Integer.valueOf(values[0]);
				int ev = Integer.valueOf(values[1]);
				double ew = Double.valueOf(values[2]);

                if(ew < this.edgeWeightThreshold) { // edge weight filter
                    filterEdgeNum++;
                    continue;
                }

				if(!vertexSet.containsKey(sv)){
					vertexSize++;
					vertexSet.put(sv, new Vertex(sv, 0));
					degreeList.put(sv, 0);
					edgeList.put(sv, new ArrayList<Edge>());
				}
				if(!vertexSet.containsKey(ev)){
					vertexSize++;
                    vertexSet.put(ev, new Vertex(ev, 0));
					degreeList.put(ev, 0);
					edgeList.put(ev, new ArrayList<Edge>());
				}
				degreeList.put(sv, degreeList.get(sv)+1);
				/* loop */
				if(sv == ev){
					degreeList.put(sv, degreeList.get(sv)+1);
					edgeSize++;
				}

				edgeList.get(sv).add(new Edge(ev, ew));
                Vertex vertex= vertexSet.get(sv);
                vertex.setWeight(vertex.getWeight() + ew);
				edgeSize ++;
			}
			int degreeSum = 0;
			for(int vid: degreeList.keySet()){
				degreeSum += degreeList.get(vid);
			}
			System.out.println("Vertex="+vertexSize+" Edge="+edgeSize +" degreeSum="+degreeSum+" filterEdgeNum="+filterEdgeNum);
            saveGraph();
			fbr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

    private void saveGraph() {
       try{
           FileOutputStream fout = new FileOutputStream("weight_graph_reforme");
           BufferedWriter fwr = new BufferedWriter(new OutputStreamWriter(fout));
           fwr.write("Vertex List\n");
           for(int vid : vertexSet.keySet()) {
              Vertex v = vertexSet.get(vid);
               StringBuffer sb = new StringBuffer();
               sb.append(v.getVid());
               sb.append(" ");
//               sb.append(this.degreeList.get(v.getVid()));
//               sb.append(" ");
               sb.append(v.getWeight());
//               sb.append(" ");

			   ArrayList<Edge> nbrs = edgeList.get(vid);
			   for(Edge edge : nbrs) {
//				   StringBuffer sb = new StringBuffer();
//				   sb.append(vid);
				   sb.append(" ");
				   sb.append(edge.getId());
				   sb.append(" ");
				   sb.append(edge.getWeight());
//				   sb.append("\n");
			   }
			   sb.append("\n");

               fwr.write(sb.toString());
           }
//           fwr.write("Edge List\n");
//           for(int vid : edgeList.keySet()) {
//               ArrayList<Edge> nbrs = edgeList.get(vid);
//               for(Edge edge : nbrs) {
//                   StringBuffer sb = new StringBuffer();
//                   sb.append(vid);
//                   sb.append(" ");
//                   sb.append(edge.getId());
//                   sb.append(" ");
//                   sb.append(edge.getWeight());
//                   sb.append("\n");
//                   fwr.write(sb.toString());
//               }
//           }
           fwr.flush();
           fwr.close();
       }catch (Exception e){
           e.printStackTrace();
        }
    }
	
	/**
	 * This is load from adjacency format
	 * @param graphFilePath input graph path
	 */
	private void loadGraph(String graphFilePath){
		try {
			FileInputStream fin = new FileInputStream(graphFilePath);
			BufferedReader fbr = new BufferedReader(new InputStreamReader(fin));
			String line;
			while((line = fbr.readLine()) != null){
				String [] values = SEPERATOR.split(line);
				int vid = Integer.valueOf(values[0]);
				ArrayList<Edge> al = new ArrayList<Edge>();
				for(int i = 1; i < values.length; ++i){
					al.add(new Edge(Integer.valueOf(values[i]), 1.0));
				}
				vertexSet.put(vid, new Vertex(vid, 0));
				degreeList.put(vid, values.length - 1);
				edgeList.put(vid, al);
				vertexSize++;
				edgeSize += values.length - 1;
			}
			System.out.println("Vertex="+vertexSize+" Edge="+edgeSize);
			fbr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
