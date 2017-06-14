import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by nico on 21/08/16.
 */
public class HomeScreen {
    private JButton LogoutButton;
    private JTextField searchText;
    private JButton searchButton;
    private JButton FriendsButton;
    public JPanel Pane;
    private JList ContentList;
    private JTextArea PublishText;
    private JButton publishButton;
    private JList pendingFREquest;
    private JButton confirmFReq;
    private JButton denyFReq;
    private JLabel statusBar;

    private JFrame me;

    private String token;

    public static DefaultListModel<String> listModel = null;
    public static DefaultListModel<String> contentListModel = null;

    private ContentRemoteInterface ri;



    public HomeScreen(String token, String username, JFrame parent) {
        this.token = token;
        this.me = parent;
        this.statusBar = new JLabel();
        this.statusBar.setPreferredSize(new Dimension(550, 16));
        Pane.add(this.statusBar, java.awt.BorderLayout.SOUTH);

        this.statusBar.setText("You are logged as " + username);

        // invalido il token ogni 24h, rieseguendo il login
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(null, "Token Expired! Login again.");
                        JFrame frame = new JFrame("Main");
                        frame.setContentPane(new Main(frame).Panel);
                        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        frame.pack();
                        frame.setSize(550,250);
                        frame.setResizable(false);
                        frame.setVisible(true);
                        me.dispose();
                    }
                },
                86400000 // ms in 24h
        );

        // avvio il server in ascolto sul gruppo multicast per il keepAlive
        new Thread(new KeepAlive(token)).start();

        // gestisco le richieste di amicizia e avvio il server delle richieste
        HomeScreen.listModel = new DefaultListModel<>();
        pendingFREquest.setModel(HomeScreen.listModel);
        new Thread(new FRequestsServer(token)).start();

        // registro la callback sul server RMI
        try {
            this.ContentList.setModel(contentListModel = new DefaultListModel<String>());
            ContentClientInterface ContentClientStub = (ContentClientInterface) UnicastRemoteObject.exportObject(new ContentClient(),0);
            Registry registry = LocateRegistry.getRegistry("localhost", 1501);
            this.ri = (ContentRemoteInterface) registry.lookup(ContentRemoteInterface.OBJECT_NAME);

            this.ri.RegisterCallback(token, ContentClientStub);

        } catch (RemoteException e ){
            e.printStackTrace();
        } catch (NotBoundException  e){
            e.printStackTrace();
        }


        LogoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ServerConnection(token).PerformLogout();

                // termino il processo
                Main.restart();
            }
        });
        FriendsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame("FriendListScreen");
                frame.setContentPane(new FriendListScreen(token, ri).FriendlistPanel);
                frame.pack();
                frame.setResizable(false);
                frame.setSize(400,400);
                frame.setVisible(true);
            }
        });
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame("SearchResultScreen");
                frame.setContentPane(new SearchResultScreen(token, searchText.getText()).SearchResultPanel);
                frame.pack();
                frame.setResizable(false);
                frame.setSize(400,400);
                frame.setVisible(true);
                searchText.setText("");
            }
        });
        publishButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (new ServerConnection(token).PublishContent(PublishText.getText()).equals("ok")){
                    PublishText.setText("");
                }
                else
                    JOptionPane.showMessageDialog(null, "Something went wrong. Try later!");
            }
        });
        confirmFReq.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // richiesta di amcizia selezionata accettata
                String frd = pendingFREquest.getSelectedValue().toString();
                if (new ServerConnection(token).PerformFRequestReply(frd, true).equals("ok")) {
                    listModel.remove(pendingFREquest.getSelectedIndex());
                    JOptionPane.showMessageDialog(null, "You and " + frd +" are now friends!");
                    confirmFReq.setEnabled(false);
                    denyFReq.setEnabled(false);
                } else
                    JOptionPane.showMessageDialog(null, "Something went wrong. Try later!");
            }
        });
        denyFReq.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // richiesta di amicizia selezionata non accettata
                String frd = pendingFREquest.getSelectedValue().toString();
                if (new ServerConnection(token).PerformFRequestReply(frd, false).equals("ok")) {
                    listModel.remove(pendingFREquest.getSelectedIndex());
                    confirmFReq.setEnabled(false);
                    denyFReq.setEnabled(false);
                } else
                    JOptionPane.showMessageDialog(null, "Something went wrong. Try later!");
            }
        });

        pendingFREquest.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    confirmFReq.setEnabled(true);
                    denyFReq.setEnabled(true);
                }
            }
        });
    }
}
