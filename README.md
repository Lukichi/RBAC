# RBAC

<h1>Подзадача 1: Создание базовых структур данных</h1>

<h2>Структура</h2>

- User - запись пользователя (ник, имя, почта)
- Permission - запись доступа (название права, ресурс для управления, описание)
- Role - класс ролей (ID, название, описание, права доступа Permission)
- AssignmentMetadata - мата-данные (кто назначил, дата и время, причина - не обязательно)
- RoleAssignment - интерфейс назначения ролей (ID, пользователь, назначенная роль, мета-данные, активно или нет, тип назначения)
- AbstractRoleAssignment - абстрактный класс RoleAssignment (ID, пользователь, роль, мета-данные)
- PermanentAssignment - постоянно назначенные роли, всегда активны (идет от AbstractRoleAssignment)
- TemporaryAssignment - временные роли, по истечению срока не активно (идет от AbstractRoleAssignment)

<h2>Паттерны и сравнение</h2>

- https://wiki.rakovets.by/java/core/misc/#_%D1%80%D0%B5%D0%B3%D1%83%D0%BB%D1%8F%D1%80%D0%BD%D1%8B%D0%B5_%D0%B2%D1%8B%D1%80%D0%B0%D0%B6%D0%B5%D0%BD%D0%B8%D1%8F
- https://javarush.com/groups/posts/regulyarnye-vyrazheniya-v-java

<h2>Интерфейс Set</h2>

- https://wiki.rakovets.by/java/core/java-collection-framework/#_interface_set

<h2>Дата и время</h2>

- https://wiki.rakovets.by/java/core/date-and-time/#_%D0%B4%D0%B0%D1%82%D0%B0_%D0%B8_%D0%B2%D1%80%D0%B5%D0%BC%D1%8F_since_version_8


<h1>Подзадача 2: Фильтры</h1>

<h2>Пометки</h2>

- endsWith - содержание подстроки в конце
- equals - содержание (полностью и только)
- contains - содержание (наличие)
- anyMatch - имеет ли хоть один

<h2>JUnit</h2>

- Статьи:
  - https://habr.com/ru/articles/590607/
- Методы:
  - assertEquals - сравнивает 2 списка, элемента (числа и тд)
  - Nested - пометки иерархии (обязательно)
  - assertThrows - проверяет, что код выдает исключение

<h2>2.1. Фильтрация пользователей</h2>

- UserFilter - для фильтра
  - boolean test(User user) - проверка условия
  - UserFilter and(UserFilter other) - объединяет 2 теста через И
  - UserFilter or(UserFilter other) - объединяет 2 теста чарез ИЛИ
- UserFilters - фильтр пользователей
  - UserFilter byUsername(String username) - проверка ника
  - UserFilter byUsernameContains(String substring) - содержание подстроки в нике
  - UserFilter byEmail(String email)  - проверка почты
  - UserFilter byEmailDomain(String domain)  - проверка домена (собака - прочее: @company.com)
  - UserFilter byFullNameContains(String substring)  - ФИО содержит часть
- RoleFilters - фильтр ролей
  - RoleFilter byName(String name) - проверка названия
  - RoleFilter byNameContains(String substring) - фильтрация по название (содержит подстроку)
  - RoleFilter hasPermission(Permission permission) - фильтрация прав доступа по объекту
  - RoleFilter hasPermission(String permissionName, String resource) - фильтрация прав доступа по названию и русерсу
  - RoleFilter hasAtLeastNPermissions(int n) - допускается от >= n прав доступа
- AssignmentFilter - фильтр мета-данных
  - AssignmentFilter byUser(User user) — назначения для конкретного пользователя
  - AssignmentFilter byUsername(String username) - соответствие ника пользователя
  - AssignmentFilter byRole(Role role) — назначения конкретной роли
  - AssignmentFilter byRoleName(String roleName) - соответствие роли пользователя
  - AssignmentFilter activeOnly() — только активные назначения
  - AssignmentFilter inactiveOnly() — только неактивные
  - AssignmentFilter byType(String type) — "PERMANENT" или "TEMPORARY"
  - AssignmentFilter assignedBy(String username) — кто назначил
  - AssignmentFilter assignedAfter(String date) — назначенные после даты
  - AssignmentFilter expiringBefore(String date) — временные назначения, истекающие до даты

<h2>2.4 Сортировка</h2>

- Comparator
    - https://wiki.rakovets.by/java/core/java-collection-framework/#_interface_comparable_and_comparator
    - Comparator.comparing - создание компаратора (инструкции для сортировки, котрый отвечает элемент больше, равен или меньше другого), в параметрах *поле для сравнения* - *признак*
    - String.CASE_INSENSITIVE_ORDER - 2 параметр (признак), сравнение без учета регистра
    - comparingInt - сравнивает числа (есть и с плавающей точкой)


<h1>Подзадача 3: Менеджеры данных</h1>

<h2>Optional</h2>

- https://wiki.rakovets.by/java/core/lambda-expressions/#_%D0%BA%D0%BB%D0%B0%D1%81%D1%81_optional
- https://javarush.com/groups/posts/3941-kofe-breyk-161-kak-obrabatihvatjh-null-v-java-s-pomojshjhju-optional
- Optional.ofNullable - если значение null, то само обработает

<h2>3.1. Интерфейс репозитория</h2>

- Repository<T> - общий интерфейс для работы с менеджерами (классами, хранящими данные)
  - void add(T item) - добавлять
  - boolean remove(T item) - удалять
  - Optional<T> findById(String id) - искать
  - List<T> findAll() - вернуть все
  - int count() - сколько
  - void clear() - очистить

<h2>3.2. Менеджер пользователей</h2>

- UserManager - класс для хранения данных пользователя
    - переопределяет методы из интерфейса
    - Optional<User> findByUsername(String username) - ищет по нику
    - Optional<User> findByEmail(String email) - ищет по почте
    - List<User> findByFilter(UserFilter filter) - ищет по фильтру
    - List<User> findAll(UserFilter filter, Comparator<User> sorter) (filter — что искать, sorter — в каком порядке вернуть)
    - boolean exists(String username) - проверяет существование
    - void update(String username, String newFullName, String newEmail) — обновить данные пользователя
- RoleManager - класс для хранения данных ролей
  - переопределённые методы из интерфейса
  - Optional<Role> findByName(String name) - ищет по названию
  - List<Role> findByFilter(RoleFilter filter) - фильтрует
  - List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) (filter — что искать, sorter — в каком порядке вернуть)
  - boolean exists(String name) - проверяет наличие
  - void addPermissionToRole(String roleName, Permission permission) - добавляет разрешения
  - void removePermissionFromRole(String roleName, Permission permission) - удаляет разрешения
  - List<Role> findRolesWithPermission(String permissionName, String resource) - ищет по названию и ресурсам, порядок обратный (обратный по отношению к добавлению ролей)
- AssignmentManager -класс для хранения данных назначения
  - переопределяет методы из интерфейса
  - List<RoleAssignment> findByUser(User user) - найти по пользователю
  - List<RoleAssignment> findByRole(Role role) - найти по роли
  - List<RoleAssignment> findByFilter(AssignmentFilter filter) - найти по фильрам
  - List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) (filter — что искать, sorter — в каком порядке вернуть) - найти и отсортировать
  - List<RoleAssignment> getActiveAssignments() - получить активных, сортируем по нику, иначе порядок рандомный
  - List<RoleAssignment> getExpiredAssignments() - получить неактивных, сортируем по нику, иначе порядок рандомный
  - boolean userHasRole(User user, Role role) - проверить роль у пользователя
  - boolean userHasPermission(User user, String permissionName, String resource) - проверить права доступа
  - Set<Permission> getUserPermissions(User user) — все права пользователя из всех его ролей
  - void revokeAssignment(String assignmentId) - убрать право доступа
  - void extendTemporaryAssignment(String assignmentId, String newExpirationDate) - продлит пользователю права до даты


<h1>Подзадача 4: Система команд и меню</h1>