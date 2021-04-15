/*
 * Developed by Mqondisi Ndlovu Copyright (c) 2021. contact @ 0772783880
 */

package com.axis.revmaxinterface;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.secureflashcard.wormapi.WORM_ERROR;
import com.secureflashcard.wormapi.WormAccess;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class wormTest {

    private short[] memoryStatus = new short[512];
    private int[] memory_read_size = {512};
    private WORM_ERROR error;
    private WormAccess myworm;
    private int is32bit;
    static int blocksk = 0, regconfirmed = 0;
    static String ItemtotalsString0rated = "0", ItemtaxesString0rated = "0", InvoicetotalsString = "0", InvoicetaxesString = "0.0";
    static Double ItemTaxes0rated, ItemTotals0rated, ItemTotals = 0.0, ItemTaxes = 0.0;
    static String hashvalue, inumcurr;
    private int numberOfPayloadSizeBytes = 2;

    public wormTest(WormAccess _myworm) {
        myworm = _myworm;
        is32bit = is32bit();
    }

    public int isLoggedIn() {
        error = myworm.DataReadStatus(memoryStatus, memory_read_size);
        if (error != WORM_ERROR.WORM_ERROR_NOERROR)
            return -1;
        if (memoryStatus[41] == 0x01)
            return 0;
        return -1;
    }

    public int is32bit() {
        error = myworm.DataReadStatus(memoryStatus, memory_read_size);
        if (error != WORM_ERROR.WORM_ERROR_NOERROR)
            return -1;
        if (memoryStatus[29] == 0x01)
            return 1;
        return -1;
    }

    public int getCurrentSize() {
        error = myworm.DataReadStatus(memoryStatus, memory_read_size);
        if (error != WORM_ERROR.WORM_ERROR_NOERROR)
            return -1;
        else {
            int totalNumberOfBlocks = (memoryStatus[24] << 24) + (memoryStatus[25] << 16) + (memoryStatus[26] << 8) + (memoryStatus[27]);
            return totalNumberOfBlocks;
        }
    }

    public int TotalSize() {
        error = myworm.DataReadStatus(memoryStatus, memory_read_size);
        if (error != WORM_ERROR.WORM_ERROR_NOERROR)
            return -1;
        else {
            int totalNumberOfBlocks = (memoryStatus[20] << 24) + (memoryStatus[21] << 16) + (memoryStatus[22] << 8) + (memoryStatus[23]);
            return totalNumberOfBlocks;
        }
    }

    public int login() {
        byte[] pin = {0x31, 0x32, 0x33, 0x34};
        error = myworm.PINLogin(pin, 4);
        if (error != WORM_ERROR.WORM_ERROR_NOERROR) {
            return -1;
        } else {
            return 0;
        }
    }
////////////////////////// AXIS MODULES TO READ CARD ////////////////////////
///////////////////////////////////////////////////

    int byte2int(short[] input, int offset, int is32bit) {
        int temp;
        if (is32bit == 1) {
            temp = (input[offset] << 24)
                    | (input[offset + 1] << 16)
                    | (input[offset + 2] << 8)
                    | input[offset + 3];
            return temp;
        } else if (is32bit == 0) {
            temp = (input[offset + 0] << 8)
                    | input[offset + 1];
            return temp;
        } else
            return -1;
    }

    public String get_Printable_Unique_ID() {
        error = myworm.DataReadStatus(memoryStatus, memory_read_size);
        if (error != WORM_ERROR.WORM_ERROR_NOERROR)
            return "";
        else {

            short copy_arr[] = new short[32];
            System.arraycopy(memoryStatus, 256, copy_arr, 0, 32);
            String mystr = "";
            for (int i = 0; i < 32; i++) {
                if (copy_arr[i] != 0) {
                    mystr += (char) (copy_arr[i]);
                }
            }
            return mystr;
        }
    }

    public int get_FW_ID() {
        error = myworm.DataReadStatus(memoryStatus, memory_read_size);
        if (error != WORM_ERROR.WORM_ERROR_NOERROR)
            return -1;
        else {
            return byte2int(memoryStatus, 36, 1);
        }
    }

    public String getSDPath() {
        String removableStoragePath = "";
        File fileList[] = new File("/storage/").listFiles();
        for (File file : fileList) {
            if (!file.getAbsolutePath().equalsIgnoreCase(Environment.getExternalStorageDirectory().getAbsolutePath()) && file.isDirectory() && file.canRead())
                removableStoragePath = file.getAbsolutePath();
        }
        return removableStoragePath;
    }

    public String isRemovableSDCardAvailable(Context mContext) {
        final String FLAG = "mnt";
        final String SECONDARY_STORAGE = System.getenv("SECONDARY_STORAGE");
        final String EXTERNAL_STORAGE_DOCOMO = System.getenv("EXTERNAL_STORAGE_DOCOMO");
        final String EXTERNAL_SDCARD_STORAGE = System.getenv("EXTERNAL_SDCARD_STORAGE");
        final String EXTERNAL_SD_STORAGE = System.getenv("EXTERNAL_SD_STORAGE");
        final String EXTERNAL_STORAGE = System.getenv("EXTERNAL_STORAGE");

        Map<Integer, String> listEnvironmentVariableStoreSDCardRootDirectory = new HashMap<Integer, String>();
        listEnvironmentVariableStoreSDCardRootDirectory.put(0, SECONDARY_STORAGE);
        listEnvironmentVariableStoreSDCardRootDirectory.put(1, EXTERNAL_STORAGE_DOCOMO);
        listEnvironmentVariableStoreSDCardRootDirectory.put(2, EXTERNAL_SDCARD_STORAGE);
        listEnvironmentVariableStoreSDCardRootDirectory.put(3, EXTERNAL_SD_STORAGE);
        listEnvironmentVariableStoreSDCardRootDirectory.put(4, EXTERNAL_STORAGE);

        File externalStorageList[] = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            externalStorageList = mContext.getExternalFilesDirs(null);
        }
        String directory = null;
        int size = listEnvironmentVariableStoreSDCardRootDirectory.size();
        for (int i = 0; i < size; i++) {
            if (externalStorageList != null && externalStorageList.length > 1 && externalStorageList[1] != null)
                directory = externalStorageList[1].getAbsolutePath();
            else
                directory = listEnvironmentVariableStoreSDCardRootDirectory.get(i);

            directory = canCreateFile(directory);
            if (directory != null && directory.length() != 0) {
                if (i == size - 1) {
                    if (directory.contains(FLAG)) {
                        Log.e(getClass().getSimpleName(), "SD Card's directory: " + directory);
                        return directory;
                    } else {
                        return null;
                    }
                }
                Log.e(getClass().getSimpleName(), "SD Card's directory: " + directory);
                return directory;
            }
        }
        return null;
    }

    public String canCreateFile(String directory) {
        final String FILE_DIR = directory + File.separator + "hayat.txt";
        File tempFlie = null;
        try {
            tempFlie = new File(FILE_DIR);
            FileOutputStream fos = new FileOutputStream(tempFlie);
            fos.write(new byte[1024]);
            fos.flush();
            fos.close();
            Log.e(getClass().getSimpleName(), "Can write file on this directory: " + FILE_DIR);
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Write file error: " + e.getMessage());
            return null;
        } finally {
            if (tempFlie != null && tempFlie.exists() && tempFlie.isFile()) {
                // tempFlie.delete();
                tempFlie = null;
            }
        }
        return directory;
    }

    //modified export worm store to get only the first 40 transactions.
    // This is to optimise recovery of card details as the transaction list grows
    public int exportCardDetails(ArrayList<WormEntry> list) {
        WormEntry lastentry = null;
        int currentblock = 0;
        if (list == null)
            list = new ArrayList<WormEntry>();
        else if (list.size() > 0) {
            //lastentry = list.Last();
            lastentry = list.get(list.size() - 1);
            currentblock = lastentry.nextBlock;
        }
        int totalNumberOfBlocks;
        totalNumberOfBlocks = getCurrentSize();

        int incidentCount = 0;
        WormEntry currentTransaction = null;
        WormEntry previousTransaction = null;

        while (currentblock < totalNumberOfBlocks) {
            currentTransaction = new WormEntry(currentblock, false);
            list.add(currentTransaction);
            // save memory!
            currentTransaction.releaseData();
            currentblock = currentTransaction.nextBlock;
            if (currentTransaction.isIncidentReport == true) { // ignore incidents
                // we have an incident report here
                incidentCount++;
            }
            previousTransaction = currentTransaction;

            //cut off reading at 40 transactions here
            if (list.size() == 40) {
                // skip last transaction, because not yet completed!
                if ((memoryStatus[30] == 1) && (currentTransaction != null)) {

                    list.remove(currentTransaction);
                }
                return 0;
            }
        }

        // skip last transaction, because not yet completed!
        if ((memoryStatus[30] == 1) && (currentTransaction != null)) {
            list.remove(currentTransaction);
        }
        return 0;
    }

    //export worm store methods to get all transactions from the card
    public ArrayList<WormEntry> exportWormStores(ArrayList<WormEntry> list) {
        WormEntry lastentry = null;
        int currentblock = 0;
        if (list == null) {
            list = new ArrayList<WormEntry>();
        } else if (list.size() > 0) {
            lastentry = list.get(list.size() - 1);
            currentblock = lastentry.nextBlock;
        }

        int totalNumberOfBlocks;
        totalNumberOfBlocks = getCurrentSize();

        int incidentCount = 0;
        WormEntry currentTransaction = null;
        WormEntry previousTransaction = null;

        while (currentblock < totalNumberOfBlocks) {
            currentTransaction = new WormEntry(currentblock, false);

            list.add(currentTransaction);
            // save memory!
            currentTransaction.releaseData();
            currentblock = currentTransaction.nextBlock;
            if (currentTransaction.isIncidentReport == true) { // ignore incidents
                // we have an incident report here
                incidentCount++;
            }
            previousTransaction = currentTransaction;
        }
        // skip last transaction, because not yet completed!
        if ((memoryStatus[30] == 1) && (currentTransaction != null)) {
            list.remove(currentTransaction);
        }
//        for (WormEntry item: list
//        ) {
//            System.out.println(String.valueOf(item));
//        }
        return list;
    }

    public int exportWormStore(ArrayList<WormEntry> list) {
        WormEntry lastentry = null;
        int currentblock = 0;
        if (list == null) {
            list = new ArrayList<WormEntry>();
        } else if (list.size() > 0) {
            lastentry = list.get(list.size() - 1);
            currentblock = lastentry.nextBlock;
        }

        int totalNumberOfBlocks;
        totalNumberOfBlocks = getCurrentSize();

        int incidentCount = 0;
        WormEntry currentTransaction = null;
        WormEntry previousTransaction = null;

        while (currentblock < totalNumberOfBlocks) {
            currentTransaction = new WormEntry(currentblock, false);

            list.add(currentTransaction);
            // save memory!
            currentTransaction.releaseData();
            currentblock = currentTransaction.nextBlock;
            if (currentTransaction.isIncidentReport == true) { // ignore incidents
                // we have an incident report here
                incidentCount++;
            }
            previousTransaction = currentTransaction;
            //   System.out.println("No removal");
        }
        // skip last transaction, because not yet completed!
        if ((memoryStatus[30] == 1) && (currentTransaction != null)) {
            // System.out.println("remove current transaction");
            list.remove(currentTransaction);
        }
        return 0;
    }

    public class WormEntry {
        private short[] wormData;
        public int wormDataLength;
        public int wormDataLengthInclPadding;
        private short[] previousHashArray;
        private short[] currentHashArray;
        public int currentblock;
        public int transactionBlocks = 0;
        public int nextBlock;
        public Boolean isIncidentReport;
        private Boolean dataLoaded;
        private int numberOfPayloadSizeBytes = 2;
        int _is32bit = is32bit();

        public WormEntry(int blockId, Boolean withData) {
            currentblock = blockId;
            wormData = new short[512];

            //   System.out.print("***curentblock " + currentblock+"  ");
            myworm.DataRead(wormData, currentblock, 1);

            if (_is32bit == 1) {
                wormDataLength = ((wormData[0] << 24) + (wormData[1] << 16) + (wormData[2] << 8) + wormData[3]);
                wormDataLength = byte2int(wormData, 0, 1);
                //  System.out.println("***Wa" + wormDataLength);
                numberOfPayloadSizeBytes = 4;
            } else {

                wormDataLength = (wormData[0] << 8) + wormData[1];
                //   System.out.println("***nWa" + wormDataLength);
                wormDataLength = byte2int(wormData, 0, 0);
                // System.out.println("***nWa" + wormDataLength);
            }
            //  System.out.println("***1WORMlen "+wormDataLength);

            if (wormDataLength == 0) {
                //we have an incident report here
                wormDataLength = (wormData[0 + numberOfPayloadSizeBytes] << 8) + wormData[1 + numberOfPayloadSizeBytes];
                wormDataLengthInclPadding = (((wormDataLength + 2 + 2 + 15) / 16) * 16);
                isIncidentReport = true;
                transactionBlocks = 1;
            } else {
                wormDataLengthInclPadding = (((wormDataLength + numberOfPayloadSizeBytes + 15) / 16) * 16);
                isIncidentReport = false;
                // include 2 hashes in read
                transactionBlocks = (wormDataLengthInclPadding + 2 * 32 + 512 - 1) / 512;
            }
            ////////////////////////////////////////////
            if (withData) {
                ReadData();
            } else {
                wormData = null;
            }
            nextBlock = currentblock + transactionBlocks;
        }

        public short[] previousHash() {
            ReadData();
            return previousHashArray;
        }

        public short[] currentHash() {
            ReadData();
            return currentHashArray;
        }

        private int ReadData() {
            int result = 0;
            if (!dataLoaded) {
                wormData = new short[transactionBlocks * 512];
                error = myworm.DataRead(wormData, currentblock, transactionBlocks);
                dataLoaded = true;
                previousHashArray = new short[32];
                System.arraycopy(wormData, wormDataLengthInclPadding, previousHashArray, 0, 32);
                currentHashArray = new short[32];
                System.arraycopy(wormData, wormDataLengthInclPadding + 32, currentHashArray, 0, 32);
            }
            return result;
        }

        public void releaseData() {
            wormData = null;
            previousHashArray = null;
            currentHashArray = null;
            dataLoaded = false;
        }

        //        public Boolean isHashOK()
//        {
//            byte[] mycalcCurrentHash = null;
//            ReadData();
//            using (SHA256 sha256 = SHA256.Create())
//            {
//                mycalcCurrentHash = sha256.ComputeHash(wormData, 0, (Int32)wormDataLengthInclPadding + 32);
//                sha256.Clear();
//            }
//            if (!mycalcCurrentHash.SequenceEqual(currentHashArray))
//            {
//                return false;
//            }
//            return true;
//        }
        public int getPayloadLength() {
            return wormDataLength;
        }

        public short[] getPayload() {
            int offset = numberOfPayloadSizeBytes;
            ReadData();
            short[] payload = new short[wormDataLength];
            if (isIncidentReport) {
                offset += 2;
            }
            System.arraycopy(wormData, offset, payload, 0, wormDataLength);
            return payload;
        }

        int byte2int(short[] input, int offset, int is32bit) {
            int temp;
            if (is32bit == 1) {
                temp = (input[offset] << 24)
                        | (input[offset + 1] << 16)
                        | (input[offset + 2] << 8)
                        | input[offset + 3];
                return temp;
            } else if (is32bit == 0) {
                temp = (input[offset + 0] << 8)
                        | input[offset + 1];
                return temp;

            } else
                return -1;
        }
    }
}