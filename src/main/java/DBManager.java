import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Класс для управления подключением к базе данных.
 * Реализует интерфейс AutoCloseable для автоматического закрытия подключения.
 */
public class DBManager implements  AutoCloseable{
    private Connection connection;

    /**
     * Конструктор для создания экземпляра DBManager и установки подключения к базе данных.
     *
     * @param url        URL базы данных.
     * @param dbUsername Имя пользователя базы данных.
     * @param dbPassword Пароль пользователя базы данных.
     * @throws SQLException Если возникает ошибка при подключении к базе данных.
     */
    public DBManager(String url, String dbUsername, String dbPassword) throws SQLException {
        this.connection = DriverManager.getConnection(url, dbUsername, dbPassword);
    }

    /**
     * Возвращает текущее подключение к базе данных.
     *
     * @return Объект Connection, представляющий текущее подключение к базе данных.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Закрывает подключение к базе данных, если оно открыто.
     *
     * @throws Exception Если возникает ошибка при закрытии подключения.
     */
    @Override
    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}