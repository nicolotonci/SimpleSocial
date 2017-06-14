import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Created by nico on 23/08/16.
 */
public class SearchResultScreen {
    public JPanel SearchResultPanel;
    private JList resultList;
    private JButton sendFSButton;

    public SearchResultScreen(String token, String query){
        // invio la richiesta di ricerca al server
        ArrayList<String> results = new ServerConnection(token).PerformSearch(query);
        DefaultListModel<String> listModel = new DefaultListModel<>();

        // se il risultato della ricerca è vuoto, nascondo la lista e stampo "No Results"
        if (results.isEmpty()){
            resultList.setVisible(false);
            sendFSButton.setVisible(false);
            SearchResultPanel.add(new JLabel("No results!"));

         // altrimenti popolo la JList
        } else {

            for (String str : results)
                listModel.addElement(str);

            this.resultList.setModel(listModel);
        }
        resultList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (!resultList.getSelectedValue().toString().contains("(Friends)")) {
                        sendFSButton.setEnabled(true);
                    }
                    else
                        sendFSButton.setEnabled(false);
                }

            }
        });
        sendFSButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (new ServerConnection(token).PerformFRequest(resultList.getSelectedValue().toString()).equals("ok"))
                    JOptionPane.showMessageDialog(null, "Friendship request sent");
                else
                    JOptionPane.showMessageDialog(null, "Something went wrong! :( Try later!");

            }
        });
    }
}
