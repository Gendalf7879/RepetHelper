package db;

public class Student {
    private long id, tutorId;
    private String name;
    private double rate;
    private boolean active;

    public Student(long id, long tutorId, String name, double rate, boolean active) {
        this.id = id;
        this.tutorId = tutorId;
        this.name = name;
        this.rate = rate;
        this.active = active;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString(){
        return "Student{id=" + id + ", name='" + name + "', rate=" + rate + ", active=" + active + "}";
    }
}
