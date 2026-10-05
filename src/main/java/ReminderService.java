import db.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
        LocalDate today = LocalDate.now();
        for (User a : UserDao.findAll()){
            List<Lesson> lessonList = (LessonDAO.findByDate(a.getId(), today));
            if (lessonList.isEmpty()) continue;
            StringBuilder sb = new StringBuilder("☀️ Доброе утро! Уроки на сегодня:");
            for (Lesson l : lessonList){
                Student st = StudentDAO.findById(l.getTutorId(), l.getStudentID());
                sb.append(BaseBot.lessInfo(l, a.getId()));
            }
            BaseBot.send(a.getId(), sb.toString());
        }
    }

    public long secondsUntilNextSixAM() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime atSixAM = now.toLocalDate().atTime(6,0);
        if(now.isAfter(atSixAM)){
            atSixAM = atSixAM.plusDays(1);
        }
        return Duration.between(now, atSixAM).getSeconds();
    }
}