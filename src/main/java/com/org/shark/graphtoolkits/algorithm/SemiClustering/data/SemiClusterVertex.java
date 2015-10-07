package com.org.shark.graphtoolkits.algorithm.semiclustering.data;

import com.org.shark.graphtoolkits.algorithm.semiclustering.SemiClusterInfo;
import com.org.shark.graphtoolkits.graph.Vertex;

import java.util.ArrayList;

/**
 * Created by yxshao on 10/7/15.
 */
public class SemiClusterVertex extends Vertex {

    SemiClusterInfo vertexClusterInfo;
    ArrayList<SemiClusterInfo> preCandidateSemiClusters;
    ArrayList<SemiClusterInfo> curCandidateSemiClusters;

    public SemiClusterVertex() {
        super();
    }

    public SemiClusterVertex(int vid, double weight) {
        super(vid, weight);
    }

    public SemiClusterInfo getVertexClusterInfo() {
        return vertexClusterInfo;
    }

    public void setVertexClusterInfo(SemiClusterInfo vcInfo) {
        vertexClusterInfo = vcInfo;
    }

    public ArrayList<SemiClusterInfo> getPreCandidateSemiClusters() {
        return this.preCandidateSemiClusters;
    }

    public void setPreCandidateSemiClusters(ArrayList<SemiClusterInfo> scInfo) {
        this.preCandidateSemiClusters = scInfo;
    }

    public ArrayList<SemiClusterInfo> getCurCandidateSemiClusters() {
        return this.curCandidateSemiClusters;
    }

    public void setCurCandidateSemiClusters(ArrayList<SemiClusterInfo> scInfo) {
        this.curCandidateSemiClusters = scInfo;
    }

}
