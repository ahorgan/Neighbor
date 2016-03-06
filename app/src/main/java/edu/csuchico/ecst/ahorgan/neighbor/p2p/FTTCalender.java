package edu.csuchico.ecst.ahorgan.neighbor.p2p;

import java.util.ArrayList;

/**
 * Created by annika on 3/5/16.
 */
public class FTTCalender {
    class Weekday {
        private int value;
        private ArrayList<Hour> hours;
    }
    class Hour {
        int value;
        ArrayList<Minute> minutes;
    }
    class Minute {
        int value;
    }
}
