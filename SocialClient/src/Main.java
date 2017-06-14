import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.*;
import java.awt.Window;
import java.lang.management.ManagementFactory;


/**
 * Created by nico on 19/08/16.
 */
public class Main {
    public JPanel Panel;
    private JPanel Login;
    private JPanel Register;
    private JTextField UsernameLogin;
    private JPasswordField PasswordLogin;
    private JButton LoginButton;
    private JTextField mailRegister;
    private JTextField UsernameRegister;
    private JPasswordField Password1Register;
    private JPasswordField Password2Register;
    private JButton RegisterButton;

    private JFrame me;

    public Main(JFrame me) {

        this.me = me;
        LoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String res = new ServerConnection("").PerformLogin(UsernameLogin.getText(), PasswordLogin.getText());
                    if (!res.equals("error")) {
                        InitHomeScreen(res, UsernameLogin.getText());
                    } else
                        JOptionPane.showMessageDialog(null, "Credentials wrong");

                } catch (NullPointerException ex) {
                    JOptionPane.showMessageDialog(null, "Network error!!");
                }
            }
        });

        RegisterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Password1Register.getText().equals(Password2Register.getText())){
                    try {
                        String res = new ServerConnection(null).PerformRegistration(mailRegister.getText(), UsernameRegister.getText(), Password1Register.getText());
                        if (res.equals("InUse"))
                            JOptionPane.showMessageDialog(null, "Username already in use!");
                        else if (!res.equals("error"))
                            InitHomeScreen(res, UsernameRegister.getText());
                        else
                            JOptionPane.showMessageDialog(null, "Something went wrong. Please retry!");

                    } catch (NullPointerException ex){
                        JOptionPane.showMessageDialog(null, "Network error!!");
                    }
                }
                else JOptionPane.showMessageDialog(null, "The two password inserted do not match!");
            }
        });
    }

    public static void main(String[] args) {
        // avvio la prima finestra (Login e Registrazione)
        JFrame frame = new JFrame("Main");
        frame.setContentPane(new Main(frame).Panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(550,250);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    private void InitHomeScreen(String token, String username){
        JFrame frame = new JFrame("HomeScreen");
        frame.setContentPane(new HomeScreen(token, username, frame).Pane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(600,580);
        frame.setResizable(false);
        frame.setVisible(true);

        //chiudo la schermata di registrazione e login
        me.dispose();
    }

    public static void restart() {
        StringBuilder cmd = new StringBuilder();
        cmd.append(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java ");
        for (String jvmArg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            cmd.append(jvmArg + " ");
        }
        cmd.append("-cp ").append(ManagementFactory.getRuntimeMXBean().getClassPath()).append(" ");
        cmd.append(Window.class.getName()).append(" ");

        try {
            Runtime.getRuntime().exec(cmd.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
