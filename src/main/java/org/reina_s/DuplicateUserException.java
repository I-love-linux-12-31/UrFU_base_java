package org.reina_s;

/** Проверяемое исключение: пользователь с таким ID или email уже есть в коллекции. */
public class DuplicateUserException extends Exception {

    private static final long serialVersionUID = 1L;

    public DuplicateUserException(String message) {
        super(message);
    }
}

