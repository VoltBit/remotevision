package acs.remotevision;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
    private static final UUID remoteVisionUUID =
            UUID.fromString("f9cf2946-d67d-4e6d-8dbd-2a1cd6794753");
    private Handler btNetworkHandle, inputHandler, logHandler;
    private ArrayAdapter networksAdapter;
    private ListView networksView;
    public ConnectedThread conThread;
    public AcceptThread accThread;

    private static final String TAG = "Fatal";
    private static final String devName = "RemoteVision Android server";
    private String passString;
    private String selectedSSID = "";

    private final Integer REQUEST_ENABLE_BT = 1;
    private final Integer MESSAGE_READ = 0;
    private final Integer PASSWORD_READ= 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button reset = (Button) findViewById(R.id.resetButton);
        networksAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);

        networksView = (ListView) findViewById(R.id.availableNetworks);
        networksView.setAdapter(networksAdapter);

        logHandler = new Handler() {
            TextView status = (TextView) findViewById(R.id.statusView);
            TextView log = (TextView) findViewById(R.id.logView);
            public void handleMessage(Message msg) {
                if (((String)msg.obj).contains("Connected"))
                    status.setText((String)msg.obj);
                log.setText((String)msg.obj);
            }
        };

        btNetworkHandle = new Handler() {
            public void handleMessage(Message msg) {
                if (!((String) msg.obj).contains("SSID_OVER") &&
                        !((String) msg.obj).contains("PASS_OVER"))
                    networksAdapter.add((String) msg.obj);
//                else if (!((String) msg.obj).contains("PASS_OVER")) {
//                    makePasswordDialog();
//                }
            }
        };

        inputHandler = new Handler() {
            public void handleMessage(Message msg) {
                TextView logText = (TextView) findViewById(R.id.logView);
                byte[] buffer;
                logText.setText("Sending: " + selectedSSID);
                conThread.write(selectedSSID.getBytes());
                buffer = ((String)msg.obj).getBytes();
                logText.setText("Sending: " + (String)msg.obj);
                try {
                    Thread.sleep(800);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                conThread.write(buffer);
            }
        };

        networksView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView loggingTextView = (TextView) findViewById(R.id.logView);
                String selected = (String) parent.getItemAtPosition(position);
                loggingTextView.setText("Sending: " + selected);
                selectedSSID = selected;
                makePasswordDialog();
                //byte[] buffer = selected.getBytes();
                //conThread.write(buffer);
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView status = (TextView) findViewById(R.id.statusView);
                TextView log = (TextView) findViewById(R.id.logView);
                status.setText("Listening...");
                log.setText("");
                networksAdapter.clear();
                accThread.cancel();
//                enableBluetooth();
                try {
                    startBluetoothServer();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        enableBluetooth();

        try {
            startBluetoothServer();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
    Enables the Bluetooth receiver and makes the device discoverable.
     */
    public void enableBluetooth() {
        if (bta == null) {
            Log.v(TAG, "Bluetooth not supported");
            System.exit(0);
        }
        if (!bta.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 400);
        startActivity(discoverableIntent);
    }

    /*
    Starts the server by creating a thread where a Bluetooth socket will be listening.
     */
    public void startBluetoothServer() throws InterruptedException {
//        while (true) {
            accThread = new AcceptThread();
            accThread.start();
//            accThread.join();
//        }
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            TextView status = (TextView) findViewById(R.id.statusView);

            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = bta.listenUsingRfcommWithServiceRecord(devName, remoteVisionUUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;
            status.setText("Listening...");
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    logHandler.obtainMessage(MESSAGE_READ, "Connected".length(), -1, "Connected").sendToTarget();
                    conThread = new ConnectedThread(socket);
                    conThread.start();
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
                conThread.cancel();
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

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    String recvMsg = new String(buffer, "UTF-8");
                    Arrays.fill(buffer, (byte) 0);
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    btNetworkHandle.obtainMessage(MESSAGE_READ, bytes, -1, recvMsg).sendToTarget();
                    logHandler.obtainMessage(MESSAGE_READ, bytes, -1, recvMsg).sendToTarget();
                } catch (IOException e) {
                    break;
                }
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

    /*
    Creates the dialog used to get the password from the user.
    When the password field is left empty the passString variable is set to NPASS to identify
    such cases.
     */
    public void makePasswordDialog() {
//        LayoutInflater inflater = this.getLayoutInflater();
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View promptView = inflater.inflate(R.layout.password_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final EditText passInput = (EditText) promptView.findViewById(R.id.passField);
        final TextView logText = (TextView) findViewById(R.id.logView);
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
}
