package edu.csuchico.ecst.ahorgan.neighbor.FTT;

import java.util.ArrayList;

/**
 * Created by annika on 3/6/16.
 */
public class FTTEvent {
    class Weekday {
        private int value;
        private Hour hour;

        public Weekday(int value) {
            this.value = value;
        }

        public void addHour(Hour hour) {
            this.hour = hour;
        }

        public int getValue() {
            return value;
        }

        public Hour getHour() {
            return hour;
        }
    }
    class Hour {
        int value;
        ArrayList<Minute> minutes;

        public Hour(int value) {
            this.value = value;
            minutes = new ArrayList<>();
        }

        public void addMinute(Minute minute) {
            minutes.add(minute);
        }

        public int getValue() {
            return value;
        }

        public ArrayList<Minute> getMinutes() {
            return minutes;
        }
    }
    class Minute {
        int value;

        public Minute(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    ArrayList<Weekday> event;

    ArrayList<Integer> getDaysOfWeek() {
        ArrayList<Integer> daysOfWeek = new ArrayList<>();
        for(Weekday day : event) {
            daysOfWeek.add(day.getValue());
        }
        return daysOfWeek;
    }
}
