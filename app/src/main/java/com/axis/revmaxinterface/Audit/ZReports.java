/*
 * Developed by Mqondisi Ndlovu Copyright (c) 2021. contact @ 0772783880
 */

package com.axis.revmaxinterface.Audit;

import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.axis.revmaxinterface.R;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class ZReports extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            ArrayList<HashMap<String, String>> userList = new ArrayList<>();
            ListView lv = (ListView) findViewById(R.id.user_list);
            String znumber = getIntent().getStringExtra("ZNUM");
            System.out.println(znumber);
            InputStream istream = getAssets().open("zreport.xml");
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(istream);
            NodeList nList = doc.getElementsByTagName("ZREPORT");
            for (int i = 0; i < nList.getLength(); i++) {
                if (nList.item(0).getNodeType() == Node.ELEMENT_NODE) {
                    HashMap<String, String> user = new HashMap<>();
                    Element elm = (Element) nList.item(i);
                    user.put("znumber", getNodeValue("Znumber", elm));
                    user.put("vatrate", getNodeValue("VATRATE", elm));
                    user.put("netamount1", getNodeValue("NETTAMOUNT", elm));
                    user.put("netamount2", getNodeValue("NETTAMOUNT", elm));
                    user.put("taxamout1", getNodeValue("TAXAMOUNT", elm));
                    user.put("taxamount2", getNodeValue("TAXAMOUNT", elm));
                    userList.add(user);
                }
            }
            ListAdapter adapter = new SimpleAdapter(ZReports.this, userList, R.layout.activity_zreports,
                    new String[]{"znumber", "vatrate", "netamount1" , "netamount2" , "taxamout1", "taxamout2"},
                    new int[]{R.id.z_number, R.id.vat_rate, R.id.zwl_total, R.id.usd_total, R.id.zwl_tax, R.id.usd_tax});
            lv.setAdapter(adapter);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    protected String getNodeValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag);
        Node node = nodeList.item(0);
        if (node != null) {
            if (node.hasChildNodes()) {
                Node child = node.getFirstChild();
                while (child != null) {
                    if (child.getNodeType() == Node.TEXT_NODE) {
                        return child.getNodeValue();
                    }
                }
            }
        }
        return "";
    }
}

