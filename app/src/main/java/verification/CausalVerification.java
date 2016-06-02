package verification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import nju.cs.ADBExecutor;

/**
 * Created by mio on 5/5/16.
 */
public class CausalVerification {

    public static void main(String[] args) {
        ADBExecutor adbExecutor = new ADBExecutor("adb");
        adbExecutor.copyFromAll("/storage/emulated/0/Android/data/com.njucs.ballgame/files/BallGameDir",
                "log");
        List<File> logDirs = new ArrayList<>();
        List<File> latestFiles = new ArrayList<>();

        // retrieve files

        logDirs = Arrays.asList(new File("log").listFiles());

        for (File logDir : logDirs) {
            File[] taggedValueFiles = logDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.isFile() && pathname.getName().startsWith("TaggedValue"))
                        return true;
                    return false;
                }
            });
            Arrays.sort(taggedValueFiles, new Comparator<File>() {
                @Override
                public int compare(File lhs, File rhs) {
                    return lhs.getName().compareTo(rhs.getName());
                }
            });
            latestFiles.add(taggedValueFiles[taggedValueFiles.length - 1]);
        }

        System.out.println(Arrays.toString(latestFiles.toArray()));

        ArrayList<OpNode> graph = new ArrayList<>();


        // read in files' content and construct po by the way
        {
            int id = 0;
            for (int i = 0; i < latestFiles.size(); i++) {
                OpNode prev = null;
                File latestFile = latestFiles.get(i);
                int device = i;
                BufferedReader bufferedReader = null;
                try {
                    bufferedReader = new BufferedReader(new FileReader(latestFile));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return;
                }
                String line = null;
                try {
                    while ((line = bufferedReader.readLine()) != null) {
                        OpNode.OpCode op = line.substring(0, 1).equals("R") ?
                                OpNode.OpCode.READ : OpNode.OpCode.WRITE;
                        String tag = line.substring(2);
                        String variable = TaggedValue.getTaggedValueFromTag(tag).getVariable();
                        // value = tag = device + variable + sID(sequential id)
                        OpNode opNode = new OpNode(op, variable, tag, device, id);


                        // construct program order
                        if (prev != null)
                            opNode.edges.add(prev.id);
                        prev = opNode;

                        graph.add(opNode);
                        id++;

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
        {
            boolean[][] adjMatrix = new boolean[graph.size()][graph.size()];

            // construct write-to order
            for (int k = 0; k < graph.size(); k++) {
                OpNode opNode = graph.get(k);
                if (opNode.op == OpNode.OpCode.READ) {
                    OpNode dictatedNode = null;
                    for (int j = 0; j < graph.size(); j++) {
                        OpNode node = graph.get(j);
                        if (node.op == OpNode.OpCode.WRITE && opNode.value.equals(node.value)) {
                            dictatedNode = node;
                            break;
                        }
                    }
                    if (dictatedNode == null)
                        throw new RuntimeException();
                    opNode.edges.add(dictatedNode.id);
                    opNode.dictatedWrite = dictatedNode.id;
                }
            }

            // write graph edges into adj matrix
            for (int k = 0; k < graph.size(); k++) {
                for (int index : graph.get(k).edges) {
                    adjMatrix[index][k] = true;

                }
            }

            // compute transitive closure of graph
            for (int mid = 0; mid < graph.size(); mid++)
                for (int src = 0; src < graph.size(); src++)
                    for (int dest = 0; dest < graph.size(); dest++)
                        if (!adjMatrix[src][dest]) {
                            adjMatrix[src][dest] = adjMatrix[src][mid] && adjMatrix[mid][dest];
//                            changed = changed || adjMatrix[src][dest];    // is it changed?
                        }
        }
        ArrayList<ArrayList<OpNode>> observationList = reconstructObservationList(graph);

        for (int i = 0; i < observationList.size(); i++) {

            ArrayList<OpNode> observation = observationList.get(i);
            boolean[][] adjMatrix = new boolean[observation.size()][observation.size()];
            boolean changed = false;
            do {
                // reset 'changed' flag at loop's beginning
                changed = false;

                System.out.println("observation " + i + " write graph edges into matrix");
                // write graph edges into adj matrix
                for (int k = 0; k < observation.size(); k++) {
                    for (int index : observation.get(k).edges) {
                        adjMatrix[index][k] = true;

                    }
                }


                System.out.println("observation " + i + " compute transitive closure");
                // compute transitive closure of graph
                for (int mid = 0; mid < observation.size(); mid++)
                    for (int src = 0; src < observation.size(); src++)
                        for (int dest = 0; dest < observation.size(); dest++)
                            if (!adjMatrix[src][dest]) {
                                adjMatrix[src][dest] = adjMatrix[src][mid] && adjMatrix[mid][dest];
//                            changed = changed || adjMatrix[src][dest];    // is it changed?
                            }

                System.out.println("observation " + i + " update graph");
                // update graph based on transitive closure
                for (int k = 0; k < observation.size(); k++) {
                    observation.get(k).edges.clear();
                    for (int l = 0; l < observation.size(); l++) {
                        if (adjMatrix[l][k])
                            observation.get(k).edges.add(l);
                    }
                }

                System.out.println("observation " + i + " compute w'wr");
                // compute w'wr
                for (OpNode opNode : observation) {
                    if (opNode.op == OpNode.OpCode.READ) {
                        int dictatedNode = opNode.dictatedWrite;
                        for (int index : opNode.edges) {
                            OpNode node = observation.get(index);
                            if (node.op == OpNode.OpCode.WRITE && opNode.variable.equals(node.variable)
                                    && !opNode.value.equals(node.value)) {
                                // important statement; otherwise the outer loop won't exit
                                if (observation.get(dictatedNode).edges.contains(node.id))
                                    continue;
                                observation.get(dictatedNode).edges.add(index);
                                changed = true;
                            }
                        }

                    }
                }


            } while (changed);

            for (int diag = 0; diag < observation.size(); diag++) {
                if (adjMatrix[diag][diag]) {
                    System.out.println("Causal: false, observation " + i + " diag " + diag);
                    return;
                }
            }

        }
        System.out.println("Causal: true");


    }

    public static ArrayList<OpNode> allWriteOperation(ArrayList<OpNode> graph)
    {
        ArrayList<OpNode> rt = new ArrayList<>();
        for (OpNode opNode : graph)
        {
            if (opNode.op == OpNode.OpCode.WRITE)
                rt.add(new OpNode(opNode));
        }
        return rt;
    }

    public static ArrayList<ArrayList<OpNode>> reconstructObservationList(ArrayList<OpNode> graph) {
        ArrayList<ArrayList<OpNode>> rt = new ArrayList<>();
        ArrayList<Map<Integer, Integer>> posMapList = new ArrayList<>();
        posMapList.add(new HashMap<Integer, Integer>());
        rt.add(new ArrayList<OpNode>());
        {
            int head = 0, tail = 0, i = 0;
            while (tail < graph.size()) {
                OpNode opNode = graph.get(tail);
                if (graph.get(head).device == opNode.device && opNode.op == OpNode.OpCode.READ) {
                    rt.get(i).add(new OpNode(graph.get(tail)));
                    posMapList.get(i).put(graph.get(tail).id, rt.get(i).size() - 1);
                    tail++;

                }
                else if (opNode.op == OpNode.OpCode.WRITE && graph.get(head).device == opNode.device)
                    tail++;
                else {
                    ArrayList<OpNode> writeOperations = allWriteOperation(graph);
                    for (OpNode writeOpNode : writeOperations)
                    {
                        rt.get(i).add(writeOpNode);
                        posMapList.get(i).put(writeOpNode.id, rt.get(i).size() - 1);
                    }
                    head = tail;
                    i++;
                    rt.add(new ArrayList<OpNode>());
                    posMapList.add(new HashMap<Integer, Integer>());
                }
            }
            if (graph.get(head).device == graph.get(tail - 1).device)
            {
                ArrayList<OpNode> writeOperations = allWriteOperation(graph);
                for (OpNode writeOpNode : writeOperations)
                {
                    rt.get(i).add(writeOpNode);
                    posMapList.get(i).put(writeOpNode.id, rt.get(i).size() - 1);
                }
            }
        }
        for (int i = 0; i < rt.size(); i++) {
            ArrayList<OpNode> observation = rt.get(i);
            Map<Integer, Integer> posMap = posMapList.get(i);
            for (OpNode opNode : observation)
            {
                Integer id = posMap.get(opNode.id);
                if (id == null)
                    throw new RuntimeException();
                opNode.id = id;
                if (opNode.op == OpNode.OpCode.READ) {
                    Integer dictatedWrite = posMap.get(opNode.dictatedWrite);
                    if (dictatedWrite == null)
                    {
                        System.err.println("dictatedWrite" + opNode.dictatedWrite);
                        throw new RuntimeException();
                    }
                    opNode.dictatedWrite = dictatedWrite;
                }

                ArrayList<Integer> newPosEdgeList = new ArrayList<>();
                for (Integer edge : opNode.edges)
                {
                    Integer newPosEdge = posMap.get(edge);
                    if (newPosEdge != null)
                        newPosEdgeList.add(newPosEdge);
                }
                opNode.edges = new HashSet<>(newPosEdgeList);
            }

        }
        return rt;
    }
}
