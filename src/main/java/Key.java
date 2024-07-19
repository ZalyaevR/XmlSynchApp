
/**
 * Класс, представляющий ключ для записи о департаменте.
 */
public class Key {
    private String depCode;
    private String depJob;

    /**
     * Конструктор для создания экземпляра Key.
     *
     * @param depCode Код департамента.
     * @param depJob  Название должности в департаменте.
     */
    public Key(String depCode, String depJob) {
        this.depCode = depCode;
        this.depJob = depJob;
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
     * Проверяет равенство текущего объекта с другим объектом.
     *
     * @param o Объект для сравнения.
     * @return true, если объекты равны; false в противном случае.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Key key = (Key) o;

        if (!depCode.equals(key.depCode)) return false;
        return depJob.equals(key.depJob);
    }

    /**
     * Возвращает хэш-код для текущего объекта.
     *
     * @return Хэш-код.
     */
    @Override
    public int hashCode() {
        int result = depCode.hashCode();
        result = 31 * result + depJob.hashCode();
        return result;
    }
}