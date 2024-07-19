
/**
 * Класс, представляющий запись о департаменте.
 */
public class DepRecord {
    private String depCode;
    private String depJob;
    private String description;

    /**
     * Конструктор для создания экземпляра DepRecord.
     *
     * @param depCode     Код департамента.
     * @param depJob      Название должности в департаменте.
     * @param description Комментарий.
     */
    public DepRecord(String depCode, String depJob, String description) {
        this.depCode = depCode;
        this.depJob = depJob;
        this.description = description;
    }

    /**
     * Возвращает код департамента.
     *
     * @return Код департамента.
     */
    public String getDepCode() {
        return depCode;
    }

    /**
     * Возвращает название должности в департаменте.
     *
     * @return Название должности.
     */
    public String getDepJob() {
        return depJob;
    }

    /**
     * Возвращает комментарий.
     *
     * @return Комментарий.
     */
    public String getDescription() {
        return description;
    }
}