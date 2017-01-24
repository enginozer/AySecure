package com.nevitech.aysecure.place.nav;

/**
 * Created by Emre on 23.1.2017.
 */
import java.io.Serializable;

@SuppressWarnings("serial")
public class FloorModel implements Comparable<FloorModel>,Serializable

{
    public String buid;
    public String description;
    public String floor_name;
    public String floor_number;

    public String bottom_left_lat;
    public String bottom_left_lng;
    public String top_right_lat;
    public String top_right_lng;

    public String toString()

    {

        return floor_number + " - [" + floor_name + "]";

    }

    public boolean isFloorValid()

    {

        if (floor_number == null || floor_number.equalsIgnoreCase("-") || floor_number.trim().isEmpty())

        {

            return false;

        }

        return true;

    }

    @Override
    public int compareTo(FloorModel arg0)

    {

        int compareQuantity = Integer.parseInt(arg0.floor_number);

        // ascending order
        return Integer.parseInt(floor_number) - compareQuantity;

    }

}
