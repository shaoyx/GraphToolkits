package com.org.shark.graphtoolkits.algorithm.semiclustering.data;

import java.util.HashMap;

/**
 * Created by yxshao on 10/7/15.
 */
public class SemiClusterGraph {

    private HashMap<Integer, SemiClusterVertex> semiClusterVertexSet;

    public SemiClusterGraph(){
        semiClusterVertexSet = new HashMap<Integer, SemiClusterVertex>();
    }

    public SemiClusterVertex getSemiClusterVertex(int vid) {
        return semiClusterVertexSet.get(vid);
    }

    public void addSemiClusterVertex(SemiClusterVertex v) {
       if(!semiClusterVertexSet.containsKey(v.getVid())) {
           semiClusterVertexSet.put(v.getVid(), v);
       }
    }

    public void updateSemiClusterVertex(SemiClusterVertex v) {
        semiClusterVertexSet.put(v.getVid(), v);
    }

}
