package com.nevitech.aysecure.place.base;

import android.support.v4.BuildConfig;
import android.support.v4.view.MotionEventCompat;

import com.nevitech.aysecure.place.interfaces.ISerialize;

import java.io.Serializable;

/**
 * Created by Emre on 27.1.2017.
 */

public class MacAddress implements Comparable<MacAddress>,ISerialize,Serializable

{

    public final short THIS_SERIALIZE_VERSION;
    private      long  lVal;

    public boolean equals(MacAddress other)

    {

        return this.lVal == other.lVal;

    }

    public int compareTo(MacAddress other)

    {

        MacAddress ma = other;

        if (this.lVal < ma.lVal)

        {

            return -1;

        }

        if (this.lVal == ma.lVal)

        {

            return 0;

        }

        return 1;

    }

    public static MacAddress getEmpty()

    {

        return new MacAddress();

    }

    public MacAddress()

    {

        this.THIS_SERIALIZE_VERSION = (short) -1;
        this.lVal                   = 0;

    }

    public MacAddress(long val)

    {

        this.THIS_SERIALIZE_VERSION = (short) -1;
        this.lVal                   = val;

    }

    public MacAddress(MacAddress adr)

    {

        this.THIS_SERIALIZE_VERSION = (short) -1;
        this.lVal                   = adr.lVal;

    }

    public MacAddress(byte[] b)

    {

        this.THIS_SERIALIZE_VERSION = (short) -1;
        initWith(b);

    }

    public void initWith(byte[] b)

    {

        this.lVal = 0;

        if (b != null && b.length >= 6)

        {

            this.lVal = 0;

            for (int i = 0; i < 6; i++)

            {

                this.lVal <<= 8;
                this.lVal  |= (long) (b[i] & MotionEventCompat.ACTION_MASK);

            }

        }

    }

    public long getLongVal()

    {

        return this.lVal;

    }

    public String toString()

    {

        String s = String.format("%X", new Object[]{Long.valueOf(this.lVal)});

        while (s.length() < 12)

        {

            s = "0" + s;

        }
        String result = s.substring(0, 2);

        for (int i = 1; i < 6; i++)

        {

            result = (result + ":") + s.substring(i * 2, (i * 2) + 2);

        }

        return result;

    }

    public MacAddress(String val)

    {

        this.THIS_SERIALIZE_VERSION = (short) -1;
        this.lVal                   = Long.decode("0x" + val.replace(":", BuildConfig.VERSION_NAME)).longValue();
        String test                 = toString();
    }

    public short getSerializeVersion()

    {

        return (short) -1;

    }

    public boolean deserialize(Deserializer des)

    {

        try

        {

            byte[] b   = new byte[8];
            des.readBytes(b);
            initWith(b);
            String str = toString();

            return true;

        }
        catch (Exception e)

        {

            this.lVal = 0;
            return false;

        }

    }

    private byte[] getBytes()

    {

        byte[] b    = new byte[8];
        long   help = this.lVal;

        for (int i = 5; i >= 0; i--)

        {

            b[i]   = (byte) ((int) (255 & help));
            help >>= 8;

        }

        b[6] = (byte) 0;
        b[7] = (byte) 0;

        return b;

    }

    public boolean serialize(Serializer ser)

    {

        ser.writeBytes(getBytes());
        return true;

    }

}
