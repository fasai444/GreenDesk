package org.example.dto.auth;

public class ResetPasswordRequest {
    private String newPassword;

    public ResetPasswordRequest() {}

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
