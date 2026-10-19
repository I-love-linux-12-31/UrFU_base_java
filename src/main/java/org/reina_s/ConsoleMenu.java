package org.reina_s;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.file.AccessDeniedException;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/** Консольный интерфейс: меню, ввод данных и вывод результатов. */
public class ConsoleMenu {

    private static final String DEFAULT_CSV_FILE = "users.csv";

    /** Парсер строки ввода, который может отклонить значение. */
    @FunctionalInterface
    private interface InputParser<T> {
        T parse(String raw) throws ValidationException;
    }

    private final Scanner scanner;
    private final UserService service;
    private final UserCsvReader csvReader;
    private boolean running = true;

    public ConsoleMenu(Scanner scanner, UserService service, UserCsvReader csvReader) {
        this.scanner = scanner;
        this.service = service;
        this.csvReader = csvReader;
    }

    /** Главный цикл: работает, пока пользователь не выберет «Выход». */
    public void run() {
        System.out.println("=== Управление пользователями ===");
        while (running) {
            printMenu();
            try {
                handleChoice(readLine("Выберите пункт: ").strip());
            } catch (NoSuchElementException e) {
                // поток ввода закрыт (Ctrl+D / Ctrl+Z) — корректно завершаем программу
                System.out.println();
                System.out.println("Ввод завершён.");
                running = false;
            } catch (RuntimeException e) {
                // страховка: непредвиденная ошибка не должна обрывать программу
                System.out.println("Непредвиденная ошибка: " + e.getMessage());
            }
        }
        System.out.println("До свидания!");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("Пользователей в списке: " + service.size());
        System.out.println("1. Добавить пользователя вручную");
        System.out.println("2. Загрузить пользователей из CSV-файла");
        System.out.println("3. Показать всех пользователей");
        System.out.println("4. Фильтр: пользователи с email в заданном домене   (filter)");
        System.out.println("5. Список контактов «Имя <email>»                   (map)");
        System.out.println("6. Сортировка по полю                               (sorted)");
        System.out.println("7. Поиск по имени: фильтр + сортировка + преобразование");
        System.out.println("0. Выход");
    }

    private void handleChoice(String choice) {
        switch (choice) {
            case "1" -> addUserManually();
            case "2" -> loadFromFile();
            case "3" -> showAll();
            case "4" -> filterByDomain();
            case "5" -> showContacts();
            case "6" -> sortUsers();
            case "7" -> searchByName();
            case "0" -> running = false;
            default -> System.out.println("Нет такого пункта: \"" + choice + "\". Введите число от 0 до 7.");
        }
    }

    // ---------- 1. ручной ввод ----------

    private void addUserManually() {
        System.out.println("--- Новый пользователь (пустая строка — отмена) ---");

        Integer id = promptUntilValid("Идентификатор (целое число > 0): ", raw -> {
            int value = UserValidator.validateId(raw);
            if (service.existsId(value)) {
                throw new ValidationException("пользователь с ID " + value + " уже существует");
            }
            return value;
        });
        if (id == null) {
            System.out.println("Добавление отменено.");
            return;
        }

        String name = promptUntilValid("Имя: ", UserValidator::validateName);
        if (name == null) {
            System.out.println("Добавление отменено.");
            return;
        }

        String email = promptUntilValid("Email: ", raw -> {
            String value = UserValidator.validateEmail(raw);
            if (service.existsEmail(value)) {
                throw new ValidationException("email " + value + " уже занят");
            }
            return value;
        });
        if (email == null) {
            System.out.println("Добавление отменено.");
            return;
        }

        try {
            User user = new User(id, name, email);
            service.addUser(user);
            System.out.println("Добавлен: " + user);
        } catch (DuplicateUserException e) {
            System.out.println("Не удалось добавить: " + e.getMessage());
        }
    }

    /**
     * Запрашивает значение, пока оно не пройдёт проверку.
     * Возвращает null, если пользователь ввёл пустую строку (отмена).
     */
    private <T> T promptUntilValid(String prompt, InputParser<T> parser) {
        while (true) {
            String raw = readLine(prompt);
            if (raw.isBlank()) {
                return null;
            }
            try {
                return parser.parse(raw);
            } catch (ValidationException e) {
                System.out.println("  Ошибка: " + e.getMessage() + ". Попробуйте ещё раз.");
            }
        }
    }

    // ---------- 2. загрузка из файла ----------

    private void loadFromFile() {
        String raw = readLine("Путь к CSV-файлу [Enter — " + DEFAULT_CSV_FILE + "]: ").strip();
        String fileName = raw.isEmpty() ? DEFAULT_CSV_FILE : stripQuotes(raw);

        try {
            Path path = Path.of(fileName);
            UserCsvReader.ParseResult parsed = csvReader.read(path);
            UserService.ImportReport report = service.addAll(parsed.users());

            System.out.println("Файл: " + path.toAbsolutePath());
            System.out.println("Добавлено пользователей: " + report.added());
            printProblems("Строки с ошибками (пропущены)", parsed.errors());
            printProblems("Дубликаты (пропущены)", report.skipped());
        } catch (InvalidPathException e) {
            System.out.println("Некорректный путь к файлу: " + e.getMessage());
        } catch (NoSuchFileException e) {
            System.out.println("Файл не найден: " + e.getFile());
        } catch (AccessDeniedException e) {
            System.out.println("Нет доступа к файлу: " + e.getFile());
        } catch (CharacterCodingException e) {
            System.out.println("Файл не в кодировке UTF-8. Пересохраните его в UTF-8 и повторите.");
        } catch (IOException e) {
            System.out.println("Ошибка чтения файла: " + e.getMessage());
        }
    }

    private void printProblems(String title, List<String> problems) {
        if (problems.isEmpty()) {
            return;
        }
        System.out.println(title + ": " + problems.size());
        problems.forEach(p -> System.out.println("  - " + p));
    }

    /** При копировании пути он может вставляться в кавычках. Их можно не убирать. */
    private String stripQuotes(String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    // ---------- 3–7. просмотр и обработка ----------

    private void showAll() {
        if (isEmptyWithMessage()) {
            return;
        }
        printUsers("Все пользователи", service.getAll());
    }

    private void filterByDomain() {
        if (isEmptyWithMessage()) {
            return;
        }
        String domain = promptUntilValid("Домен (например, gmail.com): ", UserValidator::validateDomain);
        if (domain == null) {
            return;
        }
        printUsers("Пользователи с email @" + domain, service.filterByEmailDomain(domain));
    }

    private void showContacts() {
        if (isEmptyWithMessage()) {
            return;
        }
        printLines("Список контактов", service.toContactList());
    }

    private void sortUsers() {
        if (isEmptyWithMessage()) {
            return;
        }
        System.out.println("Сортировать по: 1 — ID, 2 — имени, 3 — email");
        SortField field = switch (readLine("Ваш выбор: ").strip()) {
            case "1" -> SortField.ID;
            case "2" -> SortField.NAME;
            case "3" -> SortField.EMAIL;
            default -> null;
        };
        if (field == null) {
            System.out.println("Нет такого варианта сортировки.");
            return;
        }
        printUsers("Пользователи, отсортированные по " + field.getTitle(), service.sortBy(field));
    }

    private void searchByName() {
        if (isEmptyWithMessage()) {
            return;
        }
        String part = readLine("Часть имени для поиска: ");
        if (part.isBlank()) {
            System.out.println("Строка поиска не может быть пустой.");
            return;
        }
        printLines("Найдено по запросу \"" + part.strip() + "\"", service.searchByName(part));
    }

    // ---------- вспомогательные методы ----------

    private boolean isEmptyWithMessage() {
        if (service.isEmpty()) {
            System.out.println("Список пользователей пуст. Добавьте пользователей (пункт 1 или 2).");
            return true;
        }
        return false;
    }

    private void printUsers(String title, List<User> list) {
        System.out.println("--- " + title + " (" + list.size() + ") ---");
        if (list.isEmpty()) {
            System.out.println("Ничего не найдено.");
        }
        list.forEach(System.out::println);
    }

    private void printLines(String title, List<String> lines) {
        System.out.println("--- " + title + " (" + lines.size() + ") ---");
        if (lines.isEmpty()) {
            System.out.println("Ничего не найдено.");
        }
        lines.forEach(line -> System.out.println("  " + line));
    }

    /** @throws NoSuchElementException если поток ввода закрыт */
    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
}

