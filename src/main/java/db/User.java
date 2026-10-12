package db;

import org.jetbrains.annotations.NotNull;

public record User(long id, String username, String timezone) {

    @NotNull
    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', tz='" + timezone + "'}";
    }
}