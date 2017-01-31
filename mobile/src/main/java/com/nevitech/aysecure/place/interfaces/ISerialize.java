package com.nevitech.aysecure.place.interfaces;

/**
 * Created by Emre on 27.1.2017.
 */
import com.nevitech.aysecure.place.base.Deserializer;
import com.nevitech.aysecure.place.base.Serializer;
public interface ISerialize

{
    public static final short ACCESS_POINT_INFO   = (short) 21;
    public static final short ACCESS_POINT_INFOS  = (short) 22;
    public static final short CALIB_ROUTE         = (short) 18;
    public static final short GPS_LOCATION        = (short) 16;
    public static final short GPS_LOCATIONS       = (short) 17;
    public static final short MEASURE             = (short) 3;
    public static final short MEASURES            = (short) 6;
    public static final short MEASURE_ITEM        = (short) 2;
    public static final short MEASURE_ITEMS       = (short) 4;
    public static final short REF_ITEM            = (short) 0;
    public static final short REF_POINT           = (short) 1;
    public static final short SER_ID_NOT_USED     = (short) -1;
    public static final short WLAN_HIT_QUOTE      = (short) 10;
    public static final short WLAN_HIT_QUOTES     = (short) 9;
    public static final short WLAN_MAP            = (short) 5;
    public static final short WLAN_MAPPING        = (short) 13;
    public static final short WLAN_MAPPING_ITEM   = (short) 15;
    public static final short WLAN_MAPPING_ITEMS  = (short) 14;
    public static final short WLAN_MAP_EDGE       = (short) 12;
    public static final short WLAN_MAP_REVISION   = (short) 19;
    public static final short WLAN_MAP_VERTEX     = (short) 11;
    public static final short WLAN_WALK_DATA      = (short) 8;
    public static final short WLAN_WALK_DATA_LIST = (short) 20;
    public static final short WLAN_WALK_POS       = (short) 7;

    boolean deserialize(Deserializer deserializer);

    short getSerializeVersion();

    boolean serialize(Serializer serializer);
}
