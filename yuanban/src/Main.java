import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserSystem userSystem = new UserSystem();

        while (true) {
            System.out.println("\n=== 医疗陪诊系统 ===");
            System.out.println("1. 用户注册");
            System.out.println("2. 用户登录");
            System.out.println("3. 查看所有用户");
            System.out.println("4. 退出系统");
            System.out.print("请选择操作: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // 消费换行符

            if (choice == 1) {
                System.out.println("\n=== 用户注册 ===");
                System.out.print("请输入用户名: ");
                String username = scanner.nextLine();

                System.out.print("请输入手机号: ");
                String phone = scanner.nextLine();

                if (userSystem.isPhoneRegistered(phone)) {
                    System.out.println("该手机号已被注册！");
                    continue;
                }

                System.out.print("请输入密码: ");
                String password = scanner.nextLine();

                System.out.println("请选择用户类型：");
                System.out.println("1. 患者");
                System.out.println("2. 陪诊师");
                System.out.print("请选择(1/2): ");
                int typeChoice = scanner.nextInt();
                scanner.nextLine();

                String userType = typeChoice == 1 ? "patient" : "escort";

                if (userSystem.registerUser(username, password, phone, userType)) {
                    System.out.println("注册成功！");
                } else {
                    System.out.println("注册失败，请稍后重试。");
                }

            } else if (choice == 2) {
                System.out.println("\n=== 用户登录 ===");
                System.out.print("请输入手机号: ");
                String phone = scanner.nextLine();

                System.out.print("请输入密码: ");
                String password = scanner.nextLine();

                User user = userSystem.login(phone, password);
                if (user != null) {
                    System.out.println("\n登录成功！");
                    System.out.println(user);
                } else {
                    System.out.println("登录失败，手机号或密码错误！");
                }

            } else if (choice == 3) {
                userSystem.displayAllUsers();

            } else if (choice == 4) {
                System.out.println("感谢使用，再见！");
                break;
            }
        }

        scanner.close();
    }
}