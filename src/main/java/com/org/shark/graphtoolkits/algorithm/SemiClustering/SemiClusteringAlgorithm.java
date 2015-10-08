package com.org.shark.graphtoolkits.algorithm.SemiClustering;

import com.org.shark.graphtoolkits.GenericGraphTool;
import com.org.shark.graphtoolkits.algorithm.SemiClustering.data.SemiClusterGraph;
import com.org.shark.graphtoolkits.algorithm.SemiClustering.data.SemiClusterVertex;
import com.org.shark.graphtoolkits.graph.Edge;
import com.org.shark.graphtoolkits.graph.Graph;
import com.org.shark.graphtoolkits.graph.Vertex;
import com.org.shark.graphtoolkits.utils.GraphAnalyticTool;
import org.apache.commons.cli.CommandLine;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * Created by yxshao on 10/7/15.
 */
@GraphAnalyticTool(
        name = "Semi-Clustering",
        description = "Clustering graph nodes"
)
public class SemiClusteringAlgorithm implements GenericGraphTool {
    private int semiClusterMaximumVertexCount;
    private int vertexMaxClusterCount;
    private int vertexMaxCandidateClusterCount;
    private int iterationLimitation;
    private double boundaryFactor;

    /* the raw graph data */
    private Graph graphData;

    private SemiClusterGraph semiClusterGraph;

    public SemiClusteringAlgorithm() {}

    @Override
    public void run(CommandLine cmd) {
        String gPath;
        gPath = cmd.getOptionValue("i");
        double th = 3.0;
        if(cmd.hasOption("th")) {
            th = Double.valueOf(cmd.getOptionValue("th"));
        }
        graphData = new Graph(gPath, th);
        this.semiClusterMaximumVertexCount = 10;
        this.vertexMaxClusterCount = 5;
        this.vertexMaxCandidateClusterCount = 5;
        this.iterationLimitation = 20;
        this.boundaryFactor = 0;

        if(cmd.hasOption("iter")) {
            this.iterationLimitation = Integer.valueOf(cmd.getOptionValue("iter"));
        }

        if(cmd.hasOption("cSize")) {
            this.semiClusterMaximumVertexCount = Integer.valueOf(cmd.getOptionValue("cSize"));
        }

        if(cmd.hasOption("vcSize")) {
            this.vertexMaxClusterCount = Integer.valueOf(cmd.getOptionValue("vcSize"));
        }

        if(cmd.hasOption("vccSize")) {
            this.vertexMaxCandidateClusterCount = Integer.valueOf(cmd.getOptionValue("vccSize"));
        }

        if(cmd.hasOption("fb")) {
            this.boundaryFactor = Double.valueOf(cmd.getOptionValue("fb"));
        }

        computeSemiClusters();

        saveResults(gPath+".clusters");
    }

    @Override
    public boolean verifyParameters(CommandLine cmd) {
        return true;
    }

    private void computeSemiClusters() {
        initialCluster();
        for(int i = 1; i <= this.iterationLimitation; i++) {
            System.out.println("Iteration "+i);
            if(!updateCluster()) {
                break;
            }
        }
    }

    private void initialCluster() {
        semiClusterGraph = new SemiClusterGraph();

        for(Integer vid : this.graphData.getVertexSet().keySet()) {
            SemiClusterVertex scVertex = new SemiClusterVertex();

            List<Integer> lV = new ArrayList<Integer>();
            lV.add(vid);
            String newClusterName = "C" + createNewSemiClusterName(lV);
            SemiClusterInfo initialClusters = new SemiClusterInfo();
            initialClusters.setSemiClusterId(newClusterName);
            initialClusters.addVertexList(lV);
            initialClusters.setScore(1);

            ArrayList<SemiClusterInfo> scInfoArrayList = new ArrayList<SemiClusterInfo>();
            scInfoArrayList.add(initialClusters);

            Set<SemiClusterDetails> scList = new TreeSet<SemiClusterDetails>();
            scList.add(new SemiClusterDetails(newClusterName, 1.0));
            SemiClusterInfo vertexValue = new SemiClusterInfo();
            vertexValue.setSemiClusterContainThis(scList);

            scVertex.setVid(vid);
            scVertex.setPreCandidateSemiClusters(scInfoArrayList);
            scVertex.setVertexClusterInfo(vertexValue);

            semiClusterGraph.addSemiClusterVertex(scVertex);
        }
    }

    /**
     * update current clusters
     * @return true: the cluster is updated; false: the cluster is not updated.
     */
    private boolean updateCluster() {
        for(Integer vid : this.graphData.getVertexSet().keySet()) {

            Vertex curV = graphData.getVertexById(vid);
            ArrayList<Edge> curVNbrs = graphData.getNeighbors(vid);
//            System.out.println(semiClusterGraph+" ");
            SemiClusterVertex curSCVertex = semiClusterGraph.getSemiClusterVertex(vid);

            TreeSet<SemiClusterInfo> candidates = new TreeSet<SemiClusterInfo>();

            for(Edge e : curVNbrs) {
                SemiClusterVertex nbrSCVertex = semiClusterGraph.getSemiClusterVertex(e.getId());
                ArrayList<SemiClusterInfo> preSemiClusterInfo = nbrSCVertex.getPreCandidateSemiClusters();

                for (SemiClusterInfo msg : preSemiClusterInfo) {
                    candidates.add(msg);

                    if (!msg.contains(vid)
                            && msg.size() < semiClusterMaximumVertexCount) {
                        SemiClusterInfo msgNew = msg.copy();
                        msgNew.addVertex(vid);
                        msgNew.setSemiClusterId("C"
                                + createNewSemiClusterName(msgNew.getVertexList()));
                        msgNew.setScore(semiClusterScoreCalcuation(msgNew));

                        candidates.add(msgNew);
                    }
                }

            }

            Iterator<SemiClusterInfo> bestCandidates = candidates.descendingIterator();
            int count = 0;

            ArrayList<SemiClusterInfo> curSemiClusterInfo = new ArrayList<SemiClusterInfo>();
            while (bestCandidates.hasNext() && count < vertexMaxCandidateClusterCount) {
                SemiClusterInfo candidate = bestCandidates.next();
//                System.out.println("Score of Best candidates: " + candidate.getScore());
                curSemiClusterInfo.add(candidate);
                count++;
            }
            curSCVertex.setCurCandidateSemiClusters(curSemiClusterInfo);

            // Update candidates
            SemiClusterInfo value = curSCVertex.getVertexClusterInfo();
            bestCandidates = candidates.descendingIterator();
            Set<SemiClusterDetails> clusters = value.getSemiClusterContainThis();
            while(bestCandidates.hasNext()) {
                SemiClusterInfo msg = bestCandidates.next();
                if(!msg.contains(vid)) continue;;
//                System.out.println("Vid=" + vid + " SemiClusterInfo: "+ msg.toString());
                if (clusters.size() > vertexMaxClusterCount) {
                    break;
                } else {
                    clusters.add(new SemiClusterDetails(msg.getSemiClusterId(), msg
                            .getScore()));
                }
            }
            value.setClusters(clusters, vertexMaxClusterCount);
            curSCVertex.setVertexClusterInfo(value);
        }
        //iterative
        for(Integer vid : this.graphData.getVertexSet().keySet()) {
            SemiClusterVertex curSCVertex = semiClusterGraph.getSemiClusterVertex(vid);
            curSCVertex.setPreCandidateSemiClusters(curSCVertex.getCurCandidateSemiClusters());
        }
        return true;
    }

    public void saveResults(String savePath) {
        try{
            FileOutputStream fout = new FileOutputStream(savePath);
            BufferedWriter fwr = new BufferedWriter(new OutputStreamWriter(fout));
            for(int vid : graphData.getVertexSet().keySet()) {
                SemiClusterVertex scVertex = semiClusterGraph.getSemiClusterVertex(vid);
                SemiClusterInfo scInfo = scVertex.getVertexClusterInfo();
                Set<SemiClusterDetails> scContainVid = scInfo.getSemiClusterContainThis();

                for(SemiClusterDetails scd : scContainVid) {

                    StringBuffer sb = new StringBuffer();
                    sb.append(vid);
                    sb.append(" ");
                    sb.append(scd.getSemiClusterId());
                    sb.append(" ");
                    sb.append(scd.getSemiClusterScore());
                    sb.append("\n");

                    fwr.write(sb.toString());
                }
            }
            fwr.flush();
            fwr.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * This function create a new Semi-cluster ID for a semi-cluster from the list
     * of vertices in the cluster.It first take all the vertexIds as a list sort
     * the list and then find the HashCode of the Sorted List.
     */
    private int createNewSemiClusterName(List<Integer> semiClusterVertexList) {
        List<String> vertexIDList = new ArrayList<String>();
        for (Integer vid : semiClusterVertexList) {
            vertexIDList.add(String.valueOf(vid));
        }
        Collections.sort(vertexIDList);
        return (vertexIDList.hashCode());
    }

    public double semiClusterScoreCalcuation(SemiClusterInfo message) {
        double iC = 0.0, bC = 0.0, fB = this.boundaryFactor, sC = 0.0;
        int vC = 0, eC = 0;
        vC = message.size();
        for (Integer vid : message.getVertexList()) {

            ArrayList<Edge> vnbrs = graphData.getNeighbors(vid);

            for(Edge e : vnbrs) {
                int tid = e.getId();
                double weight = e.getWeight();
                eC++;
                if (message.contains(tid) && weight > 0.0) { //TODO: fake weight justification
                    iC = iC + weight;
                } else if (weight > 0.0) {
                    bC = bC + weight;
                }
            }
        }
        if (vC > 1)
            sC = ((iC - fB * bC) / ((vC * (vC - 1)) / 2));// / eC;
        return sC;
    }
}
