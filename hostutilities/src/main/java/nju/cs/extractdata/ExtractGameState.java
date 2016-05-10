package nju.cs.extractdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mio on 2016/3/21.
 */
public class ExtractGameState {

    public static void main(String[] args)
    {

        if (args.length < 2)
            throw new IllegalArgumentException();
        List<File> logDirs = new ArrayList<File>();
        for (String arg : args)
        {
            logDirs.add(new File("log/" + arg));
        }
        for (int i = 0; i < logDirs.size(); i++) {
            File[] logFiles = logDirs.get(i).listFiles();
            File dumpPath = new File("/home/mio/matlab/ballgame/" + args[i]);
            if (!dumpPath.exists()) {
                dumpPath.mkdir();
            }


            for (int j = 0; j < logFiles.length; j++) {
                File logFile = logFiles[j];

                if (new File(dumpPath.getPath(), logFile.getName()).exists())
                    continue;

                if (logFile.getName().startsWith("NetworkLatency") ||
                        logFile.getName().startsWith("UserLatency")) {
                    Path sPath = FileSystems.getDefault().getPath(logFile.getPath());
                    Path dPath = FileSystems.getDefault().getPath(dumpPath.getPath(), logFile.getName());
                    try {
                        Files.copy(sPath, dPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try (
                            BufferedReader bufferedReader = new BufferedReader(new FileReader(logFile));
                            PrintWriter printWriter = new PrintWriter(dumpPath + "/" + logFile.getName(), "utf-8");

                    ) {
                        String s1 = null;
                        while ((s1 = bufferedReader.readLine()) != null) {
                            SnapShot snapShot1 = null;
                            try {
                                snapShot1 = SnapShot.fromString(s1);
                            } catch (Exception e) {
                                e.printStackTrace();
                                System.err.println(logFile.getPath());
                                System.err.println(s1);
                            }

                            printWriter.print(snapShot1.time + ",");
                            for (int k = 0; k < snapShot1.ballList.size(); k++) {
                                Ball ball = snapShot1.ballList.get(k);
                                printWriter.print(ball.getX() + ",");
                                printWriter.print(ball.getY() + ",");
                                printWriter.print(ball.getSpeedX() + ",");
                                printWriter.print(ball.getSpeedY() + ",");
                                printWriter.print(ball.getAccelarationX() + ",");
                                printWriter.print(ball.getAccelarationY());
                                printWriter.print((k != (snapShot1.ballList.size() - 1) ?
                                        "," : '\n'));
                            }

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }



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
}
