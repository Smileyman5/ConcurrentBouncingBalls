import javax.swing.*;
import java.awt.*;

/**
 * Created by alex on 9/19/2016.
 * Entry Point of the program
 */
public class Main
{
    /**
     * Main method
     */
    public static void main(String[] args)
    {
        new Main();
    }

    /**
     * Sets visuals up on seperate thread
     */
    public Main()
    {
        SwingUtilities.invokeLater(() ->
        {
            // Setting up main frame
            JFrame frame = new JFrame("Bouncing Balls");
            // Exit on close
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            // mainPanel is where all visuals will take place
            frame.setContentPane(new Balls());
            // Set preferred size of screen
            frame.setPreferredSize(new Dimension(900, 900));
            // Packing...
            frame.pack();
            // Display!
            frame.setVisible(true);
        });
    }
}
