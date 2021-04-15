/*
 * Developed by Mqondisi Ndlovu Copyright (c) 2021. contact @ 0772783880
 */

package com.axis.revmaxinterface;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.crashlytics.android.Crashlytics;
import com.secureflashcard.wormapi.WORM_ERROR;
import com.secureflashcard.wormapi.WormAccess;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import io.fabric.sdk.android.Fabric;

import static android.content.ContentValues.TAG;
import static java.lang.System.arraycopy;

public class DoWork extends Service {

    //bluetooth methods
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_EBANBLE_BTCD = 3;
    private static final int REQUEST_DEVICE = 2;
    private BluetoothAdapter mBtAdapter;
    private BluetoothSocket mBtSocket;
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    StringBuilder recbld = new StringBuilder();
    File f, f2;
    private Context ctx;
    private PendingIntent pedint;
    public ArrayList<wormTest.WormEntry> list;
    String err, currentTag = "", endTag = "";
    private WORM_ERROR error;
    String conf = "Intconfig.xml", confile;
    static String VatRateA = "14.5", VatRateB = "0";
    String path, path1 = "inload.xml", path2 = "1.xml";
    public String XMLToFiscalizeSoap = "";
    private final Handler mHandler = new Handler();
    byte[] CbcMacKey = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F};
    private int NOTIFICATION_ID = 1;
    static String ItemQTYString = "0", ItemtotalsString0rated = "0", ItemtaxesString0rated = "0", InvoicetotalsString = "0", InvoicetaxesString = "0.0", qntity, price, total;
    static Double ItemTaxes0rated, ItemTotals0rated, Usdfinal0Total, final0Total, ItemTotals = 0.0, InvTotals = 0.0, UsdInvTotals = 0.0, ItemTaxes = 0.0, UsdfinalTax = 0.0, UsdfinalTotal = 0.0, finalTax = 0.0, finalTotal = 0.0;
    static String IMEI = "Nil", hashvalue, inumcurr;
    static String RevmaxSerialNumber, Invoicetotals = "0", address, res, totempts = "", GLOBALFILEPATH, inum;
    static Integer NumberOfZOnDevice = 0, NumberofZforPrint = 0;
    static String ZReportText, totlBx, totlB, totlCx, totlAx, totlA, totlC, vat, bpn, prserial, HeaderCo0, HeaderCo1, HeaderCo2, HeaderCo3, HeaderCo4, HeaderCo5;
    static int starttagtrue = -1;
    static String TotalForeign;
    static String VatRate = "0.145", icurrency = "", Invoiceamntstring = "0.0";
    Invoice newInvoice;
    Currencies newCurrency;
    Items newItem;
    static int ItemCounter = 0;
    ArrayList<Invoice> invoicesList;
    ArrayList<Currencies> currenciesList;
    ArrayList<Items> itemsList;

    private interface MethodInvoker {
        public void invoke() throws IOException;
    }

    static Double InvoiceTotals = 0.0;
    BufferedReader re = null;

    public void setString(String a) {
        XMLToFiscalizeSoap = a;
    }

    //rev components
    private boolean initDone = false;
    private boolean isRunning = false;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private static Context mContext;
    wormTest mywormTest;
    private WormAccess worm = new WormAccess();
    String output;
    static String currhash, daten;
    static int resultforZ = 0;
    private static Connection connection;
    private static ZebraPrinter printer;
//    static String currentTag;

    public DoWork() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    //THIS MODULE IS CALLED WHEN THE SERVICE STARTS
    public void onCreate() {
        super.onCreate();

        invoicesList = new ArrayList<Invoice>();
        currenciesList = new ArrayList<Currencies>();
        itemsList = new ArrayList<Items>();

        //CALL METHOD WHEN SERVICE STARTS
        startService();
    }

///////////////////////////////////////////////////////////////////////////////


    public void startService() {
        //initialise Crashlytics and Fabric and get app context
        Fabric.with(this, new Crashlytics());
        mContext = getApplicationContext();
        list = new ArrayList<wormTest.WormEntry>();
        //GET DEVICE IMEI AND MODEL
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                TelephonyManager tm = (TelephonyManager) getSystemService(this.TELEPHONY_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
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
            Crashlytics.setUserIdentifier(IMEI);
        } catch (Exception e) {
            Crashlytics.log("Android Permissions Exception ");
            Crashlytics.logException(e);
        }


        //CHECK AVAILABILITY OF EXTERNAL STORAGE DEVICE
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            //  System.out.println(" if (Environment.MEDIA_MOUNTED.equals(state)) {");
            // Can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
            //GET DEVICE EXTERNAL STORAGE DIRECTORY
            File root = Environment.getExternalStorageDirectory();
            //CHANGE STATUS OF NOTOFICATION BAR TO CHECKING
            err = "checking";
            initNotifi();

            ////////////////////////////////////////////////////
            //**check details and halt app if details are missing
            //**
            ///////////////////////////////////////////////////
            if (MainActivity.bpn.equals("na")) {
                postToast("Please Restart Device ! ");
                //Inform server  of incident
                try {
                    throw new RevMaxException("Device Registry Info Missing");
                } catch (RevMaxException e) {
                    Crashlytics.log("Device Registry Information Missing ");
                    Crashlytics.logException(e);
                }
                stopService(new Intent(getBaseContext(), Soap.class));
                stopService(new Intent(getBaseContext(), DoWork.class));
                return;
            }


            //ITERATE THROUGH ALL FILES THAT HAVE .XML EXTENSION, SKIP ALL OTHERS
            for (String RevmaxFile : root.list()) {
                //CLEAR STRING BUILDER CONTENT
                // System.out.println(RevmaxFile.toString().trim());
                //GET FILE NAME FOR LATER USE
                GLOBALFILEPATH = RevmaxFile.toString().trim();
                //   System.out.println("tapindatapindanamainyasha");
                //    System.out.println(RevmaxFile.toString().trim());
//                   postToast(GLOBALFILEPATH);

                if ((RevmaxFile.endsWith(".xml")) && (!RevmaxFile.contains("Intconfig"))) {
                    //System.out.println(GLOBALFILEPATH);
                    postToast(GLOBALFILEPATH);
                    //get details of card
                    //CHECK FOR VALUE OF BPN NUMBER AND EXIT Service IF IT IS "na"
                    if (MainActivity.bpn.equals("na")) {
//                        System.out.println("http No BPN");
//
//                        //stop service waiting for card details
                        postToast("Please Restart Device ! ");

                        //Inform server  of incident
                        try {
                            throw new RevMaxException("Device Registry Info Missing");
                        } catch (RevMaxException e) {
                            Crashlytics.log("Device Registry Information Missing ");
                            Crashlytics.logException(e);
                        }
                        stopService(new Intent(getBaseContext(), DoWork.class));
                        return;
                    } else {
                        //POPULATE FILE INFORMATION INTO A STRING TO VERIFY FIELDS
                        try {
                            //replace all special characters and write back to file
                            String originalContent = "";

                            BufferedReader reader = new BufferedReader(new FileReader(root + File.separator + RevmaxFile));
                            String line = null;
                            StringBuilder stringBuilder = new StringBuilder();
//

                            try {
                                while ((line = reader.readLine()) != null) {
                                    stringBuilder.append(line);
                                    //stringBuilder.append(ls);
                                }

                            } finally {
                                reader.close();
                            }
                            originalContent = stringBuilder.toString();
                            originalContent = originalContent.replace("&", "&amp;");
                            stringBuilder.setLength(0);


                            InputStream inputStream = new ByteArrayInputStream(originalContent.getBytes(Charset.forName("UTF-8")));

                            re = new BufferedReader(new InputStreamReader(inputStream));

                            //POPULATE FILE AND CARD DETAILS
                            try {
                                //instantiate invoice object
                                newInvoice = new Invoice();
                                ItemCounter = 0;

                                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                                factory.setNamespaceAware(true);
                                XmlPullParser xpp = factory.newPullParser();
                                // pass input for xml here
                                xpp.setInput(re);
                                int eventType = xpp.getEventType();
                                while (eventType != XmlPullParser.END_DOCUMENT) {
                                    if (eventType == XmlPullParser.START_DOCUMENT) {
                                        //Log.d(TAG,"Start document");
                                    } else if (eventType == XmlPullParser.START_TAG) {
                                        starttagtrue = 1;
                                        //get tag name of start tag
                                        //Log.d(TAG,"Start tag "+xpp.getName());
                                        currentTag = xpp.getName().trim();

                                    } else if (eventType == XmlPullParser.END_TAG) {
                                        starttagtrue = 0;
                                        //no code for now
                                    } else if (eventType == XmlPullParser.TEXT && starttagtrue == 1) {

                                        Log.d(TAG, "OG = " + currentTag + " " + xpp.getText()); // here you get the text from xml
                                        //get text and validate based on tag
                                        String text = xpp.getText();


                                        if (currentTag.equals("BPN")) {
                                            if (MainActivity.bpn.equals("na")) {
//                                             System.out.println("http No BPN");//
//                                            //stop service waiting for card details
                                                postToast("Please Restart Device ! ");

                                                //Inform server  of incident
                                                try {
                                                    throw new RevMaxException("Device Registry Info Missing");
                                                } catch (RevMaxException e) {
                                                    Crashlytics.log("Device Registry Information Missing ");
                                                    Crashlytics.logException(e);
                                                }
                                                stopService(new Intent(getBaseContext(), DoWork.class));
                                                return;
                                            } else {
                                                newInvoice.setDeviceBPN(MainActivity.bpn);
                                            }
                                        } else if (currentTag.equals("CODE")) {
                                            if (text.trim().equals("")) {
                                                newInvoice.setCODE("000");
                                            } else {
                                                newInvoice.setCODE(text.trim());
                                            }
                                        }//
                                        else if (currentTag.equals("MACNUM")) {
                                            newInvoice.setMACNUM(MainActivity.serial);
                                        } else if (currentTag.equals("DECSTARTDATE")) {
                                            newInvoice.setDECSTARTDATE(MainActivity.utilitiesClass.GetDateNoTime());
                                        } else if (currentTag.equals("DECENDDATE")) {
                                            newInvoice.setDECENDDATE(MainActivity.utilitiesClass.GetDateNoTime());
                                        } else if (currentTag.equals("DETSTARTDATE")) {
                                            newInvoice.setDETSTARTDATE(MainActivity.utilitiesClass.GetDateTime());
                                        } else if (currentTag.equals("DETENDDATE")) {
                                            newInvoice.setDETENDDATE(MainActivity.utilitiesClass.GetDateTime());
                                        } else if (currentTag.equals("CPY")) {
                                            newInvoice.setCPY("1");
                                        } else if (currentTag.equals("IND")) {
                                            newInvoice.setIND("0");
                                        } else if (currentTag.equals("ITYPE")) {
                                            newInvoice.setITYPE("na");
                                        } else if (currentTag.equals("ICODE")) {
                                            newInvoice.setICODE("na");
                                        } else if (currentTag.equals("BRANCH")) {

                                            if (text.trim().equals("")) {
                                                newInvoice.setBRANCH("");
                                            } else {
                                                newInvoice.setBRANCH(text.trim());
                                            }

                                        } else if (currentTag.equals("INUM")) {

                                            if (text.trim().equals("")) {
                                            } else {
                                                newInvoice.setINUM(text.trim());
                                            }

                                        } else if (currentTag.equals("IBPN")) {
                                            newInvoice.setIBPN(MainActivity.bpn);
                                        } else if (currentTag.equals("INAME")) {
                                            newInvoice.setINAME(MainActivity.compname);

                                        } else if (currentTag.equals("VAT")) {
                                            newInvoice.setVAT(MainActivity.vat);
                                        } else if (currentTag.equals("IADDRESS")) {
                                            newInvoice.setIADDRESS(MainActivity.addr1 + " " + MainActivity.addr2 + " " + MainActivity.addr3);

                                        } else if (currentTag.equals("ICONTACT")) {
                                            if (text.trim().equals("")) {
                                                newInvoice.setICONTACT("na");
                                            } else {
                                                newInvoice.setICONTACT(text.trim());
                                            }

                                        } else if (currentTag.equals("ISHORTNAME")) {
                                            newInvoice.setISHORTNAME("na");
                                        } else if (currentTag.equals("IPAYER")) {

                                            if (text.trim().equals("")) {
                                                newInvoice.setIPAYER("");
                                            } else {
                                                newInvoice.setIPAYER(text.trim());
                                            }

                                        } else if (currentTag.equals("IPVAT")) {

                                            if (text.trim().equals("")) {
                                                newInvoice.setIPVAT("");
                                            } else {
                                                newInvoice.setIPVAT(text.trim());
                                            }

                                        } else if (currentTag.equals("IPADDRESS")) {

                                            if (text.trim().equals("")) {
                                                newInvoice.setIPADDRESS("");
                                            } else {
                                                newInvoice.setIPADDRESS(text.trim());
                                            }


                                        } else if (currentTag.equals("IPTEL")) {
                                            if (text.trim().equals("")) {
                                                newInvoice.setIPTEL("");
                                            } else {
                                                newInvoice.setIPTEL(text.trim());
                                            }
                                        } else if (currentTag.equals("IPBPN")) {

                                            if (text.trim().equals("")) {
                                                newInvoice.setIPBPN("");
                                            } else {
                                                newInvoice.setIPBPN(text.trim());
                                            }

                                        } else if (currentTag.equals("ICURRENCY")) {
                                            if (text.trim().equals("")) {
                                                //throw new RevMaxException("Missing Base Currency");
                                            } else {
                                                newInvoice.setICURRENCY(text.trim());
                                            }
                                        } else if (currentTag.equals("IAMT")) {

                                            //this value is computed at the end of the try statement
                                            newInvoice.setIAMT("0");
                                        } else if (currentTag.equals("ITAX")) {

                                            //  //this value is computed at the end of the try statement
                                            newInvoice.setITAX("0");
                                        } else if (currentTag.equals("ISTATUS")) {

                                            if (text.trim().equals("")) {
                                                //    throw new RevMaxException("Missing Invoice Status");
                                            } else {
                                                newInvoice.setISTATUS(text.trim());
                                            }

                                        } else if (currentTag.equals("IISSUER")) {

                                            if (text.trim().equals("")) {
                                                newInvoice.setIISSUER("na");
                                            } else {
                                                newInvoice.setIISSUER(text.trim());
                                            }

                                        } else if (currentTag.equals("IDATE")) {
                                            newInvoice.setIDATE(MainActivity.utilitiesClass.GetDateTime());
                                        } else if (currentTag.equals("ITAXCTRL")) {

                                            if (text.trim().equals("")) {
                                                newInvoice.setITAXCTRL("na");
                                            } else {
                                                newInvoice.setITAXCTRL(text.trim());
                                            }

                                        } else if (currentTag.equals("IOCODE")) {

                                            if (text.trim().equals("")) {
                                                newInvoice.setIOCODE("");
                                            } else {
                                                newInvoice.setIOCODE(text.trim());
                                            }

                                        } else if (currentTag.equals("IONUM")) {

                                            if (text.trim().equals("")) {
                                                newInvoice.setIONUM("");
                                            } else {
                                                newInvoice.setIONUM(text.trim());
                                            }

                                        } else if (currentTag.equals("IREMARK")) {

                                            if (text.trim().equals("")) {
                                                newInvoice.setIREMARK("");
                                            } else {
                                                newInvoice.setIREMARK(text.trim());
                                            }
                                        }

                                        //populate items on Invoice to itemsList

                                        else if (currentTag.equals("HH")) {
                                            //create new Item object

                                            if (!text.trim().equals("")) {
                                                ItemCounter += 1;
                                                newItem = new Items();
                                                //append HH item line number automatically
                                                newItem.setHH(String.valueOf(ItemCounter));
                                            }

                                        } else if (currentTag.equals("ITEMCODE")) {
                                            if (!text.trim().equals("")) {
                                                newItem.setITEMCODE(text.trim());
                                            }
                                            //else if to rectify tag double reading for nested items
                                            else if (newItem.getITEMCODE().equals("")) {
                                                newItem.setITEMCODE("na");
                                            }
                                        } else if (currentTag.equals("ITEMNAME1")) {
                                            if (!text.trim().equals("")) {
                                                newItem.setITEMNAME1(text.trim());
                                            } else if (newItem.getITEMNAME1().equals("")) {
                                                //  throw new RevMaxException("Missing Item Name 1");
                                            }
                                        } else if (currentTag.equals("ITEMNAME2")) {
                                            if (!text.trim().equals("")) {
                                                newItem.setITEMNAME2(text.trim());
                                            } else {
                                                newItem.setITEMNAME2("na");
                                            }
                                        } else if (currentTag.equals("QTY")) {

                                            if (!text.trim().equals("")) {

                                                int result = MainActivity.utilitiesClass.DoubleValidator(Double.parseDouble(text.trim()));

                                                if (result == MainActivity.utilitiesClass.SuccessResult) {
                                                    newItem.setQTY(text.trim());
                                                } else {
                                                    throw new RevMaxException("Invalid Qty on Item :" + newItem.getITEMNAME1());
                                                }
                                            } else if (newItem.getQTY().equals("")) {
                                                throw new RevMaxException("Missing Item Quantity");
                                            }
                                        } else if (currentTag.equals("PRICE")) {
                                            if (!text.trim().equals("")) {
                                                int result = MainActivity.utilitiesClass.DoubleValidator(Double.parseDouble(text.trim()));
                                                if (result == MainActivity.utilitiesClass.SuccessResult) {
                                                    newItem.setPRICE(text.trim());
                                                } else {
                                                    throw new RevMaxException("Invalid Price on Item :" + newItem.getITEMNAME1());
                                                }
                                            } else if (newItem.getPRICE().equals("")) {
                                                throw new RevMaxException("Missing Item Price");
                                            }
                                        } else if (currentTag.equals("AMT")) {
                                            //Autocalculate item amount here
                                            newItem.setAMT(MainActivity.utilitiesClass.ItemTotalCalculator(newItem.getPRICE(), newItem.getQTY()));
                                        } else if (currentTag.equals("TAX")) {
//                                                  if(!text.trim().equals("")) {
//                                                      int result = MainActivity.utilitiesClass.DoubleValidator(Double.parseDouble(text.trim()));
//                                                      if(result==MainActivity.utilitiesClass.SuccessResult){
//                                                          newItem.setTAX(text.trim());
//                                                      }
//                                                      else{
//                                                          throw new RevMaxException("Invalid Tax on Item :"+newItem.getITEMNAME1());
//                                                      }
//                                                  }
                                            if (newItem.getPRICE().equals("")) {
                                                throw new RevMaxException("Missing Item Tax");
                                            }
                                        } else if (currentTag.equals("TAXR")) {
                                            if (!text.trim().equals("")) {
                                                if (!MainActivity.TaxesList.contains(text.trim())) {
                                                    throw new RevMaxException("Invalid Tax Rate on Item : " + newItem.getITEMNAME1());
                                                } else {

                                                    int result = MainActivity.utilitiesClass.DoubleValidator(Double.parseDouble(text.trim()));
                                                    if (result == MainActivity.utilitiesClass.SuccessResult) {

                                                        newItem.setTAXR(text.trim());
                                                        //auto calculate and set TAX here
                                                        newItem.setTAX(MainActivity.utilitiesClass.TaxCalculator(newItem.getAMT(), "1", MainActivity.TaxConfigFlag, newItem.getTAXR()));
                                                        itemsList.add(newItem);
                                                    } else {
                                                        throw new RevMaxException("Invalid TAXR on Item: " + newItem.getITEMNAME1());
                                                    }

                                                }
                                            } else if (newItem.getTAXR().equals("")) {
                                                throw new RevMaxException("Missing TaxR on Item : " + newItem.getITEMNAME1());
                                            }
                                        }

                                        //populate currencies in XML here
                                        else if (currentTag.equals("Name")) {
                                            if (!text.trim().equals("")) {
                                                newCurrency = new Currencies();
                                                newCurrency.setName(text.trim());
                                            } else if (newCurrency.getName().equals("")) {
                                                throw new RevMaxException("Missing Currency Name");
                                            }
                                        } else if (currentTag.equals("Amount")) {
                                            if (!text.trim().equals("")) {

                                                int result = MainActivity.utilitiesClass.DoubleValidator(Double.parseDouble(text.trim()));
                                                if (result == MainActivity.utilitiesClass.SuccessResult) {
                                                    newCurrency.setAmount(text.trim());
                                                } else {
                                                    throw new RevMaxException("Invalid Currency Amount for Currency : " + newCurrency.getName());
                                                }

                                            } else if (newCurrency.getAmount().equals("")) {
                                                throw new RevMaxException("Missing Currency Amount");
                                            }
                                        } else if (currentTag.equals("Rate")) {
                                            if (!text.trim().equals("")) {
                                                int result = MainActivity.utilitiesClass.DoubleValidator(Double.parseDouble(text.trim()));
                                                if (result == MainActivity.utilitiesClass.SuccessResult) {
                                                    newCurrency.setRate(text.trim());
                                                    currenciesList.add(newCurrency);
                                                } else {
                                                    throw new RevMaxException("Invalid Currency Rate for Currency : " + newCurrency.getName());
                                                }

                                            } else if (newCurrency.getRate().equals("")) {
                                                throw new RevMaxException("Missing Currency Rate");
                                            }
                                        }

                                    }
                                    eventType = xpp.next();
                                }
                                //Log.d(TAG,"End document");

                                //close off invoice after retrieving all info and computing invoice total and tax

                                //support for non multi currency invoices to default to ZWL
                                //generate default currency list
                                if (newInvoice.getICURRENCY().equals("")) {
                                    newInvoice.setICURRENCY("ZWL");
                                    Currencies currencies = new Currencies();
                                    currencies.setName("ZWL");
                                    currencies.setRate("1");
                                    currencies.setAmount(newInvoice.getIAMT());
                                    currenciesList.add(currencies);
                                }

                                newInvoice.setITAX(MainActivity.utilitiesClass.ComputeInvoiceTax(itemsList, MainActivity.TaxConfigFlag));
                                newInvoice.setIAMT(MainActivity.utilitiesClass.ComputeInvoiceTotal(itemsList, newInvoice.getITAX(), MainActivity.TaxConfigFlag));
                                invoicesList.add(newInvoice);


                                //print lists to console
                                MainActivity.utilitiesClass.PrintOutInvoiceList(invoicesList);
                                MainActivity.utilitiesClass.PrintoutItemsList(itemsList);
                                MainActivity.utilitiesClass.PrintoutCurrenciesList(currenciesList);

                            } catch (XmlPullParserException e) {
                                //        System.out.println("xmpars Invalid XML error "+ e.getMessage() );
                                Crashlytics.log("xmpars Invalid XML error " + MainActivity.compname + " " + MainActivity.serial + " " + IMEI);
                                Crashlytics.logException(e);
                                continue;
                            } catch (IOException e) {
                                //   System.out.println("xmpars Invalid XML error "+ e.getMessage() );
                                Crashlytics.log("xmpars IO error " + MainActivity.compname + " " + MainActivity.serial + " " + IMEI);
                                Crashlytics.logException(e);
                                e.printStackTrace();
                                continue;
                            } catch (RevMaxException e) {
                                postToast("RevMax XML error " + e.toString().replace("com.axis.revmaxinterface.RevMaxException", ""));
                                //         System.out.println("RevMax XML error " + e.toString().replace("com.axis.revmaxinterface.RevMaxException","") );
                                Crashlytics.log("Invalid/Missing Data Error" + MainActivity.compname + " " + MainActivity.serial + " " + IMEI);
                                Crashlytics.logException(e);
                                continue;
                            } catch (Exception e) {
                                //    System.out.println("Revmax Error "+ e.getMessage() );
                                Crashlytics.log("Revmax Error " + MainActivity.compname + " " + MainActivity.serial + " " + IMEI);
                                Crashlytics.logException(e);
                                e.printStackTrace();
                                continue;
                            }


                            //generate XML to fiscalise here
                            //if xml fiscalisation is successful then save to DB before trying to send
                            //TODO act on below code
                            String XMLToFiscalize = MainActivity.utilitiesClass.GenerateXML(invoicesList, itemsList, currenciesList);

                            String S1 = MainActivity.utilitiesClass.SoapGenerateXML(invoicesList, itemsList, currenciesList);

                            String S2 = S1.replaceAll("<soap:sub1>", "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\" xmlns:fisc=\"http://ZIMRA/FISC\">");
                            XMLToFiscalizeSoap = S2.replaceAll("</soap:sub1>", "</soapenv:Envelope>");

                            //              System.out.println("RESPONSE:DoWork:"+XMLToFiscalizeSoap)//System.out.println(MainActivity.utilitiesClass.appendFORLog+" "+XMLToFiscalize);

                            AsyncTaskRunner runner = new AsyncTaskRunner();
                            runner.execute();
                            if (!XMLToFiscalize.equals("")) {

                                //fiscalise file here
                                fiscalizer(XMLToFiscalize);
                                break;

                            }
                            //delete file as it is most probalby blank
                            else {

                                File rep = new File(Environment.getExternalStorageDirectory() + File.separator + GLOBALFILEPATH);

                                boolean deleted = rep.delete();
                                //postToast("pano");
                                break;
                            }

                        } catch (FileNotFoundException e) {
                            postToast("File Parse Error-Not Found");
                            //e.printStackTrace();
                            //Inform server  of incident
                            Crashlytics.log("File Not Found " + MainActivity.compname + " " + MainActivity.serial + " " + MainActivity.bpn + " " + MainActivity.vat + " " + MainActivity.currentSize / MainActivity.totalCapacity + " " + MainActivity.regn + " " + MainActivity.datetime + " " + MainActivity.firmWareID + " " + MainActivity.wormVersion);
                            Crashlytics.logException(e);

                        } catch (IOException e) {
                            postToast("File Parse Error");
                            //Inform server  of incident
                            Crashlytics.log("IO Exception " + MainActivity.compname + " " + MainActivity.serial + " " + MainActivity.bpn + " " + MainActivity.vat + " " + MainActivity.currentSize / MainActivity.totalCapacity + " " + MainActivity.regn + " " + MainActivity.datetime + " " + MainActivity.firmWareID + " " + MainActivity.wormVersion);
                            Crashlytics.logException(e);
                            //e.printStackTrace();
                        } finally {
                            try {
                                re.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                //TODO rework this section to modify generateZReport
                else if (RevmaxFile.contains("eod.txt") || MainActivity.triggerZreport == 1) {
                    /// System.out.println(MainActivity.triggerZreport+"doworktrigger");
                    NumberOfZOnDevice = 0;
                    // CheckForCardZReport();
                    //////////////////
                    //delete eod file to allow new eod file generation
                    File rep = new File(Environment.getExternalStorageDirectory() + File.separator + "eod.txt");
                    boolean deleted = rep.delete();
                    MainActivity.triggerZreport += 1;
                    //postToast("pano");

                    generateZReport();

                    // System.out.println(MainActivity.triggerZreport+"doworkaftergenerater");
                } else {
                    //MOVE TO NEXT FILE IN LOCATION

                    continue;
                }

            }
            Intent myIntent = new Intent(DoWork.this, Soap.class);
            pedint = PendingIntent.getService(DoWork.this, 0, myIntent, 0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.SECOND, 2);
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pedint);
            stopService(new Intent(getBaseContext(), DoWork.class));
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // Can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;

            postToast("Media is Read Only");
            //Inform server  of incident
            try {
                throw new RevMaxException("Media is Read Only");
            } catch (RevMaxException e) {
                Crashlytics.log("Media is Read Only " + MainActivity.compname + " " + MainActivity.serial + " " + MainActivity.bpn + " " + MainActivity.vat + " " + MainActivity.currentSize / MainActivity.totalCapacity + " " + MainActivity.regn + " " + MainActivity.datetime + " " + MainActivity.firmWareID + " " + MainActivity.wormVersion);
                Crashlytics.logException(e);
            }
            //  Toast.makeText(Salez.this.getApplicationContext(),"Media is Read Only",Toast.LENGTH_LONG).show();
            return;
        } else {
            // Can't read or write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
            postToast("Media Error,");
            //Inform server  of incident
            try {
                throw new RevMaxException("Failed to Read Card");
            } catch (RevMaxException e) {
                Crashlytics.log("Card Read Error " + MainActivity.compname + " " + MainActivity.serial + " " + MainActivity.bpn + " " + MainActivity.vat + " " + MainActivity.currentSize / MainActivity.totalCapacity + " " + MainActivity.regn + " " + MainActivity.datetime + " " + MainActivity.firmWareID + " " + MainActivity.wormVersion);
                Crashlytics.logException(e);
            }
            //  Toast.makeText(Salez.this.getApplicationContext(),"Media Error,",Toast.LENGTH_LONG).show();
            return;
        }

    }


    private void initNotifi() {

        NotificationManager mnotifi = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        int icon = R.drawable.icon;
        Context cont = getApplicationContext();
        Intent notifiintent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(cont, 0, notifiintent, 0);


        //   Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notifil = new
                NotificationCompat.Builder(this)
                .setContentTitle(MainActivity.serial + " Status")
                .setContentText(err)
                .setTicker("Interface Alert")
                .setAutoCancel(true)
                //     .setSound(uri)
                .setSmallIcon(icon)
                .setContentIntent(contentIntent);
        Notification arrival = notifil.build();
        mnotifi.notify(NOTIFICATION_ID, arrival);

    }

    private void postToast(final String text) {
        try {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (NullPointerException E) {
        }

    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            //     System.out.println("RESPONSE: "+"Do in backgorund");
            String response = "";
            try {
                int a1 = 100;
                int b = 200;
                //String requestXML="<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><Add xmlns=\"http://tempuri.org/\"><intA>"+a1+"</intA><intB>"+b+"</intB></Add> </soap:Body></soap:Envelope>";
                //We are creating and instance for URL class which is pointing to our specified url path
                //URL url = new URL("http://www.dneonline.com/calculator.asmx");
                URL url = new URL("http://41.220.16.133/ZimraIMS_Proxy/ZimraIMS_ProcessFiscalInvoices_SubmitInvoicesRP.asmx");
                //URL url = new URL("http://41");

                //Now we are opening the url connection  using the URLConnection class
                URLConnection connection = url.openConnection();

                //Converting the connection HTTP
                //If you want to convert to HTTPS use HttpsURLConnection
                HttpURLConnection httpConn = (HttpURLConnection) connection;

                //Below are the statements that gives the header details for the url connection
                httpConn.setRequestProperty("Content-Length", "length");
                httpConn.setRequestProperty("Content-Type", "text/xml");
                httpConn.setRequestProperty("SOAPAction", "http://tempuri.org/ZimraIMS_ProcessFiscalInvoices_SubmitInvoicesRP/ZimraSubmitInvoices");
                httpConn.setRequestMethod("POST");

                httpConn.setDoOutput(true);
                httpConn.setDoInput(true);

                //Sending the post body to the http connection
                OutputStreamWriter out = new OutputStreamWriter(httpConn.getOutputStream());
                out.write(XMLToFiscalizeSoap);
                out.close();

                InputStream newIP = httpConn.getInputStream();
                String temp;
                String tempResponse = "";
                String responseXML;

                // Read the response and write it to standard out.
                InputStreamReader isr = new InputStreamReader(newIP);
                BufferedReader br = new BufferedReader(isr);


                // Create a string using response from web services
                while ((temp = br.readLine()) != null) {
                    tempResponse = tempResponse + temp;

                }
                responseXML = tempResponse;
                response = responseXML;
                //this.outputResponse=responseXML;
                br.close();
                isr.close();

            } catch (java.net.MalformedURLException e) {
                //    System.out.println("Error in postRequest(): Secure Service Required");
                e.printStackTrace();

            } catch (Exception e) {
                //   System.out.println("Error in postRequest(): " + e.getMessage());
            }
            return response;
        }

        private String between(String value, String a, String b) {
            // Return a substring between the two strings.
            int posA = value.indexOf(a);
            if (posA == -1) {
                return "";
            }
            int posB = value.lastIndexOf(b);
            if (posB == -1) {
                return "";
            }
            int adjustedPosA = posA + a.length();
            if (adjustedPosA >= posB) {
                return "";
            }
            return value.substring(adjustedPosA, posB);
        }


        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
//            System.out.println("RESPONSE: Actual:" + "PostExecute");

//            System.out.println("RESPONSE:Actual: " + result);


            if (result.contains("<INVNUM>1</INVNUM>")) {
                String text = "";
                text = result.substring(result.indexOf("<INVOICENUMBER>"), result.indexOf("</INVOICENUMBER>"));

                appendLog(text + " " + "Transimitted successfully", "/Invoices");
                postToast("invoice transmitted successfully");


            } else {

                logfailedinvoices(XMLToFiscalizeSoap, "/Invoices", between(XMLToFiscalizeSoap, "<fisc:INUM>", "</fisc:INUM>"));

                //RENAME FILE INTO APP FOLDER AND DELETE FROM PUBIC FOLDER
                try {

                    logfailedinvoices(XMLToFiscalizeSoap, "/Invoices", between(XMLToFiscalizeSoap, "<fisc:INUM>", "</fisc:INUM>"));

                } catch (Exception vc) {

                }
                postToast("invoice sending failed: will attempt to send when network reconnects");
            }
        }


        @Override
        protected void onPreExecute() {
            //  System.out.println("RESPONSE: "+"PreExecute");
        }


        @Override
        protected void onProgressUpdate(String... text) {
            //System.out.println("RESPONSE: "+"Progress Update");

        }
    }

    //MODULE TO WRITE FILE TO REVMAX
    public void fiscalizer(final String XMLToFiscalise) {

        mContext = getApplicationContext();

        new Thread(new Runnable() {
            /////initialise card
            public void run() {
                // a potentially  time consuming task
                //worm = new WormAccess();

                //    postToast("here");
                mywormTest = new wormTest(worm);
                ////SECTION THAT INITIALISES THE CARD
                try {
                    int ret = worm.init(mContext);
                    if (-1 == ret) {
                        output = "Please reboot your phone!\n";
                        postToast(output);

                    } else if (-2 == ret) {
                        output = "Please insert card!\n";
                        postToast(output);
                    } else {
                        output = "Init Done\n";
                        output += "WormAPI version: " + worm.version() + "\n";
                        //output += mywormTest.isRemovableSDCardAvailable(mContext);
                        String id = "";
                        if (mywormTest.get_Printable_Unique_ID() != "") {
                            output += "---------------------------------\n";
                            output += "Card_Unique_ID: " + mywormTest.get_Printable_Unique_ID() + "\n";
                        }

                        if (mywormTest.get_FW_ID() != -1) {
                            output += "---------------------------------\n";
                            output += "FW_ID: " + Integer.toString(mywormTest.get_FW_ID()) + "\n";
                        }

                        //ADD MODULE TO GET CLIENT DETAILS HERE
                        //////////////////////////////////////////
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
                                        output += "Login Failed!!!! \n";
                                        //postToast(output);
                                        //Inform server  of incident
                                        try {
                                            throw new RevMaxException("Failed Login");
                                        } catch (RevMaxException e) {
                                            Crashlytics.log("Login Failure " + MainActivity.compname + " " + MainActivity.serial + " " + MainActivity.bpn + " " + MainActivity.vat + " " + MainActivity.currentSize / MainActivity.totalCapacity + " " + MainActivity.regn + " " + MainActivity.datetime + " " + MainActivity.firmWareID + " " + MainActivity.wormVersion);
                                            Crashlytics.logException(e);
                                        }
                                    } else {
                                        output += "Login Passed!!!! \n";
                                        //postToast(output);
                                    }

                                }
                            });
                            transaction.start();
                        }


                        ////SECTION THAT DOES TRANSACTION
                        output = "";
                        if (initDone) {
                            new Thread(new Runnable() {
                                public void run() {

                                    WORM_ERROR retVal = WORM_ERROR.WORM_ERROR_NOERROR;
                                    final short[] outBuffer = new short[512];
                                    final int[] outBufferSize = {512};
                                    int blk_per_transact = 32;
                                    byte[] inBuffer = new byte[512 * blk_per_transact];
                                    int start_seq_number = mywormTest.getCurrentSize();

                                    int size_of_data = XMLToFiscalise.length();
                                    inBuffer = XMLToFiscalise.getBytes();

                                    //ORIGINAL STATEMENT TO BE USED GOING FORWARD
                                    retVal = worm.DataTransact(inBuffer, inBuffer.length, outBuffer,
                                            outBufferSize, null, 0);

                                    //transaction failure
                                    if (retVal != WORM_ERROR.WORM_ERROR_NOERROR) {
                                        //  output += "---Transaction Failed---" + Integer.toString(it) + "\n";
                                        output += "---Transaction Failed--- ";
                                        postToast(output);
                                        //Inform server  of incident
                                        try {
                                            throw new RevMaxException("Transaction Failure");
                                        } catch (RevMaxException e) {
                                            Crashlytics.log("Transaction Failure Invoice: " + inum + ": " + MainActivity.compname + " " + MainActivity.serial + " " + MainActivity.bpn + " " + MainActivity.vat + " " + MainActivity.currentSize / MainActivity.totalCapacity + " " + MainActivity.regn + " " + MainActivity.datetime + " " + MainActivity.firmWareID + " " + MainActivity.wormVersion);
                                            Crashlytics.logException(e);
                                        }
                                        return;
                                    }

                                    //transaction success
                                    else {
                                        // output += "Transaction Passed---" + Integer.toString(it) + "\n";
                                        output += "Transaction Passed---";
                                        //postToast(output);
                                        try {

                                            //SAVE INVOICE, ITEMS AND CURRENCIES TO DB
                                            //INVOICE LIST WILL ALWAYS ONLY HAVE 1 ITEM
                                            //Delete invoice file after successfully saving invoice
                                            int result = MainActivity.utilitiesClass.SaveInvoiceDetail(invoicesList, itemsList, currenciesList);
//                                            System.out.println("no ifs" + result);
                                            if (result == MainActivity.utilitiesClass.SuccessResult) {
//                                                System.out.println();
                                                File CurrentDirectory = Environment.getExternalStorageDirectory();
                                                File file = new File(CurrentDirectory + File.separator + GLOBALFILEPATH);

                                                File file1 = new File(CurrentDirectory + File.separator + "Revinvoices" + File.separator + GLOBALFILEPATH);
                                                appendLog(file.getName() + " " + "saved successfully", "/Invoices");
                                                boolean deleted = file.delete();
                                                postToast("InvoiceNum: " + inum);
                                            }
                                            //TODO check this section
                                            //Rename file with error extension for non saving
                                            else {
                                            }
                                        } catch (Exception v) {
                                            v.printStackTrace();
                                            postToast(v.toString());
                                        }
                                    }

                                    //code to get current signature
                                    currhash = "";
                                    short copy_arr[] = new short[512];
                                    arraycopy(outBuffer, 0, copy_arr, 0, 512);

                                    for (int i = 0; i < 512; i++) {
                                        if (copy_arr[i] != 0) {

                                            currhash += String.format("%02X", (copy_arr[i]));
                                            //currhash += (char) (copy_arr[i]);
                                        }
                                    }
                                }
                            }).start();
                        } else {
                            postToast(output);
                        }
                    }
                } catch (Exception e) {
                    output += e.getMessage();
                }
            }
        }).start();
    }

    //Module to send USD Zreport
    public boolean usdZreport() {
        // System.out.println("Tonderayi Mondory");
        String logTime = "";
        try {
            StringBuilder ZreportBuilder = new StringBuilder();


            //populate date formats required by z tags
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = df.format(c.getTime());
            SimpleDateFormat dtz = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
            String formattedTimeFile = dtz.format(c.getTime());

            SimpleDateFormat dt = new SimpleDateFormat("HHmm");
            String formattedTime = dt.format(c.getTime());


            // CREATE Z FILE XML TO SEND TO ZIMRA
            //File Revmaxdirectory = Environment.getExternalStorageDirectory();
            File Revmaxdirectory = getApplicationContext().getFilesDir();
            File ZFile = new File(Revmaxdirectory.getAbsolutePath() + File.separator + "Z_" + formattedTimeFile + ".z");

            //   System.out.println(String.valueOf(ZFile));
            FileOutputStream eodf = null;

            eodf = new FileOutputStream(ZFile, true);
            PrintWriter pww = new PrintWriter(eodf);

            ZreportBuilder.append("<ZREPORT>");
            pww.println("<ZREPORT>");
            ZreportBuilder.append("<DATE>" + formattedDate + "</DATE>");
            pww.println("<DATE>" + formattedDate + "</DATE>");
            ZreportBuilder.append("<TIME>" + formattedTime + "</TIME>");
            pww.println("<TIME>" + formattedTime + "</TIME>");
            ZreportBuilder.append("<HEADER>");
            pww.println("<HEADER>");
            ZreportBuilder.append("<LINE>" + MainActivity.compname + "</LINE>");
            pww.println("<LINE>" + MainActivity.compname + "</LINE>");
            ZreportBuilder.append("<LINE>" + MainActivity.addr1 + "</LINE>");
            pww.println("<LINE>" + MainActivity.addr1 + "</LINE>");
            ZreportBuilder.append("<LINE>" + MainActivity.addr2 + "</LINE>");
            pww.println("<LINE>" + MainActivity.addr2 + "</LINE>");
            ZreportBuilder.append("<LINE>" + MainActivity.addr3 + "</LINE>");
            pww.println("<LINE>" + MainActivity.addr3 + "</LINE>");
            ZreportBuilder.append("<LINE>" + HeaderCo4 + "</LINE>");
            pww.println("<LINE>" + HeaderCo4 + "</LINE>");
            ZreportBuilder.append("<LINE>" + HeaderCo5 + "</LINE>");
            pww.println("<LINE>" + HeaderCo5 + "</LINE>");
            ZreportBuilder.append("</HEADER>");
            pww.println("</HEADER>");
            ZreportBuilder.append("<VATNUM>" + MainActivity.vat + "</VATNUM>");
            pww.println("<VATNUM>" + MainActivity.vat + "</VATNUM>");
            ZreportBuilder.append("<BPNUM>" + MainActivity.bpn + "</BPNUM>");
            pww.println("<BPNUM>" + MainActivity.bpn + "</BPNUM>");
            ZreportBuilder.append("<TAXOFFICE>REGION 1</TAXOFFICE>");
            pww.println("<TAXOFFICE>REGION 1</TAXOFFICE>");
            Integer NumberofZforfile;
            NumberofZforfile = NumberOfZOnDevice + 1;
            //System.out.println("***nz"+String.valueOf(NumberofZforfile));
            ZreportBuilder.append("<NumberOfZOnDevice>" + String.valueOf(NumberofZforfile) + "</NumberOfZOnDevice>");
            pww.println("<NumberOfZOnDevice>" + String.valueOf(NumberofZforfile) + "</NumberOfZOnDevice>");
            ZreportBuilder.append("<EFDSERIAL>" + MainActivity.serial + "</EFDSERIAL>");
            pww.println("<EFDSERIAL>" + MainActivity.serial + "</EFDSERIAL>");
            ZreportBuilder.append("<REGISTRATIONDATE>" + MainActivity.datetime + "</REGISTRATIONDATE>");
            pww.println("<REGISTRATIONDATE>" + MainActivity.datetime + "</REGISTRATIONDATE>");
            ZreportBuilder.append("<USER>USER</USER>");
            pww.println("<USER>USER</USER>");

            /////////////////////////ZIMRA AMENDMENT/////////////////////
            ZreportBuilder.append("<CURRENCY> USD </CURRENCY>");
            pww.println("<CURRENCY> USD </CURRENCY>");
            ZreportBuilder.append("<TOTALS>");
            ZreportBuilder.append("<DAILYTOTALAMOUNT>" + UsdInvTotals + "</DAILYTOTALAMOUNT>");
            pww.println("<DAILYTOTALAMOUNT>" + UsdInvTotals + "</DAILYTOTALAMOUNT>");

            ZreportBuilder.append("<GROSS> 0 </GROSS>");
            pww.println("<GROSS> 0 </GROSS>");
            ZreportBuilder.append("<CORRECTIONS> 0 </CORRECTIONS>");
            pww.println("<CORRECTIONS> 0 </CORRECTIONS>");
            ZreportBuilder.append("<DISCOUNTS> 0 </DISCOUNTS>");
            pww.println("<DISCOUNTS> 0 </DISCOUNTS>");
            ZreportBuilder.append("<SURCHARGES> 0 </SURCHARGES>");
            pww.println("<SURCHARGES> 0 </SURCHARGES>");
            ZreportBuilder.append("<TICKETSVOID> 0 </TICKETSVOID>");
            pww.println("<TICKETSVOID> 0 </TICKETSVOID>");
            ZreportBuilder.append("<TICKETSVOIDTOTAL> 0.00 </TICKETSVOIDTOTAL>");
            pww.println("<TICKETSVOIDTOTAL> 0.00 </TICKETSVOIDTOTAL>");
            ZreportBuilder.append("<TICKETSFISCAL> 0 </TICKETSFISCAL>");
            pww.println("<TICKETSFISCAL> 0 </TICKETSFISCAL>");
            ZreportBuilder.append("<TICKETSNONFISCAL> 0 </TICKETSNONFISCAL>");
            pww.println("<TICKETSNONFISCAL> 0 </TICKETSNONFISCAL>");
            ZreportBuilder.append("</TOTALS>");
            pww.println("</TOTALS>");

            /////////////////////////ZIMRA AMENDMENT END/////////////////////

            ZreportBuilder.append("<VATTOTALS>");
            pww.println("<VATTOTALS>");


            //uncomment to see computed totals on android monintor/logcat
            //System.out.println("vattotal"+wormTest.ItemTotals);
            //System.out.println("vattaxable"+wormTest.ItemTaxes);
            //System.out.println("nonvat"+wormTest.ItemTotals0rated);
            // postToast(String.valueOf(wormTest.ItemTotals));

            ZreportBuilder.append("<VATRATE>" + String.valueOf(VatRate) + "</VATRATE>");
            pww.println("<VATRATE>" + String.valueOf(VatRate) + "</VATRATE>");
            ZreportBuilder.append("<NETTAMOUNT>" + UsdfinalTotal + "</NETTAMOUNT>");
            pww.println("<NETTAMOUNT>" + UsdfinalTotal + "</NETTAMOUNT>");
            ZreportBuilder.append("<TAXAMOUNT>" + UsdfinalTax + "</TAXAMOUNT>");
            pww.println("<TAXAMOUNT>" + UsdfinalTax + "</TAXAMOUNT>");

            ZreportBuilder.append("<VATRATE>" + String.valueOf(VatRateB) + "</VATRATE>");
            pww.println("<VATRATE>" + String.valueOf(VatRateB) + "</VATRATE>");
            ZreportBuilder.append("<NETTAMOUNT>" + Usdfinal0Total + "</NETTAMOUNT>");
            pww.println("<NETTAMOUNT>" + Usdfinal0Total + "</NETTAMOUNT>");
            ZreportBuilder.append("<TAXAMOUNT>0</TAXAMOUNT>");
            pww.println("<TAXAMOUNT>0</TAXAMOUNT>");
            pww.println("</VATTOTALS>");
            ZreportBuilder.append(" </ZREPORT>");
            pww.println(" </ZREPORT>");
            pww.flush();
            pww.close();
            eodf.close();

            File root = android.os.Environment.getExternalStorageDirectory();
            File rep = new File(root.getAbsolutePath() + File.separator + "Z_" + formattedTimeFile + ".tp");
            logTime = formattedTimeFile;
            rep.createNewFile();
            InputStream ol = new FileInputStream(ZFile);
            OutputStream ne = new FileOutputStream(rep);
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = ol.read(buf)) > 0) {
                ne.write(buf, 0, len);
            }
            ol.close();
            ne.close();

            //populate Z Text
            ZReportText = ZreportBuilder.toString().trim();
//            System.out.println(ZReportText);

        } catch (FileNotFoundException e) {
            postToast("1 Failed to Generate USD Z Report ");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            postToast("2 Failed to Generate USD Z Report ");
        }
        //save Z Report on Revmax
        //WORM_ERROR retVal = WORM_ERROR.WORM_ERROR_NOERROR;

        final short[] outBuffer = new short[512];
        final int[] outBufferSize = {512};
        int blk_per_transact = 32;
        byte[] inBuffer = new byte[512 * blk_per_transact];


        inBuffer = ZReportText.getBytes();

        WORM_ERROR retVal = worm.DataTransact(inBuffer, inBuffer.length, outBuffer,
                outBufferSize, null, 0);

        if (retVal != WORM_ERROR.WORM_ERROR_NOERROR) {
            //  output += "---Transaction Failed---" + Integer.toString(it) + "\n";
            output += "---Transaction Failed--- ";
            output += "Failed to transact USD Z Report";
            //   postToast(output);
            return false;
        }
        //variable to build and store z report before saving
        else {
            appendLog("Z_" + logTime + ".tp" + "  " + "usd zreport created successfully", "/Zreports");
            postToast("usd zreport saved wait for print");
            printZReport("USD");
            return true;
        }
    }
    //Module to send Zwl Zreport
    public boolean zwlZreport() {
        String logTime = "";
        try {

            StringBuilder ZreportBuilder = new StringBuilder();

            //populate date formats required by z tags
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = df.format(c.getTime());
            SimpleDateFormat dtz = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
            String formattedTimeFile = dtz.format(c.getTime());

            SimpleDateFormat dt = new SimpleDateFormat("HHmm");
            String formattedTime = dt.format(c.getTime());


            // CREATE Z FILE XML TO SEND TO ZIMRA
            //File Revmaxdirectory = Environment.getExternalStorageDirectory();
            File Revmaxdirectory = getApplicationContext().getFilesDir();

            File ZFile = new File(Revmaxdirectory.getAbsolutePath() + File.separator + "Z_" + formattedTimeFile + ".z");

//            System.out.println(ZFile.toString());
            //   System.out.println( TotalForeign);
            FileOutputStream eodf = null;


            eodf = new FileOutputStream(ZFile, true);
            PrintWriter pww = new PrintWriter(eodf);

            ZreportBuilder.append("<ZREPORT>");
            pww.println("<ZREPORT>");
            ZreportBuilder.append("<DATE>" + formattedDate + "</DATE>");
            pww.println("<DATE>" + formattedDate + "</DATE>");
            ZreportBuilder.append("<TIME>" + formattedTime + "</TIME>");
            pww.println("<TIME>" + formattedTime + "</TIME>");
            ZreportBuilder.append("<HEADER>");
            pww.println("<HEADER>");
            ZreportBuilder.append("<LINE>" + MainActivity.compname + "</LINE>");
            pww.println("<LINE>" + MainActivity.compname + "</LINE>");
            ZreportBuilder.append("<LINE>" + MainActivity.addr1 + "</LINE>");
            pww.println("<LINE>" + MainActivity.addr1 + "</LINE>");
            ZreportBuilder.append("<LINE>" + MainActivity.addr2 + "</LINE>");
            pww.println("<LINE>" + MainActivity.addr2 + "</LINE>");
            ZreportBuilder.append("<LINE>" + MainActivity.addr3 + "</LINE>");
            pww.println("<LINE>" + MainActivity.addr3 + "</LINE>");
            ZreportBuilder.append("<LINE>" + HeaderCo4 + "</LINE>");
            pww.println("<LINE>" + HeaderCo4 + "</LINE>");
            ZreportBuilder.append("<LINE>" + HeaderCo5 + "</LINE>");
            pww.println("<LINE>" + HeaderCo5 + "</LINE>");
            ZreportBuilder.append("</HEADER>");
            pww.println("</HEADER>");
            ZreportBuilder.append("<VATNUM>" + MainActivity.vat + "</VATNUM>");
            pww.println("<VATNUM>" + MainActivity.vat + "</VATNUM>");
            ZreportBuilder.append("<BPNUM>" + MainActivity.bpn + "</BPNUM>");
            pww.println("<BPNUM>" + MainActivity.bpn + "</BPNUM>");
            ZreportBuilder.append("<TAXOFFICE>REGION 1</TAXOFFICE>");
            pww.println("<TAXOFFICE>REGION 1</TAXOFFICE>");
            Integer NumberofZforfile;
            NumberofZforfile = NumberOfZOnDevice + 1;
            //System.out.println("***nz"+String.valueOf(NumberofZforfile));
            ZreportBuilder.append("<NumberOfZOnDevice>" + String.valueOf(NumberofZforfile) + "</NumberOfZOnDevice>");
            pww.println("<NumberOfZOnDevice>" + String.valueOf(NumberofZforfile) + "</NumberOfZOnDevice>");
            ZreportBuilder.append("<EFDSERIAL>" + MainActivity.serial + "</EFDSERIAL>");
            pww.println("<EFDSERIAL>" + MainActivity.serial + "</EFDSERIAL>");
            ZreportBuilder.append("<REGISTRATIONDATE>" + MainActivity.datetime + "</REGISTRATIONDATE>");
            pww.println("<REGISTRATIONDATE>" + MainActivity.datetime + "</REGISTRATIONDATE>");
            ZreportBuilder.append("<USER>USER</USER>");
            pww.println("<USER>USER</USER>");

            /////////////////////////ZIMRA AMENDMENT/////////////////////
            ZreportBuilder.append("<CURRENCY> ZWL </CURRENCY>");
            pww.println("<CURRENCY> ZWL </CURRENCY>");
            ZreportBuilder.append("<TOTALS>");
            pww.println("<TOTALS>");
            ZreportBuilder.append("<DAILYTOTALAMOUNT>" + InvTotals + "</DAILYTOTALAMOUNT>");
            pww.println("<DAILYTOTALAMOUNT>" + InvTotals + "</DAILYTOTALAMOUNT>");

            ZreportBuilder.append("<GROSS> 0 </GROSS>");
            pww.println("<GROSS> 0 </GROSS>");
            ZreportBuilder.append("<CORRECTIONS> 0 </CORRECTIONS>");
            pww.println("<CORRECTIONS> 0 </CORRECTIONS>");
            ZreportBuilder.append("<DISCOUNTS> 0 </DISCOUNTS>");
            pww.println("<DISCOUNTS> 0 </DISCOUNTS>");
            ZreportBuilder.append("<SURCHARGES> 0 </SURCHARGES>");
            pww.println("<SURCHARGES> 0 </SURCHARGES>");
            ZreportBuilder.append("<TICKETSVOID> 0 </TICKETSVOID>");
            pww.println("<TICKETSVOID> 0 </TICKETSVOID>");
            ZreportBuilder.append("<TICKETSVOIDTOTAL> 0.00 </TICKETSVOIDTOTAL>");
            pww.println("<TICKETSVOIDTOTAL> 0.00 </TICKETSVOIDTOTAL>");
            ZreportBuilder.append("<TICKETSFISCAL> 0 </TICKETSFISCAL>");
            pww.println("<TICKETSFISCAL> 0 </TICKETSFISCAL>");
            ZreportBuilder.append("<TICKETSNONFISCAL> 0 </TICKETSNONFISCAL>");
            pww.println("<TICKETSNONFISCAL> 0 </TICKETSNONFISCAL>");
            ZreportBuilder.append("</TOTALS>");
            pww.println("</TOTALS>");

            /////////////////////////ZIMRA AMENDMENT END/////////////////////

            ZreportBuilder.append("<VATTOTALS>");
            pww.println("<VATTOTALS>");

            //uncomment to see computed totals on android monintor/logcat
            //System.out.println("vattotal"+wormTest.ItemTotals);
            //System.out.println("vattaxable"+wormTest.ItemTaxes);
            //System.out.println("nonvat"+wormTest.ItemTotals0rated);
            // postToast(String.valueOf(wormTest.ItemTotals));

            //0.145 tax rated
            ZreportBuilder.append("<VATRATE>" + String.valueOf(VatRate) + "</VATRATE>");
            pww.println("<VATRATE>" + String.valueOf(VatRate) + "</VATRATE>");
            ZreportBuilder.append("<NETTAMOUNT>" + finalTotal + "</NETTAMOUNT>");
            pww.println("<NETTAMOUNT>" + finalTotal + "</NETTAMOUNT>");
            ZreportBuilder.append("<TAXAMOUNT>" + finalTax + "</TAXAMOUNT>");
            pww.println("<TAXAMOUNT>" + finalTax + "</TAXAMOUNT>");
//0 tax rated
            ZreportBuilder.append("<VATRATE>" + String.valueOf(VatRateB) + "</VATRATE>");
            pww.println("<VATRATE>" + String.valueOf(VatRateB) + "</VATRATE>");
            ZreportBuilder.append("<NETTAMOUNT>" + final0Total + "</NETTAMOUNT>");
            pww.println("<NETTAMOUNT>" + final0Total + "</NETTAMOUNT>");
            ZreportBuilder.append("<TAXAMOUNT>0</TAXAMOUNT>");
            pww.println("<TAXAMOUNT>0</TAXAMOUNT>");

            pww.println("</VATTOTALS>");
            ZreportBuilder.append(" </ZREPORT>");
            pww.println(" </ZREPORT>");
            pww.flush();
            pww.close();
            eodf.close();


            File root = android.os.Environment.getExternalStorageDirectory();
            File rep = new File(root.getAbsolutePath() + File.separator + "Z_" + formattedTimeFile + ".tp");
            logTime = formattedTimeFile;
            rep.createNewFile();
            InputStream ol = new FileInputStream(ZFile);
            OutputStream ne = new FileOutputStream(rep);
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = ol.read(buf)) > 0) {
                ne.write(buf, 0, len);
            }
            ol.close();
            ne.close();

            //populate Z Text
            //   System.out.println(ZreportBuilder.toString().trim());
            ZReportText = ZreportBuilder.toString().trim();
//            System.out.println(ZReportText.toString());
        } catch (FileNotFoundException e) {
            postToast("1 Failed to Generate ZWL Z Report ");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            postToast("2 Failed to Generate ZWL Z Report ");
        }

//save Z Report on Revmax
//WORM_ERROR retVal = WORM_ERROR.WORM_ERROR_NOERROR;

        final short[] outBuffer = new short[512];
        final int[] outBufferSize = {512};
        int blk_per_transact = 32;
        byte[] inBuffer = new byte[512 * blk_per_transact];

        inBuffer = ZReportText.getBytes();

        WORM_ERROR retVal = worm.DataTransact(inBuffer, inBuffer.length, outBuffer,
                outBufferSize, null, 0);
//System.out.println(retVal.toString());
        if (retVal != WORM_ERROR.WORM_ERROR_NOERROR) {
            //  output += "---Transaction Failed---" + Integer.toString(it) + "\n";
            output += "---Transaction Failed--- ";
            output += "Failed to transact ZWL Z Report";
            postToast(output);
            return false;

        } else {
            appendLog("Z_" + logTime + ".tp" + "  " + "zwl zreport created successfully", "/Zreports");
            postToast("zwl zreport saved wait for print");
            printZReport("ZWL");
            return true;
        }
    }

    //MODULE generate Z reports
    public void generateZReport() {

//        Fabric.with(this, new Crashlytics());
        mywormTest = new wormTest(worm);
        //

        new Thread(new Runnable() {
            /////initialise card
            public void run() {
                // a potentially  time consuming task
                ////SECTION THAT INITIALISES THE CARD
                try {
                    final int ret = worm.init(mContext);
                    if (-1 == ret) {
                        output = "Please reboot your phone!\n";
                        //  postToast(output);
                    } else if (-2 == ret) {
                        output = "Please insert card!\n";
                        // postToast(output);
                    } else {
                        output = "Init Done\n";
                        output += "WormAPI version: " + worm.version() + "\n";
                        String id = "";

                        if (mywormTest.get_Printable_Unique_ID() != "") {
                            output += "---------------------------------\n";
                            output += "Card_Unique_ID: " + mywormTest.get_Printable_Unique_ID() + "\n";

                            RevmaxSerialNumber = mywormTest.get_Printable_Unique_ID().trim();
                            RevmaxSerialNumber = RevmaxSerialNumber.replace("SWISSBIT", "").trim();

                            //     postToast(RevmaxSerialNumber);
                        }

                        if (mywormTest.get_FW_ID() != -1) {
                            //  System.out.println(String.valueOf(mywormTest.get_FW_ID())+"taaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
                            output += "---------------------------------\n";
                            output += "FW_ID: " + Integer.toString(mywormTest.get_FW_ID()) + "\n";
                        }
                        initDone = true;
                        //  ADD MODULE TO GET CLIENT DETAILS HERE

                        ////////////////////////////////////////

//                          initDone = true;
//                           postToast(output);

                        ///////LOGIN TO CARD
                        if (initDone) {
                            output = "";
                            //   button2.setEnabled(false);
                            // testSelected.setText("PinLogin");
                            Thread transaction = new Thread(new Runnable() {
                                public void run() {

                                    byte[] pin = {0x31, 0x32, 0x33, 0x34};
                                    //   output +=String.format("Login with: %c %c %c %c \n",
                                    //         pin[0], pin[1], pin[2], pin[3]);
                                    WORM_ERROR retVal = worm.PINLogin(pin, 4);
                                    if (retVal != WORM_ERROR.WORM_ERROR_NOERROR) {
                                        //       System.out.println(String.valueOf(retVal)+".....................................................");
                                        output += "Login Failed!!!! \n";
                                        //       postToast(output);
                                    } else {
                                        output += "Login Passed!!!! \n";
                                        try {
                                            //get all worm transactions
                                            list = mywormTest.exportWormStores(list);
//                                            System.out.println(list.size() + "list size list size list size");
                                            if (list.size() != 0) {

                                                ItemTaxes = 0.0;
                                                ItemTaxes0rated = 0.0;
                                                ItemTotals = 0.0;
                                                finalTax = 0.0;
                                                finalTotal = 0.0;
                                                UsdfinalTax = 0.0;
                                                UsdfinalTotal = 0.0;
                                                ItemTotals0rated = 0.0;
                                                InvoicetaxesString = "0.0";
                                                ItemtaxesString0rated = "0.0";
                                                InvoicetotalsString = "0.0";
                                                Invoiceamntstring = "0.0";
                                                ItemtotalsString0rated = "0.0";
                                                ItemQTYString = "0.0";
                                                TotalForeign = "0.0";
                                                //Section that gets number of Z reports done
                                                /////////////////////////////////////////////

                                                int TransactionNumber = 0;
                                                NumberOfZOnDevice = 0;

                                                for (wormTest.WormEntry entry : list) {
                                                    String TransactionDetail = "";

//                                                  //get transaction text from hex to alphabet
                                                    short copy_arr[] = new short[512 * entry.transactionBlocks];
                                                    arraycopy(entry.getPayload(), 0, copy_arr, 0, entry.getPayload().length);
                                                    for (int i = 0; i < copy_arr.length - 1; i++) {
                                                        if (copy_arr[i] != 0) {
                                                            TransactionDetail += (char) (copy_arr[i]);
                                                            if (TransactionDetail.startsWith("2")) {
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    //    System.out.println("Transaction PFEEE");

                                                    if (TransactionDetail.contains("<ZREPORT>")) {
                                                        //increment Z count if transaction is a Zreport
                                                        NumberOfZOnDevice += 1;
                                                        //System.out.println(DoWork.NumberOfZOnDevice +"***ztrans");
                                                        //System.out.println(String.valueOf(entry.transactionBlocks)+" : "+"TRANSSIG"+TransactionNumber+"***"+TransactionDetail);
                                                    }

                                                    // System.out.println(String.valueOf(entry.transactionBlocks)+" : "+"TRANSSIG"+TransactionNumber+"***"+TransactionDetail);
                                                    TransactionNumber++;
                                                }
                                                //   System.out.println(String.valueOf(String.valueOf(NumberOfZOnDevice) + "***2"));

                                                ////////////////////////////////////////////
                                                ///////////////////////////////////////////


                                                //section that totals invoices after last Z report amounts based on tax rate done

                                                TransactionNumber = 0;
                                                int checkZnumber = 0;

                                                for (wormTest.WormEntry entry : list) {

                                                    try {
                                                        String TransactionDetail = "";

//                                                  //get transaction text
                                                        short copy_arr[] = new short[512 * entry.transactionBlocks];
                                                        arraycopy(entry.getPayload(), 0, copy_arr, 0, entry.getPayload().length);
                                                        for (int i = 0; i < copy_arr.length - 1; i++) {
                                                            if (copy_arr[i] != 0) {
                                                                TransactionDetail += (char) (copy_arr[i]);
                                                                if (TransactionDetail.startsWith("2")) {
                                                                    //              System.out.println("break");
                                                                    break;
                                                                }
                                                                //                     System.out.println("TransactionDetail");
                                                            }
                                                        }


                                                        if (TransactionDetail.contains("<ZREPORT>")) {
                                                            //increment Z count if transaction is a Zreport
                                                            checkZnumber += 1;
                                                            //    System.out.println(DoWork.NumberOfZOnDevice +"***ztrans");

                                                        }


                                                        //only populate if invoice is after last invoice
                                                        if (checkZnumber == NumberOfZOnDevice) {

                                                            String TransactionDetailTags[] = TransactionDetail.split("/");
                                                            String taxrate = "";
                                                            //    System.out.println(TransactionDetailTags.length +"***TransactionDetailTags.length");
                                                            for (int g = 0; g < TransactionDetailTags.length - 1; g++) {
//                                                                System.out.println(TransactionDetailTags[g]);
                                                                //if(inv[g].contains("<>"))
                                                                //System.out.println(inv[g]);
                                                                if (TransactionDetailTags[g].contains("<PRICE>")) {
                                                                    //  System.out.println( inv[g].toString());
                                                                    InvoicetotalsString = TransactionDetailTags[g].substring(TransactionDetailTags[g].lastIndexOf(">") + 1, TransactionDetailTags[g].lastIndexOf("<")).trim();
                                                                    //   System.out.println("muku"+InvoicetotalsString);
                                                                }
                                                                if (TransactionDetailTags[g].contains("<AMT>")) {
                                                                    //   System.out.println( inv[g].toString());
                                                                    Invoiceamntstring = TransactionDetailTags[g].substring(TransactionDetailTags[g].lastIndexOf(">") + 1, TransactionDetailTags[g].lastIndexOf("<")).trim();
                                                                    //    System.out.println("kendrik"+InvoicetaxesString);
                                                                }
                                                                if (TransactionDetailTags[g].contains("<QTY>")) {
                                                                    //  System.out.println( inv[g].toString());
                                                                    ItemQTYString = TransactionDetailTags[g].substring(TransactionDetailTags[g].lastIndexOf(">") + 1, TransactionDetailTags[g].lastIndexOf("<")).trim();//
                                                                    //       System.out.println("kuda"+ItemQTYString);

                                                                }

                                                                if (TransactionDetailTags[g].contains("<INUM>")) {
                                                                    String inum = TransactionDetailTags[g].substring(TransactionDetailTags[g].lastIndexOf(">") + 1, TransactionDetailTags[g].lastIndexOf("<")).trim();
                                                                    //    System.out.println("wyne"+inum);
                                                                    inumcurr = inum;

                                                                }

                                                                //get item tax amount
                                                                if (TransactionDetailTags[g].contains("<TAX>")) {
                                                                    //   System.out.println( inv[g].toString());
                                                                    InvoicetaxesString = TransactionDetailTags[g].substring(TransactionDetailTags[g].lastIndexOf(">") + 1, TransactionDetailTags[g].lastIndexOf("<")).trim();
                                                                    //    System.out.println("kendrik"+InvoicetaxesString);
                                                                }

                                                                //get tax rate for current product
                                                                if (TransactionDetailTags[g].contains("<TAXR>")) {
                                                                    //  System.out.println( inv[g].toString());
                                                                    taxrate = TransactionDetailTags[g].substring(TransactionDetailTags[g].lastIndexOf(">") + 1, TransactionDetailTags[g].lastIndexOf("<")).trim();//
                                                                    //      System.out.println("taxrate"+taxrate);
                                                                }

                                                                //get invoice currency

                                                                if (TransactionDetailTags[g].contains("<ICURRENCY>")) {
                                                                    //  System.out.println( inv[g].toString());
                                                                    icurrency = TransactionDetailTags[g].substring(TransactionDetailTags[g].lastIndexOf(">") + 1, TransactionDetailTags[g].lastIndexOf("<")).trim();//
//                                                                    System.out.println("taxrate" + icurrency);
                                                                }


                                                                //get invoice total amount
//                                                                System.out.println(TransactionDetailTags[g]);
//                                                                System.out.println( "ropafaddzhgjbklf;kj nmd,./m d,f.s/,ld;f");

                                                                if (TransactionDetailTags[g].contains("<IAMT>")) {
                                                                    if (icurrency.equals("ZWL")) {
                                                                        InvTotals += Double.parseDouble(TransactionDetailTags[g].substring(TransactionDetailTags[g].lastIndexOf(">") + 1, TransactionDetailTags[g].lastIndexOf("<")).trim());
                                                                    } else {
                                                                        UsdInvTotals += Double.parseDouble(TransactionDetailTags[g].substring(TransactionDetailTags[g].lastIndexOf(">") + 1, TransactionDetailTags[g].lastIndexOf("<")).trim());
                                                                    }
                                                                }
////                                                                    if (currencies.getName().equals("ZWL")){
//                                                                    finalTotal += Double.parseDouble(currencies.getAmount());
//                                                                    }
//                                                                    else if(currencies.getName().equals("USD")){
//                                                                        UsdfinalTotal += Double.parseDouble(currencies.getAmount());
//                                                                    }


                                                                //get invoice tax amount
//                                                                if (TransactionDetailTags[g].contains("<ITAX>")) {
//                                                                    //  System.out.println( inv[g].toString());
//                                                                    if (icurrency.equals("ZWL")){
//                                                                        finalTax += Double.parseDouble(TransactionDetailTags[g].substring(TransactionDetailTags[g].lastIndexOf(">") + 1, TransactionDetailTags[g].lastIndexOf("<")).trim());
//                                                                        //      System.out.println("taxrate"+taxrate);
//                                                                    }else {
//                                                                        UsdfinalTax += Double.parseDouble(TransactionDetailTags[g].substring(TransactionDetailTags[g].lastIndexOf(">") + 1, TransactionDetailTags[g].lastIndexOf("<")).trim());
//                                                                    }
//
//                                                                }
//                                                                if (TransactionDetailTags[g].contains("<IAMT>")) {
//
//                                                                    TotalForeign = TransactionDetailTags[g].substring(TransactionDetailTags[g].lastIndexOf(">") + 1, TransactionDetailTags[g].lastIndexOf("<")).trim();
//                                                                   // System.out.println("TotalForeign"+TotalForeign);
//                                                                }
                                                                //populate totals based on tax rate
                                                                if (TransactionDetailTags[g].toString().trim().startsWith("TAXR>")) {
                                                                    //System.out.println( inv[g].toString());

                                                                    if (taxrate.equals(VatRateB)) {
                                                                        //increment net
//                                                                        VatRate=taxrate;
                                                                        ItemTotals0rated += ((Double.parseDouble(InvoicetotalsString) * Double.parseDouble(ItemQTYString)) * 100);
                                                                        //  System.out.println(InvoicetotalsString);
                                                                        // ItemTotals=ItemTotals*100;
                                                                        BigDecimal pr = new BigDecimal(ItemTotals0rated);
                                                                        pr = pr.setScale(2, BigDecimal.ROUND_HALF_UP);
                                                                        NumberFormat n = NumberFormat.getInstance();
                                                                        double pry = pr.doubleValue();
                                                                        InvoicetotalsString = n.format(pry);
                                                                        InvoicetotalsString = InvoicetotalsString.replace(",", "");
                                                                        //     System.out.println("InvoicetotalsString"+InvoicetotalsString);
                                                                        InvoicetotalsString = InvoicetotalsString.trim();


                                                                        ItemTotals0rated = Double.parseDouble(InvoicetotalsString);
                                                                        //System.out.println(inv[g].toString());
//                                                                        System.out.println("**** 0 rated" + ItemTotals0rated);
//                                                                        finalTax=   ItemTaxes0rated;
//                                                                      finalTotal=ItemTaxes0rated;

                                                                        if (icurrency.equals("ZWL")) {
                                                                            final0Total = ItemTotals0rated;
                                                                        } else if (icurrency.equals("USD")) {
                                                                            Usdfinal0Total = ItemTotals0rated;
                                                                        }
                                                                    }


                                                                    if (taxrate.equals(String.valueOf(VatRateA)) || taxrate.equals(String.valueOf(VatRate))) {
                                                                        //increment tax
//                                                                        VatRate= taxrate;
                                                                        if (Invoiceamntstring.contains("-")) {
                                                                            Double signedtax = 0.0;
                                                                            signedtax = Double.parseDouble(InvoicetaxesString);
                                                                            signedtax = 0 - signedtax;
                                                                            ItemTaxes += (signedtax * 100);

                                                                        } else {
                                                                            ItemTaxes += (Double.parseDouble(InvoicetaxesString) * 100);
                                                                        }


                                                                        //ItemTaxes=ItemTaxes*100;
                                                                        BigDecimal prt = new BigDecimal(ItemTaxes);
                                                                        prt = prt.setScale(2, BigDecimal.ROUND_HALF_UP);
                                                                        NumberFormat nt = NumberFormat.getInstance();
                                                                        double pryt = prt.doubleValue();
                                                                        InvoicetaxesString = nt.format(pryt);
                                                                        InvoicetaxesString = InvoicetaxesString.replace(",", "");
                                                                        //     System.out.println("InvoicetaxesString"+InvoicetaxesString);
                                                                        InvoicetaxesString = InvoicetaxesString.trim();
                                                                        //   ItemTaxes = Double.parseDouble(InvoicetaxesString);
                                                                        //System.out.println(inv[g].toString());
                                                                        //System.out.println("****" + ItemTaxes);


                                                                        //increment net
                                                                        if (Invoiceamntstring.contains("-")) {
                                                                            Double signedtotal = 0.0;
                                                                            signedtotal = Double.parseDouble(InvoicetotalsString);
                                                                            signedtotal = 0 - signedtotal;
                                                                            ItemTotals = ((signedtotal * Double.parseDouble(ItemQTYString)) * 100);

                                                                        } else {
                                                                            ItemTotals += ((Double.parseDouble(InvoicetotalsString) * Double.parseDouble(ItemQTYString)) * 100);
                                                                        }


                                                                        //  System.out.println(InvoicetotalsString);
                                                                        // ItemTotals=ItemTotals*100;
                                                                        BigDecimal pr = new BigDecimal(ItemTotals);
                                                                        pr = pr.setScale(2, BigDecimal.ROUND_HALF_UP);
                                                                        NumberFormat n = NumberFormat.getInstance();
                                                                        double pry = pr.doubleValue();
                                                                        InvoicetotalsString = n.format(pry);
                                                                        InvoicetotalsString = InvoicetotalsString.replace(",", "");
                                                                        //   System.out.println("InvoicetotalsString"+InvoicetotalsString);
                                                                        InvoicetotalsString = InvoicetotalsString.trim();
//                                                                        ItemTotals = Double.parseDouble(InvoicetotalsString);
                                                                        //System.out.println(inv[g].toString());
                                                                        //  System.out.println("****" + ItemTotals);

//                                                                        finalTax= ItemTaxes  ;
//                                                                       finalTotal=ItemTotals;
                                                                        if (icurrency.equals("ZWL")) {
                                                                            finalTax = ItemTaxes;
                                                                            finalTotal = ItemTotals;
//                                                                            System.out.println(finalTax.toString());
//                                                                            System.out.println("****" + finalTotal.toString());
                                                                        } else {
                                                                            UsdfinalTax = ItemTaxes;
                                                                            UsdfinalTotal = ItemTotals;
//                                                                            System.out.println(UsdfinalTax.toString());
//                                                                            System.out.println("****" + UsdfinalTotal.toString());
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        //System.out.println(String.valueOf(entry.transactionBlocks)+" : "+"TRANSSIG"+TransactionNumber+"***"+TransactionDetail);
                                                        TransactionNumber++;
                                                    } catch (Exception g) {

                                                        continue;
                                                    }

                                                }
                                                //////////////////////////////////////
                                                //Generate Zreport file here

                                                HeaderCo4 = "na";
                                                HeaderCo5 = "na";
                                                boolean zwlPrinted = false;
                                                boolean zwl = zwlZreport();
                                                ////  System.out.println("String.valueOf(usd)");
                                                //print zwl zreport
                                                if (zwl) {
                                                    printZReport("Zwl");
                                                    zwlPrinted = true;
                                                }
                                                boolean usd = usdZreport();
                                                //Print usd zreports

                                                if (usd && zwlPrinted) {
                                                    printZReport("USD");
                                                }

                                                if (!zwl) {

                                                    postToast("Failed to create ZWL ZReport");
                                                }
                                                if (!usd) {
                                                    postToast("Failed to create USD ZReport");
                                                } else if (zwl && usd) {
                                                    postToast("Z Report Generated Successfully");

                                                    NumberofZforPrint = NumberOfZOnDevice;
                                                    NumberOfZOnDevice = 0;
                                                }
                                            }
                                            //printZReport();
                                        } catch (Exception g) {

//                                            postToast(g.toString()+"z Failed");
//                                            System.out.println(String.valueOf(g) + "z Failed");
                                            return;

                                        }
                                    }
                                }
                            });
                            transaction.start();
                            try {
                                Thread.sleep(10L * 1000L);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            transaction.stop();
                        }
                    }
                } catch (Exception e) {
                    output += e.getMessage();
                    //  postToast(e.toString());
                }
            }
        }).start();


    }

    // Print Zreport
    public void printZReport(String currency) {
        String ZFilePath;


        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;


            File root = android.os.Environment.getExternalStorageDirectory();

            //iterate through each file and print
            for (String zFileToPrint : root.list()) {

                //CLEAR STRING BUILDER CONTENT
                ZFilePath = zFileToPrint.toString().trim();

                if (ZFilePath.endsWith(".tp")) {
//                    System.out.println();
//try connecting to printer
                    try {
                        connection = null;
                        connection = new BluetoothConnection(MainActivity.address);
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
                                }

                                //POPULATE FILE INFORMATION INTO A STRING TO VERIFY FIELDS
                                try {

                                    FileInputStream inputreader = new FileInputStream(root + File.separator + ZFilePath);
                                    // DataInputStream in = new DataInputStream(path);

                                    re = new BufferedReader(new InputStreamReader(inputreader));

                                    //do here
                                    try {
                                        //print header
                                        connection.write(("^XA^CFD^POI^LL30^FO100,0^ADN,30,10^FD** ZIMRA START OF LEGAL RECEIPT **^FS^XZ").getBytes());
                                        connection.write(("^XA^CFD^POI^LL30^FO30,0^ADN,30,10^FD  ^FS^XZ").getBytes());
                                        connection.write(("^XA^CFD^POI^LL30^FO30,0^ADN,30,10^FD  ^FS^XZ").getBytes());
                                        connection.write("^XA^LL200^XZ".getBytes());
                                        String storagePath = Environment.getExternalStorageDirectory().toString();

                                        Resources res = mContext.getResources();
                                        int id = R.drawable.icon;
                                        Bitmap logoBMP = BitmapFactory.decodeResource(res, id);
                                        // Bitmap logoBMP = BitmapFactory.decodeFile(storagePath +File.separator+"revmaxlogo.png");

                                        //     ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
                                        ZebraImageAndroid zebraImageToPrint = new ZebraImageAndroid(logoBMP);
                                        printer.printImage(zebraImageToPrint, 30, 0, zebraImageToPrint.getWidth(), zebraImageToPrint.getHeight(), false);
                                        //printer.printImage(zebraImageToPrint, 30, 0,200, 200, false);
                                        connection.write(("^XA^CFD^POI^LL30^FO30,0^ADN,30,10^FD  ^FS^XZ").getBytes());

                                        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                                        factory.setNamespaceAware(true);
                                        XmlPullParser xpp = factory.newPullParser();

                                        xpp.setInput(re); // pass input whatever xml you have
                                        int eventType = xpp.getEventType();
                                        while (eventType != XmlPullParser.END_DOCUMENT) {
                                            if (eventType == XmlPullParser.START_DOCUMENT) {
                                                //Log.d(TAG,"Start document");
                                            } else if (eventType == XmlPullParser.START_TAG) {
                                                //Log.d(TAG,"Start tag "+xpp.getName());

                                                currentTag = xpp.getName().toString();
                                                if ((!xpp.getName().toString().equals("DATE")) && (!xpp.getName().toString().equals("TIME")) && (!xpp.getName().toString().equals("HEADER")) && (!xpp.getName().toString().equals("LINE")) && (!xpp.getName().toString().equals("USER")) && (!xpp.getName().toString().equals("VATTOTALS"))) {

                                                    //connection.write(("^XA^CFD^POI^LL30^FO30,0^ADN,30,10^FD"+xpp.getName().toString()+"^FS^XZ").getBytes());
                                                }
                                            } else if (eventType == XmlPullParser.END_TAG) {
                                                //Log.d(TAG,"End tag "+xpp.getName());

                                            } else if (eventType == XmlPullParser.TEXT) {
                                                // Log.d(TAG,"Text "+xpp.getText()); // here you get the text from xml

                                                //get text and validate based on tag
                                                String text = xpp.getText();

                                                if (currentTag.equals("DATE")) {
                                                    daten = text;
                                                }
                                                if (currentTag.equals("TIME")) {
                                                    connection.write(("^XA^CFD^POI^LL30^FO50,0^ADN,30,10^FDDATE: " + daten + " " + text + "^FS^XZ").getBytes());
                                                    connection.write(("^XA^CFD^POI^LL30^FO30,0^ADN,30,10^FD  ^FS^XZ").getBytes());
                                                }

                                                if (currentTag.equals("LINE")) {
                                                    connection.write(("^XA^CFD^POI^LL30^FO50,0^ADN,30,10^FD" + text + "^FS^XZ").getBytes());
                                                }
                                                if (currentTag.equals("BPNUM")) {
                                                    connection.write(("^XA^CFD^POI^LL30^FO50,0^ADN,30,10^FDBPN:  " + text + "^FS^XZ").getBytes());
                                                }
                                                if (currentTag.equals("VATNUM")) {
                                                    connection.write(("^XA^CFD^POI^LL30^FO50,0^ADN,30,10^FDVAT:  " + text + "^FS^XZ").getBytes());

                                                }


                                                //  if(currentTag.equals("TAXOFFICE")){
                                                //    connection.write(("^XA^CFD^POI^LL30^FO30,0^ADN,30,10^FD-------------------------------------------------------^FS^XZ").getBytes());
                                                //  connection.write(("^XA^CFD^POI^LL30^FO50,0^ADN,30,10^FDTAX OFFICE:   "+text+"^FS^XZ").getBytes());

                                                //}
                                                if (currentTag.equals("NumberOfZOnDevice")) {
                                                    connection.write(("^XA^CFD^POI^LL30^FO50,0^ADN,30,10^FDZNumber:  " + text + "^FS^XZ").getBytes());

                                                }
                                                if (currentTag.equals("EFDSERIAL")) {
                                                    connection.write(("^XA^CFD^POI^LL30^FO50,0^ADN,30,10^FDSerial:   " + text + "^FS^XZ").getBytes());

                                                }
                                                if (currentTag.equals("REGISTRATIONDATE")) {
                                                    connection.write(("^XA^CFD^POI^LL30^FO50,0^ADN,30,10^FDREG DATE: " + text + "^FS^XZ").getBytes());
                                                    connection.write(("^XA^CFD^POI^LL30^FO30,0^ADN,30,10^FD--------------------------------------------------------^FS^XZ").getBytes());
                                                    connection.write(("^XA^CFD^POI^LL30^FO30,0^ADN,30,10^FD ^FS^XZ").getBytes());
                                                }


                                                if (currentTag.equals("VATRATE")) {

                                                    postToast("POAST TOAST USD");

                                                    connection.write(("^XA^CFD^POI^LL30^FO50,0^ADN,30,10^FDVAT RATE:     " + text + "^FS^XZ").getBytes());

                                                }

                                                if (currentTag.equals("NETTAMOUNT")) {
                                                    if (!currency.equals(null)) {
                                                        text = currency + ":" + text;
                                                    }
                                                    connection.write(("^XA^CFD^POI^LL30^FO50,0^ADN,30,10^FDNET AMOUNT:   " + text + "^FS^XZ").getBytes());

                                                }
                                                if (currentTag.equals("TAXAMOUNT")) {
                                                    connection.write(("^XA^CFD^POI^LL30^FO50,0^ADN,30,10^FDTAX AMOUNT:   " + text + "^FS^XZ").getBytes());
                                                    connection.write(("^XA^CFD^POI^LL30^FO30,0^ADN,30,10^FD  ^FS^XZ").getBytes());
                                                }


                                                currentTag = "";


                                            }
                                            eventType = xpp.next();
                                        }
                                        //Log.d(TAG,"End document");

                                        connection.write(("^XA^CFD^POI^LL30^FO30,0^ADN,30,10^FD  ^FS^XZ").getBytes());
                                        connection.write(("^XA^CFD^POI^LL30^FO30,0^ADN,30,10^FD  ^FS^XZ").getBytes());
                                        connection.write(("^XA^CFD^POI^LL30^FO30,0^ADN,30,10^FD  ^FS^XZ").getBytes());
                                        connection.write(("^XA^CFD^POI^LL30^FO30,0^ADN,30,10^FD  ^FS^XZ").getBytes());
                                        connection.write(("^XA^CFD^POI^LL30^FO30,0^ADN,30,10^FD  ^FS^XZ").getBytes());

                                    } catch (XmlPullParserException e) {
//                                        System.out.println("xmpars Invalid XML error");

                                    } catch (IOException e) {

                                        e.printStackTrace();
                                    }

                                } catch (Exception e) {
                                    postToast(e.toString());
                                    //e.printStackTrace();
                                    //Inform server  of incident

                                }
//                                catch (IOException e) {
//                                   postToast("File Parse Error");
//                                    //Inform server  of incident
//                                    //e.printStackTrace();
//                                } finally {
//                                    try {
//                                        re.close();
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
//                                }


//                                printer.printImage(new ZebraImageAndroid(logoz), 0, 0, logoz.getWidth(), logoz.getHeight(), false);
//                                //  connection.write("^XA^LL50^MNV^FO5,0^AfN,30,10^FD ^FS^XZ".getBytes());
//                                connection.write("^XA^LL100^MNV^FO5,0^AfN,30,10^FD ^FS^XZ".getBytes());
//                                connection.write("^XA^LL30^MNV^FO5,0^AfN,30,10^FDCustomer  :______________^FS^XZ".getBytes());
//                                connection.write("^XA^LL500^XZ".getBytes());
//                                //  printer.printImage(new ZebraImageAndroid(SmsGetter.bmp), 0, 0, SmsGetter.bmp.getWidth(), SmsGetter.bmp.getHeight(), false);
//                                connection.write("^XA^LL60^MNV^FO5,0^A0N,30,30^FDMERCHANT COPY^FS^XZ".getBytes());
//                                // connection.write("^XA^LL30^MNV^FO5,0^A0N,30,30^FDCustomer Signature: ^FS^XZ".getBytes());
//                                connection.write("^XA^LL100^MNV^FO5,0^AfN,30,10^FD ^FS^XZ".getBytes());
//
//
//                                connection.write("^XA^LL500^XZ".getBytes());
//                                // printer.printImage(new ZebraImageAndroid(SmsGetter.bmp), 0, 0, SmsGetter.bmp.getWidth(), SmsGetter.bmp.getHeight(), false);
//                                connection.write("^XA^LL60^MNV^FO5,0^A0N,30,30^FDCUSTOMER COPY^FS^XZ".getBytes());
//                                connection.write("^XA^LL100^MNV^FO5,0^AfN,30,10^FD ^FS^XZ".getBytes());
                                //  connection.write("^XA^LL200^XZ".getBytes());
                                //printer.printImage(new ZebraImageAndroid(SmsGetter.bmp), 0, 0, SmsGetter.bmp.getWidth(), SmsGetter.bmp.getHeight(), false);
                                // connection.write("^XA^LL30^MNV^FO5,0^A0N,30,30^FDCustomer Signature: ^FS^XZ".getBytes());


                                //   connection.write(prish);

                            } catch (ConnectionException e) {

                                Toast.makeText(mContext, "Connection Failure", Toast.LENGTH_LONG).show();

                            }


                            // setStatus("Sending Data", Color.BLUE);
                        } else if (printerStatus.isHeadOpen) {
                            //setStatus("Printer Head Open", Color.RED);
                            Toast.makeText(mContext, "Print Head Open", Toast.LENGTH_LONG).show();
                        } else if (printerStatus.isPaused) {
                            //setStatus("Printer is Paused", Color.RED);
                            Toast.makeText(mContext, "Printer is Paused", Toast.LENGTH_LONG).show();
                        } else if (printerStatus.isPaperOut) {
                            //    setStatus("Printer Media Out", Color.RED);
                            Toast.makeText(mContext, "Paper Out", Toast.LENGTH_LONG).show();
                        }
                        //  DemoSleeper.sleep(1500);
                        //if (connection instanceof BluetoothConnection) {
                        //  String friendlyName = ((BluetoothConnection) connection).getFriendlyName();
                        //  setStatus(friendlyName, Color.MAGENTA);
                        //DemoSleeper.sleep(500);
                        // }
                    } catch (ConnectionException e) {
                        //setStatus(e.getMessage(), Color.RED);
                    } catch (NullPointerException n) {
                        postToast("Printer Not Found, Please Switch on Printer and Try Again");

                        return;
                    } finally {
                        disconnect();

                        //   startService(new Intent(getBaseContext(), Watchfi.class));
                        // Toast.makeText(getApplicationContext(),"Interface Service Started",Toast.LENGTH_SHORT).show();
                        //  disconnect();
                        //   finish();
                    }

                    File file = new File(root.getAbsolutePath() + File.separator + ZFilePath);
                    boolean deleted = file.delete();
                }
            }
            //postToast("");

        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // Can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;

            postToast("Media is Read Only");
            //  Toast.makeText(Salez.this.getApplicationContext(),"Media is Read Only",Toast.LENGTH_LONG).show();
            return;
        } else {
            // Can't read or write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
            postToast("Media Error,");
            //  Toast.makeText(Salez.this.getApplicationContext(),"Media Error,",Toast.LENGTH_LONG).show();
            return;
        }


    }

    //writes failed invoice file to device
    public static void logfailedinvoices(String text, String logpath, String filename) {
        Log.e("appendLog", "appendLog call");

        File root = android.os.Environment.getExternalStorageDirectory();
//        File zdirectory = new File(root.getAbsolutePath()+"/Zreports");
//        if(!zdirectory.exists()) {
//            zdirectory.mkdir();
//        }
        File log = new File(root.getAbsolutePath()
                + logpath + "/Log");

        if (!log.exists()) {
            log.mkdirs();
        }

        File logFile = new File(root.getAbsolutePath()
                + logpath + "/Log/" + filename + ".fld");

        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                Log.e("appendLog", e.getMessage());
                e.printStackTrace();
            }
        }

        try {
            logFile.setWritable(true);
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
            logFile.setReadOnly();
        } catch (IOException e) {
            Log.e("appendLog", e.getMessage());
            e.printStackTrace();
        }
    }

    //public log zreports and invoices
    public static void appendLog(String text, String logpath) {
        Log.e("appendLog", "appendLog call");

        File root = android.os.Environment.getExternalStorageDirectory();
//        File zdirectory = new File(root.getAbsolutePath()+"/Zreports");
//        if(!zdirectory.exists()) {
//            zdirectory.mkdir();
//        }
        File log = new File(root.getAbsolutePath()
                + logpath + "/Log");

        if (!log.exists()) {
            log.mkdirs();
        }

        File logFile = new File(root.getAbsolutePath()
                + logpath + "/Log/Logs_file.txt");

        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                Log.e("appendLog", e.getMessage());
                e.printStackTrace();
            }
        }

        try {
            logFile.setWritable(true);
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
            logFile.setReadOnly();
        } catch (IOException e) {
            Log.e("appendLog", e.getMessage());
            e.printStackTrace();
        }
    }


    public static void disconnect() {
        try {
            //      setStatus("Disconnecting", Color.RED);
            if (connection != null) {
                connection.close();
            }
            //    setStatus("Not Connected", Color.RED);
        } catch (ConnectionException e) {
            //  setStatus("COMM Error! Disconnected", Color.RED);
        }
    }

//    //commented out for future use
//    public void printToZebra(){
//
//
//            mBtAdapter = BluetoothAdapter.getDefaultAdapter();
//            if (mBtAdapter != null) {
//                if (!mBtAdapter.isEnabled()) {ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd
//                    mBtAdapter.enable();
//                }
//            } else {
//                // Toast.makeText(this, R.string.msg_bluetooth_is_not_supported, Toast.LENGTH_SHORT).show();
//                postToast("bluetooth_is_not_supported");
//                return;
//            }
//
//
//
//            invokeHelper(new MethodInvoker() {
//                @Override
//                public void invoke() throws IOException {
//
//                    postToast("PRINTING.... ");
//                    try {
//                        connection = null;
//                        connection = new BluetoothConnection(address);
//                        connection.open();
//
//                        if (connection.isConnected()) {
//                            try {
//
//                                printer = ZebraPrinterFactory.getInstance(connection);
//                                //setStatus("Determining Printer Language", Color.YELLOW);
//                                String pl = SGD.GET("device.languages", connection);
//                                //setStatus("Printer Language " + pl, Color.BLUE);
//                            } catch (ConnectionException e) {
//                                //setStatus("Unknown Printer Language", Color.RED);
//                                printer = null;
//                                //  DemoSleeper.sleep(1000);
//                                disconnect();
//                            } catch (ZebraPrinterLanguageUnknownException e) {
//                                // setStatus("Unknown Printer Language", Color.RED);
//                                printer = null;
//                                //DemoSleeper.sleep(1000);
//                                disconnect();
//                            }
//
//
//
//
//                        }
//                    } catch (ConnectionException v) {
//                        v.printStackTrace();
//                        System.out.println("Connerror"+v.toString());
//                        postToast("Connection to Device has Failed");
//                        //stopService(new Intent(getBaseContext(), ZebraPrint.class));
//                        return;
//                    }
//
//
//
//                    try {
//                        ZebraPrinterLinkOs linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(printer);
//
//
//                        PrinterStatus printerStatus = (linkOsPrinter != null) ? linkOsPrinter.getCurrentStatus() : printer.getCurrentStatus();
//
//
//
//
//
//                        if (printerStatus.isReadyToPrint) {
//
//                            byte prish[]=null;
//                            String connmsg;
//
//                            try {
//                                PrinterLanguage printerLanguage = printer.getPrinterControlLanguage();
//                                SGD.SET("device.languages", "zpl", connection);
//
//                                if (printerLanguage == PrinterLanguage.ZPL) {
//
//
//                                    Calendar c = Calendar.getInstance();
//                                    SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss ");
//                                    String formattedDate = df.format(c.getTime());
//
//
////                                connmsg= "^XA^LL350^FO5,200^A0I,30,30^FDDEVICE CONNECTION SUCCESS^FS" +
////                                        "^FO5,30^A0I,30,30^FD ^FS" +
////                                        "^FO5,50^A0I,30,30^FD ^FS " +
////                                        "^XZ";
////
////                                prish= connmsg.getBytes();
//
//                                    String invnum,cd1,cd2,cd3,product;
//                                    String name,surname,Route,Date,add,add1,add2;
//                                    DecimalFormat df2 = new DecimalFormat(".##");
//
//
//
//
//                                    cd1 = "^XA^LL30^MNV^FO5,0^A0N,30,30^FD";
//                                    cd3= "^XA^LL30^MNV^FO5,0^A0N,30,50^FD";
//                                    cd2 = "^FS^XZ";
//
//
//
//                                    //connection.write("^XA^LL200^XZ".getBytes());
//
////                                    String storagePath =  getApplicationContext().getFilesDir().toString();
////                                    Bitmap CustomerSignature = BitmapFactory.decodeFile(storagePath + "/C"+ViewishActivity.invnum+".png");
////                                    //     ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
////                                    ZebraImageAndroid zebraImageToPrint = new ZebraImageAndroid(CustomerSignature);
////                                    printer.printImage(zebraImageToPrint, 0, 0, zebraImageToPrint.getWidth(), zebraImageToPrint.getHeight(), false);
////                                    connection.write("^XA^LL30^MNV^FO5,0^A0N,30,30^FDCustomer Signature^FS^XZ".getBytes());
////                                    // connection.write("^XA^LL30^MNV^FO5,0^AfN,30,10^FD ^FS^XZ".getBytes());
////
////                                    connection.write("^XA^LL200^XZ".getBytes());
////                                    Bitmap DriverSignature = BitmapFactory.decodeFile(storagePath + "/D"+ViewishActivity.invnum+".png");
////                                    //     ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
////                                    ZebraImageAndroid zebraImage2ToPrint = new ZebraImageAndroid(DriverSignature);
////                                    printer.printImage(zebraImage2ToPrint, 0, 0, zebraImage2ToPrint.getWidth(), zebraImage2ToPrint.getHeight(), false);
////                                    connection.write("^XA^LL30^MNV^FO5,0^A0N,30,30^FDDriver Signature'^FS^XZ".getBytes());
////                                    connection.write("^XA^LL30^MNV^FO5,0^AfN,30,10^FD ^FS^XZ".getBytes());
//
//
//                                    // connection.write((cd1+"ITEM #                       "+quantity+cd2).getBytes());
//
//
//
//
//                                //    connection.write((cd1+"CHANGE                         $"+TenderActivity.change+cd2).getBytes());
//                                  //  connection.write((cd1+"TENDER                       $"+TenderActivity.totamtPaid+cd2).getBytes());
//                                    connection.write((cd1+"---------------------------------------"+cd2).getBytes());
//                                    connection.write((cd1+"           "+MainActivity.serial+cd2).getBytes());
//                                    connection.write((cd1+"---------------------------------------"+cd2).getBytes());
//                                    connection.write((cd1+currhash.substring(32,currhash.length())+cd2).getBytes());
//                                    connection.write((cd1+currhash.substring(0,32)+cd2).getBytes());
//                                    connection.write((cd1+"---------------------------------------"+cd2).getBytes());
//                                    connection.write((cd1+"INVOICE TOTAL                       $"+FileContentString.substring(FileContentString.indexOf("<IAMT>")+7,FileContentString.indexOf("</IAMT>"))+cd2).getBytes());
//                                    connection.write((cd1+"---------------------------------------"+cd2).getBytes());
//                                    connection.write((cd1+"Tax (incl)  15.00%                $"+FileContentString.substring(FileContentString.indexOf("<ITAX>")+7,FileContentString.indexOf("</ITAX>"))+cd2).getBytes());
//                                 //   connection.write((cd1+"Line Items                          "+NewInvoice.itemcount+cd2).getBytes());
//                                    connection.write((cd1+"  "+cd2).getBytes());
//                                    connection.write((cd1+"======================================="+cd2).getBytes());
//
//                                    String itemsstring =FileContentString;
//
//                                    for(int x = 0; x < itemsstring.length(); x ++){
//
//
//                                        if(itemsstring.contains("</ITEM>")){
//
//                                            product = itemsstring.substring(itemsstring.lastIndexOf("<ITEMNAME1>")+11,itemsstring.lastIndexOf("</ITEMNAME1>"));
//                                            qntity = itemsstring.substring(itemsstring.lastIndexOf("<QTY>")+5,itemsstring.lastIndexOf("</QTY>"));
//                                            price = itemsstring.substring(itemsstring.lastIndexOf("<PRICE>")+7,itemsstring.lastIndexOf("</PRICE>"));
//                                            total = itemsstring.substring(itemsstring.lastIndexOf("<AMOUNT>")+8,itemsstring.lastIndexOf("</AMOUNT>"));
//
//
//                                            connection.write((cd1+qntity+"  @  "+price+" = $  "+total+cd2).getBytes());
//                                            connection.write((cd1+product+cd2).getBytes());
//
//                                            itemsstring=itemsstring.replace(itemsstring.substring(itemsstring.lastIndexOf("<ITEM>"),itemsstring.lastIndexOf("</ITEM>")+7),"");                                        }
//
//
//                                    }
//
//                                    connection.write((cd1+"  "+cd2).getBytes());
//                                    connection.write((cd3+"             ITEMS"+cd2).getBytes());
//                                    connection.write((cd1+"======================================="+cd2).getBytes());
//
//
//
//
//
//
////                                    if (!ViewishActivity.custaddr3.equals("")){
////                                        connection.write((cd1+ViewishActivity.custaddr3+cd2).getBytes());
////                                    }
////
////                                    if (!ViewishActivity.custaddr2.equals("")){
////                                        connection.write((cd1+ViewishActivity.custaddr2+cd2).getBytes());
////                                    }
////
////                                    if (!ViewishActivity.custaddr.equals("")){
////                                        connection.write((cd1+ViewishActivity.custaddr+cd2).getBytes());
////                                    }
////                                    if(!ViewishActivity.custnum.equals("")){
////                                        connection.write((cd1+ViewishActivity.custnum+cd2).getBytes());
////                                    }
////                                    if (!ViewishActivity.custvat.equals("")){
////                                        connection.write((cd1+ViewishActivity.custvat+cd2).getBytes());
////                                    }
////                                    if (!ViewishActivity.custbpn.equals("")){
////                                        connection.write((cd1+ViewishActivity.custbpn+cd2).getBytes());
////                                    }
////
////                                    connection.write((cd1+OrderStock.seleccust+cd2).getBytes());
//
//
//                                    connection.write((cd1+"---------------------------------------"+cd2).getBytes());
//                                    connection.write((cd1+formattedDate+cd2).getBytes());
//                                    connection.write((cd1+"REG#: "+MainActivity.regn+cd2).getBytes());
//                                    connection.write((cd1+"VAT#: "+MainActivity.vat+cd2).getBytes());
//                                    connection.write((cd1+"BPN : "+MainActivity.bpn+cd2).getBytes());
//                                    connection.write((cd1+MainActivity.addr3+cd2).getBytes());
//                                    connection.write((cd1+MainActivity.addr2+cd2).getBytes());
//                                    connection.write((cd1+MainActivity.addr1+cd2).getBytes());
//                                    connection.write((cd1+FileContentString.substring(FileContentString.indexOf("<IISUER>")+8,FileContentString.indexOf("</IISUER>"))+cd2).getBytes());
//                                    connection.write(("^XA^LL30^MNV^FO5,0^A0N,30,50^FD"+FileContentString.substring(FileContentString.indexOf("<INUM>")+6,FileContentString.indexOf("</INUM>"))+"^FS^XZ").getBytes());
//                                    connection.write("^XA^LL30^MNV^FO5,0^AfN,30,10^FD ^FS^XZ".getBytes());
//                                    connection.write("^XA^LL30^MNV^FO5,0^A0N,30,50^FD    ***** TAX INVOICE ***** ^FS^XZ".getBytes());
//                                    connection.write("^XA^LL30^MNV^FO5,0^AfN,30,10^FD ^FS^XZ".getBytes());
//                                    connection.write("^XA^LL30^MNV^FO5,0^AfN,30,10^FD=============================^FS^XZ".getBytes());
//
////currhash="";
//
////                    connection.write(prish);
//
//                                }
//
//
//
//                                //connection.write(prish);
//
//
//
//
//                            } catch (ConnectionException e) {
//                                postToast("Connection failure");
//
//                            }
//
//
//                        } else if (printerStatus.isHeadOpen) {
//                            //setStatus("Printer Head Open", Color.RED);
//                            postToast("Print Head Open");
//                            disconnect();
//
//                           // stopService(new Intent(getBaseContext(), ZebraPrint.class));
//
//                            return;
//                            //   Toast.makeText(getApplicationContext(),"Print Head Open",Toast.LENGTH_LONG).show();
//                        } else if (printerStatus.isPaused) {
//                            //setStatus("Printer is Paused", Color.RED);
//                            postToast("Printer is Paused");
//                            disconnect();
//
//                          //  stopService(new Intent(getBaseContext(), ZebraPrint.class));
//
//                            return;
//                            // Toast.makeText(getApplicationContext(),"Printer is Paused",Toast.LENGTH_LONG).show();
//                        } else if (printerStatus.isPaperOut) {
//                            //    setStatus("Printer Media Out", Color.RED);
//                            postToast("Paper Out");
//                            disconnect();
//
//                          //  stopService(new Intent(getBaseContext(), ZebraPrint.class));
//
//                            return;
//                            // Toast.makeText(getApplicationContext(),"Paper Out",Toast.LENGTH_LONG).show();
//                        }
//                        //  DemoSleeper.sleep(1500);
//                        //if (connection instanceof BluetoothConnection) {
//                        //  String friendlyName = ((BluetoothConnection) connection).getFriendlyName();
//                        //  setStatus(friendlyName, Color.MAGENTA);
//                        //DemoSleeper.sleep(500);
//                        // }
//                    } catch (ConnectionException e) {
//                        postToast("Print Failure");
//                        disconnect();
//
//                        //stopService(new Intent(getBaseContext(), ZebraPrint.class));
//
//                        return;
//                    } finally {
//                        disconnect();
//                      //  stopService(new Intent(getBaseContext(), ZebraPrint.class));
//                        //     startService(new Intent(getBaseContext(), Watchfi.class));
//                        // Toast.makeText(getApplicationContext(),"Interface Service Started",Toast.LENGTH_SHORT).show();
//                        //  disconnect();
//                        //   finish();
//                    }
//
//
//
//                }
//            });
//
//
//
//
//    }


    //commented out for future use
//    public void printZReport(){
//
//
//        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (mBtAdapter != null) {
//            if (!mBtAdapter.isEnabled()) {
//                mBtAdapter.enable();
//            }
//        } else {
//            // Toast.makeText(this, R.string.msg_bluetooth_is_not_supported, Toast.LENGTH_SHORT).show();
//            postToast("bluetooth_is_not_supported");
//            return;
//        }
//
//
//
//        invokeHelper(new MethodInvoker() {
//            @Override
//            public void invoke() throws IOException {
//
//                postToast("PRINTING Z.... ");
//                try {
//                    connection = null;
//                    connection = new BluetoothConnection(address);
//                    connection.open();
//
//                    if (connection.isConnected()) {
//                        try {
//
//                            printer = ZebraPrinterFactory.getInstance(connection);
//                            //setStatus("Determining Printer Language", Color.YELLOW);
//                            String pl = SGD.GET("device.languages", connection);
//                            //setStatus("Printer Language " + pl, Color.BLUE);
//                        } catch (ConnectionException e) {
//                            //setStatus("Unknown Printer Language", Color.RED);
//                            printer = null;
//                            //  DemoSleeper.sleep(1000);
//                            disconnect();
//                        } catch (ZebraPrinterLanguageUnknownException e) {
//                            // setStatus("Unknown Printer Language", Color.RED);
//                            printer = null;
//                            //DemoSleeper.sleep(1000);
//                            disconnect();
//                        }
//
//
//
//
//                    }
//                } catch (ConnectionException v) {
//                    v.printStackTrace();
//                    System.out.println("Connerror"+v.toString());
//                    postToast("Connection to Device has Failed");
//                    //stopService(new Intent(getBaseContext(), ZebraPrint.class));
//                    return;
//                }
//
//
//
//                try {
//                    ZebraPrinterLinkOs linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(printer);
//
//
//                    PrinterStatus printerStatus = (linkOsPrinter != null) ? linkOsPrinter.getCurrentStatus() : printer.getCurrentStatus();
//
//
//
//
//
//                    if (printerStatus.isReadyToPrint) {
//
//                        byte prish[]=null;
//                        String connmsg;
//
//                        try {
//                            PrinterLanguage printerLanguage = printer.getPrinterControlLanguage();
//                            SGD.SET("device.languages", "zpl", connection);
//
//                            if (printerLanguage == PrinterLanguage.ZPL) {
//
//
//                                Calendar c = Calendar.getInstance();
//                                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss ");
//                                String formattedDate = df.format(c.getTime());
//
//
////                                connmsg= "^XA^LL350^FO5,200^A0I,30,30^FDDEVICE CONNECTION SUCCESS^FS" +
////                                        "^FO5,30^A0I,30,30^FD ^FS" +
////                                        "^FO5,50^A0I,30,30^FD ^FS " +
////                                        "^XZ";
////
////                                prish= connmsg.getBytes();
//
//                                String invnum,cd1,cd2,cd3,product;
//                                String name,surname,Route,Date,add,add1,add2;
//                                DecimalFormat df2 = new DecimalFormat(".##");
//
//
//
//
//                                cd1 = "^XA^LL30^MNV^FO5,0^A0N,30,30^FD";
//                                cd3= "^XA^LL30^MNV^FO5,0^A0N,30,50^FD";
//                                cd2 = "^FS^XZ";
//
//
//
//                                //connection.write("^XA^LL200^XZ".getBytes());
//
////                                    String storagePath =  getApplicationContext().getFilesDir().toString();
////                                    Bitmap CustomerSignature = BitmapFactory.decodeFile(storagePath + "/C"+ViewishActivity.invnum+".png");
////                                    //     ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
////                                    ZebraImageAndroid zebraImageToPrint = new ZebraImageAndroid(CustomerSignature);
////                                    printer.printImage(zebraImageToPrint, 0, 0, zebraImageToPrint.getWidth(), zebraImageToPrint.getHeight(), false);
////                                    connection.write("^XA^LL30^MNV^FO5,0^A0N,30,30^FDCustomer Signature^FS^XZ".getBytes());
////                                    // connection.write("^XA^LL30^MNV^FO5,0^AfN,30,10^FD ^FS^XZ".getBytes());
////
////                                    connection.write("^XA^LL200^XZ".getBytes());
////                                    Bitmap DriverSignature = BitmapFactory.decodeFile(storagePath + "/D"+ViewishActivity.invnum+".png");
////                                    //     ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
////                                    ZebraImageAndroid zebraImage2ToPrint = new ZebraImageAndroid(DriverSignature);
////                                    printer.printImage(zebraImage2ToPrint, 0, 0, zebraImage2ToPrint.getWidth(), zebraImage2ToPrint.getHeight(), false);
////                                    connection.write("^XA^LL30^MNV^FO5,0^A0N,30,30^FDDriver Signature'^FS^XZ".getBytes());
////                                    connection.write("^XA^LL30^MNV^FO5,0^AfN,30,10^FD ^FS^XZ".getBytes());
//
//
//                                // connection.write((cd1+"ITEM #                       "+quantity+cd2).getBytes());
//
//
//
//
//                                //    connection.write((cd1+"CHANGE                         $"+TenderActivity.change+cd2).getBytes());
//                                //  connection.write((cd1+"TENDER                       $"+TenderActivity.totamtPaid+cd2).getBytes());
//                                connection.write((cd1+"---------------------------------------"+cd2).getBytes());
//                                connection.write((cd1+"           "+MainActivity.serial+cd2).getBytes());
//                                connection.write((cd1+"---------------------------------------"+cd2).getBytes());
//                              //  connection.write((cd1+currhash.substring(32,currhash.length())+cd2).getBytes());
//                                //connection.write((cd1+currhash.substring(0,32)+cd2).getBytes());
//                                connection.write((cd1+"---------------------------------------"+cd2).getBytes());
//                                connection.write((cd1+"TAX AMOUNT 0"+cd2).getBytes());
//                                connection.write((cd1+"NET AMOUNT"+cd2).getBytes());
//                                connection.write((cd1+"VAT RATE 0"+cd2).getBytes());
//                                connection.write((cd1+"---------------------------------------"+cd2).getBytes());
//                                connection.write((cd1+"TAX AMOUNT 0"+cd2).getBytes());
//                                connection.write((cd1+"NET AMOUNT " + ItemTotals0rated+cd2).getBytes());
//                                connection.write((cd1+"VAT RATE 0"+cd2).getBytes());
//                                connection.write((cd1+"---------------------------------------"+cd2).getBytes());
//                                connection.write((cd1+"TAX AMOUNT "+ItemTaxes+cd2).getBytes());
//                                connection.write((cd1+"NET AMOUNT "+ ItemTotals +cd2).getBytes());
//                                connection.write((cd1+"VAT RATE 0.15"+cd2).getBytes());
//                                connection.write((cd1+"---------------------------------------"+cd2).getBytes());
//
//                                //TO ADD  OTHER INFO ON FILE
//                                //////////////////////////////
//
//                                connection.write((cd1+"VAT TOTALS"+cd2).getBytes());
//                                connection.write((cd1+"REGISTRATION DATE "+MainActivity.datetime+cd2).getBytes());
//                                //connection.write((cd1+"EFD SERIAL "+MainActivity.serial+cd2).getBytes());
//                                connection.write((cd1+"Z #: "+ String.valueOf(NumberofZforPrint+1)+cd2).getBytes());
//                                connection.write((cd1+"TAX OFFICE REGION 1 "+cd2).getBytes());
//                                connection.write((cd1+formattedDate+cd2).getBytes());
//                                connection.write((cd1+"REG#: "+MainActivity.regn+cd2).getBytes());
//                                connection.write((cd1+"VAT#: "+MainActivity.vat+cd2).getBytes());
//                                connection.write((cd1+"BPN : "+MainActivity.bpn+cd2).getBytes());
//                                connection.write((cd1+MainActivity.addr3+cd2).getBytes());
//                                connection.write((cd1+MainActivity.addr2+cd2).getBytes());
//                                connection.write((cd1+MainActivity.addr1+cd2).getBytes());
//                                connection.write((cd1+MainActivity.compname+cd2).getBytes());
//                              //  connection.write(("^XA^LL30^MNV^FO5,0^A0N,30,50^FD"+FileContentString.substring(FileContentString.indexOf("<INUM>")+6,FileContentString.indexOf("</INUM>"))+"^FS^XZ").getBytes());
//                                connection.write("^XA^LL30^MNV^FO5,0^AfN,30,10^FD ^FS^XZ".getBytes());
//                                connection.write("^XA^LL30^MNV^FO5,0^A0N,30,50^FD    ***** Z REPORT ***** ^FS^XZ".getBytes());
//                                connection.write("^XA^LL30^MNV^FO5,0^AfN,30,10^FD ^FS^XZ".getBytes());
//                                connection.write("^XA^LL30^MNV^FO5,0^AfN,30,10^FD=============================^FS^XZ".getBytes());
//
////currhash="";
//
////                    connection.write(prish);
//
//                            }
//
//
//
//                            //connection.write(prish);
//
//
//
//
//                        } catch (ConnectionException e) {
//                            postToast("Connection failure");
//
//                        }
//
//
//                    } else if (printerStatus.isHeadOpen) {
//                        //setStatus("Printer Head Open", Color.RED);
//                        postToast("Print Head Open");
//                        disconnect();
//
//                        // stopService(new Intent(getBaseContext(), ZebraPrint.class));
//
//                        return;
//                        //   Toast.makeText(getApplicationContext(),"Print Head Open",Toast.LENGTH_LONG).show();
//                    } else if (printerStatus.isPaused) {
//                        //setStatus("Printer is Paused", Color.RED);
//                        postToast("Printer is Paused");
//                        disconnect();
//
//                        //  stopService(new Intent(getBaseContext(), ZebraPrint.class));
//
//                        return;
//                        // Toast.makeText(getApplicationContext(),"Printer is Paused",Toast.LENGTH_LONG).show();
//                    } else if (printerStatus.isPaperOut) {
//                        //    setStatus("Printer Media Out", Color.RED);
//                        postToast("Paper Out");
//                        disconnect();
//
//                        //  stopService(new Intent(getBaseContext(), ZebraPrint.class));
//
//                        return;
//                        // Toast.makeText(getApplicationContext(),"Paper Out",Toast.LENGTH_LONG).show();
//                    }
//                    //  DemoSleeper.sleep(1500);
//                    //if (connection instanceof BluetoothConnection) {
//                    //  String friendlyName = ((BluetoothConnection) connection).getFriendlyName();
//                    //  setStatus(friendlyName, Color.MAGENTA);
//                    //DemoSleeper.sleep(500);
//                    // }
//                } catch (ConnectionException e) {
//                    postToast("Print Failure");
//                    disconnect();
//
//                    //stopService(new Intent(getBaseContext(), ZebraPrint.class));
//
//                    return;
//                } finally {
//                    disconnect();
//                    //  stopService(new Intent(getBaseContext(), ZebraPrint.class));
//                    //     startService(new Intent(getBaseContext(), Watchfi.class));
//                    // Toast.makeText(getApplicationContext(),"Interface Service Started",Toast.LENGTH_SHORT).show();
//                    //  disconnect();
//                    //   finish();
//                }
//
//
//
//            }
//        });
//
//
//
//
//    }


    private void invokeHelper(final MethodInvoker invoker) {
        //final ProgressDialog dialog = new ProgressDialog(this);
        //dialog.setMessage(getString(R.string.msg_please_wait));
        //dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
        //  @Override
        //public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        //  return true;
        //}

        //});
        //   dialog.show();

        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    invoker.invoke();
                } catch (final IOException e) { // Critical exception
                    e.printStackTrace();
                    postToast("Print Error");
                    //disconnect();
                } finally {
                    //                dialog.dismiss();


                }
            }
        });
        t.start();
    }
}