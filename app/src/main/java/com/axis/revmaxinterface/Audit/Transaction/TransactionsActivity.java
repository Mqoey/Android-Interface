/*
 * Developed by Mqondisi Ndlovu Copyright (c) 2021. contact @ 0772783880
 */

package com.axis.revmaxinterface.Audit.Transaction;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.axis.revmaxinterface.Currencies;
import com.axis.revmaxinterface.Invoice;
import com.axis.revmaxinterface.Items;
import com.axis.revmaxinterface.R;
import com.axis.revmaxinterface.wormTest;
import com.secureflashcard.wormapi.WORM_ERROR;
import com.secureflashcard.wormapi.WormAccess;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.printer.ZebraPrinter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionsActivity extends Fragment {

    //bluetooth methods
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_EBANBLE_BTCD = 3;
    private static final int REQUEST_DEVICE = 2;
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    static String VatRateA = "14.5", VatRateB = "0";
    static String ItemQTYString = "0", ItemtotalsString0rated = "0", ItemtaxesString0rated = "0", InvoicetotalsString = "0", InvoicetaxesString = "0.0", qntity, price, total;
    static Double ItemTaxes0rated, ItemTotals0rated, Usdfinal0Total, final0Total, ItemTotals = 0.0, InvTotals = 0.0, UsdInvTotals = 0.0, ItemTaxes = 0.0, UsdfinalTax = 0.0, UsdfinalTotal = 0.0, finalTax = 0.0, finalTotal = 0.0;
    static String IMEI = "Nil", hashvalue, inumcurr;
    static String RevmaxSerialNumber, Invoicetotals = "0", address, res, totempts = "", GLOBALFILEPATH, inum;
    static Integer NumberOfZOnDevice = 0, NumberofZforPrint = 0;
    static String ZReportText, totlBx, totlB, totlCx, totlAx, totlA, totlC, vat, bpn, prserial, HeaderCo0, HeaderCo1, HeaderCo2, HeaderCo3, HeaderCo4, HeaderCo5;
    static int starttagtrue = -1;
    static String TotalForeign;
    static String VatRate = "0.145", icurrency = "", Invoiceamntstring = "0.0";
    static int ItemCounter = 0;
    static String currhash, daten;
    static int resultforZ = 0;
    private static Context mContext;
    private static Connection connection;
    private static ZebraPrinter printer;
    private final Handler mHandler = new Handler();
    public String XMLToFiscalizeSoap = "";
    RecyclerView recyclerView;
    TransactionAdapter transactionAdapter;
    List<TransactionModel> translist;
    ArrayList<wormTest.WormEntry> list;
    StringBuilder recbld = new StringBuilder();
    File f, f2;
    String err, currentTag = "", endTag = "";
    String conf = "Intconfig.xml", confile;
    String path, path1 = "inload.xml", path2 = "1.xml";
    byte[] CbcMacKey = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F};
    Invoice newInvoice;
    Currencies newCurrency;
    Items newItem;
    ArrayList<Invoice> invoicesList;
    ArrayList<Currencies> currenciesList;
    ArrayList<Items> itemsList;
    wormTest mywormTest;
    String output;
    TextView textView;
    private BluetoothAdapter mBtAdapter;
    private BluetoothSocket mBtSocket;
    private Context ctx;
    private PendingIntent pedint;
    private WORM_ERROR error;
    private int NOTIFICATION_ID = 1;
    //rev components
    private boolean initDone = false;
    private boolean isRunning = false;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private WormAccess worm = new WormAccess();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.transaction_main_fragment, container, false);

        getlist();
//        getAllTransactions();
        textView = root.findViewById(R.id.textView4);

        recyclerView = root.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        transactionAdapter = new TransactionAdapter(getActivity(), translist);
        recyclerView.setAdapter(transactionAdapter);

        return root;
    }

    private void getlist() {
        translist = new ArrayList<>();
        translist.add(new TransactionModel("2312365765467573", "23/09/2021"));
        translist.add(new TransactionModel("2312334654733275", "03/01/2021"));
        translist.add(new TransactionModel("0839895389859899", "13/06/2021"));
    }
}