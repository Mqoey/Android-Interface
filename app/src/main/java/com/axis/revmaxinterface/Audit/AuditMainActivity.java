/*
 * Developed by Mqondisi Ndlovu Copyright (c) 2021. contact @ 0772783880
 */

package com.axis.revmaxinterface.Audit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.axis.revmaxinterface.Currencies;
import com.axis.revmaxinterface.DoWork;
import com.axis.revmaxinterface.Invoice;
import com.axis.revmaxinterface.Items;
import com.axis.revmaxinterface.MainActivity;
import com.axis.revmaxinterface.R;
import com.axis.revmaxinterface.RevMaxException;
import com.axis.revmaxinterface.wormTest;
import com.crashlytics.android.Crashlytics;
import com.secureflashcard.wormapi.WORM_ERROR;
import com.secureflashcard.wormapi.WormAccess;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static android.content.ContentValues.TAG;
import static java.lang.System.arraycopy;

public class AuditMainActivity extends AppCompatActivity {
    ImageView refresh;
    static String RevmaxSerialNumber;
    public Integer NumberOfZOnDevice = 0;
    public ArrayList<wormTest.WormEntry> list;
    DbHandler helper;
    public String znumber,currency,netamount, taxamout, vatrate, date, time ;

    //rev components
    private boolean initDone = false;
    private static Context mContext;
    wormTest mywormTest;
    private WormAccess worm = new WormAccess();
    String output;
    Invoice newInvoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audit_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        helper = new DbHandler(this);
        DbHandler db = new DbHandler(this);
        ArrayList<HashMap<String, String>> userList = db.getZReports();
        ListView lv = (ListView) findViewById(R.id.user_list);


        ListAdapter adapter = new SimpleAdapter(
                AuditMainActivity.this, userList, R.layout.list_row,
                new String[]{"znumber","currency","date"}, new int[]{R.id.name, R.id.designation, R.id.location});

        boolean ans = adapter.isEmpty();
        if (ans == true)
            System.out.println("The List is empty");
        else
            System.out.println("The List is not empty");

        lv.setAdapter(adapter);


        refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                refreshDb();
            }
        });

//        DbHandler db = new DbHandler(this);
//        ArrayList<HashMap<String, String>> userList = db.getZReports();
//        ListView lv = (ListView) findViewById(R.id.user_list);
//        ListAdapter adapter = new SimpleAdapter(AuditMainActivity.this, userList, R.layout.list_row,new String[]{"znumber","currency","date"}, new int[]{R.id.name, R.id.designation, R.id.location});
//        lv.setAdapter(adapter);

    }

    private void refreshDb() {
        Toast toast = Toast.makeText(AuditMainActivity.this,
                "Refreshing...",
                Toast.LENGTH_SHORT);
        toast.show();
        mywormTest = new wormTest(worm);

        try {
            System.out.println("triedddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd");
            final int ret = worm.init(AuditMainActivity.this);
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
                                            BufferedReader re ;

                                            InputStream inputStream = new ByteArrayInputStream(TransactionDetail.getBytes(Charset.forName("UTF-8")));

                                            //FileInputStream inputreader = new FileInputStream(root+File.separator+RevmaxFile);
                                            re = new BufferedReader(new InputStreamReader(inputStream));

                                            //POPULATE FILE AND CARD DETAILS
                                            try {
                                                //instantiate invoice object
                                                newInvoice = new Invoice();
                                                ItemCounter=0;

                                                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                                                factory.setNamespaceAware(true);
                                                XmlPullParser xpp = factory.newPullParser();
                                                // pass input for xml here
                                                xpp.setInput(re);
                                                int eventType = xpp.getEventType();
                                                while (eventType != XmlPullParser.END_DOCUMENT) {
                                                    if(eventType == XmlPullParser.START_DOCUMENT) {
                                                        //Log.d(TAG,"Start document");
                                                    } else if(eventType == XmlPullParser.START_TAG) {
                                                        starttagtrue=1;
                                                        //get tag name of start tag
                                                        //Log.d(TAG,"Start tag "+xpp.getName());
                                                        currentTag=xpp.getName().trim();

                                                    } else if(eventType == XmlPullParser.END_TAG) {
                                                        starttagtrue=0;
                                                        //no code for now
                                                    } else if(eventType == XmlPullParser.TEXT && starttagtrue==1) {

                                                        Log.d(TAG,"OG = "+currentTag+" "+xpp.getText()  ); // here you get the text from xml
                                                        //get text and validate based on tag
                                                        String text= xpp.getText();


                                                        if(currentTag.equals("BPN")){
                                                            if (MainActivity.bpn.equals("na")){
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
                                                            }
                                                            else {
                                                                newInvoice.setDeviceBPN(MainActivity.bpn);
                                                            }
                                                        }
                                                        else if (currentTag.equals("CODE")){
                                                            if(text.trim().equals("")){
                                                                newInvoice.setCODE("000");
                                                            }
                                                            else{
                                                                newInvoice.setCODE(text.trim());
                                                            }
                                                        }//
                                                        else if(currentTag.equals("MACNUM")){
                                                            newInvoice.setMACNUM(MainActivity.serial);
                                                        }

                                                        else if(currentTag.equals("DECSTARTDATE")){
                                                            newInvoice.setDECSTARTDATE(MainActivity.utilitiesClass.GetDateNoTime());
                                                        }
                                                        else if(currentTag.equals("DECENDDATE")){
                                                            newInvoice.setDECENDDATE(MainActivity.utilitiesClass.GetDateNoTime());
                                                        }
                                                        else if(currentTag.equals("DETSTARTDATE")){
                                                            newInvoice.setDETSTARTDATE(MainActivity.utilitiesClass.GetDateTime());
                                                        }
                                                        else if(currentTag.equals("DETENDDATE")){
                                                            newInvoice.setDETENDDATE(MainActivity.utilitiesClass.GetDateTime());
                                                        }

                                                        else if(currentTag.equals("CPY")){
                                                            newInvoice.setCPY("1");
                                                        }

                                                        else if(currentTag.equals("IND")){
                                                            newInvoice.setIND("0");
                                                        }

                                                        else if(currentTag.equals("ITYPE")){
                                                            newInvoice.setITYPE("na");
                                                        }

                                                        else if(currentTag.equals("ICODE")){
                                                            newInvoice.setICODE("na");
                                                        }

                                                        else if(currentTag.equals("BRANCH")){

                                                            if(text.trim().equals("")){
                                                                newInvoice.setBRANCH("");
                                                            }
                                                            else{
                                                                newInvoice.setBRANCH(text.trim());
                                                            }

                                                        }

                                                        else if(currentTag.equals("INUM")){

                                                            if(text.trim().equals("")){
                                                            }
                                                            else{
                                                                newInvoice.setINUM(text.trim());
                                                            }

                                                        }

                                                        else if (currentTag.equals("IBPN")){
                                                            newInvoice.setIBPN(MainActivity.bpn);
                                                        }
                                                        else if (currentTag.equals("INAME")){
                                                            newInvoice.setINAME(MainActivity.compname);

                                                        }


                                                        else if(currentTag.equals("VAT")){
                                                            newInvoice.setVAT(MainActivity.vat);
                                                        }


                                                        else if (currentTag.equals("IADDRESS")){
                                                            newInvoice.setIADDRESS(MainActivity.addr1+" "+MainActivity.addr2 +" "+MainActivity.addr3);

                                                        }

                                                        else if(currentTag.equals("ICONTACT")){
                                                            if(text.trim().equals("")){
                                                                newInvoice.setICONTACT("na");
                                                            }
                                                            else{
                                                                newInvoice.setICONTACT(text.trim());
                                                            }

                                                        }

                                                        else if(currentTag.equals("ISHORTNAME")){
                                                            newInvoice.setISHORTNAME("na");
                                                        }

                                                        else if(currentTag.equals("IPAYER")){

                                                            if(text.trim().equals("")){
                                                                newInvoice.setIPAYER("");
                                                            }
                                                            else{
                                                                newInvoice.setIPAYER(text.trim());
                                                            }

                                                        }

                                                        else if(currentTag.equals("IPVAT")){

                                                            if(text.trim().equals("")){
                                                                newInvoice.setIPVAT("");
                                                            }
                                                            else{
                                                                newInvoice.setIPVAT(text.trim());
                                                            }

                                                        }
                                                        else if(currentTag.equals("IPADDRESS")){

                                                            if(text.trim().equals("")){
                                                                newInvoice.setIPADDRESS("");
                                                            }
                                                            else{
                                                                newInvoice.setIPADDRESS(text.trim());
                                                            }


                                                        }
                                                        else if(currentTag.equals("IPTEL")){
                                                            if(text.trim().equals("")){
                                                                newInvoice.setIPTEL("");
                                                            }
                                                            else{
                                                                newInvoice.setIPTEL(text.trim());
                                                            }
                                                        }

                                                        else if(currentTag.equals("IPBPN")){

                                                            if(text.trim().equals("")){
                                                                newInvoice.setIPBPN("");
                                                            }
                                                            else{
                                                                newInvoice.setIPBPN(text.trim());
                                                            }

                                                        }

                                                        else if(currentTag.equals("ICURRENCY")){
                                                            if(text.trim().equals("")){
                                                                //throw new RevMaxException("Missing Base Currency");
                                                            }
                                                            else{
                                                                newInvoice.setICURRENCY(text.trim());
                                                            }
                                                        }
                                                        else if(currentTag.equals("IAMT")){

                                                            //this value is computed at the end of the try statement
                                                            newInvoice.setIAMT("0");
                                                        }
                                                        else if(currentTag.equals("ITAX")){

                                                            //  //this value is computed at the end of the try statement
                                                            newInvoice.setITAX("0");
                                                        }
                                                        else if(currentTag.equals("ISTATUS")){

                                                            if(text.trim().equals("")){
                                                                //    throw new RevMaxException("Missing Invoice Status");
                                                            }
                                                            else{
                                                                newInvoice.setISTATUS(text.trim());
                                                            }

                                                        }
                                                        else if(currentTag.equals("IISSUER")){

                                                            if(text.trim().equals("")){
                                                                newInvoice.setIISSUER("na");
                                                            }
                                                            else{
                                                                newInvoice.setIISSUER(text.trim());
                                                            }

                                                        }

                                                        else if(currentTag.equals("IDATE")){
                                                            newInvoice.setIDATE(MainActivity.utilitiesClass.GetDateTime());
                                                        }
                                                        else if(currentTag.equals("ITAXCTRL")){

                                                            if(text.trim().equals("")){
                                                                newInvoice.setITAXCTRL("na");
                                                            }
                                                            else{
                                                                newInvoice.setITAXCTRL(text.trim());
                                                            }

                                                        }
                                                        else if(currentTag.equals("IOCODE")){

                                                            if(text.trim().equals("")){
                                                                newInvoice.setIOCODE("");
                                                            }
                                                            else{
                                                                newInvoice.setIOCODE(text.trim());
                                                            }

                                                        }
                                                        else if(currentTag.equals("IONUM")){

                                                            if(text.trim().equals("")){
                                                                newInvoice.setIONUM("");
                                                            }
                                                            else{
                                                                newInvoice.setIONUM(text.trim());
                                                            }

                                                        }
                                                        else if(currentTag.equals("IREMARK")){

                                                            if(text.trim().equals("")){
                                                                newInvoice.setIREMARK("");
                                                            }
                                                            else{
                                                                newInvoice.setIREMARK(text.trim());
                                                            }
                                                        }

                                                        //populate items on Invoice to itemsList

                                                        else if(currentTag.equals("HH")){
                                                            //create new Item object

                                                            if(!text.trim().equals("")) {
                                                                ItemCounter+=1;
                                                                newItem = new Items();
                                                                //append HH item line number automatically
                                                                newItem.setHH(String.valueOf(ItemCounter));
                                                            }

                                                        }
                                                        else if(currentTag.equals("ITEMCODE")){
                                                            if(!text.trim().equals("")) {
                                                                newItem.setITEMCODE(text.trim());
                                                            }
                                                            //else if to rectify tag double reading for nested items
                                                            else if(newItem.getITEMCODE().equals("")){
                                                                newItem.setITEMCODE("na");
                                                            }
                                                        }
                                                        else if(currentTag.equals("ITEMNAME1")){
                                                            if(!text.trim().equals("")) {
                                                                newItem.setITEMNAME1(text.trim());
                                                            }
                                                            else if (newItem.getITEMNAME1().equals("")){
                                                                //  throw new RevMaxException("Missing Item Name 1");
                                                            }
                                                        }
                                                        else if (currentTag.equals("ITEMNAME2")){
                                                            if(!text.trim().equals("")) {
                                                                newItem.setITEMNAME2(text.trim());
                                                            }
                                                            else{
                                                                newItem.setITEMNAME2("na");
                                                            }
                                                        }
                                                        else if(currentTag.equals("QTY")){

                                                            if(!text.trim().equals("")) {

                                                                int result= MainActivity.utilitiesClass.DoubleValidator(Double.parseDouble(text.trim()));

                                                                if(result==MainActivity.utilitiesClass.SuccessResult){
                                                                    newItem.setQTY(text.trim());
                                                                }
                                                                else{
                                                                    throw new RevMaxException("Invalid Qty on Item :"+newItem.getITEMNAME1());
                                                                }
                                                            }
                                                            else if (newItem.getQTY().equals("")){
                                                                throw new RevMaxException("Missing Item Quantity");
                                                            }
                                                        }
                                                        else if(currentTag.equals("PRICE")){
                                                            if(!text.trim().equals("")) {
                                                                int result = MainActivity.utilitiesClass.DoubleValidator(Double.parseDouble(text.trim()));
                                                                if(result==MainActivity.utilitiesClass.SuccessResult){
                                                                    newItem.setPRICE(text.trim());
                                                                }
                                                                else{
                                                                    throw new RevMaxException("Invalid Price on Item :"+newItem.getITEMNAME1());
                                                                }
                                                            }
                                                            else if (newItem.getPRICE().equals("")){
                                                                throw new RevMaxException("Missing Item Price");
                                                            }
                                                        }
                                                        else if(currentTag.equals("AMT")){
                                                            //Autocalculate item amount here
                                                            newItem.setAMT(MainActivity.utilitiesClass.ItemTotalCalculator(newItem.getPRICE(),newItem.getQTY()));
                                                        }
                                                        else if(currentTag.equals("TAX")){
//                                                  if(!text.trim().equals("")) {
//                                                      int result = MainActivity.utilitiesClass.DoubleValidator(Double.parseDouble(text.trim()));
//                                                      if(result==MainActivity.utilitiesClass.SuccessResult){
//                                                          newItem.setTAX(text.trim());
//                                                      }
//                                                      else{
//                                                          throw new RevMaxException("Invalid Tax on Item :"+newItem.getITEMNAME1());
//                                                      }
//                                                  }
                                                            if (newItem.getPRICE().equals("")){
                                                                throw new RevMaxException("Missing Item Tax");
                                                            }
                                                        }

                                                        else if(currentTag.equals("TAXR")){
                                                            if(!text.trim().equals("")) {
                                                                if(!MainActivity.TaxesList.contains(text.trim())){
                                                                    throw new RevMaxException("Invalid Tax Rate on Item : "+newItem.getITEMNAME1());
                                                                }
                                                                else{

                                                                    int result = MainActivity.utilitiesClass.DoubleValidator(Double.parseDouble(text.trim()));
                                                                    if(result==MainActivity.utilitiesClass.SuccessResult){

                                                                        newItem.setTAXR(text.trim());
                                                                        //auto calculate and set TAX here
                                                                        newItem.setTAX(MainActivity.utilitiesClass.TaxCalculator(newItem.getAMT(),"1",MainActivity.TaxConfigFlag,newItem.getTAXR()));
                                                                        itemsList.add(newItem);
                                                                    }
                                                                    else{
                                                                        throw new RevMaxException("Invalid TAXR on Item: "+ newItem.getITEMNAME1());
                                                                    }

                                                                }
                                                            }
                                                            else if (newItem.getTAXR().equals("")){
                                                                throw new RevMaxException("Missing TaxR on Item : "+ newItem.getITEMNAME1());
                                                            }
                                                        }

                                                        //populate currencies in XML here
                                                        else if(currentTag.equals("Name")){
                                                            if(!text.trim().equals("")) {
                                                                newCurrency = new Currencies();
                                                                newCurrency.setName(text.trim());
                                                            }
                                                            else if (newCurrency.getName().equals("")){
                                                                throw new RevMaxException("Missing Currency Name");
                                                            }
                                                        }
                                                        else if(currentTag.equals("Amount")){
                                                            if(!text.trim().equals("")) {

                                                                int result = MainActivity.utilitiesClass.DoubleValidator(Double.parseDouble(text.trim()));
                                                                if(result==MainActivity.utilitiesClass.SuccessResult){
                                                                    newCurrency.setAmount(text.trim());
                                                                }
                                                                else{
                                                                    throw new RevMaxException("Invalid Currency Amount for Currency : "+ newCurrency.getName());
                                                                }

                                                            }
                                                            else if (newCurrency.getAmount().equals("")){
                                                                throw new RevMaxException("Missing Currency Amount");
                                                            }
                                                        }
                                                        else if(currentTag.equals("Rate")){
                                                            if(!text.trim().equals("")) {
                                                                int result = MainActivity.utilitiesClass.DoubleValidator(Double.parseDouble(text.trim()));
                                                                if(result==MainActivity.utilitiesClass.SuccessResult){
                                                                    newCurrency.setRate(text.trim());
                                                                    currenciesList.add(newCurrency);
                                                                }
                                                                else{
                                                                    throw new RevMaxException("Invalid Currency Rate for Currency : "+ newCurrency.getName());
                                                                }

                                                            }
                                                            else if (newCurrency.getRate().equals("")){
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
                                                if(newInvoice.getICURRENCY().equals("")){
                                                    newInvoice.setICURRENCY("ZWL");
                                                    Currencies currencies = new Currencies();
                                                    currencies.setName("ZWL");
                                                    currencies.setRate("1");
                                                    currencies.setAmount(newInvoice.getIAMT());
                                                    currenciesList.add(currencies);
                                                }

                                                newInvoice.setITAX(MainActivity.utilitiesClass.ComputeInvoiceTax( itemsList,MainActivity.TaxConfigFlag));
                                                newInvoice.setIAMT(MainActivity.utilitiesClass.ComputeInvoiceTotal(itemsList,newInvoice.getITAX(),MainActivity.TaxConfigFlag));
                                                invoicesList.add(newInvoice);


                                                //print lists to console
                                                MainActivity.utilitiesClass.PrintOutInvoiceList(invoicesList);
                                                MainActivity.utilitiesClass.PrintoutItemsList(itemsList);
                                                MainActivity.utilitiesClass.PrintoutCurrenciesList(currenciesList);

                                            } catch (XmlPullParserException e) {
                                                //        System.out.println("xmpars Invalid XML error "+ e.getMessage() );
                                                Crashlytics.logException(e);
                                                continue;
                                            } catch (IOException e) {
                                                //   System.out.println("xmpars Invalid XML error "+ e.getMessage() );
                                                Crashlytics.log("xmpars IO error "+MainActivity.compname +" "+ MainActivity.serial  +" " +IMEI);
                                                Crashlytics.logException(e);
                                                e.printStackTrace();
                                                continue;
                                            }
                                            catch (RevMaxException e){
                                                postToast("RevMax XML error " + e.toString().replace("com.axis.revmaxinterface.RevMaxException",""));
                                                //         System.out.println("RevMax XML error " + e.toString().replace("com.axis.revmaxinterface.RevMaxException","") );
                                                Crashlytics.log("Invalid/Missing Data Error"+MainActivity.compname +" "+ MainActivity.serial  +" " +IMEI);
                                                Crashlytics.logException(e);
                                                continue;
                                            }
                                            catch (Exception e){
                                                //    System.out.println("Revmax Error "+ e.getMessage() );
                                                Crashlytics.log("Revmax Error "+MainActivity.compname +" "+ MainActivity.serial  +" " +IMEI);
                                                Crashlytics.logException(e);
                                                e.printStackTrace();
                                                continue;
                                            }
                                            }
//                                            TransactionNumber++;
//                                        }

                                        //get all transactions
                                        TransactionNumber = 0;
                                        int checkZnumber = 0;

//                                        for (wormTest.WormEntry entry : list) {
//                                            try {
//                                                String TransactionDetails = "";
//
//                                                short[] copy_arr = new short[512 * entry.transactionBlocks];
//                                                arraycopy(entry.getPayload(), 0, copy_arr, 0, entry.getPayload().length);
//                                                for (int i = 0; i < copy_arr.length - 1; i++) {
//                                                    if (copy_arr[i] != 0) {
//                                                        TransactionDetails += (char) (copy_arr[i]);
//                                                        if (TransactionDetails.startsWith("2")) {
//                                                            break;
//                                                        }
//                                                    }
//                                                }
//                                                if (TransactionDetails.contains("<INVOICES>")) {
//                                                    checkZnumber += 1;
//                                                    System.out.println(TransactionDetails);
//
//                                                    Document doc = convertStringToXML(TransactionDetails);
//                                                    NodeList nList = doc.getElementsByTagName("ZimraSubmitInvoices");
//                                                    for (int i = 0; i < nList.getLength(); i++) {
//                                                        if (nList.item(0).getNodeType() == Node.ELEMENT_NODE) {
//                                                            Element elm = (Element) nList.item(i);
//
//                                                            znumber = getNodeValue("Znumber", elm);
//                                                            currency = getNodeValue("ITEMNAME1", elm);
//                                                            netamount = getNodeValue("QTY", elm);
//                                                            taxamout= getNodeValue("PRICE", elm);
//                                                            vatrate = getNodeValue("VATRATE", elm);
//                                                            date = getNodeValue("AMT", elm);
//                                                            time = getNodeValue("TAX", elm);
//                                                        }
//                                                    }
////                                                        System.out.println(znumber +  currency+netamount + taxamout + vatrate +time);
////                                                    helper.insertUserDetails(znumber,currency,netamount,taxamout,vatrate,date,time);
////
//                                                }
//                                                TransactionNumber++;
//                                            } catch (Exception ignored) {
//                                            }
//                                        }
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
}