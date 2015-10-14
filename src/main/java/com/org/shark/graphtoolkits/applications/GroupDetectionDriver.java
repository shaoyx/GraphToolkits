package com.org.shark.graphtoolkits.applications;

import com.org.shark.graphtoolkits.GenericGraphTool;
import com.org.shark.graphtoolkits.algorithm.SemiClustering.SemiClusteringAlgorithm;
import com.org.shark.graphtoolkits.algorithm.centrality.Closeness;
import com.org.shark.graphtoolkits.graph.Graph;
import com.org.shark.graphtoolkits.utils.GraphAnalyticTool;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * Created by yxshao on 10/13/15.
 */
@GraphAnalyticTool(
        name = "GroupDetectionDriver",
        description = "The Entry of graph detection."
)
public class GroupDetectionDriver implements GenericGraphTool {
    protected static final Pattern SEPERATOR =  Pattern.compile("[\t ]");

    private int startPhase;

    private String originalGraphFilePath;
    private double edgeWeightThreshold;
    private Graph originalGraph;

    private int clusterCountPerVertex; // -cSize
    private int vertexCountPerCluster; // -vcSize
    private int vertexMaxCandidateClusterCount; // -vccSize
    private int iterationLimitation;   // -iter
    private double boundaryFactor;     // -fb

    private double commonNodesThreshold;

    public GroupDetectionDriver () {
        this.startPhase = 1;
        this.edgeWeightThreshold = 3;
        this.clusterCountPerVertex = 5;
        this.vertexCountPerCluster = 10;
        this.vertexMaxCandidateClusterCount = 10;
        this.iterationLimitation = 20;
        this.boundaryFactor = 0.0;
        this.commonNodesThreshold = 1;
    }

    @Override
    public void registerOptions(Options options) {
        //job configuration
        options.addOption("sp", "startPhase", true, "The number of phase which starts the application.");

        // graph loading configure
        options.addOption("eth", "edgeWeightThreshold", true, "Threshold for edge weight");

        // semi-clustering algorithm configure
        options.addOption("iter", "iteration", true, "The limitation of iteration");
        options.addOption("cSize", "clusterSize", true, "The limitation of the number of cluster size");
        options.addOption("vcSize", "vertexClusterSize", true, "The limitation of the number of vertices in a cluster");
        options.addOption("vccSize", "vertexClusterCandidateSize", true, "The limitation of the number of candidate clusters");
        options.addOption("fb", "boundaryFactor", true, "The factor for boundary edges");

        // cluster merging configure
        options.addOption("nth", "commonNodesThreshold", true, "Threshold for the number of common nodes given two clusters");
    }

    /**
     * Group Detection Flow:
     * 1. load graph by filtering edges which has weight below ewThreshold.
     * 2. normalize the edge weight: min(w1,w2).
     * 3. use semi-clustering algorithm to get the preliminary clusters and merge the clusters of a vertex into a single one.
     * 4. clean the cluster via closeness centrality.
     * 5. find high-quality clusters on top of preliminary clusters using maximal clique detection.
     * 6. merge clusters where the number of common nodes exceeds the threshold.
     * @param cmd
     */
    @Override
    public void run(CommandLine cmd) {
        getConfiguration(cmd);
        System.out.println("Phase 1: Loading graph ... ...");
        loadGraph(startPhase);

        System.out.println("Phase 2: Execute semi-clustering algorithm ... ...");
        HashMap<Integer, Group> clusters = semiClustering(startPhase);

        System.out.println("Phase 3: Clean clusters with closeness ... ...");
        HashMap<Integer, Group> cleanedClusters = cleanByCloseness(this.startPhase, clusters);

        System.out.println("Phase 4: Search high-quality clusters ... ...");
        HashMap<Integer, Group> hClusters = findHighQualityClusters(this.startPhase, cleanedClusters);

        System.out.println("Phase 5: Merge high-quality clusters ... ...");
        HashSet<Group> result = mergeClusters(startPhase, hClusters);

        System.out.println("Phase 6: Save the final results ... ...");
        saveFinalClusters(result, this.originalGraphFilePath + ".final.clusters");
    }

    /**
     * phase 1:
     * @param sp
     */
    public void loadGraph(int sp) {
        if(sp <= 2) {
            this.originalGraph = new Graph(originalGraphFilePath, edgeWeightThreshold);
        }
        else {
           System.out.println("Skip!!!");
        }
    }

    /**
     * phase 2:
     * @return
     */
    public HashMap<Integer, Group> semiClustering(int sp) {
        if(sp <= 2) {
            SemiClusteringAlgorithm semiAlg = new SemiClusteringAlgorithm();
            semiAlg.setBoundaryFactor(this.boundaryFactor);
            semiAlg.setIterationLimitation(this.iterationLimitation);
            semiAlg.setSemiClusterMaximumVertexCount(this.vertexCountPerCluster);
            semiAlg.setVertexMaxCandidateClusterCount(this.vertexMaxCandidateClusterCount);
            semiAlg.setVertexMaxClusterCount(this.clusterCountPerVertex);
            return semiAlg.run(this.originalGraph, this.originalGraphFilePath + ".clusters");
        }
        else {
            System.out.println("Skip!!!");
        }
        return null;
    }

    /**
     * phase 3:
     * @param sp
     */
    public HashMap<Integer, Group> cleanByCloseness(int sp, HashMap<Integer, Group> clusters) {
        if(sp > 3) {
            System.out.println("Skip!!!");
            return null;
        }
//        System.out.println(clusters);
        HashMap<Integer, Group> res = null;
        if(sp == 3) {
            clusters = new HashMap<Integer, Group>();
            try {
                FileInputStream fin = new FileInputStream(this.originalGraphFilePath + ".clusters.merge");
                BufferedReader fbr = new BufferedReader(new InputStreamReader(fin));

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
                    Group gg = new Group();
                    gg.setGroupCenterId(gid);
                    gg.setMemberList(gList);
                    clusters.put(gid, gg);
                }

                fbr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Closeness closenessCleaner = new Closeness();
        res = closenessCleaner.cleanClusters(this.originalGraph, clusters, this.originalGraphFilePath + ".clusters.clean");
        return res;
    }

    /**
     * phase 4: find high Quality Clusters
     * @return
     */
    HashMap<Integer, Group> findHighQualityClusters(int sp, HashMap<Integer, Group> cleanedClusters) {
        if(sp > 4) {
            System.out.println("Skip!!!");
            return null;
        }
        if(sp == 4) {
            cleanedClusters = new HashMap<Integer, Group>();
            try {
                FileInputStream fin = new FileInputStream(this.originalGraphFilePath+".clusters.clean");
                BufferedReader fbr = new BufferedReader(new InputStreamReader(fin));

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
                    Group gg = new Group();
                    gg.setGroupCenterId(gid);
                    gg.setMemberList(gList);
                    cleanedClusters.put(gid, gg);
                }
                fbr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        MergeGroups mg = new MergeGroups();
        return mg.findMaximalGroups(cleanedClusters, this.originalGraphFilePath+".clusters.clique");
    }
    /**
     * phase 5:
     * @param sp
     */
    public HashSet<Group> mergeClusters(int sp, HashMap<Integer, Group> hClusters) {
        if(sp > 5) {
            System.out.println("Skip!!!");
            return null;
        }
        if(sp == 5) {
            hClusters = new HashMap<Integer, Group>();
            try {
                FileInputStream fin = new FileInputStream(this.originalGraphFilePath+".clusters.clique");
                BufferedReader fbr = new BufferedReader(new InputStreamReader(fin));

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
                    Group gg = new Group();
                    gg.setGroupCenterId(gid);
                    gg.setMemberList(gList);
                    hClusters.put(gid, gg);
                }
                fbr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        MergeGroups mg = new MergeGroups();
        return mg.mergeGroupsByCommonNodes(hClusters, this.commonNodesThreshold, this.originalGraphFilePath+".clusters.clique.merge");
    }

    @Override
    public boolean verifyParameters(CommandLine cmd) {
        return true;
    }

    private void getConfiguration(CommandLine cmd) {
        this.originalGraphFilePath = cmd.getOptionValue("i");
        if(cmd.hasOption("sp")) {
           this.startPhase = Integer.parseInt(cmd.getOptionValue("sp"));
        }
        if(cmd.hasOption("eth")) {
            this.edgeWeightThreshold = Double.parseDouble(cmd.getOptionValue("eth"));
        }
        if(cmd.hasOption("iter")) {
            this.iterationLimitation = Integer.parseInt(cmd.getOptionValue("iter"));
        }
        if(cmd.hasOption("cSize")) {
            this.clusterCountPerVertex = Integer.parseInt(cmd.getOptionValue("cSize"));
        }
        if(cmd.hasOption("vcSize")) {
            this.vertexCountPerCluster = Integer.parseInt(cmd.getOptionValue("vcSize"));
        }
        if(cmd.hasOption("vccSize")) {
            this.vertexMaxCandidateClusterCount = Integer.parseInt(cmd.getOptionValue("vccSize"));
        }
        if(cmd.hasOption("fb")) {
            this.boundaryFactor = Double.parseDouble(cmd.getOptionValue("fb"));
        }
        if(cmd.hasOption("nth")) {
            this.commonNodesThreshold = Integer.parseInt(cmd.getOptionValue("nth"));
        }
    }

    private void saveFinalClusters( HashSet<Group> groups, String savePath) {
        try {
            FileOutputStream fout = new FileOutputStream(savePath);
            BufferedWriter fwr = new BufferedWriter(new OutputStreamWriter(fout));
            for(Group gg : groups) {
                if(gg.getMemberList() == null) continue;
                StringBuilder sb = new StringBuilder();
                TreeSet<Integer> res = new TreeSet<Integer>(gg.getMemberList());
                for(Integer scd : res) {
                    sb.append(" ");
                    sb.append(scd);
                }
                sb.append("\n");
                fwr.write(sb.toString());
            }
            fwr.flush();
            fwr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
