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
import consistencyinfrastructure.data.kvs.Key;
import dsm.AbstractDsm;
import dsm.MWMRAtomicDsm;
import dsm.SWMRAtomicDsm;
import consistencyinfrastructure.login.SessionManagerWrapper;
import dsm.WeakDsm;
import ics.mobilememo.sharedmemory.data.kvs.VersionValue;
import log.LogParamsToFile;

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

    public static final int GOALBALLID = 0;

    public static final String ORIENTATION_NORTH = "north";

    public static final String ORIENTATION_SOUTH = "south";

    public static final String ORIENTATION_NONE = "none";
    private List<Ball> ballList = new ArrayList<>();

    public Handler handler;

    public static final String TAG = GameModel.class.getName();

    private Activity activity;

    private LogParamsToFile log;

    private int compileCount = 0;

    public GameModel(String orientation1, int id1, String orientation2, int id2, AbstractDsm dsm,
                     Activity activity)
    {
        if (orientation1.equals("north"))
        {
            if (!orientation2.equals("south"))
                System.exit(-1);
        }
        if (orientation2.equals("north"))
            if (!orientation1.equals("south"))
                System.exit(-1);

        ballList.add(new Ball(orientation1, id1));
        ballList.add(new Ball(orientation2, id2));
        ballList.add(new Ball(ORIENTATION_NONE, GOALBALLID));

        this.dsm = dsm;
        this.activity = activity;
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("MM.dd. 'at' hh:mm:ss");
        String tag = null;
        if (dsm instanceof MWMRAtomicDsm)
            tag = MWMRAtomicDsm.TAG;
        else if (dsm instanceof SWMRAtomicDsm)
            tag = SWMRAtomicDsm.TAG;
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
    public Key GOAL_KEY = new Key(String.valueOf(GOALBALLID));

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
            if (s.equals(((MWMRAtomicDsm) dsm).getReservedValue()))
            {
                //
            } else
            {
                Ball rival = Ball.fromString(s.getValue());
                snapShot.setBall(rival);
            }
            s = ((MWMRAtomicDsm) dsm).get(GOAL_KEY);
            if (s.equals(((MWMRAtomicDsm) dsm).getReservedValue()))
            {
                //
            } else
            {
                Ball goal = Ball.fromString(s.getValue());
                snapShot.setBall(goal);
            }
            MotionCalculate.INSTANCE.obtainSnapshot(snapShot);
            MotionCalculate.INSTANCE.updateAll(0.1f);

            dsm.put(GOAL_KEY, snapShot.findBall(GOALBALLID).toString());

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

            dsm.put(GOAL_KEY, snapShot.findBall(GOALBALLID));

            synchronized (this)
            {
                ballList = snapShot.ballList;
            }

        }
        /*if (compileCount == 0)
            try
            {
                TimingService.ATO.receiveAuthMsg();
            } catch (IOException e)
            {
                e.printStackTrace();
            }

        try
        {
            long time = TimePolling.ATO.pollingTime();
            log.write(time + "\t" + snapShot.toString());
        } catch (Throwable throwable)
        {
            throwable.printStackTrace();
        }
        compileCount++;*/


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
