package org.reina_s;

import java.util.Scanner;

/** Точка входа: собирает объекты приложения и запускает меню. */
public class Main {

    public static void main(String[] args) {
        // хранилище — собственная реализация List
        UserService service = new UserService(new MyLinkedList<>());
        UserCsvReader csvReader = new UserCsvReader();

        // try-with-resources: Scanner закроется при выходе из программы
        try (Scanner scanner = new Scanner(System.in)) {
            new ConsoleMenu(scanner, service, csvReader).run();
        }
    }
}

