/*
 * Developed by Mqondisi Ndlovu Copyright (c) 2021. contact @ 0772783880
 */

package com.axis.revmaxinterface.Audit.ZReport;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.axis.revmaxinterface.Audit.AuditDoWork;
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
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class ZReportsActivity extends Fragment {

    RecyclerView recyclerView;
    ZReportAdapter zReportAdapter;
    List<ZReportModel> list;
    AuditDoWork auditDoWork;

    @Override
    public View onCreateView (
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState){
        View root = inflater.inflate(R.layout.zreports_main_fragment, container,false);

//        getlist();

//        recyclerView = root.findViewById(R.id.recyclerview1);
//        recyclerView.setHasFixedSize(true);
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
//        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
//        recyclerView.setLayoutManager(linearLayoutManager);
//
//        zReportAdapter = new ZReportAdapter(getActivity(),list);
//        recyclerView.setAdapter(zReportAdapter);

        return root;
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

    private void getlist(){
        auditDoWork = new AuditDoWork();
        list = new ArrayList<>();
        list.add(new ZReportModel(AuditDoWork.ZRNumber, "123"));
//        list.add(new ZReportModel("ZReport1","29/09/2020"));
//        list.add(new ZReportModel("ZReport3","21/06/2021"));
//        list.add(new ZReportModel("ZReport3","03/01/2020"));
    }
}