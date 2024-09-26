package me.test;

import java.util.Calendar;

public class DateTest {

    public static void addMonthToCalendar() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, 2);
        Integer expiryYear = c.get(Calendar.YEAR) - 2000;
        Integer expiryMonth = c.get(Calendar.MONTH);

        System.out.println("Year: [" + expiryYear + "]");
        System.out.println("Month: [" + expiryMonth + "]");
    }

}
