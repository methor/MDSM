package nju.cs.extractdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math3.analysis.interpolation.*;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.jfree.chart.*;

import nju.cs.ADBExecutor;

/**
 * Created by Mio on 2016/3/21.
 */
public class ExtractGameState {

    public static void main(String[] args)
    {

        ADBExecutor adbExecutor = new ADBExecutor("adb");
        adbExecutor.copyFromAll("/storage/emulated/0/Android/data/com.njucs.ballgame/files/BallGameDir",
                "log");

        String dsmType = "WeakDsm";

        File rootDir = new File("log/");
        ArrayList<File> deviceDirs = new ArrayList<>();
        for (File dir : rootDir.listFiles())
            deviceDirs.add(dir);
        ArrayList<File> allFiles = new ArrayList<>();
        for (File dir : deviceDirs)
            allFiles.addAll(Arrays.asList(dir.listFiles()));

        // UserLatency
        ArrayList<File> latestSpecifiedFiles = getLatestForPattern(allFiles, "UserLatency.*" + dsmType + ".*", deviceDirs.size());
        System.out.println(Arrays.toString(latestSpecifiedFiles.toArray(new File[0])));
        try {
            FileWriter fileWriter = new FileWriter("UserLatency.txt", true);
            fileWriter.append(latestSpecifiedFiles.get(0).getName() +
                    " " + averageForAllFilesNumbers(latestSpecifiedFiles) + "\n");
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // RoundLatency
        latestSpecifiedFiles = getLatestForPattern(allFiles, "RoundLatency.*" + dsmType + ".*", deviceDirs.size());
        System.out.println(Arrays.toString(latestSpecifiedFiles.toArray(new File[0])));
        try {
            FileWriter fileWriter = new FileWriter("RoundLatency.txt", true);
            fileWriter.append(latestSpecifiedFiles.get(0).getName() +
            " " + averageForAllFilesNumbers(latestSpecifiedFiles) + "\n");
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // divergence
        latestSpecifiedFiles = getLatestForPattern(allFiles, "dsm.*" + dsmType + ".*", deviceDirs.size());
        System.out.println(Arrays.toString(latestSpecifiedFiles.toArray(new File[0])));
        double[] divergence = measureDivergence(latestSpecifiedFiles);
        try {
            FileWriter fileWriter = new FileWriter("Divergence.txt", true);
            fileWriter.append(latestSpecifiedFiles.get(0).getName() +
            " " + divergence[0] + " " + divergence[1] + " " + divergence[2] + "\n");
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


//        if (args.length < 2)
//            throw new IllegalArgumentException();
//        List<File> logDirs = new ArrayList<File>();
//        for (String arg : args)
//        {
//            logDirs.add(new File("log/" + arg));
//        }
//        for (int i = 0; i < logDirs.size(); i++) {
//            File[] logFiles = logDirs.get(i).listFiles();
//            File dumpPath = new File("/home/mio/matlab/ballgame/" + args[i]);
//            if (!dumpPath.exists()) {
//                dumpPath.mkdir();
//            }
//
//
//            for (int j = 0; j < logFiles.length; j++) {
//                File logFile = logFiles[j];
//
//                if (new File(dumpPath.getPath(), logFile.getName()).exists())
//                    continue;
//
//                if (logFile.getName().startsWith("NetworkLatency") ||
//                        logFile.getName().startsWith("UserLatency")) {
//                    Path sPath = FileSystems.getDefault().getPath(logFile.getPath());
//                    Path dPath = FileSystems.getDefault().getPath(dumpPath.getPath(), logFile.getName());
//                    try {
//                        Files.copy(sPath, dPath);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    try (
//                            BufferedReader bufferedReader = new BufferedReader(new FileReader(logFile));
//                            PrintWriter printWriter = new PrintWriter(dumpPath + "/" + logFile.getName(), "utf-8");
//
//                    ) {
//                        String s1 = null;
//                        while ((s1 = bufferedReader.readLine()) != null) {
//                            SnapShot snapShot1 = null;
//                            try {
//                                snapShot1 = SnapShot.fromString(s1);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                                System.err.println(logFile.getPath());
//                                System.err.println(s1);
//                            }
//
//                            printWriter.print(snapShot1.time + ",");
//                            for (int k = 0; k < snapShot1.ballList.size(); k++) {
//                                Ball ball = snapShot1.ballList.get(k);
//                                printWriter.print(ball.getX() + ",");
//                                printWriter.print(ball.getY() + ",");
//                                printWriter.print(ball.getSpeedX() + ",");
//                                printWriter.print(ball.getSpeedY() + ",");
//                                printWriter.print(ball.getAccelarationX() + ",");
//                                printWriter.print(ball.getAccelarationY());
//                                printWriter.print((k != (snapShot1.ballList.size() - 1) ?
//                                        "," : '\n'));
//                            }
//
//                        }
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }



    }

    /*public static float diffSnapShot(SnapShot s1, SnapShot s2)
    {
        float bias = 0f;
        for (int i = 0; i < s1.ballList.size(); i++)
        {
            Ball ball1 = s1.ballList.get(i);
            Ball ball2 = s2.ballList.get(i);
            float dist = dist(ball1.getX(), ball1.getY(),
                    ball2.getX(), ball2.getY());
            if (ball1.getBallID() != SnapShot.GOALBALLID)
                bias += dist;
            else
                bias += 2 * dist;

        }

        return bias;

    }

    public static float dist(float x1, float y1, float x2, float y2)
    {
        return (float) Math.sqrt(
                Math.pow((double) (x1 - x2), 2d) +
                        Math.pow((double) (y1 - y2), 2d)
        );

    }*/

    public static ArrayList<File> getLatestForPattern(ArrayList<File> allFiles, String regex, int size)
    {
        Pattern pattern = Pattern.compile(regex);
        ArrayList<File> filteredFiles = new ArrayList<>();
        for (File file : allFiles)
        {
            Matcher matcher = pattern.matcher(file.getName());
            if (matcher.matches())
                filteredFiles.add(file);
        }
        Collections.sort(filteredFiles, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        ArrayList<File> result = new ArrayList<>();
        for (int i = 0; i < size; i++)
            result.add(filteredFiles.get(filteredFiles.size() - 1 - i));
        return result;

    }

    public static double averageForAllFilesNumbers(ArrayList<File> allSpecifiedFiles)
    {
        int lineNum = 0;
        double sum = 0;
        for (File file : allSpecifiedFiles)
        {
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new FileReader(file));
                String s = null;
                while ((s = bufferedReader.readLine()) != null)
                {
                    sum += Double.valueOf(s);
                    lineNum++;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sum / lineNum;
    }

    public static double[] measureDivergence(ArrayList<File> snapShotFiles)
    {
        int lineNum = 0;
        double sum = 0;
        ArrayList<ArrayList<SnapShot> > listofSnapShotList = new ArrayList<>();
        for (File file : snapShotFiles)
        {
            listofSnapShotList.add(new ArrayList<SnapShot>());
            ArrayList<SnapShot> snapShotList = listofSnapShotList.get(listofSnapShotList.size() - 1);
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new FileReader(file));
                String s = null;
                while ((s = bufferedReader.readLine()) != null)
                {
                    SnapShot snapShot = SnapShot.fromString(s);
                    snapShotList.add(snapShot);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        ArrayList<SnapShot> firstSnapshots = new ArrayList<>();
        for (ArrayList<SnapShot> snapShotList : listofSnapShotList)
            firstSnapshots.add(snapShotList.get(0));
        Collections.sort(firstSnapshots, new Comparator<SnapShot>() {
            @Override
            public int compare(SnapShot o1, SnapShot o2) {
                return Long.valueOf(o2.time).compareTo(Long.valueOf(o1.time));  //reversed order
            }
        });
        long startTime = firstSnapshots.get(0).time;

        ArrayList<SnapShot> lastSnapshots = new ArrayList<>();
        for (ArrayList<SnapShot> snapShotList : listofSnapShotList)
            lastSnapshots.add(snapShotList.get(snapShotList.size() - 1));
        Collections.sort(lastSnapshots, new Comparator<SnapShot>() {
            @Override
            public int compare(SnapShot o1, SnapShot o2) {
                return Long.valueOf(o1.time).compareTo(Long.valueOf(o2.time));  //natural order
            }
        });
        long endTime = lastSnapshots.get(0).time;

        int interpolationPointNumber = listofSnapShotList.get(0).size();
        double[] interpolationArgument = new double[interpolationPointNumber];
        int snapShotParametersNumber = 6 * (listofSnapShotList.size() + 1);

        for (int i = 0; i < interpolationArgument.length; i++)
            interpolationArgument[i] = startTime + (double)i / interpolationPointNumber * (endTime - startTime);
        ArrayList<Double[][]> ballsStatesList = new ArrayList<>();
        for (int i = 0; i < listofSnapShotList.size(); i++) {
            ballsStatesList.add(new Double[1 + snapShotParametersNumber][listofSnapShotList.get(i).size()]);
            Double[][] ballsStates = ballsStatesList.get(ballsStatesList.size() - 1);
            ArrayList<SnapShot> snapShotList = listofSnapShotList.get(i);
            for (int j = 0; j < snapShotList.size(); j++) {
                SnapShot snapShot = snapShotList.get(j);
                ballsStates[0][j] = (double)snapShot.time;
                for (int k = 0; k < snapShot.ballList.size(); k++) {
                    Ball ball = snapShot.ballList.get(k);
                    ballsStates[k * 6 + 1][j] = (double) ball.getX();
                    ballsStates[k * 6 + 2][j] = (double) ball.getY();
                    ballsStates[k * 6 + 3][j] = (double) ball.getSpeedX();
                    ballsStates[k * 6 + 4][j] = (double) ball.getSpeedY();
                    ballsStates[k * 6 + 5][j] = (double) ball.getAccelarationX();
                    ballsStates[(k + 1) * 6][j] = (double) ball.getAccelarationY();
                }
            }
        }

        ArrayList<Double[][]> interpolationValuesList = new ArrayList<>();
        for (int i = 0; i < listofSnapShotList.size(); i++) {
            interpolationValuesList.add(new Double[snapShotParametersNumber][interpolationPointNumber]);
            Double[][] ballsStates = ballsStatesList.get(i);
            Double[][] interpolationValues = interpolationValuesList.get(interpolationValuesList.size() - 1);
            for (int j = 0; j < snapShotParametersNumber; j++) {
                SplineInterpolator splineInterpolator = new SplineInterpolator();
                PolynomialSplineFunction polynomialSplineFunction =
                        splineInterpolator.interpolate(Util.doubleToPrimitives(ballsStates[0]),
                                Util.doubleToPrimitives(ballsStates[j + 1]));
                for (int k = 0; k < interpolationPointNumber; k++)
                    interpolationValues[j][k] = polynomialSplineFunction.value(interpolationArgument[k]);
            }
        }

        double[] divergence = new double[3];
        for (int i = 0; i < divergence.length; i++)
            divergence[i] = 0;

        // for every pair of devices i and j
        for (int i = 0; i < listofSnapShotList.size(); i++)
        {
            for (int j = i + 1; j < listofSnapShotList.size(); j++)
            {
                Double[][] interpolationValuesI = interpolationValuesList.get(i);
                Double[][] interpolationValuesJ = interpolationValuesList.get(j);
                // for every ball k
                for (int k = 0; k < listofSnapShotList.size() + 1; k++)
                {
                   // for every interpolation  point
                    for (int m = 0; m < interpolationPointNumber; m++)
                    {
                        int factor = 1;
                        // double penalty for goal ball
                        if (k == listofSnapShotList.size())
                            factor = 2;
                        divergence[0] += factor * Math.sqrt(
                                Math.pow(interpolationValuesI[k * 6][m] - interpolationValuesJ[k * 6][m], 2d)
                                + Math.pow(interpolationValuesI[k * 6 + 1][m] - interpolationValuesJ[k * 6 + 1][m], 2d)
                        );
                        divergence[1] += factor * Math.sqrt(
                                Math.pow(interpolationValuesI[k*6+2][m] - interpolationValuesJ[k*6+2][m], 2d)
                                + Math.pow(interpolationValuesI[k*6+3][m] - interpolationValuesJ[k*6+3][m], 2d)
                        );
                        divergence[2] += factor * Math.sqrt(
                                Math.pow(interpolationValuesI[k*6+4][m] - interpolationValuesJ[k*6+4][m], 2d)
                                        + Math.pow(interpolationValuesI[k*6+5][m] - interpolationValuesJ[k*6+5][m], 2d)
                        );
                    }
                }
            }
        }
        // take average for divergences
        for (int i = 0; i < divergence.length; i++)
            divergence[i] /= (interpolationPointNumber * (listofSnapShotList.size() + 1) *
                    (listofSnapShotList.size() + 1) * listofSnapShotList.size() / 2);

        // normalize divergences
        divergence[0] /= Math.sqrt(2);
        divergence[1] /= Math.sqrt(Math.pow(2*Constant.MAX_SPEED_HORIZONTAL_NORM, 2d)
        + Math.pow(2*Constant.MAX_SPEED_VERTICAL_NORM, 2d));
        divergence[2] /= Math.sqrt(Math.pow(2*Constant.MAX_ACC_HORIZONTAL_NORM, 2d)
        + Math.pow(2*Constant.MAX_ACC_VERTICAL_NORM, 2d));

        return divergence;

    }
}
