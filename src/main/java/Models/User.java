package Models;

import java.time.Instant;

public class User {
    private Long id;
    private String email;
    private String passwordHash;
    private String fullName;
    private String profilePhoto;
    private byte[] fingerprintTemplate;
    private byte[] faceTemplate;
    private Role role;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public User() {
    }

    public User(Long id, String email, String passwordHash, String fullName, Role role, boolean active) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public byte[] getFingerprintTemplate() {
        return fingerprintTemplate;
    }

    public void setFingerprintTemplate(byte[] fingerprintTemplate) {
        this.fingerprintTemplate = fingerprintTemplate;
    }

    public byte[] getFaceTemplate() {
        return faceTemplate;
    }

    public void setFaceTemplate(byte[] faceTemplate) {
        this.faceTemplate = faceTemplate;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
