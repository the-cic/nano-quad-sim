package com.cic.quadsim.nanoquad;

import com.jme3.math.FastMath;

/**
 *
 * A very lay simulation of a depleting battery.
 *
 * It was never intended to be an actual electronically correct simulation of
 * any type of real battery, just an approximation of how the observed toy's
 * battery behaves.
 *
 * If you are an electronics expert and my horribly incorrect terminology makes
 * you wince, please bear with me.
 *
 * @author cic
 */
public class Battery {

    // Drain on battery caused by the electronics running
    private final float constantDrain = 0.0001f;

    // Drain on battery caused by power output
    private final float outputDrain = 0.01f;

    // How much of full load (1) affects capacity
    private final float loadFactor = 0.1f;

    // Weight of converting drain to load
    private final float drainToLoadFactor = 0.7f;

    // Rate of recover of battery from load
    private final float loadRecover = 0.3f;

    // Various tresholds on capacity
    private final float batteryWarningTreshold = 0.30f;
    private final float batteryCutOutTreshold = 0.05f;
    private final float batteryCutInTreshold = 0.06f;

    // Weight for applying calculated "drain" value to capacity
    private float drainFactor;

    // "True" capacity, maximal available power from battery at the moment
    // Value from 0 to 1
    private float capacity;

    // Load, or "tiredness" of battery 
    // Value from 0 to 1
    private float load;

    // State of battery controller cutting out low battery to protect it from damage
    private boolean batteryCutOut;

    public Battery(float ratedCapacity) {
        setRatedCapacity(ratedCapacity);
        reset();
    }

    /**
     * Sets depletion rate on battery, as capacity max is 1.
     *
     * @param mAh arbitrary chosen "capacity" to match 100mAh to the base drain
     * as defined by other weights.
     */
    public void setRatedCapacity(float mAh) {
        if (mAh == 0) {
            mAh = 100;
        }
        drainFactor = 100 / mAh;
    }

    /**
     * Instant charge.
     */
    public void reset() {
        capacity = 1;
        load = 0;
        batteryCutOut = false;
    }

    /**
     * @return The "true", internal capacity of the battery
     */
    public float getCapacity() {
        return capacity;
    }

    /**
     * @return The capacity affected by current load on battery
     */
    public float getEffectiveCapacity() {
        return capacity - load * loadFactor;
    }

    /**
     * @return Load, or "tiredness" of battery
     */
    public float getLoad() {
        return load;
    }

    /**
     * Get power from battery and deplete it in the process.
     *
     * @param tpf Ask jMonkey ;P But I would say time per frame
     * @param requiredPower The power needed, that would be returned if battery
     * was full and rested
     *
     * @return Required power, reduced according to battery charge
     */
    public float getPower(float tpf, float requiredPower) {

        float effectiveCapacity = getEffectiveCapacity();

        // Protect empty battery from damage by cutting out output
        if (effectiveCapacity < batteryCutOutTreshold) {
            batteryCutOut = true;
        }

        // Available power on output curve 
        // (slowly descending while full with sharp drop at the end)
        float availablePower = batteryCutOut
                ? 0
                : FastMath.pow(effectiveCapacity, 1f / 3f);

        // Return value
        float outputPower = requiredPower * availablePower;

        float drain = constantDrain + outputPower * outputDrain;

        // Increase load by drain, more so the emptier the battery
        if (effectiveCapacity > 0.001) {
            load += drain * drainToLoadFactor / effectiveCapacity;
        }

        // Battery slowly recovers from load
        load -= loadRecover * tpf;

        // Simplify and limit load to just 0 - 1
        if (load > 1) {
            load = 1;
        }

        if (load < 0) {
            load = 0;
        }

        capacity -= drain * tpf * drainFactor;

        if (capacity < 0) {
            capacity = 0;
        }

        // When battery has recovered from load and has enough remaining 
        // capacity, cut power in again
        if (batteryCutOut && effectiveCapacity > batteryCutInTreshold && load < 0.1) {
            batteryCutOut = false;
        }

        return outputPower;
    }

    /**
     * @return Low power warning
     */
    public boolean hasWarning() {
        return capacity < batteryWarningTreshold;
    }
}
