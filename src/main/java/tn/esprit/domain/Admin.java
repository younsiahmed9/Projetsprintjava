package tn.esprit.domain;

public class Admin {
    private Long userId;
    private String adminCode;

    public Admin() {
    }

    public Admin(Long userId, String adminCode) {
        this.userId = userId;
        this.adminCode = adminCode;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAdminCode() {
        return adminCode;
    }

    public void setAdminCode(String adminCode) {
        this.adminCode = adminCode;
    }
}
