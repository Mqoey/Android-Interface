package com.axis.revmaxinterface.RevAudit;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.axis.revmaxinterface.R;
import com.axis.revmaxinterface.RevAudit.ui.main.DbHandler;
import com.axis.revmaxinterface.RevAudit.ui.main.SectionsPagerAdapter;
import com.axis.revmaxinterface.wormTest;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.Transaction;
import com.secureflashcard.wormapi.WORM_ERROR;
import com.secureflashcard.wormapi.WormAccess;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static java.lang.System.arraycopy;

public class revAuditActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rev_audit);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        helper = new DbHandler(this);
        DbHandler db = new DbHandler(this);
        ArrayList<HashMap<String, String>> userList = db.getZReports();
        ListView lv = (ListView) findViewById(R.id.user_list);

        ListAdapter adapter = new SimpleAdapter(
                revAuditActivity.this, userList, R.layout.list_row,
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
    }

    private void refreshDb() {
        Toast toast = Toast.makeText(revAuditActivity.this,
                "Refreshing...",
                Toast.LENGTH_SHORT);
        toast.show();
        mywormTest = new wormTest(worm);

        try {
            final int ret = worm.init(revAuditActivity.this);
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

                                                Document doc = convertStringToXML(TransactionDetail);
                                                NodeList nList = doc.getElementsByTagName("ZREPORT");
                                                for (int i = 0; i < nList.getLength(); i++) {
                                                    if (nList.item(0).getNodeType() == Node.ELEMENT_NODE) {
                                                        Element elm = (Element) nList.item(i);
                                                        znumber = getNodeValue("Znumber", elm);
                                                        currency = getNodeValue("CURRENCY", elm);
                                                        netamount = getNodeValue("NETTAMOUNT", elm);
                                                        taxamout = getNodeValue("TAXAMOUNT", elm);
                                                        vatrate = getNodeValue("VATRATE", elm);
                                                        date = getNodeValue("DATE", elm);
                                                        time = getNodeValue("TIME", elm);
                                                    }
                                                }
                                                helper.insertUserDetails(znumber, currency, netamount, taxamout, vatrate, date, time);
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
                                                            taxamout = getNodeValue("PRICE", elm);
                                                            vatrate = getNodeValue("VATRATE", elm);
                                                            date = getNodeValue("AMT", elm);
                                                            time = getNodeValue("TAX", elm);
                                                        }
                                                    }
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