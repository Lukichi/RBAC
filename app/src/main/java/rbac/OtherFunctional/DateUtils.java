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
            throw new IllegalArgumentException("Date cannot be null or empty");
        }

        String[] parts1 = date1.split(" ");
        String[] parts2 = date2.split(" ");

        String firstDate = parts1[0].trim();
        String secondDate = parts2[0].trim();

        String[] dateParts1 = firstDate.split("-");
        String[] dateParts2 = secondDate.split("-");

        if (dateParts1.length < 3 || dateParts2.length < 3) {
            throw new IllegalArgumentException("Date must be format: yyyy-MM-dd");
        }

        int year1 = Integer.parseInt(dateParts1[0]);
        int month1 = Integer.parseInt(dateParts1[1]);
        int day1 = Integer.parseInt(dateParts1[2]);

        int year2 = Integer.parseInt(dateParts2[0]);
        int month2 = Integer.parseInt(dateParts2[1]);
        int day2 = Integer.parseInt(dateParts2[2]);

        if (year1 < year2) return true;
        if (year1 > year2) return false;

        if (month1 < month2) return true;
        if (month1 > month2) return false;

        if (day1 < day2) return true;
        if (day1 > day2) return false;

        if (parts1.length > 1 && parts2.length > 1) {
            String time1 = parts1[1].trim();
            String time2 = parts2[1].trim();

            if (!time1.isEmpty() && !time2.isEmpty()) {
                String[] timeParts1 = time1.split(":");
                String[] timeParts2 = time2.split(":");

                int hour1 = Integer.parseInt(timeParts1[0]);
                int minute1 = Integer.parseInt(timeParts1[1]);
                int second1 = Integer.parseInt(timeParts1[2]);

                int hour2 = Integer.parseInt(timeParts2[0]);
                int minute2 = Integer.parseInt(timeParts2[1]);
                int second2 = Integer.parseInt(timeParts2[2]);

                if (hour1 < hour2) return true;
                if (hour1 > hour2) return false;

                if (minute1 < minute2) return true;
                if (minute1 > minute2) return false;

                return second1 < second2;
            }
        }

        return false;
    }

    public static boolean isAfter(String date1, String date2) {
        return !isBefore(date1, date2) && !date1.equals(date2);
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
