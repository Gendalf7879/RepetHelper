package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBase {
    private static final String URL = "jdbc:sqlite:bot.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void init() {
        String createUsers = """
                CREATE TABLE IF NOT EXISTS users (
                    id          INTEGER PRIMARY KEY,
                    username    TEXT,
                    timezone    TEXT DEFAULT 'Moscow',
                    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
        String createStudents = """
        CREATE TABLE IF NOT EXISTS students (
            id          INTEGER PRIMARY KEY AUTOINCREMENT,
            tutor_id    INTEGER NOT NULL,
            name        TEXT NOT NULL,
            rate        REAL DEFAULT 0,
            active      INTEGER DEFAULT 1,
            created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (tutor_id) REFERENCES users(id)
        )
        """;
        String createLessons = """
                CREATE TABLE IF NOT EXISTS lessons (
                    id              INTEGER PRIMARY KEY AUTOINCREMENT,
                    tutor_id        INTEGER NOT NULL,
                    student_id      INTEGER NOT NULL,
                    start_at        TEXT NOT NULL,
                    duration_min    INTEGER DEFAULT 60,
                    price           REAL DEFAULT 0,
                    status          TEXT DEFAULT 'PLANNED',
                    note            TEXT,
                    remind_at       TEXT,
                    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (tutor_id) REFERENCES users(id),
                    FOREIGN KEY (student_id) REFERENCES students(id)
                )
                """;
        String createRegularLessons = """
                CREATE TABLE IF NOT EXISTS regular_lessons (
                id            INTEGER PRIMARY KEY AUTOINCREMENT,
                tutor_id      INTEGER NOT NULL,
                student_id    INTEGER NOT NULL,
                weekday       INTEGER NOT NULL,
                time          TEXT NOT NULL,
                duration_min  INTEGER DEFAULT 60,
                active        INTEGER DEFAULT 1,
                created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (tutor_id) REFERENCES users(id),
                FOREIGN KEY (student_id) REFERENCES students(id)
                );""";
        String createUniqueIndexLessons = "CREATE UNIQUE INDEX IF NOT EXISTS idx_lessons_unique " +
                "ON lessons(tutor_id, student_id, start_at)";

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(createUsers);
            statement.execute(createStudents);
            statement.execute(createLessons);
            statement.execute(createRegularLessons);
            statement.execute(createUniqueIndexLessons);
            System.out.println("БД работает");
        } catch (SQLException e) {
            System.out.println("Ошибка при создании БД: " + e.getMessage());
        }
    }
}