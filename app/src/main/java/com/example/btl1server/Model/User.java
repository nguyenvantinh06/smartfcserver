package com.example.btl1server.Model;

public class User {
    private String Name;
    private String Password;
    private String Phone;
    private String IsStaff;

    public User() {
    }

    public User(String Name, String Password) {
        this.Name = Name;
        this.Password = Password;
    }

    public String getIsStaff() {
        return IsStaff;
    }

    public void setIsStaff(String isStaff) {
        IsStaff = isStaff;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getName() {
        return this.Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getPassword() {
        return this.Password;
    }

    public void setPassword(String Password) {
        this.Password = Password;
    }
}
