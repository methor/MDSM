/**
 * @author hengxin
 * @creation 2013-8-26; 2014-05-08
 * @file MessagingService.java
 * @description basic message-passing mechanism: establish and listen to connections, send and receive messages
 * it also dispatches the received messages of type {@link IPMessage} to appropriate handlers.
 */
package consistencyinfrastructure.communication;

import consistencyinfrastructure.config.NetworkConfig;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Singleton pattern with Java Enum which is simple and thread-safe
 */
public enum MessagingService implements IReceiver {
    MATO, SATO, WEAK, CAUSAL;


    IReceiver receiver = null;

    ConcurrentHashMap<String, SocketOut> clientMap = new ConcurrentHashMap<>();


    private static final String TAG = MessagingService.class.getName();

    private static ExecutorService exec = Executors.newCachedThreadPool();

    /**
     * server socket on which the server replica is listening to and accept messages
     */
    private ServerSocket server_socket = null;

    /**
     * send the message to the designated receiver and return immediately
     * without waiting for response
     *
     * @param receiver_ip ip of the designated receiver
     * @param msg         message of type {@link IPMessage} to send
     */
    public void sendOneWay(final String receiver_ip, final IPMessage msg)
    {

        final int port = getServerPort();
//		Log.d(TAG, "Send to " + receiver_ip);
        Runnable runnable = new Runnable() {
            @Override
            public void run()
            {


                SocketOut socketOut = new SocketOut();
                SocketOut prevSocketOut = clientMap.putIfAbsent(receiver_ip, socketOut);
                if (prevSocketOut == null)
                {
                    prevSocketOut = socketOut;
                }
                synchronized (prevSocketOut)
                {
                    Socket socket = prevSocketOut.getSocket();
                    if (socket == null || !socket.isConnected() || socket.isClosed())
                    {
                        InetSocketAddress socket_address = new InetSocketAddress(
                                receiver_ip, port);
                        socket = new Socket();
                        try
                        {
                            socket.connect(socket_address, NetworkConfig.TIMEOUT);
                            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                            prevSocketOut.setSocket(socket);
                            prevSocketOut.setOos(oos);
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    try
                    {
                        prevSocketOut.getOos().writeObject(msg);
                        prevSocketOut.getOos().flush();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }

            }
        };

        if (this == WEAK || this == CAUSAL)
            runnable.run();
        else
        {
            try
            {
                exec.execute(runnable);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

    }

    /**
     * start as a server to listen to socket connection requests
     *
     * @param server_ip ip address of server
     */
    public void start2Listen(String server_ip)
    {
        final int port = getServerPort();
        try
        {
            server_socket = new ServerSocket();
            server_socket.setReuseAddress(true);
            server_socket.bind(new InetSocketAddress(server_ip, port));

            try
            {
                while (true)
                {
                    final Socket connection = server_socket.accept();
                    Runnable receive_task = new Runnable() {
                        @Override
                        public void run()
                        {
                            try
                            {
                                ObjectInputStream ois = new ObjectInputStream(connection.getInputStream());


                                while (true)
                                {
                                    Object obj = ois.readObject();
                                    //TODO
                                    //Log.i(TAG, obj.getClass().toString());
                                    IPMessage msg = (IPMessage)obj;
                                    //Log.i(TAG, "Receiving message: " + msg.toString());

                                    MessagingService.this.onReceive(msg);
                                }


                            } catch (StreamCorruptedException sce)
                            {
                                sce.printStackTrace();
                            } catch (IOException ioe)
                            {
                                ioe.printStackTrace();
                            } catch (ClassNotFoundException cnfe)
                            {
                                cnfe.printStackTrace();
                            } finally
                            {
                                try
                                {
                                    connection.close();
                                } catch (IOException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }
                    };
                    exec.execute(receive_task);
                }
            } finally
            {
                server_socket.close();

            }
        } catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    /**
     * upon receiving messages of type {@link IPMessage},
     * {@link MessagingService} dispatches them
     * to appropriate handlers according to their concrete sub-types.
     *
     * @param msg received message of type {@link IPMessage}
     */
    @Override
    public void onReceive(IPMessage msg)
    {
        /*if (msg instanceof AtomicityMessage)
            AtomicityMessagingService.MATO.onReceive(msg);
        else // TODO: other messages
            return;*/
        String s = msg.getClass().getSimpleName();
//        System.out.println("Message type:" + s);
        receiver.onReceive(msg);

    }

    public int getServerPort()
    {
        if (this == MATO)
            return NetworkConfig.NETWORK_PORT;
        else if (this == WEAK)
            return NetworkConfig.NETWORK_PORT + 1;
        else
            return NetworkConfig.NETWORK_PORT + 100;
    }

    public MessagingService registerReceiver(IReceiver receiver)
    {
        this.receiver = receiver;
        return this;
    }


    /**
     * exit the messaging service: close the server socket
     */
    public void exit()
    {
        if (this.server_socket != null && !this.server_socket.isClosed())
        {
            try
            {
                this.server_socket.close();

                exec.shutdown();
                try {
                    if (!exec.awaitTermination(300, TimeUnit.MILLISECONDS))
                    {
                        exec.shutdownNow();
                        if (!exec.awaitTermination(500, TimeUnit.MILLISECONDS))
                            //TODO
                            System.err.println("thread pool did not terminate");

                        else
                            for (SocketOut socketOut : clientMap.values())
                                socketOut.getSocket().close();

                    }
                } catch (InterruptedException e)
                {
                    exec.shutdownNow();
                    Thread.currentThread().interrupt();

                }



            } catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
        }
    }

    private void startThreadPool()
    {
        if (exec.isShutdown())
            exec = Executors.newCachedThreadPool();
    }


    /*public class ServerTask extends AsyncTask<String, Void, Void>
    {*/
    public class ServerTask extends Thread {
        String ip;
        MessagingService messagingService;

        public ServerTask(String ip)
        {
            this.ip = ip;
            messagingService = MessagingService.this;
        }

        @Override
        public void run()
        {
            messagingService.startThreadPool();
            messagingService.start2Listen(ip);
        }

        public void onDestroy()
        {
            messagingService.exit();
        }
    }

}

class SocketOut {
    public Socket socket = null;
    public ObjectOutputStream oos = null;

    public SocketOut()
    {}


    public Socket getSocket()
    {
        return socket;
    }

    public void setSocket(Socket socket)
    {
        this.socket = socket;
    }

    public ObjectOutputStream getOos()
    {
        return oos;
    }

    public void setOos(ObjectOutputStream oos)
    {
        this.oos = oos;
    }
}


