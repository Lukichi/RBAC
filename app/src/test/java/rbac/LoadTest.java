package rbac;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import rbac.CommandAndMenuSystem.RBACSystem;
import rbac.Components.*;
import rbac.Filters.UserFilters;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Tag("load")
public class LoadTest {

    RBACSystem system;

    private static final int THREAD_COUNT = 5;
    private static final int OPERATIONS_PER_THREAD = 20;

    @BeforeEach
    void initialData() {
        system = new RBACSystem();
        system.initialize();
        system.setCurrentUser("admin");
        system.startExpiredAssignmentsCleaner(20);

        User adminUser = RBACSystem.getUserManager().findByUsername("admin").orElse(null);
        Role adminRole = RBACSystem.getRoleManager().findByName("admin").orElse(null);
        for(int i=0; i < THREAD_COUNT; i++) {
            String name = "admin" + (i + 1);
            String email = "admin" + (i + 1) + "@mail.ru";
            User user = new User(name, "User Fot Tests", email);
            RBACSystem.getUserManager().add(user);
            AssignmentMetadata metadataAdmin = AssignmentMetadata.now("admin", "Initial data");
            PermanentAssignment assignmentAdmin = new PermanentAssignment(user, adminRole, metadataAdmin);
            RBACSystem.getAssignmentManager().add(assignmentAdmin);
        }
    }

    @AfterEach
    void down() {
        system.shutdown();
    }

    @Test
    void testLoad() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        List<String> allCreateUsers = new CopyOnWriteArrayList<>();

        AtomicInteger errorCount = new AtomicInteger(0);
        List<Exception> errorList = new CopyOnWriteArrayList<>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;

            String name = "admin" + (i + 1);
            executor.submit(() -> {
                system.setCurrentUser(name);
                Random random = new Random();
                List<String> myUsers = new ArrayList<>();

                for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                    try {
                        int operation = random.nextInt(6);

                        switch (operation) {
                            case 0:
                                String username = "user_" + threadId + "_" + j + "_" + System.nanoTime();
                                String fullName = "User " + username;
                                String email = username + "@test.com";
                                User newUser = new User(username, fullName, email);

                                RBACSystem.getUserManager().add(newUser);
                                myUsers.add(username);
                                allCreateUsers.add(username);
                                successCount.incrementAndGet();
                                break;
                            case 1:
                                if (!myUsers.isEmpty()) {
                                    String userToUpdate = myUsers.get(random.nextInt(myUsers.size()));
                                    String newName = "Updated Name";
                                    String newEmail = "updated_" + System.currentTimeMillis() + "@test.com";

                                    RBACSystem.getUserManager().update(userToUpdate, newName, newEmail);
                                }
                                successCount.incrementAndGet();
                                break;
                            case 2:
                                if (!myUsers.isEmpty()) {
                                    String userToAssign = myUsers.get(random.nextInt(myUsers.size()));
                                    Role role = RBACSystem.getRoleManager().findByName("viewer").orElse(null);
                                    User user = RBACSystem.getUserManager().findByUsername(userToAssign).orElse(null);
                                    int num = random.nextInt(2);

                                    if (num == 0) {
                                        AssignmentMetadata metadata = AssignmentMetadata.now(name, "Load test");
                                        TemporaryAssignment temp = new TemporaryAssignment(user, role, metadata);
                                        RBACSystem.getAssignmentManager().add(temp);
                                    }
                                    else {
                                        AssignmentMetadata metadata = AssignmentMetadata.now(name, "Load test");
                                        PermanentAssignment per = new PermanentAssignment(user, role, metadata);
                                        RBACSystem.getAssignmentManager().add(per);
                                    }
                                }
                                successCount.incrementAndGet();
                                break;

                            case 3:
                                List<User> filtered = RBACSystem.getUserManager().findByFilterParallel(UserFilters.byUsername(name));
                                assertNotNull(filtered);
                                successCount.incrementAndGet();
                                break;

                            case 4:
                                Optional<Role> found = RBACSystem.getRoleManager().findByName("viewer");
                                assertTrue(found.isPresent(), "Error check have role");
                                successCount.incrementAndGet();
                                break;

                            case 5:
                                User user = RBACSystem.getUserManager().findByUsername(name).orElse(null);
                                Set<Permission> permissions = RBACSystem.getAssignmentManager().getUserPermissions(user);
                                assertNotNull(permissions);
                                successCount.incrementAndGet();
                                break;
                        }

                        if (j % 3 == 0) {
                            Thread.sleep(5);
                        }

                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        errorList.add(e);
                        System.err.println("Error in thread " + threadId + ": " + e.getMessage());
                    }
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(90, TimeUnit.SECONDS);

        System.out.println("Complite " + successCount);
        System.out.println("Errors " + errorCount);

        if (errorCount.get() > 0) {
            System.err.println("\nExceptions occurred:");
            for (Exception e : errorList) {
                System.err.println("  - " + e.getMessage());
            }
        }

        int allCount = THREAD_COUNT * OPERATIONS_PER_THREAD;
        int succressNum = successCount.get();
        assertEquals(allCount, succressNum);
    }
}
