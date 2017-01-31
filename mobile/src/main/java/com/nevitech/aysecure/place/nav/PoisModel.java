package com.nevitech.aysecure.place.nav;

/**
 * Created by Emre on 23.1.2017.
 */
import java.io.Serializable;

public class PoisModel implements Serializable,IPoisClass

{
    public String  puid;
    public String  buid;
    public String  name;
    public String  description = "";
    public String  lat = "0.0";
    public String  lng = "0.0";
    public String  floor_name;
    public String  floor_number;
    public String  pois_type;
    public boolean is_building_entrance;

    public String toString()

    {

        return name + "[" + buid + "]";

    }


    public double lat()

    {

        return Double.parseDouble(lat);

    }


    public double lng()

    {

        return Double.parseDouble(lng);

    }


    public String name()

    {

        return name;

    }


    public String description()

    {

        return description;

    }


    public Type type()

    {

        return Type.NevitechPOI;

    }


    public String id()

    {

        return puid;

    }

}
