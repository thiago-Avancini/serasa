package br.com.serasa.serasa.reading;

public enum SessionEvent {

    /** Nothing state-changing happened; still accumulating or still idle/stabilized. */
    NONE,

    /** First reading above the empty threshold after being idle: a truck just stepped on the scale. */
    STARTED,

    /** The reading window settled within tolerance: this is the weight to persist. */
    STABILIZED,

    /** Weight dropped back to (near) empty after an active session: the truck left. */
    RESET
}