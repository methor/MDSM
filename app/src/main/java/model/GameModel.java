package model;

import android.app.Activity;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.njucs.main.MainActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import calc.MotionCalculate;
import consistencyinfrastructure.data.kvs.Key;
import consistencyinfrastructure.group.GroupConfig;
import consistencyinfrastructure.login.SessionManagerWrapper;
import dsm.AbstractDsm;
import dsm.CausalDsm;
import dsm.MWMRAtomicDsm;
import dsm.WeakDsm;
import ics.mobilememo.sharedmemory.data.kvs.VersionValue;
import log.LogParamsToFile;
import log.TimePolling;
import nju.cs.timingservice.TimingService;
import verification.TaggedValue;
import verification.ValueTagging;
import weakconsistency.ReservedValue;

/**
 * Created by Mio on 2015/12/25.
 */
public class GameModel extends Thread {


    public static final String ORIENTATION_NORTH = "north";

    public static final String ORIENTATION_SOUTH = "south";

    public static final String ORIENTATION_NONE = "none";

    public static final Random random = new Random();
    private List<Ball> ballList = new ArrayList<>();

    public Handler handler;

    public static final String TAG = GameModel.class.getName();

    public Activity activity;

    private LogParamsToFile logDeviation;
    private LogParamsToFile logUserLatency;
    private LogParamsToFile logRoundLatency;
    private LogParamsToFile logTaggedValue;
    private LogParamsToFile logOp;

    private int compileCount = 0;

    public long handledNumber = 0;


    private Long pcTime = null;
    private long localTime;
    private long lastProcessTime = 0;

    public GameModel(int id1, int id2, AbstractDsm dsm,
                     Activity activity) {

        ballList.add(new Ball(ORIENTATION_NORTH, id1));
        ballList.add(new Ball(ORIENTATION_SOUTH, id2));
        ballList.add(new Ball(ORIENTATION_NONE, SnapShot.GOALBALLID));

        this.dsm = dsm;
        this.activity = activity;

//        logDeviation = createLog("");
//        logUserLatency = createLog("UserLatency");
//        logRoundLatency = createLog("RoundLatency");
//        logTaggedValue = createLog("TaggedValue");
        logOp = createLog("logOp");
    }


    public LogParamsToFile createLog(String name)
    {
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("MM.dd.'at'HH.mm.ss");
        String tag = dsm.getClass().getName();
        String feedback = (((MainActivity) activity).feedbackEnabled ?
                "_" + "Feedback" : "");
        String injectedLatencyUpperBound = (dsm.getMessagingService().injectedLatencyUpperBound == 0 ?
                "" : "_LatencyUpperBound." + String.valueOf(dsm.getMessagingService().injectedLatencyUpperBound));

        String sensorFreq = "";
        if (MainActivity.DEBUG)
            sensorFreq = "_" + 1000000 / ((MainActivity) activity).sensorEmulator.getSampleIntervalMicro();

        name = (name.isEmpty() ? name : name + "_");

        return new LogParamsToFile(activity.getApplication(), name + tag + feedback + "_" +
                ft.format(date) + sensorFreq + injectedLatencyUpperBound + "__" + SessionManagerWrapper.NODEID);
    }

    public List<Ball> getBallList() {
        return ballList;
    }

    private AbstractDsm dsm;

    public Key MY_KEY = new Key(String.valueOf(SessionManagerWrapper.NODEID));
    public Key OTHER_KEY = new Key(String.valueOf(SessionManagerWrapper.OTHERID.get(0)));
    public Key GOAL_KEY = new Key(String.valueOf(SnapShot.GOALBALLID));

    public void handleData(float v, float v1, long userActTime, int sampleIntervalMicro) {
        SnapShot snapShot = null;
        long processTime, postProcessTime;

        processTime = System.currentTimeMillis();
        float sampleIntervalSecF = (lastProcessTime == 0 ? 0 : (processTime - lastProcessTime) / 1000f);
        lastProcessTime = processTime;

        // only handle 10000 sensor events
        if (handledNumber == 10000) {
            Log.d(TAG, "Operations number reaches " + handledNumber);
            return;
        }


        if (((MainActivity) activity).orientation.equals(ORIENTATION_SOUTH)) {
            v = -v;
            v1 = -v1;
        }

        if (MainActivity.DEBUG)     //receive auth message
        {
            if (compileCount == 0) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            TimingService.INSTANCE.getHostSocket().getInputStream()));
                    PrintWriter writer = new PrintWriter(TimingService.INSTANCE.getHostSocket().getOutputStream(), true);
                    writer.println("connected?");
                    while (reader.readLine() == null);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                compileCount++;
            }
        }

        if (MainActivity.DEBUG) {
            processTime = System.currentTimeMillis();
//            logUserLatency.write(String.valueOf(processTime - userActTime));

        }


        if (dsm instanceof MWMRAtomicDsm) {
            /*dsm.put(MY_KEY, String.valueOf(new Random().nextInt()));
            dsm.get(OTHER_KEY);
            dsm.put(OTHER_KEY, String.valueOf(new Random().nextInt()));
            dsm.get(MY_KEY);*/
            snapShot = new SnapShot(this);
            Ball myBall = snapShot.findBall(SessionManagerWrapper.NODEID);
            myBall.setAccelarationX(v);
            myBall.setAccelarationY(v1);
            dsm.put(MY_KEY, myBall.toString());
            VersionValue s = ((MWMRAtomicDsm) dsm).get(OTHER_KEY);
            if (s.compareTo(((MWMRAtomicDsm) dsm).getReservedValue()) == 0) {
                //
            } else {

                Ball rival = Ball.fromString(s.getValue());
                snapShot.setBall(rival);
            }
            s = ((MWMRAtomicDsm) dsm).get(GOAL_KEY);
            if (s.compareTo(((MWMRAtomicDsm) dsm).getReservedValue()) == 0) {
                //
            } else {
                Ball goal = Ball.fromString(s.getValue());
                snapShot.setBall(goal);
            }
            MotionCalculate.INSTANCE.obtainSnapshot(snapShot);
            MotionCalculate.INSTANCE.updateAll(sampleIntervalSecF);

            dsm.put(GOAL_KEY, snapShot.findBall(SnapShot.GOALBALLID).toString());

            synchronized (this) {
                ballList = snapShot.ballList;
            }

        } else if (dsm instanceof WeakDsm || dsm instanceof CausalDsm) {
            snapShot = new SnapShot(this);
            Ball myBall = snapShot.findBall(SessionManagerWrapper.NODEID);
            myBall.setAccelarationX(v);
            myBall.setAccelarationY(v1);

            //
            if (handledNumber == 0) {
//                logTaggedValue.write("W" + " " + ValueTagging.valueTagging(MY_KEY.toString(),
//                        ReservedValue.RESERVED_VALUE, 0, getPCTime()).getTag());
//                if (SessionManagerWrapper.isLeader())
//                    logTaggedValue.write("W" + " " + ValueTagging.valueTagging(GOAL_KEY.toString(),
//                            ReservedValue.RESERVED_VALUE, 0, getPCTime()).getTag());
            }

            // dsm operation 1
            Serializable s = ValueTagging.valueTagging(MY_KEY.toString(), getPCTime(), new Ball(myBall)).setRnd(random.nextLong());
            long beforeOpTime = getPCTime();
            dsm.put(MY_KEY, s);
//            logTaggedValue.write("W" + " " + ((TaggedValue) s).getTag());
            logOp.write(((TaggedValue) s).getVariable() + "|||" + ((TaggedValue) s).getTime() + "@" + ((TaggedValue) s).rnd
                + "|||" + "WRITE" + "|||" + beforeOpTime + "|||" + getPCTime());

            // dsm operation 2
            beforeOpTime = getPCTime();
            s = dsm.get(OTHER_KEY);

            if (dsm.getReservedValue().equals(s)) {
                //
//                logTaggedValue.write("R" + " " + new TaggedValue(String.valueOf(SessionManagerWrapper
//                        .OTHERID.get(0)), OTHER_KEY.toString(), 0, getPCTime(), null).getTag());
                logOp.write(OTHER_KEY.toString() + "|||" + "DEFAULT" + "|||" + "READ" + "|||" + beforeOpTime + "|||" + getPCTime());
            } else {
//                logTaggedValue.write("R" + " " + ((TaggedValue) s).getTag());
                Ball rival = ((Ball) ValueTagging.tagStripping((TaggedValue) s));
                snapShot.setBall(new Ball(rival));
                logOp.write(OTHER_KEY.toString() + "|||" + ((TaggedValue) s).getTime() + "@" + ((TaggedValue) s).rnd
                    + "|||" + "READ" + "|||" + beforeOpTime + "|||" + getPCTime());
            }

            // dsm operation 3
            beforeOpTime = getPCTime();
            s = dsm.get(GOAL_KEY);
            if (dsm.getReservedValue().equals(s)) {
                //
//                logTaggedValue.write("R" + " " + new TaggedValue(String.valueOf(SessionManagerWrapper
//                        .getLeader()), GOAL_KEY.toString(), 0, getPCTime(), null).getTag());
                logOp.write(GOAL_KEY.toString() + "|||" + "DEFAULT" + "|||" + "READ" + "|||" + beforeOpTime + "|||" + getPCTime());
            } else {
//                logTaggedValue.write("R" + " " + ((TaggedValue) s).getTag());
                Ball goal = ((Ball) ValueTagging.tagStripping((TaggedValue) s));
                snapShot.setBall(new Ball(goal));
                logOp.write(GOAL_KEY.toString() + "|||" + ((TaggedValue) s).getTime() + "@" + ((TaggedValue) s).rnd
                    + "|||" + "READ" + "|||" + beforeOpTime + "|||" + getPCTime());
            }
            MotionCalculate.INSTANCE.obtainSnapshot(snapShot);
            MotionCalculate.INSTANCE.updateAll(sampleIntervalSecF);

            // dsm operation 4
            s = ValueTagging.valueTagging(GOAL_KEY.toString(), getPCTime(),
                    new Ball(snapShot.findBall(SnapShot.GOALBALLID))).setRnd(random.nextLong());
            beforeOpTime = getPCTime();
            dsm.put(GOAL_KEY, s);
//            logTaggedValue.write("W" + " " + ((TaggedValue) s).getTag());
            logOp.write(GOAL_KEY.toString() + "|||" + ((TaggedValue) s).getTime() + "@" + ((TaggedValue) s).rnd
                + "|||" + "WRITE" + "|||" + beforeOpTime + "|||" + getPCTime());

            synchronized (this) {
                ballList = snapShot.ballList;
            }

        }

        if (MainActivity.DEBUG) {
            postProcessTime = System.currentTimeMillis();
//            logRoundLatency.write(String.valueOf(postProcessTime - processTime));
        }


        if (MainActivity.DEBUG) {


            long pcTime = getPCTime();
            snapShot.time = pcTime;
//            logDeviation.write(snapShot.toString());

        }

        handledNumber++;
        Log.d(TAG, "handledNumber = " + handledNumber);


    }

   public long getPCTime() {
        if (pcTime == null) {
            long logPre = System.nanoTime();
            long time = 0;
            try {
                time = TimePolling.INSTANCE.pollingTime();

            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

            long logPost = System.nanoTime();
            pcTime = time - (logPost - logPre) / 2;
            localTime = logPost;
            Log.d("PC TIME", Long.toString(pcTime));

            if (pcTime <= 0) {
                throw new RuntimeException("time:" + time + ", logPost: " + logPost + ", logpre: " + logPre);
            }
        } else {
            long newLocalTime = System.nanoTime();
            pcTime += newLocalTime - localTime;
            localTime = newLocalTime;
        }
//       if (pcTime <= 0)
//           throw new RuntimeException();

        return pcTime;
    }

    @Override
    public void run() {
        Looper.prepare();

        handler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    Log.d("MsgHandler", "Current Time: " + System.currentTimeMillis());
                    Bundle bundle = msg.getData();
                    float vx = bundle.getFloat("accelarationX");
                    float vy = bundle.getFloat("accelarationY");
                    long userActTime = bundle.getLong("userActTime");
                    int sampleInterval = bundle.getInt("sampleIntervalMicro");

                    handleData(vx, vy, userActTime, sampleInterval);
                }
            }
        };


        Looper.loop();

    }

    public void onDestroy() {
        handler.getLooper().quit();
//        try {
//            handler.getLooper().getThread().join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        // looper thread may dead waiting for something, send interrupt to cancel it
        // place interrupt in while in case the interrupt is handled from inside some called method
        while (handler.getLooper().getThread().isAlive()) {
            handler.getLooper().getThread().interrupt();
            try {
                sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "Game looper quit completely");
//        logDeviation.close();
//        logUserLatency.close();
//        logRoundLatency.close();
//        logTaggedValue.close();
        logOp.close();
        MediaScannerConnection.scanFile(activity.getApplication(), new String[]{
//                logDeviation.getFile().toString(),
//                logUserLatency.getFile().toString(), logRoundLatency.getFile().toString(),
//                logTaggedValue.getFile().toString(),
                logOp.getFile().toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
//                        TimePolling.INSTANCE.closeDeviceHostConnection();
                    }
                });
    }

    public void scanFileOnExit(LogParamsToFile logParamsToFile)
    {
        MediaScannerConnection.scanFile(activity.getApplication(), new String[]{logParamsToFile.getFile().toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
//                        TimePolling.INSTANCE.closeDeviceHostConnection();
                    }
                });
    }


    private String getOwnerIp(String s) {
        Pattern p = Pattern.compile("SnapShot");
        Matcher matcher = p.matcher(s);
        matcher.find();
        int end = matcher.start();
        p = Pattern.compile("ip:");
        matcher = p.matcher(s);
        matcher.find();
        int start = matcher.start();
        String ownerIp = s.substring(start, end);

        Log.d(TAG, "getOwnerIp: " + ownerIp);
        return ownerIp;
    }

}
