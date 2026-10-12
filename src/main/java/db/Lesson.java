package db;

public class Lesson {
    private final long id, tutorId, studentID;
    private final String startAt, status, note, remindAt;
    private final double price;
    private final int duration;

    public Lesson(long id, long tutorId, long studentID, String startAt, String status, String note, String remindAt, double price, int duration) {
        this.id = id;
        this.tutorId = tutorId;
        this.studentID = studentID;
        this.startAt = startAt;
        this.status = status;
        this.note = note;
        this.remindAt = remindAt;
        this.price = price;
        this.duration = duration;
    }

    public long getId() {
        return id;
    }

    public long getTutorId() {
        return tutorId;
    }

    public long getStudentID() {
        return studentID;
    }

    public String getStartAt() {
        return startAt;
    }

    public String getStatus() {
        return status;
    }

    public String getNote() {
        return note;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "{Lesson: " + "id = " + id + ", tutorID = " + tutorId + ", studentID = " + studentID
                + ", price = " + price + ", duration = " + duration + ", status = " + status + ", startsAt = " + startAt + "remindAt = " + remindAt + "}";
    }
}