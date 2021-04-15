/*
 * Developed by Mqondisi Ndlovu Copyright (c) 2021. contact @ 0772783880
 */

package com.axis.revmaxinterface;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Environment;
import android.util.Xml;

import com.crashlytics.android.Crashlytics;

import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.axis.revmaxinterface.MainActivity.TaxConfigFlag;

public class UtilitiesClass {

    static int SuccessResult=0;
    static int FailResult=1;
    static String appendFORLog ="RVI263";
    MyDB myDB;
    // variable to hold context
    private Context context;

    //save the context received via constructor in a local variable
    public UtilitiesClass(Context context){
        this.context=context;
        myDB=new MyDB(context);

    }

    public int DoubleValidator(double value){
        try{
            value = value*1;
            return SuccessResult;
        }
        catch(Exception e){
            return FailResult;
        }
    }

    public int IntegerValidator(int value){
        try{
            value = value*1;
            return SuccessResult;
        }
        catch(Exception e){
            return FailResult;
        }
    }

    public  int NullValidator(Object object ){

        if(object==null){
            return FailResult;
        }
        else{
            return SuccessResult;
        }
    }

    public String TaxCalculator(String price,String quantity,int TaxConfigFlag,String VatRate){
        String taxValue;
        double linetotal;
        double TaxCalculated=0;

        linetotal= Double.parseDouble( price)*Double.parseDouble(quantity);
//        System.out.println("Cliviz" + String.valueOf(linetotal));

        //tax inclusive calculation (get tax from total amount)
        if (TaxConfigFlag==0){
            TaxCalculated = linetotal -(linetotal/(1+Double.parseDouble(VatRate)));
        }

        //tax exclusive add tax to current amount
        else if(TaxConfigFlag==1){
            TaxCalculated = linetotal*Double.parseDouble(VatRate);
        }
//        System.out.println("Cliviz" + String.valueOf(TaxCalculated));
        taxValue = CurrencyRounder(String.valueOf(TaxCalculated));
        return taxValue;
    }

    public String ComputeInvoiceTax(ArrayList<Items> itemsArrayList,int TaxConfigFlag){
        String InvoiceTaxTotal;
        double taxtotal=0;

        for(Items item:itemsArrayList){
            taxtotal += Double.parseDouble(TaxCalculator(item.getPRICE(),item.getQTY(),TaxConfigFlag,item.getTAXR()));
        }
        InvoiceTaxTotal = CurrencyRounder(String.valueOf(taxtotal));
        return InvoiceTaxTotal;
    }

    public String ComputeInvoiceTotal(ArrayList<Items> itemsArrayList,String InvoiceTaxAmount,int TaxConfigFLag){
        String InvoiceTotal;
        double invoicetotal=0;
        String qty="";
        for(Items item:itemsArrayList){
            if (item.getAMT().contains("-")){
                qty="-";
            }
            invoicetotal+= Double.parseDouble(ItemTotalCalculator(item.getPRICE(),qty+item.getQTY()));
        }

        //vat exclusive, add tax to invoice total
        if(TaxConfigFLag==1){
            invoicetotal+= Double.parseDouble(InvoiceTaxAmount);
        }

        InvoiceTotal = CurrencyRounder(String.valueOf(invoicetotal));
        return InvoiceTotal;

    }

    public ArrayList<VatCalculated> RatioBasedTaxCalcuator(String taxAmount, String invoiceBaseCurrencyTotalPaid, ArrayList<Currencies> CurrenciesTendered)
    {
        //list to return vat due
        ArrayList<VatCalculated> vatcalculated = new ArrayList<VatCalculated>();

        //for statement to calculate vat due for each currency
        for (Currencies currencytendered : CurrenciesTendered)
        {
            //get currency amount in base currency value
            double basecurrencyamount = Double.parseDouble(currencytendered.getAmount()) / Double.parseDouble(currencytendered.getRate());
            //get ratio of that currency against total paid
            double ratioforcurrency = basecurrencyamount / Double.parseDouble(invoiceBaseCurrencyTotalPaid);
            //calculate vat amount due from that currency in base currency
            double vatamountdue = ratioforcurrency * Double.parseDouble(taxAmount);
            //calculate vat due in that currency based on rate
            double vatamountdueforcurrency = vatamountdue * Double.parseDouble(currencytendered.getRate());

//            System.out.println(currencytendered.getName());
//            System.out.println((CurrencyRounder(String.valueOf(vatamountdueforcurrency))));

            VatCalculated newVatDue = new VatCalculated();
            newVatDue.setCurrency(currencytendered.getName());
            newVatDue.setVatAmount((CurrencyRounder(String.valueOf(vatamountdueforcurrency))));
            vatcalculated.add(newVatDue);
        }
        return vatcalculated;
    }

    public String CurrencyRounder(String AmounttoRound){
        double roundedAmount;
        String roundedAmountString;

        String[] div =AmounttoRound.split("\\.");

        if( div[1].length()>3){
            //first round rounding to correct precision digits problem
            BigDecimal bigdecimal = new BigDecimal(AmounttoRound);
            bigdecimal=bigdecimal.setScale(3, BigDecimal.ROUND_HALF_UP);
            NumberFormat numberFormat = NumberFormat.getInstance();
            double newAmount = bigdecimal.doubleValue();
            roundedAmountString= numberFormat.format(newAmount);
            roundedAmountString=roundedAmountString.replace(",", "");
            roundedAmountString=roundedAmountString.trim();

            //2nd round rounding to get 2 dp
            bigdecimal = new BigDecimal(roundedAmountString);
            bigdecimal=bigdecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
            numberFormat = NumberFormat.getInstance();
            newAmount = bigdecimal.doubleValue();
            roundedAmountString= numberFormat.format(newAmount);
            roundedAmountString=roundedAmountString.replace(",", "");
            roundedAmountString=roundedAmountString.trim();

            return roundedAmountString;
        }
        else{
            BigDecimal bigdecimal = new BigDecimal(AmounttoRound);
            bigdecimal=bigdecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
            NumberFormat numberFormat = NumberFormat.getInstance();
            double newAmount = bigdecimal.doubleValue();
            roundedAmountString= numberFormat.format(newAmount);
            roundedAmountString=roundedAmountString.replace(",", "");
            roundedAmountString=roundedAmountString.trim();
            return roundedAmountString;
        }
    }

    public String InvoiceIDGenerator(){
        String InvoiceNumber;
        InvoiceNumber= GetDateTime();
        return InvoiceNumber;
    }

    public String ItemTotalCalculator(String Price,String Quantity){
        String ItemTotal;
        double linetotal;

        linetotal= Double.parseDouble(Price)*Double.parseDouble(Quantity);
        ItemTotal = CurrencyRounder(String.valueOf(linetotal));
        return ItemTotal;
    }

    public String GetDateTime (){
        String datetime;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        datetime = df.format(c.getTime());

        return datetime;
    }

    public String GetDateNoTime (){
        String datetime;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        datetime = df.format(c.getTime());

        return datetime;
    }

    public String MemoryUsagePercentage(int currentSize,int TotalSize){

        double result;
        double resultpercentage;
        result = Double.parseDouble(String.valueOf(currentSize))/Double.parseDouble(String.valueOf(TotalSize));
        resultpercentage = result*100;
        return  CurrencyRounder(String.valueOf(resultpercentage));
    }

    public String SoapGenerateXML(ArrayList<Invoice>invoiceList,ArrayList<Items> itemsList, ArrayList<Currencies> currenciesList) throws IOException {

        //TODO check why XML is populate with tag annotation
        String XMLFile = "";
        StringWriter writer = new StringWriter();
        StringWriter Soapwriter = new StringWriter();
        XmlSerializer serializer = Xml.newSerializer();
        XmlSerializer Soapserializer = Xml.newSerializer();
        String InvoiceTotal="";
        String InvoiceTax="";

        Soapserializer.setOutput(Soapwriter);
        Soapserializer.startDocument(null, null );

        //build invoice related tags here
        for(Invoice invoice:invoiceList){

            ////build invoice SoapXML here
            Soapserializer.startTag("", "soap:sub1");

            //Soapserializer.startTag("xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\" xmlns:fisc=\"http://ZIMRA/FISC\"", "soapenv:Envelope");
            Soapserializer.startTag("", ConstantVariables.Header);
            Soapserializer.endTag("", ConstantVariables.Header);
            Soapserializer.startTag("", "soapenv:Body");
//                Soapserializer.startTag(null, ConstantVariables.Tem);
            Soapserializer.startTag("","tem:ZimraSubmitInvoices");

            Soapserializer.startTag("","fisc:INVOICE");

            Soapserializer.startTag("", ConstantVariables.SBPN);
            Soapserializer.text(invoice.getDeviceBPN());
            Soapserializer.endTag("", ConstantVariables.SBPN);

            Soapserializer.startTag("", ConstantVariables.SCODE);
            Soapserializer.text(invoice.getCODE());
            Soapserializer.endTag("", ConstantVariables.SCODE);

            Soapserializer.startTag("", ConstantVariables.SMACNUM);
            Soapserializer.text(invoice.getMACNUM());
            Soapserializer.endTag("", ConstantVariables.SMACNUM);

            Soapserializer.startTag("", ConstantVariables.SDECSTARTDATE);
            Soapserializer.text(invoice.getDECSTARTDATE());
            Soapserializer.endTag("", ConstantVariables.SDECSTARTDATE);

            Soapserializer.startTag("", ConstantVariables.SDECENDDATE);
            Soapserializer.text(invoice.getDECENDDATE());
            Soapserializer.endTag("", ConstantVariables.SDECENDDATE);


            Soapserializer.startTag("", ConstantVariables.SDETSTARTDATE);
            Soapserializer.text(invoice.getDETSTARTDATE());
            Soapserializer.endTag("", ConstantVariables.SDETSTARTDATE);


            Soapserializer.startTag("", ConstantVariables.SDETENDDATE);
            Soapserializer.text(invoice.getDETENDDATE());
            Soapserializer.endTag("", ConstantVariables.SDETENDDATE);


            Soapserializer.startTag("", ConstantVariables.SCPY);
            Soapserializer.text(invoice.getCPY());
            Soapserializer.endTag("", ConstantVariables.SCPY);


            Soapserializer.startTag("", ConstantVariables.SIND);
            Soapserializer.text(invoice.getIND());
            Soapserializer.endTag("", ConstantVariables.SIND);

            Soapserializer.startTag("","fisc:INVOICES");
            Soapserializer.startTag("","fisc:RECORD");

            Soapserializer.startTag("", ConstantVariables.SITYPE);
            Soapserializer.text(invoice.getITYPE());
            Soapserializer.endTag("", ConstantVariables.SITYPE);

            Soapserializer.startTag("", ConstantVariables.SICODE);
            Soapserializer.text(invoice.getICODE());
            Soapserializer.endTag("", ConstantVariables.SICODE);

            Soapserializer.startTag("", ConstantVariables.SBRANCH);
            Soapserializer.text(invoice.getBRANCH());
            Soapserializer.endTag("", ConstantVariables.SBRANCH);

            Soapserializer.startTag("", ConstantVariables.SINUM);
            Soapserializer.text(invoice.getINUM());
            Soapserializer.endTag("", ConstantVariables.SINUM);

            Soapserializer.startTag("", ConstantVariables.SIBPN);
            Soapserializer.text(invoice.getIBPN());
            Soapserializer.endTag("", ConstantVariables.SIBPN);

            Soapserializer.startTag("", ConstantVariables.SINAME);
            Soapserializer.text(invoice.getINAME());
            Soapserializer.endTag("", ConstantVariables.SINAME);

            Soapserializer.startTag("", ConstantVariables.SVAT);
            Soapserializer.text(invoice.getVAT());
            Soapserializer.endTag("", ConstantVariables.SVAT);

            Soapserializer.startTag("", ConstantVariables.SIADDRESS);
            Soapserializer.text(invoice.getIADDRESS());
            Soapserializer.endTag("", ConstantVariables.SIADDRESS);

            Soapserializer.startTag("", ConstantVariables.SICONTACT);
            Soapserializer.text(invoice.getICONTACT());
            Soapserializer.endTag("", ConstantVariables.SICONTACT);

            Soapserializer.startTag("", ConstantVariables.SISHORTNAME);
            Soapserializer.text(invoice.getISHORTNAME());
            Soapserializer.endTag("", ConstantVariables.SISHORTNAME);

            Soapserializer.startTag("", ConstantVariables.SIPAYER);
            Soapserializer.text(invoice.getIPAYER());
            Soapserializer.endTag("", ConstantVariables.SIPAYER);

            Soapserializer.startTag("", ConstantVariables.SIPVAT);
            Soapserializer.text(invoice.getIPVAT());
            Soapserializer.endTag("", ConstantVariables.SIPVAT);

            Soapserializer.startTag("", ConstantVariables.SIPADDRESS);
            Soapserializer.text(invoice.getIPADDRESS());
            Soapserializer.endTag("", ConstantVariables.SIPADDRESS);

            Soapserializer.startTag("", ConstantVariables.SIPTEL);
            Soapserializer.text(invoice.getIPTEL());
            Soapserializer.endTag("", ConstantVariables.SIPTEL);

            Soapserializer.startTag("", ConstantVariables.SIPBPN);
            Soapserializer.text(invoice.getIPBPN());
            Soapserializer.endTag("", ConstantVariables.SIPBPN);

            Soapserializer.startTag("", ConstantVariables.SICURRENCY);
            Soapserializer.text(invoice.getICURRENCY());
            Soapserializer.endTag("", ConstantVariables.SICURRENCY);

            Soapserializer.startTag("", ConstantVariables.SIAMT);
            Soapserializer.text(invoice.getIAMT());
            InvoiceTotal = invoice.getIAMT();
            Soapserializer.endTag("", ConstantVariables.SIAMT);

            Soapserializer.startTag("", ConstantVariables.SITAX);
            Soapserializer.text(invoice.getITAX());
            InvoiceTax = invoice.getITAX();
            Soapserializer.endTag("", ConstantVariables.SITAX);

            Soapserializer.startTag("", ConstantVariables.SISTATUS);
            Soapserializer.text(invoice.getISTATUS());
            Soapserializer.endTag("", ConstantVariables.SISTATUS);

            Soapserializer.startTag("", ConstantVariables.SIISSUER);
            Soapserializer.text(invoice.getIISSUER());
            Soapserializer.endTag("", ConstantVariables.SIISSUER);

            Soapserializer.startTag("", ConstantVariables.SIDATE);
            Soapserializer.text(invoice.getIDATE());
            Soapserializer.endTag("", ConstantVariables.SIDATE);

            Soapserializer.startTag("", ConstantVariables.SITAXCTRL);
            Soapserializer.text(invoice.getITAXCTRL());
            Soapserializer.endTag("", ConstantVariables.SITAXCTRL);

            Soapserializer.startTag("", ConstantVariables.SIOCODE);
            Soapserializer.text(invoice.getIOCODE());
            Soapserializer.endTag("", ConstantVariables.SIOCODE);

            Soapserializer.startTag("", ConstantVariables.SIONUM);
            Soapserializer.text(invoice.getIONUM());
            Soapserializer.endTag("", ConstantVariables.SIONUM);

            Soapserializer.startTag("", ConstantVariables.SIREMARK);
            Soapserializer.text(invoice.getIREMARK());
            Soapserializer.endTag("", ConstantVariables.SIREMARK);

            //populate items in xml here
            //serializer.startTag("","ITEMS");
            Soapserializer.startTag("","fisc:ITEMS");
            for(Items item:itemsList){
                //serializer.startTag("","ITEM");
                Soapserializer.startTag("","fisc:ITEM");

                ///soap XML  build for items

                Soapserializer.startTag("", ConstantVariables.SHH);
                Soapserializer.text(item.getHH());
                Soapserializer.endTag("", ConstantVariables.SHH);

                Soapserializer.startTag("", ConstantVariables.SITEMCODE);
                Soapserializer.text(item.getITEMCODE());
                Soapserializer.endTag("", ConstantVariables.SITEMCODE);

                Soapserializer.startTag("", ConstantVariables.SITEMNAME1);
                Soapserializer.text(item.getITEMNAME1());
                Soapserializer.endTag("", ConstantVariables.SITEMNAME1);

                Soapserializer.startTag("", ConstantVariables.SITEMNAME2);
                Soapserializer.text("NA");
                Soapserializer.endTag("", ConstantVariables.SITEMNAME2);

                Soapserializer.startTag("", ConstantVariables.SQTY);
                Soapserializer.text(item.getQTY());
                Soapserializer.endTag("", ConstantVariables.SQTY);

                Soapserializer.startTag("", ConstantVariables.SPRICE);
                Soapserializer.text(item.getPRICE());
                Soapserializer.endTag("", ConstantVariables.SPRICE);

                Soapserializer.startTag("", ConstantVariables.SAMT);
                Soapserializer.text(item.getAMT());
                Soapserializer.endTag("", ConstantVariables.SAMT);

                Soapserializer.startTag("", ConstantVariables.STAX);
                Soapserializer.text(item.getTAX());
                Soapserializer.endTag("", ConstantVariables.STAX);

                Soapserializer.startTag("", ConstantVariables.STAXR);
                Soapserializer.text(item.getTAXR());
                Soapserializer.endTag("", ConstantVariables.STAXR);



                Soapserializer.endTag("","fisc:ITEM");
            }

            Soapserializer.endTag("","fisc:ITEMS");

            ArrayList<VatCalculated> vatCalculatedArrayList;
            vatCalculatedArrayList = RatioBasedTaxCalcuator(InvoiceTax,InvoiceTotal,currenciesList);

            PrintoutVatCalculatedList(vatCalculatedArrayList);

            Soapserializer.endTag("","fisc:RECORD");
            Soapserializer.endTag("","fisc:INVOICES");
        }

        Soapserializer.endTag("","fisc:INVOICE");
        Soapserializer.endTag("","tem:ZimraSubmitInvoices");
        //Soapserializer.endTag("", ConstantVariables.Tem);
        //Soapserializer.endTag("", ConstantVariables.Body);
        Soapserializer.endTag("", "soapenv:Body");
        //Soapserializer.endTag("xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\" xmlns:fisc=\"http://ZIMRA/FISC\"", "soapenv:Envelope");

        //serializer.endDocument();
        Soapserializer.endDocument();
        Soapserializer.flush();

//        System.out.println(appendFORLog+"123 "+writer.toString());
//        System.out.println("RESPONSE: "+Soapwriter.toString());

        return Soapwriter.toString();
    }

    public String GenerateXML(ArrayList<Invoice>invoiceList,ArrayList<Items> itemsList, ArrayList<Currencies> currenciesList) throws IOException {

        //TODO check why XML is populate with tag annotation
        String XMLFile = "";
        StringWriter writer = new StringWriter();
        StringWriter Soapwriter = new StringWriter();
        XmlSerializer serializer = Xml.newSerializer();
        XmlSerializer Soapserializer = Xml.newSerializer();
        String InvoiceTotal="";
        String InvoiceTax="";

        serializer.setOutput(writer);
        //Soapserializer.setOutput(Soapwriter);
        serializer.startDocument(null, null );
        //Soapserializer.startDocument(null, null );

        serializer.startTag("http://tempuri.org/", "ZimraSubmitInvoices");
        serializer.startTag("http://ZIMRA/FISC","INVOICE");


        //build invoice related tags here
        for(Invoice invoice:invoiceList){
            serializer.startTag("", ConstantVariables.BPN);
            serializer.text(invoice.getDeviceBPN());
            serializer.endTag("", ConstantVariables.BPN);

            serializer.startTag("", ConstantVariables.CODE);
            serializer.text(invoice.getCODE());
            serializer.endTag("", ConstantVariables.CODE);

            serializer.startTag("", ConstantVariables.MACNUM);
            serializer.text(invoice.getMACNUM());
            serializer.endTag("", ConstantVariables.MACNUM);

            serializer.startTag("", ConstantVariables.DECSTARTDATE);
            serializer.text(invoice.getDECSTARTDATE());
            serializer.endTag("", ConstantVariables.DECSTARTDATE);

            serializer.startTag("", ConstantVariables.DECENDDATE);
            serializer.text(invoice.getDECENDDATE());
            serializer.endTag("", ConstantVariables.DECENDDATE);


            serializer.startTag("", ConstantVariables.DETSTARTDATE);
            serializer.text(invoice.getDETSTARTDATE());
            serializer.endTag("", ConstantVariables.DETSTARTDATE);


            serializer.startTag("", ConstantVariables.DETENDDATE);
            serializer.text(invoice.getDETENDDATE());
            serializer.endTag("", ConstantVariables.DETENDDATE);


            serializer.startTag("", ConstantVariables.CPY);
            serializer.text(invoice.getCPY());
            serializer.endTag("", ConstantVariables.CPY);


            serializer.startTag("", ConstantVariables.IND);
            serializer.text(invoice.getIND());
            serializer.endTag("", ConstantVariables.IND);

            serializer.startTag("","INVOICES");
            serializer.startTag("","RECORD");

            serializer.startTag("", ConstantVariables.ITYPE);
            serializer.text(invoice.getITYPE());
            serializer.endTag("", ConstantVariables.ITYPE);

            serializer.startTag("", ConstantVariables.ICODE);
            serializer.text(invoice.getICODE());
            serializer.endTag("", ConstantVariables.ICODE);

            serializer.startTag("", ConstantVariables.BRANCH);
            serializer.text(invoice.getBRANCH());
            serializer.endTag("", ConstantVariables.BRANCH);

            serializer.startTag("", ConstantVariables.INUM);
            serializer.text(invoice.getINUM());
            serializer.endTag("", ConstantVariables.INUM);

            serializer.startTag("", ConstantVariables.IBPN);
            serializer.text(invoice.getIBPN());
            serializer.endTag("", ConstantVariables.IBPN);

            serializer.startTag("", ConstantVariables.INAME);
            serializer.text(invoice.getINAME());
            serializer.endTag("", ConstantVariables.INAME);

            serializer.startTag("", ConstantVariables.VAT);
            serializer.text(invoice.getVAT());
            serializer.endTag("", ConstantVariables.VAT);

            serializer.startTag("", ConstantVariables.IADDRESS);
            serializer.text(invoice.getIADDRESS());
            serializer.endTag("", ConstantVariables.IADDRESS);

            serializer.startTag("", ConstantVariables.ICONTACT);
            serializer.text(invoice.getICONTACT());
            serializer.endTag("", ConstantVariables.ICONTACT);

            serializer.startTag("", ConstantVariables.ISHORTNAME);
            serializer.text(invoice.getISHORTNAME());
            serializer.endTag("", ConstantVariables.ISHORTNAME);

            serializer.startTag("", ConstantVariables.IPAYER);
            serializer.text(invoice.getIPAYER());
            serializer.endTag("", ConstantVariables.IPAYER);

            serializer.startTag("", ConstantVariables.IPVAT);
            serializer.text(invoice.getIPVAT());
            serializer.endTag("", ConstantVariables.IPVAT);

            serializer.startTag("", ConstantVariables.IPADDRESS);
            serializer.text(invoice.getIPADDRESS());
            serializer.endTag("", ConstantVariables.IPADDRESS);

            serializer.startTag("", ConstantVariables.IPTEL);
            serializer.text(invoice.getIPTEL());
            serializer.endTag("", ConstantVariables.IPTEL);

            serializer.startTag("", ConstantVariables.IPBPN);
            serializer.text(invoice.getIPBPN());
            serializer.endTag("", ConstantVariables.IPBPN);

            serializer.startTag("", ConstantVariables.ICURRENCY);
            serializer.text(invoice.getICURRENCY());
            serializer.endTag("", ConstantVariables.ICURRENCY);

            serializer.startTag("", ConstantVariables.IAMT);
            serializer.text(invoice.getIAMT());
            InvoiceTotal = invoice.getIAMT();
            serializer.endTag("", ConstantVariables.IAMT);

            serializer.startTag("", ConstantVariables.ITAX);
            serializer.text(invoice.getITAX());
            InvoiceTax = invoice.getITAX();
            serializer.endTag("", ConstantVariables.ITAX);

            serializer.startTag("", ConstantVariables.ISTATUS);
            serializer.text(invoice.getISTATUS());
            serializer.endTag("", ConstantVariables.ISTATUS);

            serializer.startTag("", ConstantVariables.IISSUER);
            serializer.text(invoice.getIISSUER());
            serializer.endTag("", ConstantVariables.IISSUER);

            serializer.startTag("", ConstantVariables.IDATE);
            serializer.text(invoice.getIDATE());
            serializer.endTag("", ConstantVariables.IDATE);

            serializer.startTag("", ConstantVariables.ITAXCTRL);
            serializer.text(invoice.getITAXCTRL());
            serializer.endTag("", ConstantVariables.ITAXCTRL);

            serializer.startTag("", ConstantVariables.IOCODE);
            serializer.text(invoice.getIOCODE());
            serializer.endTag("", ConstantVariables.IOCODE);

            serializer.startTag("", ConstantVariables.IONUM);
            serializer.text(invoice.getIONUM());
            serializer.endTag("", ConstantVariables.IONUM);

            serializer.startTag("", ConstantVariables.IREMARK);
            serializer.text(invoice.getIREMARK());
            serializer.endTag("", ConstantVariables.IREMARK);

            serializer.startTag("","ITEMS");
            //Soapserializer.startTag("","fisc:ITEMS");
            for(Items item:itemsList){
                serializer.startTag("","ITEM");
                //Soapserializer.startTag("","fisc:ITEM");

                serializer.startTag("", ConstantVariables.HH);
                serializer.text(item.getHH());
                serializer.endTag("", ConstantVariables.HH);

                serializer.startTag("", ConstantVariables.ITEMCODE);
                serializer.text(item.getITEMCODE());
                serializer.endTag("", ConstantVariables.ITEMCODE);

                serializer.startTag("", ConstantVariables.ITEMNAME1);
                serializer.text(item.getITEMNAME1());
                serializer.endTag("", ConstantVariables.ITEMNAME1);

                serializer.startTag("", ConstantVariables.ITEMNAME2);
                serializer.text("Magetsi");
                serializer.endTag("", ConstantVariables.ITEMNAME2);

                serializer.startTag("", ConstantVariables.QTY);
                serializer.text(item.getQTY());
                serializer.endTag("", ConstantVariables.QTY);

                serializer.startTag("", ConstantVariables.PRICE);
                serializer.text(item.getPRICE());
                serializer.endTag("", ConstantVariables.PRICE);

                serializer.startTag("", ConstantVariables.AMT);
                serializer.text(item.getAMT());
                serializer.endTag("", ConstantVariables.AMT);

                serializer.startTag("", ConstantVariables.TAX);
                serializer.text(item.getTAX());
                serializer.endTag("", ConstantVariables.TAX);

                serializer.startTag("", ConstantVariables.TAXR);
                serializer.text(item.getTAXR());
                serializer.endTag("", ConstantVariables.TAXR);

                serializer.endTag("","ITEM");
                //Soapserializer.endTag("","fisc:ITEM");
            }
            serializer.endTag("","ITEMS");
            //Soapserializer.endTag("","fisc:ITEMS");


            //populate invoice currencies here
            serializer.startTag("","CurrenciesReceived");
            for(Currencies currency:currenciesList){
                serializer.startTag("","Currency");

                serializer.startTag("", ConstantVariables.Name);
                serializer.text(currency.getName());
                serializer.endTag("", ConstantVariables.Name);

                serializer.startTag("", ConstantVariables.Amount);
                serializer.text(currency.getAmount());
                serializer.endTag("", ConstantVariables.Amount);

                serializer.startTag("", ConstantVariables.Rate);
                serializer.text(currency.getRate());
                serializer.endTag("", ConstantVariables.Rate);

                serializer.endTag("","Currency");
            }
            serializer.endTag("","CurrenciesReceived");


            //calculate vat ratios here based on currencies listing
            ArrayList<VatCalculated> vatCalculatedArrayList;
            vatCalculatedArrayList = RatioBasedTaxCalcuator(InvoiceTax,InvoiceTotal,currenciesList);

            PrintoutVatCalculatedList(vatCalculatedArrayList);

            //add vat ratio here
            serializer.startTag("","VATRATIO");
            for(VatCalculated vatCalculated: vatCalculatedArrayList){
                serializer.startTag("","RATIO");
//
                serializer.startTag("", ConstantVariables.Name);
                serializer.text(vatCalculated.getCurrency());
                serializer.endTag("", ConstantVariables.Name);
//
                serializer.startTag("", ConstantVariables.Amount);
                serializer.text(vatCalculated.getVatAmount());
                serializer.endTag("", ConstantVariables.Amount);

                serializer.endTag("","RATIO");
            }
            serializer.endTag("","VATRATIO");

            serializer.endTag("","RECORD");
            serializer.endTag("","INVOICES");
        }

        serializer.endTag("http://ZIMRA/FISC","INVOICE");
        serializer.endDocument();
        //Soapserializer.endDocument();
        //Soapserializer.flush();

//        System.out.println(appendFORLog+"123 "+writer.toString());
        //System.out.println("RESPONSE: "+Soapwriter.toString());

        return writer.toString();

    }

    public int SaveInvoiceDetail(ArrayList<Invoice> invoiceArrayList,ArrayList<Items> itemsArrayList,ArrayList<Currencies> currenciesArrayList){

        String InvoiceTotal="";
        String InvoiceTax="";
        String InvoiceNumber="";
        String ID = InvoiceIDGenerator();

        try {
            //save invoice detail to db here
            for(Invoice invoice:invoiceArrayList){
                InvoiceTotal = invoice.getIAMT();
                InvoiceTax = invoice.getITAX();
                InvoiceNumber = invoice.getINUM();

                myDB.InsertInvoices(ID,invoice.getDeviceBPN() ,invoice.getCODE(),invoice.getMACNUM(),invoice.getDECSTARTDATE(),invoice.getDECENDDATE(),invoice.getDETSTARTDATE(),invoice.getDETENDDATE(),invoice.getCPY(),invoice.getIND(),invoice.getITYPE(),invoice.getICODE(),invoice.getVAT(),invoice.getBRANCH(),invoice.getINUM(),invoice.getIBPN(),invoice.getINAME(),invoice.getIADDRESS(),invoice.getICONTACT(),invoice.getISHORTNAME(),invoice.getIPAYER(),invoice.getIPVAT(),invoice.getIPADDRESS(),invoice.getIPTEL(),invoice.getIPBPN(),invoice.getICURRENCY(),invoice.getIAMT(),invoice.getITAX(),invoice.getISTATUS(),invoice.getIISSUER(),invoice.getIDATE(),invoice.getITAXCTRL(),invoice.getIOCODE(),invoice.getIONUM(),invoice.getIREMARK(),"","");

            }

            //populate items in xml here
            //save in items db
            for(Items item:itemsArrayList){

                myDB.InsertInvoiceItems(ID,item.getHH(), item.getITEMCODE(),item.getITEMNAME1(),item.getITEMNAME2(),item.getQTY(),item.getPRICE(),item.getAMT(),item.getTAX(),item.getTAXR(),InvoiceNumber);

            }

            //save invoice currencies here
            for(Currencies currency:currenciesArrayList){

                myDB.InsertInvoiceCurrency(ID,currency.getName(),currency.getAmount(),currency.getRate(),InvoiceNumber);

            }



            //save vat ratio here
            ArrayList<VatCalculated> vatCalculatedArrayList;
            vatCalculatedArrayList = RatioBasedTaxCalcuator(InvoiceTax,InvoiceTotal,currenciesArrayList);
            for(VatCalculated vatCalculated: vatCalculatedArrayList){

                myDB.InsertVatCalculated(ID,vatCalculated.getCurrency(),vatCalculated.getVatAmount(),InvoiceNumber);
            }
            return SuccessResult;
        }
        catch(Exception g){
            Crashlytics.log("Save Transaction Failure, Invoice: "+ InvoiceNumber+": "+MainActivity.compname +" "+ MainActivity.serial +" " +MainActivity.bpn + " "+MainActivity.vat+ " " +MainActivity.currentSize/MainActivity.totalCapacity + " " + MainActivity.regn+ " " +MainActivity.datetime+ " " +MainActivity.firmWareID + " "+MainActivity.wormVersion);
            Crashlytics.logException(g);
            return FailResult;
        }
    }

    @TargetApi(21)
    public int ReadTaxConfiguration() throws IOException {

        //Method to get SD card Path, only works on API 21 and above
        String removableStoragePath = "";
        File fileList[] = new File("/storage/").listFiles();
        for (File file : fileList) {
            try {
                if (Environment.isExternalStorageRemovable(file)) {
                    removableStoragePath = file.getAbsolutePath();
                }
            }
            catch (Exception g){
                continue;
            }
        }
        File file = new File(removableStoragePath+ "/"+ ConstantVariables.configFile );

//        System.out.println("RevError"+file.getAbsolutePath());
        String lineBeingRead;
        BufferedReader br = null;

        /// try {

        br = new BufferedReader(new FileReader(file));
        while ((lineBeingRead =  br.readLine()) != null) {

            if (lineBeingRead.contains("VatFlag:"))
            {

                String vatFlag = lineBeingRead.substring(lineBeingRead.lastIndexOf(":") + 1, lineBeingRead.length()).trim();

                if(IntegerValidator(Integer.parseInt(vatFlag))!=SuccessResult){
                    return FailResult;
                }
                else{
                    TaxConfigFlag= Integer.parseInt(vatFlag);
                }

            }
            else if (lineBeingRead.contains("VatA:"))
            {
                String vatA = lineBeingRead.substring(lineBeingRead.lastIndexOf(":") + 1, lineBeingRead.length()).trim();
                if(DoubleValidator(Double.parseDouble(vatA))!=SuccessResult){
                    return FailResult;
                }
                else{
                    MainActivity.TaxesList.add(vatA);
//                    System.out.println("RevError"+vatA);
                }
            }
            else if (lineBeingRead.contains("VatB:"))
            {
                String vatB = lineBeingRead.substring(lineBeingRead.lastIndexOf(":") + 1, lineBeingRead.length()).trim();
                if(DoubleValidator(Double.parseDouble(vatB))!=SuccessResult){
                    return FailResult;
                }
                else{
                    MainActivity.TaxesList.add(vatB);
                }
            }
            else if (lineBeingRead.contains("VatC:"))
            {
                String vatC = lineBeingRead.substring(lineBeingRead.lastIndexOf(":") + 1, lineBeingRead.length()).trim();
                if(DoubleValidator(Double.parseDouble(vatC))!=SuccessResult){
                    return FailResult;
                }
                else{
                    MainActivity.TaxesList.add(vatC);
                }
            }
            else if (lineBeingRead.contains("VatD:"))
            {
                String vatD = lineBeingRead.substring(lineBeingRead.lastIndexOf(":") + 1, lineBeingRead.length()).trim();
                if(DoubleValidator(Double.parseDouble(vatD))!=SuccessResult){
                    return FailResult;
                }
                else{
                    MainActivity.TaxesList.add(vatD);
                }
            }
            else if (lineBeingRead.contains("VatE:"))
            {
                String vatE = lineBeingRead.substring(lineBeingRead.lastIndexOf(":") + 1, lineBeingRead.length()).trim();
                if(DoubleValidator(Double.parseDouble(vatE))!=SuccessResult){
                    return FailResult;
                }
                else{
                    MainActivity.TaxesList.add(vatE);
                }
            }
            else if (lineBeingRead.contains("VatF:"))
            {
                String vatF = lineBeingRead.substring(lineBeingRead.lastIndexOf(":") + 1, lineBeingRead.length()).trim();
                if(DoubleValidator(Double.parseDouble(vatF))!=SuccessResult){
                    return FailResult;
                }
                else{
                    MainActivity.TaxesList.add(vatF);
                }
            }
        }

        if(MainActivity.TaxesList.size()>0){
            return SuccessResult;
        }
        else{
            return FailResult;
        }
    }

    //PRINTS ALL INVOICE OBJECTS HELD IN AN ARRAY LIST
    public void PrintOutInvoiceList(ArrayList<Invoice> InvoiceObjectArrayList){

        for(Invoice invoice: InvoiceObjectArrayList){
            System.out.println("=======================================");
            System.out.println(appendFORLog +invoice.getDeviceBPN());
            System.out.println( appendFORLog +invoice.getCODE());
            System.out.println( appendFORLog +invoice.getMACNUM());
            System.out.println( appendFORLog +invoice.getDECSTARTDATE());
            System.out.println( appendFORLog +invoice.getDECENDDATE());
            System.out.println( appendFORLog +invoice.getDETSTARTDATE());
            System.out.println( appendFORLog +invoice.getDETENDDATE());
            System.out.println( appendFORLog +invoice.getCPY());
            System.out.println( appendFORLog +invoice.getIND());
            System.out.println( appendFORLog +invoice.getITYPE());
            System.out.println( appendFORLog +invoice.getICODE());
            System.out.println( appendFORLog +invoice.getBRANCH());
            System.out.println( appendFORLog +invoice.getINUM());
            System.out.println( appendFORLog +invoice.getIBPN());
            System.out.println( appendFORLog +invoice.getVAT());
            System.out.println( appendFORLog +invoice.getINAME());
            System.out.println( appendFORLog +invoice.getIADDRESS());
            System.out.println( appendFORLog +invoice.getICONTACT());
            System.out.println( appendFORLog +invoice.getISHORTNAME());
            System.out.println( appendFORLog +invoice.getIPAYER());
            System.out.println( appendFORLog +invoice.getIPVAT());
            System.out.println( appendFORLog +invoice.getIPADDRESS());
            System.out.println( appendFORLog +invoice.getIPTEL());
            System.out.println( appendFORLog +invoice.getIPBPN());
            System.out.println( appendFORLog +invoice.getICURRENCY());
            System.out.println( appendFORLog +invoice.getIAMT());
            System.out.println( appendFORLog +invoice.getITAX());
            System.out.println( appendFORLog +invoice.getISTATUS());
            System.out.println( appendFORLog +invoice.getIISSUER());
            System.out.println( appendFORLog +invoice.getIDATE());
            System.out.println( appendFORLog +invoice.getITAXCTRL());
            System.out.println( appendFORLog +invoice.getIOCODE());
            System.out.println( appendFORLog +invoice.getIONUM());
            System.out.println( appendFORLog +invoice.getIREMARK());
            System.out.println( appendFORLog +invoice.getITEMSXML());
            System.out.println( appendFORLog +invoice.getCurrenciesReceivedXML());
        }
    }

    public void PrintoutItemsList(ArrayList<Items> ItemsObjectArrayList){
        for(Items items: ItemsObjectArrayList){
            System.out.println("================================");
            System.out.println(appendFORLog +items.getHH());
            System.out.println(appendFORLog +items.getITEMCODE());
            System.out.println(appendFORLog +items.getITEMNAME1());
            System.out.println(appendFORLog +items.getITEMNAME2());
            System.out.println(appendFORLog +items.getQTY());
            System.out.println(appendFORLog +items.getPRICE());
            System.out.println(appendFORLog +items.getAMT());
            System.out.println(appendFORLog +items.getTAX());
            System.out.println(appendFORLog +items.getTAXR());
        }
    }

    public void PrintoutCurrenciesList(ArrayList<Currencies> CurrencyObjectArrayList){

        for(Currencies currency: CurrencyObjectArrayList){
            System.out.println("================================");
            System.out.println(appendFORLog +currency.getName());
            System.out.println(appendFORLog +currency.getAmount());
            System.out.println(appendFORLog +currency.getRate());
        }
    }

    public void PrintoutVatCalculatedList(ArrayList<VatCalculated> VatCalculatedObjectArrayList){

        for(VatCalculated vatCalculated: VatCalculatedObjectArrayList){
            System.out.println("================================");
            System.out.println(appendFORLog+"VC" +vatCalculated.getCurrency());
            System.out.println(appendFORLog +"VC"+vatCalculated.getVatAmount());
        }
    }
}