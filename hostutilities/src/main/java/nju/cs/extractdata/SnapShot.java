package nju.cs.extractdata;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Mio on 2015/11/23.
 */
public class SnapShot {
    public static final int GOALBALLID = 0;

    public List<Ball> ballList = new ArrayList<>();
    public long time = System.currentTimeMillis();


    private SnapShot()
    {
    }

    @Override
    public String toString()
    {
        return "SnapShot{" +
                "ballList=" + ballList +
                ", time=" + time +
                '}';
    }

    public Ball findBall(int id)
    {
        for (Ball ball : ballList)
        {
            if (ball.getBallID() == id)
                return ball;
        }

        return null;
    }

    public void setBall(Ball ball)
    {
        for (int i = 0; i < ballList.size(); i++)
        {
            if (ball.getBallID() == ballList.get(i).getBallID())
                ballList.set(i, ball);
        }
    }

    public static SnapShot fromString(String s)
    {
        SnapShot snapShot = new SnapShot();

        Pattern p = Pattern.compile("SnapShot\\{(.*)\\}");
        Matcher matcher = p.matcher(s);
        matcher.find();

        String info = matcher.group(1);
        int start = info.indexOf("ballList=") + "ballList=".length();
        int end = Util.findCharPair(info, start);
        String balls = info.substring(start + 1, end);

        int start1 = 0, end1;
        while (true)
        {
            start1 = balls.indexOf("Ball{", start1);
            end1 = Util.findCharPair(balls, start1 + 4);
            if (end1 == -1)
                break;
            snapShot.ballList.add(Ball.fromString(balls.substring(start1, end1 + 1)));
            start1 = end1;
        }

        start = info.indexOf("time=", end);
        snapShot.time = Long.valueOf(info.substring(start + 5));

        return snapShot;
    }
}
