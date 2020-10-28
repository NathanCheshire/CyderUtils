package com.cyder.utilities;

import com.cyder.ui.CyderButton;
import com.cyder.ui.CyderScrollPane;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

public class Pizza {
    private Util pizzaUtil = new Util();
    private JFrame pizzaFrame;
    private JTextField enterNameField;
    private JTextField enterName;
    private JRadioButton medium;
    private JRadioButton small;
    private JRadioButton large;
    private JList<?> pizzaTopingsList;
    private JComboBox<?> pizzaCrustCombo;
    private JTextArea orderCommentsTextArea;
    private JCheckBox breadSticks;
    private JCheckBox salad;
    private JCheckBox soda;

    public Pizza() {
        if (pizzaFrame != null)
            new Util().closeAnimation(pizzaFrame);

        pizzaFrame = new JFrame();
        pizzaFrame.setTitle("Pizza");
        pizzaFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pizzaFrame.setIconImage(pizzaUtil.getCyderIcon().getImage());
        pizzaFrame.setResizable(false);

        JPanel parentPanel = new JPanel();
        parentPanel.setLayout(new BoxLayout(parentPanel, BoxLayout.Y_AXIS));
        parentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel CustomerName = new JLabel("Name");
        CustomerName.setFont(pizzaUtil.weatherFontSmall);
        CustomerName.setForeground(pizzaUtil.navy);
        JPanel namePanel = new JPanel();
        namePanel.add(CustomerName, SwingConstants.CENTER);

        enterName = new JTextField(20);
        enterName.setSelectionColor(pizzaUtil.selectionColor);
        enterName.setForeground(pizzaUtil.navy);
        enterName.setFont(pizzaUtil.weatherFontSmall);
        enterName.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (enterName.getText().length() == 1) {
                    enterName.setText(enterName.getText().toUpperCase());
                }
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (enterName.getText().length() == 1) {
                    enterName.setText(enterName.getText().toUpperCase());
                }
            }

            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (enterName.getText().length() == 1) {
                    enterName.setText(enterName.getText().toUpperCase());
                }
            }
        });

        enterName.setBorder(new LineBorder(pizzaUtil.navy,5,false));
        JPanel nameFieldPanel = new JPanel();
        nameFieldPanel.add(enterName, SwingConstants.CENTER);

        JPanel pizzaSizeLabelPanel = new JPanel();
        JLabel pizzaSizeLabel = new JLabel("Pizza Size");
        pizzaSizeLabel.setFont(pizzaUtil.weatherFontSmall);
        pizzaSizeLabel.setForeground(pizzaUtil.navy);
        pizzaSizeLabelPanel.add(pizzaSizeLabel, SwingConstants.CENTER);

        JPanel pizzaSizePanel = new JPanel();
        small = new JRadioButton("small");
        small.setFont(pizzaUtil.weatherFontSmall);
        small.setForeground(pizzaUtil.navy);
        medium = new JRadioButton("medium");
        medium.setFont(pizzaUtil.weatherFontSmall);
        medium.setForeground(pizzaUtil.navy);
        large = new JRadioButton("Large");
        large.setFont(pizzaUtil.weatherFontSmall);
        large.setForeground(pizzaUtil.navy);
        ButtonGroup pizzaSizeGroup = new ButtonGroup();
        pizzaSizeGroup.add(small);
        pizzaSizeGroup.add(medium);
        pizzaSizeGroup.add(large);
        pizzaSizePanel.add(small);
        pizzaSizePanel.add(medium);
        pizzaSizePanel.add(large);

        JPanel crustPanel = new JPanel();
        JLabel crustLabel = new JLabel("Crust Type");
        crustLabel.setFont(pizzaUtil.weatherFontSmall);
        crustLabel.setForeground(pizzaUtil.navy);
        crustPanel.add(crustLabel,SwingConstants.CENTER);


        String[] CrustTypeChoice = {"Thin", "Thick", "Deep Dish", "Classic", "Tavern", "Seasonal"};
        pizzaCrustCombo = new JComboBox(CrustTypeChoice);
        pizzaCrustCombo.setBorder(new LineBorder(pizzaUtil.navy,5,false));
        pizzaCrustCombo.setForeground(pizzaUtil.navy);
        pizzaCrustCombo.setFont(pizzaUtil.weatherFontSmall);
        JPanel crustChoicePanel = new JPanel();
        crustChoicePanel.add(pizzaCrustCombo,SwingConstants.CENTER);

        JPanel pizzaTopingsLabelPanel = new JPanel();
        JLabel Topings = new JLabel("Topings");
        Topings.setFont(pizzaUtil.weatherFontSmall);
        Topings.setForeground(pizzaUtil.navy);
        pizzaTopingsLabelPanel.add(Topings, SwingConstants.CENTER);

        String[] pizzaTopingsStrList = {"Pepperoni", "Sausage", "Green Peppers",
                "Onions", "Tomatoes", "Anchovies", "Bacon", "Chicken", "Beef",
                "Olives", "Mushrooms"};
        JList pizzaToppingsJList = new JList(pizzaTopingsStrList);
        pizzaTopingsList = pizzaToppingsJList;
        pizzaTopingsList.setForeground(pizzaUtil.navy);
        pizzaTopingsList.setFont(pizzaUtil.weatherFontSmall);
        pizzaTopingsList.setSelectionBackground(pizzaUtil.selectionColor);
        pizzaTopingsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        CyderScrollPane PizzaTopingsListScroll = new CyderScrollPane(pizzaTopingsList,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        PizzaTopingsListScroll.setThumbColor(pizzaUtil.regularRed);
        PizzaTopingsListScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        PizzaTopingsListScroll.getViewport().setBorder(null);
        PizzaTopingsListScroll.setViewportBorder(null);
        PizzaTopingsListScroll.setBorder(new LineBorder(pizzaUtil.navy,5,false));
        PizzaTopingsListScroll.setPreferredSize(new Dimension(200,200));
        JPanel pizzaTopingsScrollPanel = new JPanel();
        pizzaTopingsScrollPanel.add(PizzaTopingsListScroll, SwingConstants.CENTER);

        JPanel extrasLabelPanel = new JPanel();
        JLabel Extra = new JLabel("Extras");
        Extra.setForeground(pizzaUtil.navy);
        Extra.setFont(pizzaUtil.weatherFontSmall);
        extrasLabelPanel.add(Extra,SwingConstants.CENTER);

        JPanel extraCheckPanel = new JPanel();
        breadSticks = new JCheckBox("Breadsticks");
        breadSticks.setForeground(pizzaUtil.navy);
        breadSticks.setFont(pizzaUtil.weatherFontSmall);
        salad = new JCheckBox("Salad");
        salad.setForeground(pizzaUtil.navy);
        salad.setFont(pizzaUtil.weatherFontSmall);
        soda = new JCheckBox("Soda");
        soda.setForeground(pizzaUtil.navy);
        soda.setFont(pizzaUtil.weatherFontSmall);
        extraCheckPanel.add(breadSticks);
        extraCheckPanel.add(salad);
        extraCheckPanel.add(soda);

        JLabel orderCommentsLabel = new JLabel("Order Comments");
        orderCommentsLabel.setFont(pizzaUtil.weatherFontSmall);
        orderCommentsLabel.setForeground(pizzaUtil.navy);
        JPanel orderCommentsLabelPanel = new JPanel();
        orderCommentsLabelPanel.add(orderCommentsLabel, SwingConstants.CENTER);

        orderCommentsTextArea = new JTextArea(5,20);
        orderCommentsTextArea.setFont(pizzaUtil.weatherFontSmall);
        orderCommentsTextArea.setAutoscrolls(true);
        orderCommentsTextArea.setLineWrap(true);
        orderCommentsTextArea.setWrapStyleWord(true);
        orderCommentsTextArea.setSelectedTextColor(pizzaUtil.selectionColor);
        orderCommentsTextArea.setBorder(new LineBorder(new Color(0, 0, 0)));

        CyderScrollPane orderCommentsScroll = new CyderScrollPane(orderCommentsTextArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        orderCommentsScroll.setThumbColor(pizzaUtil.regularRed);
        orderCommentsScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        orderCommentsScroll.getViewport().setBorder(null);
        orderCommentsScroll.setViewportBorder(null);
        orderCommentsScroll.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(pizzaUtil.navy,5,false)));
        orderCommentsScroll.setPreferredSize(new Dimension(400,200));
        JPanel scrollPanel = new JPanel();
        scrollPanel.add(orderCommentsScroll, SwingConstants.CENTER);

        JPanel BottomButtons = new JPanel();
        CyderButton placeOrder = new CyderButton("Place Order");
        placeOrder.setFont(pizzaUtil.weatherFontSmall);
        placeOrder.setColors(pizzaUtil.regularRed);
        placeOrder.addActionListener(e -> {
            if (enterName.getText().length() <= 0) {
                pizzaUtil.inform("Sorry, but you must enter a name.","", 400, 200);
            }

            else {
                String Name = enterName.getText().substring(0, 1).toUpperCase() + enterName.getText().substring(1) + "<br/>";
                String Size;

                if (small.isSelected()) {
                    Size = "small<br/>";
                }

                else if (medium.isSelected()) {
                    Size = "medium<br/>";
                }

                else {
                    Size = "Large<br/>";
                }

                String Crust = pizzaCrustCombo.getSelectedItem() + "<br/>";
                List<?> TopingsList = pizzaTopingsList.getSelectedValuesList();
                ArrayList<String> TopingsArrList = new ArrayList<>();

                for (Object o : TopingsList) {
                    TopingsArrList.add(o.toString());
                }

                if (TopingsArrList.isEmpty()) {
                    TopingsArrList.add("Plain");
                }

                StringBuilder TopingsChosen = new StringBuilder();

                for (String s : TopingsArrList) {
                    TopingsChosen.append(s).append("<br/>");
                }

                String Extras = "";

                if (breadSticks.isSelected()) {
                    Extras += "Breadsticks<br/>";
                }

                if (salad.isSelected()) {
                    Extras += "Salad<br/>";
                }

                if (soda.isSelected()) {
                    Extras += "Soda<br/>";
                }

                String Comments = orderCommentsTextArea.getText().trim();

                if (Extras.length() == 0) {
                    Extras = "";
                }

                else {
                    Extras = "<br/>Extras: " + "<br/>" + Extras;
                }



                if (Comments.length() == 0) {
                    pizzaUtil.inform("Customer Name: " + "<br/>" + Name + "<br/><br/>" + "Size: "
                            + "<br/>" + Size + "<br/><br/>" + "Crust: " + "<br/>" + Crust + "<br/><br/>" + "Topings: " + "<br/><br/>" + TopingsChosen
                            + "<br/><br/>" + Extras,"", 500, 1200);
                }

                else {
                    pizzaUtil.inform("Customer Name: " + "<br/>" + Name + "<br/><br/>" + "Size: "
                            + "<br/>" + Size + "<br/><br/>" + "Crust Type: " + "<br/>" + Crust + "<br/><br/>" + "Topings: " + "<br/>" + TopingsChosen
                            + "<br/>" + Extras + "<br/><br/>Comments: " + "<br/><br/>" + Comments,"", 500, 1200);
                }
            }
        });

        CyderButton resetPizza = new CyderButton("Reset Values");
        resetPizza.setColors(pizzaUtil.regularRed);
        resetPizza.setFont(pizzaUtil.weatherFontSmall);
        resetPizza.addActionListener(e -> {
            enterName.setText("");
            pizzaSizeGroup.clearSelection();
            pizzaCrustCombo.setSelectedItem("Thin");
            pizzaTopingsList.clearSelection();
            breadSticks.setSelected(false);
            salad.setSelected(false);
            soda.setSelected(false);
            orderCommentsTextArea.setText("");
        });

        resetPizza.setFocusPainted(false);
        resetPizza.setBorder(new LineBorder(pizzaUtil.navy,5,false));
        placeOrder.setBorder(new LineBorder(pizzaUtil.navy,5,false));
        resetPizza.setBackground(pizzaUtil.regularRed);
        placeOrder.setBackground(pizzaUtil.regularRed);
        placeOrder.setFocusPainted(false);
        BottomButtons.add(placeOrder);
        BottomButtons.add(resetPizza);

        parentPanel.add(namePanel);
        parentPanel.add(nameFieldPanel);
        parentPanel.add(pizzaSizeLabelPanel);
        parentPanel.add(pizzaSizePanel);
        parentPanel.add(crustPanel);
        parentPanel.add(crustChoicePanel);
        parentPanel.add(pizzaTopingsLabelPanel);
        parentPanel.add(pizzaTopingsScrollPanel);
        parentPanel.add(extrasLabelPanel);
        parentPanel.add(extraCheckPanel);
        parentPanel.add(orderCommentsLabelPanel);
        parentPanel.add(scrollPanel);
        parentPanel.add(BottomButtons);

        pizzaFrame.add(parentPanel);
        pizzaFrame.pack();
        pizzaFrame.setVisible(true);
        pizzaFrame.setLocationRelativeTo(null);
    }
}
