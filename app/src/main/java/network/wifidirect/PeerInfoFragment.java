package network.wifidirect;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.*;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.njucs.main.MainActivity;
import com.njucs.main.R;
import constant.Constant;
import consistencyinfrastructure.group.GroupConfig;
import consistencyinfrastructure.group.member.SystemNode;
import consistencyinfrastructure.login.SessionManagerWrapper;
import ics.mobilememo.sharedmemory.atomicity.AtomicityRegisterClientFactory;
import log.TimePolling;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by Mio on 2015/12/22.
 */
public class PeerInfoFragment extends Fragment implements WifiP2pManager.ConnectionInfoListener {

    private WifiP2pDevice peerDevice;
    private View mContentView;
    private ProgressDialog progressDialog;
    private WifiP2pInfo info;
    private ServerThread serverThread = null;
    private WifiP2pGroup group;

    public static String TAG = PeerInfoFragment.class.getName();


    public void showDetail(WifiP2pDevice device)
    {
        this.peerDevice = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(device.toString());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        mContentView = inflater.inflate(R.layout.device_detail, null);
        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = peerDevice.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                if (progressDialog != null && progressDialog.isShowing())
                {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + peerDevice.deviceAddress, true, true
//                        new DialogInterface.OnCancelListener() {
//
//                            @Override
//                            public void onCancel(DialogInterface dialog) {
//                                ((DeviceActionListener) getActivity()).cancelDisconnect();
//                            }
//                        }
                );
                ((PeerListFragment.DeviceActionListener) getActivity()).connect(config);

            }
        });

        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v)
                    {
                        ((PeerListFragment.DeviceActionListener) getActivity()).disconnect();
                    }
                });
        mContentView.findViewById(R.id.btn_startGame).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        ((PeerListFragment.DeviceActionListener) getActivity()).startMainActivity(info, serverThread);
                    }
                }
        );


        return mContentView;
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info)
    {
        if (progressDialog != null && progressDialog.isShowing())
        {
            progressDialog.dismiss();
        }
        this.info = info;
        this.getView().setVisibility(View.VISIBLE);

        // The owner IP is now known.
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText("am I group owner? "
                + ((info.isGroupOwner == true) ? "yes"
                : "no"));

        // InetAddress from WifiP2pInfo struct.
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());

        // hide the connect button
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);

        // reveal startGame button
        mContentView.findViewById(R.id.btn_disconnect).setVisibility(View.VISIBLE);

        mContentView.findViewById(R.id.btn_startGame).setVisibility(View.VISIBLE);


        // start PollingTime functionality
        if (MainActivity.DEBUG)
            TimePolling.INSTANCE.establishDeviceHostConnection();

        if (info.isGroupOwner == true)
        {
            if (serverThread != null &&
                    serverThread.isAlive())
            {
                serverThread.terminate();
                serverThread = null;
            }
            if (serverThread == null || !serverThread.isAlive())
            {

                final InetAddress ownerAddress = info.groupOwnerAddress;

                ServerThread thread = new ServerThread(
                ) {
                    @Override
                    public void run()
                    {

                        try
                        {
                            serverSocket = new ServerSocket();
                            serverSocket.setReuseAddress(true);
                            serverSocket.bind(new InetSocketAddress(ownerAddress, Constant.SERVERPORT));
                            Log.d(TAG, "ServerSocket bind success");

                            try
                            {

                                    try
                                    {
                                        final Socket socket = serverSocket.accept();
                                        Log.d(TAG, "ServerSocket accept success");

                                        try
                                        {
                                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                                                    socket.getInputStream()));
                                            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

                                            while (true)
                                            {
                                                try
                                                {
                                                    Thread.sleep(50);
                                                } catch (InterruptedException e)
                                                {
                                                    break;
                                                }
                                            }
                                            Log.d(TAG, "Server start game pressed");

                                            Log.d(TAG, "" + bufferedReader.readLine());
                                            printWriter.println("Ack");
                                            Log.d(TAG, "" + bufferedReader.readLine());
                                            String deviceAddress = socket.getInetAddress().getHostAddress();

                                            if (!new SessionManagerWrapper().isSessionAlive(ownerAddress.getHostAddress(), deviceAddress, 100, 101))
                                            {
                                                GroupConfig.INSTANCE.clearReplicas();
                                                GroupConfig.INSTANCE.addReplica(new SystemNode(100, "server", ownerAddress.getHostAddress()));
                                                GroupConfig.INSTANCE.addReplica(new SystemNode(101, "client", deviceAddress));
                                                // work around ATO code
                                                new SessionManagerWrapper()
                                                        .setNodeID(100)
                                                        .setNodeName("server")
                                                        .setNodeIp(ownerAddress.getHostAddress())
                                                        .setNodeAlgType(AtomicityRegisterClientFactory.MWMR_ATOMICITY)
                                                        .setOtherID(Arrays.asList(101))
                                                        .setOtherIp(deviceAddress);


                                            }
                                        } catch (IOException e)
                                        {
                                            e.printStackTrace();
                                        } finally
                                        {
                                            try
                                            {
                                                socket.close();
                                            } catch (IOException e)
                                            {
                                                e.printStackTrace();
                                            }
                                        }
                                    } catch (IOException e)
                                    {
                                        e.printStackTrace();
                                    }


                            } finally
                            {
                                serverSocket.close();

                            }
                        }

                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                }

            };

            this.serverThread = thread;
            serverThread.start();

        }

    }

}

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */

    public void resetViews()
    {
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        mContentView.findViewById(R.id.btn_disconnect).setVisibility(View.GONE);
        mContentView.findViewById(R.id.btn_startGame).setVisibility(View.GONE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        this.getView().setVisibility(View.GONE);
    }


}

class ServerThread extends Thread {
    ServerSocket serverSocket;

    public void terminate()
    {
        try
        {
            if (serverSocket != null)
                serverSocket.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}


