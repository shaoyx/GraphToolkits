/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.org.shark.graphtoolkits.algorithm.SemiClustering;

import com.org.shark.graphtoolkits.graph.Graph;

import java.util.*;

/**
 * The SemiClusterInfo class defines the structure of the value stored by
 * each vertex in the graph Job which is same as the Message sent my each
 * vertex.
 * 
 */
public class SemiClusterInfo implements Comparable<SemiClusterInfo> {

  private String semiClusterId = null;
  private double semiClusterScore = 0.0;

  private List<Integer> semiClusterVertexList;
  private Set<SemiClusterDetails> semiClusterContainThis;

  public SemiClusterInfo() {
    semiClusterVertexList = new ArrayList<Integer>();
    semiClusterContainThis = new TreeSet<SemiClusterDetails>();
  }

  public double getScore() {
    return semiClusterScore;
  }

  public void setScore(double score) {
    this.semiClusterScore = score;
  }

  public List<Integer> getVertexList() {
    return semiClusterVertexList;
  }

  public void addVertex(Integer v) {
    this.semiClusterVertexList.add(v);
  }

  public void addVertexList(List<Integer> list) {
	  for (Integer v : list) {
		  addVertex(v);
	  }
  }

  public void setSemiClusterContainThis(
      Set<SemiClusterDetails> semiClusterContainThis) {
    this.semiClusterContainThis = semiClusterContainThis;
  }

  public String getSemiClusterId() {
    return semiClusterId;
  }

  public void setSemiClusterId(String scId) {
    this.semiClusterId = scId;
  }

  public boolean contains(int vertexID) {
    for (Integer vid : this.semiClusterVertexList) {
      if (vid == vertexID) {
        return true;
      }
    }
    return false;
  }

  public Set<SemiClusterDetails> getSemiClusterContainThis() {
    return semiClusterContainThis;
  }

  public void setClusters(Set<SemiClusterDetails> clusters,
      int graphJobVertexMaxClusterCount) {
    int clusterCountToBeRemoved = 0;
    NavigableSet<SemiClusterDetails> setSort = new TreeSet<SemiClusterDetails>(
        new Comparator<SemiClusterDetails>() {

          @Override
          public int compare(SemiClusterDetails o1, SemiClusterDetails o2) {
            return (o1.getSemiClusterScore() == o2.getSemiClusterScore() ? 0
                : o1.getSemiClusterScore() < o2.getSemiClusterScore() ? -1 : 1);
          }
        });
    setSort.addAll(this.semiClusterContainThis);
    setSort.addAll(clusters);
    clusterCountToBeRemoved = setSort.size() - graphJobVertexMaxClusterCount;
    Iterator<SemiClusterDetails> itr = setSort.descendingIterator();
    while (clusterCountToBeRemoved > 0) {
      itr.next();
      itr.remove();
      clusterCountToBeRemoved--;
    }
    this.semiClusterContainThis = setSort;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((semiClusterId == null) ? 0 : semiClusterId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
      System.out.println("Equal function");
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SemiClusterInfo other = (SemiClusterInfo) obj;
    if (semiClusterId == null) {
      if (other.semiClusterId != null)
        return false;
    } else if (!semiClusterId.equals(other.semiClusterId))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "SCMessage [semiClusterId=" + semiClusterId + ", semiClusterScore="
        + semiClusterScore + ", semiClusterVertexList=" + semiClusterVertexList
        + ", semiClusterContainThis=" + semiClusterContainThis + "]";
  }

  public int size() {
    return this.semiClusterVertexList.size();
  }

  public SemiClusterInfo copy() {
    SemiClusterInfo scInfo = new SemiClusterInfo();
    List<Integer> vList = new ArrayList<Integer>(this.semiClusterVertexList);
    scInfo.addVertexList(vList);
    return scInfo;
  }

  @Override
  public int compareTo(SemiClusterInfo m) {
      System.out.println("CompareTo methods: this="+ this.getSemiClusterId()+" other="+m.getSemiClusterId());
      if(this.getSemiClusterId().equals(m.getSemiClusterId()))
          return 0;
    return (this.getScore() == m.getScore() ? 0
            : this.getScore() < m.getScore() ? -1 : 1);
  }

}
