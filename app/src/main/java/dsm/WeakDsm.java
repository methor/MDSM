package dsm;


import android.app.Activity;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

import consistencyinfrastructure.communication.MessagingService;
import consistencyinfrastructure.data.kvs.Key;
import consistencyinfrastructure.login.SessionManager;
import log.LogParamsToFile;
import log.NetworkLog;
import log.TimePolling;
import model.Ball;
import model.GameModel;
import verification.TaggedValue;
import weakconsistency.KVStoreInMemory;
import weakconsistency.ReservedValue;
import weakconsistency.WeakConsistencyClient;
import weakconsistency.WeakConsistencyMessage;
import weakconsistency.WeakConsistencyMessagingService;
import weakconsistency.WeakConsistencyServer;

import java.io.Serializable;

/**
 * Created by Mio on 2016/3/7.
 */
public class WeakDsm extends AbstractDsm<Serializable, Key, Serializable> implements NetworkLog {


    private static WeakDsm instance = null;

    private MessagingService.ServerTask serverTask;

    private Activity activity;

    private GameModel gameModel;

    private LogParamsToFile logNetworkDelay;

    private WeakDsm()
    {

        WeakConsistencyServer.INSTANCE.registerNetworkLog(this);


        MessagingService.WEAK.registerReceiver(WeakConsistencyMessagingService.INSTANCE);
        serverTask = MessagingService.WEAK.new ServerTask(SessionManager.getNewInstance().getNodeIp());
        serverTask.start();

    }

    public MessagingService getMessagingService()
    {
        return MessagingService.WEAK;
    }


    public void registerGameModel(GameModel gameModel)
    {
        this.gameModel = gameModel;
        logNetworkDelay = gameModel.createLog("NetworkDelay");
    }

    public synchronized static WeakDsm INSTANCE()
    {
        if (instance == null)
            instance = new WeakDsm();
        return instance;
    }

    @Override
    public Serializable put(Key key, Serializable val)
    {
        //// TODO: 2016/3/7
        return WeakConsistencyClient.INSTANCE.put(key, val);
//        TaggedValue taggedValue = (TaggedValue)val;
//        taggedValue.setValue(Ball.randomBall());
//
//        return WeakConsistencyClient.INSTANCE.put(key, taggedValue);
    }

    @Override
    public Serializable get(Key key)
    {
        return WeakConsistencyClient.INSTANCE.get(key);
    }

    @Override
    public void onDestroy()
    {
        serverTask.onDestroy();
        KVStoreInMemory.INSTANCE.clean();
        instance = null;


        logNetworkDelay.close();
        gameModel.scanFileOnExit(logNetworkDelay);
    }

    @Override
    public Serializable getReservedValue()
    {
        return ReservedValue.RESERVED_VALUE;
    }

    @Override
    public void logNetworkLatency(Serializable msg) {

        WeakConsistencyMessage weakConsistencyMessage = (WeakConsistencyMessage)msg;
        TaggedValue taggedValue = (TaggedValue)weakConsistencyMessage.getVal();

        long sendTime = taggedValue.getTime();
        long receiveTime = gameModel.getPCTime();

        logNetworkDelay.write(String.valueOf(receiveTime - sendTime));


    }
}
