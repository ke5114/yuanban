import java.util.HashMap;
import java.util.Map;

public class UserSystem {
    private Map<String, User> usersByPhone;  // 使用手机号作为唯一标识

    public UserSystem() {
        usersByPhone = new HashMap<>();
        // 添加一些测试账号
        addTestUsers();
    }

    private void addTestUsers() {
        registerUser("测试患者", "123456", "13800000000", "patient");
        registerUser("测试陪诊师", "123456", "13900000000", "escort");
    }

    public boolean registerUser(String username, String password, String phone, String userType) {
        // 检查手机号是否已被注册
        if (usersByPhone.containsKey(phone)) {
            return false;
        }

        User newUser = new User(username, password, phone, userType);
        usersByPhone.put(phone, newUser);
        return true;
    }

    public User login(String phone, String password) {
        User user = usersByPhone.get(phone);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public boolean isPhoneRegistered(String phone) {
        return usersByPhone.containsKey(phone);
    }

    public void displayAllUsers() {
        if (usersByPhone.isEmpty()) {
            System.out.println("\n当前没有注册用户");
            return;
        }

        System.out.println("\n=== 所有用户列表 ===");
        for (User user : usersByPhone.values()) {
            System.out.println("\n" + user);
        }
    }
}
