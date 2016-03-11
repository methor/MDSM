package calc;

import android.graphics.PointF;

import constant.Constant;
import model.Ball;
import model.Field;
import model.SnapShot;

/**
 * Created by CS on 2015/9/24.
 * These functions require values of model entities.
 * So to preserve "some internal consistency(determined in later work),
 * I may insert some code in all of the getXXX() and setXXX() functions or
 * alternatively apply some rules to these functions themselves.
 */
public enum MotionCalculate {

    INSTANCE;

    private static Field field = Field.INSTANCE;
    private SnapShot snapShot;

    public void obtainSnapshot(SnapShot snapShot) {
        this.snapShot = snapShot;
    }

    public SnapShot getSnapShot() {
        return snapShot;
    }




    /**
     * @param roundTime
     */
    public void collisionDetectWithBalls(float roundTime) {

        for (int i = 0; i < snapShot.ballList.size(); i++)
        {
            Ball ballA = snapShot.ballList.get(i);
            for (int j = i + 1; j < snapShot.ballList.size(); j++)
            {
                Ball ballB = snapShot.ballList.get(j);

                float xA = ballA.getX();
                float yA = ballA.getY();
                float vXA = ballA.getSpeedX();
                float vYA = ballA.getSpeedY();
                float aXA = ballA.getAccelarationX();
                float aYA = ballA.getAccelarationY();

                double displacementXA = displacement(aXA, vXA, roundTime);
                double displacementYA = displacement(aYA, vYA, roundTime);
                double newXA = xA + displacementXA;
                double newYA = yA + displacementYA;

                float xB = ballB.getX();
                float yB = ballB.getY();
                float vXB = ballB.getSpeedX();
                float vYB = ballB.getSpeedY();
                float aXB = ballB.getAccelarationX();
                float aYB = ballB.getAccelarationY();

                double displacementXB = displacement(aXB, vXB, roundTime);
                double displacementYB = displacement(aYB, vYB, roundTime);
                double newXB = xB + displacementXB;
                double newYB = yB + displacementYB;

                if (Math.sqrt(Math.pow(newXA - newXB, 2d) +
                        Math.pow((newYA - newYB) * Constant.FIELD_HEIGHT / Constant.FIELD_WIDTH, 2d))
                        < 2 * Constant.RADIUS_NORM)
                {
                    ballA.collisionFlagsWithBalls.add(true);
                }
                else
                    ballA.collisionFlagsWithBalls.add(false);



            }
        }

    }

    public void collisionDetectWithField(float roundTime) {
        for (int i = 0; i < snapShot.ballList.size(); i++)
        {
            Ball ball = snapShot.ballList.get(i);

            float x = ball.getX();
            float y = ball.getY();
            float vX = ball.getSpeedX();
            float vY = ball.getSpeedY();
            float aX = ball.getAccelarationX();
            float aY = ball.getAccelarationY();
            double displacementX = displacement(aX, vX, roundTime);
            double displacementY = displacement(aY, vY, roundTime);

            // left-top corner and right-bottom corner of the rectangle encompass the ball
            double newXLeftTop = x + displacementX - Constant.RADIUS_NORM;
            double newYLeftTop = y + displacementY - Constant.RADIUS_NORM;
            double newXRightBottom = x + displacementX + Constant.RADIUS_NORM;
            double newYRightBottom = y + displacementY + Constant.RADIUS_NORM;

            // collide with field top
            if (newYLeftTop <= 0 && (newXLeftTop > 0 || newXLeftTop >= newYLeftTop) &&
                    (newXRightBottom <= 1 || newXRightBottom - 1 <= -newYLeftTop))
                ball.collisionFlagsWithField.add(true);
            else
                ball.collisionFlagsWithField.add(false);

            // collide with field bottom
            if (newYRightBottom >= 1 && (newXRightBottom <= 1 || newYRightBottom >= newXRightBottom) &&
                    (newXLeftTop >= 0 || newYRightBottom - 1 >= -newXLeftTop))
                ball.collisionFlagsWithField.add(true);
            else
                ball.collisionFlagsWithField.add(false);

            // collide with field left
            if (newXLeftTop <= 0 && (newYLeftTop >= 0 || newYLeftTop > newXLeftTop) &&
                    (newYRightBottom <= 1 || newYRightBottom - 1 < -newXLeftTop))
                ball.collisionFlagsWithField.add(true);
            else
                ball.collisionFlagsWithField.add(false);

            // collide with field right
            if (newXRightBottom >= 1 && (newYLeftTop >= 0 || -newYLeftTop < newXRightBottom - 1) &&
                    (newYRightBottom <= 1 || newYRightBottom < newXRightBottom))
                ball.collisionFlagsWithField.add(true);
            else
                ball.collisionFlagsWithField.add(false);

        }

    }


    public void changeSpeedOfBall(float roundTime) {

        for (int i = 0; i < snapShot.ballList.size(); i++)
        {
            Ball ballA = snapShot.ballList.get(i);
            PointF speedA = new PointF(ballA.getSpeedX(), ballA.getSpeedY());
            for (int j = i + 1; j < snapShot.ballList.size(); j++)
            {
                Ball ballB = snapShot.ballList.get(j);

                PointF speedB = new PointF(ballB.getSpeedX(), ballB.getSpeedY());
                PointF lineAB = new PointF(ballA.getX() - ballB.getX(), ballA.getY() - ballB.getY());

                if (ballA.collisionFlagsWithBalls.get(j - i - 1))
                {
                    PointF[] newSpeedFromAOfAB = getNewSpeed(speedA, lineAB);
                    PointF[] newSpeedFromBOfAB = getNewSpeed(speedB, lineAB);
                    speedA = twoSpeedAdd(newSpeedFromBOfAB[0], newSpeedFromAOfAB[1]);
                    speedB = twoSpeedAdd(newSpeedFromAOfAB[0], newSpeedFromBOfAB[1]);

                    ballB.setSpeedX(speedB.x);
                    ballB.setSpeedY(speedB.y);
                    ballA.setAccelarationX(0);
                    ballA.setAccelarationY(0);
                    ballB.setAccelarationX(0);
                    ballB.setAccelarationY(0);
                }
            }

            PointF lineTopAndBottomOfField = new PointF(1f, 0f);
            PointF lineLeftAndRightOfFiled = new PointF(0f, 1f);
            if (ballA.collisionFlagsWithField.get(0) || ballA.collisionFlagsWithField.get(1))
            {
                PointF[] newSpeedFromA = getNewSpeed(speedA, lineTopAndBottomOfField);
                newSpeedFromA[1].y = -newSpeedFromA[1].y;
                speedA = twoSpeedAdd(newSpeedFromA[0], newSpeedFromA[1]);
                ballA.setAccelarationX(0);
                ballA.setAccelarationY(0);
            }
            if (ballA.collisionFlagsWithField.get(2) || ballA.collisionFlagsWithField.get(3))
            {
                PointF[] newSpeedFromA = getNewSpeed(speedA, lineLeftAndRightOfFiled);
                newSpeedFromA[1].x = -newSpeedFromA[1].x;
                speedA = twoSpeedAdd(newSpeedFromA[0], newSpeedFromA[1]);
                ballA.setAccelarationX(0);
                ballA.setAccelarationY(0);
            }

            ballA.setSpeedX(speedA.x);
            ballA.setSpeedY(speedA.y);
        }


    }
    /**
     * update of accelaration has not added yet.
     * @param roundTime
     */
    private void updateParametersOfBall(float roundTime) {

        for (Ball ballA : snapShot.ballList)
        {
            float aXA = ballA.getAccelarationX();
            float aYA = ballA.getAccelarationY();
            float vXA = ballA.getSpeedX();
            float vYA = ballA.getSpeedY();
            float xA = ballA.getX();
            float yA = ballA.getY();
            double displacementXOfA = displacement(aXA, vXA, roundTime);
            double displacementYOfA = displacement(aYA, vYA, roundTime);
            ballA.setX(xA + (float) displacementXOfA);
            ballA.setY(yA + (float) displacementYOfA);
            ballA.setSpeedX(vXA + roundTime * aXA);
            ballA.setSpeedY(vYA + roundTime * aYA);
            speedThreshold(ballA);
        }
    }

    public void updateAll(float roundTime) {

        for (Ball ball : snapShot.ballList)
        {
            ball.collisionFlagsWithField.clear();
            ball.collisionFlagsWithBalls.clear();
        }
        collisionDetectWithBalls(roundTime);
        collisionDetectWithField(roundTime);
        changeSpeedOfBall(roundTime);
        updateParametersOfBall(roundTime);

    }



    private PointF[] getNewSpeed(PointF speed, PointF line) {
        PointF normal = new PointF(line.y, -(line.x));        //normal to the line
        PointF[] newSpeed = new PointF[2];
        newSpeed[0] = getProject(speed, line);
        newSpeed[1] = getProject(speed, normal);

        return newSpeed;
    }

    private float dotProduct(PointF a, PointF b) {
        return a.x * b.x + a.y * b.y;
    }

    private PointF twoSpeedAdd(PointF a, PointF b) {
        return new PointF(a.x + b.x, a.y + b.y);
    }

    private double displacement(float a, float v, float roundTime) {
        return a * Math.pow(roundTime, 2d) / 2 + v * roundTime;
    }

    private PointF getProject(PointF a, PointF b) {
        float c = dotProduct(a, b) / dotProduct(b, b);
        return  new PointF(b.x * c, b.y * c);
    }

    private void speedThreshold(Ball ball) {
        float vX = ball.getSpeedX();
        float vY = ball.getSpeedY();

        if (Math.abs(vX) > Constant.MAX_SPEED_HORIZONTAL_NORM) {
            if (vX < 0)
                ball.setSpeedX(-(Constant.MAX_SPEED_HORIZONTAL_NORM));
            else
                ball.setSpeedX(Constant.MAX_SPEED_HORIZONTAL_NORM);
        }

        if (Math.abs(vY) > Constant.MAX_SPEED_VERTICAL_NORM) {
            if (vY < 0)
                ball.setSpeedY(-(Constant.MAX_SPEED_VERTICAL_NORM));
            else
                ball.setSpeedY((Constant.MAX_SPEED_VERTICAL_NORM));
        }
    }




}
