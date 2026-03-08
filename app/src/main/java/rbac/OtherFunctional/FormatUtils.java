package rbac.OtherFunctional;

import java.util.List;

public class FormatUtils {

    public static final String RED = "\u001B[31m";
    public static final String WHITE = "\u001B[37m";
    public static final String RESET = "\u001B[0m";
    public static final String GREEN = "\u001B[32m";

    public static final String BOLD = "\u001B[1m";

    public static String formatTable(String[] headers, List<String[]> rows) {
        int[] sizes = new int[headers.length];
        for (int i=0; i < headers.length; i++) {
            int maxSize = headers[i].length();
            for (String[] mass : rows) {
                if (mass[i].trim().length() > maxSize)
                    maxSize = mass[i].length();
            }
            sizes[i] = maxSize + 2;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(RED + BOLD + "+");
        for (int i=0; i < headers.length; i++) {
            sb.append("-".repeat(sizes[i]) + "+");
        }
        sb.append("\n|");
        for (int i=0; i < headers.length; i++) {
            sb.append(String.format("%-" + sizes[i] + "s", headers[i].trim()) + "|");
        }
        sb.append("\n+");
        for (int i=0; i < headers.length; i++) {
            sb.append("-".repeat(sizes[i]) + "+");
        }
        sb.append(RESET + "\n|");
        int help = 0;
        for (String[] mass : rows) {
            if (help > 0)
                sb.append("|");
            for (int i=0; i < headers.length; i++) {
                sb.append(String.format("%-" + sizes[i] + "s", mass[i].trim()) + "|");
            }
            help ++;
            sb.append("\n");
        }
        sb.append("+");
        for (int i=0; i < headers.length; i++) {
            sb.append("-".repeat(sizes[i]) + "+");
        }

        return sb.toString();
    }

    public static String formatBox(String text) {
        String[] mass = text.split("\n");
        int maxSize = 0;
        for (int i = 0; i < mass.length; i++) {
            if (mass[i].length() > maxSize) {
                maxSize = mass[i].length();
            }
        }
        maxSize += 2;

        StringBuilder sb = new StringBuilder();
        sb.append("+" + "-".repeat(maxSize) + "+");
        for (int i = 0; i < mass.length; i++) {
            sb.append(String.format("\n|%-" + maxSize + "s" + "|", mass[i]));
        }
        sb.append("\n+" + "-".repeat(maxSize) + "+");

        return sb.toString();
    }

    public static String formatHeader(String text) {

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("*".repeat(text.length() + 6)));
        sb.append(String.format("\n*" + " ".repeat(2) + text + " ".repeat(2) + "*\n"));
        sb.append(String.format("*".repeat(text.length() + 6)));

        return sb.toString();
    }

    public static String truncate(String text, int maxLength) {
        return text.substring(0, maxLength - 2) + "...";
    }

    public static String padRight(String text, int length) {
        StringBuilder sb = new StringBuilder();
        if (length - text.length() < 0){
            return truncate(text,length);
        }
        else {
            return String.format(text + " ".repeat(length - text.length()));
        }
    }

    public static String padLeft(String text, int length) {
        StringBuilder sb = new StringBuilder();
        if (length - text.length() < 0) {
            return truncate(text,length);
        }
        else {
            return String.format(" ".repeat(length - text.length()) + text);
        }
    }
}
