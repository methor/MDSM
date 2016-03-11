package view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.njucs.main.MainActivity;
import constant.Constant;
import consistencyinfrastructure.login.SessionManagerWrapper;
import model.*;

import java.util.List;
import java.util.Random;

/**
 * Created by CS on 2015/9/17.
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder surfaceHolder;
    private GameModel model;
    private String orientation;
    private DrawThread drawThread;

    public GameView(Context context, GameModel model) {
        super(context);
        orientation = ((MainActivity) context).orientation;
        surfaceHolder = this.getHolder();
        surfaceHolder.addCallback(this);

        this.model = model;


    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawThread = new DrawThread(this);
        drawThread.setName("ViewDrawer");
        drawThread.start();
        //repaint();


    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        if (drawThread.isAlive())
            drawThread.interrupt();

    }



    private void drawBall(Ball ball, Canvas canvas, Paint paint) {
        float x = getLocalX(ball.getX());
        float y = getLocalY(ball.getY());
        float radius = Constant.BALL_RADIOUS;
        paint.reset();

        if (ball.getBallID() == SessionManagerWrapper.NODEID)
                paint.setColor(Color.GREEN);
        else if (ball.getBallID() == GameModel.GOALBALLID)
        {
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2.0f);
        }
        else
        {
            paint.setColor(Color.RED);
        }

       canvas.drawCircle(x, y, radius, paint);
    }

    private void drawField(Field field, Canvas canvas, Paint paint) {
        RectF rectF = field.getRectF();
        paint.reset();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        canvas.drawRect(rectF, paint);
        //canvas.drawRect(new RectF(0f, 0f, 270f, 160.8f), paint);
        //canvas.drawRect(new RectF(540f, 1000f, 270f, 160.8f), paint);

        Field.Goal[] goals = field.getGoals();
        for (int i = 0; i < 2; i++) {
            canvas.drawRect(goals[i].getRectF(), paint);
        }


    }

    public void repaint() {
        if (surfaceHolder == null)
            return;
        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas == null)
            return;
        canvas.drawColor(Color.WHITE);
        Paint paint = new Paint();

        List<Ball> ballList;
        ballList = new SnapShot(model).ballList;


        drawField(Field.INSTANCE, canvas, paint);
       /* SnapShot snapShot = new SnapShot(myBall, rivalBall, goalBall, 0);
        motionCalculate.obtainSnapshot(snapShot);
        motionCalculate.updateAll((float) Constant.REPAINT_WINDOW_SLIDE_IN_SECOND);*/

        int y = 0;
        for (Ball ball : ballList)
        {
            drawBall(ball, canvas, paint);
            paint.setTextSize(20);
            canvas.drawText(ball.getBallID() + "[x=" + ball.getX() + ",y=" + ball.getY(), 0, y = (y + 40), paint);
            canvas.drawText("    accX=" + ball.getAccelarationX() + ",accY=" + ball.getAccelarationY(), 0, y = (y + 40), paint);
            canvas.drawText("    sX=" + ball.getSpeedX() + ",sY=" + ball.getSpeedY(), 0, y = (y + 40), paint);
        }
        Random random = new Random();

        canvas.drawText(String.valueOf(random.nextLong()), 0, Constant.FIELD_HEIGHT / 2, paint);

        surfaceHolder.unlockCanvasAndPost(canvas);

/*
        paint.setTextSize(40);
        canvas.drawText("I x: " + Constant.encodeAccelearteX(myBall.getAccelarationX()) + " y: " + Constant.encodeAccelerateY(gameModel.getMyBallTransient().getAccelarationY()), 0, 80, paint);
        canvas.drawText("Rival x: " + Constant.encodeAccelearteX(rivalBall.getAccelarationX()) + " y: " + Constant.encodeAccelerateY(gameModel.getRivalBallTransient().getAccelarationY()), 0, 160, paint);
        surfaceHolder.unlockCanvasAndPost(canvas);


        String s = String.valueOf(Constant.encodeX(myBall.getX())) + " " + String.valueOf(Constant.encodeY(myBall.getY()))
                + " " + String.valueOf(Constant.encodeSpeedX(myBall.getSpeedX())) + " " + String.valueOf(Constant.encodeSpeedY(myBall.getSpeedY()))
                + " " + String.valueOf(Constant.encodeAccelearteX(myBall.getAccelarationX())) + " " + String.valueOf(Constant.encodeAccelerateY(myBall.getAccelarationY()))
                + " " + String.valueOf(Constant.encodeX(rivalBall.getX())) + " " + String.valueOf(Constant.encodeY(rivalBall.getY()))
                + " " + String.valueOf(Constant.encodeSpeedX(rivalBall.getSpeedX())) + " " + String.valueOf(Constant.encodeSpeedY(rivalBall.getSpeedY()))
                + " " + String.valueOf(Constant.encodeAccelearteX(rivalBall.getAccelarationX())) + " " + String.valueOf(Constant.encodeAccelerateY(rivalBall.getAccelarationY()))
                + " " + String.valueOf(Constant.encodeX(goalBall.getX())) + " " + String.valueOf(Constant.encodeY(goalBall.getY()))
                + " " + String.valueOf(Constant.encodeSpeedX(goalBall.getSpeedX())) + " " + String.valueOf(Constant.encodeSpeedY(goalBall.getSpeedY()))
                + " " + String.valueOf(Constant.encodeAccelearteX(goalBall.getAccelarationX())) + " " + String.valueOf(Constant.encodeAccelerateY(goalBall.getAccelarationY()));

        logParamsToFile.put(s);*/
    }

    private float getLocalX(float x)
    {
        float rx = x * Constant.FIELD_WIDTH;
        if (orientation.equals(GameModel.ORIENTATION_NORTH))
            return rx + Constant.FIELD_X;
        else
        {
            return Constant.FIELD_WIDTH - rx + Constant.FIELD_X;
        }
    }

    private float getLocalY(float y)
    {
        float ry = y * Constant.FIELD_HEIGHT;
        if (orientation.equals(GameModel.ORIENTATION_NORTH))
            return ry + Constant.FIELD_Y;
        else
        {
            return Constant.FIELD_HEIGHT - ry + Constant.FIELD_Y;
        }
    }

   /* private float getLocalSpeedX(float sx)
    {
        float rsx = sx / Constant.MAX_SPEED_HORIZONTAL_NORM * Constant.FIELD_WIDTH;
        if (orientation.equals(GameModel.ORIENTATION_NORTH))
            return rsx;
        else
        {
            return -rsx;
        }
    }

    private float getLocalSpeedY(float sy)
    {
        float rsy = sy / Constant.MAX_SPEED_VERTICAL_NORM * Constant.FIELD_HEIGHT;
        if (orientation.equals(GameModel.ORIENTATION_NORTH))
            return rsy;
        else
        {
            return -rsy;
        }
    }

    private float getLocalAccX(float ax)
    {
        float rax = ax / Constant.MAX_ACC_HORIZONTAL_NORM * Constant.FIELD_WIDTH;
        if (orientation.equals(GameModel.ORIENTATION_NORTH))
            return rax;
        else
        {
            return -rax;
        }
    }

    private float getLocalAccY(float ay)
    {
        float ray = ay / Constant.MAX_ACC_VERTICAL_NORM * Constant.FIELD_HEIGHT;
        if (orientation.equals(GameModel.ORIENTATION_NORTH))
            return ray;
        else
        {
            return -ray;
        }
    }
*/

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        //this.setKeepScreenOn(true);
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        //this.setKeepScreenOn(false);
    }

    class DrawThread extends Thread {
        GameView gameView;

        public DrawThread(GameView view) {
            gameView = view;
        }
        @Override
        public void run() {
            while(true) {
                gameView.repaint();
                try {
                    Thread.sleep(Constant.REPAINT_WINDOW_SLIDE_IN_MILLISECOND, 0);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }
}
