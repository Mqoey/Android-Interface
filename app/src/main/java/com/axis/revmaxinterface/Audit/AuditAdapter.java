/*
 * Developed by Mqondisi Ndlovu Copyright (c) 2021. contact @ 0772783880
 */

package com.axis.revmaxinterface.Audit;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.axis.revmaxinterface.Audit.Transaction.TransactionsActivity;
import com.axis.revmaxinterface.Audit.ZReport.ZReportsActivity;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class AuditAdapter extends FragmentPagerAdapter {

    private final Context mContext;
    AuditDoWork auditDoWork;

    public AuditAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                TransactionsActivity fragment1 = new TransactionsActivity();
                return fragment1;
            case 1:
                ZReportsActivity fragment2 = new ZReportsActivity();
                return fragment2;
            case 2:

            default:
                return null;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Transactions";
            case 1:
                return "ZReports" ;

        }
        return null;
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }
}