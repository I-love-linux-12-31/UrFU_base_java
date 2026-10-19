package org.reina_s;

import java.util.Objects;


/**
 * Модель предметной области — пользователь.
 * Объект неизменяемый: значения полей задаются один раз в конструкторе.
 * Корректность данных проверяется заранее в {@link UserValidator}.
 */
public class User {

    private final int id;
    private final String name;
    private final String email;

    public User(int id, String name, String email) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "name не может быть null");
        this.email = Objects.requireNonNull(email, "email не может быть null");
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getEmailDomain() {
        return email.substring(email.indexOf('@') + 1);
    }

    /** Пользователи считаются одинаковыми, если совпадает идентификатор. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }
        User other = (User) o;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("ID: %-5d | Имя: %-22s | Email: %s", id, name, email);
    }
}
