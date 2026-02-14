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


