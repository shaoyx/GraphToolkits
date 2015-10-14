package com.org.shark.graphtoolkits.metrics;

import com.org.shark.graphtoolkits.GenericGraphTool;
import com.org.shark.graphtoolkits.graph.Edge;
import com.org.shark.graphtoolkits.graph.Graph;
import com.org.shark.graphtoolkits.graph.Vertex;
import org.apache.commons.cli.CommandLine;

import com.org.shark.graphtoolkits.utils.GraphAnalyticTool;
import org.apache.commons.cli.Options;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * Created by yxshao on 10/5/15.
 */

@GraphAnalyticTool(
        name = "Graph Component Statistics",
        description = ""
)
public class ComponentStatistics implements GenericGraphTool {

    private Graph graphData;
    private double threshold;
    private int startVertex;

    @Override
    public void registerOptions(Options options) {
        //two-hop algorithm
        options.addOption("sv", "startVertex", true, "The start vertex for 2-hop search");
    }

    public void run(CommandLine cmd) {
        if(cmd.hasOption("th")) {
            this.threshold = Double.valueOf(cmd.getOptionValue("th"));
        }
        else {
            this.threshold = 3; //default is 20 matches
        }

        graphData = new Graph(cmd.getOptionValue("i"), this.threshold);

        if(cmd.hasOption("sv")) {
            this.startVertex = Integer.valueOf(cmd.getOptionValue("sv"));
        }
        else {
            this.startVertex = -1;
        }
//        System.out.println("threshold="+this.threshold);
        if(this.startVertex != -1) {
            twoHopSearch(this.startVertex);
        }
        else {
            findComponents();
        }
    }

    public boolean verifyParameters(CommandLine cmd) {
        return true;
    }

    private void twoHopSearch(int sVertex) {

        try{
            FileOutputStream fout = new FileOutputStream("search_path_"+sVertex);
            BufferedWriter fwr = new BufferedWriter(new OutputStreamWriter(fout));
        Queue<Integer> queue = new LinkedList<Integer>();
        Set<Integer> visited = new HashSet<Integer>();
        ArrayList<Integer> cc = new ArrayList<Integer>();
        HashMap<Integer, Vertex> vertexSet = graphData.getVertexSet();
        HashMap<Integer, Integer> dist = new HashMap<Integer, Integer>();

        int size = 1;
        queue.add(sVertex);
        visited.add(sVertex);
        cc.add(sVertex);
        dist.put(sVertex, 0);
        while(!queue.isEmpty()) {
            int vid = queue.peek();
            int distance = dist.get(vid);
            queue.remove();
            Vertex vertex = vertexSet.get(vid);
            ArrayList<Edge> nbrs = graphData.getNeighbors(vid);
            if(nbrs == null) continue;
            for(int idx = 0; idx < nbrs.size(); idx++) {
                Edge e = nbrs.get(idx);

                StringBuffer sb = new StringBuffer();
                sb.append(vid);
                sb.append(" ");
                sb.append(e.getId());
                sb.append(" ");
                sb.append(e.getWeight());
                sb.append("\n");
                fwr.write(sb.toString());

                //filter by visited or the weight is below threshold
                if(visited.contains(e.getId()))
                    continue;
                if(distance + 1 < 3) {
                    visited.add(e.getId());
                    queue.add(e.getId());
                    dist.put(e.getId(), distance + 1);
                    cc.add(e.getId());
                    size++;
                }
            }
        }
            fwr.flush();
            fwr.close();
            saveComponent(sVertex, cc);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void findComponents() {
        HashMap<Integer, Vertex> vertices = graphData.getVertexSet();
        Set<Integer> visited = new HashSet<Integer>();

        Iterator<Integer> iter = vertices.keySet().iterator();
        int curCCId = 0;
        int singlePoint = 0;
        while(iter.hasNext()) {
            Integer cur = iter.next();
            if(visited.contains(cur)) {
               continue;
            }
            ArrayList<Integer> cc = new ArrayList<Integer>();
            int ccSize = searchSingleComponent(cur, visited, vertices, cc);
            if(ccSize > 1) {
                curCCId++;
                System.out.println("CurCCId=" + curCCId + ": size=" + ccSize);
                saveComponent(curCCId, cc);
            }
            else {
               singlePoint++;
            }
        }
        System.out.println("SinglePoint=" + singlePoint);
    }

    /**
     * search using BFS, the simplest solution.
     * -- threshold rules:
     *    1. the vertex weight (matches) greater than th;
     *    2. the edge weight should be greater than the average;
     * @param startId
     * @param visited
     * @return
     */
    private int searchSingleComponent(int startId, Set<Integer> visited, HashMap<Integer, Vertex> vertexSet, ArrayList<Integer> cc) {
        Queue<Integer> queue = new LinkedList<Integer>();
        int size = 1;
        queue.add(startId);
        visited.add(startId);
        cc.add(startId);
        if(vertexSet.get(startId).getWeight() < this.threshold + 1e-6) {
            return size;
        }
        while(!queue.isEmpty()) {
           int vid = queue.peek();
            queue.remove();
            Vertex vertex = vertexSet.get(vid);
            ArrayList<Edge> nbrs = graphData.getNeighbors(vid);
            double average_weight = vertex.getWeight() / nbrs.size();
            for(int idx = 0; idx < nbrs.size(); idx++) {
                Edge e = nbrs.get(idx);
                //filter by visited or the weight is below threshold
                if(visited.contains(e.getId()) ||
                        e.getWeight() < average_weight + 1e-6 || //filter by average weight under random model;
                        vertexSet.get(e.getId()).getWeight() < this.threshold + 1e-6) //filter by vertex threshold;
                    continue;
                visited.add(e.getId());
                queue.add(e.getId());
                cc.add(e.getId());
                size++;
            }
        }
        return size;
    }

    private void saveComponent(Integer ccId, ArrayList<Integer> cc) {
        try{
            FileOutputStream fout = new FileOutputStream("foundCC/bfs_ccId_"+ccId+"_size_"+cc.size());
            BufferedWriter fwr = new BufferedWriter(new OutputStreamWriter(fout));
            for(int vid : cc) {
                StringBuffer sb = new StringBuffer();
                sb.append(vid);
                sb.append("\n");
                fwr.write(sb.toString());
            }
            fwr.flush();
            fwr.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
