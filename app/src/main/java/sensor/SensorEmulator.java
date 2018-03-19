package sensor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import consistencyinfrastructure.login.SessionManagerWrapper;
import constant.Constant;
import model.GameModel;
import network.wifidirect.ConnectActivity;

import java.util.Random;

/**
 * Created by Mio on 2016/1/16.
 */
public class SensorEmulator extends Thread {

    private GameModel model;

    public static final String TAG = AccelarateSensor.class.getName();


    public long actNumber = 0;

    public int getSampleIntervalMicro() {
        return sampleIntervalMicro;
    }

    public void setSampleIntervalMicro(int sampleIntervalMicro) {
        this.sampleIntervalMicro = sampleIntervalMicro;
    }

    public int sampleIntervalMicro = 20000;
    public SensorEmulator(GameModel model)
    {
        this.model = model;
    }

    public SensorEmulator(GameModel model, long actNumber, int sampleIntervalMicro)
    {
        this.model = model;
        this.actNumber = actNumber;
        this.sampleIntervalMicro = sampleIntervalMicro;
    }

    @Override
    public void run()
    {
        Float a1 = 0f, a2 = -0.5f;
        while (true)
        {
            try
            {
                Thread.sleep(sampleIntervalMicro / 1000);
            } catch (InterruptedException e)
            {
                Log.d(TAG, "interrupted");
                break;
            }

            // restrict sensor event number
            if (actNumber == 2500) {
                Intent intent = new Intent(model.activity, ConnectActivity.class);
                intent.putExtra("wait10s", true);
                model.activity.setResult(Activity.RESULT_OK, intent);
                model.activity.finish();
                continue;
            }

            Random random = new Random();
            if (!(actNumber < 100 && SessionManagerWrapper.isLeader())) {
                if (random.nextFloat() > 0.8) {
                    a1 = random.nextFloat();
                    if (random.nextFloat() > 0.5)
                        a1 = -a1;
                    a2 = random.nextFloat();
                    if (random.nextFloat() > 0.5)
                        a2 = -a2;
                }
            }
            Message message = Message.obtain(model.handler);
            message.what = 1;
            Bundle bundle = new Bundle();
            bundle.putFloat("accelarationX", a1 * Constant.MAX_ACC_HORIZONTAL_NORM);
            bundle.putFloat("accelarationY", a2 * Constant.MAX_ACC_VERTICAL_NORM);
            bundle.putLong("userActTime", System.currentTimeMillis());
            bundle.putInt("sampleIntervalMicro", sampleIntervalMicro);

            message.setData(bundle);
            message.sendToTarget();

            actNumber++;
        Log.d(TAG, "accX=" + bundle.getFloat("accelarationX") + ", accY=" + bundle.getFloat("accelarationY"));
        }
    }
}
