package org.reina_s;

import java.util.Locale;
import java.util.regex.Pattern;


/**
 * Валидация полей пользователя.
 * Каждый метод принимает «сырую» строку (из консоли или CSV),
 * возвращает нормализованное значение или бросает {@link ValidationException}.
 */
public final class UserValidator {

    private static final int NAME_MIN_LENGTH = 2;
    private static final int NAME_MAX_LENGTH = 50;
    private static final int EMAIL_MAX_LENGTH = 254;

    /** Буквы (любой алфавит), слова разделяются пробелом, дефисом или апострофом. */
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^\\p{L}+([ '-]\\p{L}+)*$");

    /** Упрощённая проверка email: local@domain.zone */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

    /** Домен почты для фильтрации: example.com, mail.ru */
    private static final Pattern DOMAIN_PATTERN =
            Pattern.compile("^[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

    private UserValidator() {
        // утилитный класс — экземпляры не нужны
    }

    public static int validateId(String raw) throws ValidationException {
        String value = requireNotBlank(raw, "Идентификатор");
        int id;
        try {
            id = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ValidationException(
                    "Идентификатор должен быть целым числом, получено: \"" + value + "\"", e);
        }
        if (id <= 0) {
            throw new ValidationException("Идентификатор должен быть больше нуля, получено: " + id);
        }
        return id;
    }

    public static String validateName(String raw) throws ValidationException {
        String value = requireNotBlank(raw, "Имя").replaceAll("\\s+", " ");
        if (value.length() < NAME_MIN_LENGTH || value.length() > NAME_MAX_LENGTH) {
            throw new ValidationException(String.format(
                    "Имя должно содержать от %d до %d символов, получено: %d",
                    NAME_MIN_LENGTH, NAME_MAX_LENGTH, value.length()));
        }
        if (!NAME_PATTERN.matcher(value).matches()) {
            throw new ValidationException(
                    "Имя может содержать только буквы, пробел, дефис и апостроф: \"" + value + "\"");
        }
        return value;
    }

    public static String validateEmail(String raw) throws ValidationException {
        String value = requireNotBlank(raw, "Email").toLowerCase(Locale.ROOT);
        if (value.length() > EMAIL_MAX_LENGTH) {
            throw new ValidationException("Email слишком длинный (максимум " + EMAIL_MAX_LENGTH + " символа)");
        }
        String localPart = value.contains("@") ? value.substring(0, value.indexOf('@')) : value;
        boolean badDots = localPart.startsWith(".") || localPart.endsWith(".") || value.contains("..");
        if (!EMAIL_PATTERN.matcher(value).matches() || badDots) {
            throw new ValidationException(
                    "Некорректный email: \"" + value + "\". Ожидается формат name@domain.com");
        }
        return value;
    }

    /** Домен для фильтрации; допускается ввод с ведущим "@" (например, "@gmail.com"). */
    public static String validateDomain(String raw) throws ValidationException {
        String value = requireNotBlank(raw, "Домен").toLowerCase(Locale.ROOT);
        if (value.startsWith("@")) {
            value = value.substring(1);
        }
        if (!DOMAIN_PATTERN.matcher(value).matches()) {
            throw new ValidationException("Некорректный домен: \"" + value + "\". Пример: gmail.com");
        }
        return value;
    }

    private static String requireNotBlank(String raw, String fieldName) throws ValidationException {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException(fieldName + " не может быть пустым");
        }
        return raw.strip();
    }
}

