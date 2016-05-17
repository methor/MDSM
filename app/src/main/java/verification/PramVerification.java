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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SortedSet;

import nju.cs.ADBExecutor;

/**
 * Created by mio on 4/22/16.
 */
public class PramVerification {

    public static void main(String[] args) {
//
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

        ArrayList<Observation> observations = new ArrayList<>();
        ArrayList<Observation> writeObservations = new ArrayList<>();


        // read in files' content and construct po by the way
        {
            for (int i = 0; i < latestFiles.size(); i++) {
                int id = 0;
                File latestFile = latestFiles.get(i);
                observations.add(new Observation());
                writeObservations.add(new Observation());
                Observation observation = observations.get(observations.size() - 1);
                Observation writeObservation = writeObservations.get(writeObservations.size() - 1);
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

                        if (opNode.op == OpNode.OpCode.WRITE) {
                            OpNode dupWithoutIdOpNode = new OpNode(op, variable, tag, device, 0);
                            writeObservation.operations.add(dupWithoutIdOpNode);
                        }


                        // construct program order
                        if (observation.operations.size() != 0)
                            opNode.edges.add(observation.operations.get(observation.operations.size() - 1).id);
                        observation.operations.add(opNode);


                        id++;

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
        for (int i = 0; i < observations.size(); i++) {
            Observation observation = observations.get(i);
            for (int j = 0; j < writeObservations.size(); j++) {
                if (j != i) {
                    OpNode prev = null;
                    for (OpNode opNode : writeObservations.get(j).operations) {
                        OpNode dupOpNode = new OpNode(opNode);
                        dupOpNode.id = observation.operations.size();
                        observation.operations.add(dupOpNode);
                        if (prev != null)
                            dupOpNode.edges.add(prev.id);
                        prev = dupOpNode;
                    }

                }
            }
            boolean[][] adjMatrix = new boolean[observation.operations.size()][observation.operations.size()];

            // construct write-to order
            for (int k = 0; k < observation.operations.size(); k++) {
                OpNode opNode = observation.operations.get(k);
                if (opNode.op == OpNode.OpCode.READ) {
                    OpNode dictatedNode = null;
                    for (int j = 0; j < observation.operations.size(); j++) {
                        OpNode node = observation.operations.get(j);
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
            boolean changed = false;
            do {
                // reset 'changed' flag at loop's beginning
                changed = false;

                System.out.println("observation " + i + " write graph edges into matrix");
                // write graph edges into adj matrix
                for (int k = 0; k < observation.operations.size(); k++) {
                    for (int index : observation.operations.get(k).edges) {
                        adjMatrix[index][k] = true;

                    }
                }


                System.out.println("observation " + i + " compute transitive closure");
                // compute transitive closure of graph
                for (int mid = 0; mid < observation.operations.size(); mid++)
                    for (int src = 0; src < observation.operations.size(); src++)
                        for (int dest = 0; dest < observation.operations.size(); dest++)
                            if (!adjMatrix[src][dest]) {
                                adjMatrix[src][dest] = adjMatrix[src][mid] && adjMatrix[mid][dest];
//                            changed = changed || adjMatrix[src][dest];    // is it changed?
                            }

                System.out.println("observation " + i + " update graph");
                // update graph based on transitive closure
                for (int k = 0; k < observation.operations.size(); k++) {
                    observation.operations.get(k).edges.clear();
                    for (int l = 0; l < observation.operations.size(); l++) {
                        if (adjMatrix[l][k])
                            observation.operations.get(k).edges.add(l);
                    }
                }

                System.out.println("observation " + i + " compute w'wr");
                // compute w'wr
                for (OpNode opNode : observation.operations) {
                    if (opNode.op == OpNode.OpCode.READ) {
                        int dictatedNode = opNode.dictatedWrite;
                        for (int index : opNode.edges) {
                            OpNode node = observation.operations.get(index);
                            if (node.op == OpNode.OpCode.WRITE && opNode.variable.equals(node.variable)
                                    && !opNode.value.equals(node.value)) {
                                // important statement; otherwise the outer loop won't exit
                                if (observation.operations.get(dictatedNode).edges.contains(node.id))
                                    continue;
                                observation.operations.get(dictatedNode).edges.add(index);
                                changed = true;
                            }
                        }

                    }
                }


            } while (changed);

            for (int diag = 0; diag < observation.operations.size(); diag++) {
                if (adjMatrix[diag][diag]) {
                    System.out.println("PRAM: false, observation " + i + " diag " + diag);
                    return;
                }
            }
        }


        System.out.println("PRAM: true");


    }

    public static void closure(boolean[][] adjMatrix, int start, int end) {
        for (int mid = start; mid < end; mid++)
            for (int src = start; src < end; src++)
                for (int dest = start; dest < end; dest++)
                    if (!adjMatrix[src][dest]) {
                        adjMatrix[src][dest] = adjMatrix[src][mid] && adjMatrix[mid][dest];
//                            changed = changed || adjMatrix[src][dest];    // is it changed?
                    }

    }
}


