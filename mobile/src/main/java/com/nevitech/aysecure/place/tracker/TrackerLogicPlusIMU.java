package com.nevitech.aysecure.place.tracker;

/**
 * Created by Emre on 25.1.2017.
 */
import java.util.ArrayList;
import java.util.List;

import com.nevitech.aysecure.place.sensors.MovementDetector;
import com.nevitech.aysecure.place.sensors.SensorsMain;
import com.nevitech.aysecure.place.sensors.SensorsStepCounter;
import com.nevitech.aysecure.place.tracker.IMU.TrackedLocAnyplaceIMUListener;
import com.google.android.gms.maps.model.LatLng;

public class TrackerLogicPlusIMU extends NevitechTracker

{
    private static final int IMU_RESET_TIME                     = 20000;

    private List<TrackedLocAnyplaceTrackerListener> tllisteners = new ArrayList<TrackedLocAnyplaceTrackerListener>();

    public void addListener(TrackedLocAnyplaceTrackerListener list)

    {

        tllisteners.add(list);

    }

    public void removeListener(TrackedLocAnyplaceTrackerListener list)

    {

        tllisteners.remove(list);

    }

    private void triggerTrackedLocListeners(LatLng pos)

    {

        for (TrackedLocAnyplaceTrackerListener l : tllisteners)

        {

            l.onNewLocation(pos);

        }

    }

    // AnyplaceTracker
    private boolean reset = false;
    private KalmanFilter  kalmanFilter;
    private RunningMedian runningMedian;
    // </IMU
    private IMU      imu;
    private Long     lastIMUresetTimestamp;
    private LatLng   resetIMUPoint;
    private boolean  resetIMU = false;
    // />
    private boolean walkingTracker_old = false;
    private boolean walking            = false;

    public TrackerLogicPlusIMU(MovementDetector   movementDetector,
                               SensorsMain        sensorsMain,
                               SensorsStepCounter sensorsStep)

    {
        super(sensorsMain);

        // Add listeners from MOVEMENT DETECTOR, TRACKER
        movementDetector.addStepListener(new WalkingListener());

        super.addListener( new TrackerListenerInit(sensorsMain, sensorsStep));

    }

    private class WalkingListener implements MovementDetector.MovementListener

    {

        @Override
        public void onWalking()

        {

            walking = true;

        }

        @Override
        public void onStanding()

        {

            // TODO Auto-generated method stub
            walking = false;

        }

    }

    class TrackerListenerInit implements NevitechTracker.TrackedLocAnyplaceTrackerListener

    {

        SensorsMain        sensorsMain;
        SensorsStepCounter sensorsStep;

        TrackerListenerInit(SensorsMain        sensorsMain,
                            SensorsStepCounter sensorsStep)

        {

            this.sensorsMain = sensorsMain;
            this.sensorsStep = sensorsStep;

        }

        @Override
        public void onNewLocation(LatLng pos)

        {
            // Call this method only for Initialisation
            TrackerLogicPlusIMU.super.removeListener(this);
            TrackerLogicPlusIMU.super.addListener( new TrackerListener());

            kalmanFilter  = new KalmanFilter(pos.latitude, pos.longitude);
            runningMedian = new RunningMedian(pos.latitude, pos.longitude);
            imu           = new IMU(sensorsMain, sensorsStep, pos.latitude, pos.longitude);

            lastIMUresetTimestamp = System.currentTimeMillis();

            resetIMU      = walking;
            resetIMUPoint = pos;
            triggerTrackedLocListeners(pos);

            imu.addListener(new IMUListener());

        }

    }

    // Callback from Tracker
    class TrackerListener implements NevitechTracker.TrackedLocAnyplaceTrackerListener

    {

        // RUNS IN BACKGROUND THREAD
        @Override
        public void onNewLocation(LatLng pos)

        {

            if (reset)

            {

                reset = false;
                kalmanFilter.reset(pos.latitude, pos.longitude);
                runningMedian.reset(pos.latitude, pos.longitude);
                imu.reset(pos.latitude, pos.longitude);

            }


            if (walking)

            {
                // Walking
                // Reset Kalman filter if Standing Update Triggered
                if (walkingTracker_old != true)

                {

                    walkingTracker_old = true;
                    kalmanFilter.reset(pos.latitude, pos.longitude);

                }
                else

                {

                    pos = kalmanFilter.update(pos.latitude, pos.longitude);

                }

                long timestamp = System.currentTimeMillis();

                if (timestamp - lastIMUresetTimestamp > IMU_RESET_TIME)

                {

                    imu.reset(pos.latitude, pos.longitude);
                    lastIMUresetTimestamp = timestamp;

                }

            }
            else
            {// Standing

                if (walkingTracker_old != false)

                {

                    walkingTracker_old = false;
                    runningMedian.reset(pos.latitude, pos.longitude);
                    resetIMUPoint = pos;

                }
                else

                {

                    resetIMUPoint = runningMedian.update(pos.latitude, pos.longitude);

                }

                resetIMU = true;
                triggerTrackedLocListeners(resetIMUPoint);

            }

        }

    }

    // Callback from IMU
    private class IMUListener implements TrackedLocAnyplaceIMUListener

    {

        // Runs on UI THREAD
        @Override
        public void onNewLocation(LatLng pos)

        {

            if (walking)

            {

                if (resetIMU == true)

                {
                    // User was standing still and now moves
                    resetIMU = false;
                    imu.reset(resetIMUPoint.latitude, resetIMUPoint.longitude);
                    lastIMUresetTimestamp = System.currentTimeMillis();

                }
                else

                {

                    triggerTrackedLocListeners(pos);

                }

            }

        }

    }

    @Override
    public void trackOff()

    {
        // Wait before Change listener because a separate thread may change
        // it!!!!
        super.trackOff();
        reset = true;

    }

    @Override
    public void setAlgorithm(String name)

    {

        super.setAlgorithm(name);
        reset = true;

    }

    public void reset()

    {

        reset = true;

    }

}
