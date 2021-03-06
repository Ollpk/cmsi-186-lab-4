import java.awt.Graphics;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class RobotSoccerSimulation extends JPanel {
    private static final long serialVersionUID = -5228718339006830546L;

    // The soccer field
    private static final double WIDTH = 400;
    private static final double HEIGHT = 600;

    private static double PLAYER_RADIUS;
    private static double ENEMY_RADIUS;
    private static double PLAYER_SPEED;
    private static double ENEMY_SPEED;
    private static double FRICTION;

    public static void main(String[] args) {
        try {
            if (args.length != 5) {
                throw new IllegalArgumentException("You need exactly 5 arguments required");
            }

            PLAYER_RADIUS = Double.parseDouble(args[0]);
            ENEMY_RADIUS = Double.parseDouble(args[1]);
            PLAYER_SPEED = Double.parseDouble(args[2]);
            ENEMY_SPEED = Double.parseDouble(args[3]);
            FRICTION = Double.parseDouble(args[4]);
            balls = new Ball[] { new Ball(0.0, HEIGHT / 2, PLAYER_RADIUS, PLAYER_SPEED, Color.BLUE),
                    new Ball(WIDTH * 0.25, 40, ENEMY_RADIUS, ENEMY_SPEED, Color.RED),
                    new Ball(WIDTH * 0.75, 40, ENEMY_RADIUS, ENEMY_SPEED, Color.RED),
                    new Ball(WIDTH / 2, HEIGHT / 2, ENEMY_RADIUS, ENEMY_SPEED, Color.RED) };

            SwingUtilities.invokeLater(() -> {
                var panel = new RobotSoccerSimulation();
                panel.setBackground(Color.GREEN.brighter());
                var frame = new JFrame("Robotic Soccer");
                frame.setSize((int) WIDTH, (int) HEIGHT);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().add(panel, BorderLayout.CENTER);
                frame.setVisible(true);
                new Thread(() -> panel.runTheAnimation()).start();
            });

        } catch (NumberFormatException e) {
            System.err.println("Entry format was wrong");
        }
    }

    // Initially null; Set this to a string to end the simulation
    private volatile String endMessage = null;

    static class Ball {
        private double x;
        private double y;
        private double radius;
        private double speed;
        private Color color;

        Ball(double x, double y, double radius, double speed, Color color) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.speed = speed;
            this.color = color;

        }

        void moveToward(double targetX, double targetY) { // method movetoward will update the X and y of the ball to
                                                          // move it to the target x and y value.
            var dx = targetX - this.x;
            var dy = targetY - this.y;
            var v = this.speed / Math.hypot(dx, dy);

            this.x = constrain(this.x + v * dx, this.radius, WIDTH - this.radius);
            this.y = constrain(this.y + v * dy, this.radius, HEIGHT - this.radius);

        }

        private static double constrain(double value, double low, double high) {
            if (value < low) {
                return low;
            }
            if (value > high) {
                return high;
            }
            return value;
        }

        void applyFriction() {
            if (this.speed < 0) {
                this.speed = 1.3;
            } else {
                this.speed = constrain(this.speed - FRICTION, 0, 10000);
            }

        }

        // Returns whether the ball is *entirely* inside the goal
        boolean inside(Goal goal) {

            return (this.x - this.radius > goal.x - (double) goal.x / 2
                    && x + this.radius < goal.x + (double) goal.w / 2
                    && this.y - this.radius > goal.y - (double) goal.h / 2
                    && this.y + this.radius < goal.y + (double) goal.h / 2);

        }
    }

    private static Ball[] balls;

    private static class Goal {
        double x = (double) WIDTH / 2;
        double y = 0;
        double w = 100;
        double h = 100;
    }

    private static Goal goal = new Goal();

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (var ball : balls) {
            g.setColor(ball.color);
            g.fillOval((int) (ball.x - ball.radius), (int) (ball.y - ball.radius), (int) ball.radius * 2,
                    (int) ball.radius * 2);
        }
        g.setColor(new Color(255, 255, 255, 128));
        g.fillRect((int) (goal.x - goal.w / 2), (int) (goal.y - goal.h / 2), (int) goal.w, (int) goal.h);
        if (endMessage != null) {
            g.setFont(new Font("Arial", Font.PLAIN, 50));
            g.setColor(Color.RED);
            g.drawString(endMessage, 30, (int) HEIGHT / 2);

        }
    }

    private void runTheAnimation() {
        while (endMessage == null) {
            for (var i = 0; i < balls.length; i++) {

                balls[i].applyFriction();
                if (i == 0) {
                    balls[i].moveToward(goal.x, goal.y);
                } else {
                    balls[i].moveToward(balls[0].x, balls[0].y);
                }
            }
            adjustIfCollisions();
            endSimulationIFNecessary();
            repaint();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }

        }
    }

    private void adjustIfCollisions() {
        for (var b1 : balls) {
            for (var b2 : balls) {
                if (b1 != b2) {
                    var dx = b2.x - b1.x;
                    var dy = b2.y - b1.y;
                    var distance = Math.hypot(dx, dy);
                    var overlap = b1.radius + b2.radius - distance;
                    if (overlap > 0) {

                        double adjustX, adjustY;

                        adjustX = ((double) overlap / 2) * ((double) dx / distance);
                        adjustY = ((double) overlap / 2) * ((double) dy / distance);
                        b1.x -= adjustX;
                        b1.y -= adjustY;
                        b2.x += adjustX;
                        b2.y += adjustY;

                    }
                }
            }
        }

    }

    private void endSimulationIFNecessary() {
        if (balls[0].speed <= 0) {
            endMessage = "Oh no";
        } else if (balls[0].inside(goal)) {
            endMessage = "goal!";

        }
    }

}