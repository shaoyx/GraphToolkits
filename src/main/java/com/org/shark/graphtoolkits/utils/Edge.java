package com.org.shark.graphtoolkits.utils;

/**
 * Created by yxshao on 10/5/15.
 */
public class Edge {
    private int id;
    private double weight;

    public Edge(int id, double weight) {
        this.id = id;
        this.weight = weight;
    }

    public Edge() {}

    public int getId() { return this.id; }
    public void setId(int id) { this.id = id; }

    public double getWeight() { return this.weight; }
    public void setWeight(double weight) { this.weight = weight; }

}
