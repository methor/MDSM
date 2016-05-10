package network.wifidirect;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import com.njucs.main.MainActivity;
import com.njucs.main.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

import consistencyinfrastructure.group.GroupConfig;
import consistencyinfrastructure.group.member.SystemNode;
import consistencyinfrastructure.login.SessionManagerWrapper;
import constant.Constant;
import ics.mobilememo.sharedmemory.atomicity.AtomicityRegisterClientFactory;


/**
 * Created by Mio on 2015/10/28.
 */
public class ConnectActivity extends AppCompatActivity implements PeerListFragment.DeviceActionListener {

    private IntentFilter intentFilter = null;
    public WifiP2pManager.Channel mChannel = null;
    public WifiP2pManager mWifiManager = null;
    private BroadcastReceiver receiver = null;
    public static String TAG = ConnectActivity.class.getName();


    public boolean feedbackEnabled;

    private boolean isWifiP2pEnabled;


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver = new WifiBroadcastReceiver(mWifiManager, mChannel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();


    }

    @Override
    protected void onStart() {
        super.onStart();


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, Build.MODEL);
        Log.d(TAG, Build.PRODUCT);
        Log.d(TAG, Build.BRAND);
        Log.d(TAG, Build.SERIAL);

        setContentView(R.layout.activity_connect);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

// App Logo
        toolbar.setLogo(R.drawable.ic_launcher);
// Title
        toolbar.setTitle("BallGame Connect");
// Sub Title
        //toolbar.setSubtitle("Sub title");

        setSupportActionBar(toolbar);
        feedbackEnabled = true;


        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mWifiManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        mChannel = mWifiManager.initialize(this, getMainLooper(), null);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_items, menu);
        return true;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.atn_direct_enable:
                if (mWifiManager != null && mChannel != null) {

                    // Since this is the system wireless settings activity, it's
                    // not going to send us a result. We will be notified by
                    // WiFiDeviceBroadcastReceiver instead.

                    startActivity(new Intent(Settings.ACTION_SETTINGS));
                } else {
                    Log.e(TAG, "channel or manager is null");
                }
                return true;

            case R.id.atn_direct_discover:
                if (!isWifiP2pEnabled) {
                    Toast.makeText(ConnectActivity.this, R.string.p2p_off_warning,
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
                final PeerListFragment fragment = (PeerListFragment) getFragmentManager()
                        .findFragmentById(R.id.frag_list);
                fragment.onInitiateDiscovery();
                mWifiManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(ConnectActivity.this, "Discovery Initiated",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(ConnectActivity.this, "Discovery Failed : " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void connect(WifiP2pConfig config) {
        mWifiManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WifiBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(ConnectActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void showDetail(WifiP2pDevice device) {
        PeerInfoFragment peerInfoFragment = ((PeerInfoFragment) getFragmentManager().findFragmentById(R.id.frag_detail));
        peerInfoFragment.showDetail(device);

    }

    @Override
    public void disconnect() {
        final PeerInfoFragment fragment = (PeerInfoFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.resetViews();
        mWifiManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);

            }

            @Override
            public void onSuccess() {
                fragment.getView().setVisibility(View.GONE);
            }

        });
    }

    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void resetData() {
        PeerListFragment fragmentList = (PeerListFragment) getFragmentManager()
                .findFragmentById(R.id.frag_list);
        PeerInfoFragment fragmentDetails = (PeerInfoFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        if (fragmentList != null) {
            fragmentList.clearPeers();
        }
        if (fragmentDetails != null) {
            fragmentDetails.resetViews();
        }
    }

    public void chooseFeedback(View view)
    {
        if (!(view instanceof RadioButton))
            return;
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId())
        {
            case R.id.feedback:
                if (checked)
                {
                    feedbackEnabled = true;
                }
                break;
            case R.id.nofeedback:
                if (checked)
                {
                    feedbackEnabled = false;
                }
                break;
        }
    }


    public void startMainActivity(final WifiP2pInfo info, final Thread serverThread) {

        final ProgressDialog progressDialog = ProgressDialog.show(this, "Press back to cancel", "Waiting for peers", true,
                true, new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {

                    }
                });
        progressDialog.show();


        Runnable runnable = new Runnable() {
            @Override
            public void run() {


                String ownerAddress = info.groupOwnerAddress.getHostAddress();
                Intent intent = new Intent(ConnectActivity.this, MainActivity.class);
                Bundle bundle = new Bundle();
                String consistency = null;

                if (info.isGroupOwner == true) {
                    while (serverThread.isAlive()) {
                        try {
                            Thread.sleep(20);
                            serverThread.interrupt();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    consistency = ((PeerInfoFragment) getFragmentManager().
                            findFragmentById(R.id.frag_detail)).consistency;
                } else {

                    try {
                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress(info.groupOwnerAddress, Constant.SERVERPORT));
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                                socket.getInputStream()));
                        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                        try {
                            printWriter.println("Request");
                            Log.d(TAG, "" + bufferedReader.readLine());
                            printWriter.println("Ack");
                            consistency = bufferedReader.readLine();
                            feedbackEnabled = Boolean.valueOf(bufferedReader.readLine());

                            String myIp = socket.getLocalAddress().getHostAddress();

                            if (!new SessionManagerWrapper().isSessionAlive(myIp, ownerAddress, 101, 100)) {
                                GroupConfig.INSTANCE.clearReplicas();

                                GroupConfig.INSTANCE.addReplica(new SystemNode(100, "server", ownerAddress));
                                GroupConfig.INSTANCE.addSelf(new SystemNode(101, "client", myIp));

                            }
                        } finally {
                            socket.close();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                //bundle.putString("orientation1", GameModel.ORIENTATION_NORTH);
                //bundle.putString("orientation2", GameModel.ORIENTATION_SOUTH);
                bundle.putInt("id1", 100);
                bundle.putInt("id2", 101);
                if (consistency == null)
                    throw new RuntimeException();
                bundle.putString(getString(R.string.consistency), consistency);
                bundle.putBoolean(getString(R.string.feedback), feedbackEnabled);
                intent.putExtras(bundle);

                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();

                ((PeerInfoFragment) getFragmentManager().findFragmentById(R.id.frag_detail)).returnFromActivity = true;
                startActivity(intent, bundle);
            }
        };

        new Thread(runnable).start();
    }

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }
}


