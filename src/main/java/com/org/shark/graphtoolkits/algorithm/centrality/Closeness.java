package com.org.shark.graphtoolkits.algorithm.centrality;

import com.org.shark.graphtoolkits.algorithm.SemiClustering.SemiClusterInfo;
import com.org.shark.graphtoolkits.graph.Edge;
import com.org.shark.graphtoolkits.graph.Graph;
import com.org.shark.graphtoolkits.graph.Vertex;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * Created by yxshao on 10/9/15.
 */
public class Closeness {

    private Graph graphData;

    public Closeness() {}

    public void computeClosenessForVertexSet(HashSet<Integer> vertexIdSets) {
        TreeSet<Vertex> centralitySet = new TreeSet<Vertex>(
                new Comparator<Vertex>() {
                    @Override
                    public int compare(Vertex o1, Vertex o2) {
                        return (o1.getWeight() == o2.getWeight() ? 0
                                : o1.getWeight() > o2.getWeight() ? -1 : 1);
                    }
                });
        for(int vid : vertexIdSets) {
            double centrality = computeSingleVertex(vid, vertexIdSets);
            centralitySet.add(new Vertex(vid, centrality));
        }
    }

    public double computeSingleVertex(int vid, HashSet<Integer> vertexIdSets) {
        if(vertexIdSets.size() <= 1) return 0.0;
        double result = 0.0;
        Queue<Integer> queue = new LinkedList<Integer>();
        Set<Integer> visited = new HashSet<Integer>();
        HashMap<Integer, Integer> dist = new HashMap<Integer, Integer>();

        queue.add(vid);
        visited.add(vid);
        dist.put(vid, 0);

        while(!queue.isEmpty()) {
            int curId = queue.peek();
            int distance = dist.get(curId);
            queue.remove();
            ArrayList<Edge> nbrs = graphData.getNeighbors(vid);
            if (nbrs == null) continue;

            for (int idx = 0; idx < nbrs.size(); idx++) {
                Edge e = nbrs.get(idx);

                //filter by visited or the weight is below threshold
                if (visited.contains(e.getId()) || !vertexIdSets.contains(e.getId()))
                    continue;

                result += distance + 1;
                visited.add(e.getId());
                queue.add(e.getId());
                dist.put(e.getId(), distance + 1);
            }
        }
        return result / (vertexIdSets.size()  - 1);
    }
}
