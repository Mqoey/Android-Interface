/*
 * Developed by Mqondisi Ndlovu Copyright (c) 2021. contact @ 0772783880
 */

package com.axis.revmaxinterface.RevAudit.ui.main.ZReport;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.axis.revmaxinterface.R;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class ZReportsActivity extends Fragment {

    RecyclerView recyclerView;
    ZReportAdapter zReportAdapter;
    List<ZReportModel> list;

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

}