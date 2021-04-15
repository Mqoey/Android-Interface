/*
 * Developed by Mqondisi Ndlovu Copyright (c) 2021. contact @ 0772783880
 */

package com.axis.revmaxinterface;

///////////////////////////////////////////////////////////////////////////////////////////
//***this is the first method called in the app
//***it checks for card details and starts the relevant services to watch for files
//***if the card is not registered or information cannot be found, services are not started
///////////////////////////////////////////////////////////////////////////////////////////

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.axis.revmaxinterface.Audit.AuditActivity;
import com.axis.revmaxinterface.Audit.AuditDoWork;
import com.axis.revmaxinterface.Audit.AuditMainActivity;
import com.axis.revmaxinterface.Audit.MainDb;
import com.crashlytics.android.Crashlytics;
import com.secureflashcard.wormapi.WORM_ERROR;
import com.secureflashcard.wormapi.WormAccess;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.SGD;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.ZebraPrinterLinkOs;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_EBANBLE_BTCD = 3;
    private static final int REQUEST_DEVICE = 2;
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //revmax variables
    public static int REQUEST_BLUETOOTH = 1;
    public static int triggerZreport = 0, triggerPrinter = 0;
    static ArrayList<String> TaxesList;
    static int TaxConfigFlag;
    static String RevmaxSerialNumber, RevmaxFirmware, dbname = "RevInterface";
    static Integer currentSize, totalCapacity, regconfirmed = 1, result = 0;
    static String RevMaxInvoicetoSave = "";
    static String IMEI = "", bpn = "na", vat = "na", compname = "na", addr1 = "na", addr2 = "na", addr3 = "na", regn = "na", serial = "na", datetime = "na";
    static String statz = "Fiscalised", firmWareID = "", wormVersion = "";
    static UtilitiesClass utilitiesClass;
    static ConnectionTest connectionTest;
    static String DeviceToken = "";
    static Cursor cr;
    static String address = "";
    private static Context mContext;
    private final Handler mHandler = new Handler();
    wormTest mywormTest;
    BluetoothAdapter BTAdapter;
    String output;
    ArrayList<wormTest.WormEntry> list;
    TextView textCompanyName, textstatus, textFirmwareVersion, textbpn, textvat, textaddr1, textaddr2, textadd3, textRegNumber, textserial, textdate, textMemoryStatus;
    ////////////////////////////////////////////////////
    Button printerButton;
    String devname, err;
    ImageView prinpik;
    private boolean initDone = false;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private WormAccess worm = new WormAccess();
    private Activity mActivity;
    private BluetoothAdapter mBtAdapter;
    private BluetoothSocket mBtSocket;
    private Connection connection;
    private ZebraPrinter printer;

    public static void restartActivity(Activity activity) {
        if (Build.VERSION.SDK_INT >= 11) {
            activity.recreate();
        } else {
            activity.finish();
            activity.startActivity(activity.getIntent());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //initialize reporting
        Fabric.with(this, new Crashlytics());
        utilitiesClass = new UtilitiesClass(getApplicationContext());
        connectionTest = new ConnectionTest();


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //***Required to power cycle REVMAX before use
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RevMaxInterface:MyWormWake");
        wakeLock.acquire();
        mContext = getApplicationContext();
        mActivity = MainActivity.this;
        list = new ArrayList<wormTest.WormEntry>();
        TaxesList = new ArrayList<String>();

        //DEFINE TEXT FIELDS
        textbpn = (TextView) findViewById(R.id.BPN);
        textvat = (TextView) findViewById(R.id.vat);
        textaddr1 = (TextView) findViewById(R.id.addrl1);
        textaddr2 = (TextView) findViewById(R.id.addrl2);
        textadd3 = (TextView) findViewById(R.id.addrl3);
        textRegNumber = (TextView) findViewById(R.id.reg);
        textserial = (TextView) findViewById(R.id.textView);
        textdate = (TextView) findViewById(R.id.textViewdatereg);
        textFirmwareVersion = (TextView) findViewById(R.id.textView2);
        textMemoryStatus = (TextView) findViewById(R.id.textView3);
        textstatus = (TextView) findViewById(R.id.status);
        textCompanyName = (TextView) findViewById(R.id.compname);

        //ENABLES STRICT MODE TO ALLOW LONG PROCESSES ON UI THREAD
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        //***request permissions, without user allowing all permissions then app exits. Only for versions above android M
        //block also extracts device IMEI for Crashlytics logs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.WAKE_LOCK, Manifest.permission.BLUETOOTH, Manifest.permission.READ_PHONE_STATE
                }, 10);
                return;
            } else {
                //GET DEVICE IMEI AND MODEL
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    TelephonyManager tm = (TelephonyManager) getSystemService(this.TELEPHONY_SERVICE);
                    IMEI = tm.getMeid();
                }
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                    TelephonyManager tm = (TelephonyManager) getSystemService(this.TELEPHONY_SERVICE);
                    IMEI = tm.getImei(0);
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    TelephonyManager tm = (TelephonyManager) getSystemService(this.TELEPHONY_SERVICE);
                    IMEI = tm.getDeviceId();
                }
            }
        } else {
            //android versions less than M
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                TelephonyManager tm = (TelephonyManager) getSystemService(this.TELEPHONY_SERVICE);
                IMEI = tm.getMeid();
            }
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                TelephonyManager tm = (TelephonyManager) getSystemService(this.TELEPHONY_SERVICE);
                IMEI = tm.getImei(0);
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                TelephonyManager tm = (TelephonyManager) getSystemService(this.TELEPHONY_SERVICE);
                IMEI = tm.getDeviceId();
            }
        }

        // creates databasse if its not already setup
        CheckDatabaseOrCreate();
        //initialise card
        CheckForCard();

        Crashlytics.setUserIdentifier(IMEI);

        findViewById(R.id.TestConnectionButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //postToast("here");
                String result = connectionTest.InitiateSend();
                if (result.equals(String.valueOf(utilitiesClass.SuccessResult))) {
                    postToast("Connection Success");
                } else {
                    //postToast("Connection Error");
                    postToast("Connection Established");
                }
            }
        });
        findViewById(R.id.zreportTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postToast("Zreporting process started");
                triggerZreport = 1;
//                startService(new Intent(getBaseContext(), DoWork.class));
            }
        });

        findViewById(R.id.audit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startService(new Intent(getBaseContext(), AuditDoWork.class));
                Intent intent = new Intent(MainActivity.this, AuditMainActivity.class);
                startActivity(intent);
            }
        });

        printerButton = findViewById(R.id.printerConnection);
        printerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mBtAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBtAdapter != null) {
                    if (mBtAdapter.isEnabled()) {
                        selectDevice();
                    } else {
                        enableBluetooth();
                        selectDevice();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.msg_bluetooth_is_not_supported, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            }
        });
    }

    private void enableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    private void selectDevice() {
        Intent selectDevice = new Intent(this, DeviceActivity.class);
        startActivityForResult(selectDevice, REQUEST_DEVICE);
    }

    public void connectZebra(final String address) {

        invokeHelper(new MethodInvoker() {
            @Override
            public void invoke() throws IOException {
                try {
                    connection = null;
                    connection = new BluetoothConnection(address);
                    connection.open();

                    if (connection.isConnected()) {
                        try {

                            printer = ZebraPrinterFactory.getInstance(connection);
                            //setStatus("Determining Printer Language", Color.YELLOW);
                            String pl = SGD.GET("device.languages", connection);
                            //setStatus("Printer Language " + pl, Color.BLUE);
                        } catch (ConnectionException e) {
                            //setStatus("Unknown Printer Language", Color.RED);
                            printer = null;
                            //  DemoSleeper.sleep(1000);
                            disconnect();
                        } catch (ZebraPrinterLanguageUnknownException e) {
                            // setStatus("Unknown Printer Language", Color.RED);
                            printer = null;
                            //DemoSleeper.sleep(1000);
                            disconnect();
                        }
                    }
                } catch (ConnectionException v) {
                    v.printStackTrace();
                    postToast("Connection to Device has Failed");
                    return;
                }
                try {
                    ZebraPrinterLinkOs linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(printer);
                    PrinterStatus printerStatus = (linkOsPrinter != null) ? linkOsPrinter.getCurrentStatus() : printer.getCurrentStatus();
                    if (printerStatus.isReadyToPrint) {

                        byte prish[] = null;
                        String connmsg;

                        try {
                            PrinterLanguage printerLanguage = printer.getPrinterControlLanguage();
                            SGD.SET("device.languages", "zpl", connection);

                            if (printerLanguage == PrinterLanguage.ZPL) {

                                connmsg = "^XA^LL350^FO5,200^A0I,30,30^FDDEVICE CONNECTION SUCCESS^FS" +
                                        "^FO5,30^A0I,30,30^FD ^FS" +
                                        "^FO5,50^A0I,30,30^FD ^FS " +
                                        "^XZ";

                                prish = connmsg.getBytes();
                            }
                            connection.write(prish);
                            try {
//                                db=openOrCreateDatabase(dbapp, SQLiteDatabase.CREATE_IF_NECESSARY, null);
//                                db.execSQL("UPDATE " +ChekStart.tblSettings + " SET DeviceName = '"+devname+"';");
//                                db.execSQL("UPDATE " +ChekStart.tblSettings + " SET DeviceAddress = '"+address+"';");
//                                db.close();
//                                postToast("Printer Saved");
//                                ChekStart.printname=devname;
//                                ChekStart.printadd=address;
                                disconnect();

                            } catch (Exception pr) {
                                pr.printStackTrace();
                                postToast("Failed to save printer");
                            }
                        } catch (ConnectionException e) {
                            postToast("Connection failure");
                        }

                        // setStatus("Sending Data", Color.BLUE);
                    } else if (printerStatus.isHeadOpen) {
                        //setStatus("Printer Head Open", Color.RED);
                        postToast("Print Head Open");
                        //   Toast.makeText(getApplicationContext(),"Print Head Open",Toast.LENGTH_LONG).show();
                    } else if (printerStatus.isPaused) {
                        //setStatus("Printer is Paused", Color.RED);
                        postToast("Printer is Paused");
                        // Toast.makeText(getApplicationContext(),"Printer is Paused",Toast.LENGTH_LONG).show();
                    } else if (printerStatus.isPaperOut) {
                        //    setStatus("Printer Media Out", Color.RED);
                        postToast("Paper Out");
                        // Toast.makeText(getApplicationContext(),"Paper Out",Toast.LENGTH_LONG).show();
                    }
                    //  DemoSleeper.sleep(1500);
                    //if (connection instanceof BluetoothConnection) {
                    //  String friendlyName = ((BluetoothConnection) connection).getFriendlyName();
                    //  setStatus(friendlyName, Color.MAGENTA);
                    //DemoSleeper.sleep(500);
                    // }
                } catch (ConnectionException e) {
                    //setStatus(e.getMessage(), Color.RED);
                } finally {
                    disconnect();
                    //     startService(new Intent(getBaseContext(), Watchfi.class));
                    // Toast.makeText(getApplicationContext(),"Interface Service Started",Toast.LENGTH_SHORT).show();
                    //  disconnect();
                    //   finish();
                }
            }
        });
    }

    public synchronized void disconnect() {
        if (mBtSocket != null) {
            try {
                mBtSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //***Method TO ALLOW BACKGROUND THREADS TO POST Message TO UI
    private void postToast(final String text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
            }
        });
    }

    //***Method to find discoverable bluetooth devices (printers)
    public void findPrinters() {

    }

    //***Method that checks for card details
    public void CheckForCard() {

        postToast("Initializing.");
        new Thread(new Runnable() {
            /////initialise card
            public void run() {

                mywormTest = new wormTest(worm);
                //    postToast("here");

                ////SECTION THAT INITIALISES THE CARD
                try {
                    final int ret = worm.init(mContext);
                    if (-1 == ret) {
                        output = "Please reboot your phone!\n";
                        postToast(output);

                    } else if (-2 == ret) {
                        output = "Please insert card!\n";
                        postToast(output);
                    } else {
                        output = "Init Done\n";
                        output += "WormAPI version: " + worm.version() + "\n";
                        wormVersion = String.valueOf(worm.version());
                        //output += mywormTest.isRemovableSDCardAvailable(mContext);
                        String id = "";
                        if (mywormTest.get_Printable_Unique_ID() != "") {
                            output += "---------------------------------\n";
                            output += "Card_Unique_ID: " + mywormTest.get_Printable_Unique_ID() + "\n";
                            //postToast(output);

                            /////////////////////////////////////////////
                            //** below two lines get worm size in blocks
                            /////////////////////////////////////////////

                            currentSize = mywormTest.getCurrentSize();
                            totalCapacity = mywormTest.TotalSize();
                        }

                        if (mywormTest.get_FW_ID() != -1) {
                            output += "---------------------------------\n";
                            output += "FW_ID: " + Integer.toString(mywormTest.get_FW_ID()) + "\n";
                            //postToast(output);
                            firmWareID = String.valueOf(mywormTest.get_FW_ID());
                        }


                        initDone = true;

                        ///////LOGIN TO CARD
                        if (initDone) {
                            output = "";
                            Thread transaction = new Thread(new Runnable() {
                                public void run() {

                                    byte[] pin = {0x31, 0x32, 0x33, 0x34};
                                    //   output +=String.format("Login with: %c %c %c %c \n",
                                    //         pin[0], pin[1], pin[2], pin[3]);
                                    WORM_ERROR retVal = worm.PINLogin(pin, 4);
                                    if (retVal != WORM_ERROR.WORM_ERROR_NOERROR) {
                                        output += "Login Failed!!!! \nPlease Restart Application";
                                        postToast(output);
                                        worm.PINLogoff();
                                        return;
                                    } else {
                                        output += "Login Passed!!!! \n";
                                        //postToast(output);
                                    }

                                }
                            });
                            transaction.start();
                        }

                        ////SECTION THAT READS REG INFO

                        output = "";
                        if (initDone) {
                            //   button2.setEnabled(false);
                            // testSelected.setText("TransactMultiBlock");
                            new Thread(new Runnable() {
                                public void run() {

                                    result = mywormTest.exportCardDetails(list);

                                    if (result != 0) {
                                        postToast("Read Data Error !");
                                        return;
                                    } else {
                                        postToast("Read Data Success !");

                                        int TransactionNumber = 0;

                                        ArrayList<String> toonde = new ArrayList<>();


                                        for (wormTest.WormEntry entry : list) {
                                            String invtext = "";

//                                           //get transaction text
                                            short copy_arr[] = new short[512 * entry.transactionBlocks];
                                            System.arraycopy(entry.getPayload(), 0, copy_arr, 0, entry.getPayload().length);
                                            for (int i = 0; i < copy_arr.length - 1; i++) {
                                                if (copy_arr[i] != 0) {
                                                    invtext += (char) (copy_arr[i]);
                                                }
                                            }
                                            //System.out.println("tran"+invtext);


                                            try {
                                                String currentTag = "";
                                                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                                                factory.setNamespaceAware(true);
                                                XmlPullParser xpp = factory.newPullParser();
                                                // pass input for xml here
                                                xpp.setInput(new StringReader(invtext));
                                                int eventType = xpp.getEventType();
                                                while (eventType != XmlPullParser.END_DOCUMENT) {

                                                    if (eventType == XmlPullParser.START_DOCUMENT) {
                                                        //Log.d(TAG,"Start document");
                                                    } else if (eventType == XmlPullParser.START_TAG) {
                                                        //Log.d(TAG,"Text Start tag "+xpp.getName());
                                                        currentTag = xpp.getName().trim();
                                                    } else if (eventType == XmlPullParser.END_TAG) {
                                                        //Log.d(TAG,"End tag "+xpp.getName());
                                                        //////////////////////////////////////////////////////////////
                                                        //**stop method execution once registration details are located
                                                        //
                                                        //////////////////////////////////////////////////////////////
                                                        if (xpp.getName().toString().trim().equals("REGISTRYINFO")) {
                                                            sendMessage();
                                                            return;
                                                        }
                                                    } else if (eventType == XmlPullParser.TEXT) {
                                                        //Log.d(TAG,"Text "+xpp.getText()); // here you get the text from xml

                                                        //get text and validate based on tag
                                                        String text = xpp.getText();


                                                        //extract registration information if found
                                                        if (currentTag.equals("BPN")) {
//                                                            System.out.println("bpn" + text.toString());
                                                            MainActivity.bpn = text.trim();//
                                                            //postToast(String.valueOf(ItemTotals));
                                                            //  System.out.println("********" + actualnumberofblocks + String.valueOf(ItemTotals));
                                                        }

                                                        if (currentTag.equals("COMPNAME")) {
                                                            //  System.out.println( text.toString());
                                                            MainActivity.compname = text.trim();//
                                                            //postToast(String.valueOf(ItemTotals));
                                                            //  System.out.println("********" + actualnumberofblocks + String.valueOf(ItemTotals));
                                                        }

                                                        if (currentTag.equals("VAT")) {
                                                            //  System.out.println( text.toString());
                                                            MainActivity.vat = text.trim();//
                                                            //postToast(String.valueOf(ItemTotals));
                                                            //  System.out.println("********" + actualnumberofblocks + String.valueOf(ItemTotals));

                                                        }
                                                        if (currentTag.equals("COMPNAME")) {
                                                            //  System.out.println( text.toString());
                                                            MainActivity.compname = text.trim();//
                                                            //postToast(String.valueOf(ItemTotals));
                                                            //  System.out.println("********" + actualnumberofblocks + String.valueOf(ItemTotals));

                                                        }
                                                        if (currentTag.equals("ADDR1")) {
                                                            //  System.out.println( text.toString());
                                                            MainActivity.addr1 = text.trim();//
                                                            //postToast(String.valueOf(ItemTotals));
                                                            //  System.out.println("********" + actualnumberofblocks + String.valueOf(ItemTotals));

                                                        }
                                                        if (currentTag.equals("ADDR2")) {
                                                            //  System.out.println( text.toString());
                                                            MainActivity.addr2 = text.trim();//
                                                            //postToast(String.valueOf(ItemTotals));
                                                            //  System.out.println("********" + actualnumberofblocks + String.valueOf(ItemTotals));

                                                        }
                                                        if (currentTag.equals("ADDR3")) {
                                                            //  System.out.println( text.toString());
                                                            MainActivity.addr3 = text.trim();//
                                                            //postToast(String.valueOf(ItemTotals));
                                                            //  System.out.println("********" + actualnumberofblocks + String.valueOf(ItemTotals));

                                                        }
                                                        if (currentTag.equals("REGN")) {
//                                                            System.out.println("TEXT" + text.toString());
                                                            MainActivity.regn = text.trim();//
                                                            //postToast(String.valueOf(ItemTotals));
                                                            //  System.out.println("********" + actualnumberofblocks + String.valueOf(ItemTotals));

                                                        }
                                                        if (currentTag.equals("SERIAL")) {
                                                            //  System.out.println( text.toString());
                                                            MainActivity.serial = text.trim();//
                                                            //postToast(String.valueOf(ItemTotals));
                                                            //  System.out.println("********" + actualnumberofblocks + String.valueOf(ItemTotals));

                                                        }
                                                        if (currentTag.equals("DATETIME")) {
                                                            //  System.out.println( text.toString());
                                                            MainActivity.datetime = text.trim();//
                                                            //postToast(String.valueOf(ItemTotals));
                                                            // Log.d(TAG,"Text "+xpp.getText()); // here you get the text from xml
                                                            //post changes to screen

                                                        }
                                                    }
                                                    eventType = xpp.next();
                                                }
                                                //Log.d(TAG,"End document");

                                            } catch (XmlPullParserException e) {
                                                //System.out.println("Errorxx "+invtext);
                                                //System.out.println("xmpars Invalid XML error" );
                                                Crashlytics.log("xmpars Invalid XML error " + compname + " " + serial + " " + bpn + " " + vat + " " + currentSize / totalCapacity + " " + regn + " " + datetime + " " + firmWareID + " " + wormVersion);
                                                Crashlytics.logException(e);

                                            } catch (IOException e) {
                                                Crashlytics.log("IO Exception " + compname + " " + serial + " " + bpn + " " + vat + " " + currentSize / totalCapacity + " " + regn + " " + datetime + " " + firmWareID + " " + wormVersion);
                                                Crashlytics.logException(e);
                                                //System.out.println("Errorxx "+invtext);

                                            }
                                            TransactionNumber++;

                                        }
                                    }

                                }
                            }).start();
                        } else {
                            postToast(output);
                        }

                    }
                    //shows that card is not registered
                    throw new RevMaxException("Revmax Registration Check Error");
                } catch (RevMaxException v) {
                    Crashlytics.log("RevMaxException Card Init ");
                    Crashlytics.logException(v);
                } catch (Exception e) {
                    output += e.getMessage();
                }
                //  postToast(output);

            }
        }).start();


    }

    public void sendMessage() throws IOException {
        if (bpn.equals("na")) {
            statz = "Not Registered";
        }

        textstatus.post(new Runnable() {
            public void run() {
                textstatus.setText("STATUS        " + statz);
            }
        });

        textCompanyName.post(new Runnable() {
            public void run() {
                textCompanyName.setText("COMPANY    " + compname);
            }
        });

        textserial.post(new Runnable() {
            public void run() {
                textserial.setText("SERIAL        " + serial);
            }
        });
        textbpn.post(new Runnable() {
            public void run() {
                textbpn.setText("BPN              " + bpn);
            }
        });
        textvat.post(new Runnable() {
            public void run() {
                textvat.setText("VAT              " + vat);
            }
        });
        textaddr1.post(new Runnable() {
            public void run() {
                textaddr1.setText("ADDR           " + addr1);
            }
        });
        textaddr2.post(new Runnable() {
            public void run() {
                textaddr2.setText("                 " + addr2);
            }
        });
        textadd3.post(new Runnable() {
            public void run() {
                textadd3.setText("                  " + addr3);
            }
        });
        textRegNumber.post(new Runnable() {
            public void run() {
                textRegNumber.setText("REG                " + regn);
            }
        });
        textMemoryStatus.post(new Runnable() {
            public void run() {
                textMemoryStatus.setText("MEMORY USED " + utilitiesClass.MemoryUsagePercentage(currentSize, totalCapacity) + "%");
            }
        });

        textdate.post(new Runnable() {
            public void run() {
                textdate.setText("FISCALISED       " + datetime);
            }
        });
        textFirmwareVersion.post(new Runnable() {
            public void run() {
                textFirmwareVersion.setText("FIRMWARE ID  " + RevmaxFirmware);
            }
        });

        if (bpn.equals("na")) {
//            System.out.println("no bpn");
            return;
        } else {//start services
            //ONCE DETAILS ARE RECOVERED, END MAIN ACTIVITY AND START BACKGROUND THREAD
            //check tax configuration here
            int result = utilitiesClass.ReadTaxConfiguration();
            if (result != utilitiesClass.SuccessResult) {
//                //System.out.println("Failed to Read Vat Configuration");
                postToast("Failed to Read Vat Configuration");
                return;
            } else {
//                startService(new Intent(getBaseContext(), Watchfi.class));
                postToast("Interface Poll Service Started");
                //finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        wakeLock.release();
        //worm.exit();
        //Runtime.getRuntime().gc();
        //System.gc();

        Log.e("MainActivity", "onDestroy");
        // System.exit(0);

    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 10: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //CHECKS WHETHER REVMAX IS INSERTED OR NOT INITIATES PROCESSES
                    CheckForCard();
                    //restartActivity(mActivity);

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getApplicationContext(), "Cannot Use Application without Required Permissions", Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        System.out.println("Tapinda tapinda namai nyasha");
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT: {
                if (resultCode == RESULT_OK) {
                    selectDevice();
                } else {
                    finish();
                }
                break;
            }

            case REQUEST_DEVICE: {
                if (resultCode == RESULT_OK) {
                    address = data.getStringExtra(DeviceActivity.EXTRA_ADDRESS);
                    devname = data.getStringExtra(DeviceActivity.EXTRA_NAME);
                    // Toast.makeText(getApplicationContext(),address,Toast.LENGTH_SHORT).show();
                    //crefile();
                    if (devname.contains("FMP-10")) {
                        postToast("Please use zebra printer");
                        return;
                    } else {
                        // Toast.makeText(this.getApplicationContext(), "Zebra", Toast.LENGTH_SHORT).show();
                        int idz = getResources().getIdentifier("com.axis.remaxinterface:drawable/imz", null, null);
//                        prinpik.setImageResource(idz);
//                        ((TextView) findViewById(R.id.textViewdev)).setText(devname);
//                        ((TextView) findViewById(R.id.textviewadd)).setText(address);
                        connectZebra(address);
                    }


                } else {
                    finish();
                }
                break;
            }
        }
    }

    private void invokeHelper(final MethodInvoker invoker) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.msg_please_wait));
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return true;
            }
        });
        dialog.show();
        dialog.setCancelable(false);
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    invoker.invoke();
                } catch (final IOException e) { // Critical exception
                    e.printStackTrace();


                    disconnect();
                    selectDevice();
                } finally {
                    dialog.dismiss();
                }
            }
        });
        t.start();
    }

    public void intentHandler() {
    }

    //CHECK DB EXISTENCE AND CREATE TABLES IF MISSING
    @SuppressLint("WrongConstant")
    public void CheckDatabaseOrCreate() {
        SQLiteDatabase db;
        Cursor cr;

        //OPEN OR CREATE DATABASE
        db = openOrCreateDatabase(dbname, SQLiteDatabase.CREATE_IF_NECESSARY, null);
        db.setVersion(1);
        db.setLocale(Locale.getDefault());


        //add deliverycomment colunmn to tbl deliveries table
//        try{
//            db.execSQL("ALTER TABLE "+ConstantVariables.tbltokens+" ADD DeviceSerial TEXT");
//            //postToast("zvaita");
//        }
//        catch (Exception h){
//
//        }
        try {
            cr = db.query(ConstantVariables.tblInvoices, null, null, null, null, null, null);

        } catch (RuntimeException e) {

            try {
                db.execSQL("CREATE TABLE IF NOT EXISTS " + ConstantVariables.tblInvoices + " (ID String PRIMARY KEY,DeviceBPN TEXT,CODE TEXT,MACNUM TEXT,DECSTARTDATE TEXT,DECENDDATE TEXT,     DETSTARTDATE TEXT,     DETENDDATE TEXT,     CPY TEXT,     IND TEXT,     ITYPE TEXT,     ICODE TEXT,     VAT TEXT,     BRANCH TEXT,     INUM TEXT ,     IBPN TEXT,     INAME TEXT,     IADDRESS TEXT,     ICONTACT TEXT,     ISHORTNAME TEXT,     IPAYER TEXT,     IPVAT TEXT,     IPADDRESS TEXT,     IPTEL TEXT,     IPBPN TEXT,     ICURRENCY TEXT,     IAMT TEXT,     ITAX TEXT,     ISTATUS TEXT,     IISSUER TEXT,     IDATE TEXT,     ITAXCTRL TEXT,     IOCODE TEXT,     IONUM TEXT,     IREMARK TEXT,     ITEMSXML TEXT,     CurrenciesReceivedXML TEXT,syncedToZimra TEXT,syncedToRevMaxPortal TEXT)");
            } catch (RuntimeException el) {
                Toast.makeText(this.getApplicationContext(), "Failed to Create Invoices Table" + el.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
        }
        try {
            cr = db.query(ConstantVariables.tblCurrencies, null, null, null, null, null, null);

        } catch (RuntimeException e) {

            try {
                db.execSQL("CREATE TABLE IF NOT EXISTS " + ConstantVariables.tblCurrencies + " (ID String,Name TEXT,Amount TEXT, Rate TEXT,INUM TEXT,	syncedToZimra TEXT,syncedToRevMaxPortal TEXT)");
                //     Toast.makeText(this.getApplicationContext(), "Users Created", Toast.LENGTH_SHORT).show();
            } catch (RuntimeException el) {
                Toast.makeText(this.getApplicationContext(), "Failed to Create Currencies" + el.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
        }

        try {
            cr = db.query(ConstantVariables.tblItems, null, null, null, null, null, null);

        } catch (RuntimeException e) {

            try {
                db.execSQL("CREATE TABLE IF NOT EXISTS " + ConstantVariables.tblItems + " (ID String,HH TEXT,ITEMCODE TEXT, ITEMNAME1 TEXT,ITEMNAME2 TEXT,QTY TEXT,PRICE TEXT, AMT TEXT, TAX TEXT, TAXR TEXT,INUM TEXT,	syncedToZimra TEXT,	syncedToRevMaxPortal TEXT)");
                //     Toast.makeText(this.getApplicationContext(), "Users Created", Toast.LENGTH_SHORT).show();
            } catch (RuntimeException el) {
                Toast.makeText(this.getApplicationContext(), "Failed to Create Items" + el.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
        }

        try {
            cr = db.query(ConstantVariables.tblVatCalculated, null, null, null, null, null, null);

        } catch (RuntimeException e) {

            try {
                db.execSQL("CREATE TABLE IF NOT EXISTS " + ConstantVariables.tblVatCalculated + " (ID String,Currency TEXT,VatAmount TEXT,INUM TEXT,	syncedToZimra TEXT,	syncedToRevMaxPortal TEXT)");
                //     Toast.makeText(this.getApplicationContext(), "Users Created", Toast.LENGTH_SHORT).show();
            } catch (RuntimeException el) {
                Toast.makeText(this.getApplicationContext(), "Failed to Create VatCalculated" + el.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
        }

        try {
            cr = db.query(ConstantVariables.tblZReports, null, null, null, null, null, null);

        } catch (RuntimeException e) {

            try {
                db.execSQL("CREATE TABLE IF NOT EXISTS " + ConstantVariables.tblZReports + " (UserID Integer,Username TEXT PRIMARY KEY,Password TEXT,UserFirstName TEXT,UserSurname TEXT,JobTitle TEXT,EmailAddress TEXT,UserType TEXT,UserPhoneNumber TEXT,RouteName TEXT,CustomerVisitsTarget TEXT,TargetSales TEXT,TargetOrders TEXT,TargetInvoices TEXT,TargetHitRate TEXT,TargetTotalTimeInField TEXT,Deleted TEXT,CreatedDate TEXT,CreatedBy TEXT,UpdatedDate TEXT,UpdatedBy TEXT,Systat TEXT)");
                //     Toast.makeText(this.getApplicationContext(), "Users Created", Toast.LENGTH_SHORT).show();
            } catch (RuntimeException el) {
                Toast.makeText(this.getApplicationContext(), "Failed to Create ZReportsActivity" + el.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
        }
        try {
            cr = db.query(ConstantVariables.tbltokens, null, null, null, null, null, null);

        } catch (RuntimeException e) {

            try {
                db.execSQL("CREATE TABLE IF NOT EXISTS " + ConstantVariables.tbltokens + " (ID Integer,DeviceSerial TEXT," + ConstantVariables.token + " TEXT PRIMARY KEY)");
                //     Toast.makeText(this.getApplicationContext(), "Users Created", Toast.LENGTH_SHORT).show();
            } catch (RuntimeException el) {
                Toast.makeText(this.getApplicationContext(), "Failed to Create Currencies" + el.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    private interface MethodInvoker {
        public void invoke() throws IOException;
    }
}

//RevMaxException Class to throw exceptions on data anomalies
class RevMaxException extends Exception {
    public RevMaxException(String msg) {
        super(msg);
    }
}