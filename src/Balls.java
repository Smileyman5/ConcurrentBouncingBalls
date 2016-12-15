import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Created by alex on 9/19/2016.
 */
public class Balls extends JPanel implements MouseListener
{
    // Frames per seconds
    private static final int UPDATE_RATE = 30;
    // Number of balls the program will start with
    private static final int STARTING_BALL_COUNT = 10;

    private final CopyOnWriteArrayList<BallModel> balls = new CopyOnWriteArrayList<BallModel>();
    private final CopyOnWriteArrayList<Rect> rects = new CopyOnWriteArrayList<Rect>();

    private DrawCanvas canvas = new DrawCanvas();

    public Balls()
    {
        this.setLayout(new BorderLayout());
        add(canvas, BorderLayout.CENTER);
        addMouseListener(this);
        for(int i = 0; i < STARTING_BALL_COUNT; i++)
            balls.add(new BallModel());
        balls.forEach(ball -> new Thread(() ->
        {
            // while (true)
            // {
            //     int width = (getWidth() > 0) ? getWidth() : 900;
            //     int height = (getHeight() > 0) ? getHeight() : 900;
            //     if(!ball.updateNoCollisions(width, height,  balls))
            //         break;
            //     repaint();
            //     try
            //     {
            //         Thread.sleep(1000 / UPDATE_RATE);
            //     } catch (InterruptedException ex)
            //     {
            //         System.err.println(ex.getMessage());
            //     }
            // }
            ball.inInitial.set(false);
            while (true)
            {
                int width = (getWidth() > 0) ? getWidth() : 900;
                int height = (getHeight() > 0) ? getHeight() : 900;
                ball.update(width, height, balls, rects);
                repaint();
                try
                {
                    Thread.sleep(1000 / UPDATE_RATE);
                } catch (InterruptedException ex)
                {
                    System.err.println(ex.getMessage());
                }
            }
        }).start());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseClicked(MouseEvent e)
    {
        if (e.getClickCount() >= 2)
        {
            BallModel ball = new BallModel(e.getPoint());
            balls.add(ball);
            new Thread(() ->
            {
                // while (true)
                // {
                //     if(!ball.updateNoCollisions(getWidth(), getHeight(),  balls))
                //         break;
                //     repaint();
                //     try
                //     {
                //         Thread.sleep(1000 / UPDATE_RATE);
                //     } catch (InterruptedException ex)
                //     {
                //         System.err.println(ex.getMessage());
                //     }
                // }
                ball.inInitial.set(false);
                while (true)
                {
                    ball.update(getWidth(), getHeight(), balls, rects);
                    repaint();
                    try
                    {
                        Thread.sleep(1000 / UPDATE_RATE);
                    } catch (InterruptedException ex)
                    {
                        System.err.println(ex.getMessage());
                    }
                }
            }).start();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mousePressed(MouseEvent e)
    {
        rects.add(new Rect(e.getPoint()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseReleased(MouseEvent e)
    {
        Rect r = rects.get(rects.size() - 1);
        if(r.pointx == e.getPoint().x || r.pointy == e.getPoint().y)
            rects.remove(r); 
        else
            r.setDimensions(e.getPoint());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseEntered(MouseEvent e) { }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseExited(MouseEvent e) { }

    /**
     * Inner class to display background and paint components
     */
    class DrawCanvas extends JPanel
    {
        /**
         * {@inheritDoc}
         */
        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            balls.forEach(ball -> ball.draw(g));
            rects.forEach(rect ->
            {
                g.setColor(Color.YELLOW);
                if (rect.width != -1 || (rect.pointx > getWidth() && rect.pointy > getHeight()))
                    g.fillRect(rect.pointx, rect.pointy, rect.width, rect.height);
            });
        }
    }

    /**
     * Inner class for holding rectangle data
     */
    class Rect
    {
        int pointx;
        int pointy;
        int width;
        int height;

        /**
         * Constructor.
         * @param p
         *      The point of the rectangles origin
         */
        public Rect(Point p)
        {
            pointx = p.x;
            pointy = p.y;
            width = -1;
            height = -1;
        }

        /**
         * Sets the dimensions of the line drawn
         *  (0,0)_ _ _ _ _ _(X, 0)
         *      |
         *      |
         *      |
         * (0,Y)|
         *
         * @param p
         *      The opposite point from the rectangles origin
         */
        public void setDimensions(Point p)
        {
            // Switch origin point if drawn backwards
            int w = p.x - pointx;
            if(w < 0)
                pointx = p.x;
            width = Math.abs(w);

            int h = p.y - pointy;
            if(h < 0)
                pointy = p.y;
            height = Math.abs(h);
        }
    }

}
