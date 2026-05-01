<h2>Команды для работы</h2>

- Работает: curl.exe -X **тип** путь + данные (если используем RequestParam) -H "Content-Type: application/json" -d данные в виде JSON
- Пассажир:

  - Нормальный пассажир:  curl.exe -X POST http://localhost:8080/passengers -H "Content-Type: application/json" -d '{"name":"John","email":"john@example.com","phone":"+7 111 222 33 44"}'
  - Ошибка пассажир: curl.exe -X POST http://localhost:8080/passengers -H "Content-Type: application/json" -d '{"name":"John","email":"@example.com","phone":"123456789"}'

- Водитель:

  - Нормальный водителя:  curl.exe -X POST http://localhost:8080/drivers -H "Content-Type: application/json" -d '{"name":"John","email":"john@example.com","phone":"+7 111 222 33 44"}'
  - Ошибка водителя: curl.exe -X POST http://localhost:8080/drivers -H "Content-Type: application/json" -d '{"name":"John","email":"@example.com","phone":"123456789"}'
  - Обновление статуса водителя: curl.exe -X PATCH "http://localhost:8080/drivers/1/status?status=BUSY"

- Поездки:

  - Добавить поездку: curl.exe -X POST http://localhost:8080/trips -H "Content-Type: application/json" -d '{"id":1,"start":"START","end":"END","length":0.8,"hour":11}'
  - Обновление статуса поездки: curl.exe -X PATCH "http://localhost:8080/trips/1/status?status=FINISHED"
  - Список всех поездок по ID: curl.exe -X GET http://localhost:8080/trips?passenger_id=1

<h1>Spring</h1>

- https://docs.spring.io/spring-boot/index.html
- Пессимистический поиск: 
- 
  - https://habr.com/ru/articles/858714/
  - https://proselyte.net/row-level-blocking/

- <h2>Аннотации</h2>
  - RestController - указывает, что класс контроллер. Обрабатывает запросы и возвращает ответы
  - GetMapping - показывает от какого пути обрабатывается запрос
  - RequestMapping - указывает базовый путь для контроллера
  - PostMapping - запрос вставки (POST)
  - GetMapping - получение (GET)
  - PatchMapping - запрос обновления (PATCH)
  - Entity - указывает, что класс - сущность, которая связана с таблицей.
  - Table - указывает имя таблицы в БД
  - Id - указывает на идентификатор
  - GeneratedValue - автоматическая генерация
  - Column - указывает что колонка и в скобках стратегия (не пустое, макс длина и тд
  - Autowired - помогает автоматический найти нужный бин (класс) и присвоить его
  - RequestBody - параметр передается в запросе как JSON и тд
  - RequestParam - параметр из строки URL
  - PathVariable - получать параметр из пути обращения
  - Transactional - выполняет атомарность и блокирует данные до завершения транзакции (блокирует -  SELECT FOR UPDATE )