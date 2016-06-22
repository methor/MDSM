package sensor;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import constant.Constant;
import model.GameModel;

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

    public int sampleIntervalMicro = 100000;
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
            if (actNumber == 11000)
                continue;

            Random random = new Random();
            float a1 = random.nextFloat();
            if (random.nextFloat() > 0.5)
                a1 = -a1;
            float a2 = random.nextFloat();
            if (random.nextFloat() > 0.5)
                a2 = -a2;
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
