package mindc.btcontroller;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.annotation.BoolRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import android.os.Handler;

import org.w3c.dom.Text;

import static mindc.btcontroller.R.id.logging;
import static mindc.btcontroller.R.id.noPass;
import static mindc.btcontroller.R.id.pairedBtDevices;
import static mindc.btcontroller.R.id.passField;

public class MainActivity extends AppCompatActivity {

    private ArrayAdapter pairedDevsAdapter, networksAdapter;
    private BluetoothDevice comPartner;
    private static final String TAG = "MyActivity";
    private static final Integer REQUEST_ENABLE_BT = 1;
    private ArrayList inRangeDevices = new ArrayList<>();
    private BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
    private static final UUID remoteVisionUUID = UUID.fromString("f9cf2946-d67d-4e6d-8dbd-2a1cd6794753");
    private static final String NAME = "RemoteVision";
    public TextView recvTextView;
    public Handler handler, networksHandler, inputHandler;
    public Integer ssid_count;
    public ListView networksView;
    public ArrayList<String> ssids = new ArrayList<>();
    public ConnectedThread conThread;
    public Boolean ssid_ack = false, pass_ack = false;
    public AlertDialog passDialog;
    private String passString;
    private final int MESSAGE_READ = 0;
    private final int PASSWORD_READ= 2;

    private View.OnClickListener btClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getApplicationContext(), "Pressed the item!", Toast.LENGTH_LONG).show();
        }
    };

    public void makePasswordDialog() {
//        LayoutInflater inflater = this.getLayoutInflater();
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View promptView = inflater.inflate(R.layout.password_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final EditText passInput = (EditText) promptView.findViewById(passField);
        final TextView logText = (TextView) findViewById(logging);
        builder.setView(promptView);
        builder.setMessage("Enter WiFi password");
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                final String[] input = new String[1];
                if (passInput == null) {
                    logText.setText("NULL err");
                } else {
                    input[0] = passInput.getText().toString();
                    if (input[0].matches("")) {
                        passString = "NPASS";
                        logText.setText(passString);
                    } else {
                        passString = input[0];
                    }
//                    logText.setText(input[0]);
                    inputHandler.obtainMessage(PASSWORD_READ, passString.length(), -1, passString).sendToTarget();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

//        final CheckBox noPassCheck = (CheckBox) findViewById(noPass);
//        if (noPassCheck.isChecked()) {
//            passInput.setFocusable(false);
//        }
        AlertDialog passDialog = builder.create();
        passDialog.show();
//        return builder.create();
//        while(passDialog.isShowing());
//        logText.setText(input[0]);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView logText = (TextView) findViewById(logging);

        pairedDevsAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        networksAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);

        recvTextView = (TextView) findViewById(R.id.recvText);
        handler = new Handler() {
            public void handleMessage(Message msg) {
                recvTextView.setText((String) msg.obj);
            }
        };

        networksView = (ListView) findViewById(R.id.availableNetworks);
        networksView.setAdapter(networksAdapter);

        networksHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (!((String) msg.obj).contains("ACK_SSID"))
                    networksAdapter.add((String) msg.obj);
                else if (!((String) msg.obj).contains("ACK_PASS")) {
                    makePasswordDialog();
//                    logText.setText("Sent: " + s);
                }
            }
        };

        inputHandler = new Handler() {
            public void handleMessage(Message msg) {
                TextView logTxt = (TextView) findViewById(logging);
                logText.setText("Sending: " + (String)msg.obj);
                byte[] buffer = ((String)msg.obj).getBytes();
                conThread.write(buffer);
            }
        };

        networksView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView loggingTextView = (TextView) findViewById(R.id.logging);
                String selected = (String) parent.getItemAtPosition(position);
                loggingTextView.setText("Sending: " + selected);
                byte[] buffer = selected.getBytes();
                conThread.write(buffer);
            }
        });
//        passDialog = makePasswordDialog();
//        passDialog.show();
//        Button testButton = passDialog.getButton(DialogInterface.BUTTON_POSITIVE);
//        testButton.setOnClickListener(passClickListener);
//        makePasswordDialog();
//        logText.setText(pass);
        btComm();
    }

    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                pairedDevsAdapter.add(device.getName() + "\n" + device.getAddress());
                inRangeDevices.add(device);
            }
        }
    };

    private void makeDiscoverable() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    public void btComm() {
        TextView pairedNrTextView = (TextView) findViewById(R.id.btPairedTextView);
        ListView listView = (ListView) findViewById(pairedBtDevices);
        listView.setAdapter(pairedDevsAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView loggingTextView = (TextView) findViewById(R.id.logging);
                String selected = (String) parent.getItemAtPosition(position);
                Toast toast = Toast.makeText(getApplicationContext(), getAddress(selected),
                        Toast.LENGTH_SHORT);
                toast.show();
                comPartner = (BluetoothDevice) inRangeDevices.get(position);

//                if (!comPartner.fetchUuidsWithSdp()) {
//                    loggingTextView.setText("UUID fetch failed");
//                } else {
//                    ParcelUuid uuids[] = comPartner.getUuids();
//                    if (uuids == null) {
//                        loggingTextView.setText("UUID error - null");
//                    } else if (uuids.length != 1) {
//                        loggingTextView.setText("UUID error" + uuids.length);
//                    } else {
//                        Toast dispUUID = Toast.makeText(getApplicationContext(), uuids[0].toString(), Toast.LENGTH_LONG);
//                        dispUUID.show();
//                    }
//                }
            }
        });

        if (bta == null) {
            Log.v(TAG, "Bluetooth not supported");
            System.exit(0);
        }
        if (!bta.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        makeDiscoverable();
        Set<BluetoothDevice> pairedDevs = bta.getBondedDevices();
        Log.v(TAG, ((Integer) pairedDevs.size()).toString());
        pairedNrTextView.setText("Paired devs: " + ((Integer) pairedDevs.size()).toString());
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(btReceiver, filter);
        bta.startDiscovery();
        AcceptThread accThread = new AcceptThread();
        accThread.start();
    }

    private String getAddress(String device) {
        String lines[] = device.split("\\r?\\n");
        return lines[1];
    }

    private String getName(String device) {
        String lines[] = device.split("\\r?\\n");
        return lines[0];
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        TextView loggingTextView = (TextView) findViewById(R.id.logging);

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = bta.listenUsingRfcommWithServiceRecord(NAME, remoteVisionUUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            loggingTextView.setText("Listening...");
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    conThread = new ConnectedThread(socket);
                    conThread.start();
//                    manageConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Get ssid_count

            try {
                bytes = mmInStream.read(buffer);
                if (bytes > 4) {
                    //->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>TODO: handle this case!
                    ssid_count = 1;
                    handler.obtainMessage(MESSAGE_READ, bytes, -1, ssid_count.toString()).sendToTarget();
                } else {
                    ssid_count = (buffer[0] & 0xFF) |
                            ((buffer[1] & 0xFF) << 8) |
                            ((buffer[2] & 0xFF) << 16) |
                            ((buffer[3] & 0xFF) << 24);
                    handler.obtainMessage(MESSAGE_READ, bytes, -1, ssid_count.toString()).sendToTarget();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    // Clear the buffer
                    for (int i = 0; i < 1024; ++i) buffer[i] = 0;
                    bytes = mmInStream.read(buffer);
                    String recvMsg = new String(buffer, "UTF-8");
                    if (recvMsg == "ACK_SSID") {
                        ssid_ack = true;
                        // The selected SSID was successfully sent, now ask for the password
                        networksHandler.obtainMessage(MESSAGE_READ, bytes, -1, recvMsg).sendToTarget();
                        throw new IOException();
                    }
                    if (!ssids.contains(recvMsg)) {
                        ssids.add(recvMsg);
                        networksHandler.obtainMessage(MESSAGE_READ, bytes, -1, recvMsg).sendToTarget();
                    }
                } catch (IOException e) {
                    break;
                }
            }
            try {
                bytes = mmInStream.read(buffer);
                String recvMsg = new String(buffer, "UTF-8");
                if (recvMsg == "ACK_PASS") {
                    pass_ack = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}
