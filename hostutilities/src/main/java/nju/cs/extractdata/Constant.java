package nju.cs.extractdata;

/**
 * Created by CS on 2015/9/19.
 */
public class Constant {

    public static float BALL_RADIOUS;
    public static float RADIUS_NORM;
    public static float FIELD_HEIGHT;
    public static float FIELD_WIDTH;
    public static float FIELD_X;
    public static float FIELD_Y;
    public static float MY_GOAL_X;
    public static float MY_GOAL_Y;
    public static float RIVAL_GOAL_X;
    public static float RIVAL_GOAL_Y;
    public static float GOAL_HEIGHT;
    public static float GOAL_WIDTH;

    public static int REPAINT_WINDOW_SLIDE_IN_MILLISECOND = 20;

    public static float TIME_ACROSS_FIELD = 4f;    /* constant accelarate with max_acc from 0 to max speed */


    public static float MAX_SPEED_HORIZONTAL_NORM = 1f / TIME_ACROSS_FIELD;
    public static float MAX_SPEED_VERTICAL_NORM = 1f / TIME_ACROSS_FIELD;
    public static float MAX_ACC_HORIZONTAL_NORM = 1 / MAX_SPEED_HORIZONTAL_NORM;
    public static float MAX_ACC_VERTICAL_NORM = 1 / MAX_SPEED_VERTICAL_NORM;

    public static int SERVERPORT = 7892;



    public static void constantInit(float fieldHeight, float fieldWidth, float fieldX, float fieldY) {

        FIELD_HEIGHT = fieldHeight;
        FIELD_WIDTH = fieldWidth;
        FIELD_X = fieldX;
        FIELD_Y = fieldY;


        BALL_RADIOUS = FIELD_WIDTH / 24;
        RADIUS_NORM = BALL_RADIOUS / FIELD_WIDTH;
        GOAL_HEIGHT = FIELD_HEIGHT / 10;
        GOAL_WIDTH = FIELD_WIDTH / 4;
        RIVAL_GOAL_X = FIELD_X + FIELD_WIDTH * 3 / 8;
        RIVAL_GOAL_Y = FIELD_Y;
        MY_GOAL_X = RIVAL_GOAL_X;
        MY_GOAL_Y = FIELD_Y + FIELD_HEIGHT - GOAL_HEIGHT;

    }



}
