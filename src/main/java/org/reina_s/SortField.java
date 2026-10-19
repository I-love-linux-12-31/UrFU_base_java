package org.reina_s;

import java.util.Comparator;

/** Поле, по которому можно отсортировать пользователей. */
public enum SortField {

    ID("идентификатору", Comparator.comparingInt(User::getId)),
    NAME("имени", Comparator.comparing(User::getName, String.CASE_INSENSITIVE_ORDER)
            .thenComparingInt(User::getId)),
    EMAIL("email", Comparator.comparing(User::getEmail));

    private final String title;
    private final Comparator<User> comparator;

    SortField(String title, Comparator<User> comparator) {
        this.title = title;
        this.comparator = comparator;
    }

    public String getTitle() {
        return title;
    }

    public Comparator<User> getComparator() {
        return comparator;
    }
}

