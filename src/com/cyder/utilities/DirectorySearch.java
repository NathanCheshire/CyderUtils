package com.cyder.utilities;

import com.cyder.ui.CyderButton;
import com.cyder.ui.CyderScrollPane;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Stack;

public class DirectorySearch {
    private Util dirUtil = new Util();
    private JFrame dirFrame;
    private JTextField dirField;
    private CyderScrollPane dirScroll;
    private JList<?> directoryNameList;
    private JList<?> directoryList;
    private JPanel dirSearchParentPanel;

    private Stack<String> backward = new Stack<>();
    private Stack<String> foward = new Stack<>();

    private String rightNow = System.getProperty("user.dir");

    public DirectorySearch() {
        if (dirFrame != null) {
            dirUtil.closeAnimation(dirFrame);
            dirFrame.dispose();
        }

        dirFrame = new JFrame();
        dirFrame.setTitle("Directory Search");
        dirFrame.setResizable(false);
        dirFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dirFrame.setIconImage(dirUtil.getCyderIcon().getImage());

        dirSearchParentPanel = new JPanel();
        dirSearchParentPanel.setLayout(new BorderLayout());

        dirField = new JTextField(40);
        dirField.setSelectionColor(dirUtil.selectionColor);
        dirField.setText(System.getProperty("user.dir"));
        dirField.setFont(dirUtil.weatherFontSmall);
        dirField.setForeground(dirUtil.navy);

        dirField.addActionListener(directoryFieldListener);
        JPanel dirFieldPanel = new JPanel();
        dirFieldPanel.setLayout(new BorderLayout());

        dirField.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),new LineBorder(dirUtil.navy,5,false)));

        CyderButton last = new CyderButton(" < ");
        last.setFocusPainted(false);
        last.setForeground(dirUtil.navy);
        last.setBackground(dirUtil.regularRed);
        last.setFont(dirUtil.weatherFontSmall);
        last.setBorder(new LineBorder(dirUtil.navy,5,false));
        last.setColors(dirUtil.regularRed);
        last.addActionListener(e -> {
            if (!backward.empty()) {
                foward.push(rightNow);
                File ChosenDir = new File(backward.pop());
                rightNow = ChosenDir.toString();

                printStacks();

                dirField.setText(ChosenDir.toString());
                directoryList = new JList(ChosenDir.listFiles());

                File[] Files = ChosenDir.listFiles();
                String[] Names = new String[Files.length];

                for (int i = 0 ; i < Files.length ; i++) {
                    Names[i] = Files[i].getName();
                }

                directoryNameList = new JList(Names);
                directoryNameList.setFont(dirUtil.weatherFontSmall);
                directoryNameList.setForeground(dirUtil.navy);
                directoryNameList.setSelectionBackground(dirUtil.selectionColor);
                directoryNameList.addMouseListener(directoryListener);
                directoryNameList.addKeyListener(directoryEnterListener);

                dirScroll.setViewportView(directoryNameList);
                dirScroll.revalidate();
                dirScroll.repaint();

                dirSearchParentPanel.revalidate();
                dirSearchParentPanel.repaint();

                dirFrame.revalidate();
                dirFrame.repaint();
            }
        });

        dirFieldPanel.add(last, BorderLayout.LINE_START);
        dirFieldPanel.add(dirField, BorderLayout.CENTER);

        CyderButton next = new CyderButton(" > ");
        next.setFocusPainted(false);
        next.setForeground(dirUtil.navy);
        next.setBackground(dirUtil.regularRed);
        next.setFont(dirUtil.weatherFontSmall);
        next.setBorder(new LineBorder(dirUtil.navy,5,false));
        next.setColors(dirUtil.regularRed);
        next.addActionListener(e -> {
            if (!foward.empty()) {
                backward.push(rightNow);
                File ChosenDir = new File(foward.pop());
                rightNow = ChosenDir.toString();

                printStacks();

                dirField.setText(ChosenDir.toString());
                directoryList = new JList(ChosenDir.listFiles());

                File[] Files = ChosenDir.listFiles();
                String[] Names = new String[Files.length];

                for (int i = 0 ; i < Files.length ; i++) {
                    Names[i] = Files[i].getName();
                }

                directoryNameList = new JList(Names);
                directoryNameList.setFont(dirUtil.weatherFontSmall);
                directoryNameList.setForeground(dirUtil.navy);
                directoryNameList.setSelectionBackground(dirUtil.selectionColor);
                directoryNameList.addMouseListener(directoryListener);
                directoryNameList.addKeyListener(directoryEnterListener);

                dirScroll.setViewportView(directoryNameList);
                dirScroll.revalidate();
                dirScroll.repaint();

                dirSearchParentPanel.revalidate();
                dirSearchParentPanel.repaint();

                dirFrame.revalidate();
                dirFrame.repaint();
            }
        });

        dirFieldPanel.add(next, BorderLayout.LINE_END);

        dirSearchParentPanel.add(dirFieldPanel, BorderLayout.PAGE_START);

        File[] DirFiles = new File(System.getProperty("user.dir")).listFiles();

        directoryList = new JList(DirFiles);
        directoryList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        File ChosenDir = new File(System.getProperty("user.dir"));

        directoryList = new JList(ChosenDir.listFiles());

        File[] Files = ChosenDir.listFiles();
        String[] Names = new String[0];

        if (Files != null) {
            Names = new String[Files.length];
        }

        if (Files != null) {
            for (int i = 0 ; i < Files.length ; i++) {
                Names[i] = Files[i].getName();
            }
        }

        directoryNameList = new JList(Names);
        directoryNameList.setFont(dirUtil.weatherFontSmall);
        directoryNameList.setSelectionBackground(dirUtil.selectionColor);
        directoryNameList.setForeground(dirUtil.navy);
        directoryNameList.addMouseListener(directoryListener);

        directoryNameList.addKeyListener(directoryEnterListener);
        dirScroll = new CyderScrollPane(directoryNameList,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        dirScroll.setThumbColor(dirUtil.regularRed);

        dirScroll.setForeground(dirUtil.navy);
        dirScroll.setFont(dirUtil.weatherFontSmall);
        dirScroll.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(dirUtil.navy,5,false)));

        dirSearchParentPanel.add(dirScroll, BorderLayout.CENTER);
        dirSearchParentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        dirFrame.add(dirSearchParentPanel);
        dirFrame.pack();
        dirFrame.setLocationRelativeTo(null);
        dirFrame.setVisible(true);
        dirField.requestFocus();
    }

    private MouseListener directoryListener = new MouseAdapter() {
        public void mouseClicked(MouseEvent mouseEvent) {
            JList theList = (JList) mouseEvent.getSource();

            if (mouseEvent.getClickCount() == 2) {
                int index = theList.locationToIndex(mouseEvent.getPoint());

                if (index >= 0) {
                    File ChosenDir = (File) directoryList.getModel().getElementAt(index);

                    if (ChosenDir.isDirectory()) {
                        if (!foward.isEmpty() && !foward.peek().equals(rightNow))
                            foward.clear();

                        if (!backward.isEmpty() && !backward.peek().equals(rightNow) && !foward.isEmpty() && !foward.peek().equals(rightNow))
                            backward.push(rightNow);
                        else if (backward.isEmpty());
                            backward.push(rightNow);

                        rightNow = ChosenDir.toString();
                        printStacks();

                        dirField.setText(ChosenDir.toString());

                        directoryList = new JList(ChosenDir.listFiles());

                        File[] Files = ChosenDir.listFiles();
                        String[] Names = new String[Files.length];

                        for (int i = 0 ; i < Files.length ; i++) {
                            Names[i] = Files[i].getName();
                        }

                        directoryNameList = new JList(Names);

                        directoryNameList.setFont(dirUtil.weatherFontSmall);
                        directoryNameList.setForeground(dirUtil.navy);
                        directoryNameList.setSelectionBackground(dirUtil.selectionColor);

                        directoryNameList.addMouseListener(directoryListener);
                        directoryNameList.addKeyListener(directoryEnterListener);

                        dirScroll.setViewportView(directoryNameList);
                        dirScroll.revalidate();
                        dirScroll.repaint();

                        dirSearchParentPanel.revalidate();
                        dirSearchParentPanel.repaint();

                        dirFrame.revalidate();
                        dirFrame.repaint();
                    }

                    else if (ChosenDir.isFile()) {
                        dirUtil.openFile(ChosenDir.getAbsolutePath());
                    }
                }
            }
        }
    };

    private ActionListener directoryFieldListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String newDir = dirField.getText();
            File ChosenDir = new File(newDir);

            if (ChosenDir.exists()) {
                if (ChosenDir.isDirectory()) {
                    if (!foward.isEmpty() && !foward.peek().equals(rightNow))
                        foward.clear();

                    if (!backward.isEmpty() && !backward.peek().equals(rightNow) && !foward.isEmpty() && !foward.peek().equals(rightNow))
                        backward.push(rightNow);
                    else if (backward.isEmpty());
                        backward.push(rightNow);

                    rightNow = ChosenDir.toString();
                    printStacks();

                    directoryList = new JList(ChosenDir.listFiles());
                    File[] Files = ChosenDir.listFiles();
                    String[] Names = new String[0];
                    if (Files != null) {
                        Names = new String[Files.length];
                    }
                    if (Files != null) {
                        for (int i = 0 ; i < Files.length ; i++) {
                            Names[i] = Files[i].getName();
                        }
                    }

                    directoryNameList = new JList(Names);
                    directoryNameList.setFont(dirUtil.weatherFontSmall);
                    directoryNameList.setForeground(dirUtil.navy);
                    directoryNameList.setSelectionBackground(dirUtil.selectionColor);
                    directoryNameList.addMouseListener(directoryListener);
                    directoryNameList.addKeyListener(directoryEnterListener);
                    dirScroll.setViewportView(directoryNameList);
                    dirScroll.revalidate();
                    dirScroll.repaint();
                    dirSearchParentPanel.revalidate();
                    dirSearchParentPanel.repaint();
                    dirFrame.revalidate();
                    dirFrame.repaint();
                }

                else if (ChosenDir.isFile()) {
                    dirUtil.openFile(ChosenDir.getAbsolutePath());
                }
            }

            else {
                dirUtil.beep();
            }
        }
    };

    private KeyListener directoryEnterListener = new KeyAdapter() {
        @Override
        public void keyPressed(java.awt.event.KeyEvent e) {
            int index = directoryNameList.getSelectedIndex();

            if (index >= 0) {
                File ChosenDir = (File) directoryList.getModel().getElementAt(index);

                if (ChosenDir.isDirectory()) {
                    if (!foward.isEmpty() && !foward.peek().equals(rightNow))
                        foward.clear();

                    if (!backward.isEmpty() && !backward.peek().equals(rightNow) && !foward.isEmpty() && !foward.peek().equals(rightNow))
                        backward.push(rightNow);
                    else if (backward.isEmpty());
                        backward.push(rightNow);

                    rightNow = ChosenDir.toString();
                    printStacks();

                    dirField.setText(ChosenDir.toString());
                    directoryList = new JList(ChosenDir.listFiles());

                    File[] Files = ChosenDir.listFiles();
                    String[] Names = new String[Files.length];

                    for (int i = 0 ; i < Files.length ; i++) {
                        Names[i] = Files[i].getName();
                    }

                    directoryNameList = new JList(Names);
                    directoryNameList.setFont(dirUtil.weatherFontSmall);
                    directoryNameList.addMouseListener(directoryListener);
                    directoryNameList.addKeyListener(directoryEnterListener);

                    dirScroll.setViewportView(directoryNameList);
                    dirScroll.revalidate();
                    dirScroll.repaint();

                    dirSearchParentPanel.revalidate();
                    dirSearchParentPanel.repaint();

                    dirFrame.revalidate();
                    dirFrame.repaint();
                }

                else if (ChosenDir.isFile()) {
                    dirUtil.openFile(ChosenDir.getAbsolutePath());
                }
            }
        }
    };

    private void printStacks() {
        System.out.println("backwards:");
        for (int i = backward.size() - 1; i >= 0 ; i--) {
            System.out.println(backward.get(i));
        }

        System.out.println("fowards:");
        for (int i = foward.size() - 1; i >= 0 ; i--) {
            System.out.println(foward.get(i));
        }
    }
}
