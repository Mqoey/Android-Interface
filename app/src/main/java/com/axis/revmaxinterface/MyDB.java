package com.axis.revmaxinterface;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;

/**
 * Created by Admin on 26/02/2018.
 */
public class MyDB{

    private MyDatabaseHelper dbHelper;

    private SQLiteDatabase database,databaseread;

    // name of table
   /**
     *
     * @param context
     */
    public MyDB(Context context){
        dbHelper = new MyDatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        databaseread=dbHelper.getReadableDatabase();

    }


//
//    //INSERT Invoices
    public long InsertInvoices(String ID,String DeviceBPN ,String CODE ,String MACNUM ,String DECSTARTDATE ,String DECENDDATE ,String      DETSTARTDATE ,String      DETENDDATE ,String      CPY ,String      IND ,String      ITYPE ,String      ICODE ,String      VAT ,String      BRANCH ,String      INUM,String     IBPN ,String      INAME ,String      IADDRESS ,String      ICONTACT ,String      ISHORTNAME ,String      IPAYER ,String      IPVAT ,String      IPADDRESS ,String      IPTEL ,String      IPBPN ,String      ICURRENCY ,String      IAMT ,String      ITAX ,String      ISTATUS ,String      IISSUER ,String      IDATE ,String      ITAXCTRL ,String      IOCODE ,String      IONUM ,String      IREMARK ,String      ITEMSXML ,String      CurrenciesReceivedXML){
        ContentValues values = new ContentValues();
        values.put(ConstantVariables.DeviceBPN, DeviceBPN);
        values.put(ConstantVariables.CODE,CODE );
        values.put(ConstantVariables.MACNUM,MACNUM );
        values.put(ConstantVariables.DECSTARTDATE,DECSTARTDATE );
        values.put(ConstantVariables.DECENDDATE, DECENDDATE);
        values.put(ConstantVariables.     DETSTARTDATE,DETSTARTDATE );
        values.put(ConstantVariables.     DETENDDATE , DETENDDATE);
        values.put( ConstantVariables.    CPY ,CPY );
        values.put(ConstantVariables.     IND , IND);
        values.put( ConstantVariables.    ITYPE,ITYPE );
        values.put(ConstantVariables.     ICODE , ICODE);
        values.put(ConstantVariables.     VAT ,VAT );
        values.put(ConstantVariables.     BRANCH,BRANCH );
        values.put( ConstantVariables.    INUM , INUM);
        values.put( ConstantVariables.    IBPN ,IBPN );
        values.put( ConstantVariables.    INAME ,INAME );
        values.put( ConstantVariables.    IADDRESS, IADDRESS);
        values.put( ConstantVariables.    ICONTACT , ICONTACT);
        values.put(ConstantVariables.     ISHORTNAME,ISHORTNAME );
        values.put( ConstantVariables.    IPAYER , IPAYER);
        values.put( ConstantVariables.    IPVAT , IPVAT);
        values.put( ConstantVariables.    IPADDRESS, IPADDRESS);
        values.put( ConstantVariables.    IPTEL ,IPTEL );
        values.put( ConstantVariables.    IPBPN , IPBPN);
        values.put( ConstantVariables.    ICURRENCY ,ICURRENCY );
        values.put( ConstantVariables.    IAMT , IAMT);
        values.put( ConstantVariables.    ITAX , ITAX);
        values.put(ConstantVariables.     ISTATUS, ISTATUS);
        values.put( ConstantVariables.    IISSUER , IISSUER);
        values.put( ConstantVariables.    IDATE , IDATE);
        values.put( ConstantVariables.    ITAXCTRL, ITAXCTRL);
        values.put( ConstantVariables.    IOCODE , IOCODE);
        values.put( ConstantVariables.    IONUM , IONUM);
        values.put( ConstantVariables.    IREMARK, IREMARK);
        values.put( ConstantVariables.    ITEMSXML, ITEMSXML);
        values.put( ConstantVariables.    CurrenciesReceivedXML,CurrenciesReceivedXML );
        values.put(ConstantVariables.syncedToZimra , "0");
        values.put(ConstantVariables.syncedToRevMaxPortal,"0" );

        return database.insert(ConstantVariables.tblInvoices,null,values);
    }


    //INSERT Invoices
    public long InsertInvoiceCurrency(String ID,String Name ,String Amount ,String Rate,String INUM){
        ContentValues values = new ContentValues();
        values.put(ConstantVariables.Name, Name);
        values.put(ConstantVariables.Amount,Amount );
        values.put(ConstantVariables.Rate,Rate );
        values.put(ConstantVariables.INUM,INUM );
        values.put(ConstantVariables.syncedToZimra, "0");
        values.put(ConstantVariables.syncedToRevMaxPortal,"0" );

        return database.insert(ConstantVariables.tblCurrencies,null,values);
    }


    //INSERT Invoices
    public long InsertInvoiceItems(String ID,String HH ,String ITEMCODE ,String ITEMNAME1,String ITEMNAME2,String QTY,String PRICE,String AMT, String TAX, String TAXR, String INUM){
        ContentValues values = new ContentValues();
        values.put(ConstantVariables.HH, HH);
        values.put(ConstantVariables.ITEMCODE,ITEMCODE );
        values.put(ConstantVariables.ITEMNAME1,ITEMNAME1 );
        values.put(ConstantVariables.ITEMNAME2,ITEMNAME2 );
        values.put(ConstantVariables.QTY,QTY );
        values.put(ConstantVariables.PRICE,PRICE );
        values.put(ConstantVariables.AMT,AMT );
        values.put(ConstantVariables.TAX,TAX );
        values.put(ConstantVariables.TAXR,TAXR );
        values.put(ConstantVariables.INUM,INUM );
        values.put(ConstantVariables.syncedToZimra, "0");
        values.put(ConstantVariables.syncedToRevMaxPortal,"0" );

        return database.insert(ConstantVariables.tblItems,null,values);
    }


    //INSERT Invoices
    public long InsertVatCalculated(String ID,String Currency ,String VatAmount,String INUM ){
        ContentValues values = new ContentValues();
        values.put(ConstantVariables.Currency, Currency);
        values.put(ConstantVariables.VatAmount,VatAmount );
        values.put(ConstantVariables.INUM,INUM );
        values.put(ConstantVariables.syncedToZimra, "0");
        values.put(ConstantVariables.syncedToRevMaxPortal,"0" );

        return database.insert(ConstantVariables.tblVatCalculated,null,values);
    }


    //INSERT Invoices
    public long InsertToken(String Token ){
        ContentValues values = new ContentValues();
        values.put(ConstantVariables.ID, "1");
        values.put(ConstantVariables.DeviceSerial,MainActivity.serial );
        values.put(ConstantVariables.token,Token );
       // values.put(ConstantVariables.syncedToZimra, "0");
        //values.put(ConstantVariables.syncedToRevMaxPortal,"0" );

        return database.insertWithOnConflict(ConstantVariables.tbltokens,ConstantVariables.DeviceSerial,values,SQLiteDatabase.CONFLICT_REPLACE);
    }

    //     //select statement for items
    public Cursor selectInvoiceDetail() {
        String[] cols = new String[] {ConstantVariables.ID,ConstantVariables.INUM,ConstantVariables.BRANCH,ConstantVariables.IADDRESS,ConstantVariables.ICURRENCY,ConstantVariables.IAMT,ConstantVariables.DeviceBPN,ConstantVariables.ICODE,ConstantVariables.ICONTACT,ConstantVariables.IDATE,ConstantVariables.IISSUER,ConstantVariables.INAME,ConstantVariables.IOCODE,ConstantVariables.INUM,ConstantVariables.IPADDRESS,ConstantVariables.IPAYER,ConstantVariables.IPBPN,ConstantVariables.IPVAT,ConstantVariables.IPTEL,ConstantVariables.IREMARK,ConstantVariables.ISHORTNAME,ConstantVariables.ISTATUS,ConstantVariables.ITAX,ConstantVariables.ITAXCTRL,ConstantVariables.ITYPE};
        Cursor mCursor = databaseread.query(true, ConstantVariables.tblInvoices,cols,null
                , null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();

        }
        return mCursor; // iterate to get each value.
    }

//     //select statement for items
   public Cursor selectInvoiceItems(String whereIDvalue) {
        String[] cols = new String[] {ConstantVariables.ID,ConstantVariables.ITEMCODE,ConstantVariables.ITEMNAME1,ConstantVariables.ITEMNAME2,ConstantVariables.PRICE,ConstantVariables.QTY,ConstantVariables.TAX,ConstantVariables.TAXR,ConstantVariables.AMT};
        String [] wherearg = new String[]{whereIDvalue};
        Cursor mCursor = databaseread.query(true, ConstantVariables.tblItems,cols,"ID=?"
                , wherearg, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();

        }
        return mCursor; // iterate to get each value.
    }


    public Cursor selectInvoiceCurrency(String whereIDvalue) {
        String[] cols = new String[] {ConstantVariables.ID,ConstantVariables.Name,ConstantVariables.Amount,ConstantVariables.Rate};
        String [] wherearg = new String[]{whereIDvalue};
        Cursor mCursor = databaseread.query(true, ConstantVariables.tblCurrencies,cols,"ID=?"
                , wherearg, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();

        }
        return mCursor; // iterate to get each value.
    }

    public Cursor selectToken() {
        String[] cols = new String[] {ConstantVariables.ID,ConstantVariables.DeviceSerial,ConstantVariables.token};
        String [] wherearg = new String[]{MainActivity.serial};
        Cursor mCursor = databaseread.query(true, ConstantVariables.tbltokens,cols,"DeviceSerial=?"
                , wherearg, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();

        }
        return mCursor; // iterate to get each value.
    }

    //update sync status
    public long SetSyncedtoTrue(String dbtoupdate,String columnToUpdate, String columnToCheck, String columnToCheckValue){
        ContentValues contentValues= new ContentValues();
        contentValues.put(columnToUpdate,"1");
        return database.update(dbtoupdate, contentValues, ""+columnToCheck+"=?",new String[] {columnToCheckValue});
    }
//
//
//    public long DeleteInvoiceRecords(String Customer){
//        String [] wherearg = new String[]{Customer};
//        ContentValues contentValues= new ContentValues();
//        return database.delete(ChekStart.tblOwingInvoices,"CustomerName=?" ,wherearg);
//    }
//
//    public long DeleteZReportRecords(String Customer){
//        String [] wherearg = new String[]{Customer};
//        ContentValues contentValues= new ContentValues();
//        return database.delete(ChekStart.tblOwingInvoices,"CustomerName=?" ,wherearg);
//    }






}