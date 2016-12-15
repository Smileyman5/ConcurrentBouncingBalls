import java.awt.*;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by alex on 9/19/2016.
 */
public class BallModel
{
    // Package Private
    AtomicBoolean inInitial = new AtomicBoolean();
    int ballX;
    int ballY;
    float ballSpeedX;
    float ballSpeedY;
    int nameCount;
    final ReentrantLock lock = new ReentrantLock();

    private static final Random RAND = new Random();

    private static int COUNT = 0;

    private String name;
    private int ballRadius;

    public Color color;

    /**
     * Constructor.
     * All values randomized
     */
    public BallModel()
    {
        this(new Point(RAND.nextInt(900), RAND.nextInt(900)));
    }

    /**
     * Constructor.
     * All values randomized except start location
     * @param point
     *      The location at which the ball will spawn
     */
    public BallModel(Point point)
    {
        name = "Ball " + COUNT;
        nameCount = COUNT++;
        color = new Color(RAND.nextInt(155) + 100, RAND.nextInt(155) + 100, RAND.nextInt(155) + 100);
        ballRadius = RAND.nextInt(20) + 20;
        ballX = ballRadius + point.x;
        ballY = ballRadius + point.y;
        ballSpeedX = RAND.nextInt(30) - 15;
        ballSpeedY = RAND.nextInt(30) - 15;
        inInitial.set(true);
    }

    public void draw (Graphics g)
    {
        Graphics2D g2d = (Graphics2D)g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(color);
        g2d.fillOval(ballX - ballRadius, ballY - ballRadius, 2 * ballRadius, 2 * ballRadius);

        g2d.dispose();
    }

    public void update(int width, int height, CopyOnWriteArrayList<BallModel> balls, CopyOnWriteArrayList<Balls.Rect> rects)
    {
        checkOtherBallCollision(width, balls);
        checkRectangleCollision(rects);
        checkWallCollision(width, height);

        lock.lock();
        try
        {
            ballX += ballSpeedX;
            ballY += ballSpeedY;
        } finally
        {
            lock.unlock();
        }

        // Put a limit on the velocity
//        if (ballSpeedX > 15)
//            ballSpeedX = 15;
//        else if (ballSpeedX < -15)
//             ballSpeedX = -15;
//        if (ballSpeedY > 15)
//            ballSpeedY = 15;
//        else if (ballSpeedY < -15)
//            ballSpeedY = -15;
    }

    public String getName()
    {
        return name;
    }
    public int getBallRadius()
    {
        return ballRadius;
    }
    public synchronized int getBallX()
    {
        return ballX;
    }
    public synchronized void setBallX(int ballX)
    {
        this.ballX = ballX;
    }
    public synchronized int getBallY()
    {
        return ballY;
    }
    public synchronized void setBallY(int ballY)
    {
        this.ballY = ballY;
    }
    public synchronized float getBallSpeedX()
    {
        return ballSpeedX;
    }
    public synchronized void setBallSpeedX(float ballSpeedX)
    {
        this.ballSpeedX = ballSpeedX;
    }
    public synchronized float getBallSpeedY()
    {
        return ballSpeedY;
    }
    public synchronized void setBallSpeedY(float ballSpeedY)
    {
        this.ballSpeedY = ballSpeedY;
    }

    public synchronized void avoidLeft(BallModel ball, double distance)
    {
        ballX -= (ballRadius + ball.getBallRadius()) - distance;
        ball.ballX -= ball.getBallSpeedX();
    }

    public synchronized void avoidRight(BallModel ball, double distance)
    {
        ballX += (ballRadius + ball.getBallRadius()) - distance;
        ball.ballX -= ball.getBallSpeedX();
    }

    private void checkWallCollision(int width, int height)
    {
        if (ballX - ballRadius < 0)
        {
            setBallSpeedX(-ballSpeedX);
            setBallX(ballRadius);
        }
        else if (ballX + ballRadius > width)
        {
            setBallSpeedX(-ballSpeedX);
            setBallX(width - ballRadius);
        }

        if (ballY - ballRadius < 0)
        {
            setBallSpeedY(-ballSpeedY);
            setBallY(ballRadius);
        }
        else if (ballY + ballRadius > height)
        {
            setBallSpeedY(-ballSpeedY);
            setBallY(height - ballRadius);
        }
    }

    private void checkOtherBallCollision(int width, CopyOnWriteArrayList<BallModel> balls)
    {
        for (BallModel ball: balls)
        {
            if (ball == this)
                continue;
            double distance = Math.sqrt(Math.pow(ballX - ball.getBallX(), 2) + Math.pow(ballY - ball.getBallY(), 2));
            if (!inInitial.get() && distance <= (ballRadius + ball.getBallRadius()))
            {
                if (nameCount < ball.nameCount && ball.lock.tryLock())
                {
                    lock.lock();
                    try
                    {
                        distance = Math.sqrt(Math.pow(ballX - ball.getBallX(), 2) + Math.pow(ballY - ball.getBallY(), 2));
                        if (distance <= (ballRadius + ball.getBallRadius()))
                        {
                            changeColor();
                            ball.changeColor();
                            if (ballX > width / 2)
                            {
                                if (ball.ballX > ballX)
                                    avoidLeft(ball, distance);
                                else
                                    ball.avoidLeft(ball, distance);
                            }
                            else
                            {
                                if (ballX > ball.ballX)
                                    avoidRight(ball, distance);
                                else
                                    ball.avoidRight(ball, distance);
                            }
                            if (ballY < ball.ballY)
                            {
                                ballY -= (ballRadius + ball.getBallRadius()) - distance;
                                ball.ballY -= ball.getBallSpeedY();
                            }
                            else
                            {
                                ballY += (ballRadius + ball.getBallRadius()) - distance;
                                ball.ballY -= ball.getBallSpeedY();
                            }

                            float bsx = ballSpeedX;
                            float bsy = ballSpeedY;
                            ballSpeedX = (ballSpeedX * (ballRadius - ball.getBallRadius()) + (2 * ball.getBallRadius() * ball.getBallSpeedX())) / (ballRadius + ball.getBallRadius());
                            ballSpeedY = (ballSpeedY * (ballRadius - ball.getBallRadius()) + (2 * ball.getBallRadius() * ball.getBallSpeedY())) / (ballRadius + ball.getBallRadius());
                            ball.ballSpeedX = (ball.getBallSpeedX() * (ball.getBallRadius() - ballRadius) + (2 * ballRadius * bsx)) / (ballRadius + ball.getBallRadius());
                            ball.ballSpeedY = (ball.getBallSpeedY() * (ball.getBallRadius() - ballRadius) + (2 * ballRadius * bsy)) / (ballRadius + ball.getBallRadius());
                        }
                    }
                    finally
                    {
                        lock.unlock();
                        ball.lock.unlock();
                    }

                    for (BallModel b : balls)
                    {
                        if (b == this)
                            continue;
                        double dist = Math.sqrt(Math.pow(ballX - b.getBallX(), 2) + Math.pow(ballY - b.getBallY(), 2));
                        if (!inInitial.get() && dist <= (ballRadius + b.getBallRadius()))
                        {
                            CopyOnWriteArrayList<BallModel> smallerList = new CopyOnWriteArrayList<>(balls);
                            smallerList.remove(ball);
                            checkOtherBallCollision(width, smallerList);
                        }
                    }
                }
            }
        }
    }

    public synchronized void changeColor()
    {
        color = new Color(RAND.nextInt(155) + 100, RAND.nextInt(155) + 100, RAND.nextInt(155) + 100);
    }

    private void checkRectangleCollision(CopyOnWriteArrayList<Balls.Rect> rects)
    {
        for (Balls.Rect rect: rects)
        {
            /* Awesome, but not right! /
            if (Math.abs(ballX - (rect.pointx + rect.width/2)) <= (rect.width/2 + ballRadius)
                    && Math.abs(ballY - (rect.pointy + rect.height/2)) <= (rect.height/2 + ballRadius))
            {
                ballSpeedX = -ballSpeedX;
                ballSpeedY = -ballSpeedY;
                ballX = rect.pointx + rect.width/2;
                ballY = rect.pointy + rect.height/2;
            }
            /-------------------------*/

            /* Right Side */
            if (ballX - ballRadius <= rect.pointx + rect.width && ballX - ballRadius > rect.pointx + rect.width/2 && ballY >= rect.pointy && ballY <= rect.pointy + rect.height)
            {
                setBallSpeedX(-ballSpeedX);
                setBallX(rect.pointx + rect.width + ballRadius);
            }
            // Left Side
            else if (ballX + ballRadius >= rect.pointx && ballX + ballRadius < rect.pointx + rect.width/2 && ballY >= rect.pointy && ballY <= rect.pointy + rect.height)
            {
                setBallSpeedX(-ballSpeedX);
                setBallX(rect.pointx - ballRadius);
            }
            // Top
            else if (ballY + ballRadius >= rect.pointy && ballY + ballRadius < rect.pointy + rect.height/2 && ballX >= rect.pointx && ballX <= rect.pointx + rect.width)
            {
                setBallSpeedY(-ballSpeedY);
                setBallY(rect.pointy - ballRadius);
            }
            // Bottom
            else if (ballY - ballRadius <= rect.pointy + rect.height && ballY - ballRadius > rect.pointy + rect.height/2 && ballX >= rect.pointx && ballX <= rect.pointx + rect.width)
            {
                setBallSpeedY(-ballSpeedY);
                setBallY(rect.pointy + rect.height + ballRadius);
            }
        }
    }

    public boolean updateNoCollisions(int width, int height, CopyOnWriteArrayList<BallModel> balls)
    {
        checkWallCollision(width, height);

        lock.lock();
        try
        {
            ballX += ballSpeedX;
            ballY += ballSpeedY;
        } finally
        {
            lock.unlock();
        }

        for (BallModel ball: balls)
        {
            if (ball == this)
                continue;
            double distance = Math.sqrt(Math.pow(ballX - ball.getBallX(), 2) + Math.pow(ballY - ball.getBallY(), 2));
            if (distance <= (ballRadius + ball.getBallRadius()))
            {
                return true;
            }
        }
        return false;
    }
}
