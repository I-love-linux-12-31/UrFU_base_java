package org.reina_s;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Бизнес-логика: хранение пользователей в коллекции {@link List}
 * и их обработка через Stream API.
 */
public class UserService {

    private final List<User> users;

    /** Отчёт об импорте: сколько добавлено и какие записи пропущены. */
    public record ImportReport(int added, List<String> skipped) {
    }

    /**
     * @param storage коллекция для хранения (например, {@code new MyLinkedList<>()}).
     *                Сервис работает через интерфейс List и не зависит от реализации.
     */
    public UserService(List<User> storage) {
        this.users = Objects.requireNonNull(storage, "storage не может быть null");
    }

    // ---------- добавление ----------

    public void addUser(User user) throws DuplicateUserException {
        if (existsId(user.getId())) {
            throw new DuplicateUserException("пользователь с ID " + user.getId() + " уже существует");
        }
        if (existsEmail(user.getEmail())) {
            throw new DuplicateUserException("email " + user.getEmail() + " уже занят");
        }
        users.add(user);
    }


    public ImportReport addAll(List<User> candidates) {
        List<String> skipped = new MyLinkedList<>();
        int added = 0;
        for (User user : candidates) {
            try {
                addUser(user);
                added++;
            } catch (DuplicateUserException e) {
                skipped.add("ID " + user.getId() + ": " + e.getMessage());
            }
        }
        return new ImportReport(added, skipped);
    }

    public boolean existsId(int id) {
        return users.stream().anyMatch(u -> u.getId() == id);
    }

    public boolean existsEmail(String email) {
        return users.stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
    }

    // ---------- чтение ----------


    public List<User> getAll() {
        return users.stream().toList();
    }

    public int size() {
        return users.size();
    }

    public boolean isEmpty() {
        return users.isEmpty();
    }

    // ---------- обработка через Stream API ----------

    /** filter(): пользователи, у которых email в указанном искомом домене. */
    public List<User> filterByEmailDomain(String domain) {
        return users.stream()
                .filter(u -> u.getEmailDomain().equalsIgnoreCase(domain))
                .toList();
    }

    /** map(): преобразование пользователей в строки контактов «Имя <email>». */
    public List<String> toContactList() {
        return users.stream()
                .map(u -> u.getName() + " <" + u.getEmail() + ">")
                .toList();
    }

    /** sorted(): сортировка по выбранному полю. */
    public List<User> sortBy(SortField field) {
        return users.stream()
                .sorted(field.getComparator())
                .toList();
    }

    public List<String> searchByName(String part) {
        String needle = part.strip().toLowerCase(Locale.ROOT);
        return users.stream()
                .filter(u -> u.getName().toLowerCase(Locale.ROOT).contains(needle))
                .sorted(SortField.NAME.getComparator())
                .map(u -> u.getName().toUpperCase(Locale.ROOT) + " — " + u.getEmail())
                .toList();
    }
}

