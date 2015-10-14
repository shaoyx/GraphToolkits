package com.org.shark.graphtoolkits.applications;

/**
 * Created by yxshao on 10/10/15.
 */

import com.org.shark.graphtoolkits.GenericGraphTool;
import com.org.shark.graphtoolkits.utils.GraphAnalyticTool;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

@GraphAnalyticTool(
        name = "MergeGroups",
        description = "Merge groups for find minimal satisfied groups"
)
public class MergeGroups implements GenericGraphTool {
    protected static final Pattern SEPERATOR =  Pattern.compile("[\t ]");
    HashMap<Integer, Group> rawGroups;
    HashMap<String, Group> refinedGroups;

    ArrayList<Integer> flags;
    private double threshold;
    private boolean isPrune;

    public HashMap<Integer, Group> findMaximalGroups(HashMap<Integer, Group> clusters, String savePath) {
        this.rawGroups = clusters;
        doCompute(savePath);
        HashMap<Integer, Group> results = new HashMap<Integer, Group>();
        for(String gid : refinedGroups.keySet()) {
            results.put(refinedGroups.get(gid).getGroupCenterId(), refinedGroups.get(gid));
        }
        return results;
    }

    public HashSet<Group> mergeGroupsByCommonNodes(HashMap<Integer, Group> clusters, double nodeThreashold, String savePath) {
        this.rawGroups = clusters;
        this.threshold = nodeThreashold;
        return mergeGlobalGroup(savePath);
    }

    @Override
    public void registerOptions(Options options) {
        options.addOption("th", "threshold", true, "Threshold for edge weight");
        options.addOption("gf", "groupFile", true, "The path of group file");
        options.addOption("pr", "prune", false, "Whether to prune the results");
        options.addOption("mg", "mergeGlobal", false, "Merge global results");
        options.addOption("mf", "mergeFinalResults", false, "Merge final results");
        options.addOption("mf2", "mergeFinalResults", false, "Merge final results");
    }

    @Override
    public void run(CommandLine cmd) {
        String rawGroupFile = cmd.getOptionValue("gf");
        if(cmd.hasOption("th")) {
            threshold = Double.valueOf(cmd.getOptionValue("th"));
        }
        else {
            threshold = 2;
        }
        isPrune = cmd.hasOption("pr");
        flags = new ArrayList<Integer>();

//        flags.add(136176934);
//        flags.add(136780720);
//        flags.add(142614968);
//        flags.add(144496930);
//        flags.add(239841293);

        loadRawGroupFile(rawGroupFile);
//        System.out.println("Begin computation ...");
        if(cmd.hasOption("mg")) { //merge cliques.
            mergeGlobalGroup(rawGroupFile+".gmerge");
        }
        else if(cmd.hasOption("mf")) {
            String globalGroupFilePath = cmd.getOptionValue("i");
            HashMap<Integer, List<Group> > globalGroups = new HashMap<Integer, List<Group> >();
            loadGlobalGroupFile(globalGroups, globalGroupFilePath);
            mergeFinalGroup(globalGroups, rawGroupFile+".final");
        }
        else if(cmd.hasOption("mf2")){
            String globalGroupFilePath = cmd.getOptionValue("i");
            HashMap<Integer, List<Group> > globalGroups = new HashMap<Integer, List<Group> >();
            loadGlobalGroupFile(globalGroups, globalGroupFilePath);
            mergeFinalGroup2(globalGroups, rawGroupFile + ".final2");

        }
        else
        {
            doCompute(rawGroupFile + ".refined");
        }
    }

    public HashSet<Group> mergeFinalGroup(HashMap<Integer, List<Group> > globalGroups, String savePath) {
        int count = 0;
       for(int gid : rawGroups.keySet()) {
           count++;
           if(count % 100 == 0)
               System.out.println("Processing "+ count);
           Group rawGroup = rawGroups.get(gid);
           boolean isChanged = false;
           do {
               isChanged = false;
               Set<Integer> maxInterSet = null;
               for (int memberId : rawGroup.getMemberList()) {
                   List<Group> memberGroup = globalGroups.get(memberId);
                   if (memberGroup == null) continue;
                   for(Group gg : memberGroup) {
                       Set<Integer> inter = rawGroup.intersection(gg);
                       if (maxInterSet == null || maxInterSet.size() < inter.size()) {
                           maxInterSet = inter;
                       }
                   }
               }
               if(maxInterSet != null && maxInterSet.size() > 2) {
                   isChanged = true;
                   for(int did : maxInterSet) {
                       rawGroup.deleteMember(did);
                   }
               }
           }while(isChanged);
       }
        System.out.println("Saving results......");
        try {
            FileOutputStream fout = new FileOutputStream(savePath);
            BufferedWriter fwr = new BufferedWriter(new OutputStreamWriter(fout));

            for(int gid : rawGroups.keySet()) {
                Group gg = rawGroups.get(gid);
                fwr.write(gg.getGroupId() + ": " + gg.toString());
            }
            fwr.flush();
            fwr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void mergeFinalGroup2(HashMap<Integer, List<Group> > globalGroups, String savePath) {
        HashSet<Group> finalResults = new HashSet<Group>();

        for(int gid : globalGroups.keySet()) {
            List<Group> gList = globalGroups.get(gid);
            if(gList == null || gList.size() == 0) continue;
            for(Group g : gList) {
//                System.out.println(g);
//                System.out.println(finalResults.add(g));
                finalResults.add(g);
            }
        }
        for(int gid : rawGroups.keySet()) {
            Group rawGroup = rawGroups.get(gid);
            if(rawGroup == null) continue;
            finalResults.add(rawGroup);
        }
        System.out.println("Saving results......");
        try {
            FileOutputStream fout = new FileOutputStream(savePath);
            BufferedWriter fwr = new BufferedWriter(new OutputStreamWriter(fout));

            for(Group gg : finalResults) {
                fwr.write(gg.getGroupId() + ": " + gg.toString());
            }
            fwr.flush();
            fwr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashSet<Group> mergeGlobalGroup(String savePath) {
        HashSet<Group> result = new HashSet<Group>();
        int count = 0;
        for(int gid : rawGroups.keySet()) {
            count ++;
            if(count % 100 == 0)
                System.out.println("Processing "+ count);
            doMergeGroup(rawGroups.get(gid), result);
        }
        System.out.println("Saving results......");
        try {
            FileOutputStream fout = new FileOutputStream(savePath);
            BufferedWriter fwr = new BufferedWriter(new OutputStreamWriter(fout));

            for(Group gid : result) {
                fwr.write(gid.getGroupId() + ": " + gid.toString());
            }
            fwr.flush();
            fwr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void doMergeGroup(Group group, HashSet<Group> gSet) {
        if(gSet.isEmpty()) {
            gSet.add(group.copy());
        }

        Group tmpGroup = group.copy();
        boolean isChanged = true;
        while(isChanged) {
            isChanged = false;
           for(Group g : gSet) {
               Set<Integer> inter = g.intersection(tmpGroup);
               if(inter.size() > this.threshold) {
                  isChanged = true;
                   tmpGroup.addMemberList(g.getMemberList());
                   gSet.remove(g);
                   break;
               }
           }
        }
        gSet.add(tmpGroup);
    }

    @Override
    public boolean verifyParameters(CommandLine cmd) {
        return cmd.hasOption("gf");
    }


    boolean isOutputId(int vid) {
        for(int i = 0; i < flags.size(); i++) {
            if(flags.get(i) == vid)
                return true;
        }
        return false;
    }

    public void doCompute(String savePath) {
        refinedGroups = new HashMap<String, Group>();
        for(int gid : rawGroups.keySet()) {
            Group shrinkedGroup = refineGroup(gid);
            if(shrinkedGroup.size() > this.threshold  && shrinkedGroup.getMemberList().contains(gid)) {
                shrinkedGroup.setGroupCenterId(gid);
                refinedGroups.put(shrinkedGroup.getGroupId(), shrinkedGroup);
                if(isPrune)
                    updateRawGroups(shrinkedGroup);
            }
        }
        saveResults(savePath);
    }

    /**
     * search the maximal clique which contain gid
     * @param gid
     * @return
     */
    public Group refineGroup(int gid) {
        Group result = new Group();
        Group tmp = new Group();
        Group gidGroup = rawGroups.get(gid);
        result.addMember(gid);
        tmp.addMember(gid);
        int depth = 0;
        if(gidGroup == null || gidGroup.getMemberList() == null)
            return result;
        for(int vid : gidGroup.getMemberList()) {
            if(vid != gid && checkConnectivity(vid, tmp)) {
                tmp.addMember(vid);
                dfs(vid, tmp, result, depth);
                tmp.deleteMember(vid);
            }
        }
        return result;
    }

    public void dfs(int vid, Group curClique, Group res, int depth) {
        if(res.size() < curClique.size()) {
            res.copy(curClique);
        }
//        System.out.println("depth="+depth+": "+curClique);
        Group vidGroup = rawGroups.get(vid); //must not be empty
        for(int tmpVid : vidGroup.getMemberList()) {
            if(tmpVid != vid && !curClique.hasMember(tmpVid) && checkConnectivity(tmpVid, curClique)) {
                curClique.addMember(tmpVid);
                dfs(tmpVid, curClique, res, depth + 1);
                curClique.deleteMember(tmpVid);
            }
        }
    }

    public boolean checkConnectivity(int vid, Group curClique) {
        Group vidGroup = rawGroups.get(vid);
        if(vidGroup == null || vidGroup.getMemberList() == null)
            return false;
        for(int tmpVid : curClique.getMemberList()) {
            if(!vidGroup.getMemberList().contains(tmpVid))
                return false;
            if(rawGroups.get(tmpVid) == null || !rawGroups.get(tmpVid).hasMember(vid))
                return  false;
        }
        return true;
    }

    public void updateRawGroups(Group newGroup) {
        Set<Integer> memberList = new HashSet<Integer>(newGroup.getMemberList());
        for(int gid : memberList) {
            Group group = rawGroups.get(gid);
            for(int vid : newGroup.getMemberList()) {
                group.deleteMember(vid);
            }
        }
    }

    public void saveResults(String savePath) {
        try {
            FileOutputStream fout = new FileOutputStream(savePath);
            BufferedWriter fwr = new BufferedWriter(new OutputStreamWriter(fout));

            for(String gid : refinedGroups.keySet()) {
                fwr.write(refinedGroups.get(gid).getGroupId()+": "+refinedGroups.get(gid).toString());
            }

            if(isPrune) {
                for (Integer vid : rawGroups.keySet()) {
                    if (rawGroups.get(vid) != null || rawGroups.get(vid).size() > 0) {
                        fwr.write(rawGroups.get(vid).getGroupId() + ": " + rawGroups.get(vid).toString());
                    }
                }
            }

            fwr.flush();
            fwr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadRawGroupFile(String filePath) {
        try {
            FileInputStream fin = new FileInputStream(filePath);
            BufferedReader fbr = new BufferedReader(new InputStreamReader(fin));

            String line;
            rawGroups = new HashMap<Integer, Group>();

            while((line = fbr.readLine()) != null) {
                if (line.startsWith("#")) continue;
                String[] values = SEPERATOR.split(line);

                String vid = values[0];
//                System.out.println(values[0]);
                int gid = Integer.valueOf(values[0].substring(0, values[0].indexOf(":")));
                HashSet<Integer> gList = new HashSet<Integer>();
                for (int i = 1; i < values.length; i++) {
                    int sv = Integer.valueOf(values[i]);
                    gList.add(sv);
                }
                if(!gList.contains(gid)) { //gid is not important in the group.
                    gList.add(gid);
                }
                    Group g = new Group();
                    g.setGroupCenterId(gid);
                    g.addMemberList(gList);
                    rawGroups.put(gid, g);
            }
            fbr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadGlobalGroupFile(HashMap<Integer, List<Group>> globalGroups, String filePath) {
        try {
            FileInputStream fin = new FileInputStream(filePath);
            BufferedReader fbr = new BufferedReader(new InputStreamReader(fin));

            String line;

            while((line = fbr.readLine()) != null) {
                if (line.startsWith("#")) continue;
                String[] values = SEPERATOR.split(line);

                String vid = values[0];
//                System.out.println(values[0]);
                int gid = Integer.valueOf(values[0].substring(0, values[0].indexOf(":")));
                HashSet<Integer> gList = new HashSet<Integer>();
                for (int i = 1; i < values.length; i++) {
                    int sv = Integer.valueOf(values[i]);
                    gList.add(sv);
                }
                for(int tmpId : gList) {
                    if(!globalGroups.containsKey(tmpId))  {
                        globalGroups.put(tmpId, new ArrayList<Group>());
//                       System.out.println("Conflict: "+tmpId+" pre="+globalGroups.get(tmpId)+" cur="+gList);
                    }
                    Group g = new Group();
                    g.setGroupCenterId(tmpId);
                    g.addMemberList(gList);
                    globalGroups.get(tmpId).add(g);
                }
            }
            fbr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
