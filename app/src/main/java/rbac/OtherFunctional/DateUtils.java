package rbac.OtherFunctional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class DateUtils {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String getCurrentDate(){
        String time = LocalDateTime.now().format(DATE);
        return time;
    }

    public static String getCurrentDateTime() {
        String time = LocalDateTime.now().format(DATE_TIME);
        return time;
    }

    public static boolean isBefore(String date1, String date2) {
        if (date1 == null || date2 == null || date1.isEmpty() || date2.isEmpty()) {
            throw new IllegalArgumentException("Date not de null or empty");
        }

        String[] mass1 = date1.split(" ");
        String[] mass2 = date2.split(" ");

        String firstDate = mass1[0].trim();
        String secondDate = mass2[0].trim();

        if (firstDate.isEmpty() || secondDate.isEmpty()) {
            throw new IllegalArgumentException("Date not de null or empty");
        }

        String[] daysMassFirst = firstDate.split("-");
        String[] daysMassSecond = secondDate.split("-");
        if (daysMassFirst.length < 3 || daysMassSecond.length < 3) {
            throw new IllegalArgumentException("First part date must be format: yyyy-MM-dd");
        }

        boolean flagDays = false;
        int win = 0;
        for (int i=0; i<3; i++) {
            int firstNum = Integer.parseInt(daysMassFirst[i]);
            int secondtNum = Integer.parseInt(daysMassSecond[i]);
            if (firstNum > secondtNum) {
                flagDays = true;
                win = 1;
            }
            else if(firstNum < secondtNum) {
                flagDays = true;
                win = 2;
            }
        }

        if (flagDays) {
            return win == 1;
        }

        String firstDate2 = mass1[1].trim();
        String secondDate2 = mass2[1].trim();
        if (!firstDate2.isEmpty() || !secondDate2.isEmpty()) {
            String[] timeMassFirst = firstDate2.split(":");
            String[] timeMassSecond = secondDate2.split(":");
            if (timeMassFirst.length < 3 || timeMassSecond.length < 3) {
                return win == 1 || win == 0;
            }

            for (int i=0; i<3; i++) {
                int firstNum = Integer.parseInt(timeMassFirst[i]);
                int secondtNum = Integer.parseInt(timeMassSecond[i]);
                if (firstNum > secondtNum) {
                    win = 1;
                    break;
                }
                else if(firstNum < secondtNum) {
                    win = 2;
                    break;
                }
            }
        }

        return win == 1 || win == 0;
    }

    public static boolean isAfter(String date1, String date2) {
        return !isBefore(date1, date2);
    }

    public static String addDays(String date, int days) {
        if (date.split(" ").length < 2) {
            String[] dataDate = date.split("-");
            if (dataDate.length < 3) {
                throw new IllegalArgumentException("First part date must be format: yyyy-MM-dd");
            }

            int year = Integer.parseInt(dataDate[0]);
            int month = Integer.parseInt(dataDate[1]);
            int day = Integer.parseInt(dataDate[2]) + days;

            while (day > 30) {
                day -= 30;
                month++;
            }

            while (month > 12) {
                month -= 12;
                year++;
            }

            String dayString = "", yearString = "", monthString = "";
            if (day < 10)
                dayString = "0" + day;
            else
                dayString += day;

            if (month < 10)
                monthString = "0" + month;
            else
                monthString += month;

            yearString += year;

            String res = String.format("%s-%s-%s", yearString, monthString, dayString);
            return res.trim();
        }
        else {
            String inputFist = date.split(" ")[0];
            String inputSecond = date.split(" ")[1];

            String[] dataDate = inputFist.split("-");
            if (dataDate.length < 3) {
                throw new IllegalArgumentException("First part date must be format: yyyy-MM-dd");
            }

            int year = Integer.parseInt(dataDate[0]);
            int month = Integer.parseInt(dataDate[1]);
            int day = Integer.parseInt(dataDate[2]) + days;

            while (day > 30) {
                day -= 30;
                month++;
            }

            while (month > 12) {
                month -= 12;
                year++;
            }

            String dayString = "", yearString = "", monthString = "";
            if (day < 10)
                dayString = "0" + day;
            else
                dayString += day;

            if (month < 10)
                monthString = "0" + month;
            else
                monthString += month;

            yearString += year;

            String res = String.format("%s-%s-%s %s", yearString, monthString, dayString, inputSecond);
            return res;
        }
    }

    public static String formatRelativeTime(String date) {
        String input = date.split(" ")[0];
        String[] dataDate = input.split("-");
        if (dataDate.length < 3) {
            throw new IllegalArgumentException("First part date must be format: yyyy-MM-dd");
        }

        int year = Integer.parseInt(dataDate[0]);
        int month = Integer.parseInt(dataDate[1]);
        int day = Integer.parseInt(dataDate[2]);

        String[] time = LocalDateTime.now().format(DATE).split("-");
        int yearNow = Integer.parseInt(time[0]);
        int monthNow = Integer.parseInt(time[1]);
        int dayNow = Integer.parseInt(time[2]);

        long  nowDay = yearNow * 365 + monthNow * 30 + dayNow;
        long  inputDay = year * 365 + month * 30 + day;
        int res = Math.toIntExact(inputDay - nowDay);

        if (res < 0) {
            return Math.abs(res) + " days ago";
        }
        else {
            return "in " + Math.abs(res) + " days";
        }
    }
}
