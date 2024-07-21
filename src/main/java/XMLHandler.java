import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс для работы с XML-файлами и базой данных.
 */
public class XMLHandler {
    private DBManager dbManager;

    /**
     * Конструктор для создания экземпляра XMLHandler.
     *
     * @param dbManager Менеджер базы данных.
     */
    public XMLHandler(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Экспортирует данные из базы данных в XML-файл.
     *
     * @param fileName Имя файла, в который будут экспортированы данные.
     * @throws Exception Если произошла ошибка при работе с базой данных или файлом.
     */
    public void exportToXML(String fileName) throws Exception {
        Connection connection = dbManager.getConnection();
        String query = "SELECT DepCode, DepJob, Description FROM departments";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();
        Element rootElement = doc.createElement("Departments");
        doc.appendChild(rootElement);

        while (rs.next()) {
            Element department = doc.createElement("Department");

            Element depCode = doc.createElement("DepCode");
            depCode.appendChild(doc.createTextNode(rs.getString("DepCode")));
            department.appendChild(depCode);

            Element depJob = doc.createElement("DepJob");
            depJob.appendChild(doc.createTextNode(rs.getString("DepJob")));
            department.appendChild(depJob);

            Element description = doc.createElement("Description");
            description.appendChild(doc.createTextNode(rs.getString("Description")));
            department.appendChild(description);

            rootElement.appendChild(department);
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(fileName));
        transformer.transform(source, result);
    }

    /**
     * Синхронизирует данные в базе данных с данными из XML-файла.
     *
     * @param fileName Имя файла, из которого будут синхронизированы данные.
     * @throws Exception Если произошла ошибка при работе с базой данных или файлом.
     */
    public void syncFromXML(String fileName) throws Exception {
        File xmlFile = new File(fileName);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        NodeList nodeList = doc.getElementsByTagName("Department");

        Map<Key, DepRecord> xmlData = new HashMap<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                String depCode = element.getElementsByTagName("DepCode").item(0).getTextContent();
                String depJob = element.getElementsByTagName("DepJob").item(0).getTextContent();
                String description = element.getElementsByTagName("Description").item(0).getTextContent();

                Key key = new Key(depCode, depJob);
                if (xmlData.containsKey(key)) {
                    throw new Exception("Повторяющаяся запись в XML-файле для ключа: " + key);
                }
                xmlData.put(key, new DepRecord(depCode, depJob, description));
            }
        }

        Connection connection = dbManager.getConnection();
        try {
            connection.setAutoCommit(false);

            // Получить текущие данные из БД
            String selectSQL = "SELECT DepCode, DepJob, Description FROM departments";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(selectSQL);

            Map<Key, DepRecord> dbData = new HashMap<>();
            while (rs.next()) {
                String depCode = rs.getString("DepCode");
                String depJob = rs.getString("DepJob");
                String description = rs.getString("Description");
                dbData.put(new Key(depCode, depJob), new DepRecord(depCode, depJob, description));
            }

            // Удаление записи отсутсвуещего в XML
            for (Key key : dbData.keySet()) {
                if (!xmlData.containsKey(key)) {
                    String deleteSQL = "DELETE FROM departments WHERE DepCode = ? AND DepJob = ?";
                    PreparedStatement prStatement = connection.prepareStatement(deleteSQL);
                    prStatement.setString(1, key.getDepCode());
                    prStatement.setString(2, key.getDepJob());
                    prStatement.executeUpdate();
                }
            }

            // Обновление или ввод записи
            for (Map.Entry<Key, DepRecord> entry : xmlData.entrySet()) {
                Key key = entry.getKey();
                DepRecord record = entry.getValue();
                if (dbData.containsKey(key)) {
                    String updateSQL = "UPDATE departments SET Description = ? WHERE DepCode = ? AND DepJob = ?";
                    PreparedStatement prStat = connection.prepareStatement(updateSQL);
                    prStat.setString(1, record.getDescription());
                    prStat.setString(2, key.getDepCode());
                    prStat.setString(3, key.getDepJob());
                    prStat.executeUpdate();
                } else {
                    String insertSQL = "INSERT INTO departments (DepCode, DepJob, Description) VALUES (?, ?, ?)";
                    PreparedStatement prStat = connection.prepareStatement(insertSQL);
                    prStat.setString(1, record.getDepCode());
                    prStat.setString(2, record.getDepJob());
                    prStat.setString(3, record.getDescription());
                    prStat.executeUpdate();
                }
            }

            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}