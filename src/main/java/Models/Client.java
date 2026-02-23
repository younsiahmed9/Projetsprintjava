package Models;

public class Client {
    private Long userId;
    private String cin;
    private String phone;

    public Client() {
    }

    public Client(Long userId, String cin, String phone) {
        this.userId = userId;
        this.cin = cin;
        this.phone = phone;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
