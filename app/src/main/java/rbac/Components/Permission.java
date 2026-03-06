package rbac.Components;

import rbac.SystemValidation.ValidationUtils;

import java.util.regex.Pattern;

public record Permission(String name, String resource, String description) {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Z]+$");
    private static final Pattern RES_PATTERN = Pattern.compile("^[a-z]+$");

    public Permission (String name, String resource, String description){
        String goodName, goodResource;

        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("Invalid name format. Name  not be empty");
        else
            goodName = name.toUpperCase();
        if (!NAME_PATTERN.matcher(goodName).matches())
            throw new IllegalArgumentException("Invalid name format. Name must contain only letters and not be empty");

        if (resource == null || resource.isEmpty())
            throw new IllegalArgumentException("Invalid resource format. Resource not be empty");
        else
            goodResource = resource.toLowerCase();
        if (!RES_PATTERN.matcher(goodResource).matches())
            throw new IllegalArgumentException("Invalid resource format. resource must contain only letters");

        this.name = goodName;
        this.resource = goodResource;
        this.description = description;
    }

    public String format(){
        return String.format("%s on %s: %s", name, resource, description);
    }

    public boolean matches(String namePattern, String resourcePattern){
        if (namePattern == null || resourcePattern == null)
            return false;

        boolean flag1 = this.name.contains(ValidationUtils.normalizeString(namePattern).toUpperCase());
        boolean flag2 = this.resource.contains(ValidationUtils.normalizeString(resourcePattern));

        return flag1 && flag2;
    }

}
