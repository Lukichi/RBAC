# RBAC

<h1>Подзадача 1: Создание базовых структур данных</h1>

<h2>Структура</h2>
+ User - запись пользователя (ник, имя, почта)
+ Permission - запись доступа (название права, ресурс для управления, описание)
+ Role - класс ролей (ID, название, описание, права доступа Permission)
+ AssignmentMetadata - мата-данные (кто назначил, дата и время, причина - не обязательно)
+ RoleAssignment - интерфейс назначения ролей (ID, пользователь, назначенная роль, мета-данные, активно или нет, тип назначения)
+ AbstractRoleAssignment - абстрактный класс RoleAssignment (ID, пользователь, роль, мета-данные)
+ PermanentAssignment - постоянно назначенные роли, всегда активны (идет от AbstractRoleAssignment)
+ TemporaryAssignment - временные роли, по истечению срока не активно (идет от AbstractRoleAssignment)

<h2>Паттерны и сравнение</h2>
+ https://wiki.rakovets.by/java/core/misc/#_%D1%80%D0%B5%D0%B3%D1%83%D0%BB%D1%8F%D1%80%D0%BD%D1%8B%D0%B5_%D0%B2%D1%8B%D1%80%D0%B0%D0%B6%D0%B5%D0%BD%D0%B8%D1%8F
+ https://javarush.com/groups/posts/regulyarnye-vyrazheniya-v-java

<h2>Интерфейс Set</h2>
+ https://wiki.rakovets.by/java/core/java-collection-framework/#_interface_set

<h2>Дата и время</h2>
+ https://wiki.rakovets.by/java/core/date-and-time/#_%D0%B4%D0%B0%D1%82%D0%B0_%D0%B8_%D0%B2%D1%80%D0%B5%D0%BC%D1%8F_since_version_8


<h1>Подзадача 2: Фильтры</h1>

<h2>Пометки</h2>
+ endsWith - содержание подстроки в конце
+ equals - содержание (полностью и только)
+ contains - содержание (наличие)
+ anyMatch - имеет ли хоть один

<h2>JUnit</h2>
+ Статьи:
  + https://habr.com/ru/articles/590607/
+ Методы:
  + assertEquals - сравнивает 2 списка, элемента (числа и тд)
  + Nested - пометки иерархии (обязательно)

<h2>2.1. Фильтрация пользователей</h2>
+ UserFilter - для фильтра
  + boolean test(User user) - проверка условия
  + UserFilter and(UserFilter other) - объединяет 2 теста через И
  + UserFilter or(UserFilter other) - объединяет 2 теста чарез ИЛИ
+ UserFilters - фильтр пользователей
  + UserFilter byUsername(String username) - проверка ника
  + UserFilter byUsernameContains(String substring) - содержание подстроки в нике
  + UserFilter byEmail(String email)  - проверка почты
  + UserFilter byEmailDomain(String domain)  - проверка домена (собака + прочее: @company.com)
  + UserFilter byFullNameContains(String substring)  - ФИО содержит часть
+ RoleFilters - фильтр ролей
  + RoleFilter byName(String name) - проверка названия
  + RoleFilter byNameContains(String substring) - фильтрация по название (содержит подстроку)
  + RoleFilter hasPermission(Permission permission) - фильтрация прав доступа по объекту
  + RoleFilter hasPermission(String permissionName, String resource) - фильтрация прав доступа по названию и русерсу
  + RoleFilter hasAtLeastNPermissions(int n) - допускается от >= n прав доступа
+ AssignmentFilter - фильтр мета-данных
  + AssignmentFilter byUser(User user) — назначения для конкретного пользователя
  + AssignmentFilter byUsername(String username) - соответствие ника пользователя
  + AssignmentFilter byRole(Role role) — назначения конкретной роли
  + AssignmentFilter byRoleName(String roleName) - соответствие роли пользователя
  + AssignmentFilter activeOnly() — только активные назначения
  + AssignmentFilter inactiveOnly() — только неактивные
  + AssignmentFilter byType(String type) — "PERMANENT" или "TEMPORARY"
  + AssignmentFilter assignedBy(String username) — кто назначил
  + AssignmentFilter assignedAfter(String date) — назначенные после даты
  + AssignmentFilter expiringBefore(String date) — временные назначения, истекающие до даты