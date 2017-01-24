package com.nevitech.aysecure.place.nav;

/**
 * Created by Emre on 23.1.2017.
 */
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.nevitech.aysecure.place.MyApplication;
import com.nevitech.aysecure.place.cache.NevitechCache;
import com.nevitech.aysecure.place.tasks.FetchFloorsByBuidTask;
import com.nevitech.aysecure.place.tasks.FetchFloorsByBuidTask.FetchFloorsByBuidTaskListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;


@SuppressWarnings("serial")
public class BuildingModel implements Comparable<BuildingModel>,ClusterItem,Serializable

{
    public interface FetchBuildingTaskListener

    {

        void onErrorOrCancel(String result);

        void onSuccess(String result,
                       BuildingModel building);

    }

    public String buid = "";
    public String name;

    // public String description;
    // public String address;
    // public String url;
    public double latitude;
    public double longitude;

    // last fetched floors
    private List<FloorModel> mLoadedFloors = new ArrayList<FloorModel>(0);

    // List index Used in SelectBuilding Activity
    private int selectedFloorIndex         = 0;

    public List<FloorModel> getFloors()

    {

        return mLoadedFloors;

    }

    public void loadFloors(final FetchFloorsByBuidTaskListener l,
                           Context ctx,
                           boolean forceReload,
                           boolean showDialog)

    {

        if (!forceReload && isFloorsLoaded())

        {

            l.onSuccess("Successfully read from cache", mLoadedFloors);

        }
        else

        {

            new FetchFloorsByBuidTask(new FetchFloorsByBuidTask.FetchFloorsByBuidTaskListener()

            {

                @Override
                public void onSuccess(String result,
                                      List<FloorModel> floors)

                {

                    mLoadedFloors = floors;
                    NevitechCache.saveInstance(MyApplication.getAppContext());
                    l.onSuccess(result, floors);

                }

                @Override
                public void onErrorOrCancel(String result)

                {

                    l.onErrorOrCancel(result);

                }

            }, ctx,
               buid,
               showDialog).execute();

        }

    }

    public boolean isFloorsLoaded()

    {

        if (mLoadedFloors.size() == 0)

        {

            return false;

        }

        else

        {

            return true;

        }

    }

    public FloorModel getSelectedFloor()

    {

        FloorModel f = null;

        try

        {

            f = mLoadedFloors.get(selectedFloorIndex);

        }
        catch (IndexOutOfBoundsException ex)

        {


        }

        return f;

    }

    public int getSelectedFloorIndex()

    {

        return selectedFloorIndex;

    }

    public FloorModel getFloorFromNumber(String floor_number)

    {

        Integer index = checkFloorIndex(floor_number);

        if (index == null)

        {

            return null;

        }

        return mLoadedFloors.get(index);
    }

    // Set Currently Selected floor number
    public boolean setSelectedFloor(String floor_number)

    {

        Integer floor_index = checkFloorIndex(floor_number);

        if (floor_index != null)

        {

            selectedFloorIndex = floor_index;
            return true;

        }
        else

        {

            return false;

        }

    }

    // Set Currently Selected floor number (array index)
    public boolean checkIndex(int floor_index)

    {

        if (floor_index >= 0 && floor_index < mLoadedFloors.size())

        {

            return true;

        }
        else
        {

            return false;

        }
    }

    public Integer checkFloorIndex(String floor_number)

    {

        Integer index = null;

        for (int i = 0; i < mLoadedFloors.size(); i++)

        {
            FloorModel floorModel = mLoadedFloors.get(i);

            if (floorModel.floor_number.equals(floor_number))

            {

                index = i;
                break;

            }

        }

        return index;

    }

    @Override
    public String toString()

    {
        // return name + " [" + description + "]";
        return name;

    }

    public boolean equals(Object object2)

    {

        return object2 instanceof BuildingModel && buid.equals(((BuildingModel) object2).buid);

    }

    public String getLatitudeString()

    {

        return Double.toString(latitude);

    }

    public String getLongitudeString()

    {

        return Double.toString(longitude);

    }

    @Override
    public LatLng getPosition()

    {

        return new LatLng(latitude, longitude);

    }

    public void setPosition(String latitude,
                            String longitude)

    {

        this.latitude  = Double.parseDouble(latitude);
        this.longitude = Double.parseDouble(longitude);

    }

    @Override
    public int compareTo(BuildingModel arg0)

    {
        // ascending order
        return name.compareTo(arg0.name);

    }

}
