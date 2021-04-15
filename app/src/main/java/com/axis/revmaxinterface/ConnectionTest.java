package com.axis.revmaxinterface;

import android.os.AsyncTask;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class ConnectionTest {
    static JSONObject DevicejsonObject;
    static String IMEI,DeviceMode,PostDataResponse="";


    public String InitiateSend(){
        try {
            DevicejsonObject = new JSONObject();
            DevicejsonObject.put("token", "e684385f071604b09ea5843e838190f0860a83bf00f3627e1a07137f2a0b76fe");
         String result=RevMaxPortalSend();

            AsyncTaskRunner runner = new AsyncTaskRunner();
            runner.execute("100");

         return result;
        }
        catch (org.json.JSONException e){
            Crashlytics.logException(e);
            return String.valueOf(UtilitiesClass.FailResult);
        }
    }
    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            System.out.println("RESPONSE: "+"Do in backgorund");
            String response ="";
            try {
                int a1=100;
                int b=200;
                //String requestXML="<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><Add xmlns=\"http://tempuri.org/\"><intA>"+a1+"</intA><intB>"+b+"</intB></Add> </soap:Body></soap:Envelope>";
                String requestXML="<?xml version=\"1.0\" encoding=\"utf-8\"?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\" xmlns:fisc=\"http://ZIMRA/FISC\">"
                        +"<soapenv:Header/>"
                        +"<soapenv:Body>"
                        +"<tem:ZimraSubmitInvoices>"
                        +"<fisc:INVOICE>"
                        +"<fisc:BPN>111111111</fisc:BPN>"
                        +"<fisc:CODE>00000</fisc:CODE>"
                        +"<fisc:MACNUM>925DC9FA014C</fisc:MACNUM>"
                        +"<fisc:DECSTARTDATE>2020-10-20</fisc:DECSTARTDATE>"
                        +"<fisc:DECENDDATE>2020-10-20</fisc:DECENDDATE>"
                        +"<fisc:DETSTARTDATE>20201020161634</fisc:DETSTARTDATE>"
                        +"<fisc:DETENDDATE>20201020161634</fisc:DETENDDATE>"
                        +"<fisc:CPY>1</fisc:CPY>"
                        +"<fisc:IND>0</fisc:IND>"
                        +"<fisc:INVOICES>"
                        +"<fisc:RECORD>"
                        +"<fisc:ITYPE>na</fisc:ITYPE>"
                        +"<fisc:ICODE>na</fisc:ICODE>"
                        +"<fisc:INUM>inum1</fisc:INUM>"
                        +"<fisc:IBPN>111111111</fisc:IBPN>"
                        +"<fisc:INAME>Axis Solutions</fisc:INAME>"
                        +"<fisc:ITAXCODE>VAT</fisc:ITAXCODE>"
                        +"<fisc:VAT>22222222</fisc:VAT>"
                        +"<fisc:IADDRESS>14 Arundel Road,Alexandra Park,Harare</fisc:IADDRESS>"
                        +"<fisc:ICONTACT>na</fisc:ICONTACT>"
                        +"<fisc:ISHORTNAME>na</fisc:ISHORTNAME>"
                        +"<fisc:IPAYER>AxisDeveloper</fisc:IPAYER>"
                        +"<fisc:IPVAT>11111</fisc:IPVAT>"
                        +"<fisc:IPADDRESS>14, Arundel Road, Alexandra Park, Harare</fisc:IPADDRESS>"
                        +"<fisc:IPTEL>04745650</fisc:IPTEL>"
                        +"<fisc:IPBPN>111111111</fisc:IPBPN>"
                        +"<fisc:IAMT>100</fisc:IAMT>"
                        +"<fisc:ICUR>USD</fisc:ICUR>"
                        +"<fisc:ITAX>15</fisc:ITAX>"
                        +"<fisc:ISTATUS>01</fisc:ISTATUS>"
                        +"<fisc:IISSUER>NEILL</fisc:IISSUER>"
                        +"<fisc:IDATE>2017-01-31</fisc:IDATE>"
                        +"<fisc:ITAXCTRL>na</fisc:ITAXCTRL>"
                        +"<fisc:IOCODE>na</fisc:IOCODE>"
                        +"<fisc:IONUM>na</fisc:IONUM>"
                        +"<fisc:IREMARK>test</fisc:IREMARK>"
                        +"<fisc:Lattitude>0</fisc:Lattitude>"
                        +"<fisc:Longitude>0</fisc:Longitude>"
                        +"<fisc:ITEMS>"
                        +"<fisc:ITEM>"
                        +"<fisc:HH>1</fisc:HH>"
                        +"<fisc:ITEMCODE>456</fisc:ITEMCODE>"
                        +"<fisc:ITEMNAME1>Paper</fisc:ITEMNAME1>"
                        +"<fisc:ITEMNAME2>Paper</fisc:ITEMNAME2>"
                        +"<fisc:QTY>10</fisc:QTY>"
                        +"<fisc:PRICE>2</fisc:PRICE>"
                        +"<fisc:AMT>20</fisc:AMT>"
                        +"<fisc:CUR>USD</fisc:CUR>"
                        +"<fisc:TAX>3</fisc:TAX>"
                        +"<fisc:TAXR>0.15</fisc:TAXR>"
                        +"</fisc:ITEM>"
                        +"<fisc:ITEM>"
                        +"<fisc:HH>2</fisc:HH>"
                        +"<fisc:ITEMCODE>1006</fisc:ITEMCODE>"
                        +"<fisc:ITEMNAME1>Paper Clip</fisc:ITEMNAME1>"
                        +"<fisc:ITEMNAME2>Paper</fisc:ITEMNAME2>"
                        +"<fisc:QTY>1</fisc:QTY>"
                        +"<fisc:PRICE>80</fisc:PRICE>"
                        +"<fisc:AMT>80</fisc:AMT>"
                        +"<fisc:CUR>USD</fisc:CUR>"
                        +"<fisc:TAX>12</fisc:TAX>"
                        +"<fisc:TAXR>0.15</fisc:TAXR>"
                        +"</fisc:ITEM>"
                        +"</fisc:ITEMS>"
                        +"</fisc:RECORD>"
                        +"</fisc:INVOICES>"
                        +"</fisc:INVOICE>"
                        +"</tem:ZimraSubmitInvoices>"
                        +"</soapenv:Body>"
                        +"</soapenv:Envelope>";



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
            System.out.println("RESPONSE: "+"PostExecute");

            System.out.println("RESPONSE: "+result);

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




    public String RevMaxPortalSend(){

        //post client visit
        try {

            //postToast(String.valueOf(cr.getCount()));
            JSONObject InvoiceJson = DevicejsonObject;

            InvoiceJson.put("InvoiceNumber",MainActivity.utilitiesClass.GetDateTime());
            InvoiceJson.put( "IMEIMAC","abc1234efg");
            InvoiceJson.put("Branch","Harare");
            InvoiceJson.put("IAddress","14 Arundel Road, Alexandra Park");
            InvoiceJson.put("InvoiceCurrency","usd");
            InvoiceJson.put("IAMT","40.08");
            InvoiceJson.put("InvoiceBPN","111111111");
            InvoiceJson.put("InvoiceCode","12");
            InvoiceJson.put( "InvoiceContact","Axis Accounts");
            InvoiceJson.put( "InvoiceDate",MainActivity.utilitiesClass.GetDateTime());
            InvoiceJson.put( "InvoiceIssuer","Cashier");
            InvoiceJson.put("InvoiceName","Axis Invoice");
            InvoiceJson.put( "IOCode","123");
            InvoiceJson.put("IONumber","345");
            InvoiceJson.put(  "IPAddress","12 Test Avenue, Harare, Zimbabwe");
            InvoiceJson.put(   "InvoicePayer","Test Company");
            InvoiceJson.put("InvoicePayerBPN","123123123");
            InvoiceJson.put(   "InvoicePayerVAT","23423423");
            InvoiceJson.put("InvoicePayerTel","+263778581173");
            InvoiceJson.put(    "InvoiceRemark","None");
            InvoiceJson.put(   "InvoiceShortName","TectCo");
            InvoiceJson.put(   "InvoiceStatus","01");
            InvoiceJson.put(    "InvoiceTax","5.08");
            InvoiceJson.put(    "InvoiceTaxCTRL","1");
            InvoiceJson.put("InvoiceType","1");
            InvoiceJson.put("products","");
            JSONArray productsArray = new JSONArray();
            JSONObject elementi= new JSONObject();
            elementi.put("ItemCode", "12");
            elementi.put("ItemName1", "test product");
            elementi.put("Price", "35.00");
            elementi.put("Quantity", "1");
            elementi.put("Tax", "5.08");
            elementi.put("TaxR", "0.145");
            elementi.put("Amount", "40.08");
            productsArray.put(elementi);
            InvoiceJson.put("products", productsArray);
            JSONArray currencyArray = new JSONArray();
            JSONObject currency= new JSONObject();
            currency.put("Name", "USD");
            currency.put("Amount", "41");
            currencyArray.put(currency);
           // currency= new JSONObject();
            //currency.put("Name", "ZWL");
            //currency.put("Amount", "250");
            //currencyArray.put(currency);
            //add currencies
            InvoiceJson.put("currency",currencyArray);

            //changed to timout to cater for sql reserverd word

            PostDataResponse = PostData("kk", InvoiceJson, "kj");

            //postToast("Cliviz"+PostDataResponse);
            System.out.println("Cliviz" + PostDataResponse);
           // return "Cliviz" + PostDataResponse;


            if (PostDataResponse.contains("success")) {
                //postToast("Visit Uploaded");
                return String.valueOf(UtilitiesClass.SuccessResult);
                //set synced to true on post success;
                //myDB.SetSyncedtoTrue(ChekStart.tblClientVisits,Systatd, "ID", cr.getString(cr.getColumnIndex("ID")));
            } else {
                //postToast("Visit Upload Failed");
                return PostDataResponse;
            }




        }
        catch (JSONException e){
            //postToast("Visit Upload Error"+ e.getMessage().toString().trim());
            Crashlytics.logException(e);
            //stopService(new Intent(getBaseContext(), SyncData.class));
            return e.getMessage();
        }
    }

    //post sync request to restapi
    public String PostData (String EndPointName, JSONObject DataToPost, String URLToPost){
        String result="";

        try {

            //System.out.println("URL SENT"+URLToPost+EndpointPrecedent+EndPointName);


            //define URL and content type
            //URL url = new URL(URLToPost+EndpointPrecedent+EndPointName);
            //URL url = new URL("http://axiscare.co.zw:8001/ZimraWeb/api/updateAccountProducts");
            URL url = new URL("http://axiscare.co.zw:8001/ZimraWeb/api/updateAccountProducts");
            URLConnection connection = url.openConnection();
            HttpURLConnection httpConn = (HttpURLConnection) connection;
            //httpConn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            //httpConn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
            httpConn.setRequestMethod("POST");
            httpConn.setDoOutput(true);

            //send JsonObject to REST API
            OutputStream os = httpConn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(DataToPost.toString());
            Log.i("JSONINfo", DataToPost.toString());
            writer.flush();
            writer.close();
            os.close();

            //Connect to URL and get response
            httpConn.connect();
            String httpConnResponsemessage = httpConn.getResponseMessage().toString();
            System.out.println("http connection status :" + httpConnResponsemessage);

            InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
            BufferedReader in = new BufferedReader(isr);
            String inputLine;
            while ((inputLine = in.readLine()) != null){
                System.out.println("response"+inputLine);
                result+=inputLine;
            }
            return result;

        }
        catch(java.net.MalformedURLException e) {
            System.out.println("url error"+e.getStackTrace().toString().trim());
            //postToast("Url Error");
            Crashlytics.logException(e);
            result = e.getStackTrace().toString().trim();
            return result;
        } catch (IOException e) {
            System.out.println("Send error " +e.toString().toString());
           // postToast("Sync Failed, try again later: ");
            Crashlytics.logException(e);
            result= e.getStackTrace().toString().trim();
            return result;
        }
    }
}
