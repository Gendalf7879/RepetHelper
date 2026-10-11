package db;

import java.time.LocalTime;

public class RegularLesson {
    private long id, tutorId, studentId;
    private int weekday, durationMin, active;
    private LocalTime time;

    public RegularLesson(long id, long tutorId, long studentId, int weekday, int durationMin, int active, LocalTime time) {
        this.id = id;
        this.tutorId = tutorId;
        this.studentId = studentId;
        this.weekday = weekday;
        this.durationMin = durationMin;
        this.active = active;
        this.time = time;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTutorId() {
        return tutorId;
    }

    public void setTutorId(long tutorId) {
        this.tutorId = tutorId;
    }

    public long getStudentId() {
        return studentId;
    }

    public void setStudentId(long studentId) {
        this.studentId = studentId;
    }

    public int getWeekday() {
        return weekday;
    }

    public void setWeekday(int weekday) {
        this.weekday = weekday;
    }

    public int getDurationMin() {
        return durationMin;
    }

    public void setDurationMin(int durationMin) {
        this.durationMin = durationMin;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }
}
