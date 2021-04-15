/*
 * Developed by Mqondisi Ndlovu Copyright (c) 2021. contact @ 0772783880
 */

package com.axis.revmaxinterface;

import android.content.ClipData;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {

    UtilitiesClass utilitiesClass = new UtilitiesClass();

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void CurrencyRounder() throws Exception{

        assertEquals ("2.33",utilitiesClass.CurrencyRounder("2.333"));
        assertEquals("5.2",utilitiesClass.CurrencyRounder("5.2"));
        assertEquals("5.2",utilitiesClass.CurrencyRounder("5.2"));
        assertEquals("123666.67",utilitiesClass.CurrencyRounder("123666.666"));
        assertEquals("0.15",utilitiesClass.CurrencyRounder("0.145"));
        assertEquals("5.08",utilitiesClass.CurrencyRounder("5.075"));
    }

    @Test
    public void TaxCalculator() throws Exception{
        assertEquals ("0.15",utilitiesClass.TaxCalculator("1.00","1",1,"0.15"));
        assertEquals ("0.13",utilitiesClass.TaxCalculator("1.00","1",0,"0.15"));
        assertEquals ("0.15",utilitiesClass.TaxCalculator("1.00","1",1,"0.145"));
        assertEquals ("0.13",utilitiesClass.TaxCalculator("1.00","1",0,"0.145"));
        assertEquals ("5.08",utilitiesClass.TaxCalculator("35.00","1",1,"0.145"));
        assertEquals ("37.49",utilitiesClass.TaxCalculator("296.00","1",0,"0.145"));
        assertEquals ("42.92",utilitiesClass.TaxCalculator("296.00","1",1,"0.145"));
    }


    @Test
    public void RatioBasedTaxCalculator()
    {
        //test currency paid list
        ArrayList<Currencies> CurrenciesPaid = new ArrayList<Currencies>();

        Currencies currency = new Currencies();

        currency.setName("USD");
        currency.setAmount("30.06");
        currency.setRate("1");
        CurrenciesPaid.add(currency);

        currency= new Currencies();
        currency.setName("ZWL");
        currency.setAmount("250.50");
        currency.setRate("25");
        CurrenciesPaid.add(currency);

        //test currency paid list

        ArrayList<VatCalculated> ExpectedList = new ArrayList<VatCalculated>();

        VatCalculated vatCalculated = new VatCalculated();
        vatCalculated.setCurrency("USD");
        vatCalculated.setVatAmount("3.81");
        ExpectedList.add(vatCalculated);

        vatCalculated= new VatCalculated();
        vatCalculated.setCurrency("ZWL");
        vatCalculated.setVatAmount("31.75");
        ExpectedList.add(vatCalculated);

        assertEquals(ExpectedList,utilitiesClass.RatioBasedTaxCalcuator("5.08","40.08",CurrenciesPaid));

    }

    @Test
    public void ComputeInvoieTax() throws Exception{

        ArrayList<Items> itemsArrayList = new ArrayList<Items>();

        Items item = new Items();
        item.setPRICE("1");
        item.setQTY("1");
        item.setTAXR("0.145");
        itemsArrayList.add(item);

        assertEquals("0.15",utilitiesClass.ComputeInvoiceTax(itemsArrayList,1));
        assertEquals("0.13",utilitiesClass.ComputeInvoiceTax(itemsArrayList,0));


    }

    @Test
    public void ComputeInvoieTotal() throws Exception{

        ArrayList<Items> itemsArrayList = new ArrayList<Items>();

        Items item = new Items();
        item.setPRICE("1");
        item.setQTY("1");
        item.setTAXR("0.145");
        itemsArrayList.add(item);

        assertEquals("1.15",utilitiesClass.ComputeInvoiceTotal(itemsArrayList,"0.145",1));
        assertEquals("1",utilitiesClass.ComputeInvoiceTotal(itemsArrayList,"0.126",0));


    }
}