package com.org.shark.graphtoolkits.algorithm.SemiClustering.data;

import com.org.shark.graphtoolkits.algorithm.SemiClustering.SemiClusterInfo;
import com.org.shark.graphtoolkits.graph.Vertex;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by yxshao on 10/7/15.
 */
public class SemiClusterVertex extends Vertex {

//    SemiClusterInfo vertexClusterInfo;
    Set<SemiClusterInfo> vertexClusterContainThis;
    ArrayList<SemiClusterInfo> preCandidateSemiClusters;
    ArrayList<SemiClusterInfo> curCandidateSemiClusters;

    public SemiClusterVertex() {
        super();
        vertexClusterContainThis = new TreeSet<SemiClusterInfo>();
    }

    public SemiClusterVertex(int vid, double weight) {
        super(vid, weight);
        vertexClusterContainThis = new TreeSet<SemiClusterInfo>();
    }

    public Set<SemiClusterInfo> getVertexClusterContainThis() {
        return vertexClusterContainThis;
    }

    public void setVertexClusterContainThis(Set<SemiClusterInfo> vcInfo) {
        vertexClusterContainThis = vcInfo;
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
