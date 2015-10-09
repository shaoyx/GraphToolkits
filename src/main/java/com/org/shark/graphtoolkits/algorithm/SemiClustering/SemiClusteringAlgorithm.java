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

        System.out.println("iter="+this.iterationLimitation+
                "\ncSize="+this.semiClusterMaximumVertexCount +
                "\nvcSize="+this.vertexMaxClusterCount+
                "\nvccSize="+this.vertexMaxCandidateClusterCount+
                "\nboundaryFactor="+this.boundaryFactor);


//        Set<SemiClusterInfo> sets = new TreeSet<SemiClusterInfo>();
//        SemiClusterInfo scInfo1 = new SemiClusterInfo();
//        scInfo1.setSemiClusterId("C1");
//        scInfo1.setScore(1.0);
//        System.out.println(sets.add(scInfo1));
////        scInfo1.setSemiClusterId("C2");
//
//        SemiClusterInfo scInfo2 = scInfo1.copy();
//        scInfo2.setSemiClusterId("C1");
//        scInfo2.setScore(2.0);
//        System.out.println("scInfo2="+scInfo2);
//        System.out.println(sets.add(scInfo2));
//
//        for(SemiClusterInfo info : sets) {
//            System.out.println(info);
//        }

        computeSemiClusters();

        saveResults(gPath + ".clusters");
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
            initialClusters.setScore(0);

            ArrayList<SemiClusterInfo> scInfoArrayList = new ArrayList<SemiClusterInfo>();
            scInfoArrayList.add(initialClusters);

            Set<SemiClusterInfo> scList = new TreeSet<SemiClusterInfo>();
            SemiClusterInfo initialClusters2 = new SemiClusterInfo();
            initialClusters2.setSemiClusterId(newClusterName);
            initialClusters2.addVertexList(lV);
            initialClusters2.setScore(0);
            scList.add(initialClusters2);

            scVertex.setVid(vid);
            scVertex.setPreCandidateSemiClusters(scInfoArrayList);
            scVertex.setVertexClusterContainThis(scList);

            if(vid == 136176934) {
                System.out.println("vid "+vid+": initial cluster ==> " + scVertex.getVertexClusterContainThis());
            }

            semiClusterGraph.addSemiClusterVertex(scVertex);
        }
    }

    /**
     * update current clusters
     * @return true: the cluster is updated; false: the cluster is not updated.
     */
    private boolean updateCluster() {
        boolean hasUpdated = false;
        for(Integer vid : this.graphData.getVertexSet().keySet()) {

            Vertex curV = graphData.getVertexById(vid);
            ArrayList<Edge> curVNbrs = graphData.getNeighbors(vid);
            SemiClusterVertex curSCVertex = semiClusterGraph.getSemiClusterVertex(vid);

            TreeSet<SemiClusterInfo> candidates = new TreeSet<SemiClusterInfo>();

            for(Edge e : curVNbrs) {
                SemiClusterVertex nbrSCVertex = semiClusterGraph.getSemiClusterVertex(e.getId());
                ArrayList<SemiClusterInfo> preSemiClusterInfo = nbrSCVertex.getPreCandidateSemiClusters();

                for (SemiClusterInfo msg : preSemiClusterInfo) {
                    candidates.add(msg);

                    if (!msg.contains(vid)
                            && msg.size() < semiClusterMaximumVertexCount
                            && connectivityValidation(vid, msg.getVertexList())) { //check connectivity
                        SemiClusterInfo msgNew = msg.copy();
                        msgNew.addVertex(vid);
                        msgNew.setSemiClusterId("C" + createNewSemiClusterName(msgNew.getVertexList()));
                        msgNew.setScore(semiClusterScoreCalcuation(msgNew));
                        candidates.add(msgNew);
                        hasUpdated = true;
                    }
                }
            }

//            String preNamce = "";
//            for(SemiClusterInfo scInfo : candidates) {
//                System.out.println(scInfo);
//                if(scInfo.getSemiClusterId().equals(preNamce)) {
//                    System.err.println("Error");
//                    System.exit(0);
//                }
//                preNamce = scInfo.getSemiClusterId();
//            }
//            System.out.println();

            Iterator<SemiClusterInfo> bestCandidates = candidates.descendingIterator();
            int count = 0;

            ArrayList<SemiClusterInfo> curSemiClusterInfo = new ArrayList<SemiClusterInfo>();
            while (bestCandidates.hasNext() && count < vertexMaxCandidateClusterCount) {
                SemiClusterInfo candidate = bestCandidates.next();
                curSemiClusterInfo.add(candidate);
                count++;
            }
            curSCVertex.setCurCandidateSemiClusters(curSemiClusterInfo);

            // Update candidates
            Set<SemiClusterInfo> clusters = curSCVertex.getVertexClusterContainThis();
            System.out.println("Update clusters: ");
            String name = "";
            for(SemiClusterInfo info : clusters) {
                System.out.println("before="+info);
                if(info.getSemiClusterId().equals(name)) {
                    System.err.println("before Error");
                    System.exit(0);
                }
                name = info.getSemiClusterId();
            }
            bestCandidates = candidates.descendingIterator();
            while(bestCandidates.hasNext()) {
                SemiClusterInfo msg = bestCandidates.next();
                if(!msg.contains(vid))
                    continue;
                if(vid == 136176934) {
                    System.out.println("update semiCluster: Vid=" + vid + " SemiClusterInfo: "+ msg.toString());
                }
                hasUpdated = true;
                SemiClusterInfo newCluster = msg.copy();
//                newCluster.addVertex(vid);
                newCluster.setSemiClusterId(msg.getSemiClusterId()); //"C" + createNewSemiClusterName(newCluster.getVertexList()));
                newCluster.setScore(msg.getScore());
                System.out.println("add new: " + newCluster);
                clusters.add(newCluster);
            }
            name = "";
            for(SemiClusterInfo info : clusters) {
                System.out.println("mid="+info);
                if(info.getSemiClusterId().equals(name)) {
                System.err.println("mid Error");
                System.exit(0);
            }
            name = info.getSemiClusterId();
            }

            clusters = cleanNewClusters(clusters, vertexMaxClusterCount);
            name = "";
            for(SemiClusterInfo info : clusters) {
                System.out.println("after="+info);
                if(info.getSemiClusterId().equals(name)) {
                    System.err.println("after Error");
                    System.exit(0);
                }
                name = info.getSemiClusterId();
            }
//            System.out.println("after clean clusters size="+clusters.size());
            curSCVertex.setVertexClusterContainThis(clusters);
        }
        //iterative
        for(Integer vid : this.graphData.getVertexSet().keySet()) {
            SemiClusterVertex curSCVertex = semiClusterGraph.getSemiClusterVertex(vid);
            curSCVertex.setPreCandidateSemiClusters(curSCVertex.getCurCandidateSemiClusters());
        }
        return hasUpdated;
    }

    // common functions
    private Set<SemiClusterInfo> cleanNewClusters(Set<SemiClusterInfo> clusters, int limitation) {
        int clusterCountToBeRemoved = 0;
        NavigableSet<SemiClusterInfo> setSort = new TreeSet<SemiClusterInfo>(
                new Comparator<SemiClusterInfo>() {
                    @Override
                    public int compare(SemiClusterInfo o1, SemiClusterInfo o2) {
                        return (o1.getScore() == o2.getScore() ? 0
                                : o1.getScore() > o2.getScore() ? -1 : 1);
                    }
                });
        setSort.addAll(clusters);
        clusterCountToBeRemoved = setSort.size() - limitation;
        Iterator<SemiClusterInfo> itr = setSort.descendingIterator();
        while (clusterCountToBeRemoved > 0) {
            itr.next();
            itr.remove();
            clusterCountToBeRemoved--;
        }
        return setSort;
    }

    private boolean connectivityValidation(int vid, List<Integer> candidateCluster) {
        ArrayList<Edge>  nbrs = graphData.getNeighbors(vid);
        HashSet<Integer> tSets = new HashSet<Integer>();
        for(Edge e : nbrs) {
            tSets.add(e.getId());
        }
        for(int cv : candidateCluster) {
            if(tSets.contains(cv)) {
                return true;
            }
        }
        return false;
    }

    public void saveResults(String savePath) {
        try{
            FileOutputStream fout = new FileOutputStream(savePath);
            BufferedWriter fwr = new BufferedWriter(new OutputStreamWriter(fout));
            for(int vid : graphData.getVertexSet().keySet()) {
                SemiClusterVertex scVertex = semiClusterGraph.getSemiClusterVertex(vid);
                Set<SemiClusterInfo> scInfos = scVertex.getVertexClusterContainThis();
                for(SemiClusterInfo scd : scInfos) {

                    StringBuffer sb = new StringBuffer();
                    sb.append(vid);
                    sb.append(" ");
                    sb.append(scd.getSemiClusterId());
                    sb.append(" ");
                    sb.append(scd.getVertexList());
                    sb.append(" ");
                    sb.append(scd.getScore());
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
            Vertex curVertex = graphData.getVertexSet().get(vid);

            for(Edge e : vnbrs) {
                int tid = e.getId();
                Vertex targetVertex = graphData.getVertexSet().get(tid);
                double weight = e.getWeight() / Math.max(curVertex.getWeight(), targetVertex.getWeight());
                eC += e.getWeight();
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
