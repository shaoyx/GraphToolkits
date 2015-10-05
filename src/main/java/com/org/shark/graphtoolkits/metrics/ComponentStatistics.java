package com.org.shark.graphtoolkits.metrics;

import com.org.shark.graphtoolkits.GenericGraphTool;
import com.org.shark.graphtoolkits.utils.Edge;
import com.org.shark.graphtoolkits.utils.Graph;
import org.apache.commons.cli.CommandLine;

import com.org.shark.graphtoolkits.utils.GraphAnalyticTool;

import java.util.*;

/**
 * Created by yxshao on 10/5/15.
 */

@GraphAnalyticTool(
        name = "Graph Component Statistics",
        description = "Counting the Degree. format: (#vid degrees bigD smallD equalD bigL smallL)"
)
public class ComponentStatistics implements GenericGraphTool {

    private Graph graphData;
    private double threshold;

    public void run(CommandLine cmd) {
        graphData = new Graph(cmd.getOptionValue("i"));
        if(cmd.hasOption("th")) {
           this.threshold = Double.valueOf(cmd.getOptionValue("th"));
        }
        else {
           this.threshold = 20; //default is 20 matches
        }
        findComponents();
    }

    public boolean verifyParameters(CommandLine cmd) {
        return true;
    }

    private void findComponents() {
        Set<Integer> vertices = graphData.getVertexSet();
        Set<Integer> visited = new HashSet<Integer>();

        Iterator<Integer> iter = vertices.iterator();
        int curCCId = 0;
        int singlePoint = 0;
        while(iter.hasNext()) {
            Integer cur = iter.next();
            if(visited.contains(cur) == true) {
               continue;
            }
            int ccSize = searchSingleComponent(cur, visited);
            if(ccSize > 1) {
                System.out.println("CurCCId=" + curCCId + ": size=" + ccSize);
            }
            else {
               singlePoint++;
            }
        }
        System.out.println("SinglePoint=" + singlePoint);
    }

    /**
     * search using BFS, the simplest solution.
     * @param startId
     * @param visited
     * @return
     */
    private int searchSingleComponent(int startId, Set<Integer> visited) {
        Queue<Integer> queue = new LinkedList<Integer>();
        int size = 1;
        queue.add(startId);
        visited.add(new Integer(startId));

        while(queue.isEmpty() == false) {
           int vid = queue.peek();
            queue.remove();
            ArrayList<Edge> nbrs = graphData.getNeighbors(vid);
            for(int idx = 0; idx < nbrs.size(); idx++) {
               Edge e = nbrs.get(idx);
                //filter by visited or the weight is below threshold
                if(visited.contains(e.getId()) || e.getWeight() < this.threshold)
                    continue;
                visited.add(new Integer(e.getId()));
                queue.add(e.getId());
                size++;
            }
        }
        return size;
    }
}
