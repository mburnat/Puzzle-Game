package puzzle.view;

import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.util.Duration;
import javafx.animation.Animation;
import javafx.scene.shape.*;
import javafx.animation.PathTransition;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.*;
import puzzle.MainApp;
import javafx.scene.control.Label;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.io.File;
import javafx.embed.swing.SwingFXUtils;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import javafx.scene.layout.TilePane;
import java.util.Collections;
import javafx.scene.Node;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import puzzle.model.Tile;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.PrintWriter;
import java.util.Scanner;



public class TileController  {

    @FXML
    private MainApp main;
    @FXML
    private TilePane panel;
    @FXML
    private Label timelabel;

    @FXML private Label bestTimeLabel;

    ArrayList<Tile> tilesList = new ArrayList<Tile>();

    @FXML
    Tile first;
    @FXML
    Tile second;

    private Tile[] tiles = new Tile[9];
    private BufferedImage[] partsOfImage = new BufferedImage[9];
    private Timeline timeline;
    private String timeForm;
    private File file;
    private Scanner scanner;
    private long seconds;
    private long minutes;
    private long millis;
    private long theBestTime;



    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetheight)
    {

        BufferedImage resizedImage = new BufferedImage(targetWidth, targetheight, originalImage.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, targetWidth, targetheight, null);
        g.dispose();

        return resizedImage;
    }

    private PathTransition getPathTransition(Tile first, Tile second)
    {
        PathTransition ptr = new PathTransition();
        Path path = new Path();
        path.getElements().clear();
        path.getElements().add(new MoveToAbs(first));
        path.getElements().add(new LineToAbs(first, second.getLayoutX(), second.getLayoutY()));
        ptr.setPath(path);
        ptr.setNode(first);
        return ptr;
    }

    @FXML
    private void initialize(){

        try
        {
            loadFile();
            BufferedImage bufferedImage = ImageIO.read(new File("out/production/puzzle/assets/water.jpg"));
            BufferedImage image = resizeImage(bufferedImage,600,600);

            for(int i = 0, x = 0, y = 0; i < 9; i++, x += 200)
            {
                partsOfImage[i] = image.getSubimage(x,y, 200,200);
                tiles[i] = new Tile(200, 200, partsOfImage[i], i);
                tiles[i].setLayoutX(x);
                tiles[i].setLayoutY(y);
                tiles[i].setFill(new ImagePattern(SwingFXUtils.toFXImage(tiles[i].getPart(),null)));
                tilesList.add(tiles[i]);

                if (x == 400)
                {
                    x = -200;
                    y += 200;
                }

            }

            panel.getChildren().addAll(tilesList);
            first = null;
            second = null;

            for (Tile tile: tiles)
            {

                tile.setOnMouseClicked(new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent event) {

                            if (first == null)
                            {
                                first = (Tile) event.getSource();
                                return;
                            }

                            if (first != null)
                                second = (Tile) event.getSource();

                            if (first != null && second != null && first != second)
                            {
                                double fx = first.getLayoutX();
                                double fy = first.getLayoutY();
                                double sx = second.getLayoutX();
                                double sy = second.getLayoutY();

                                Collections.swap(tilesList, first.getid(), second.getid());
                                PathTransition ptr = getPathTransition(first, second);
                                PathTransition ptr2 = getPathTransition(second, first);
                                ParallelTransition pt = new ParallelTransition(ptr, ptr2);
                                pt.play();
                                pt.setOnFinished(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {
                                        first.setTranslateX(0);
                                        first.setTranslateY(0);
                                        second.setTranslateX(0);
                                        second.setTranslateY(0);

                                        first.setLayoutX(sx);
                                        first.setLayoutY(sy);
                                        second.setLayoutX(fx);
                                        second.setLayoutY(fy);

                                        first.setFill(new ImagePattern(SwingFXUtils.toFXImage(tilesList.get(first.getid()).getPart(),null)));
                                        second.setFill(new ImagePattern(SwingFXUtils.toFXImage(tilesList.get(second.getid()).getPart(),null)));
                                        first = null;
                                        second = null;
                                    }
                                });

                                if(isWon())
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {

                                            if (timeline != null)
                                                timeline.stop();
                                            saveFile();
                                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                            alert.setTitle("Win");
                                            alert.setHeaderText("You won!");
                                            alert.setContentText("Your time: " + timelabel.getText());
                                            alert.showAndWait();
                                        }
                                    });

                            }
                    }
                 });
               }

        }catch(IOException e){
            System.out.println("Error reading file!");
        }
    }

    @FXML
    private void handleRunBtnAction(){
        Collections.shuffle(tilesList);
        for (int i = 0; i < tilesList.size(); i++)
        {
            Tile tile = tilesList.get(i);
            int num = tile.getid();
            tile.setFill(new ImagePattern(SwingFXUtils.toFXImage(tilesList.get(num).getPart(), null)));
        }
        timeline = new Timeline(new KeyFrame(Duration.millis(100),
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        updateTime();
                    }
                }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private long time = 0;
    private void updateTime()
    {
        seconds = TimeUnit.MILLISECONDS.toSeconds(time);
        minutes = TimeUnit.MILLISECONDS.toMinutes(time);
        millis = time - TimeUnit.SECONDS.toMillis(seconds);
        timeForm = String.format("%02d:%02d:%d", minutes, seconds, millis);
        timelabel.setText(timeForm);
        time += 100;
    }

    private void loadFile()
    {
        try{
            file = new File("src/assets/najlepszyCzas.txt");
            scanner = new Scanner(file);

            long tmpValue = 0;
            String format = new String();
            String tmp = scanner.nextLine();
            format = tmp + ":";
            tmpValue += Long.parseLong(tmp);
            tmpValue *= 10000;
            theBestTime += tmpValue;
            tmp = scanner.nextLine();
            format +=tmp + ":";
            tmpValue = Long.parseLong(tmp);
            tmpValue *= 1000;
            theBestTime += tmpValue;
            tmp = scanner.nextLine();
            format +=tmp;
            tmpValue = Long.parseLong(tmp);
            theBestTime += tmpValue;
            bestTimeLabel.setText(format);
            scanner.close();

        }catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    private void saveFile()
    {
        long tmpTime = minutes * 10000 + seconds * 1000 + millis;

        if(tmpTime < theBestTime)
            try
            {
                PrintWriter printWriter = new PrintWriter("src/assets/najlepszyCzas.txt");
                String tmp = new String(String.valueOf(minutes));
                printWriter.println(tmp);
                tmp = String.valueOf(seconds);
                printWriter.println(tmp);
                tmp = String.valueOf(millis);
                printWriter.println(tmp);
                printWriter.close();
            }

            catch(FileNotFoundException e)
            {
                e.printStackTrace();
            }
    }

    private boolean isWon()
    {
        boolean returnedValue = false;

        for (int i = 0; i < 9; i++)
            if(tilesList.get(i).getid() == i)
                returnedValue = true;

            else
                return false;

        return returnedValue;
    }

    @FXML
    public void setMainApp(MainApp main) {
        this.main = main;
    }

}

class MoveToAbs extends MoveTo
{
    public MoveToAbs(Node node)
    {
        super(node.getLayoutBounds().getWidth()/2 , node.getLayoutBounds().getHeight()/2 );
    }
}

class LineToAbs extends LineTo
{
    public LineToAbs(Node node, double x, double y)
    {
        super(x - node.getLayoutX() + node.getLayoutBounds().getWidth()/2 , y - node.getLayoutY() + node.getLayoutBounds().getHeight()/2 );
    }
}
