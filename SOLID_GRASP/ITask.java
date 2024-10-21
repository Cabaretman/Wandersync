public interface ITask {
    String getTitle();
    void setTitle(String title);

    String getDescription();
    void setDescription(String description);

    int getDueDate();
    void setDueDate(int dueDate);

    TaskStatus getStatus();
    void setStatus(TaskStatus status);

    int getPriority();
    void setPriority(int priority);

    boolean execute();
}
