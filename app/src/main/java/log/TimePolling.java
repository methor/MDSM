package log;

import nju.cs.ADBExecutor;
import nju.cs.timingservice.TimingService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Mio on 2016/1/29.
 */
public enum TimePolling {

    INSTANCE;

    private ServerSocket serverSocket = null;

    public static final String TAG = TimePolling.class.getName();

    public void establishDeviceHostConnection()
    {



        Socket host_socket = TimingService.INSTANCE.getHostSocket();
        if (host_socket != null && (host_socket.isConnected() && !host_socket.isClosed()))
            try
            {
                host_socket.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }

        Runnable runnable = new Runnable() {
            @Override
            public void run()
            {
                try
                {
                    if (serverSocket == null || !serverSocket.isBound() || serverSocket.isClosed())
                    {
                        try
                        {
                            serverSocket =  new ServerSocket();
                            serverSocket.bind(new InetSocketAddress("localhost", ADBExecutor.ANDROID_PORT));
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }

                    }
                    TimingService.INSTANCE.setHostSocket(serverSocket.accept());

                } catch (IOException e)
                {
                    e.printStackTrace();
                }

                // receive (and consume) {@link AuthMsg} from PC and enable the time-polling functionality.

                if (TimingService.INSTANCE.getHostSocket() == null)
                    throw new AssertionError("socket cannot be null");

                //TimingService.RESERVED_VALUE.receiveAuthMsg();
            }
        };
        new Thread(runnable).start();
    }

    public long pollingTime() throws Throwable
    {
        return TimingService.INSTANCE.pollingTime();
    }

}
