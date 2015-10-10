package com.org.shark.graphtoolkits.applications;

/**
 * Created by yxshao on 10/10/15.
 */

import com.org.shark.graphtoolkits.GenericGraphTool;
import com.org.shark.graphtoolkits.utils.GraphAnalyticTool;
import org.apache.commons.cli.CommandLine;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
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

    @Override
    public void run(CommandLine cmd) {
        String rawGroupFile = cmd.getOptionValue("gf");
        flags = new ArrayList<Integer>();

        flags.add(136176934);
        flags.add(136780720);
        flags.add(142614968);
        flags.add(144496930);
        flags.add(239841293);

        loadRawGroupFile(rawGroupFile);
//        System.out.println("Begin computation ...");
        doCompute(rawGroupFile + ".refined");
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
            if(this.isOutputId(gid)) {
                System.out.println("gid=" + gid+": "+rawGroups.get(gid));
            }
            Group shrinkedGroup = refineGroup(gid);
            if(this.isOutputId(gid)) {
                System.out.println("gid=" + gid+": shrinked="+shrinkedGroup);
            }
            if(shrinkedGroup.size() > 2) {
                refinedGroups.put(shrinkedGroup.getGroupId(), shrinkedGroup);
                updateRawGroups(shrinkedGroup);
            }
        }
        saveResults(savePath);
    }

    public Group refineGroup(int gid) {
        Group result = rawGroups.get(gid).copy();
        HashSet<Integer> visited = new HashSet<Integer>();
        int curId = gid;
        while(true) {
            if(visited.contains(curId)) {
                curId = result.getNext(visited);
                if(curId == -1) break;
                continue;
            }
            visited.add(curId);
            Group other = rawGroups.get(curId);
            Set<Integer> inter = result.intersection(other);
            if(this.isOutputId(gid)) {
                System.out.println("Search vid="+curId +"g1="+result+" other="+other+" intersection="+inter);
            }
            result.setMemberList(inter);
        }
        return result;
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

            for(Integer vid : rawGroups.keySet()) {
                if(rawGroups.get(vid) != null || rawGroups.get(vid).size() > 0) {
                    fwr.write(rawGroups.get(vid).getGroupId()+": "+rawGroups.get(vid).toString());
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
                int gid = Integer.valueOf(values[0].substring(0, values[0].indexOf(":")));
                HashSet<Integer> gList = new HashSet<Integer>();
                for (int i = 1; i < values.length; i++) {
                    int sv = Integer.valueOf(values[i]);
                    gList.add(sv);
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
}
