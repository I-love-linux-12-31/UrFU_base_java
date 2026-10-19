package org.reina_s;

/** Проверяемое исключение: введённые или прочитанные из файла данные некорректны. */
public class ValidationException extends Exception {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

