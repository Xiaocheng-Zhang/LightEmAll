import java.awt.Color;
import javalib.worldimages.*;
import java.util.*;

class GamePiece {
  int row;
  int col;
  // whether this GamePiece is connected to the
  // adjacent left, right, top, or bottom pieces
  boolean left;
  boolean right;
  boolean top;
  boolean bottom;
  // whether the power station is on this piece
  boolean powerStation;
  boolean haveLeft;
  boolean haveRight;
  boolean haveTop;
  boolean haveBottom;
  Color color;
  ArrayList<GamePiece> neighbor;
  int colorNum;
  int depth;
  int strength;
  int radius;
  int id;

  GamePiece(int id, int row, int col) {
    this.row = row;
    this.col = col;
    this.left = false;
    this.right = false;
    this.top = false;
    this.bottom = false;
    this.haveRight = false;
    this.haveLeft = false;
    this.haveTop = false;
    this.haveBottom = false;
    this.powerStation = false;
    this.depth = 1;
    this.strength = 0;
    this.radius = 0;
    this.neighbor = new ArrayList<GamePiece>();
    this.id = id;
  }
  
  GamePiece() {
    this.row = 0;
    this.col = 0;
    this.left = false;
    this.right = false;
    this.top = false;
    this.bottom = false;
    this.haveRight = false;
    this.haveLeft = false;
    this.haveTop = false;
    this.haveBottom = false;
    this.powerStation = false;
    this.depth = 1;
    this.strength = 0;
    this.radius = 0;
    this.neighbor = new ArrayList<GamePiece>();
    this.id = 0;
  }


  GamePiece(boolean left, boolean right, boolean top, boolean bottom) {
    this.left = left;
    this.right = right;
    this.top = top;
    this.bottom = bottom;
    this.haveRight = right;
    this.haveLeft = left;
    this.haveTop = top;
    this.haveBottom = bottom;
    this.powerStation = false;
    this.depth = 1;
    this.strength = 0;
    this.radius = 0;
    this.neighbor = new ArrayList<GamePiece>();
    this.id = 0;
  }

  // draw the single GamePiece
  public WorldImage drawGamePiece(int side) {
    // useful graph
    Color c = new Color(128, 128, 128);
    if (this.strength > 0) {
      c = new Color(128, (255 * strength) / radius, (255 * strength) / radius);
    }

    WorldImage horiLf = new RectangleImage(side / 2, side / 20 , OutlineMode.SOLID,
        c).movePinhole(side / 4, 0);
    WorldImage horiRt = new RectangleImage(side / 2, side / 20 , OutlineMode.SOLID,
        c).movePinhole(- side / 4, 0);
    WorldImage vertUp = new RectangleImage(side / 20, side / 2, OutlineMode.SOLID,
        c).movePinhole(0, side / 4);
    WorldImage vertDn = new RectangleImage(side / 20, side / 2, OutlineMode.SOLID,
        c).movePinhole(0, - side / 4);
    WorldImage pieceOutline = new RectangleImage(side, side, 
        OutlineMode.OUTLINE, Color.black);
    WorldImage piece = new OverlayImage(pieceOutline, 
        new RectangleImage(side, side, 
            OutlineMode.SOLID, Color.DARK_GRAY));
    final WorldImage power = new OverlayImage(
        new StarImage(side / 3, 7, OutlineMode.OUTLINE, Color.yellow),
        new StarImage(side / 3, 7, OutlineMode.SOLID, Color.magenta));

    if (this.left) {
      piece = new OverlayImage(horiLf, piece);
    }
    if (this.right) {
      piece = new OverlayImage(horiRt, piece);
    }
    if (this.top) {
      piece = new OverlayImage(vertUp, piece);
    }
    if (this.bottom) {
      piece = new OverlayImage(vertDn, piece);
    }
    if (this.powerStation) {
      piece = new OverlayImage(power, piece);
    }
    return piece;
  }

  // change the strength of piece
  public void changeStrength(int passInRadius) {
    if (passInRadius > this.strength && passInRadius > 0) {
      this.strength = passInRadius;
      for (int i = 0; i < this.neighbor.size(); i++) {
        GamePiece g = this.neighbor.get(i);
        g.neighbor.remove(this);
        g.changeStrength(passInRadius - 1);
      }
    }
  }

  // rotate the piece
  public void rotation(int n) {
    for (int p = 0; p < n; p++) {
      boolean temp = false;
      temp = this.right;
      this.right = this.top;
      this.top = this.left;
      this.left = this.bottom;
      this.bottom = temp;
    }
  }
}

class Edge {
  GamePiece fromNode;
  GamePiece toNode;
  int weight;

  Edge(GamePiece fromNode, GamePiece toNode, int weight) {
    this.fromNode = fromNode;
    this.toNode = toNode;
    this.weight = weight;
  }

}
