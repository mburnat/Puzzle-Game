package puzzle.model;

import javafx.scene.shape.*;
import java.awt.image.BufferedImage;

public class Tile extends Rectangle
{
    private BufferedImage part;
    private int id;

    public BufferedImage getPart()
    {
        return part;
    }

    public void setPart(BufferedImage part)
    {
        this.part = part;
    }

    public int getid()
    {
        return id;
    }

    public void setNumber(int number)
    {
        this.id = number;
    }

    public Tile(int width, int height, BufferedImage part, int number)
    {
        super(width,height);
        this.part = part;
        this.id = number;
    }

    public Tile(BufferedImage part, int id)
    {
        this.part = part;
        this.id = id;
    }
}
