/**
 * @author hengxin
 * @date Jun 21, 2014
 * @description pc host side:
 * (1) port forwarding
 * (2)
 */
package nju.cs.timingservice.pc;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import nju.cs.ADBExecutor;
import nju.cs.SocketUtil;
import nju.cs.timingservice.message.AuthMsg;
import nju.cs.timingservice.message.Message;
import nju.cs.timingservice.message.RequestTimeMsg;
import nju.cs.timingservice.message.ResponseTimeMsg;

public class PCHost {
    private ExecutorService exec;

    /**
     * pairs of (device, hostport)
     */
    private final Map<String, Integer> device_hostport_map;
    private final Map<String, Socket> device_hostsocket_map = new HashMap<>();

    /**
     * constructor of {@link PCHost}:
     * establish socket connections for each device
     *
     * @param device_hostport_map {@link #device_hostport_map}: map of (device, port)
     */
    public PCHost(Map<String, Integer> device_hostport_map) {
        this.device_hostport_map = device_hostport_map;
        this.createDeviceHostConnection();
    }

    /**
     * Establish connections for each device on a specified port on the host PC
     * and store them in the {@link #device_hostsocket_map} for further use
     */
    private void createDeviceHostConnection() {
        String device = null;
        int host_port = -1;

        exec = Executors.newFixedThreadPool(5);     // start a new thread pool

        for (Map.Entry<String, Integer> device_hostport : this.device_hostport_map.entrySet()) {
            device = device_hostport.getKey();
            host_port = device_hostport.getValue();

            // create socket for each "device" on the "host_port" of host PC
            Socket device_hostsocket = null;
            try {
                device_hostsocket = new Socket("localhost", host_port);
            } catch (UnknownHostException uhe) {
                uhe.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            // store the sockets created for each "device"
            this.device_hostsocket_map.put(device, device_hostsocket);
        }
    }

    /**
     * Send {@link AuthMsg} to an Android device on the other side of a specified socket
     *
     * @param host_socket send message via this specified socket
     */
    private void sendAuthMsg(final Socket host_socket) throws IOException {
        SocketUtil.INSTANCE.sendMsg(new AuthMsg(), host_socket);
    }

    /**
     * Wait for {@link RequestTimeMsg} from Android device
     *
     * @param host_socket waiting on this specified socket
     * @return {@link RequestTimeMsg} received
     */
    private Message waitForRequestTimeMsg(final Socket host_socket) throws IOException {
        Message msg = SocketUtil.INSTANCE.receiveMsg(host_socket);
        assert msg.getType() == Message.REQUEST_TIME_MSG;
        return (RequestTimeMsg) msg;
    }

    /**
     * Send {@link ResponseTimeMsg} to an Android device on the other side of a specified socket
     *
     * @param host_socket send message via this specified socket
     */
    private void sendResponseTimeMsg(final Socket host_socket) throws IOException {
        SocketUtil.INSTANCE.sendMsg(new ResponseTimeMsg(System.nanoTime()), host_socket);
    }

    /**
     * Send the current system time (denoted by {@link ResponseTimeMsg})
     * to some device attached to the specified socket in a new thread
     *
     * @param host_socket the message is sent via this socket
     */
    private void sendResponseTimeMsgInNewThread(final Socket host_socket, ObjectOutputStream outputStream) {
        SocketUtil.INSTANCE.sendMsgInNewThread(new ResponseTimeMsg(System.currentTimeMillis()), host_socket, outputStream);
    }

    /**
     * Core method for starting and providing time-polling service
     */
    public void startTimePollingService() {
        Socket host_socket = null;
        for (Map.Entry<String, Socket> device_hostsocket : this.device_hostsocket_map.entrySet()) {
            host_socket = device_hostsocket.getValue();

            /**
             * Give authorization of polling time to Android devices
             */
            if (host_socket == null)
                throw new AssertionError("socket cannot be null");

            /**
             * Alternate: wait for {@link RequestTimeMsg} from Android devices
             * and send {@link ResponseTimeMsg} with system time to them
             */
            exec.execute(new Alternate(host_socket));
        }
        exec.shutdown();    // shutdown this thread pool
    }

    /**
     * Core class for providing time-polling service.
     * Alternate: wait for {@link RequestTimeMsg} from Android devices
     * and send {@link ResponseTimeMsg} with system time to them
     * <p/>
     * Multi-threaded Issue:
     * It assigns a separate thread for each attached Android device.
     *
     * @author hengxin
     * @date Jul 15, 2014
     */
    final class Alternate implements Runnable {
        final Socket host_socket;

        public Alternate(Socket host_socket) {
            this.host_socket = host_socket;
        }

        @Override
        public void run() {
            Message msg = null;
            while (true) {

                System.out.println("Start Alternate");

                // wait for {@link RequestTimeMsg} from Android device

                    try {
//                        msg = waitForRequestTimeMsg(host_socket);
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(host_socket.getInputStream()));
                        String request = bufferedReader.readLine();
                        if (request == null) {
                            host_socket.close();
                            break;
                        }
                        System.out.println("Receiving RequestTimeMsg: " + request);
                        PrintWriter writer = new PrintWriter(host_socket.getOutputStream(), true);
                        if (request.equals("connected?"))
                            writer.println("connected");
                        // send {@link ResponseTimeMsg} with current system time to Android device
//                        sendResponseTimeMsg(host_socket);
                        else if (request.equals("current time")) {
                            String time = String.valueOf(System.nanoTime());
                            System.out.println(time);
                            writer.println(time);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        try {
                            host_socket.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        break;
                    }
            }
        }

    }

    /**
     * close all the host sockets
     */
    public void shutDown() {
        for (Map.Entry<String, Socket> device_hostsocket : this.device_hostsocket_map.entrySet()) {
            try {
                device_hostsocket.getValue().close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ADBExecutor adb_executor = new ADBExecutor("D:\\Android\\sdk\\platform-tools\\adb.exe");
        while (true) {
            Map<String, Integer> device_hostport_map = adb_executor.execAdbOnlineDevicesPortForward();
            final PCHost host = new PCHost(device_hostport_map);

            host.startTimePollingService();

            for (Map.Entry<String, Socket> device_hostsocket : host.device_hostsocket_map.entrySet()) {
                while (!device_hostsocket.getValue().isClosed()) {
                    Thread.sleep(10);
                }
            }
            System.out.println("start overrrrrrrrrrrrrr");
        }

/*
        adb_executor.copyFromAll("/storage/emulated/0/Android/data/com.njucs.ballgame/files/BallGameDir",
                "log");
        Map<String, String> devices = adb_executor.execAdbDevices();
        List<String> logDirNames = new ArrayList<>();
        for (Map.Entry<String, String> entry : devices.entrySet()) {
            logDirNames.add(entry.getValue() + entry.getKey());
        }
        ExtractGameState.main(logDirNames.toArray(new String[0]));*/
    }

}



