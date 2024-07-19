import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Основной класс приложения для экспорта и синхронизации данных с базой данных.
 */
public class App {

    private static final Logger logger = LogManager.getLogger(App.class);

    /**
     * Точка входа в приложение.
     *
     * @param args Аргументы командной строки.
     *             <ul>
     *                 <li>args[0] - команда (export, sync, help)</li>
     *                 <li>args[1] - имя файла (опционально для команд export и sync)</li>
     *             </ul>
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            printHelp();
            System.exit(1);
        }

        String command = args[0];
        String fileName = args.length > 1 ? args[1] : null;

        Properties properties = loadProperties("application.properties");
        if (properties == null) {
            System.exit(1);
        }

        String logFilename = properties.getProperty("log4j.appender.file.File", "logs/app.log");
        System.setProperty("logFilename", logFilename);
        Configurator.initialize(null, "log4j2.xml");

        String dbUrl = properties.getProperty("db.url");
        String dbUsername = properties.getProperty("db.user");
        String dbPassword = properties.getProperty("db.password");

        try (DBManager dbManager = new DBManager(dbUrl, dbUsername, dbPassword)) {
            XMLHandler xmlHandler = new XMLHandler(dbManager);

            switch (command.toLowerCase()) {
                case "export":
                    if (fileName != null) {
                        xmlHandler.exportToXML(fileName);
                        System.out.println("Данные успешно экспортированы в файл " + fileName);
                        logger.info("Данные успешно экспортированы в файл " + fileName);
                    } else {
                        System.err.println("Комманада 'export' должна иметь второй аргумент <file>");
                        printHelp();
                    }
                    break;

                case "sync":
                    if (fileName != null) {
                        xmlHandler.syncFromXML(fileName);
                        System.out.println("Данные успешно синхроинизированы из файла " + fileName);
                        logger.info("Данные успешно синхроинизированы из файла " + fileName);
                    } else {
                        System.err.println("Комманада 'sync' должна иметь второй аргумент <file>");
                        printHelp();
                    }
                    break;

                case "help":
                    printHelp();
                    break;

                default:
                    System.out.println("Неизвестная команда: " + command);
                    printHelp();
                    break;
            }
        } catch (SQLException e) {
            logger.error("Ошибка подключения к базе данных: URL={}, Пользователь={}", dbUrl, dbUsername, e);
            System.err.println("Ошибка подключения к базе данных. Просьба проверить параметры подключения " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            logger.error("Произошла ошибка: ", e);
            System.err.println("Произошла ошибка: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Загрузка свойств из файла.
     *
     * @param file Путь к файлу свойств.
     * @return Объект Properties с загруженными свойствами.
     */
    private static Properties loadProperties(String file) {
        Properties properties = new Properties();
        try (InputStream inputStream = App.class.getClassLoader().getResourceAsStream(file)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Файл конфигурации не найден: " + file);
            }
            properties.load(inputStream);
        } catch (Exception e) {
            logger.error("Ошибка при загрузке файла конфигурации:", e);
            System.err.println("Ошибка при загрузке файла конфигурации: " + e.getMessage());
            return null;
        }
        return properties;
    }



    /**
     * Вывод справочного сообщения с использованием примеров команд.
     */
    private static void printHelp() {
        String helpMessage = "Формат ввода: java -jar jarName.jar <command> <file>\n" +
                "Команды:\n" +
                "  export <file>   : Экспорт данных из базы данных в указанный XML файл.\n" +
                "  sync <file>     : Синхронизация данных в базе данных из указанного XML файла.\n" +
                "  help            : Показать справочное сообщение.\n" +
                "Примеры:\n" +
                "  java -jar jarName.jar export file.xml\n" +
                "  java -jar jarName.jar sync file.xml\n" +
                "  java -jar jarName.jar sync C:/Users/user/Desktop/file.xml\n";

        System.out.println(helpMessage);
    }
}