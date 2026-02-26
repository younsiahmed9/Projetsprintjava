package Models;

import java.time.LocalDateTime;

public class ScheduledTransfer {
    private int id;
    private int userId;
    private int fromCardId;
    private int toCardId;
    private double amount;
    private LocalDateTime scheduledDate;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private int attempts;
    private LocalDateTime lastAttempt;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public ScheduledTransfer() {}

    public ScheduledTransfer(int userId, int fromCardId, int toCardId, double amount, LocalDateTime scheduledDate) {
        this.userId = userId;
        this.fromCardId = fromCardId;
        this.toCardId = toCardId;
        this.amount = amount;
        this.scheduledDate = scheduledDate;
        this.status = "PENDING";
        this.attempts = 0;
    }

    // Getters and setters for all fields...
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getFromCardId() { return fromCardId; }
    public void setFromCardId(int fromCardId) { this.fromCardId = fromCardId; }
    public int getToCardId() { return toCardId; }
    public void setToCardId(int toCardId) { this.toCardId = toCardId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public LocalDateTime getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDateTime scheduledDate) { this.scheduledDate = scheduledDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public LocalDateTime getLastAttempt() { return lastAttempt; }
    public void setLastAttempt(LocalDateTime lastAttempt) { this.lastAttempt = lastAttempt; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}