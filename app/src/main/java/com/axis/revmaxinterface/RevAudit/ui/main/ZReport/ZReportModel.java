/*
 * Developed by Mqondisi Ndlovu Copyright (c) 2021. contact @ 0772783880
 */

package com.axis.revmaxinterface.RevAudit.ui.main.ZReport;

public class ZReportModel {

    private String InvoiceNumber;
    private String InvoiceDate;

    public ZReportModel(String invoiceNumber, String invoiceDate) {
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