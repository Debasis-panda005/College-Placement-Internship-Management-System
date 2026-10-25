
package com.college.placement.student;

public class Student {
    private int id;
    private String name;
    private String email;
    private String phone;
    private String department;
    private int graduationYear;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) {
        this.department = department;
    }

    public int getGraduationYear() { return graduationYear; }
    public void setGraduationYear(int graduationYear) {
        this.graduationYear = graduationYear;
    }
}
