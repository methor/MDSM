package nju.cs.extractdata;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Mio on 2016/3/21.
 */
public class ExtractGameState {

    public static void main(String[] args)
    {

        System.out.println(ExtractGameState.class.getSimpleName());
        if (args.length < 2)
            throw new IllegalArgumentException();
        try (
                BufferedReader bufferedReader1 = new BufferedReader(new FileReader(args[0]));
                BufferedReader bufferedReader2 = new BufferedReader(new FileReader(args[1]));
                PrintWriter printWriter1 = new PrintWriter("deviceNo1.dat", "utf-8");
                PrintWriter printWriter2 = new PrintWriter("deviceNo2.dat", "utf-8");

        )
        {
            String s1 = null;
            String s2 = null;
            while ((s1 = bufferedReader1.readLine()) != null &&
                    (s2 = bufferedReader2.readLine()) != null)
            {
                SnapShot snapShot1 = SnapShot.fromString(s1);
                SnapShot snapShot2 = SnapShot.fromString(s2);

                printWriter1.print(snapShot1.time + ",");
                printWriter2.print(snapShot2.time + ",");
                for (int i = 0; i < snapShot1.ballList.size(); i++)
                {
                    Ball ball1 = snapShot1.ballList.get(i);
                    Ball ball2 = snapShot2.ballList.get(i);
                    printWriter1.print(ball1.getX() + ",");
                    printWriter1.print(ball1.getY() + ",");
                    printWriter1.print(ball1.getSpeedX() + ",");
                    printWriter1.print(ball1.getSpeedY() + ",");
                    printWriter1.print(ball1.getAccelarationX() + ",");
                    printWriter1.print(ball1.getAccelarationY());
                    printWriter1.print((i != (snapShot1.ballList.size() - 1) ?
                            "," : '\n'));

                    printWriter2.print(ball2.getX() + ",");
                    printWriter2.print(ball2.getY() + ",");
                    printWriter2.print(ball2.getSpeedX() + ",");
                    printWriter2.print(ball2.getSpeedY() + ",");
                    printWriter2.print(ball2.getAccelarationX() + ",");
                    printWriter2.print(ball2.getAccelarationY());
                    printWriter2.print((i != (snapShot1.ballList.size() - 1) ?
                            "," : '\n'));
                }

            }

        } catch (IOException e)
        {
            e.printStackTrace();
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
