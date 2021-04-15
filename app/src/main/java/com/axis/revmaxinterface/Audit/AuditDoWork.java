/*
 * Developed by Mqondisi Ndlovu Copyright (c) 2021. contact @ 0772783880
 */

package com.axis.revmaxinterface.Audit;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.axis.revmaxinterface.wormTest;
import com.secureflashcard.wormapi.WORM_ERROR;
import com.secureflashcard.wormapi.WormAccess;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static java.lang.System.arraycopy;

public class AuditDoWork extends Service {

    public ArrayList<wormTest.WormEntry> list;
    static String VatRateA = "14.5", VatRateB = "0";
    static String ItemQTYString = "0",  InvoicetotalsString = "0", InvoicetaxesString = "0.0";
    static Double ItemTotals0rated, Usdfinal0Total, final0Total, ItemTotals = 0.0, InvTotals = 0.0, UsdInvTotals = 0.0, ItemTaxes = 0.0, UsdfinalTax = 0.0, UsdfinalTotal = 0.0, finalTax = 0.0, finalTotal = 0.0;
    static String  inumcurr;
    public static String ZRNumber;
    static String RevmaxSerialNumber;
    public String znumber,currency,netamount, taxamout, vatrate, date, time ;
    public Integer NumberOfZOnDevice = 0, NumberofZforPrint = 0;
    static String HeaderCo4, HeaderCo5;
    static String VatRate = "0.145", icurrency = "", Invoiceamntstring = "0.0";

    //rev components
    private boolean initDone = false;
    private static Context mContext;
    wormTest mywormTest;
    private WormAccess worm = new WormAccess();
    String output;
    DbHandler helper;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void onCreate() {
        super.onCreate();
        helper = new DbHandler(this);
        //CALL METHOD WHEN SERVICE STARTS
        startService();
    }

    private void startService() {
        mywormTest = new wormTest(worm);
        //
        new Thread(new Runnable() {
            /////initialise card
            public void run() {
                try {
                    final int ret = worm.init(AuditDoWork.this);
                    if (-1 == ret) {
                        output = "Please reboot your phone!\n";
                    } else if (-2 == ret) {
                        output = "Please insert card!\n";
                    } else {
                        output = "Init Done\n";
                        output += "WormAPI version: " + worm.version() + "\n";
                        String id = "";

                        if (mywormTest.get_Printable_Unique_ID() != "") {
                            output += "---------------------------------\n";
                            output += "Card_Unique_ID: " + mywormTest.get_Printable_Unique_ID() + "\n";
                            RevmaxSerialNumber = mywormTest.get_Printable_Unique_ID().trim();
                            RevmaxSerialNumber = RevmaxSerialNumber.replace("SWISSBIT", "").trim();
                        }

                        if (mywormTest.get_FW_ID() != -1) {
                            output += "---------------------------------\n";
                            output += "FW_ID: " + Integer.toString(mywormTest.get_FW_ID()) + "\n";
                        }
                        initDone = true;

                        ///////LOGIN TO CARD
                        if (initDone) {
                            output = "";
                            Thread transaction = new Thread(new Runnable() {
                                public void run() {
                                    byte[] pin = {0x31, 0x32, 0x33, 0x34};
                                    WORM_ERROR retVal = worm.PINLogin(pin, 4);
                                    if (retVal != WORM_ERROR.WORM_ERROR_NOERROR) {
                                        output += "Login Failed!!!! \n";
                                    } else {
                                        output += "Login Passed!!!! \n";
                                        try {
                                            //get all worm transactions
                                            list = mywormTest.exportWormStores(list);
                                            if (list.size() != 0) {

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
                                                    if (TransactionDetail.contains("<ZREPORT>")) {
                                                        //increment Z count if transaction is a Zreport
                                                        NumberOfZOnDevice += 1;
                                                        ArrayList<HashMap<String, String>> userList = new ArrayList<>();
//                                                            ListView lv = (ListView) findViewById(R.id.user_list);
//                                                            String znumber = getIntent().getStringExtra("ZNUM");
//                                                            System.out.println(znumber);

//                                                            InputStream istream = new ByteArrayInputStream(TransactionDetail.getBytes(StandardCharsets.UTF_8));
//                                                            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
//                                                            DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
//                                                            Document doc = docBuilder.parse(istream);

                                                        Document doc = convertStringToXML(TransactionDetail);
                                                        NodeList nList = doc.getElementsByTagName("ZREPORT");
                                                        for (int i = 0; i < nList.getLength(); i++) {
                                                            if (nList.item(0).getNodeType() == Node.ELEMENT_NODE) {
                                                                Element elm = (Element) nList.item(i);
                                                                znumber = getNodeValue("Znumber", elm);
                                                                currency = getNodeValue("CURRENCY", elm);
                                                                netamount = getNodeValue("NETTAMOUNT", elm);
                                                                taxamout= getNodeValue("TAXAMOUNT", elm);
                                                                vatrate = getNodeValue("VATRATE", elm);
                                                                date = getNodeValue("DATE", elm);
                                                                time = getNodeValue("TIME", elm);
                                                            }
                                                        }
//                                                        System.out.println(znumber +  currency+netamount + taxamout + vatrate +time);
//                                                            ListAdapter adapter = new SimpleAdapter(ZReports.this, userList, R.layout.activity_zreports,
//                                                                    new String[]{"znumber", "vatrate", "netamount1" , "netamount2" , "taxamout1", "taxamout2"},
//                                                                    new int[]{R.id.z_number, R.id.vat_rate, R.id.zwl_total, R.id.usd_total, R.id.zwl_tax, R.id.usd_tax});
//                                                            lv.setAdapter(adapter);
//                                                        System.out.println(TransactionDetail);
//                                                        System.out.println(" tytytytyypeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"+TransactionDetail.getClass().getSimpleName());
//                                                        writeToFile(TransactionDetail, AuditDoWork.this);
                                                        helper.insertUserDetails(znumber,currency,netamount,taxamout,vatrate,date,time);
//                                                        helper.getData();

//                                                        int id = helper.cid;
//                                                        String znumber = helper.znumber;
//                                                        String currency = helper.currency;
//                                                        System.out.println(id + " " + znumber + " " + currency);
                                                    }
                                                    TransactionNumber++;
                                                }

                                                //get all transactions
                                                TransactionNumber = 0;
                                                int checkZnumber = 0;

                                                for (wormTest.WormEntry entry : list) {
                                                    try {
                                                        String TransactionDetails = "";

                                                        short[] copy_arr = new short[512 * entry.transactionBlocks];
                                                        arraycopy(entry.getPayload(), 0, copy_arr, 0, entry.getPayload().length);
                                                        for (int i = 0; i < copy_arr.length - 1; i++) {
                                                            if (copy_arr[i] != 0) {
                                                                TransactionDetails += (char) (copy_arr[i]);
                                                                if (TransactionDetails.startsWith("2")) {
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                        if (TransactionDetails.contains("<INVOICES>")) {
                                                            checkZnumber += 1;
                                                            System.out.println(TransactionDetails);

                                                            Document doc = convertStringToXML(TransactionDetails);
                                                            NodeList nList = doc.getElementsByTagName("ZimraSubmitInvoices");
                                                            for (int i = 0; i < nList.getLength(); i++) {
                                                                if (nList.item(0).getNodeType() == Node.ELEMENT_NODE) {
                                                                    Element elm = (Element) nList.item(i);

                                                                    znumber = getNodeValue("Znumber", elm);
                                                                    currency = getNodeValue("ITEMNAME1", elm);
                                                                    netamount = getNodeValue("QTY", elm);
                                                                    taxamout= getNodeValue("PRICE", elm);
                                                                    vatrate = getNodeValue("VATRATE", elm);
                                                                    date = getNodeValue("AMT", elm);
                                                                    time = getNodeValue("TAX", elm);
                                                                }
                                                            }
//                                                        /System.out.println(znumber +  currency+netamount + taxamout + vatrate +time);
//
                                                        }
                                                        TransactionNumber++;
                                                    } catch (Exception ignored) {
                                                    }
                                                }
                                            }
                                        } catch (Exception ignored) {
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
                }
            }
        }).start();
    }

    private static Document convertStringToXML(String transactionDetail) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource(new StringReader(transactionDetail)));

        return document;

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String getNodeValue(String znumber, Element element) {
        NodeList nodeList = element.getElementsByTagName(znumber);
        Node node = nodeList.item(0);
        if (node != null) {
            if (node.hasChildNodes()) {
                Node first_child = node.getFirstChild();
                Node last_child = node.getLastChild();
                while (first_child != null) {
                    while (last_child != null) {
                        if (first_child.getNodeType() == Node.TEXT_NODE) {
                            return first_child.getNodeValue();
                        }
                    }
                }
            }
        }
        return "";
    }

    private void writeToFile(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.xml", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("config.xml");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }
}