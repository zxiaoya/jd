package com.jd.utils;

import javax.swing.*;
import java.awt.*;

public class Picture extends JFrame {

    public Picture(String filePath) {
        //constructor
        this.setSize(400, 400);
        Image image = Toolkit.getDefaultToolkit().getImage(filePath);
        ImageIcon icon = new ImageIcon();
        icon.setImage(image);
        JButton button = new JButton();
        button.setIcon(icon);

        this.getContentPane().add(button);
        this.setVisible(true);
    }

    public Picture(String filePath, int showTime) {
        //constructor
        this.setSize(400, 400);
        Image image = Toolkit.getDefaultToolkit().getImage(filePath);
        ImageIcon icon = new ImageIcon();
        icon.setImage(image);
        JButton button = new JButton();
        button.setIcon(icon);

        this.getContentPane().add(button);
        this.setVisible(true);

        while (showTime-- < 0)
        {
                System.exit(0);
        }
    }

    public static void shutdown () {
        System.exit(0);
    }

    public static void main(String[] args) throws InterruptedException {
        new Picture("D://QR.png");
        Thread.sleep(2000);
        shutdown();
    }
}
