/*
 * Developed by Mqondisi Ndlovu Copyright (c) 2021. contact @ 0772783880
 */

package com.axis.revmaxinterface.RevAudit.ui.main.Transaction;

public class TransactionModel {

    private String InvoiceNumber;
    private String InvoiceDate;

    public TransactionModel(String invoiceNumber, String invoiceDate) {
        InvoiceNumber = invoiceNumber;
        InvoiceDate = invoiceDate;
    }

    public String getInvoiceNumber() {
        return InvoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        InvoiceNumber = invoiceNumber;
    }

    public String getInvoiceDate() {
        return InvoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        InvoiceDate = invoiceDate;
    }
}
