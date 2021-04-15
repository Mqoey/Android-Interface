/*
 * Developed by Mqondisi Ndlovu Copyright (c) 2021. contact @ 0772783880
 */

package com.axis.revmaxinterface.RevAudit.ui.main.Transaction;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.axis.revmaxinterface.R;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.MyHolder> {

    Context context;
    List<TransactionModel> list;

    public TransactionAdapter(Context context, List<TransactionModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        return  new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        holder.invoiceNumber.setText(list.get(position).getInvoiceNumber());
        holder.invoiceDate.setText(list.get(position).getInvoiceDate());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder{
        TextView invoiceNumber, invoiceDate;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            invoiceDate = itemView.findViewById(R.id.invoiceDate);
            invoiceNumber = itemView.findViewById(R.id.invoiceNumber);
        }
    }
}
