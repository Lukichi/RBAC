package rbac.Managers;

import rbac.Filters.UserFilter;
import rbac.Components.Repository;
import rbac.Components.User;
import rbac.SystemValidation.ValidationUtils;

import java.util.*;
import java.util.regex.Pattern;

public class UserManager implements Repository<User> {

    private static final Pattern FN_PATTER = Pattern.compile("^[a-zA-Z\\s]+$");

    private Map<String, User> usersData = new HashMap<>();

    @Override
    public void add(User item) {
        if (item == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        String key = item.username();
        if (usersData.containsKey(key)) {
            throw new IllegalArgumentException("User with username '" + key + "' already register");
        }

        usersData.put(key, item);
    }

    public boolean remove(User item) {
        if (item == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        String key = item.username();
        User res = usersData.remove(key);
        return res != null;
    }

    public Optional<User> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(usersData.get(id));
    }

    public List<User> findAll() {
        return usersData.values().stream().toList();
    }

    public int count() {
        return usersData.size();
    }

    public void clear() {
        usersData.clear();
    }

    public Optional<User> findByUsername(String username){
        return findById(username);
    }

    public Optional<User> findByEmail(String email){
        if (email == null || email.trim().isEmpty())
            return Optional.empty();


        return usersData.values().stream().filter(user -> email.equalsIgnoreCase(user.email())).findFirst();
    }

    public List<User> findByFilter(UserFilter filter){
        return usersData.values().stream().filter(filter::test).toList();
    }

    public List<User> findAll(UserFilter filter, Comparator<User> sorter){
        List<User> afterFilter, afterSort;
        if (filter != null)
            afterFilter = usersData.values().stream().filter(filter::test).toList();
        else
            afterFilter = usersData.values().stream().toList();

        if (sorter != null)
            afterSort = afterFilter.stream().sorted(sorter).toList();
        else
            afterSort  = afterFilter;

        return afterSort;
    }

    public boolean exists(String username){
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        return usersData.containsKey(username);
    }

    public void update(String username, String newFullName, String newEmail){
        String newFN, newE;

        User user = usersData.get(username);

        if (user == null)
            throw new IllegalArgumentException("User with username '" + username + "' not register");

        if (newFullName == null && newEmail == null)
            return;

        if (newFullName.isEmpty()) {
            newFN = user.fullName();
        }
        else {
            if (!FN_PATTER.matcher((newFullName)).matches())
                throw new IllegalArgumentException("Invalid format full name. Username must not contain special characters and numbers");
            newFN = newFullName;
        }

        if (newEmail.isEmpty())
            newE = user.email();
        else {
            if (!ValidationUtils.isValidEmail(newEmail))
                throw new IllegalArgumentException("Invalid format email");
            newE = newEmail;
        }

        User newUser = new User(username, newFN, newE);

        usersData.put(username, newUser);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserManager that = (UserManager) o;
        return Objects.equals(usersData, that.usersData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usersData);
    }


}
