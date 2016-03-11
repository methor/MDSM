package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by CS on 2015/9/18.
 */
public class Ball implements Serializable{

    private static final long serialVersionUID = 8822308586080932227L;
    private int ballID;
    private float accelarationX;                   //accelaration in X axis
    private float accelarationY;                //accelaration in Y axis
    private float speedX;                          //speed in X axis
    private float speedY;                       //speed in Y axis
    private float X;                     //coordinate of X axis
    private float Y;                     //coordinate of Y axis


    public List<Boolean> collisionFlagsWithBalls = new ArrayList<>();
    public List<Boolean> collisionFlagsWithField = new ArrayList<>();


    /**
     * we have two versions of get/set functions.
     * directGetXX/directSetXX directly get and set the value,
     * whereas getXX/setXX get and set the value through the DSM delegation.
     */

    public int getBallID()
    {
        return ballID;
    }

    public void setBallID(int ballID)
    {
        this.ballID = ballID;
    }

    public float getAccelarationX()
    {
        return accelarationX;
    }

    public void setAccelarationX(float accelarationX)
    {
        this.accelarationX = accelarationX;
    }

    public float getAccelarationY()
    {
        return accelarationY;
    }

    public void setAccelarationY(float accelarationY)
    {
        this.accelarationY = accelarationY;
    }

    public float getSpeedX()
    {
        return speedX;
    }

    public void setSpeedX(float speedX)
    {
        this.speedX = speedX;
    }

    public float getSpeedY()
    {
        return speedY;
    }

    public void setSpeedY(float speedY)
    {
        this.speedY = speedY;
    }

    public float getX()
    {
        return X;
    }

    public void setX(float x)
    {
        this.X = x;
    }

    public float getY()
    {
        return Y;
    }


    public void setY(float y)
    {
        this.Y = y;
    }


    private Ball()
    {

    }

    public Ball(String orientation, int id)
    {
        accelarationX = 0;
        accelarationY = 0;
        speedX = 0;
        speedY = 0;
        X = 0.5f;
        ballID = id;
        if (orientation.equals(GameModel.ORIENTATION_NORTH))
            Y = 0.75f;
        else if (orientation.equals(GameModel.ORIENTATION_SOUTH))
            Y = 0.25f;
        else
            Y = 0.5f;


    }

    public Ball(Ball ball)
    {
        this.X = ball.X;
        this.Y = ball.Y;
        this.speedX = ball.speedX;
        this.speedY = ball.speedY;
        this.accelarationX = ball.accelarationX;
        this.accelarationY = ball.accelarationY;
        this.ballID = ball.ballID;

        this.collisionFlagsWithField = new ArrayList<>(ball.collisionFlagsWithField);
        this.collisionFlagsWithBalls = new ArrayList<>(ball.collisionFlagsWithBalls);

    }

    public void directSetAccelarationX(float accelarationX)
    {
        this.accelarationX = accelarationX;
    }

    public void directSetAccelarationY(float accelarationY)
    {
        this.accelarationY = accelarationY;
    }

    @Override
    public String toString()
    {
        return "Ball{" +
                "ballID=" + ballID +
                ", accelarationX=" + accelarationX +
                ", accelarationY=" + accelarationY +
                ", speedX=" + speedX +
                ", speedY=" + speedY +
                ", X=" + X +
                ", Y=" + Y +
                ", collisionFlagsWithBalls=" + collisionFlagsWithBalls +
                ", collisionFlagsWithField=" + collisionFlagsWithField +
                '}';
    }

    public static Ball fromString(String s)
    {
        Pattern p = Pattern.compile("Ball\\{(.*)\\}");
        Matcher matcher = p.matcher(s);
        matcher.find();

        String info = matcher.group(1);
        p = Pattern.compile("([a-zA-Z]+)=\\[?([\\w.-]+)]?, ");
        matcher = p.matcher(info);


        Ball ball = new Ball();
        while (matcher.find())
        {
            String key = matcher.group(1);
            String value = matcher.group(2);
            switch (key)
            {
                case "ballID":
                    ball.setBallID(Integer.valueOf(value));
                    break;
                case "X":
                    ball.setX(Float.valueOf(value));
                    break;
                case "Y":
                    ball.setY(Float.valueOf(value));
                    break;
                case "speedX":
                    ball.setSpeedX(Float.valueOf(value));
                    break;
                case "speedY":
                    ball.setSpeedY(Float.valueOf(value));
                    break;
                case "accelarationX":
                    ball.setAccelarationX(Float.valueOf(value));
                    break;
                case "accelarationY":
                    ball.setAccelarationY(Float.valueOf(value));
                    break;
                case "collisionFlagsWithBalls":
                case "collisionFlagsWithField":
                    int start = matcher.start(2);
                    int end = info.indexOf("]", start);
                    String[] items = info.substring(start, end).split(", ");
                    List<Boolean> booleanList = new ArrayList<>();
                    for (String item : items)
                        booleanList.add(Boolean.valueOf(item));
                    if (key.equals("collisionFlagsWithBalls"))
                        ball.collisionFlagsWithBalls = booleanList;
                    else
                        ball.collisionFlagsWithField = booleanList;
                    break;
                default:
                    break;
            }
        }

        return ball;
    }
}
