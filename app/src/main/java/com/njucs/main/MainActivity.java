package com.njucs.main;

import android.app.Activity;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;


import java.util.ArrayList;

import consistencyinfrastructure.login.SessionManagerWrapper;
import constant.Constant;
import dsm.AbstractDsm;
import dsm.MWMRAtomicDsm;
import dsm.WeakDsm;
import ics.mobilememo.sharedmemory.atomicity.MWMRAtomicityRegisterClient;
import model.Field;
import model.GameModel;
import sensor.AccelarateSensor;
import sensor.SensorEmulator;
import view.GameView;

public class MainActivity extends Activity {
    private GameModel model;
    private GameView gameView;
    private AccelarateSensor mAccelarateSensor;
    private SensorManager mSensorManager;
    private AbstractDsm dsm;
    private SensorEmulator sensorEmulator;

    public String orientation;

    public Thread feedbackThread;

    public static final String TAG = MainActivity.class.getName();

    public static final boolean DEBUG = false;

    private void unregisterDataSource()
    {
        if (DEBUG == true)
            sensorEmulator.interrupt();
        else if (mSensorManager != null)
            mSensorManager.unregisterListener(mAccelarateSensor);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterDataSource();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        feedbackThread.interrupt();
        dsm.onDestroy();
        model.onDestroy();
        unregisterDataSource();


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DEBUG == false)
            mSensorManager.registerListener(mAccelarateSensor, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    mAccelarateSensor.getSampleInterval());
        else {
            sensorEmulator = new SensorEmulator(model);
            sensorEmulator.start();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 全屏显示
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //setContentView(R.layout.activity_connect);


        final DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        Constant.constantInit(height, width, 0, 0);
        //Constant.constantInit(800, 600, 0, 0);

        Field.INSTANCE.initiate();
        Bundle extras = getIntent().getExtras();
        String consistency = extras.getString(getResources().getString(R.string.consistency));
        if (consistency.equals(getResources().getString(R.string.atomic_consistency)))
            dsm = MWMRAtomicDsm.INSTANCE();
        else if (consistency.equals(getResources().getString(R.string.weak_consistency)))
            dsm = WeakDsm.INSTANCE();


        //String orientation1 = extras.getString("orientation1");
        //String orientation2 = extras.getString("orientation2");
        int id1 = extras.getInt("id1");
        int id2 = extras.getInt("id2");
        if (SessionManagerWrapper.NODEID == id1)
            orientation = GameModel.ORIENTATION_NORTH;
        else
            orientation = GameModel.ORIENTATION_SOUTH;


        model = new GameModel(id1, id2, dsm, this);
        model.setName("ModelMessageLooper");
        model.start();
        gameView = new GameView(this, model);


        //P2PNetwork.RESERVED_VALUE.setDSM(dsm);


        if (DEBUG == false) {
            mAccelarateSensor = new AccelarateSensor(model);
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        } else
            sensorEmulator = new SensorEmulator(model);


        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(gameView);

        feedbackThread = new Thread(new Runnable() {
            @Override
            public void run() {

                ArrayList<Long> prevPendingList = new ArrayList<>();

                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                    long actNumber, handledNumber, pendingNumber;
                    if (DEBUG == true)
                        actNumber = sensorEmulator.actNumber;
                    else
                        actNumber = mAccelarateSensor.actNumber;

                    handledNumber = model.handledNumber;
                    pendingNumber = actNumber - handledNumber;
                    prevPendingList.add(pendingNumber);

                    if (prevPendingList.size() > 2) {
                        double multiplier = 1;
                        int sampleInterval = (DEBUG ? sensorEmulator.getSampleInterval()
                                : mAccelarateSensor.getSampleInterval());
                        int size = prevPendingList.size();
                        if (prevPendingList.get(size - 1) > prevPendingList.get(size - 2)
                                && prevPendingList.get(size - 2) > prevPendingList.get(size - 3)) {
                            multiplier = 1.2;
                        } else if (prevPendingList.get(size - 1) <= prevPendingList.get(size - 2) &&
                                prevPendingList.get(size - 2) <= prevPendingList.get(size - 3)) {
                            if (prevPendingList.get(size - 1) <= 1)
                            multiplier = 0.8;
                        } else if (prevPendingList.get(size - 1) > 1000000f / sampleInterval * 0.3){
                            multiplier = 1.2;
                        }
                        if (!DEBUG) {
                            if (multiplier != 1) {
                                sampleInterval = (int) (mAccelarateSensor.getSampleInterval() * multiplier);
                                mSensorManager.unregisterListener(mAccelarateSensor);
                                mAccelarateSensor.setSampleInterval(sampleInterval);
                                mSensorManager.registerListener(mAccelarateSensor, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                                        sampleInterval);
                            }
                        } else {
                            if (multiplier != 1) {
                                sampleInterval = (int) (sensorEmulator.getSampleInterval() * multiplier);
                                sensorEmulator.interrupt();
                                sensorEmulator = new SensorEmulator(model, actNumber, sampleInterval);
                                sensorEmulator.start();
                            }
                        }
                        Log.d(TAG, "SampleInterval = " + sampleInterval + ", Recent: " + actNumber + ", "
                            + handledNumber);


                    }
                }
            }
        });
        feedbackThread.start();


        /**
         * here ViewTreeObserver is used to configure screen parameters AFTER layout is set.
         * an alternative to this is View.post(Runnable) to do the same thing.
         */
        /*
        ViewTreeObserver vto = gameView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                gameView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int height = gameView.getHeight();
                int width = gameView.getWidth();
                Constant.constantInit(height, width, 0, 0);
                gameModel = new GameModel();
                gameView = new GameView(gameView.getContext(), gameModel);

                //motionCalculate = new MotionCalculate(gameModel.getMyBallTransient(), gameModel.getRivalBallTransient(),
                //        gameModel.getGoalBallTransient(), gameModel.getField());
                //motionCalculate.startCalculate();
                dsm = new DSM(gameModel);
                p2PNetwork = new P2PNetwork(mWifiManager, mChannel, activity, dsm);

                p2PNetwork.discoveryPeers();
                p2PNetwork.connect();
                synchronized (mPauseLock) {
                    mPaused = true;
                    while (mPaused) {
                        try {
                            mPauseLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                mAccelarateSensor = new AccelarateSensor(dsm);
                SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
                sensorManager.registerListener(mAccelarateSensor, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                        SensorManager.SENSOR_DELAY_GAME);

                setContentView(gameView);

            }
        });*/


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
