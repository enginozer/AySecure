package com.nevitech.aysecure.place.base;

/**
 * Created by Emre on 30.1.2017.
 */

public class Pos2D

{

    public float f37x;
    public float f38y;

    public float getX()

    {

        return this.f37x;

    }

    public void setX(float value)

    {

        this.f37x = value;

    }

    public float getY()

    {

        return this.f38y;

    }

    public Pos2D()

    {

        this.f37x = 0.0f;
        this.f38y = 0.0f;

    }

    public Pos2D(Pos2D pos2D)

    {

        this.f37x = pos2D.f37x;
        this.f38y = pos2D.f38y;

    }

    public Pos2D(float x,
                 float y)

    {

        this.f37x = x;
        this.f38y = y;

    }

    public void setY(float value)

    {

        this.f38y = value;

    }

}
