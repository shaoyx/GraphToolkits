package com.org.shark.graphtoolkits.algorithm.SemiClustering;

import com.org.shark.graphtoolkits.GenericGraphTool;
import com.org.shark.graphtoolkits.graph.Graph;
import com.org.shark.graphtoolkits.utils.GraphAnalyticTool;
import org.apache.commons.cli.CommandLine;

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
    private int iterationLimitation;

    private Graph graphData;

    public SemiClusteringAlgorithm() {}


    @Override
    public void run(CommandLine cmd) {
        graphData = new Graph(cmd.getOptionValue("-i"));
        this.semiClusterMaximumVertexCount = 10;
        this.vertexMaxClusterCount = 2;
        this.iterationLimitation = 20;

        if(cmd.hasOption("-iter")) {
            this.iterationLimitation = Integer.valueOf(cmd.getOptionValue("-iter"));
        }

        if(cmd.hasOption("-cSize")) {
            this.semiClusterMaximumVertexCount = Integer.valueOf(cmd.getOptionValue("-cSize"));
        }

        if(cmd.hasOption("-vcSize")) {
            this.vertexMaxClusterCount = Integer.valueOf(cmd.getOptionValue("-vcSize"));
        }

        computeSemiClusters();
    }

    @Override
    public boolean verifyParameters(CommandLine cmd) {
        return true;
    }

    private void computeSemiClusters() {
        initialCluster();
        for(int i = 1; i <= this.iterationLimitation; i++) {
            if(!updateCluster()) {
                break;
            }
        }
    }

    private void initialCluster() {
        for(Integer vid : this.graphData.getVertexSet().keySet()) {
            List<Integer> lV = new ArrayList<Integer>();
            lV.add(vid);
            String newClusterName = "C" + createNewSemiClusterName(lV);
            SemiClusterMessage initialClusters = new SemiClusterMessage();
            initialClusters.setSemiClusterId(newClusterName);
            initialClusters.addVertexList(lV);
            initialClusters.setScore(1);

            Set<SemiClusterDetails> scList = new TreeSet<SemiClusterDetails>();
            scList.add(new SemiClusterDetails(newClusterName, 1.0));
            SemiClusterMessage vertexValue = new SemiClusterMessage();
            //TODO: use SemiClusterVertex
//            vertexValue.setSemiClusterContainThis(scList);
//            this.setValue(vertexValue);
        }
    }

    /**
     * update current clusters
     * @return true: the cluster is updated; false: the cluster is not updated.
     */
    private boolean updateCluster() {
        for(Integer vid : this.graphData.getVertexSet().keySet()) {
            TreeSet<SemiClusterMessage> candidates = new TreeSet<SemiClusterMessage>();

            for (SemiClusterMessage msg : messages) {
                candidates.add(msg);

                if (!msg.contains(this.getId().get())
                        && msg.size() == semiClusterMaximumVertexCount) {
                    SemiClusterMessage msgNew = WritableUtils.clone(msg, this.getConf());
                    msgNew.addVertex(SemiClusterMessage.createGroupedVertex(this));
                    msgNew.setSemiClusterId("C"
                            + createNewSemiClusterName(msgNew.getVertexList()));
                    msgNew.setScore(semiClusterScoreCalcuation(msgNew));

                    candidates.add(msgNew);
                }
            }

            Iterator<SemiClusterMessage> bestCandidates = candidates
                    .descendingIterator();
            int count = 0;

            while (bestCandidates.hasNext() && count < graphJobMessageSentCount) {
                SemiClusterMessage candidate = bestCandidates.next();
                this.sendMessageToAllEdges(candidate);
                count++;
            }

            // Update candidates
            SemiClusterMessage value = this.getValue();
            Set<SemiClusterDetails> clusters = value.getSemiClusterContainThis();
            for (SemiClusterMessage msg : candidates) {
                if (clusters.size() > graphJobVertexMaxClusterCount) {
                    break;
                } else {
                    clusters.add(new SemiClusterDetails(msg.getSemiClusterId(), msg
                            .getScore()));
                }
            }
        }

        return true;
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
}
