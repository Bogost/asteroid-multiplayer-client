package edu.bo.app.asteroid_multiplayer_client.controller;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class FramePainter {

    private interface PaintElement {

        void execute();
    }

    private ArrayList<PaintElement> paintArray = new ArrayList<>();

    private Canvas canvas;
    private GraphicsContext gc;
    private Image ufo, bigUfo;

    private double width;
    private double height;

    public FramePainter(Canvas c) {
        this.canvas = c;
        width = this.canvas.getWidth();
        height = this.canvas.getHeight();
        initializeImages();
        // initializeImages();
        gc = canvas.getGraphicsContext2D();
        clean();
    }

    private String getUrlFromPath(String path) {
        try {
            return new File(path).toURI()
                                 .toURL()
                                 .toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void initializeImages() {
        ufo = new Image(getUrlFromPath("images/asteroid_ufo.png"));
        bigUfo = new Image(getUrlFromPath("images/asteroid_big_ufo.png"));
    }

    private void clean() {
        paintArray.clear();
        paintArray.add(this::cleanCanvas);
    }

    public void paint() {
        paintArray.forEach(PaintElement::execute);
        clean();
    }

    public void cleanCanvas() {
        int width = (int) canvas.getWidth();
        int height = (int) canvas.getHeight();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, width, height);
    }

    private void paintShip(double x, double y, double rotation, Color color) {
        double[] xPoints = {0.0, 10.0, 20.0};
        double[] yPoints = {0.0, 30.0, 0.0};
        double xMiddle = 10;
        double yMiddle = 15;
        double xStore;
        for (int i = 0; i < xPoints.length; i++) {
            xPoints[i] -= xMiddle;
            yPoints[i] -= yMiddle;
            xStore = xPoints[i];
            xPoints[i] = yPoints[i] * Math.sin(degreeToRadian(rotation)) + xStore * Math.cos(degreeToRadian(rotation));
            yPoints[i] = -1 * yPoints[i] * Math.cos(degreeToRadian(rotation)) + xStore * Math.sin(degreeToRadian(rotation));
            xPoints[i] += xMiddle + x;
            yPoints[i] += yMiddle + y;
        }
        paintArray.add(() -> {
            gc.setStroke(color);
            gc.strokePolygon(xPoints, yPoints, 3);
        });
    }

    public void addPlayer(double x, double y, double rotation) {
        paintShip(x, y, rotation, Color.GREEN);
    }

    private double degreeToRadian(double degree) {
        return degree * Math.PI / 180;
    }

    public void addRemotePlayer(double x, double y, double rotation) {
        paintShip(x, y, rotation, Color.WHITE);
    }

    public void addCounter(int i) {
        paintArray.add(() -> {
            gc.setFill(Color.WHITE);
            gc.setFont(new Font(30));
            gc.fillText("" + i, width / 2 - 20, 30);
            gc.rotate(0);
        });
    }

    private void paintPoints(int points, int position, Color color) {
        paintArray.add(() -> {
            gc.setFill(color);
            gc.setFont(new Font(7));
            gc.fillText("" + points, width - 30, 10 * (position + 1));
            gc.rotate(0);
        });
    }

    public void addRemotePoints(int points, int position) {
        paintPoints(points, position, Color.WHITE);
    }

    public void addYouPoints(int points, int position) {
        paintPoints(points, position, Color.YELLOW);
    }

    private void paintBullet(double x, double y, Color color) {
        double radius = 5;
        paintArray.add(() -> {
            gc.setFill(color);
            gc.fillOval(x, y, radius, radius);
        });
    }

    public void addRemotePlayerBullet(double x, double y) {
        paintBullet(x, y, Color.WHITE);
    }

    public void addPlayerBullet(double x, double y) {
        paintBullet(x, y, Color.GREEN);
    }

    public void addBigUfo(double x, double y) {
        paintArray.add(() -> {
            gc.drawImage(ufo, x, y);
        });
    }

    public void addSmallUfo(double x, double y) {
        paintArray.add(() -> {
            gc.drawImage(bigUfo, x, y);
        });
    }

}
