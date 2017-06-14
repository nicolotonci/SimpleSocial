import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Created by nico on 21/08/16.
 */
public class FriendListScreen {
    private JList friendlist;
    public JPanel FriendlistPanel;
    private JButton FollowBtn;

    private String token = null;

    public FriendListScreen(String token, ContentRemoteInterface ri){
        this.token = token;

        // invio la richiesta della lista amici al server
        ArrayList<String> friendlst = new ServerConnection(token).RetriveFriendList();

        // popolo la JList
        DefaultListModel<String> listModel = new DefaultListModel();
        for (String str : friendlst)
            listModel.addElement(str);

        this.friendlist.setModel(listModel);

        this.friendlist.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (!friendlist.getSelectedValue().toString().contains("(Following)")){
                        FollowBtn.setEnabled(true);
                    }
                    else
                        FollowBtn.setEnabled(false);
                }
            }
        });

        FollowBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // invio al server la volonta di seguire l'utente selezionato (RMI)
                    ri.Follow(token, friendlist.getSelectedValue().toString().split(" ")[0].trim());
                    FollowBtn.setEnabled(false);
                    // aggiorno l'elemento selezionato scrivendo che lo sto seguendo
                    listModel.setElementAt(friendlist.getSelectedValue() + " (Following)", friendlist.getSelectedIndex());
                } catch (RemoteException ex){
                    ex.printStackTrace();
                }
            }
        });
    }
}
