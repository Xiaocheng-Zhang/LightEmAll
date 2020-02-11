import tester.*;
import javalib.impworld.*;
import javalib.worldimages.*;
import java.awt.Color;
import java.util.*;

class LightEmAllGame extends World {
  // a list of columns of GamePieces,
  // i.e., represents the board in column-major order
  ArrayList<ArrayList<GamePiece>> board;
  // a list of all nodes
  ArrayList<GamePiece> nodes;
  // a list of edges of the minimum spanning tree
  ArrayList<Edge> mst;
  // the width and height of the board
  int width;
  int height;
  // the current location of the power station,
  // as well as its effective radius
  int powerRow;
  int powerCol;
  int radius;
  int side;
  boolean win;
  int countSteps;
  int hardLevel;
  int tick;
  boolean start;

  LightEmAllGame(int width, int height, int side) {
    this.start = false;
    this.width = width;
    this.height = height;
    this.powerRow = 0;
    this.powerCol = 0;
    this.side = side;
    this.win = false;
    this.board = new ArrayList<ArrayList<GamePiece>>();
    this.nodes = new ArrayList<GamePiece>();
    this.mst = new ArrayList<Edge>();
    this.radius = 0;
    this.countSteps = 0;
    this.tick = 0;
    this.formBoard();
    this.formNode();
    this.formEdge();
    this.sortEdge();
    this.formFinalBoard();
    this.linkTheBoard();
    this.bFS2();
    this.randomRotation();
    this.linkTheBoard();
    this.changeStrength();
  }

  LightEmAllGame(ArrayList<ArrayList<GamePiece>> board) {
    this.width = board.size();
    this.height = board.get(0).size();
    this.powerRow = 0;
    this.powerCol = 0;
    this.side = 100;
    this.board = board;
    this.win = false;
    this.nodes = new ArrayList<GamePiece>();
    this.mst = new ArrayList<Edge>();
    this.radius = 1;
    this.countSteps = 0;
    this.formBoard();
    this.formNode();
    this.formEdge();
    this.sortEdge();
  }

  LightEmAllGame() {
    this.width = 0;
    this.height = 0;
    this.powerRow = 0;
    this.powerCol = 0;
    this.side = 100;
    this.board = new ArrayList<ArrayList<GamePiece>>();
    this.win = false;
    this.nodes = new ArrayList<GamePiece>();
    this.mst = new ArrayList<Edge>();
    this.radius = 1;
    this.countSteps = 0;
    this.start = false;
    this.formBoard();
    this.formNode();
    this.formEdge();
    this.sortEdge();
  }

  // mouse click on the board
  public void onMouseReleased(Posn pos, String buttonName) {
    if (!this.win) {
      this.start = true;
      if (pos.y >= this.side) {
        GamePiece current = this.board.get(pos.x / this.side).get((pos.y - this.side) / this.side);
        // rotate the piece on clockwise
        if (buttonName.equals("LeftButton")) {
          current.rotation(1);
        }
        if (buttonName.equals("RightButton")) {
          // rotate the piece on clockwise
          current.rotation(3);
        }
        this.countSteps++;
        this.linkTheBoard();
        this.changeStrength();
        this.linkTheBoard();
        this.win();
      }
    }
  }

  // on key event handles
  public void onKeyEvent(String key) {
    if (!this.win) { 
      GamePiece temp = this.board.get(this.powerRow).get(this.powerCol);
      if (key.equals("left") && temp.haveLeft) {
        temp.powerStation = false;
        this.powerRow = this.powerRow - 1;
      }
      if (key.equals("right") && temp.haveRight) {
        temp.powerStation = false;
        this.powerRow = this.powerRow + 1;
      }
      if (key.equals("up") && temp.haveTop) {
        // ignore the fake top
        temp.powerStation = false;
        this.powerCol = this.powerCol - 1;
      }
      if (key.equals("down") && temp.haveBottom) {
        // ignore the fake bottom
        temp.powerStation = false;
        this.powerCol = this.powerCol + 1;
      }
      this.changeStrength();
      this.linkTheBoard();
      this.win();
    }
    if (this.win) {
      if (key.equals(" ")) {
        this.powerRow = 0;
        this.powerCol = 0;
        this.win = false;
        this.board = new ArrayList<ArrayList<GamePiece>>();
        this.nodes = new ArrayList<GamePiece>();
        this.mst = new ArrayList<Edge>();
        this.radius = 0;
        this.countSteps = 0;
        this.tick = 0;
        this.start = false;
        this.formBoard();
        this.formNode();
        this.formEdge();
        this.sortEdge();
        this.formFinalBoard();
        this.linkTheBoard();
        this.bFS2();
        this.randomRotation();
        this.linkTheBoard();
        this.changeStrength();
      }
    }
  }

  // draw the scene
  public WorldScene makeScene() {
    WorldScene scene = this.getEmptyScene();
    WorldImage boardOutline = new RectangleImage(this.width * this.side, this.side, 
        OutlineMode.OUTLINE, Color.black);
    WorldImage stepsCount = new TextImage(this.countFlag(this.countSteps),
        this.side / 2, Color.LIGHT_GRAY);
    WorldImage stepsBoard = new RectangleImage(this.side, this.side / 2,
        OutlineMode.SOLID, new Color(41, 36, 33));
    WorldImage stepsPart = new OverlayImage(stepsCount, stepsBoard);
    WorldImage countBoard = new OverlayImage(boardOutline, 
        new RectangleImage(this.width * this.side, this.side, 
            OutlineMode.SOLID, Color.DARK_GRAY));
    WorldImage tickCount = new OverlayImage(this.displayTicks(), stepsBoard);
    WorldImage finalSceneSteps = new TextImage("Steps: " 
        + Integer.toString(this.countSteps), this.side / 3, Color.BLACK);
    WorldImage finalScene = new AboveImage(new TextImage("You Win!!!",
        this.side / 3, Color.yellow), finalSceneSteps);
    scene.placeImageXY(countBoard, this.width * this.side / 2, this.side / 2);
    scene.placeImageXY(stepsPart, this.side * 3 / 4, this.side / 2);
    scene.placeImageXY(tickCount, this.width * this.side - this.side * 3 / 4, this.side / 2);
    scene.placeImageXY(this.drawBoard(), 
        this.width * this.side / 2,
        this.height * this.side / 2 + this.side);
    if (this.hardLevel == 1) {
      scene.placeImageXY(new TextImage("Easy Level", this.side / 3, Color.yellow),
          this.width * this.side / 2, this.side / 2);
    }
    if (this.hardLevel == 2) {
      scene.placeImageXY(new TextImage("Normal Level", this.side / 3, Color.yellow),
          this.width * this.side / 2, this.side / 2);
    }
    if (this.hardLevel == 3) {
      scene.placeImageXY(new TextImage("Hard Level", this.side / 3, Color.yellow),
          this.width * this.side / 2, this.side / 2);
    }
    if (this.win) {
      scene.placeImageXY(finalScene, this.width * this.side / 2, this.side * 3 / 2);
      scene.placeImageXY(new TextImage("Press space to restart", this.side / 3, Color.yellow),
          this.width * this.side / 2, this.height * this.side / 2);
    }
    return scene;
  }

  // count the number of Steps;
  String countFlag(int steps) {
    if (steps >= 100 && steps <= 999) {
      return Integer.toString(steps);
    }
    if (steps < 100 && steps >= 10) {
      return 0 + Integer.toString(steps);
    }
    if (steps < 10 && steps >= 0) {
      return "00" + Integer.toString(steps);
    }
    if (steps > 999) {
      return "999";
    }
    else {
      return "000";
    }
  }

  //count the time
  public void onTick() {
    if (this.start && !this.win) {
      this.tick++; 
    }
  }

  //display the ticks
  WorldImage displayTicks() {
    if (this.tick < 10) {
      return new TextImage("00" + this.tick, this.side / 2, Color.LIGHT_GRAY);
    }
    if (this.tick >= 10 
        && this.tick < 100) {
      return new TextImage("0" + this.tick, this.side / 2, Color.LIGHT_GRAY);
    }
    if (this.tick >= 100 
        && this.tick < 999) {
      return new TextImage(Integer.toString(this.tick), this.side / 2, Color.LIGHT_GRAY);
    }
    if (this.tick >= 999) {
      return new TextImage("999", this.side / 2, Color.LIGHT_GRAY);
    }
    else {
      return null;
    }
  }

  // is win or not
  void win() {
    boolean result = true;
    for (int i = 0; i < this.width; i++) {
      for (int p = 0; p < this.height; p++) {
        if (this.board.get(i).get(p).strength == 0) {
          result = false;
        }
      }
    }
    this.win = result;
  }

  // draw a visible board
  private WorldImage drawBoard() {
    WorldImage boardImage = new EmptyImage();
    for (int i = 0; i < this.width; i++) {
      WorldImage emptyColumn = new EmptyImage();
      for (int p = 0; p < this.height; p++) {
        GamePiece current = this.board.get(i).get(p);
        emptyColumn = new AboveImage(emptyColumn, current.drawGamePiece(this.side));
      }
      boardImage = new BesideImage(boardImage, emptyColumn);
    }
    return boardImage;
  }

  // form a empty board of game
  void formBoard() {
    int q = 0;
    for (int i = 0; i < width; i++) {
      ArrayList<GamePiece> vertical = new ArrayList<GamePiece>();
      for (int p = 0; p < height; p++) {
        vertical.add(new GamePiece(q, i, p));
        q++;
      }
      this.board.add(vertical);
    }
  }

  // form a list of nodes
  void formNode() {
    for (int i = 0; i < this.width; i++) {
      for (int p = 0; p < this.height; p++) {
        GamePiece temp = this.board.get(i).get(p);
        this.nodes.add(temp);
      }
    }
  }

  // form a list of edges
  void formEdge() {
    int j = this.width * this.height;
    for (int i = 0; i < this.width; i++) {
      for (int p = 0; p < this.height; p++) {
        GamePiece temp = this.board.get(i).get(p);
        if (i == 0) {
          if (p == 0) {
            this.mst.add(new Edge(temp, this.board.get(i + 1).get(p),
                new Random().nextInt(j)));
            this.mst.add(new Edge(temp, this.board.get(i).get(p + 1),
                new Random().nextInt(j)));
          }
          if (p == this.height - 1) {
            this.mst.add(new Edge(temp, this.board.get(i + 1).get(p),
                new Random().nextInt(j)));
            this.mst.add(new Edge(temp, this.board.get(i).get(p - 1),
                new Random().nextInt(j)));
          }
          if (p != 0 && p != this.height - 1) {
            this.mst.add(new Edge(temp, this.board.get(i + 1).get(p),
                new Random().nextInt(j)));
            this.mst.add(new Edge(temp, this.board.get(i).get(p - 1),
                new Random().nextInt(j)));
            this.mst.add(new Edge(temp, this.board.get(i).get(p + 1),
                new Random().nextInt(j)));
          }
        }
        if (i == this.width - 1) {
          if (p == 0) {
            this.mst.add(new Edge(temp, this.board.get(i - 1).get(p),
                new Random().nextInt(j)));
            this.mst.add(new Edge(temp, this.board.get(i).get(p + 1),
                new Random().nextInt(j)));
          }
          if (p == this.height - 1) {
            this.mst.add(new Edge(temp, this.board.get(i - 1).get(p),
                new Random().nextInt(j)));
            this.mst.add(new Edge(temp, this.board.get(i).get(p - 1),
                new Random().nextInt(j)));
          }
          if (i != this.width - 1 && p != this.height - 1) {
            this.mst.add(new Edge(temp, this.board.get(i - 1).get(p),
                new Random().nextInt(j)));
            this.mst.add(new Edge(temp, this.board.get(i).get(p - 1),
                new Random().nextInt(j)));
            this.mst.add(new Edge(temp, this.board.get(i).get(p + 1),
                new Random().nextInt(j)));
          }
        }
        if (p == 0 && i != 0 && i != this.width - 1) {
          this.mst.add(new Edge(temp, this.board.get(i - 1).get(p),
              new Random().nextInt(j)));
          this.mst.add(new Edge(temp, this.board.get(i + 1).get(p),
              new Random().nextInt(j)));
          this.mst.add(new Edge(temp, this.board.get(i).get(p + 1),
              new Random().nextInt(j)));
        }
        if (p == this.height - 1 
            && i != 0 && i != this.width - 1) {
          this.mst.add(new Edge(temp, this.board.get(i - 1).get(p),
              new Random().nextInt(j)));
          this.mst.add(new Edge(temp, this.board.get(i + 1).get(p),
              new Random().nextInt(j)));
          this.mst.add(new Edge(temp, this.board.get(i).get(p - 1),
              new Random().nextInt(j)));
        }
        if (p != 0 && p != this.height - 1 
            && i != 0 && i != this.width - 1) {
          this.mst.add(new Edge(temp, this.board.get(i - 1).get(p),
              new Random().nextInt(j)));
          this.mst.add(new Edge(temp, this.board.get(i + 1).get(p),
              new Random().nextInt(j)));
          this.mst.add(new Edge(temp, this.board.get(i).get(p + 1),
              new Random().nextInt(j)));
          this.mst.add(new Edge(temp, this.board.get(i).get(p - 1),
              new Random().nextInt(j)));
        }
      }
    }
  }

  // sort the edge
  void sortEdge() {
    this.mst.sort(new SmallerThan());
  }

  // kruskal method
  ArrayList<Edge> kruskal() {
    ArrayList<Edge> spanningTree = new ArrayList<Edge>();
    Map<Integer, Integer> unionFind = new HashMap<Integer, Integer>();
    for (int i = 0; i < this.nodes.size(); i++) {
      Integer v = this.nodes.get(i).id;
      unionFind.put(v, v);
    }
    while (spanningTree.size() < this.nodes.size() - 1) {
      // find the shortest edge that does not create a cycle
      Edge edge = this.mst.remove(0);
      Integer v1 = edge.fromNode.id;
      Integer v2 = edge.toNode.id;
      Integer v1Rep = find(unionFind, v1);
      Integer v2Rep = find(unionFind, v2);
      if (v1Rep == v2Rep) {
        continue;
      } 
      else {
        union(unionFind, v1, v2);
        spanningTree.add(edge);
      }
    }
    return spanningTree;
  }

  // find method for kruskal
  private static Integer find(Map<Integer, Integer> map, Integer from) {
    Integer fromRep = map.get(from);
    if (from.equals(fromRep)) {
      return fromRep;
    } else {
      return find(map, fromRep);
    }
  }

  // union method for kruskal
  private static void union(Map<Integer, Integer> map, Integer from, Integer to) {
    map.replace(find(map, from), find(map, to));
  }

  // randomRotation 
  void randomRotation() {
    for (int i = 0; i < this.width; i++) {
      for (int p = 0; p < this.height; p++) {
        GamePiece temp = this.board.get(i).get(p);
        temp.rotation(new Random().nextInt(4));
      }
    }
  }

  // form the final board
  void formFinalBoard() {
    ArrayList<Edge> tree = this.kruskal();
    for (int i = 0; i < tree.size(); i++) {
      Edge temp = tree.get(i);
      if (temp.fromNode.row == temp.toNode.row) {
        if (temp.fromNode.col < temp.toNode.col) {
          temp.fromNode.bottom = true;
          temp.fromNode.haveBottom = true;
          temp.toNode.top = true;
          temp.toNode.haveTop = true;
        }
        else {
          temp.fromNode.top = true;
          temp.fromNode.haveTop = true;
          temp.toNode.bottom = true;
          temp.toNode.haveBottom = true;
        }
      }
      if (temp.fromNode.col == temp.toNode.col) {
        if (temp.fromNode.row < temp.toNode.row) {
          temp.fromNode.right = true;
          temp.fromNode.haveRight = true;
          temp.toNode.left = true;
          temp.toNode.haveLeft = true;
        }
        else {
          temp.fromNode.left = true;
          temp.fromNode.haveLeft = true;
          temp.toNode.right = true;
          temp.toNode.haveRight = true;
        }
      }
    }
  }

  // link the board by edge according to the GamePiece
  void linkTheBoard() {
    this.board.get(powerRow).get(powerCol).powerStation = true;
    for (int j = 0; j < width; j++) {
      for (int i = 0; i < height; i++) {
        int left = j - 1;
        int right = j + 1;
        int up = i - 1;
        int down = i + 1;        

        GamePiece g = board.get(j).get(i);
        g.radius = this.radius;
        g.neighbor = new ArrayList<GamePiece>();
        g.haveRight = false;
        g.haveLeft = false;
        g.haveTop = false;
        g.haveBottom = false;

        if (right < width) {
          GamePiece gRight = board.get(right).get(i);
          if (g.right && gRight.left) {
            g.neighbor.add(board.get(right).get(i));
            g.haveRight = true;
          }
        }
        if (left >= 0) {
          GamePiece gLeft = board.get(left).get(i);
          if (g.left && gLeft.right) {
            g.neighbor.add(board.get(left).get(i));
            g.haveLeft = true;
          }
        }
        if (down < height) {
          GamePiece gDown = board.get(j).get(down);
          if (g.bottom && gDown.top) {
            g.neighbor.add(board.get(j).get(down));
            g.haveBottom = true;
          }
        }
        if (up >= 0) {
          GamePiece gUp = board.get(j).get(up);
          if (g.top && gUp.bottom) {
            g.neighbor.add(board.get(j).get(up));
            g.haveTop = true;
          }
        }
      }
    }
  }

  // find the farthest piece
  GamePiece bFS1() {
    Queue<GamePiece> q = new LinkedList<>();
    GamePiece start = this.board.get(this.width / 2).get(this.height / 2);
    ArrayList<GamePiece> exist = new ArrayList<GamePiece>();
    q.add(start);
    GamePiece finalPiece = new GamePiece();
    while (!q.isEmpty()) {
      GamePiece current = q.poll();
      for (int i = 0; i < current.neighbor.size(); i++) {
        if (!exist.contains(current.neighbor.get(i))) {
          q.add(current.neighbor.get(i));
          exist.add(current);
        }
      }
      finalPiece = current;
    }
    return finalPiece;
  }

  // sort back
  void bFS2() {
    Queue<GamePiece> q = new LinkedList<>();
    GamePiece start = this.bFS1();
    ArrayList<GamePiece> exist = new ArrayList<GamePiece>();
    q.add(start);
    int p = 0;
    while (!q.isEmpty()) {
      GamePiece current = q.poll();
      for (int i = 0; i < current.neighbor.size(); i++) {
        if (!exist.contains(current.neighbor.get(i))) {
          current.neighbor.get(i).depth = current.depth + 1;
          q.add(current.neighbor.get(i));
          exist.add(current);
        }
      }
      p = current.depth;
    }
    radius = p / 2 + 1;
  }

  // change color strength by radius
  void changeStrength() {
    for (int i = 0; i < this.width; i++) {
      for (int p = 0; p < this.height; p++) {
        this.board.get(i).get(p).strength = 0;
      }
    }
    GamePiece powerStation = board.get(powerRow).get(powerCol);
    powerStation.changeStrength(this.radius);
  }
}

// function object smaller
class SmallerThan implements Comparator<Edge> {

  public int compare(Edge arg0, Edge arg1) {
    return arg0.weight - arg1.weight;
  }
}

class ExampleLightEmAll {
  GamePiece g1;
  GamePiece g2;
  GamePiece g3;
  GamePiece g4;

  GamePiece gt1;
  GamePiece gt2;
  GamePiece gt3;
  GamePiece gt4;

  ArrayList<GamePiece> a1;
  ArrayList<GamePiece> a2;

  ArrayList<GamePiece> at1;
  ArrayList<GamePiece> at2;

  LightEmAllGame l1;
  LightEmAllGame lt;

  static int width;
  static int height;
  static int side;
  static LightEmAllGame model;

  void initData() {
    width = 7;
    height = 7;
    side = 100;
    model = new LightEmAllGame(width, height, side);

    g1 = new GamePiece(false, false, false, false);
    g2 = new GamePiece(true, false, false, false);
    g3 = new GamePiece(false, true, false, false);
    g4 = new GamePiece(false, false, true, false);

    gt1 = new GamePiece(false, true, false, false);
    gt2 = new GamePiece(true, false, false, true);
    gt3 = new GamePiece(false, true, false, false);
    gt4 = new GamePiece(true, false, true, false);

    a1 = new ArrayList<GamePiece>(Arrays.asList(g1, g2));
    a2 = new ArrayList<GamePiece>(Arrays.asList(g3, g4));

    at1 = new ArrayList<GamePiece>(Arrays.asList(gt1, gt3));
    at2 = new ArrayList<GamePiece>(Arrays.asList(gt2, gt4));

    l1 = new LightEmAllGame(new ArrayList<ArrayList<GamePiece>>(Arrays.asList(a1, a2)));
    lt = new LightEmAllGame(new ArrayList<ArrayList<GamePiece>>(Arrays.asList(at1, at2)));
  }

  // run the game
  void testRunGame(Tester t) {
    initData();
    model.bigBang(width * side, height * side + side, 1);
  }

  public static void main(String[] args) {
    width = 7;
    height = 7;
    side = 100;
    model = new LightEmAllGame(width, height, side);
    model.bigBang(width * side, height * side + side, 1);
  }


  // test bfs1
  void testBFS2(Tester t) {
    this.initData();
    gt1.neighbor.add(gt2);
    gt2.neighbor.add(gt1);
    gt2.neighbor.add(gt4);
    gt4.neighbor.add(gt2);
    gt4.neighbor.add(gt3);
    gt3.neighbor.add(gt4);
    lt.bFS2();
    t.checkExpect(lt.radius, 3);
  }

  // test on mouse released
  void testonMouseReleased(Tester t) {
    this.initData();
    t.checkExpect(g1.left, false);
    t.checkExpect(g1.right, false);
    t.checkExpect(g1.top, false);
    t.checkExpect(g1.bottom, false);
    t.checkExpect(g2.left, true);
    t.checkExpect(g2.right, false);
    t.checkExpect(g2.top, false);
    t.checkExpect(g2.bottom, false);
    t.checkExpect(g3.left, false);
    t.checkExpect(g3.right, true);
    t.checkExpect(g3.top, false);
    t.checkExpect(g3.bottom, false);
    t.checkExpect(g4.left, false);
    t.checkExpect(g4.right, false);
    t.checkExpect(g4.top, true);
    t.checkExpect(g4.bottom, false);

    l1.onMouseReleased(new Posn(150, 250), "LeftButton");

    t.checkExpect(g1.left, false);
    t.checkExpect(g1.right, false);
    t.checkExpect(g1.top, false);
    t.checkExpect(g1.bottom, false);
    t.checkExpect(g2.left, true);
    t.checkExpect(g2.right, false);
    t.checkExpect(g2.top, false);
    t.checkExpect(g2.bottom, false);
    t.checkExpect(g3.left, false);
    t.checkExpect(g3.right, true);
    t.checkExpect(g3.top, false);
    t.checkExpect(g3.bottom, false);
    t.checkExpect(g4.left, false);
    t.checkExpect(g4.right, true);
    t.checkExpect(g4.top, false);
    t.checkExpect(g4.bottom, false);
  }

  // test on key event
  void testOnKeyEvent(Tester t) {
    this.initData();
    t.checkExpect(lt.powerRow, 0);
    t.checkExpect(lt.powerCol, 0);
    lt.onKeyEvent("right");
    t.checkExpect(lt.powerRow, 1);
    t.checkExpect(lt.powerCol, 0);
    lt.onKeyEvent("down");
    t.checkExpect(lt.powerRow, 1);
    t.checkExpect(lt.powerCol, 1);
    lt.onKeyEvent("left");
    t.checkExpect(lt.powerRow, 0);
    t.checkExpect(lt.powerCol, 1);
    lt.onKeyEvent("right");
    t.checkExpect(lt.powerRow, 1);
    t.checkExpect(lt.powerCol, 1);
    lt.onKeyEvent("down");
    t.checkExpect(lt.powerRow, 1);
    t.checkExpect(lt.powerCol, 1);
  }

  // test form the board
  void testFormBoard(Tester t) {
    LightEmAllGame l1 = new LightEmAllGame();
    t.checkExpect(l1.board.isEmpty(), true);
    l1.width = 2; 
    l1.height = 2;
    l1.formBoard();
    t.checkExpect(l1.board.isEmpty(), false);
    t.checkExpect(l1.board.get(0).get(0).id, 0);
    t.checkExpect(l1.board.get(0).get(1).id, 1);
    t.checkExpect(l1.board.get(1).get(0).id, 2);
    t.checkExpect(l1.board.get(1).get(1).id, 3);
  }

  // test kruskal
  void testKruskal(Tester t) {
    LightEmAllGame l1 = new LightEmAllGame();
    t.checkExpect(l1.mst.isEmpty(), true);
    t.checkExpect(l1.kruskal().isEmpty(), true);
    l1.width = 7;
    l1.height = 7;
    l1.formBoard(); 
    l1.formNode();
    l1.formEdge(); 
    l1.sortEdge();
    t.checkExpect(l1.mst.isEmpty(), false);
    t.checkExpect(l1.kruskal().size(), 48);
    LightEmAllGame l2 = new LightEmAllGame();
    t.checkExpect(l2.mst.isEmpty(), true);
    t.checkExpect(l2.kruskal().isEmpty(), true);
    l2.width = 5;
    l2.height = 5;
    l2.formBoard(); 
    l2.formNode();
    l2.formEdge(); 
    l2.sortEdge();
    t.checkExpect(l2.mst.isEmpty(), false);
    t.checkExpect(l2.kruskal().size(), 24);
  }

  // test form the node
  void testFormNode(Tester t) {
    LightEmAllGame l1 = new LightEmAllGame();
    t.checkExpect(l1.nodes.isEmpty(), true);
    l1.width = 7;
    l1.height = 7;
    l1.formBoard(); 
    l1.formNode();
    t.checkExpect(l1.nodes.size(), 49);
    l1.nodes.clear();
    l1.width = 2;
    l1.height = 2;
    l1.formBoard(); 
    l1.formNode();
    t.checkExpect(l1.nodes.size(), 4);
  }

  // test win checker
  void testWin(Tester t) {
    this.initData();
    t.checkExpect(lt.win, false);
    lt.win();
    t.checkExpect(lt.win, false);
    gt1.strength = 1;
    gt2.strength = 1;
    gt3.strength = 1;
    gt4.strength = 1;
    lt.win();
    t.checkExpect(lt.win, true);
  }

  // test form an edge
  void testFormEdge(Tester t) {
    LightEmAllGame l1 = new LightEmAllGame();
    t.checkExpect(l1.mst.isEmpty(), true);
    l1.width = 2;
    l1.height = 2;
    l1.formBoard(); 
    l1.formNode();
    l1.formEdge();
    t.checkExpect(l1.mst.size(), 8);
  }

  // test function object smallerThan
  void testSmallerThan(Tester t) {
    SmallerThan f = new SmallerThan();
    Edge e1 = new Edge(null, null, 1);
    Edge e2 = new Edge(null, null, 2);
    t.checkExpect(f.compare(e1, e2), -1);
    e1.weight = 0;
    t.checkExpect(f.compare(e1, e2), -2);
    e2.weight = 0;  
    t.checkExpect(f.compare(e1, e2), 0);
  }
}
