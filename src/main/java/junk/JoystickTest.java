/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package junk;

import com.cic.quadsim.input.GamePad;
import com.cic.quadsim.input.JinputDriverExtractor;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JFrame;
import net.java.games.input.Component;

/**
 *
 * @author mirko
 */
public class JoystickTest extends JFrame {

    static GamePad joystick = null;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        JinputDriverExtractor.extract();
        GamePad[] joysticks = GamePad.get();
        if (joysticks.length == 0) {
            return;
        }
        joystick = joysticks[0];

        JoystickTest frame = new JoystickTest();
        frame.setSize(400,400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public JoystickTest(){
    }

    public void paint(Graphics g0){
        long tick = System.currentTimeMillis();

        Graphics2D g = (Graphics2D)g0;

        joystick.poll();

        g.clearRect(0, 0, 400, 400);
        g.setColor(Color.black);
        float x = joystick.getChannelValue(net.java.games.input.Component.Identifier.Axis.X);
        float y = joystick.getChannelValue(net.java.games.input.Component.Identifier.Axis.Y);

        float rx = joystick.getChannelValue(net.java.games.input.Component.Identifier.Axis.RX);
        float ry = joystick.getChannelValue(net.java.games.input.Component.Identifier.Axis.RY);

        float powX = joystick.getPovX();
        float powY = joystick.getPovY();

        int leftRadius = (int) (50 * Math.sqrt(x*x + y*y));
        int rightRadius = (int) (50 * Math.sqrt(rx*rx + ry*ry));

        g.drawOval(100 - leftRadius, 100 - leftRadius, leftRadius * 2, leftRadius * 2);
        g.drawLine(100, 100, 100+(int)(x * 50), 100+(int)(y * 50));

        g.drawOval(300 - rightRadius, 100 - rightRadius, rightRadius * 2, rightRadius * 2);
        g.drawLine(300, 100, 300+(int)(rx * 50), 100+(int)(ry * 50));

        g.drawLine(100, 300, 100+(int)(powX * 50), 300+(int)(powY * 50));

        long tock = System.currentTimeMillis();
        long paintDuration = tock - tick;
        long frameDelay = 15 - paintDuration;

        if (frameDelay > 0) {
            try {
                Thread.sleep(frameDelay);
            } catch (Exception e) {
            }
        }

        this.repaint();
    }
}
