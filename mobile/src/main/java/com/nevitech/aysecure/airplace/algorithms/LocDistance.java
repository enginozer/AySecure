package com.nevitech.aysecure.airplace.algorithms;

/**
 * Created by Emre on 23.1.2017.
 */

public class LocDistance

{
    private double distance;
    private String location;

    public LocDistance(double distance,
                       String location)

    {

        this.distance = distance;
        this.location = location;

    }

    public double getDistance()

    {

        return distance;

    }

    public String getLocation()

    {

        return location;

    }

}
