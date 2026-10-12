package db;

import java.time.LocalTime;

public record RegularLesson(long id, long tutorId, long studentId, int weekday, int durationMin, int active,
                            LocalTime time) {
}
