package db;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReminderService {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter FMT_TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public void start() {
        scheduler.scheduleAtFixedRate(this::checkHourlyReminders, 0, 1, TimeUnit.MINUTES);

        long initialDelay = secondsUntilNextSixAM();
        scheduler.scheduleAtFixedRate(this::dailyMorningReport, initialDelay, 24*60*60, TimeUnit.SECONDS);

        System.out.println("Сервис напоминаний запущен");
    }

    public void stop() {
        scheduler.shutdown();
    }

    private void checkHourlyReminders(){

    }

    private void dailyMorningReport() {

    }

    public long secondsUntilNextSixAM(){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime atSixAM = now.toLocalDate().atTime(6,0);
        if(now.isAfter(atSixAM)){
            atSixAM = atSixAM.plusDays(1);
        }
        return Duration.between(now, atSixAM).getSeconds();
    }
}