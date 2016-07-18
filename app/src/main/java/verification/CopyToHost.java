package verification;

import nju.cs.ADBExecutor;

/**
 * Created by mio on 6/26/16.
 */
public class CopyToHost {

    public static void main(String[] args)
    {
        ADBExecutor adbExecutor = new ADBExecutor("adb");
        adbExecutor.copyFromAll("/storage/emulated/0/Android/data/com.njucs.ballgame/files/BallGameDir",
                "log");
    }
}
