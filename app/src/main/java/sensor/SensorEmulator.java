package sensor;

import android.os.Bundle;
import android.os.Message;
import constant.Constant;
import model.GameModel;

import java.util.Random;

/**
 * Created by Mio on 2016/1/16.
 */
public class SensorEmulator extends Thread {

    private GameModel model;

    public static final String TAG = AccelarateSensor.class.getName();

    public SensorEmulator(GameModel model)
    {
        this.model = model;
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                Thread.sleep(40);
            } catch (InterruptedException e)
            {
                break;
            }
            Random random = new Random();
            float a1 = random.nextFloat();
            if (random.nextFloat() > 0.5)
                a1 = -a1;
            float a2 = random.nextFloat();
            if (random.nextFloat() > 0.5)
                a2 = -a2;
            Message message = Message.obtain(model.handler);
            Bundle bundle = new Bundle();
            bundle.putFloat("accelarationX", a1 * Constant.MAX_ACC_HORIZONTAL_NORM);
            bundle.putFloat("accelarationY", a2 * Constant.MAX_ACC_VERTICAL_NORM);
            message.setData(bundle);
            message.sendToTarget();
        }
    }
}
