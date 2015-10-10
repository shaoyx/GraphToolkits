package com.org.shark.graphtoolkits.applications;

import com.org.shark.graphtoolkits.algorithm.SemiClustering.SemiClusterDetails;

import java.util.*;

/**
 * Created by yxshao on 10/10/15.
 */
public class Group {
    private Set<Integer> memberList;
    private int groupCenterId;

    public Group() {
        memberList = new HashSet<Integer>();
    }

    public String getGroupId() {
        return String.valueOf(this.hashCode());
    }

    public int getGroupCenterId() {
        return this.groupCenterId;
    }

    public void setGroupCenterId(int gcId) {
        this.groupCenterId = gcId;
    }

    public void addMemberList(Set<Integer> vertexList) {
        for(int vid : vertexList) {
            addMember(vid);
        }
    }

    public void addMember(int vid) {
        memberList.add(vid);
    }

    public Set<Integer> getMemberList() {
        return memberList;
    }

    @Override
    public int hashCode() {
        List<String> vertexIDList = new ArrayList<String>();
        for (Integer vid : memberList) {
            vertexIDList.add(String.valueOf(vid));
        }
        Collections.sort(vertexIDList);
        return (vertexIDList.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(groupCenterId);
        for(Integer scd : memberList) {
            sb.append(" ");
            sb.append(scd);
        }
        sb.append("\n");
        return sb.toString();
    }
}
