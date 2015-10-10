package com.org.shark.graphtoolkits.algorithm.centrality;

import com.org.shark.graphtoolkits.GenericGraphTool;
import com.org.shark.graphtoolkits.graph.Edge;
import com.org.shark.graphtoolkits.graph.Graph;
import com.org.shark.graphtoolkits.graph.Vertex;
import com.org.shark.graphtoolkits.utils.GraphAnalyticTool;
import org.apache.commons.cli.CommandLine;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by yxshao on 10/9/15.
 */
@GraphAnalyticTool(
        name = "Closeness",
        description = "Compute cloessness"
)
public class Closeness implements GenericGraphTool {
    protected static final Pattern SEPERATOR =  Pattern.compile("[\t ]");

    private Graph graphData;

    public Closeness() {}

    public void computeClosenessForVertexSet(int gid, HashSet<Integer> vertexIdSets) {

//        System.out.println("Size="+vertexIdSets.size());

        if(vertexIdSets.size() <= 10) return;

        TreeSet<Vertex> centralitySet = new TreeSet<Vertex>();
        for(int vid : vertexIdSets) {
            double centrality = computeSingleVertex(gid, vid, vertexIdSets);
            centralitySet.add(new Vertex(vid, centrality));
        }
//        System.out.println(centralitySet.size());
//        System.out.println("original set size="+centralitySet.size() + ": "+centralitySet);
        int clusterCountToBeRemoved = 0;
        NavigableSet<Vertex> setSort = new TreeSet<Vertex>(
                new Comparator<Vertex>() {
                    @Override
                    public int compare(Vertex o1, Vertex o2) {
                        if(o1.getWeight() == o2.getWeight()) {
                           return o1.getVid() < o2.getVid() ? -1 : 1;
                        }
                        else
                            return (o1.getWeight() == o2.getWeight() ? 0
                                : o1.getWeight() > o2.getWeight() ? -1 : 1);
                    }
                });
        setSort.addAll(centralitySet);
//        System.out.println("set size="+setSort.size() + ": "+setSort);
        clusterCountToBeRemoved = setSort.size() - 10;
        Iterator<Vertex> itr = setSort.descendingIterator();
//        System.out.println("Remove size="+clusterCountToBeRemoved);
        while (clusterCountToBeRemoved > 0) {
            Vertex removedV = itr.next();
//            System.out.println("remove "+removedV.getVid()+" "+vertexIdSets.remove(removedV.getVid()));
            vertexIdSets.remove(removedV.getVid());
            itr.remove();
            clusterCountToBeRemoved--;
        }
//        System.out.println(vertexIdSets);
    }

    public double computeSingleVertex(int gid, int vid, HashSet<Integer> vertexIdSets) {
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
//                System.out.println("neighbor="+e.getId());
                //filter by visited or the weight is below threshold
                if (visited.contains(e.getId()) || !vertexIdSets.contains(e.getId()))
                    continue;

                if(distance + 1 > 2 && gid != vid) { //do not increase the penalty for vid.
                    result += 10000;
                }
                else {
                    result += distance + 1;
                }
                visited.add(e.getId());
                queue.add(e.getId());
                dist.put(e.getId(), distance + 1);
            }
        }
//        System.out.println(vid+": score="+result);
        return result / (vertexIdSets.size()  - 1);
    }

    @Override
    public void run(CommandLine cmd) {
        String gPath;
        gPath = cmd.getOptionValue("i");
        double th = 3.0;
        if(cmd.hasOption("th")) {
            th = Double.valueOf(cmd.getOptionValue("th"));
        }
        graphData = new Graph(gPath, th);

        loadGroupAndRefine(cmd.getOptionValue("gf"));
    }

    @Override
    public boolean verifyParameters(CommandLine cmd) {
        return (cmd.hasOption("gf"));
    }

    private void loadGroupAndRefine(String gpath) {
        try {
            FileInputStream fin = new FileInputStream(gpath);
            BufferedReader fbr = new BufferedReader(new InputStreamReader(fin));
            FileOutputStream fout = new FileOutputStream(gpath + ".clean");
            BufferedWriter fwr = new BufferedWriter(new OutputStreamWriter(fout));

            String line;
            while((line = fbr.readLine()) != null) {
                if (line.startsWith("#")) continue;
                String[] values = SEPERATOR.split(line);

                String vid = values[0];
                int gid = Integer.valueOf(values[0].substring(0, values[0].indexOf(":")));
                HashSet<Integer> gList = new HashSet<Integer>();
                for (int i = 1; i < values.length; i++) {
                    int sv = Integer.valueOf(values[i]);
                    gList.add(sv);
                }
//                System.out.println(vid + " " +gList);
                this.computeClosenessForVertexSet(gid, gList);
//                System.out.println(vid + "cleaned: " +gList);

                StringBuilder sb = new StringBuilder();
                sb.append(vid);
                for(Integer scd : gList) {
                    sb.append(" ");
                    sb.append(scd);
                }
                sb.append("\n");
                fwr.write(sb.toString());
            }
            fbr.close();
            fwr.flush();
            fwr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
