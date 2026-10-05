import db.DataBase;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import java.io.InputStream;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws Exception{
        DataBase.init();
        Properties props = new Properties();
        try (InputStream is = Main.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            props.load(is);
        }

        String token = props.getProperty("bot.token");

        TelegramBotsLongPollingApplication app = new TelegramBotsLongPollingApplication();
        app.registerBot(token, new BaseBot(token));

        ReminderService rm = new ReminderService();
        rm.start();

        System.out.println("Бот запущен");

        Thread.currentThread().join();

        Runtime.getRuntime().addShutdownHook(new Thread(rm::stop));

        Thread.currentThread().join();
    }
}
