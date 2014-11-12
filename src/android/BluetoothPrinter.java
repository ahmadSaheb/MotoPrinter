package org.apache.cordova.sipkita;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Set;
import java.util.UUID;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;




public class BluetoothPrinter extends CordovaPlugin {
	private static final String LOG_TAG = "BluetoothPrinter";
	BluetoothAdapter mBluetoothAdapter;
	BluetoothSocket mmSocket;
	BluetoothDevice mmDevice;
	OutputStream mmOutputStream;
	InputStream mmInputStream;
	Thread workerThread;
	byte[] readBuffer;
	int readBufferPosition;
	int counter;
	volatile boolean stopWorker;
	
	//Motorola 
	private ZebraPrinter printer;
	private Connection printerConnection;
	private String name ;

	public BluetoothPrinter() {}

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
	    if (action.equals("open")) {
				name = args.getString(0);
				doConnectionTest();
			 
		} else if (action.equals("print")) {
			 
		} else if (action.equals("close")) {
			try {
				 
			} catch (IOException e) {
			 
			}
			return true;
		}
		return false;
	}
	
	
	 private void doConnectionTest() {
        printer = connect();
        if (printer != null) {
            sendTestLabel();
        } else {
            disconnect();
        }
    }
	private void sendTestLabel() {
        try {
            byte[] configLabel = getConfigLabel();
            printerConnection.write(configLabel);
            //setStatus("Sending Data", Color.BLUE);
				sleep(1500);
            if (printerConnection instanceof BluetoothConnection) {
                String friendlyName = ((BluetoothConnection) printerConnection).getFriendlyName();
               // setStatus(friendlyName, Color.MAGENTA);
                sleep(500);
            }
        } catch (ConnectionException e) {
           // setStatus(e.getMessage(), Color.RED);
        } finally {
            disconnect();
        }
    }
	
	private byte[] getConfigLabel() {
        PrinterLanguage printerLanguage = printer.getPrinterControlLanguage();

        byte[] configLabel = null;
        if (printerLanguage == PrinterLanguage.ZPL) {
            configLabel = "^XA^FO17,16^GB379,371,8^FS^FT65,255^A0N,135,134^FDTEST^FS^XZ".getBytes();
        } else if (printerLanguage == PrinterLanguage.CPCL) {
            String cpclConfigLabel = "! 0 200 200 406 1\r\n" + "ON-FEED IGNORE\r\n" + "BOX 20 20 380 380 8\r\n" + "T 0 6 137 177 TEST\r\n" + "PRINT\r\n";
            configLabel = cpclConfigLabel.getBytes();
        }
        return configLabel;
    }
	
	 public   void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
	
	public void disconnect() {
        try {
            //setStatus("Disconnecting", Color.RED);
            if (printerConnection != null) {
                printerConnection.close();
            }
            //setStatus("Not Connected", Color.RED);
        } catch (ConnectionException e) {
            //setStatus("COMM Error! Disconnected", Color.RED);
        } finally {
            //enableTestButton(true);
        }
    }
	
	
	
	 public ZebraPrinter connect() {
        //setStatus("Connecting...", Color.YELLOW);
        printerConnection = null;
        if (true) {
            printerConnection = new BluetoothConnection(name);  //name = MAC address
            //SettingsHelper.saveBluetoothAddress(this, getMacAddressFieldText());
        } else {
            /*try {
                int port = Integer.parseInt(getTcpPortNumber());
                printerConnection = new TcpConnection(getTcpAddress(), port);
               // SettingsHelper.saveIp(this, getTcpAddress());
               // SettingsHelper.savePort(this, getTcpPortNumber());
            } catch (NumberFormatException e) {
               // setStatus("Port Number Is Invalid", Color.RED);
                return null;
            }*/
        }

        try {
            printerConnection.open();
           // setStatus("Connected", Color.GREEN);
        } catch (ConnectionException e) {
           // setStatus("Comm Error! Disconnecting", Color.RED);
            sleep(1000);
            disconnect();
        }

        ZebraPrinter printer = null;

        if (printerConnection.isConnected()) {
            try {
                printer = ZebraPrinterFactory.getInstance(printerConnection);
              //  setStatus("Determining Printer Language", Color.YELLOW);
                PrinterLanguage pl = printer.getPrinterControlLanguage();
               // setStatus("Printer Language " + pl, Color.BLUE);
            } catch (ConnectionException e) {
                //setStatus("Unknown Printer Language", Color.RED);
                printer = null;
                sleep(1000);
                disconnect();
            } catch (ZebraPrinterLanguageUnknownException e) {
                //setStatus("Unknown Printer Language", Color.RED);
                printer = null;
                sleep(1000);
                disconnect();
            }
        }

        return printer;
    }

}
