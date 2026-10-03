package db;

public class Lesson {
    private long id, tutorId, studentID;
    private String startAt, status, note, remindAt;
    private double price;
    private int duration;

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

    public void setId(long id) {
        this.id = id;
    }

    public long getTutorId() {
        return tutorId;
    }

    public void setTutorId(long tutorId) {
        this.tutorId = tutorId;
    }

    public long getStudentID() {
        return studentID;
    }

    public void setStudentID(long studentID) {
        this.studentID = studentID;
    }

    public String getStartAt() {
        return startAt;
    }

    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getRemindAt() {
        return remindAt;
    }

    public void setRemindAt(String remindAt) {
        this.remindAt = remindAt;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "{Lesson: " + "id = " + id + ", tutorID = " + tutorId + ", studentID = " + studentID
                + ", price = " + price + ", duration = " + duration + ", status = " + status + ", startsAt = " + startAt + "}";
    }
}