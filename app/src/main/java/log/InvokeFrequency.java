package log;

import android.os.Build;
import android.util.Log;

/**
 * Created by Mio on 2015/12/3.
 */
public class InvokeFrequency {

    private double mFrequency;
    private boolean mHasInvoked = false;
    private long mLastInvoking;
    private long mEchoedInterval = 0;
    public static String TAG = InvokeFrequency.class.getName();
    private Object mClassReference;

    public InvokeFrequency(Object classReference) {
        mClassReference = classReference;
    }

    public void invoking() {

        if (mHasInvoked == false) {
            mLastInvoking = System.currentTimeMillis();
            mHasInvoked = true;
            return;
        }

        long l = System.currentTimeMillis() - mLastInvoking;
        mFrequency = 1000d / l;
        mLastInvoking += l;

        mEchoedInterval += l;
        if (mEchoedInterval > 2000) {
            Log.d(TAG, Build.MODEL + " " + mClassReference.getClass().getName() + " frequency: " + mFrequency);
            mEchoedInterval = 0;
        }
    }
}
