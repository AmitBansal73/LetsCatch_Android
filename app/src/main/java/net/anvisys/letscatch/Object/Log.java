package net.anvisys.letscatch.Object;

import android.content.Context;

import net.anvisys.letscatch.Common.DataAccess;

/**
 * Created by Amit Bansal on 10-02-2017.
 */
public class Log {
    public int ID;
    public String text;
    public String DateTime;


    public static void AddLog(String Message, Context context)
    {
        DataAccess da = new DataAccess(context);
        da.open();
        da.InsertLog(Message);
        da.LimitLogData();
        da.close();

    }
}
