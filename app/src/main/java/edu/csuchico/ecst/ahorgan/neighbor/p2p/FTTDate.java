package edu.csuchico.ecst.ahorgan.neighbor.p2p;

import java.util.Calendar;
import java.util.Date;
import java.lang.Math

/**
 * Created by annika on 3/5/16.
 */
public class FTTDate {
    int date;
    private FTTDate next;

    public FTTDate() {
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minute = Calendar.getInstance().get(Calendar.MINUTE);
        date = 0;
        translateDateIn(dayOfWeek, hour, minute);
    }

    public void translateDateIn(int dayOfWeek, int hour, int minute) {
        /*
            Translate Minutes
         */
        if(minute < 30) {
            if(minute < 15)
                date = 0x01;
            else
                date = 0x02;
        }
        else {
            if(minute < 45)
                date = 0x04;
            else
                date = 0x08;
        }

        /*
            Translate Hours
            Starts with 4 bc hour starts counting at 0
         */
        date += (int)Math.pow(2,(4+hour));

        /*
            Translate Day of Week
            Starts with 27 bc week starts counting at 1
         */
        date += (int)Math.pow(2,(27+dayOfWeek));
    }

    public int getWeekDay() {
        Calendar.getInstance().set(Calendar.)
    }

    public void addNewDate(FTTDate newDate) {
        int merge = newDate.date | date;
        /*
            If not the same date
         */
        if((merge & date) != date) {
            int weekDayMask = (int)Math.pow(2.0,35)-1 - (int)Math.pow(2.0,28)-1;
            int minuteMask = (int)Math.pow(2.0, 4)-1;
            int hourMask = (int)Math.pow(2.0, 28)-1 - minuteMask;

            int mergeWeek = weekDayMask & merge;
            int mergeMinute = minuteMask & merge;
            int mergeHour = hourMask & merge;

            /*
                If hours are the same, then either
                    continuation of 15 minute intervals
                    or same time occurring multiple days per week
                Otherwise not related, insert into next
             */
            if((date & hourMask) == mergeHour) {
                /*
                    Continuation of 15 minute intervals
                 */
                if((date & weekDayMask) == mergeWeek) {
                    date = ((newDate.date | date) & minuteMask) +
                            (date & hourMask) +
                            (date & weekDayMask);
                    return;
                }
                /*
                    Same time occurring multiple days per week
                 */
                else if((date & minuteMask) == mergeMinute) {
                    date = ((newDate.date | date) & weekDayMask) +
                            (date & hourMask) +
                            (date & minuteMask);
                    return;
                }
            }
            newDate.next = this.next;
            this.next = newDate;
        }
    }
}
