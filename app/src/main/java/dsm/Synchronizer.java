/*
package dsm;

import android.util.Log;

import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import constant.Constant;
import log.InvokeFrequency;
import model.Ball;
import model.GameModel;
import model.SnapShot;
import view.GameView;

*/
/**
 * Created by Mio on 2015/11/29.
 *//*

public class Synchronizer extends DsmPolicy {

    public static String TAG = Synchronizer.class.getName();
    private int frameNumber = 0;
    private SlidingWindow<SnapShot> slidingWindow = new SlidingWindow<>(10);
    private long timeAtWindowStart;
    private float calculateFrequency = 1 / Constant.ROUND_TIME;
    private long myThreadLastActivate;
    private Queue<float[]> accelarateQueue = new PriorityBlockingQueue<>();

    float[] newestAcc = new float[2];
    float[] lastAcc = new float[2];
    SnapShot newestSnapShot = new SnapShot(new Ball(Ball.ballID.MYBALL), new Ball(Ball.ballID.RIVALBALL),
            new Ball(Ball.ballID.GOALBALL), System.currentTimeMillis());
    Boolean isInitiated = false;

    private InvokeFrequency invokeFrequency = new InvokeFrequency(this);
    private InvokeFrequency sensorFrequency = new InvokeFrequency(this);


    public Synchronizer(GameModel gameModel) {
        super(gameModel);
    }

    public Synchronizer(GameModel gameModel, GameView gameView) {
        super(gameModel, gameView);
    }



    public void initialMessaging() {
        SnapShot firstSnapShot = new SnapShot(new Ball(Ball.ballID.MYBALL),
                new Ball(Ball.ballID.RIVALBALL), new Ball(Ball.ballID.GOALBALL),
                System.currentTimeMillis());
        frameNumber++;
        listener.sendingData(DSM.createMessage(false, frameNumber, calculateFrequency, firstSnapShot, DSM.POLICY.SYNC));
        isInitiated = true;
    }

    @Override
    public void onGettingData(float accX, float accY) {

        synchronized (newestAcc) {
            newestAcc = new float[]{accX, accY};
        }

        sensorFrequency.invoking();
    }

    @Override
    public void onReceivingData(SnapShot snapShot, MessageHeader messageHeader) {


        boolean isNotifyAttr = messageHeader.isNotify();
        int frameNumberAttr = messageHeader.getFrameNumber();
        float renderFrequencyAttr = messageHeader.getCalculateFrequency();
        float renderFrequencyForThisRound = Math.max(this.calculateFrequency, renderFrequencyAttr);

        //invokeFrequency.invoking();


        if (frameNumberAttr == 1 && isInitiated == false) {
            Log.d(TAG, "message too early");
            synchronized (isInitiated) {
                try {
                    while (!isInitiated)
                        isInitiated.wait(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        */
/*
          deprecated frame. simply discard it.
         *//*

        if (frameNumberAttr < frameNumber)
            return;

        newestSnapShot.getMyBall().setAccelarationX(lastAcc[0]);
        newestSnapShot.getMyBall().setAccelarationY(lastAcc[1]);
        newestSnapShot.getRivalBall().setAccelarationX(snapShot.getRivalBall().getAccelarationX());
        newestSnapShot.getRivalBall().setAccelarationY(snapShot.getRivalBall().getAccelarationY());

        motionCalculate.obtainSnapshot(newestSnapShot);
        motionCalculate.updateAll(1f / renderFrequencyForThisRound);
        newestSnapShot = motionCalculate.getSnapShot();

        if (frameNumberAttr > frameNumber) {
            */
/* do nothing. *//*

            Log.d(TAG, "Message is out of order, expected: " + frameNumber + "incoming: " + frameNumberAttr);
        }


        long interval = Math.abs(snapShot.getTime() - newestSnapShot.getTime());
        if (interval < 10) interval += 10;
        calculateFrequency = calculateFrequency / 2 + (float)1000 / (2 * interval);
        newestSnapShot.setTime(snapShot.getTime());

        synchronized (newestAcc) {
            lastAcc[0] = newestAcc[0];
            lastAcc[1] = newestAcc[1];
        }
        newestSnapShot.getMyBall().setAccelarationX(lastAcc[0]);
        newestSnapShot.getMyBall().setAccelarationY(lastAcc[1]);
        synchronized (gameModel) {
            gameModel.setBalls(newestSnapShot, new boolean[]{true, true, true});
        }
        frameNumber = frameNumberAttr + 1;


        listener.sendingData(DSM.createMessage(false, frameNumber, calculateFrequency, newestSnapShot, DSM.POLICY.SYNC));


        gameView.repaint();
    }
}
*/
