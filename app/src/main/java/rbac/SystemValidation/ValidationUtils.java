package rbac.SystemValidation;

import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern UN_PATTER = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern FN_PATTER = Pattern.compile("^[a-zA-Z\\s]+$");
    private static final Pattern EM_PATTER = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");
    private static final Pattern DATE_PATTERN =  Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$");

    static public boolean isValidUsername(String username){
        if (username == null || username.isEmpty())
            return false;
        if (!UN_PATTER.matcher((username)).matches())
            return false;

        return true;
    }

    static public boolean isValidFullName(String fullName){
        if (fullName == null || fullName.isEmpty())
            return false;
        if (!FN_PATTER.matcher((fullName)).matches())
            return false;

        return true;
    }

    static public boolean isValidEmail(String email){
        if (email == null || email.isEmpty())
            return false;
        if (!EM_PATTER.matcher((email)).matches())
            return false;

        return true;
    }

    static public boolean isValidDate(String date){
        if (date == null || date.isEmpty())
            return false;
        if (!DATE_PATTERN.matcher((date)).matches())
            return false;

        return true;
    }

    static public String normalizeString(String input){
        return input.trim().toLowerCase();
    }

    static public void requireNonEmpty(String value, String fieldName){
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " not be empty");
        }
    }
}
