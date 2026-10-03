package db;

public class User {
    private long id;
    private String username, timezone;

    public User(long id, String username, String timezone) {
        this.id = id;
        this.username = username;
        this.timezone = timezone;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', tz='" + timezone + "'}";
    }
}
