package rbac;

import java.util.regex.Pattern;
import java.util.InputMismatchException;

public record User(String username, String fullName, String email) {

    private static final Pattern UN_PATTER = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern FN_PATTER = Pattern.compile("^[a-zA-Z\\s]+$");
    private static final Pattern EM_PATTER = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");

    public static User validate(String username, String fullName, String email){
        if (username == null)
            throw new IllegalArgumentException("Username not be empty");
        if (fullName == null)
            throw new IllegalArgumentException("Full name not be empty");
        if (email == null)
            throw new IllegalArgumentException("Email not be empty");

        if (!UN_PATTER.matcher((username)).matches())
            throw new IllegalArgumentException("Invalid format username. Username must not contain special characters");
        if (!FN_PATTER.matcher((fullName)).matches())
            throw new IllegalArgumentException("Invalid format full name. Username must not contain special characters and numbers");
        if (!EM_PATTER.matcher((email)).matches())
            throw new IllegalArgumentException("Invalid format email");

        return new User(username, fullName, email);
    }

    public String format(){
        return String.format("%s (%s) %s", username, fullName, email);
    }
}
