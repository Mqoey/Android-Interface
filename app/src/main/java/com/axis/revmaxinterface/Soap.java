package com.axis.revmaxinterface;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;


import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

import static android.content.ContentValues.TAG;

public class Soap extends Service {

    private final Handler mHandler = new Handler();
    static String filetosend, currentUrl, ftpHostName, ftpUsername, ftpPassword, ftpWorkingDirectory;
    private PendingIntent pedint;
    private int NOTIFICATION_ID = 1;
    String err;
    String SoapXml="";
    static String IMEI, DeviceMode, PostDataResponse = "";
    static JSONObject DevicejsonObject;
    MyDB myDB;


    public Soap() {
    }

    private interface MethodInvoker {
        public void invoke() throws IOException;
    }


    public void onCreate() {
        super.onCreate();
        // ctx = this;
        myDB = new MyDB(getApplicationContext());
        //initialise Crashlytics and Fabric and get app context
        Fabric.with(this, new Crashlytics());

        //build json object to call requests
        try {
            DevicejsonObject = new JSONObject();

            //get token
            MainActivity.cr = myDB.selectToken();
            if (MainActivity.cr != null) {
                while (MainActivity.cr.isAfterLast() == false) {
                    MainActivity.DeviceToken = MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.token));
                }
                DevicejsonObject.put("token", MainActivity.DeviceToken);
            }
            else {
                DevicejsonObject.put("IMEIMAC", MainActivity.serial);
               // PostDataResponse = GetTokenPostData("kk", DevicejsonObject, "kj");
                //postToast("Cliviz"+PostDataResponse);
                System.out.println("Invoice Send Feedback " + PostDataResponse);

                try {
                    //convert json response to string object and get system token
                    JSONObject myJson = new JSONObject(PostDataResponse);
                    MainActivity.DeviceToken = myJson.optString("token");
                    postToast(MainActivity.DeviceToken);
                    myDB.InsertToken(MainActivity.DeviceToken);

                } catch (Exception e) {
                    postToast("JSON Object Build Error \n" + e.getStackTrace().toString().trim());
                    Crashlytics.logException(e);
                    return;
                }

            }


        } catch (org.json.JSONException e) {
            postToast("JSON Object Build Error \n" + e.getStackTrace().toString().trim());
            Crashlytics.logException(e);
        }

        //declaration of transaction list


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
                TelephonyManager tm = (TelephonyManager)getSystemService(this.TELEPHONY_SERVICE);
                IMEI=tm.getImei(0);
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                TelephonyManager tm = (TelephonyManager)getSystemService(this.TELEPHONY_SERVICE);
                IMEI=tm.getDeviceId();
            }
            Crashlytics.setUserIdentifier(IMEI);
           }
        catch (Exception e){
            Crashlytics.log("Android Permissions Exception " );
            Crashlytics.logException(e);
        }

        //to comment out
//        RevMaxPortalSend();

        ///////////////////////////////////////////////////////////////////////////////////////////////////
        //** this if statement is very key
        //** it determines whether transactions are sent to the Zimra live or demo server
        //** demo device bpn should always be 111111111 (9 ones) and company name should be Axis Solutions
        //** if device has no bpn no transaction is sent
        ///////////////////////////////////////////////////////////////////////////////////////////////////

        //TODO uncomment
        if (MainActivity.bpn.equals("na")){
//                        System.out.println("http No BPN");
//
//                        //stop service waiting for card details
            postToast("Please Restart Device ! ");

            //Inform server  of incident
            try {
                throw new RevMaxException("Device Registry Info Missing");
            } catch (RevMaxException e) {
                Crashlytics.log("Device Registry Information Missing " );
                Crashlytics.logException(e);
            }
            stopService(new Intent(getBaseContext(), Soap.class));
            return;
        }


        //postToast(MainActivity.bpn+ " "+MainActivity.compname);
        //uncomment for live app
       if((!MainActivity.bpn.equals("na")) && (MainActivity.bpn.equals("111111111")||MainActivity.bpn.equals("222222222")) && MainActivity.compname.contains("Axis Solutions")) {
           if ((!MainActivity.bpn.equals("na"))) {
              // postToast(MainActivity.bpn + " " + MainActivity.compname);

               err = "Demo Transaction Sending";
               currentUrl = "http://41.220.16.133/ZimraIMS_Proxy/ZimraIMS_ProcessFiscalInvoices_SubmitInvoicesRP.asmx";
               ftpHostName = "41.220.16.133";
               ftpUsername = "ftpaxis";
               ftpPassword = "Password2020";
               ftpWorkingDirectory = "\\Axis\\Dump";
               DeviceMode = "Demo";
               //uncomment for live  app
          }
       }
        //**uncomment when going live
        else{

            err = "Live Transaction Sending";
            currentUrl = "http://41.220.16.133/ZimraIMS_Proxy/ZimraIMS_ProcessFiscalInvoices_SubmitInvoicesRP.asmx";
            ftpHostName="41.220.16.133";
            ftpUsername="ftpaxis";
            ftpPassword="Password2020";
            ftpWorkingDirectory="\\Axis\\Dump";
            DeviceMode="Demo";
        }





        initNotifi();



//TODO uncomment

        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            ///mounted

            //File dir = android.os.Environment.getExternalStorageDirectory();

            //GET SUCCESSFULLY FISCALISED FILES FROM REVMAX DIRECTORY
            File dir = getApplicationContext().getFilesDir();
            ArrayList<String> filepath = new ArrayList<String>();
            File listFile[] = dir.listFiles();

            if (listFile != null) {
                for (int i = 0; i < listFile.length; i++) {
System.out.println("hglf;dsalkdjfhgnkdmfl'sadf;lgkhmfd,s;'adl;fkgnmb,v.d'se;lkdgnm,bvc.;';dlfkgjm,fds;glkg,");
                    if (listFile[i].isDirectory()) {// if its a directory need to get the files under that directory
                      continue;
                    } else {// add path of  files to your arraylist for later use
                  //     postToast(listFile[i].toString());
                        if(listFile[i].toString().endsWith(".xml") && (!listFile[i].toString().contains("Intconfig"))){
                         //   postToast(listFile[i].toString());
                            filetosend=listFile[i].toString();


                            invokeHelper(new MethodInvoker() {
                                @Override
                                public void invoke() throws IOException {
                                    soap();

                                }
                            });
                            break;

                        }else if(listFile[i].toString().endsWith(".z")){

                            filetosend=listFile[i].toString();


                            invokeHelper(new MethodInvoker() {
                                @Override
                                public void invoke() throws IOException {
                                    ftpZ();

                                }
                            });
                            break;

                        }
                        //Do what ever u want



                    }
                }

                Intent myIntent = new Intent(Soap.this, Watchfi.class);
                pedint = PendingIntent.getService(Soap.this, 0, myIntent, 0);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.add(Calendar.SECOND, 2);
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pedint);
                stopService(new Intent(getBaseContext(),Soap.class));
            }

        }



    }


    ///modify to for statement

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }



//public void RevMaxPortalSend(){
//
//
//    try {
//        String InvoiceID="";
//
//        //TODO iterate through invoices and send here
//        SoapXml="<?xml version=\"1.0\"";
//        //get invoice detail here
//        MainActivity.cr= myDB.selectInvoiceDetail();
//        JSONObject InvoiceJsonObject = DevicejsonObject;
//        if(MainActivity.cr!=null){
//
//         while (MainActivity.cr.isAfterLast()==false){
//            InvoiceID= MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.ID));
//             SoapXml="<?xml version=\"1.0\" encoding=\"utf-8\"?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\" xmlns:fisc=\"http://ZIMRA/FISC\">"
//                     +"<soapenv:Header/>"
//                     +"<soapenv:Body>"
//                     +"<tem:ZimraSubmitInvoices>"
//                     +"<fisc:INVOICE>"
//                     +"<fisc:BPN>"+MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.DeviceBPN))+"</fisc:BPN>"
//                     +"<fisc:CODE>"+00000+"</fisc:CODE>"
//                     +"<fisc:MACNUM>"+MainActivity.IMEI+"</fisc:MACNUM>"
//                     +"<fisc:DECSTARTDATE>"+20201020+"</fisc:DECSTARTDATE>"
//                     +"<fisc:DECENDDATE>"+2020100+"</fisc:DECENDDATE>"
//                     +"<fisc:DETSTARTDATE>"+202020+"</fisc:DETSTARTDATE>"
//                     +"<fisc:DETENDDATE>"+202020+"</fisc:DETENDDATE>"
//                     +"<fisc:CPY>"+1+"</fisc:CPY>"
//                     +"<fisc:IND>"+0+"</fisc:IND>"
//                     +"<fisc:INVOICES>"
//                     +"<fisc:RECORD>"
//                     +"<fisc:ITYPE>NA</fisc:ITYPE>"
//                     +"<fisc:ICODE"+MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.ICODE))+"</fisc:ICODE>"
//                     +"<fisc:INUM>"+MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.INUM))+"</fisc:INUM>"
//                     +"<fisc:IBPN>"+111111111+"</fisc:IBPN>"
//                     +"<fisc:INAME>"+MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.INAME))+"</fisc:INAME>"
//                     +"<fisc:ITAXCODE>"+MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.ITAX))+"</fisc:ITAXCODE>"
//                     +"<fisc:VAT>"+22222222+"</fisc:VAT>"
//                     +"<fisc:IADDRESS>"+MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.IADDRESS))+"</fisc:IADDRESS>"
//                     +"<fisc:ICONTACT>"+MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.ICONTACT))+"</fisc:ICONTACT>"
//                     +"<fisc:ISHORTNAME>"+MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.ISHORTNAME))+"</fisc:ISHORTNAME>"
//                     +"<fisc:IPAYER>"+MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.IPAYER))+"</fisc:IPAYER>"
//                     +"<fisc:IPVAT>"+MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.IPVAT))+"</fisc:IPVAT>"
//                     +"<fisc:IPADDRESS>"+MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.IPADDRESS))+"</fisc:IPADDRESS>"
//                     +"<fisc:IPTEL>"+MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.IPTEL))+"</fisc:IPTEL>"
//                     +"<fisc:IPBPN>"+MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.IPBPN))+"</fisc:IPBPN>"
//                     +"<fisc:IAMT>"+MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.IAMT))+"</fisc:IAMT>"
//                     +"<fisc:ICUR>"+MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.ICURRENCY))+"</fisc:ICUR>"
//                     +"<fisc:ITAX>"+MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.ITAX))+"</fisc:ITAX>"
//                     +"<fisc:ISTATUS>"+MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.ISTATUS))+"</fisc:ISTATUS>"
//                     +"<fisc:IISSUER>NEIL</fisc:IISSUER>"
//                     +"<fisc:IDATE>"+MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.IDATE))+"</fisc:IDATE>"
//                     +"<fisc:ITAXCTRL>"+MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.ITAXCTRL))+"</fisc:ITAXCTRL>"
//                     +"<fisc:IOCODE>"+MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.IOCODE))+"</fisc:IOCODE>"
//                     +"<fisc:IONUM>"+MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.INUM))+"</fisc:IONUM>"
//                     +"<fisc:IREMARK>"+MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.IREMARK))+"</fisc:IREMARK>"
//                     +"<fisc:Lattitude>0</fisc:Lattitude>"
//                     +"<fisc:Longitude>0</fisc:Longitude>"
//
//                     +"</fisc:RECORD>"
//                     +"</fisc:INVOICES>"
//                     +"</fisc:INVOICE>"
//                     +"</tem:ZimraSubmitInvoices>"
//                     +"</soapenv:Body>"
//                     +"</soapenv:Envelope>";
//
//             System.out.println("RESPONSE:SoapXML: "+SoapXml);
////             InvoiceJsonObject.put("InvoiceNumber",MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.INUM)));
////             InvoiceJsonObject.put( "IMEIMAC",MainActivity.IMEI);
////             InvoiceJsonObject.put("Branch",MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.BRANCH)));
////             InvoiceJsonObject.put("IAddress",MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.IADDRESS)));
////             InvoiceJsonObject.put("InvoiceCurrency",MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.ICURRENCY)));
////             InvoiceJsonObject.put("IAMT",MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.IAMT)));
////             InvoiceJsonObject.put("InvoiceBPN",MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.DeviceBPN)));
////             InvoiceJsonObject.put("InvoiceCode",MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.ICODE)));
////             InvoiceJsonObject.put( "InvoiceContact",MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.ICONTACT)));
////             InvoiceJsonObject.put( "InvoiceDate",MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.IDATE)));
////             InvoiceJsonObject.put( "InvoiceIssuer",MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.IISSUER)));
////             InvoiceJsonObject.put("InvoiceName",MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.INAME)));
////             InvoiceJsonObject.put( "IOCode",MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.IOCODE)));
////             InvoiceJsonObject.put("IONumber",MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.INUM)));
////             InvoiceJsonObject.put(  "IPAddress",MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.IPADDRESS)));
////             InvoiceJsonObject.put(   "InvoicePayer",MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.IPAYER)));
////             InvoiceJsonObject.put("InvoicePayerBPN",MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.IPBPN)));
////             InvoiceJsonObject.put(   "InvoicePayerVAT",MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.IPVAT)));
////             InvoiceJsonObject.put("InvoicePayerTel",MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.IPTEL)));
////             InvoiceJsonObject.put(    "InvoiceRemark",MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.IREMARK)));
////             InvoiceJsonObject.put(   "InvoiceShortName",MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.ISHORTNAME)));
////             InvoiceJsonObject.put(   "InvoiceStatus",MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.ISTATUS)));
////             InvoiceJsonObject.put(    "InvoiceTax",MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.ITAX)));
////             InvoiceJsonObject.put(    "InvoiceTaxCTRL",MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.ITAXCTRL)));
////             InvoiceJsonObject.put("InvoiceType",MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.ITYPE)));
////             InvoiceJsonObject.put("products","");
//            }
//        }
//        AsyncTaskRunner runner = new AsyncTaskRunner();
//        runner.execute("100");
//
//        MainActivity.cr=myDB.selectInvoiceItems(InvoiceID);
//        JSONArray productsArray = new JSONArray();
//        if(MainActivity.cr!=null){
//            while (MainActivity.cr.isAfterLast()==false){
//
////                +"<fisc:ITEMS>"
////                        +"<fisc:ITEM>"
////                        +"<fisc:HH>1</fisc:HH>"
////                        +"<fisc:ITEMCODE>"+ MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.ITEMCODE))+"</fisc:ITEMCODE>"
////                        +"<fisc:ITEMNAME1>"+MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.ITEMNAME1))+"</fisc:ITEMNAME1>"
////                        +"<fisc:ITEMNAME2>Paper</fisc:ITEMNAME2>"
////                        +"<fisc:QTY>"+MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.QTY))+"</fisc:QTY>"
////                        +"<fisc:PRICE>"+MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.PRICE))+"</fisc:PRICE>"
////                        +"<fisc:AMT>"+MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.AMT))+"</fisc:AMT>"
////                        +"<fisc:CUR>USD</fisc:CUR>"
////                        +"<fisc:TAX>"+MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.TAX))+"</fisc:TAX>"
////                        +"<fisc:TAXR>"+ MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.TAXR))+"</fisc:TAXR>"
////                        +"</fisc:ITEM>"
////                        +"<fisc:ITEM>"
////                        +"<fisc:HH>2</fisc:HH>"
////                        +"<fisc:ITEMCODE>1006</fisc:ITEMCODE>"
////                        +"<fisc:ITEMNAME1>Paper Clip</fisc:ITEMNAME1>"
////                        +"<fisc:ITEMNAME2>Paper</fisc:ITEMNAME2>"
////                        +"<fisc:QTY>1</fisc:QTY>"
////                        +"<fisc:PRICE>80</fisc:PRICE>"
////                        +"<fisc:AMT>80</fisc:AMT>"
////                        +"<fisc:CUR>USD</fisc:CUR>"
////                        +"<fisc:TAX>12</fisc:TAX>"
////                        +"<fisc:TAXR>0.15</fisc:TAXR>"
////                        +"</fisc:ITEM>"
////                        +"</fisc:ITEMS>"
////                JSONObject itemsobject= new JSONObject();
////                itemsobject.put("ItemCode", MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.ITEMCODE)));
////                itemsobject.put("ItemName1", MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.ITEMNAME1)));
////                itemsobject.put("Price", MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.PRICE)));
////                itemsobject.put("Quantity", MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.QTY)));
////                itemsobject.put("Tax", MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.TAX)));
////                itemsobject.put("TaxR", MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.TAXR)));
////                itemsobject.put("Amount", MainActivity.cr.getString(MainActivity.cr.getColumnIndex(ConstantVariables.AMT)));
////                productsArray.put(itemsobject);
//            }
//        }
//        InvoiceJsonObject.put("products", productsArray);
//
//
//
//        MainActivity.cr=myDB.selectInvoiceCurrency(InvoiceID);
//        JSONArray currencyArray = new JSONArray();
//        if(MainActivity.cr!=null){
//            while (MainActivity.cr.isAfterLast()==false){
//                JSONObject currencyobject= new JSONObject();
//                currencyobject.put("Name", "USD");
//                currencyobject.put("Amount", "250");
//                currencyArray.put(currencyobject);
//               }
//        }
//        InvoiceJsonObject.put("currency",currencyArray);
//
//
//
//
//
//                        //changed to timout to cater for sql reserverd word
//
//                        //PostDataResponse = PostData("kk", InvoiceJsonObject, "kj");
//
//                        //postToast("Cliviz"+PostDataResponse);
//                        System.out.println("Invoice Send Feedback "+ PostDataResponse);
//
//
//                        if (PostDataResponse.contains("success")) {
//                            postToast("Invoice Uploaded");
//                            //TODO flag invoices as sent here and add total to Z Report
//                            myDB.SetSyncedtoTrue(ConstantVariables.tblInvoices,ConstantVariables.syncedToRevMaxPortal,ConstantVariables.ID,InvoiceID);
//                            myDB.SetSyncedtoTrue(ConstantVariables.tblItems,ConstantVariables.syncedToRevMaxPortal,ConstantVariables.ID,InvoiceID);
//                            myDB.SetSyncedtoTrue(ConstantVariables.tblCurrencies,ConstantVariables.syncedToRevMaxPortal,ConstantVariables.ID,InvoiceID);
//                            myDB.SetSyncedtoTrue(ConstantVariables.tblVatCalculated,ConstantVariables.syncedToRevMaxPortal,ConstantVariables.ID,InvoiceID);
//                            //set synced to true on post success;
//
//                        } else {
//                            postToast("Invoice Upload Failed");
//                        }
//
//
//
//
//    }
//    catch (JSONException e){
//        postToast("Invoice Upload Error"+ e.getMessage().toString().trim());
//        Crashlytics.logException(e);
//        //stopService(new Intent(getBaseContext(), SyncData.class));
//        return;
//    }
//}


    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            System.out.println("RESPONSE: "+"Do in backgorund");
            String response ="";
            try {
                int a1=100;
                int b=200;
                //String requestXML="<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><Add xmlns=\"http://tempuri.org/\"><intA>"+a1+"</intA><intB>"+b+"</intB></Add> </soap:Body></soap:Envelope>";
                System.out.println("RESPONSE:SoapXML: "+SoapXml);
                String requestXML = SoapXml;



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
                httpConn.setRequestProperty("Content-Type","text/xml");
                httpConn.setRequestProperty("SOAPAction","http://tempuri.org/ZimraIMS_ProcessFiscalInvoices_SubmitInvoicesRP/ZimraSubmitInvoices");
                httpConn.setRequestMethod("POST");

                httpConn.setDoOutput(true);
                httpConn.setDoInput(true);

                //Sending the post body to the http connection
                OutputStreamWriter out=new OutputStreamWriter(httpConn.getOutputStream());
                out.write(requestXML);
                out.close();

                InputStream newIP=httpConn.getInputStream();
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

            }
            catch (java.net.MalformedURLException e) {
                System.out.println("Error in postRequest(): Secure Service Required");
                e.printStackTrace();

            } catch (Exception e) {
                System.out.println("Error in postRequest(): " + e.getMessage());
            }


            return  response;
        }


        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
          //  appendLog(result,"/Invoices");
//            System.out.println("RESPONSE: "+"PostExecute");
//
//            System.out.println("RESPONSE: "+result);


        }


        @Override
        protected void onPreExecute() {
            System.out.println("RESPONSE: "+"PreExecute");


        }


        @Override
        protected void onProgressUpdate(String... text) {
            System.out.println("RESPONSE: "+"Progress Update");

        }
    }


    //Section sends invoice to Zimra
    public void soap(){

        //postToast("here"+filetosend);
        File root = android.os.Environment.getExternalStorageDirectory();
        String inputLine;

       //if sass to chek file before action


        //  Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("YOUR PROXY", PORT NUMBER));
       // String SOAPUrl = "http://197.155.227.154/ZimraIMS_Proxy/ZimraIMS_ProcessFiscalInvoices_SubmitInvoicesRP.asmx";
        String xmlFile2Send = filetosend;
        String responseFileName = root.getAbsolutePath()+ "/resp.txt";



        // Create the connection with http

       // System.out.println("http "+xmlFile2Send.toString());



        try {
            URL url = new URL(currentUrl);
            URLConnection connection = url.openConnection();
            HttpURLConnection httpConn = (HttpURLConnection) connection;
            FileInputStream fin = new FileInputStream(xmlFile2Send);

            ByteArrayOutputStream bout = new ByteArrayOutputStream();

            copy(fin, bout);
            fin.close();

            byte[] b = bout.toByteArray();
            StringBuffer buf=new StringBuffer();
            String s=new String(b);

            b=s.getBytes();


            // Set the appropriate HTTP parameters.
            // httpConn.setRequestProperty("Content-Length", String.valueOf(b.length));
            httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            httpConn.setRequestProperty("SOAPAction", "http://tempuri.org/ZimraIMS_ProcessFiscalInvoices_SubmitInvoicesRP/ZimraSubmitInvoices");
            httpConn.setRequestMethod("POST");
            httpConn.setDoOutput(true);

            // send the XML that was read in to b.
            OutputStream out = httpConn.getOutputStream();
            out.write(b);
            out.close();

            // Read the response.
            httpConn.connect();
            String httpConnResponsemessage = httpConn.getResponseMessage().toString();
            System.out.println("http connection status :" + httpConnResponsemessage);

            InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
            BufferedReader in = new BufferedReader(isr);

            while ((inputLine = in.readLine()) != null){
               System.out.println("zimra response"+inputLine);
            }
            System.out.println("http connection status :" + httpConnResponsemessage);

            String inputlineresponse;
//            if(inputLine!=null){
//                inputlineresponse=inputLine;
//            }

             if(inputLine!=null){
                inputlineresponse=inputLine;
             }
             else{
                inputlineresponse="";
             }

            if (inputlineresponse.equals("") || inputlineresponse.contains("<INVNUM>0</INVNUM><INVITM>0</INVITM>")){

                //error from server
               //postToast(httpConnResponsemessage);

                //RENAME FILE INTO APP FOLDER AND DELETE FROM PUBIC FOLDER
                try{

                    System.out.println("send failure");
                    String failfile;
                    failfile=xmlFile2Send.substring(xmlFile2Send.lastIndexOf("/")+1,xmlFile2Send.length()).trim();
                    File rep = new File(Environment.getExternalStorageDirectory()+File.separator+failfile+".fld");


                    rep.createNewFile();
                    //Input stream is revmax file current locatsion, output stream is new destination
                    InputStream ol = new FileInputStream(xmlFile2Send);
                    OutputStream ne = new FileOutputStream(rep);

                    // Transfer bytes from in to out
                    byte[] buff = new byte[1024];
                    int len;
                    while ((len = ol.read(buff)) > 0) {
                        ne.write(buff, 0, len);
                    }
                    ol.close();
                    ne.close();

                    //del file
                    File file = new File(xmlFile2Send);
                    appendLog(file.getName()+"   "+"transimited successfully","/Invoices");
                    boolean deleted = file.delete();
                    Log.v("log_tag","deleted: " + deleted);


                }
                catch(Exception vc){

                }

                //********STOP SERVICE HERE
             //  stopService(new Intent(getBaseContext(),Soap.class));
            }
            else{//SUCCESSFUL FILE SEND, DELETE FILE
                //postToast(httpConnResponsemessage);

                RevLog("Invoice","Success");
             //System.out.println("send success :" + httpConnResponsemessage);
                Log.v("log_tag","send success" );
                System.out.println(xmlFile2Send);
                File file = new File(xmlFile2Send);
                appendLog(file.getName()+"  "+"Invoice transimitted successfully...","/Invoices");
                boolean deleted = file.delete();
                Log.v("log_tag","deleted: " + deleted);
                System.out.println("send success :" + deleted);
                postToast("send success");

            }





            FileOutputStream fos=new FileOutputStream(responseFileName);
            copy(httpConn.getInputStream(),fos);
            in.close();
            stopService(new Intent(getBaseContext(),Soap.class));

        }
        catch(java.net.MalformedURLException e) {
            Toast.makeText(this.getApplicationContext(),"Url error",Toast.LENGTH_LONG).show();
            Crashlytics.log("SOAP MalformedURL " );
            Crashlytics.logException(e);
            stopService(new Intent(getBaseContext(),Soap.class));
        } catch (IOException e) {
            Crashlytics.log("SOAP IO Exception " );
            Crashlytics.logException(e);
               System.out.println("Send error " +e.toString().toString());
               postToast("Sync Failed, try again later: "+ e.toString());
              stopService(new Intent(getBaseContext(),Soap.class));

        }






    }
    public static void appendLog(String text,String logpath) {
        Log.e("appendLog", "appendLog call");
System.out.println("appendlog");
        File root = android.os.Environment.getExternalStorageDirectory();
//        File zdirectory = new File(root.getAbsolutePath()+"/Zreports");
//        if(!zdirectory.exists()) {
//            zdirectory.mkdir();
//        }
        File log = new File(root.getAbsolutePath()
                + logpath+"/Log");

        if (!log.exists()) {
            log.mkdirs();
        }

        File logFile = new File(root.getAbsolutePath()
                + logpath+"/Log/Logs_file.txt");

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
    public static void copy(InputStream in, OutputStream out)
            throws IOException {

        synchronized (in) {
            synchronized (out) {
                byte[] buffer = new byte[256];
                while (true) {
                    int bytesRead = in.read(buffer);
                    if (bytesRead == -1)
                        break;
                    out.write(buffer, 0, bytesRead);
                }
            }
        }
    }


    private void postToast(final String text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
            }
        });
    }



    private void invokeHelper(final MethodInvoker invoker) {
       /* final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.msg_please_wait));
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return true;
            }

        });
       // dialog.show();*/

        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    invoker.invoke();
                } catch (final IOException e) { // Critical exception
                    e.printStackTrace();



                    Intent myIntent = new Intent(Soap.this, Watchfi.class);
                    pedint = PendingIntent.getService(Soap.this, 0, myIntent, 0);
                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.add(Calendar.SECOND, 2);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pedint);
                    stopService(new Intent(getBaseContext(),Soap.class));

                    // stopService(new Intent(getBaseContext(), MyService.class));

                    //disconnect();
                    //doish();
                    // selectDevice();
                }

                catch (final NullPointerException e) {
                    e.printStackTrace();
                    stopService(new Intent(getBaseContext(),Soap.class));

                    //Toast.makeText(MyService.this,"NULL POINTER EXCEPTION",Toast.LENGTH_LONG).show();
                } catch (final RuntimeException e){
                    e.printStackTrace();

                    Intent myIntent = new Intent(Soap.this, Watchfi.class);
                    pedint = PendingIntent.getService(Soap.this, 0, myIntent, 0);
                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.add(Calendar.SECOND, 2);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pedint);
                    stopService(new Intent(getBaseContext(),Soap.class));

                }
                finally {
                    //  dialog.dismiss();
                    // doish();

                }
            }
        });
        t.start();
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
                .setContentTitle(MainActivity.serial+" Status")
                .setContentText(err)
                .setTicker("Interface Alert")
                .setAutoCancel(true)
                //     .setSound(uri)
                .setSmallIcon(icon)
                .setContentIntent(contentIntent);
        Notification arrival = notifil.build();
        mnotifi.notify(NOTIFICATION_ID, arrival);

    }


    public void ftpZ(){
        ///////////////////////////////////////////////////
///Sync Signatures section, uses ftp connectionn
System.out.println("send zreports");
        File dirFiles = getApplicationContext().getFilesDir();
        FTPClient ftpClient = new FTPClient();
        ftpClient.setConnectTimeout(5000);

        try {
           ftpClient.connect(ftpHostName);
            ftpClient.login(ftpUsername, ftpPassword);
            ftpClient.changeWorkingDirectory(ftpWorkingDirectory);
System.out.println(ftpClient.getReplyString());
            if (ftpClient.getReplyString().contains("250")) {

                for (String SignatureFile: dirFiles.list())
                {

                    if(SignatureFile.contains(".z")){
                        //   postToast(SignatureFile);
                        // SignatureFile is the file name
                        ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
                        BufferedInputStream buffIn = null;
                        buffIn = new BufferedInputStream(new FileInputStream( getFilesDir()+"/"+ SignatureFile));
                        ftpClient.enterLocalPassiveMode();
                        //  ProgressInputStream progressInput = new ProgressInputStream(buffIn, progressHandler);

                        boolean result = ftpClient.storeFile(SignatureFile.replace(".z",".xml"), buffIn);

                        int x=ftpClient.getReplyCode();
System.out.println(x);
                        if(x==226){

                            RevLog("ZReport","Success");
                            postToast("Z Transmitted Successfully");
                            File SignaturetoDelete = new File(dirFiles,SignatureFile);
                            appendLog(SignaturetoDelete.getName()+"  "+"zreport transimitted successfully","/Zreports");
                            boolean deleted = SignaturetoDelete.delete();
                            System.out.println(deleted);
                        }

                        buffIn.close();
                    }




                }
                ftpClient.logout();
                ftpClient.disconnect();
            }
            stopService(new Intent(getBaseContext(),Soap.class));
        } catch (SocketException e) {
            e.printStackTrace();
           // g=1;
            // continue;


            Crashlytics.log("FTP SocketException " );
            Crashlytics.logException(e);
            stopService(new Intent(getBaseContext(),Soap.class));
             //Log.e(FTPsend.TAG, e.getStackTrace().toString());
        } catch (UnknownHostException e) {
            e.printStackTrace();


           // g=1;
            postToast("Ftp Error");
            Crashlytics.log("FTP UnknownHostException " );
            Crashlytics.logException(e);
            stopService(new Intent(getBaseContext(),Soap.class));
            // Log.e(SorensonApplication.TAG, e.getStackTrace().toString());
        } catch (IOException e) {


            Crashlytics.log("FTP IO Exception " );
            Crashlytics.logException(e);
            stopService(new Intent(getBaseContext(),Soap.class));
            //e.printStackTrace();
          //  g=1;

            //  Log.e(SorensonApplication.TAG, e.getStackTrace().toString());
        }//catch (NetworkOnMainThreadException e){

    }


    public void RevLog(String TransactionTyper, String TransactionResultr){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Create a new company with a first and last name
        Map<String, Object> company = new HashMap<>();
        company.put("COMPANY NAME", MainActivity.compname);
        company.put("BPN", MainActivity.bpn);
        company.put("VAT", MainActivity.vat);
        company.put("LastLog",formattedDate);


        // Add a new document with company name
        db.collection("Companies").document(MainActivity.compname)
                .set(company)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Toast.makeText(getContext(),"Registry Failure, Check Network",Toast.LENGTH_LONG).show();
                        Log.w(TAG, "Error writing document", e);
                        // return;
                    }
                });



        // Add a new device
        // Create a new device
        Map<String, Object> device = new HashMap<>();
        device.put("COMPANY NAME", MainActivity.compname);
        device.put("IMEIMAC", IMEI);
        device.put("SerialNum",MainActivity.serial);
        device.put("Device Name", android.os.Build.MODEL);
        device.put("RegDate",MainActivity.datetime);
        device.put("FirmWareVersion",MainActivity.firmWareID);
        device.put("WormVersion",MainActivity.wormVersion);
        device.put("LastLog",formattedDate);
        device.put("DeviceMode",DeviceMode);
        device.put("TransactionType",TransactionTyper);
        device.put("TransactionResult",TransactionResultr);


        db.collection("Devices").document(MainActivity.IMEI)
                .set(device)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //   Log.d(TAG, "Device Detail successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Toast.makeText(getContext(),"Registry Failure, Check Network",Toast.LENGTH_LONG).show();
                        Log.w(TAG, "Error writing device", e);
                        //  return;
                    }
                });
    }



    //post sync request to restapi
//    public String GetTokenPostData (String EndPointName, JSONObject DataToPost, String URLToPost){
//        String result="";
//
//        try {
//
//            //System.out.println("URL SENT"+URLToPost+EndpointPrecedent+EndPointName);
//
//
//            //define URL and content type
//            //URL url = new URL(URLToPost+EndpointPrecedent+EndPointName);
//            URL url = new URL("http://axiscare.co.zw:8001/ZimraWeb/api/updateAccountProducts");
//            //URL url = new URL("http://154.120.229.190:2020/ZimraWeb/api/getToken");
//            URLConnection connection = url.openConnection();
//            HttpURLConnection httpConn = (HttpURLConnection) connection;
//            //httpConn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
//            //httpConn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
//            httpConn.setRequestMethod("POST");
//            httpConn.setDoOutput(true);
//
//            //send JsonObject to REST API
//            OutputStream os = httpConn.getOutputStream();
//            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
//            writer.write(DataToPost.toString());
//            Log.i("JSONINfo", DataToPost.toString());
//            writer.flush();
//            writer.close();
//            os.close();
//
//            //Connect to URL and get response
//            httpConn.connect();
//            String httpConnResponsemessage = httpConn.getResponseMessage().toString();
//            System.out.println("http connection status :" + httpConnResponsemessage);
//
//            InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
//            BufferedReader in = new BufferedReader(isr);
//            String inputLine;
//            while ((inputLine = in.readLine()) != null){
//                System.out.println("response"+inputLine);
//                result+=inputLine;
//            }
//            return result;
//
//        }
//        catch(java.net.MalformedURLException e) {
//            System.out.println("url error"+e.getStackTrace().toString().trim());
//            postToast("Url Error");
//            Crashlytics.logException(e);
//            result = e.getStackTrace().toString().trim();
//            return result;
//        } catch (IOException e) {
//            System.out.println("Send error " +e.toString().toString());
//            postToast("Sync Failed, try again later: ");
//            Crashlytics.logException(e);
//            result= e.getStackTrace().toString().trim();
//            return result;
//        }
//    }


    //post sync request to restapi
//    public String PostData (String EndPointName, JSONObject DataToPost, String URLToPost){
//        String result="";
//
//        try {
//
//            //System.out.println("URL SENT"+URLToPost+EndpointPrecedent+EndPointName);
//
//
//            //define URL and content type
//            //URL url = new URL(URLToPost+EndpointPrecedent+EndPointName);
//            URL url = new URL("http://axiscare.co.zw:8001/ZimraWeb/api/updateAccountProducts");
//            //URL url = new URL("http://154.120.229.190:2020/ZimraWeb/api/updateAccountProducts");
//            URLConnection connection = url.openConnection();
//            HttpURLConnection httpConn = (HttpURLConnection) connection;
//           //httpConn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
//            //httpConn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
//            httpConn.setRequestMethod("POST");
//            httpConn.setDoOutput(true);
//
//            //send JsonObject to REST API
//            OutputStream os = httpConn.getOutputStream();
//            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
//            writer.write(DataToPost.toString());
//            Log.i("JSONINfo", DataToPost.toString());
//            writer.flush();
//            writer.close();
//            os.close();
//
//            //Connect to URL and get response
//            httpConn.connect();
//            String httpConnResponsemessage = httpConn.getResponseMessage().toString();
//            System.out.println("http connection status :" + httpConnResponsemessage);
//
//            InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
//            BufferedReader in = new BufferedReader(isr);
//            String inputLine;
//            while ((inputLine = in.readLine()) != null){
//                System.out.println("response"+inputLine);
//                result+=inputLine;
//            }
//            return result;
//
//        }
//        catch(java.net.MalformedURLException e) {
//            System.out.println("url error"+e.getStackTrace().toString().trim());
//            postToast("Url Error");
//            Crashlytics.logException(e);
//            result = e.getStackTrace().toString().trim();
//            return result;
//        } catch (IOException e) {
//            System.out.println("Send error " +e.toString().toString());
//            postToast("Sync Failed, try again later: ");
//            Crashlytics.logException(e);
//            result= e.getStackTrace().toString().trim();
//            return result;
//        }
//    }
}
