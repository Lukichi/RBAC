package rbac.Components;

import rbac.SystemValidation.ValidationUtils;

import java.util.regex.Pattern;

public record User(String username, String fullName, String email) {

    private static final Pattern FN_PATTER = Pattern.compile("^[a-zA-Z\\s]+$");

    public static User create(String username, String fullName, String email){

        if (!ValidationUtils.isValidUsername(username))
            throw new IllegalArgumentException("Invalid format username. Username must not contain special characters and numbers and not be empty");
        if (!ValidationUtils.isValidEmail(email))
            throw new IllegalArgumentException("Invalid format email.");
        if (!ValidationUtils.isValidFullName(fullName))
            throw new IllegalArgumentException("Invalid format full name. Full name must not contain special characters and numbers and not be empty");

        return new User(username, fullName, email);
    }

    public String format(){
        return String.format("%s (%s) %s", username, fullName, email);
    }
}
