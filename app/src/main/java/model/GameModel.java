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

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import calc.MotionCalculate;
import consistencyinfrastructure.data.kvs.Key;
import consistencyinfrastructure.group.member.SystemNode;
import consistencyinfrastructure.login.SessionManagerWrapper;
import dsm.AbstractDsm;
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
    private List<Ball> ballList = new ArrayList<>();

    public Handler handler;

    public static final String TAG = GameModel.class.getName();

    private Activity activity;

    private LogParamsToFile logDeviation;
    private LogParamsToFile logUserLatency;
    private LogParamsToFile logNetworkLatency;
    private LogParamsToFile logTaggedValue;

    private int compileCount = 0;

    public long handledNumber = 0;

    private long pcTime;
    private long localTime;

    public GameModel(int id1, int id2, AbstractDsm dsm,
                     Activity activity) {

        ballList.add(new Ball(ORIENTATION_NORTH, id1));
        ballList.add(new Ball(ORIENTATION_SOUTH, id2));
        ballList.add(new Ball(ORIENTATION_NONE, SnapShot.GOALBALLID));

        this.dsm = dsm;
        this.activity = activity;
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("MM.dd. 'at' HH:mm:ss");
        String tag = dsm.getClass().getName();
        String feedback = (((MainActivity) activity).feedbackEnabled ?
                "_" + "Feedback" : "");

        String sensorFreq = "";
        if (MainActivity.DEBUG)
            sensorFreq = "_" + 1000000 / ((MainActivity) activity).sensorEmulator.getSampleIntervalMicro();

        logDeviation = new LogParamsToFile(activity.getApplication(), tag + feedback + "_" +
                ft.format(date) + sensorFreq + ".dat");
        logUserLatency = new LogParamsToFile(activity.getApplication(), "UserLatency" + "_" + tag + feedback + "_" +
                ft.format(date) + sensorFreq + ".dat");
        logNetworkLatency = new LogParamsToFile(activity.getApplication(), "NetworkLatency" + "_" +
                tag + feedback + "_" + ft.format(date) + sensorFreq + ".dat");
        logTaggedValue = new LogParamsToFile(activity.getApplication(), "TaggedValue" + "_" +
                tag + ft.format(date) + sensorFreq + ".dat");
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
        float sampleIntervalSecF = sampleIntervalMicro / 1000000f;
        long processTime, postProcessTime;

        if (((MainActivity) activity).orientation.equals(ORIENTATION_SOUTH)) {
            v = -v;
            v1 = -v1;
        }

        if (MainActivity.DEBUG)     //receive auth message
        {
            if (compileCount == 0) {
                try {
                    TimingService.INSTANCE.receiveAuthMsg();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                compileCount++;
            }
        }

        if (MainActivity.DEBUG)
        {
            processTime = System.currentTimeMillis();
            logUserLatency.write(String.valueOf(processTime - userActTime));

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

        } else if (dsm instanceof WeakDsm) {
            snapShot = new SnapShot(this);
            Ball myBall = snapShot.findBall(SessionManagerWrapper.NODEID);
            myBall.setAccelarationX(v);
            myBall.setAccelarationY(v1);

            //
            if (handledNumber == 0)
            {
                logTaggedValue.write("W" + " " + ValueTagging.valueTagging(MY_KEY.toString(),
                        ReservedValue.RESERVED_VALUE, 0).getTag());
                if (SessionManagerWrapper.isLeader())
                    logTaggedValue.write("W" + " " + ValueTagging.valueTagging(GOAL_KEY.toString(),
                            ReservedValue.RESERVED_VALUE, 0).getTag());
            }

            // dsm operation 1
            Serializable s = ValueTagging.valueTagging(MY_KEY.toString(), myBall);
            dsm.put(MY_KEY, s);
            logTaggedValue.write("W"+ " " + ((TaggedValue)s).getTag());

            // dsm operation 2
            s = dsm.get(OTHER_KEY);

            if (dsm.getReservedValue().equals(s)) {
                //
                logTaggedValue.write("R" + " " + new TaggedValue(String.valueOf(SessionManagerWrapper
                .OTHERID.get(0)), OTHER_KEY.toString(), 0, s).getTag());
            } else {
                logTaggedValue.write("R" + " " + ((TaggedValue) s).getTag());
                Ball rival = ((Ball) ValueTagging.tagStripping((TaggedValue)s));
                snapShot.setBall(rival);
            }

            // dsm operation 3
            s = dsm.get(GOAL_KEY);
            if (dsm.getReservedValue().equals(s)) {
                //
                logTaggedValue.write("R" + " " + new TaggedValue(String.valueOf(SessionManagerWrapper
                .getLeader()), GOAL_KEY.toString(), 0, s).getTag());
            } else {
                logTaggedValue.write("R" + " " + ((TaggedValue) s).getTag());
                Ball goal = ((Ball) ValueTagging.tagStripping((TaggedValue)s));
                snapShot.setBall(goal);
            }
            MotionCalculate.INSTANCE.obtainSnapshot(snapShot);
            MotionCalculate.INSTANCE.updateAll(sampleIntervalSecF);

            // dsm operation 4
            s = ValueTagging.valueTagging(GOAL_KEY.toString(),
                    snapShot.findBall(SnapShot.GOALBALLID));
            dsm.put(GOAL_KEY, s);
            logTaggedValue.write("W" + " " + ((TaggedValue) s).getTag());

            synchronized (this) {
                ballList = snapShot.ballList;
            }

        }

        if (MainActivity.DEBUG)
        {
            postProcessTime = System.currentTimeMillis();
            logNetworkLatency.write(String.valueOf(postProcessTime - processTime));
        }



        if (MainActivity.DEBUG) {

            try {
                if (handledNumber == 0) {
                    long logPre = System.currentTimeMillis();
                    long time = TimePolling.INSTANCE.pollingTime();
                    long logPost = System.currentTimeMillis();
                    pcTime = time - (logPost - logPre) / 2;
                    localTime  = logPost;
                }
                else {
                    long newLocalTime = System.currentTimeMillis();
                    pcTime += newLocalTime - localTime;
                    localTime = newLocalTime;
                }
                snapShot.time = pcTime;
                logDeviation.write(snapShot.toString());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }

        handledNumber++;


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
        while (handler.getLooper().getThread().isAlive())
        {
            try {
                sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "Game looper quit completely");
        logDeviation.close();
        logUserLatency.close();
        logNetworkLatency.close();
        logTaggedValue.close();
        MediaScannerConnection.scanFile(activity.getApplication(), new String[]{logDeviation.getFile().toString(),
                        logUserLatency.getFile().toString(), logNetworkLatency.getFile().toString(),
                        logTaggedValue.getFile().toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                        TimePolling.INSTANCE.closeDeviceHostConnection();
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
