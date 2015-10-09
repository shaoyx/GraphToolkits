package com.org.shark.graphtoolkits.graph;

import java.util.Comparator;

/**
 * Created by yxshao on 10/6/15.
 */
public class Vertex implements Comparable<Vertex> {
    private int vid;
    private double weight;

    public Vertex() {}
    public Vertex(int vid, double weight) {
        this.vid = vid;
        this.weight = weight;
    }

    public void setVid(int vid) { this.vid = vid; }
    public int getVid() { return  this.vid; }

    public void setWeight(double weight) { this.weight = weight; }
    public double getWeight() { return this.weight; }


    @Override
    public int compareTo(Vertex o) {
        return this.getVid() - o.getVid();
    }
}
