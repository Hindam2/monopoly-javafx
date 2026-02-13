package javafxapplication1;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class JavaFXApplication1 extends Application {
    private static final int GRID_DIM = 11;
    private static final int CORNER_SIZE = 97;
    private static final int SIDE_LENGTH_SHORT = 64;
    private static final int SIDE_LENGTH_LONG = 119;

    private Stage stage;
    private Scene loginScene;
    private Scene gameScene;

    private Spinner<Integer> playerCountSpinner;
    private final List<TextField> nameFields = new ArrayList<>();
    private final List<ColorPicker> colorPickers = new ArrayList<>();

    private Label info;
    private GridPane boardGrid;

    private VBox leftPane;
    private VBox playersStatusBox;
    private TextArea ownershipArea;
    private Label jailLabel;
    private Label cardLabel;
    private Button rollBtn;
    private HBox diceBox;
    private Label logTitle;
    private TextArea gameLog;

    private Button manageBuildingsBtn;

    private final Map<Player, Label> balanceLabels = new HashMap<>();
    private final Map<Player, HBox> playerRows = new HashMap<>();

    private LogWriter logWriter;
    private Path logFile;
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
    private int turnCounter = 1;

    private Game game;
    private final Map<Player, Circle> tokens = new HashMap<>();
    private final Map<Integer, StackPane> boardCells = new HashMap<>();
    private final Map<Integer, Tooltip> cellTooltips = new HashMap<>();
    private final Map<Integer, Node> ownerBadges = new HashMap<>();
    private DiceView diceView;

    private final DropShadow glow = new DropShadow(12, Color.GOLD);

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Monopoly – Classic Board GUI (Houses/Hotel + Binary Save)");
        buildLoginScene();
        stage.setScene(loginScene);
        stage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (logWriter != null) {
            logWriter.close();
        }
    }

    public static void main(String[] args) { launch(args); }

    private void buildLoginScene() {
        Label title = new Label("Monopoly – Setup");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        playerCountSpinner = new Spinner<>(2, 4, 2);

        VBox playersBoxContainer = new VBox(6);
        rebuildPlayerInputs(playerCountSpinner.getValue());
        playersBoxContainer.getChildren().setAll(buildPlayersGrid());

        playerCountSpinner.valueProperty().addListener((obs, o, n) -> {
            rebuildPlayerInputs(n);
            playersBoxContainer.getChildren().setAll(buildPlayersGrid());
        });

        Button startBtn = new Button("Start Game");
        startBtn.setDefaultButton(true);
        startBtn.setOnAction(e -> {
            if (validateNames()) {
                List<Player> players = new ArrayList<>();
                for (int i = 0; i < playerCountSpinner.getValue(); i++) {
                    String name = nameFields.get(i).getText().trim();
                    Color color = colorPickers.get(i).getValue();
                    players.add(new Player(name.isEmpty() ? "P" + (i + 1) : name, color));
                }
                startGame(players);
            }
        });

        Button importBtn = new Button("Import Saved Game (Binary)");
        importBtn.setOnAction(e -> importSavedGameFromLogin());

        VBox root = new VBox(12, title,
                new HBox(8, new Label("Players:"), playerCountSpinner),
                playersBoxContainer,
                new HBox(10, startBtn, importBtn));
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20));
        root.setPrefWidth(700);
        loginScene = new Scene(root);
    }

    private void rebuildPlayerInputs(int count) {
        nameFields.clear();
        colorPickers.clear();
        for (int i = 0; i < count; i++) {
            TextField tf = new TextField();
            tf.setPromptText("Player " + (i + 1) + " name");
            nameFields.add(tf);

            ColorPicker cp = new ColorPicker(defaultColor(i));
            colorPickers.add(cp);
        }
    }

    private Color defaultColor(int i) {
        return switch (i) {
            case 0 -> Color.RED;
            case 1 -> Color.BLUE;
            case 2 -> Color.GREEN;
            case 3 -> Color.PURPLE;
            default -> Color.BLACK;
        };
    }

    private GridPane buildPlayersGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(6);
        for (int i = 0; i < nameFields.size(); i++) {
            grid.add(new Label("Player " + (i + 1) + ":"), 0, i);
            grid.add(nameFields.get(i), 1, i);
            grid.add(new Label("Token color:"), 2, i);
            grid.add(colorPickers.get(i), 3, i);
        }
        return grid;
    }

    private boolean validateNames() {
        Set<String> names = new HashSet<>();
        for (TextField tf : nameFields) {
            String n = tf.getText().trim();
            if (n.isEmpty()) continue;
            if (!names.add(n)) {
                showAlert("Duplicate names", "Each player must have a unique name.");
                return false;
            }
        }
        return true;
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setHeaderText(title);
        a.initOwner(stage);
        a.initModality(Modality.APPLICATION_MODAL);
        a.showAndWait();
    }

    private void startGame(List<Player> players) {
        game = new Game(players);

        info = new Label(players.get(0).getName() + " starts");
        info.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        ownershipArea = new TextArea();
        ownershipArea.setEditable(false);
        ownershipArea.setPrefRowCount(10);
        jailLabel = new Label();
        cardLabel = new Label();

        diceView = new DiceView();
        diceBox = new HBox(10, diceView.die1(), diceView.die2());
        diceBox.setAlignment(Pos.CENTER_LEFT);
        diceBox.setMouseTransparent(false);

        boardGrid = new GridPane();
        boardGrid.setGridLinesVisible(false);
        boardGrid.setAlignment(Pos.CENTER);
        boardGrid.setPadding(new Insets(10));
        boardGrid.setStyle("-fx-background-color: #f5f5dc; -fx-border-color: black; -fx-border-width: 6;");

        for (int i = 0; i < GRID_DIM; i++) {
            ColumnConstraints cc = new ColumnConstraints(
                    (i == 0 || i == GRID_DIM - 1) ? CORNER_SIZE : SIDE_LENGTH_SHORT
            );
            boardGrid.getColumnConstraints().add(cc);
        }
        for (int i = 0; i < GRID_DIM; i++) {
            RowConstraints rc = new RowConstraints(
                    (i == 0 || i == GRID_DIM - 1) ? CORNER_SIZE : SIDE_LENGTH_SHORT
            );
            boardGrid.getRowConstraints().add(rc);
        }

        drawClassicBoardRing();
        drawPlayers();

        rollBtn = new Button("Roll Dice");
        rollBtn.setDefaultButton(true);
        rollBtn.setOnAction(e -> playTurn());

        manageBuildingsBtn = new Button("Manage Buildings");
        manageBuildingsBtn.setOnAction(e -> showManageBuildingsDialog(game.getCurrentPlayer()));

        gameLog = new TextArea();
        gameLog.setEditable(false);
        gameLog.setPrefRowCount(10);
        gameLog.setWrapText(true);
        gameLog.setPromptText("Game Log");

        logTitle = new Label("Game Log");
        logTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Button btnChangeLog = new Button("Change Text Log File");
        btnChangeLog.setOnAction(e -> changeLogFile());
        Button btnExportSave = new Button("Export Save (Binary)");
        btnExportSave.setOnAction(e -> exportBinarySave());
        Button btnOpenLog = new Button("Open Text Log");
        btnOpenLog.setOnAction(e -> openLogFileIntoDialog());
        HBox logControls = new HBox(10, btnChangeLog, btnExportSave, btnOpenLog);
        logControls.setAlignment(Pos.CENTER_LEFT);

        leftPane = buildLeftPane(players, logControls);

        BorderPane root = new BorderPane();
        root.setTop(new VBox(6, info));
        BorderPane.setAlignment(root.getTop(), Pos.CENTER);
        root.setCenter(boardGrid);
        root.setLeft(leftPane);
        BorderPane.setMargin(leftPane, new Insets(8));

        gameScene = new Scene(root, 1400, 900);
        stage.setScene(gameScene);

        try {
            logFile = Paths.get(System.getProperty("user.home"), "MonopolyGameLog.txt");
            logWriter = new LogWriter(logFile);
            appendLog("[INIT] Writing log to: " + logFile.toAbsolutePath());
        } catch (IOException ex) {
            showAlert("Log Writer Error", "Could not open default log file: " + ex.getMessage());
        }

        refreshSidebar();
        logGlobal("Game started with " + players.size() + " player(s).");
    }

    private VBox buildLeftPane(List<Player> players, Node logControls) {
        Label sbTitle = new Label("Game Status");
        sbTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        playersStatusBox = new VBox(8);
        playersStatusBox.getChildren().add(new Label("Players:"));
        for (Player p : players) {
            Rectangle swatch = new Rectangle(16, 16, p.getColor());
            swatch.setStroke(Color.BLACK);
            Label nameLbl = new Label(p.getName());
            nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
            Label balLbl = new Label("$" + p.getBalance());
            balLbl.setStyle("-fx-font-size: 12px;");
            balanceLabels.put(p, balLbl);

            HBox row = new HBox(10, swatch, nameLbl, balLbl);
            row.setAlignment(Pos.CENTER_LEFT);
            playerRows.put(p, row);
            playersStatusBox.getChildren().add(row);
        }

        ownershipArea.setPrefWidth(360);
        jailLabel.setStyle("-fx-font-size: 12px;");
        cardLabel.setStyle("-fx-font-size: 12px;");

        VBox sb = new VBox(10,
                sbTitle,
                new Separator(),
                playersStatusBox,
                new Separator(),
                new Label("Ownership:"),
                ownershipArea,
                manageBuildingsBtn,
                new Separator(),
                new Label("Jail / Cards:"),
                jailLabel,
                cardLabel,
                new Separator(),
                rollBtn,
                diceBox,
                new Separator(),
                logTitle,
                gameLog,
                logControls
        );
        sb.setPadding(new Insets(8));
        sb.setPrefWidth(450);
        return sb;
    }

    private void drawClassicBoardRing() {
        boardGrid.getChildren().clear();
        boardCells.clear();
        ownerBadges.clear();
        cellTooltips.clear();

        StackPane centerPane = new StackPane();
        centerPane.setBackground(new Background(new BackgroundFill(Color.BEIGE, null, null)));

        Label monopolyLabel = new Label("MONOPOLY");
        monopolyLabel.setFont(Font.font("Arial Black", FontWeight.BOLD, 52));
        monopolyLabel.setTextFill(Color.DARKRED);
        monopolyLabel.setEffect(new javafx.scene.effect.DropShadow(5, Color.GRAY));
        StackPane.setAlignment(monopolyLabel, Pos.CENTER);
        centerPane.getChildren().add(monopolyLabel);

        boardGrid.add(centerPane, 1, 1, GRID_DIM - 2, GRID_DIM - 2);

        int N = game.getBoard().size();
        for (int idx = 0; idx < N; idx++) {
            int[] rc = indexToGrid(idx);
            int col = rc[0], row = rc[1];

            Space s = game.getSpace(idx);

            StackPane cell = new StackPane();
            cell.setStyle("-fx-border-color: black; -fx-border-width: 2;");

            boolean corner = isCorner(row, col);
            boolean topOrBottom = isTopOrBottom(row);

            if (corner) {
                cell.setPrefSize(CORNER_SIZE, CORNER_SIZE);
                styleCorner(cell, s);
            } else {
                if (topOrBottom) cell.setPrefSize(SIDE_LENGTH_SHORT, SIDE_LENGTH_LONG);
                else cell.setPrefSize(SIDE_LENGTH_LONG, SIDE_LENGTH_SHORT);
                styleSideCell(cell, s, topOrBottom);
            }

            if (s instanceof Ownable) {
                Circle badge = new Circle(7);
                badge.setFill(Color.TRANSPARENT);
                badge.setStroke(Color.GRAY);
                StackPane.setAlignment(badge, Pos.BOTTOM_RIGHT);
                StackPane.setMargin(badge, new Insets(0, 4, 4, 0));
                cell.getChildren().add(badge);
                ownerBadges.put(idx, badge);
            }

            Tooltip tp = new Tooltip(spaceTooltipText(s));
            Tooltip.install(cell, tp);
            cellTooltips.put(idx, tp);

            boardGrid.add(cell, col, row);
            boardCells.put(idx, cell);
        }
    }

    private void styleCorner(StackPane cell, Space s) {
        cell.setBackground(new Background(new BackgroundFill(Color.LIGHTYELLOW, null, null)));
        Label label = new Label(s.getName());
        label.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        label.setTextFill(Color.DARKRED);
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setPadding(new Insets(8));
        label.setMaxWidth(CORNER_SIZE - 12);
        cell.getChildren().add(label);
    }

    private void styleSideCell(StackPane cell, Space s, boolean topOrBottom) {
        cell.setStyle("-fx-border-color: black; " + gradientForType(s.getType()));
        String nameText = s.getName();
        Label propLabel = new Label(nameText);
        propLabel.setWrapText(true);
        propLabel.setTextAlignment(TextAlignment.CENTER);
        propLabel.setTextFill(Color.BLACK);
        propLabel.setFont(Font.font("Arial", nameText.length() > 24 ? 11 : 13));

        if (s.getType() == SpaceType.PROPERTY) {
            Color band = groupColor((Property) s);
            if (topOrBottom) {
                VBox vbox = new VBox(0);
                vbox.setAlignment(Pos.TOP_CENTER);
                Rectangle colorBar = new Rectangle(cell.getPrefWidth(), 18, band);
                colorBar.setStroke(Color.BLACK);
                colorBar.setStrokeWidth(1);
                propLabel.setAlignment(Pos.CENTER);
                propLabel.setMaxWidth(cell.getPrefWidth() - 8);
                propLabel.setPadding(new Insets(6, 4, 4, 4));
                vbox.getChildren().addAll(colorBar, propLabel);
                cell.getChildren().add(vbox);
            } else {
                HBox hbox = new HBox(0);
                hbox.setAlignment(Pos.CENTER_LEFT);
                Rectangle colorBar = new Rectangle(16, cell.getPrefHeight(), band);
                colorBar.setStroke(Color.BLACK);
                colorBar.setStrokeWidth(1);
                propLabel.setPrefWidth(cell.getPrefHeight() - 24);
                propLabel.setMaxWidth(cell.getPrefHeight() - 24);
                propLabel.setAlignment(Pos.CENTER);
                propLabel.setRotate(-90);
                hbox.getChildren().addAll(colorBar, propLabel);
                cell.getChildren().add(hbox);
            }
        } else {
            if (topOrBottom) {
                StackPane.setAlignment(propLabel, Pos.TOP_CENTER);
                StackPane.setMargin(propLabel, new Insets(6, 6, 4, 6));
                propLabel.setMaxWidth(cell.getPrefWidth() - 12);
                cell.getChildren().add(propLabel);
            } else {
                propLabel.setRotate(-90);
                propLabel.setMaxWidth(cell.getPrefHeight() - 12);
                StackPane.setAlignment(propLabel, Pos.CENTER);
                cell.getChildren().add(propLabel);
            }
        }
    }

    private boolean isCorner(int row, int col) {
        return (row == 0 && col == 0)
                || (row == 0 && col == GRID_DIM - 1)
                || (row == GRID_DIM - 1 && col == 0)
                || (row == GRID_DIM - 1 && col == GRID_DIM - 1);
    }

    private boolean isTopOrBottom(int row) { return row == 0 || row == GRID_DIM - 1; }

    private void drawPlayers() {
        tokens.clear();
        int i = 0;
        for (Player p : game.getPlayers()) {
            Circle token = new Circle(10, p.getColor());
            token.setTranslateX((i % 2 == 0) ? -12 : 12);
            token.setTranslateY((i < 2) ? 10 : -10);
            tokens.put(p, token);
            placeToken(p);
            i++;
        }
    }

    private void placeToken(Player p) {
        int pos = p.getPosition();
        StackPane cell = boardCells.get(pos);
        Circle token = tokens.get(p);
        if (token.getParent() instanceof StackPane oldCell) {
            oldCell.getChildren().remove(token);
        }
        if (cell != null && !cell.getChildren().contains(token)) {
            cell.getChildren().add(token);
            StackPane.setAlignment(token, Pos.CENTER);
        }
        applyHighlightForPosition(pos, true);
    }

    private void applyHighlightForPosition(int idx, boolean highlight) {
        StackPane cell = boardCells.get(idx);
        if (cell != null) {
            cell.setEffect(highlight ? glow : null);
        }
    }

    private void playTurn() {
        Player current = game.getCurrentPlayer();

        if (current.isInJail()) {
            int passesSince = game.getTotalGoPasses() - current.getGoPassCounterAtJail();
            if (passesSince >= (game.getPlayers().size() - 1)) {
                current.setInJail(false);
                current.resetJailTurns();
                info.setText(current.getName() + " automatically released (all players passed GO).");
                log(current, "Auto-release: all players passed GO");
                refreshSidebar();

                int[] diceAuto = game.rollDice();
                diceView.show(diceAuto[0], diceAuto[1]);
                logTurnHeader(current, diceAuto);
                proceedMovement(current, diceAuto[0] + diceAuto[1], new StringBuilder());
                return;
            }

            boolean canPay = current.getBalance() >= 300;
            boolean hasCard = current.hasGetOutOfJailFree();

            List<ButtonType> options = new ArrayList<>();
            ButtonType btnStay = new ButtonType("Stay in Jail", ButtonBar.ButtonData.CANCEL_CLOSE);
            if (canPay) options.add(new ButtonType("Pay $300", ButtonBar.ButtonData.OK_DONE));
            if (hasCard) options.add(new ButtonType("Use Get Out Of Jail Free", ButtonBar.ButtonData.APPLY));
            options.add(btnStay);

            Alert jailChoices = new Alert(Alert.AlertType.CONFIRMATION);
            jailChoices.initOwner(stage);
            jailChoices.initModality(Modality.APPLICATION_MODAL);
            jailChoices.setTitle("Jail Options");
            jailChoices.setHeaderText(current.getName() + " is in Jail");

            if (canPay && hasCard) {
                jailChoices.setContentText("Choose to Pay $300, Use GOJF, or Stay in Jail.");
            } else if (canPay) {
                jailChoices.setContentText("Choose to Pay $300 or Stay in Jail.");
            } else if (hasCard) {
                jailChoices.setContentText("Choose to Use GOJF or Stay in Jail.");
            } else {
                jailChoices.setContentText("No options available; must Stay in Jail.");
            }

            jailChoices.getButtonTypes().setAll(options);

            if (rollBtn != null) rollBtn.setDisable(true);
            Optional<ButtonType> res = jailChoices.showAndWait();
            if (rollBtn != null) rollBtn.setDisable(false);

            if (res.isPresent() && res.get() != btnStay) {
                if (res.get().getText().contains("Pay $300")) {
                    current.subtractBalance(300);
                    showCashDelta(current, -300);
                    log(current, "Paid $300 to leave Jail");
                } else if (res.get().getText().contains("Use Get Out Of Jail Free")) {
                    current.useGetOutOfJailFree();
                    log(current, "Used Get Out Of Jail Free");
                }
                current.setInJail(false);
                current.resetJailTurns();
                refreshSidebar();
                int[] dice = game.rollDice();
                diceView.show(dice[0], dice[1]);
                logTurnHeader(current, dice);
                proceedMovement(current, dice[0] + dice[1], new StringBuilder());
            } else {
                current.incrementJailTurns();
                info.setText(current.getName() + " stays in Jail (" + current.getJailTurns() + ")");
                log(current, "Stayed in Jail (" + current.getJailTurns() + ")");
                refreshSidebar();
                game.advanceTurn();
                turnCounter++;
            }
            return;
        }

        int[] dice = game.rollDice();
        diceView.show(dice[0], dice[1]);
        logTurnHeader(current, dice);
        StringBuilder msg = new StringBuilder(current.getName() + " rolled " + dice[0] + " + " + dice[1] + " → ");
        proceedMovement(current, dice[0] + dice[1], msg);
    }

    private void proceedMovement(Player p, int steps, StringBuilder msg) {
        TurnPath path = game.computePath(p, steps);
        int N = game.getBoard().size();
        for (int i = 0; i < N; i++) applyHighlightForPosition(i, false);
        animatePath(p, path.positions(), () -> Platform.runLater(() -> resolveLanding(p, msg)));
    }

    private void resolveLanding(Player p, StringBuilder msg) {
        Space s = game.getSpace(p.getPosition());

        if (s instanceof Ownable ownable && ownable.getOwner() == null && p.getBalance() >= ownable.getPrice()) {
            showPurchaseDialog(p, ownable, s.getName(), ownable.getPrice(), resultText -> {
                info.setText(s.getName() + ": " + resultText);
                log(p, "Landed on " + s.getName() + " → " + resultText);
                updateOwnershipBadge(p.getPosition());
                refreshSidebar();

                if (s instanceof Property prop) {
                    if (game.getBoard().playerOwnsFullSet(p, prop.getColorGroup())) {
                        promptBuildForColorGroup(p, prop.getColorGroup());
                    }
                }
                if (!p.isInJail()) {
                    game.advanceTurn();
                    turnCounter++;
                }
            });
            return;
        }

        int before = p.getBalance();
        String action = s.onLand(game, p);
        int after = p.getBalance();
        int delta = after - before;
        if (delta != 0) showCashDelta(p, delta);

        msg.append(s.getName()).append(": ").append(action);
        info.setText(msg.toString());
        log(p, "Landed on " + s.getName() + " → " + action);
        updateOwnershipBadge(p.getPosition());
        refreshSidebar();

        if (!p.isInJail()) {
            game.advanceTurn();
            turnCounter++;
        }
    }

    private void showPurchaseDialog(Player p, Ownable ownable, String spaceName, int price, Consumer<String> onDone) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(stage);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle("Purchase");
        alert.setHeaderText("Buy " + spaceName + " for $" + price + "?");
        alert.setContentText("Choose Buy to purchase, or Skip to leave it unowned.");
        ButtonType buy = new ButtonType("Buy", ButtonBar.ButtonData.OK_DONE);
        ButtonType skip = new ButtonType("Skip", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buy, skip);
        if (rollBtn != null) rollBtn.setDisable(true);
        Optional<ButtonType> res = alert.showAndWait();
        if (rollBtn != null) rollBtn.setDisable(false);
        if (res.isPresent() && res.get() == buy) {
            ownable.setOwner(p);
            p.subtractBalance(price);
            showCashDelta(p, -price);
            onDone.accept("Bought for $" + price);
        } else {
            onDone.accept("Declined purchase");
        }
    }

    private void updateOwnershipBadge(int idx) {
        Space s = game.getSpace(idx);
        Node badge = ownerBadges.get(idx);
        if (badge == null || !(s instanceof Ownable)) return;
        Ownable ownable = (Ownable) s;
        Player owner = ownable.getOwner();
        if (badge instanceof Circle c) {
            if (owner == null) { c.setFill(Color.TRANSPARENT); c.setStroke(Color.GRAY); }
            else { c.setFill(owner.getColor()); c.setStroke(Color.BLACK); }
        }
        Tooltip tp = cellTooltips.get(idx);
        if (tp != null) tp.setText(spaceTooltipText(s));
    }

    private void showCashDelta(Player p, int delta) {
        Label balLbl = balanceLabels.get(p);
        HBox row = playerRows.get(p);
        if (balLbl == null || row == null) return;
        balLbl.setText("$" + p.getBalance());
        String text = (delta >= 0 ? "+" : "-") + "$" + Math.abs(delta);
        Label deltaLbl = new Label(text);
        deltaLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; "
                + (delta >= 0 ? "-fx-text-fill: #2e7d32;" : "-fx-text-fill: #c62828;"));
        row.getChildren().add(deltaLbl);
        Timeline t = new Timeline(
                new KeyFrame(Duration.ZERO, e -> { deltaLbl.setOpacity(1.0); deltaLbl.setTranslateY(0); }),
                new KeyFrame(Duration.millis(800), e -> { deltaLbl.setOpacity(0.0); deltaLbl.setTranslateY(-16); })
        );
        t.setOnFinished(e -> row.getChildren().remove(deltaLbl));
        t.play();
    }

    private void animatePath(Player p, List<Integer> positions, Runnable onFinished) {
        Timeline timeline = new Timeline();
        for (int i = 0; i < positions.size(); i++) {
            int idx = i;
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(150 * (i + 1)), e -> {
                p.setPosition(positions.get(idx));
                placeToken(p);
            }));
        }
        timeline.setOnFinished(e -> onFinished.run());
        timeline.play();
    }

    private void refreshSidebar() {
        for (Player p : game.getPlayers()) {
            Label balLbl = balanceLabels.get(p);
            if (balLbl != null) balLbl.setText("$" + p.getBalance());
        }
        StringBuilder own = new StringBuilder();
        for (Player pl : game.getPlayers()) {
            own.append(pl.getName()).append(":\n");
            List<String> props = game.getBoard().getOwnedNames(pl);
            if (props.isEmpty()) own.append(" (none)\n");
            else props.forEach(n -> own.append(" • ").append(n).append("\n"));
            own.append("\n");
        }
        ownershipArea.setText(own.toString());
        jailLabel.setText(game.getPlayers().stream()
                .map(pl -> pl.getName() + ": " + (pl.isInJail() ? "IN JAIL (" + pl.getJailTurns() + ")" : "Free"))
                .collect(Collectors.joining("\n")));
        cardLabel.setText(game.getPlayers().stream()
                .map(pl -> pl.getName() + " – Get Out Of Jail Free: " + pl.getGoojfCount())
                .collect(Collectors.joining("\n")));
        int N = game.getBoard().size();
        for (int i = 0; i < N; i++) {
            Space s = game.getSpace(i);
            Tooltip tp = cellTooltips.get(i);
            if (tp != null) tp.setText(spaceTooltipText(s));
            updateOwnershipBadge(i);
        }
    }

    private void promptBuildForColorGroup(Player p, String colorGroup) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.initOwner(stage);
        a.setTitle("Monopoly Achieved");
        a.setHeaderText(p.getName() + " owns all " + colorGroup + " properties!");
        a.setContentText("You can now build houses and a hotel on " + colorGroup + " properties.\nUse the “Manage Buildings” button to build.");
        a.showAndWait();
    }

    private void showManageBuildingsDialog(Player current) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(stage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Manage Buildings – " + current.getName());
        VBox content = new VBox(12);
        content.setPadding(new Insets(10));
        Map<String, List<Property>> byColor = game.getBoard().propertiesByColorOwnedBy(current);
        if (byColor.isEmpty()) {
            content.getChildren().add(new Label("No complete color groups owned."));
        } else {
            for (Map.Entry<String, List<Property>> entry : byColor.entrySet()) {
                String color = entry.getKey();
                List<Property> props = entry.getValue();
                if (!game.getBoard().playerOwnsFullSet(current, color)) continue;
                Label header = new Label(color + " group (House $" + Property.houseCostFor(color)
                        + ", Hotel $" + Property.houseCostFor(color) * 5 + ")");
                header.setStyle("-fx-font-weight: bold;");
                VBox groupBox = new VBox(6);
                groupBox.getChildren().add(header);
                for (Property prop : props) {
                    HBox row = new HBox(10);
                    Label name = new Label(prop.getName());
                    Label state = new Label("Houses: " + prop.getHouses() + ", Hotel: " + (prop.hasHotel() ? "Yes" : "No")
                            + " | Current Rent: $" + prop.getCurrentRent());
                    Button addHouse = new Button("Add House ($" + prop.getHouseCost() + ")");
                    Button addHotel = new Button("Add Hotel ($" + prop.getHotelCost() + ")");
                    addHouse.setDisable(prop.getHouses() >= 4 || prop.hasHotel() || current.getBalance() < prop.getHouseCost());
                    addHotel.setDisable(prop.hasHotel() || prop.getHouses() < 4 || current.getBalance() < prop.getHotelCost());
                    addHouse.setOnAction(e -> {
                        if (prop.buildHouse(current)) {
                            showCashDelta(current, -prop.getHouseCost());
                            state.setText("Houses: " + prop.getHouses() + ", Hotel: " + (prop.hasHotel() ? "Yes" : "No")
                                    + " | Current Rent: $" + prop.getCurrentRent());
                            addHouse.setDisable(prop.getHouses() >= 4 || prop.hasHotel() || current.getBalance() < prop.getHouseCost());
                            addHotel.setDisable(prop.hasHotel() || prop.getHouses() < 4 || current.getBalance() < prop.getHotelCost());
                            refreshSidebar();
                            int idx = game.getBoard().indexOf(prop);
                            Tooltip tp = cellTooltips.get(idx);
                            if (tp != null) tp.setText(spaceTooltipText(prop));
                        }
                    });
                    addHotel.setOnAction(e -> {
                        if (prop.buildHotel(current)) {
                            showCashDelta(current, -prop.getHotelCost());
                            state.setText("Houses: " + prop.getHouses() + ", Hotel: " + (prop.hasHotel() ? "Yes" : "No")
                                    + " | Current Rent: $" + prop.getCurrentRent());
                            addHouse.setDisable(prop.getHouses() >= 4 || prop.hasHotel() || current.getBalance() < prop.getHouseCost());
                            addHotel.setDisable(prop.hasHotel() || prop.getHouses() < 4 || current.getBalance() < prop.getHotelCost());
                            refreshSidebar();
                            int idx = game.getBoard().indexOf(prop);
                            Tooltip tp = cellTooltips.get(idx);
                            if (tp != null) tp.setText(spaceTooltipText(prop));
                        }
                    });
                    row.getChildren().addAll(name, state, addHouse, addHotel);
                    row.setAlignment(Pos.CENTER_LEFT);
                    groupBox.getChildren().add(row);
                }
                content.getChildren().add(groupBox);
            }
        }
        dialog.getDialogPane().setContent(new ScrollPane(content));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void logTurnHeader(Player p, int[] dice) {
        String time = LocalTime.now().format(timeFmt);
        appendLog(String.format("[%s] Turn %d – %s rolls %d + %d", time, turnCounter, p.getName(), dice[0], dice[1]));
    }
    private void log(Player p, String message) {
        String time = LocalTime.now().format(timeFmt);
        appendLog(String.format("[%s] %s – %s", time, p.getName(), message));
    }
    private void logGlobal(String message) {
        String time = LocalTime.now().format(timeFmt);
        appendLog(String.format("[%s] %s", time, message));
    }

    private void appendLog(String line) {
        if (gameLog != null) {
            if (!gameLog.getText().isEmpty()) gameLog.appendText("\n");
            gameLog.appendText(line);
            gameLog.setScrollTop(Double.MAX_VALUE);
        }
        if (logWriter != null) {
            logWriter.submit(line);
        }
    }

    private void changeLogFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Text Log File");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        if (logFile != null) chooser.setInitialFileName(logFile.getFileName().toString());
        try {
            Path startDir = logFile != null ? logFile.getParent() : Paths.get(System.getProperty("user.home"));
            chooser.setInitialDirectory(startDir.toFile());
        } catch (Exception ignored) {}
        File file = chooser.showSaveDialog(stage);
        if (file == null) return;
        Path chosen = file.toPath();
        try {
            if (logWriter == null) logWriter = new LogWriter(chosen);
            else logWriter.setFile(chosen);
            logFile = chosen;
            appendLog("[LOG] Switched text log file to: " + logFile.toAbsolutePath());
        } catch (IOException ex) {
            showAlert("Log Switch Error", "Failed to switch log file: " + ex.getMessage());
        }
    }

    private void exportBinarySave() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Save (Binary)");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Monopoly Save", "*.mon"));
        chooser.setInitialFileName("MonopolySave.mon");
        File file = chooser.showSaveDialog(stage);
        if (file == null) return;
        try {
            SaveData data = SaveData.fromGame(game, gameLog.getText());
            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(file));
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) { oos.writeObject(data); }
            showInfo("Exported", "Binary save written to:\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            showAlert("Export Error", "Failed to export save: " + ex.getMessage());
        }
    }

    private void importSavedGameFromLogin() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Save (Binary)");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Monopoly Save", "*.mon"));
        File file = chooser.showOpenDialog(stage);
        if (file == null) return;
        try (InputStream fis = new BufferedInputStream(new FileInputStream(file));
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            SaveData data = (SaveData) ois.readObject();
            List<Player> players = new ArrayList<>();
            for (SaveData.PlayerData pd : data.players) {
                Player p = new Player(pd.name, SaveData.colorFromHex(pd.colorHex));
                p.setPosition(pd.position);
                p.addBalance(pd.balance - p.getBalance());
                if (pd.inJail) p.setInJail(true);
                for (int i = 0; i < pd.goojfCount; i++) p.addGetOutOfJailFree();
                for (int i = 0; i < pd.jailTurns; i++) p.incrementJailTurns();
                p.setGoPassCounterAtJail(pd.goPassCounterAtJail);
                players.add(p);
            }
            startGame(players);
            game.setCurrentIndex(data.currentIndex);
            game.setTotalGoPasses(data.totalGoPasses);
            game.applyOwnershipFromSave(data);
            game.applyDecksFromSave(data);
            gameLog.clear();
            gameLog.appendText(data.textLog == null ? "" : data.textLog);
            refreshSidebar();
            int N = game.getBoard().size();
            for (int i = 0; i < N; i++) { updateOwnershipBadge(i); }
            showInfo("Imported", "Loaded save:\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            showAlert("Import Error", "Failed to import save: " + ex.getMessage());
        }
    }

    private void openLogFileIntoDialog() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Text Log File");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = chooser.showOpenDialog(stage);
        if (file == null) return;
        Path path = file.toPath();
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line; while ((line = br.readLine()) != null) { content.append(line).append("\n"); }
        } catch (IOException ex) { showAlert("Open Log Error", "Failed to read log file: " + ex.getMessage()); return; }
        TextArea ta = new TextArea(content.toString());
        ta.setEditable(false); ta.setPrefRowCount(20); ta.setWrapText(true);
        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(stage); dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Log Viewer – " + path.getFileName());
        dialog.getDialogPane().setContent(ta);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(title);
        a.initOwner(stage);
        a.initModality(Modality.APPLICATION_MODAL);
        a.showAndWait();
    }

    private int[] indexToGrid(int idx) {
        if (idx < 10) { int col = 10 - idx; return new int[]{col, 10}; }
        else if (idx < 20) { int row = 10 - (idx - 10); return new int[]{0, row}; }
        else if (idx < 30) { int col = idx - 20; return new int[]{col, 0}; }
        else { int row = idx - 30; return new int[]{10, row}; }
    }

    private String gradientForType(SpaceType t) {
        return switch (t) {
            case START -> "-fx-background-color: linear-gradient(to bottom, #eaffea, #c9f7c9);";
            case JAIL, GOTO_JAIL -> "-fx-background-color: linear-gradient(to bottom, #f2eaff, #d9c9f7);";
            case FREE_PARKING -> "-fx-background-color: linear-gradient(to bottom, #eaffff, #c9f7f7);";
            case TAX -> "-fx-background-color: linear-gradient(to bottom, #ffeaea, #f7c9c9);";
            case CHANCE -> "-fx-background-color: linear-gradient(to bottom, #eaf2ff, #c9d9f7);";
            case COMMUNITY_CHEST -> "-fx-background-color: linear-gradient(to bottom, #fff5e6, #f7ddb8);";
            case PROPERTY -> "-fx-background-color: linear-gradient(to bottom, #fff4c2, #f8e6a0);";
            case RAILROAD -> "-fx-background-color: linear-gradient(to bottom, #ffe1f2, #f7cbe0);";
            case UTILITY -> "-fx-background-color: linear-gradient(to bottom, #eaffd1, #d7f7a8);";
        };
    }

    private String spaceTooltipText(Space s) {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(s.getName()).append("\n");
        sb.append("Type: ").append(s.getType()).append("\n");
        if (s instanceof Property prop) {
            sb.append("Price: $").append(prop.getPrice()).append("\n");
            sb.append("Base Rent: $").append(prop.getBaseRent()).append("\n");
            sb.append("Houses: ").append(prop.getHouses()).append(", Hotel: ").append(prop.hasHotel() ? "Yes" : "No").append("\n");
            sb.append("Current Rent: $").append(prop.getCurrentRent()).append("\n");
            sb.append("Owner: ").append(prop.getOwner() == null ? "None" : prop.getOwner().getName());
        } else if (s instanceof Railroad rr) {
            sb.append("Price: $").append(rr.getPrice()).append("\n");
            sb.append("Owner: ").append(rr.getOwner() == null ? "None" : rr.getOwner().getName());
        } else if (s instanceof Utility ut) {
            sb.append("Price: $").append(ut.getPrice()).append("\n");
            sb.append("Owner: ").append(ut.getOwner() == null ? "None" : ut.getOwner().getName());
        }
        return sb.toString();
    }

    private Color groupColor(Property prop) {
        String g = prop.getColorGroup();
        return switch (g) {
            case "Brown" -> Color.SIENNA;
            case "Light Blue" -> Color.LIGHTSKYBLUE;
            case "Pink" -> Color.MEDIUMPURPLE;
            case "Orange" -> Color.ORANGE;
            case "Red" -> Color.CRIMSON;
            case "Yellow" -> Color.GOLD;
            case "Green" -> Color.DARKGREEN;
            case "Dark Blue" -> Color.DARKBLUE;
            default -> Color.WHITE;
        };
    }
}
