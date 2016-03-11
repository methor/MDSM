package com.njucs.main;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;


import consistencyinfrastructure.login.SessionManagerWrapper;
import constant.Constant;
import dsm.AbstractDsm;
import dsm.WeakDsm;
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

    public static final String TAG = MainActivity.class.getName();

    private static final boolean DEBUG = true;


    @Override
    protected void onPause()
    {
        super.onPause();
        if (DEBUG == true)
            sensorEmulator.interrupt();
        else if (mSensorManager != null)
            mSensorManager.unregisterListener(mAccelarateSensor);

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        dsm.onDestroy();
        model.onDestroy();


    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (DEBUG == false)
            mSensorManager.registerListener(mAccelarateSensor, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    mSensorManager.SENSOR_DELAY_NORMAL);
        else
        {
            sensorEmulator = new SensorEmulator(model);
            sensorEmulator.start();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 全屏显示
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //setContentView(R.layout.activity_connect);


        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        Constant.constantInit(height, width, 0, 0);
        //Constant.constantInit(800, 600, 0, 0);

        Field.INSTANCE.initiate();
        dsm = WeakDsm.INSTANCE();


        Bundle extras = getIntent().getExtras();
        String orientation1 = extras.getString("orientation1");
        String orientation2 = extras.getString("orientation2");
        int id1 = extras.getInt("id1");
        int id2 = extras.getInt("id2");
        if (SessionManagerWrapper.NODEID == id1)
            orientation = orientation1;
        else
            orientation = orientation2;


        model = new GameModel(orientation1, id1, orientation2, id2, dsm, this);
        model.setName("ModelMessageLooper");
        model.start();
        gameView = new GameView(this, model);


        //P2PNetwork.RESERVED_VALUE.setDSM(dsm);


        if (DEBUG == false)
        {
            mAccelarateSensor = new AccelarateSensor(model);
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        } else
            sensorEmulator = new SensorEmulator(model);


        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(gameView);



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
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
    }
}
