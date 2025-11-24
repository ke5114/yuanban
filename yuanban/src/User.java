public class User {
    private String username;
    private String password;
    private String phone;
    private String userType;  // "patient" 或 "escort"

    public User(String username, String password, String phone, String userType) {
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.userType = userType;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getPhone() { return phone; }
    public String getUserType() { return userType; }

    @Override
    public String toString() {
        return "用户信息：\n" +
                "用户名: " + username + "\n" +
                "手机号: " + phone + "\n" +
                "用户类型: " + (userType.equals("patient") ? "患者" : "陪诊师");
    }
}