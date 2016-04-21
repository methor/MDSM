package sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import android.os.Bundle;
import android.os.Message;

import constant.Constant;
import model.GameModel;

/**
 * Created by CS on 2015/9/26.
 */
public class AccelarateSensor implements SensorEventListener {
    //private DSM dsm;
    //private boolean dsmStateSwitch = false;
    private GameModel model;

    public static final String TAG = AccelarateSensor.class.getName();

    public int getSampleIntervalMicro() {
        return sampleIntervalMicro;
    }

    public void setSampleIntervalMicro(int sampleIntervalMicro) {
        this.sampleIntervalMicro = sampleIntervalMicro;
    }

    public int sampleIntervalMicro = 80000;

    public long actNumber = 0;

    public AccelarateSensor(GameModel model) {
        //this.dsm = dsm;
        this.model = model;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        /*if (!dsm.isReady())
            return;*/

        // initiate first message under sync policy
        /*if (dsm.getDsmPolicy() instanceof Synchronizer && dsmStateSwitch == false) {
            ((Synchronizer) dsm.getDsmPolicy()).initialMessaging();
            dsmStateSwitch = true;
        }*/


        if (Math.abs(event.values[0]) < 1f)
            event.values[0] = 0f;
        if (Math.abs(event.values[1]) < 1f)
            event.values[1] = 0f;

        event.values[0] = -event.values[0];
        Message message = Message.obtain(model.handler);
        Bundle bundle = new Bundle();
        bundle.putFloat("accelarationX", event.values[0] / 9.8f * Constant.MAX_ACC_HORIZONTAL_NORM);
        bundle.putFloat("accelarationY", event.values[1] / 9.8f * Constant.MAX_ACC_VERTICAL_NORM);
        bundle.putLong("userActTime", System.currentTimeMillis());
        bundle.putInt("sampleIntervalMicro", sampleIntervalMicro);
        message.setData(bundle);
        message.sendToTarget();

//        Log.d(TAG, "accX=" + bundle.getFloat("accelarationX") + ", accY=" + bundle.getFloat("accelarationY"));

        actNumber++;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
