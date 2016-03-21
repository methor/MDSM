package model;

import android.app.Activity;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import calc.MotionCalculate;
import com.njucs.main.MainActivity;
import consistencyinfrastructure.data.kvs.Key;
import dsm.AbstractDsm;
import dsm.MWMRAtomicDsm;
import dsm.SWMRAtomicDsm;
import consistencyinfrastructure.login.SessionManagerWrapper;
import dsm.WeakDsm;
import ics.mobilememo.sharedmemory.data.kvs.VersionValue;
import log.LogParamsToFile;
import log.TimePolling;
import nju.cs.timingservice.TimingService;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private LogParamsToFile log;

    private int compileCount = 0;

    public GameModel(int id1, int id2, AbstractDsm dsm,
                     Activity activity)
    {

        ballList.add(new Ball(ORIENTATION_NORTH, id1));
        ballList.add(new Ball(ORIENTATION_SOUTH, id2));
        ballList.add(new Ball(ORIENTATION_NONE, SnapShot.GOALBALLID));

        this.dsm = dsm;
        this.activity = activity;
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("MM.dd. 'at' hh_mm_ss");
        String tag = dsm.getClass().getName();

        log = new LogParamsToFile(activity.getApplication(), tag + "_" +
                ft.format(date) + ".dat");
    }

    public List<Ball> getBallList()
    {
        return ballList;
    }

    private AbstractDsm dsm;

    public Key MY_KEY = new Key(String.valueOf(SessionManagerWrapper.NODEID));
    public Key OTHER_KEY = new Key(String.valueOf(SessionManagerWrapper.OTHERID.get(0)));
    public Key GOAL_KEY = new Key(String.valueOf(SnapShot.GOALBALLID));

    public void handleData(float v, float v1)
    {
        SnapShot snapShot = null;
        if (dsm instanceof MWMRAtomicDsm)
        {
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
            if (s.compareTo(((MWMRAtomicDsm) dsm).getReservedValue()) == 0)
            {
                //
            } else
            {

                Ball rival = Ball.fromString(s.getValue());
                snapShot.setBall(rival);
            }
            s = ((MWMRAtomicDsm) dsm).get(GOAL_KEY);
            if (s.compareTo(((MWMRAtomicDsm) dsm).getReservedValue()) == 0)
            {
                //
            } else
            {
                Ball goal = Ball.fromString(s.getValue());
                snapShot.setBall(goal);
            }
            MotionCalculate.INSTANCE.obtainSnapshot(snapShot);
            MotionCalculate.INSTANCE.updateAll(0.1f);

            dsm.put(GOAL_KEY, snapShot.findBall(SnapShot.GOALBALLID).toString());

            synchronized (this)
            {
                ballList = snapShot.ballList;
            }

        }
        else if (dsm instanceof WeakDsm)
        {
            snapShot = new SnapShot(this);
            Ball myBall = snapShot.findBall(SessionManagerWrapper.NODEID);
            myBall.setAccelarationX(v);
            myBall.setAccelarationY(v1);
            dsm.put(MY_KEY, myBall);
            Serializable s = dsm.get(OTHER_KEY);
            if (dsm.getReservedValue().equals(s))
            {
                //
            } else
            {
                Ball rival = ((Ball) s);
                snapShot.setBall(rival);
            }
            s = dsm.get(GOAL_KEY);
            if (dsm.getReservedValue().equals(s))
            {
                //
            } else
            {
                Ball goal = ((Ball) s);
                snapShot.setBall(goal);
            }
            MotionCalculate.INSTANCE.obtainSnapshot(snapShot);
            MotionCalculate.INSTANCE.updateAll(0.1f);

            dsm.put(GOAL_KEY, snapShot.findBall(SnapShot.GOALBALLID));

            synchronized (this)
            {
                ballList = snapShot.ballList;
            }

        }
        if (MainActivity.DEBUG)
        {
            if (compileCount == 0)
                try
                {
                    TimingService.INSTANCE.receiveAuthMsg();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }

            try
            {
                long time = TimePolling.INSTANCE.pollingTime();
                snapShot.time = time;
                log.write(snapShot.toString());
            } catch (Throwable throwable)
            {
                throwable.printStackTrace();
            }
            compileCount++;
        }


    }

    @Override
    public void run()
    {
        Looper.prepare();

        handler = new Handler() {
            public void handleMessage(Message msg)
            {
                Log.d("MsgHandler", "receive data from sensor");
                Bundle bundle = msg.getData();
                float vx = bundle.getFloat("accelarationX");
                float vy = bundle.getFloat("accelarationY");

                handleData(vx, vy);
            }
        };

        Looper.loop();

    }

    public void onDestroy()
    {
        log.close();
        MediaScannerConnection.scanFile(activity.getApplication(), new String[]{log.getFile().toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri)
                    {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }

    private String getOwnerIp(String s)
    {
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
