package org.reina_s;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Чтение пользователей из CSV-файла формата:
 * id,name,email
 * 1,Иван Петров,ivan@mail.ru
 * Строка заголовка необязательна. Пустые строки и строки, начинающиеся с '#', пропускаются.
 * Некорректные строки не прерывают чтение — они попадают в список ошибок с номером строки.
 */
public class UserCsvReader {

    private static final String DELIMITER = ",";
    private static final int EXPECTED_COLUMNS = 3;
    private static final String HEADER = "id,name,email";
    private static final char BOM = '﻿';

    public record ParseResult(List<User> users, List<String> errors) {
    }

    public ParseResult read(Path path) throws IOException {
        List<User> users = new MyLinkedList<>();
        List<String> errors = new MyLinkedList<>();

        // try-with-resources: reader закроется автоматически, даже если возникнет исключение
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            boolean firstDataLine = true;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = removeBom(line).strip();

                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                if (firstDataLine) {
                    firstDataLine = false;
                    if (isHeader(trimmed)) {
                        continue;
                    }
                }

                try {
                    users.add(parseLine(trimmed));
                } catch (ValidationException e) {
                    errors.add("строка " + lineNumber + ": " + e.getMessage());
                }
            }
        }
        return new ParseResult(users, errors);
    }

    private User parseLine(String line) throws ValidationException {
        String[] parts = line.split(DELIMITER, -1);
        if (parts.length != EXPECTED_COLUMNS) {
            throw new ValidationException(String.format(
                    "ожидалось %d поля (id,name,email), найдено %d", EXPECTED_COLUMNS, parts.length));
        }
        int id = UserValidator.validateId(parts[0]);
        String name = UserValidator.validateName(parts[1]);
        String email = UserValidator.validateEmail(parts[2]);
        return new User(id, name, email);
    }

    private boolean isHeader(String line) {
        return line.replace(" ", "").toLowerCase(Locale.ROOT).equals(HEADER);
    }

    /** Для совместимости с ПО от M$ */
    private String removeBom(String line) {
        return !line.isEmpty() && line.charAt(0) == BOM ? line.substring(1) : line;
    }
}
