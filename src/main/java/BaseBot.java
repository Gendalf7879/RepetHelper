import db.*;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

public class BaseBot implements LongPollingUpdateConsumer {
    private static TelegramClient client;
    private static final DateTimeFormatter DATE_FMT = new DateTimeFormatterBuilder()
            .appendPattern("dd.MM")
            .optionalStart()
            .appendPattern(".yyyy")
            .optionalEnd()
            .parseDefaulting(ChronoField.YEAR, LocalDate.now().getYear())
            .toFormatter();
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public BaseBot(String botToken) {
        client = new OkHttpTelegramClient(botToken);
    }

    public static void send(long chatId, String text) {
        try{
            client.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .build());
        } catch (TelegramApiException e){
            System.err.println("Не удалось отправить сообщение: " + e.getMessage());
        }
    }

    @Override
    public void consume(List<Update> updates) {
        for (Update update : updates) {
            if (!update.hasMessage() || !update.getMessage().hasText()) return;
            try {
                long chatId = update.getMessage().getChatId();
                String text = update.getMessage().getText();
                var from = update.getMessage().getFrom();
                long userId = from.getId();
                String username = from.getUserName() != null ? from.getUserName() : "unknown";

                String answer;
                if (text.equals("/start")) {
                    User user = UserDao.findById(userId);
                    if (user == null) {
                        UserDao.saveOrUpdate(new User(userId, username, "Moscow"));
                        answer = "Здравствуйте, я добавил вас в свою базу данных, теперь я умею присылать напоминания! Напишите /help чтобы увидеть все доступные команды.";
                    } else
                        answer = "C возвращением, " + username + "! Теперь я умею присылать напоминания! Напишите /help чтобы увидеть все доступные команды.";
                } else if (text.equals("/help")) answer = AllComm.allComm;
                else if (text.startsWith("/addStudent")) answer = handleAddStudent(userId, text);
                else if (text.startsWith("/delStudent")) answer = handleDelStud(userId, text);
                else if (text.equals("/students")) answer = handleListStudent(userId);
                else if (text.startsWith("/addLesson")) answer = handleAddLesson(userId, text);
                else if (text.startsWith("/regular")) answer = addRegularLesson(userId, text);
                else if (text.startsWith("/replan")) answer = rePlanLesson(userId, text);
                else if (text.equals("/today")) answer = todayLessons(userId);
                else if (text.equals("/week")) answer = weekLessons(userId);
                else if (text.equals("/lessons")) answer = showLessons(userId);
                else if (text.startsWith("/plan")) answer = plannedLesson(userId, text);
                else if (text.startsWith("/done")) answer = doneLesson(userId, text);
                else if (text.startsWith("/cancel")) answer = cancelLesson(userId, text);
                else if (text.startsWith("/lesson")) answer = showLessonInfo(userId, text);
                else if (text.startsWith("/earned")) answer = moneyEarned(userId, text);
                else
                    answer = "Вы написали что то не то, или то, что я пока не умею, напишите /help чтобы увидеть все доступные команды";

                SendMessage message = SendMessage.builder()
                        .chatId(chatId)
                        .text(answer)
                        .build();
                client.execute(message);

            } catch (TelegramApiException e) {
                System.err.println("Ошибочка в командах: " + e.getMessage());
            }
        }
    }

    //
    //
    //

    public String handleAddStudent(long tutorId, String text){
        String[] strings = text.split("\\s+");
        if (strings.length < 3) return "Формат: /addStudent Фамилия Имя ставка";
        String sRate = strings[strings.length - 1];
        double rate;
        try {
            rate = Double.parseDouble(sRate);
        } catch (NumberFormatException e) {
            return "ставка должна быть числом, например 2000";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < strings.length - 1; i++ ){
            if(i > 1) sb.append(" ");
            sb.append(strings[i]);
        }
        if (sb.isEmpty()) return "Укажите имя ученика";
        long id = StudentDAO.addStud(tutorId, sb.toString(), rate);
        return "Студент " + sb + ", cо ставкой " + rate + "₽ был добавлен в ваш список" + ". Его id - " + id;
    }

    public String handleDelStud(long tutorId, String text) {
        String[] parts = text.split("\\s+");
        if (parts.length != 2) return "Формат: /delStudent <id>";
        long studId;
        try {
            studId = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return "id должен быть числом";
        }
        boolean ok = StudentDAO.lightDeleteStud(tutorId, studId);
        return ok ? "Ученик удалён" : "Ученик не найден";
    }

    public String handleListStudent(long tutorId){
        ArrayList<Student> students = StudentDAO.findByTutor(tutorId);
        if (students.isEmpty()) return "У вас пока что нет учеников, /addStudent Фамилия Имя ставка";

        StringBuilder sb = new StringBuilder("Ваши ученики:\n");
        for (Student s : students){
            sb.append(s.getId()).append(". ")
                    .append(s.getName())
                    .append(", ставка: ").append(s.getRate()).append(" ₽\n");
        } return sb.toString().trim();
    }

    public String handleAddLesson(long tutorId, String text) {
        String[] parts = text.split("\\s+");
        if (parts.length < 4 || parts.length > 5) return "Формат: /addLesson <studentId> <дата> <время> [минуты]";

        long studId;
        try {
            studId = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return "studentId должен быть числом";
        }

        Student student = StudentDAO.findById(tutorId, studId);
        if (student == null) return "Ученик с id=" + studId + " не найден";

        int durationMin = 60;
        if (parts.length == 5) {
            try {
                durationMin = Integer.parseInt(parts[4]);
            } catch (NumberFormatException e) {
                return "Длительность должна быть числом";
            }
        }

        LocalDate date;
        String dateToken = parts[2].toLowerCase();
        switch (dateToken) {
            case "сегодня" -> date = LocalDate.now();
            case "завтра" -> date = LocalDate.now().plusDays(1);
            default -> {
                try {
                    date = LocalDate.parse(parts[2], DATE_FMT);

                } catch (DateTimeParseException e) {
                    return "Дата в формате дд.ММ.гггг, 'завтра' или 'сегодня', время в формате ЧЧ:ММ";
                }
            }
        }

        LocalTime time;
        try {
            time = LocalTime.parse(parts[3], TIME_FMT);
        } catch (DateTimeParseException e){
            return "Время должно быть в формате ЧЧ:ММ, например 18:30";
        }

        LocalDateTime startAt = LocalDateTime.of(date, time);
        double price = student.getRate() * durationMin / 60.0;
        LocalDateTime remindAt = startAt.minusHours(1);

        long id = LessonDAO.addLesson(
                tutorId, studId, startAt, durationMin, price,
                LessonsStatus.PLANNED.name(), null, remindAt);

        return "Урок для " + student.getName() + " на " + startAt +
                " запланирован. id = " + id;
    }

    public String todayLessons(long tutorId){
        LocalDate date = LocalDate.now();
        List<Lesson> lessonList = LessonDAO.findByDate(tutorId, date);
        if (lessonList.isEmpty()) return "Уроков сегодня нет";
        StringBuilder result = new StringBuilder();
        for(Lesson a : lessonList){
            result.append(lessInfo(a, tutorId));
        }
        return "Уроки сегодня:" + result;
    }

    public String weekLessons(long tutorId) {
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = from.plusDays(7);
        List<Lesson> lessonList = LessonDAO.findByRange(tutorId, from, to);
        if (lessonList.isEmpty()) return "Уроков на неделе нет";
        StringBuilder result = new StringBuilder();
        for(Lesson a : lessonList){
            result.append(lessInfo(a, tutorId));
        }
        return "Уроки на неделе:" + result;
    }

    public String plannedLesson(long tutorId, String text) {
        String[] parts = text.split("\\s+");
        if (parts.length != 2) return "Формат: /plan <id урока>";
        long lessId;
        try{
            lessId = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return "id должно быть числом";
        }
        boolean ok = LessonDAO.updateStatus(lessId, tutorId, LessonsStatus.PLANNED);
        if (ok) return "Урок с id " + lessId + " был помечен как запланированный";
        else return "Урок не найден";
    }

    public String doneLesson(long tutorId, String text) {
        String[] parts = text.split("\\s+");
        if (parts.length != 2) return "Формат: /done <id урока>";
        long lessId;
        try{
            lessId = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return "id должно быть числом";
        }
        boolean ok = LessonDAO.updateStatus(lessId, tutorId, LessonsStatus.DONE);
        if (ok) return "Урок с id " + lessId + " был помечен как завершённый";
        else return "Урок не найден";
    }

    public String cancelLesson(long tutorId, String text) {
        String[] parts = text.split("\\s+");
        if (parts.length != 2) return "Формат: /cancel <id урока>";
        long lessId;
        try {
            lessId = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return "id должно быть числом";
        }
        boolean ok = LessonDAO.updateStatus(lessId, tutorId, LessonsStatus.CANCELLED);
        if (ok) return "Урок с id " + lessId + " был помечен как отменённый";
        else return "Урок не найден";
    }

    public String showLessonInfo(long tutorId, String text) {
        String[] parts = text.split("\\s+");
        if (parts.length != 2) return "Формат: /lesson <id урока>";
        long lessId;
        try {
            lessId = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return "id должно быть числом";
        }
        Lesson l1 = LessonDAO.findById(lessId, tutorId);
        if (l1 == null) return "Урок не найден";
        Student st = StudentDAO.findById(tutorId, l1.getStudentID());
        String studentName = st != null ? st.getName() : "Удалён";
        String note = l1.getNote() == null || l1.getNote().isBlank() ? "(нет)" : l1.getNote();
        return "Урок " + l1.getStartAt() + ": " + studentName
                + " | " + l1.getPrice() + " ₽ | " + l1.getStatus() + "\n заметка: " + note;
    }

    public String showLessons(long tutorId) {
        List<Lesson> l1 = LessonDAO.showLessons(tutorId);
        if (l1.isEmpty()) return "У вас пока нет уроков, добавьте /addLesson";
        StringBuilder result = new StringBuilder();
        for(Lesson a : l1){
            result.append(lessInfo(a, tutorId));
        }
        return "Все ваши уроки:" + result;
    }

    public String moneyEarned(long tutorId, String text) {
        LocalDate now = LocalDate.now();
        LocalDate monthStart;
        if (text.equals("/earned")){
            monthStart = now.withDayOfMonth(1);
        } else {
            String[] parts = text.split("\\s+");
            if(parts.length != 2) return "Формат /earned или /earned ММ.гггг";
            try{
                monthStart = LocalDate.parse("01." + parts[1], DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            } catch (DateTimeParseException e){
                return "Месяц в формате ММ.гггг, например 09.2026";
            }
        }
        LocalDateTime from = monthStart.atStartOfDay();
        LocalDateTime to = monthStart.plusMonths(1).atStartOfDay();
        double sum = LessonDAO.moneyEarned(tutorId, from, to);
        return "Заработано за " + monthStart.getMonth() + "." + monthStart.getYear()
                + ": " + sum + " ₽";
    }

    public static String rePlanLesson(long tutorId, String text) {
        String[] parts = text.split("\\s+");
        if (parts.length < 4 || parts.length > 5) return "Формат: /addLesson <id урока> <дата> <время> [минуты]";

        long lessId;
        try {
            lessId = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return "id урока должно быть числом";
        }

        int durationMin = 60;
        if (parts.length == 5) {
            try {
                durationMin = Integer.parseInt(parts[4]);
            } catch (NumberFormatException e) {
                return "Длительность должна быть числом";
            }
        }

        LocalDate date;
        String dateToken = parts[2].toLowerCase();
        switch (dateToken) {
            case "сегодня" -> date = LocalDate.now();
            case "завтра" -> date = LocalDate.now().plusDays(1);
            default -> {
                try {
                    date = LocalDate.parse(parts[2], DATE_FMT);

                } catch (DateTimeParseException e) {
                    return "Дата в формате дд.ММ.гггг, 'завтра' или 'сегодня', время в формате ЧЧ:ММ";
                }
            }
        }
        LocalTime time;
        try {
            time = LocalTime.parse(parts[3], TIME_FMT);
        } catch (DateTimeParseException e) {
            return "Время должно быть в формате ЧЧ:ММ, например 18:30";
        }

        LocalDateTime startAt = LocalDateTime.of(date, time);
        if (startAt.isBefore(LocalDateTime.now())) {
            return "Нельзя перенести урок в прошлое";
        }
        boolean mark = LessonDAO.replanLesson(lessId, tutorId, startAt, durationMin);
        if (!mark) return "Урок не найден";
        else return "Урок с id " + lessId + " перенесён на " + startAt.format(
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    public static String addRegularLesson(long tutorId, String text){
        String[] parts = text.split("\\s+");
        if (parts.length < 4 || parts.length > 5) return "Формат /regular <id ученика> <номер дня недели> <время> [длительность (минуты)]";
        long studentId;
        try{
            studentId = Long.parseLong(parts[1]);
        } catch (NumberFormatException e){
            return "Id должно быть числом";
        }
        Student st = StudentDAO.findById(tutorId, studentId);
        if (st == null) return "Ученик с id=" + studentId + " не найден";

        int duration_min = 60;
        if (parts.length == 5){
            try{
                duration_min = Integer.parseInt(parts[4]);
            } catch (NumberFormatException e) {
                return "Длительность(минуты) должна быть числом";
            }
        }
        int weekday;
        try{
            weekday = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            return "День недели должен быть числом 1–7 (1 = Пн)";
        }
        if (weekday < 1 || weekday > 7) {
            return "День недели должен быть от 1 до 7";
        }
        LocalTime time;
        try{
        time = LocalTime.parse(parts[3], TIME_FMT);
        } catch (DateTimeParseException e) {
            return "Формат времени ЧЧ:ММ, например 18:30";
        }
        boolean mark = LessonDAO.addRegularLesson(tutorId, studentId, weekday, time, duration_min);
        String answer ="Регулярный урок добавлен";
        return mark ? answer : "Ученик не найден";
    }

    public static String lessInfo (Lesson a, long tutorId) {
        Student st = StudentDAO.findById(tutorId, a.getStudentID());
        String studentName = st != null ? st.getName() : "(удалён)";
        String studentRate = st != null ? a.getPrice() + " ₽ " : "—";
        return "\n • " + a.getStartAt() +
                " id = " + a.getId() +
                " | " + studentName +
                " | " + studentRate +
                " | " + a.getStatus();
    }
}