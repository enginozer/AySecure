package com.nevitech.aysecure.place.base;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Emre on 27.1.2017.
 */

public class DataBaseHelper extends SQLiteOpenHelper

{

    private static       String   COL_DATE               = null;
    private static       String   COL_LOC_ID             = null;
    private static       String   COL_MAC_ADR            = null;
    private static       String   INSERT_LINE_PREFIX     = null;
    private static       String   INSERT_VALUE_FORMAT    = null;
    private static final String   LOCATION_DATABASE_NAME = "location_database.db";
    private static       String   SELECT_LOCID           = null;
    private static       String   TABLE_NAME             = null;
    private static final int      VERSION                = 1;
    private              String[] columns;
    private              String[] columnsLocId;

    static

    {

        TABLE_NAME          = "UsedMacAdr";
        COL_MAC_ADR         = "MacAdr";
        COL_LOC_ID          = "LocID";
        COL_DATE            = "Date";
        INSERT_LINE_PREFIX  = "INSERT INTO " + TABLE_NAME + " ( " + COL_MAC_ADR + " , " + COL_LOC_ID + "  ," + COL_DATE + " ) ";
        INSERT_VALUE_FORMAT = "VALUES ( %d, %d , %d )";
        SELECT_LOCID        = "SELECT " + COL_LOC_ID + " FROM " + TABLE_NAME + " WHERE ";

    }

    public DataBaseHelper(Context context)

    {
        super(context,
              LOCATION_DATABASE_NAME,
              null,
              VERSION);

        this.columns      = new String[]{COL_MAC_ADR,COL_LOC_ID,COL_DATE};
        String []strArr   = new String[VERSION];
        strArr[0]         = COL_LOC_ID;
        this.columnsLocId = strArr;

    }

    public void onCreate(SQLiteDatabase db)

    {

        try

        {

            db.execSQL("CREATE TABLE " + TABLE_NAME + " ( " + COL_MAC_ADR + " INT8 ," + COL_LOC_ID + " INT4 ," + COL_DATE + " INT8 )");

            String str = "OK";

        }
        catch(Exception e)

        {

            e.getMessage();

        }

    }

    public void onUpgrade(SQLiteDatabase db,
                          int            oldVersion,
                          int            newVersion)

    {



    }

    public synchronized void deleteLocation(int locationID)

    {

        String         sql = "DELETE FROM " + TABLE_NAME + " WHERE " + COL_LOC_ID + " = " + Integer.valueOf(locationID).toString();

        SQLiteDatabase db  = getWritableDatabase();

        try

        {
            db.execSQL(sql);
            db.close();

            String str = "OK";

        }
        catch(Exception e)

        {

            e.getMessage();

        }

    }

    public void fillLocation()

    {

        //Decompile edilemeyen bölüm....

    }

    public synchronized int findLocation(MacAddress[] macAdrs)

    {

        int result;
        int result2 = -1;
        int len     = macAdrs.length;

        if(len<VERSION)

        {

            result = -1;

        }else

        {

           String[][] str       = new String[1][0];
                    str[1][0]   = COL_LOC_ID;

           String   where       = COL_MAC_ADR + "=?";

           for(int count = len-1 ; count > 0 ; count--)

           {

               where = where + " OR " + COL_MAC_ADR + "=?";

           }

           String[] args = new String[len];

           for(int i = 0 ; i < len ; i += VERSION )

           {

               args[i] = Long.toString(macAdrs[i].getLongVal());

           }

           SQLiteDatabase db     = getReadableDatabase();
           Cursor         cursor = db.query(TABLE_NAME,
                                            this.columnsLocId,
                                            where,
                                            args,
                                            null,
                                            null,
                                            "1");

           int countResult       = cursor.getCount();

           if (cursor != null && cursor.getCount() > 0)

           {

                cursor.moveToFirst();
                result2 = cursor.getInt(0);

           }

           db.close();
           result = result2;
        }

        return result;
    }

}
