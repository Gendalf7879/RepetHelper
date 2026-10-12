package db;

public class Student {
    private final long id;
    private final long tutorId;
    private final String name;
    private final double rate;
    private final boolean active;

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

    public String getName() {
        return name;
    }

    public double getRate() {
        return rate;
    }

    @Override
    public String toString() {
        return "Student{id=" + id + ", name='" + name + "', rate=" + rate + ", active=" + active + "tutorId = " + tutorId + "}";
    }
}
