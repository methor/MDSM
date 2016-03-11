package model;


import android.graphics.RectF;

import constant.Constant;

/**
 * Created by CS on 2015/9/19.
 * the field where players battle.
 */
public enum Field {

    INSTANCE;

    private RectF rectF;
    private Goal[] goals;

    public Goal[] getGoals() {
        return goals;
    }

    public void setGoals(Goal[] goals) {
        this.goals = goals;
    }

    public RectF getRectF() {
        return rectF;
    }

    public void setRectF(RectF rectF) {
        this.rectF = rectF;
    }


    /**
      define the goal area of both side
     */
    public class Goal {

        RectF rectF;

        public Goal(RectF rect) {
            rectF = rect;
        }

        public RectF getRectF() {
            return rectF;
        }

        public void setRectF(RectF rectF) {
            this.rectF = rectF;
        }
    }


    public void initiate() {
        rectF = new RectF(Constant.FIELD_X, Constant.FIELD_Y, Constant.FIELD_X + Constant.FIELD_WIDTH,
                Constant.FIELD_Y + Constant.FIELD_HEIGHT);
        goals = new Goal[2];
        goals[0] = new Goal(new RectF(Constant.MY_GOAL_X, Constant.MY_GOAL_Y,
                Constant.FIELD_X + Constant.GOAL_WIDTH + Constant.MY_GOAL_X
                , Constant.FIELD_Y + Constant.GOAL_HEIGHT + Constant.MY_GOAL_Y));
        goals[1] = new Goal(new RectF(Constant.RIVAL_GOAL_X, Constant.RIVAL_GOAL_Y,
                Constant.FIELD_X + Constant.GOAL_WIDTH + Constant.RIVAL_GOAL_X
                , Constant.FIELD_Y + Constant.GOAL_HEIGHT + Constant.RIVAL_GOAL_Y));

    }
}
