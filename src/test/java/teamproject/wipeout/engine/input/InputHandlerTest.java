package teamproject.wipeout.engine.input;

import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.robot.Motion;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

// From https://github.com/TestFX/TestFX

@ExtendWith(ApplicationExtension.class)
public class InputHandlerTest {

    private final double stageWidth = 300.0;
    private final double stageHeight = 100.0;

    private InputHandler inputHandler;
    private Point2D stagePosition;

    // Start will be called before each test method.
    // "stage" argument will be injected by the test runner.
    @Start
    private void start(Stage stage) {
        Canvas canvas = new Canvas(stageWidth, stageHeight);
        Scene scene = new Scene(new StackPane(canvas), stageWidth, stageHeight);

        this.inputHandler = new InputHandler(scene);

        stage.setScene(scene);
        stage.show();

        this.stagePosition = new Point2D(stage.getX(), stage.getY());
    }

    // "robot" test method argument will be supplied by the test runner for all tests.

    @RepeatedTest(3)
    @DisplayName("onKeyPress")
    void testKeyPress(FxRobot robot) {
        AtomicBoolean letterKeyPressed = new AtomicBoolean(false);
        AtomicBoolean numberKeyPressed = new AtomicBoolean(false);
        AtomicBoolean arrowKeyPressed = new AtomicBoolean(false);

        inputHandler.onKeyPress(KeyCode.W, () -> letterKeyPressed.set(true));
        inputHandler.onKeyPress(KeyCode.DIGIT8, () -> numberKeyPressed.set(true));
        inputHandler.onKeyPress(KeyCode.UP, () -> arrowKeyPressed.set(true));

        robot.press(KeyCode.W);
        robot.press(KeyCode.DIGIT8);
        robot.press(KeyCode.UP);

        Assertions.assertTrue(letterKeyPressed.get(), "onKeyPress didn't register letter key press.");
        Assertions.assertTrue(numberKeyPressed.get(), "onKeyPress didn't register number key press.");
        Assertions.assertTrue(arrowKeyPressed.get(), "onKeyPress didn't register arrow key press.");
    }

    @RepeatedTest(3)
    @DisplayName("onKeyRelease")
    void testKeyRelease(FxRobot robot) {
        AtomicBoolean letterKeyReleased = new AtomicBoolean(false);
        AtomicBoolean numberKeyReleased = new AtomicBoolean(false);
        AtomicBoolean arrowKeyReleased = new AtomicBoolean(false);

        inputHandler.onKeyRelease(KeyCode.S, () -> letterKeyReleased.set(true));
        inputHandler.onKeyRelease(KeyCode.DIGIT2, () -> numberKeyReleased.set(true));
        inputHandler.onKeyRelease(KeyCode.DOWN, () -> arrowKeyReleased.set(true));

        robot.press(KeyCode.S);
        robot.release(KeyCode.S);
        robot.press(KeyCode.DIGIT2);
        robot.release(KeyCode.DIGIT2);
        robot.press(KeyCode.DOWN);
        robot.release(KeyCode.DOWN);

        Assertions.assertTrue(letterKeyReleased.get(), "onKeyRelease didn't register letter key release.");
        Assertions.assertTrue(numberKeyReleased.get(), "onKeyRelease didn't register number key release.");
        Assertions.assertTrue(arrowKeyReleased.get(), "onKeyRelease didn't register arrow key release.");
    }

    @RepeatedTest(3)
    @DisplayName("addKeyAction")
    void testKeyAction(FxRobot robot) {
        AtomicInteger letterKeyPressed = new AtomicInteger(0);
        AtomicInteger numberKeyPressed = new AtomicInteger(0);
        AtomicInteger arrowKeyPressed = new AtomicInteger(0);
        AtomicInteger letterKeyReleased = new AtomicInteger(0);
        AtomicInteger numberKeyReleased = new AtomicInteger(0);
        AtomicInteger arrowKeyReleased = new AtomicInteger(0);

        inputHandler.addKeyAction(KeyCode.Q,
                () -> letterKeyPressed.incrementAndGet(),
                () -> letterKeyReleased.incrementAndGet());
        inputHandler.addKeyAction(KeyCode.DECIMAL,
                () -> numberKeyPressed.incrementAndGet(),
                () -> numberKeyReleased.incrementAndGet());
        inputHandler.addKeyAction(KeyCode.LEFT,
                () -> arrowKeyPressed.incrementAndGet(),
                () -> arrowKeyReleased.incrementAndGet());

        robot.type(KeyCode.Q, 1);
        robot.type(KeyCode.DECIMAL, 1);
        robot.type(KeyCode.LEFT, 1);

        robot.type(KeyCode.LEFT, 2);
        robot.type(KeyCode.DECIMAL, 2);
        robot.type(KeyCode.Q, 2);

        Assertions.assertEquals(3, letterKeyPressed.get(),
                "addKeyAction didn't register letter key press.");
        Assertions.assertEquals(3, numberKeyPressed.get(),
                "addKeyAction didn't register number key press.");
        Assertions.assertEquals(3, arrowKeyPressed.get(),
                "addKeyAction didn't register arrow key press.");
        Assertions.assertEquals(3, letterKeyReleased.get(),
                "addKeyAction didn't register letter key release.");
        Assertions.assertEquals(3, numberKeyReleased.get(),
                "addKeyAction didn't register number key release.");
        Assertions.assertEquals(3, arrowKeyReleased.get(),
                "addKeyAction didn't register arrow key release.");
    }

    @RepeatedTest(3)
    @DisplayName("onMouseClick within stage bounds")
    void testMouseClickWithinStage(FxRobot robot) {
        AtomicReference<Point2D> primaryButtonClicked = new AtomicReference<>(null);
        AtomicReference<Point2D> middleButtonClicked = new AtomicReference<>(null);
        AtomicReference<Point2D> secondaryButtonClicked = new AtomicReference<>(null);

        inputHandler.onMouseClick(MouseButton.PRIMARY, (x, y) -> primaryButtonClicked.set(new Point2D(x, y)));
        inputHandler.onMouseClick(MouseButton.MIDDLE, (x, y) -> middleButtonClicked.set(new Point2D(x, y)));
        inputHandler.onMouseClick(MouseButton.SECONDARY, (x, y) -> secondaryButtonClicked.set(new Point2D(x, y)));

        Point2D primaryClick = Point2D.ZERO;
        Point2D middleClick = new Point2D((stageWidth / 2), (stageHeight / 2));
        Point2D secondaryClick = new Point2D((stageWidth - 1), (stageHeight - 1));

        robot.clickOn(stagePosition.add(primaryClick), Motion.DIRECT, MouseButton.PRIMARY);
        robot.clickOn(stagePosition.add(middleClick), Motion.DIRECT, MouseButton.MIDDLE);
        robot.clickOn(stagePosition.add(secondaryClick), Motion.DIRECT, MouseButton.SECONDARY);

        Assertions.assertNotNull(primaryButtonClicked.get(),
                "onMouseClick didn't register primary mouse button click.");
        Assertions.assertNotNull(middleButtonClicked.get(),
                "onMouseClick didn't register middle mouse button click.");
        Assertions.assertNotNull(secondaryButtonClicked.get(),
                "onMouseClick didn't register secondary mouse button click.");

        Assertions.assertEquals(primaryClick, primaryButtonClicked.get(),
                "onMouseClick registered primary mouse button click with wrong coordinates.");
        Assertions.assertEquals(middleClick, middleButtonClicked.get(),
                "onMouseClick registered middle mouse button click with wrong coordinates.");
        Assertions.assertEquals(secondaryClick, secondaryButtonClicked.get(),
                "onMouseClick registered secondary mouse button click with wrong coordinates.");
    }

    @RepeatedTest(3)
    @DisplayName("onMouseClick outside stage bounds")
    void testMouseClickOutsideStage(FxRobot robot) {
        AtomicBoolean primaryButtonClicked = new AtomicBoolean(false);
        AtomicBoolean middleButtonClicked = new AtomicBoolean(false);
        AtomicBoolean secondaryButtonClicked = new AtomicBoolean(false);

        inputHandler.onMouseClick(MouseButton.PRIMARY, (x, y) -> primaryButtonClicked.set(true));
        inputHandler.onMouseClick(MouseButton.MIDDLE, (x, y) -> middleButtonClicked.set(true));
        inputHandler.onMouseClick(MouseButton.SECONDARY, (x, y) -> secondaryButtonClicked.set(true));

        Point2D primaryClick = new Point2D(-1, -1);
        Point2D middleClick = Point2D.ZERO;
        Point2D secondaryClick = new Point2D(stageWidth, stageHeight);

        robot.clickOn(stagePosition.add(primaryClick), Motion.DIRECT, MouseButton.PRIMARY);
        robot.clickOn(middleClick, Motion.DIRECT, MouseButton.MIDDLE);
        robot.clickOn(stagePosition.add(secondaryClick), Motion.DIRECT, MouseButton.SECONDARY);

        Assertions.assertFalse(primaryButtonClicked.get(),
                "onMouseClick registered primary mouse button click outside stage bounds.");
        Assertions.assertFalse(middleButtonClicked.get(),
                "onMouseClick registered middle mouse button click outside stage bounds.");
        Assertions.assertFalse(secondaryButtonClicked.get(),
                "onMouseClick registered secondary mouse button click outside stage bounds.");
    }

    @RepeatedTest(3)
    @DisplayName("onMouseDrag within stage bounds")
    void testMouseDragWithinStage(FxRobot robot) {
        AtomicReference<Point2D> primaryButtonStartDrag = new AtomicReference<>(null);
        AtomicReference<Point2D> secondaryButtonStartDrag = new AtomicReference<>(null);

        AtomicReference<Point2D> primaryButtonDragging = new AtomicReference<>(null);
        AtomicReference<Point2D> secondaryButtonDragging = new AtomicReference<>(null);

        AtomicReference<Point2D> primaryButtonEndDrag = new AtomicReference<>(null);
        AtomicReference<Point2D> secondaryButtonEndDrag = new AtomicReference<>(null);

        inputHandler.onMouseDrag(MouseButton.PRIMARY,
                (x, y) -> primaryButtonStartDrag.set(new Point2D(x, y)),
                (x, y) -> primaryButtonDragging.set(new Point2D(x, y)),
                (x, y) -> primaryButtonEndDrag.set(new Point2D(x, y)));

        inputHandler.onMouseDrag(MouseButton.SECONDARY,
                (x, y) -> secondaryButtonStartDrag.set(new Point2D(x, y)),
                (x, y) -> secondaryButtonDragging.set(new Point2D(x, y)),
                (x, y) -> secondaryButtonEndDrag.set(new Point2D(x, y)));

        Point2D primaryDragStart = Point2D.ZERO;
        Point2D primaryDragEnd = new Point2D(stageWidth / 2, stageHeight / 2);
        Point2D secondaryDragStart = new Point2D(stageWidth / 2, stageHeight / 2);
        Point2D secondaryDragEnd = new Point2D(stageWidth - 1, stageHeight - 1);

        robot.drag(stagePosition.add(primaryDragStart), MouseButton.PRIMARY);
        robot.dropTo(stagePosition.add(primaryDragEnd));
        robot.drag(stagePosition.add(secondaryDragStart), MouseButton.SECONDARY);
        robot.dropTo(stagePosition.add(secondaryDragEnd));

        Assertions.assertEquals(primaryDragStart, primaryButtonStartDrag.get(),
                "onMouseDrag primary start isn't accurate.");
        Assertions.assertEquals(primaryDragEnd, primaryButtonEndDrag.get(),
                "onMouseDrag primary end isn't accurate.");
        Assertions.assertEquals(primaryButtonEndDrag.get(), primaryButtonDragging.get(),
                "onMouseDrag primary isn't accurate.");

        Assertions.assertEquals(secondaryDragStart, secondaryButtonStartDrag.get(),
                "onMouseDrag secondary start isn't accurate.");
        Assertions.assertEquals(secondaryDragEnd, secondaryButtonEndDrag.get(),
                "onMouseDrag secondary end isn't accurate.");
        Assertions.assertEquals(secondaryButtonEndDrag.get(), secondaryButtonDragging.get(),
                "onMouseDrag secondary isn't accurate.");
    }

    @RepeatedTest(3)
    @DisplayName("onMouseDrag outside stage bounds")
    void testMouseDragOutsideStage(FxRobot robot) {
        AtomicReference<Point2D> primaryButtonStartDrag = new AtomicReference<>(null);
        AtomicReference<Point2D> secondaryButtonStartDrag = new AtomicReference<>(null);

        AtomicReference<Point2D> primaryButtonDragging = new AtomicReference<>(null);
        AtomicReference<Point2D> secondaryButtonDragging = new AtomicReference<>(null);

        AtomicReference<Point2D> primaryButtonEndDrag = new AtomicReference<>(null);
        AtomicReference<Point2D> secondaryButtonEndDrag = new AtomicReference<>(null);

        inputHandler.onMouseDrag(MouseButton.PRIMARY,
                (x, y) -> primaryButtonStartDrag.set(new Point2D(x, y)),
                (x, y) -> primaryButtonDragging.set(new Point2D(x, y)),
                (x, y) -> primaryButtonEndDrag.set(new Point2D(x, y)));

        inputHandler.onMouseDrag(MouseButton.SECONDARY,
                (x, y) -> secondaryButtonStartDrag.set(new Point2D(x, y)),
                (x, y) -> secondaryButtonDragging.set(new Point2D(x, y)),
                (x, y) -> secondaryButtonEndDrag.set(new Point2D(x, y)));

        Point2D primaryDragStart = Point2D.ZERO;
        Point2D midStage = new Point2D(stageWidth / 2, stageHeight / 2);
        Point2D secondaryDragEnd = Point2D.ZERO;

        robot.drag(primaryDragStart, MouseButton.PRIMARY);
        robot.dropTo(stagePosition.add(midStage));
        robot.drag(stagePosition.add(midStage), MouseButton.SECONDARY);
        robot.dropTo(secondaryDragEnd);

        Assertions.assertNull(primaryButtonStartDrag.get(),
                "onMouseDrag primary start isn't null even though it started outside the stage's bounds.");
        Assertions.assertNull(primaryButtonEndDrag.get(),
                "onMouseDrag primary end isn't null even though it started outside the stage's bounds.");
        Assertions.assertEquals(primaryButtonEndDrag.get(), primaryButtonDragging.get(),
                "onMouseDrag primary isn't accurate.");

        Assertions.assertNotNull(secondaryButtonStartDrag.get(),
                "onMouseDrag secondary start is null even though it started inside the stage's bounds.");
        Assertions.assertNotNull(secondaryButtonEndDrag.get(),
                "onMouseDrag secondary end is null even though it started inside the stage's bounds.");
        Assertions.assertEquals(secondaryButtonEndDrag.get(), secondaryButtonDragging.get(),
                "onMouseDrag secondary isn't accurate.");
    }

    @RepeatedTest(3)
    @DisplayName("onMouseDrag with onMouseClick")
    void testMouseInputCombined(FxRobot robot) {
        AtomicInteger primaryButtonClickCount = new AtomicInteger(0);
        AtomicInteger middleButtonClickCount = new AtomicInteger(0);
        AtomicInteger secondaryButtonClickCount = new AtomicInteger(0);

        AtomicReference<Point2D> primaryButtonStartDrag = new AtomicReference<>(null);
        AtomicReference<Point2D> secondaryButtonStartDrag = new AtomicReference<>(null);

        AtomicReference<Point2D> primaryButtonDragging = new AtomicReference<>(null);
        AtomicReference<Point2D> secondaryButtonDragging = new AtomicReference<>(null);

        AtomicReference<Point2D> primaryButtonEndDrag = new AtomicReference<>(null);
        AtomicReference<Point2D> secondaryButtonEndDrag = new AtomicReference<>(null);

        inputHandler.onMouseClick(MouseButton.PRIMARY, (x, y) -> primaryButtonClickCount.incrementAndGet());
        inputHandler.onMouseClick(MouseButton.MIDDLE, (x, y) -> middleButtonClickCount.incrementAndGet());
        inputHandler.onMouseClick(MouseButton.SECONDARY, (x, y) -> secondaryButtonClickCount.incrementAndGet());

        inputHandler.onMouseDrag(MouseButton.PRIMARY,
                (x, y) -> primaryButtonStartDrag.set(new Point2D(x, y)),
                (x, y) -> primaryButtonDragging.set(new Point2D(x, y)),
                (x, y) -> primaryButtonEndDrag.set(new Point2D(x, y)));

        inputHandler.onMouseDrag(MouseButton.SECONDARY,
                (x, y) -> secondaryButtonStartDrag.set(new Point2D(x, y)),
                (x, y) -> secondaryButtonDragging.set(new Point2D(x, y)),
                (x, y) -> secondaryButtonEndDrag.set(new Point2D(x, y)));

        Point2D primaryDragStart = Point2D.ZERO;
        Point2D primaryDragEnd = new Point2D(stageWidth / 2, stageHeight / 2);
        Point2D secondaryDragStart = new Point2D(stageWidth / 2, stageHeight / 2);
        Point2D secondaryDragEnd = new Point2D(stageWidth - 1, stageHeight - 1);

        robot.clickOn(stagePosition.add(primaryDragEnd.add(1, 0)), Motion.DIRECT, MouseButton.PRIMARY);
        robot.clickOn(stagePosition.add(primaryDragEnd.add(1, 1)), Motion.DIRECT, MouseButton.MIDDLE);
        robot.clickOn(stagePosition.add(primaryDragEnd.add(1, 0)), Motion.DIRECT, MouseButton.SECONDARY);

        robot.moveTo(stagePosition.add(primaryDragStart), Motion.DIRECT);
        robot.press(MouseButton.PRIMARY);
        robot.moveBy(stageWidth / 2, stageHeight / 2,  Motion.DIRECT);
        robot.clickOn(MouseButton.SECONDARY);
        robot.clickOn(MouseButton.MIDDLE);
        robot.clickOn(MouseButton.SECONDARY);
        robot.release(MouseButton.PRIMARY);

        robot.clickOn(stagePosition.add(primaryDragEnd.add(0, -1)), Motion.DIRECT, MouseButton.PRIMARY);
        robot.clickOn(stagePosition.add(primaryDragEnd.add(-1, -1)), Motion.DIRECT, MouseButton.MIDDLE);
        robot.clickOn(stagePosition.add(primaryDragEnd.add(-1, 0)), Motion.DIRECT, MouseButton.SECONDARY);

        robot.moveTo(stagePosition.add(secondaryDragStart), Motion.DIRECT);
        robot.press(MouseButton.SECONDARY);
        robot.moveTo(stagePosition.add(secondaryDragEnd),  Motion.DIRECT);
        robot.clickOn(MouseButton.PRIMARY);
        robot.clickOn(MouseButton.MIDDLE);
        robot.clickOn(MouseButton.PRIMARY);
        robot.release(MouseButton.SECONDARY);

        robot.clickOn(stagePosition.add(secondaryDragStart), Motion.DIRECT, MouseButton.PRIMARY);
        robot.clickOn(stagePosition.add(secondaryDragStart), Motion.DIRECT, MouseButton.MIDDLE);
        robot.clickOn(stagePosition.add(secondaryDragStart), Motion.DIRECT, MouseButton.SECONDARY);

        Assertions.assertEquals(3, primaryButtonClickCount.get(),
                "onMouseClick primary clicks count isn't accurate.");
        Assertions.assertEquals(3, middleButtonClickCount.get(),
                "onMouseClick middle clicks count isn't accurate.");
        Assertions.assertEquals(3, secondaryButtonClickCount.get(),
                "onMouseClick secondary clicks count isn't accurate.");

        Assertions.assertEquals(primaryDragStart, primaryButtonStartDrag.get(),
                "onMouseDrag primary start isn't accurate.");
        Assertions.assertEquals(primaryDragEnd, primaryButtonEndDrag.get(),
                "onMouseDrag primary end isn't accurate.");
        Assertions.assertEquals(primaryButtonEndDrag.get(), primaryButtonDragging.get(),
                "onMouseDrag primary isn't accurate.");

        Assertions.assertEquals(secondaryDragStart, secondaryButtonStartDrag.get(),
                "onMouseDrag secondary start isn't accurate.");
        Assertions.assertEquals(secondaryDragEnd, secondaryButtonEndDrag.get(),
                "onMouseDrag secondary end isn't accurate.");
        Assertions.assertEquals(secondaryButtonEndDrag.get(), secondaryButtonDragging.get(),
                "onMouseDrag secondary isn't accurate.");
    }

    @RepeatedTest(3)
    @DisplayName("Testing all input methods together")
    void testInputs(FxRobot robot) {
        AtomicInteger keyPressACount = new AtomicInteger(0);
        AtomicInteger keyReleaseACount = new AtomicInteger(0);
        AtomicInteger keyPressLCount = new AtomicInteger(0);
        AtomicInteger keyReleaseLCount = new AtomicInteger(0);

        AtomicInteger primaryButtonClickCount = new AtomicInteger(0);
        AtomicInteger middleButtonClickCount = new AtomicInteger(0);
        AtomicInteger secondaryButtonClickCount = new AtomicInteger(0);

        AtomicReference<Point2D> primaryButtonStartDrag = new AtomicReference<>(null);
        AtomicReference<Point2D> secondaryButtonStartDrag = new AtomicReference<>(null);

        AtomicReference<Point2D> primaryButtonDragging = new AtomicReference<>(null);
        AtomicReference<Point2D> secondaryButtonDragging = new AtomicReference<>(null);

        AtomicReference<Point2D> primaryButtonEndDrag = new AtomicReference<>(null);
        AtomicReference<Point2D> secondaryButtonEndDrag = new AtomicReference<>(null);

        inputHandler.onMouseClick(MouseButton.PRIMARY, (x, y) -> primaryButtonClickCount.incrementAndGet());
        inputHandler.onMouseClick(MouseButton.MIDDLE, (x, y) -> middleButtonClickCount.incrementAndGet());
        inputHandler.onMouseClick(MouseButton.SECONDARY, (x, y) -> secondaryButtonClickCount.incrementAndGet());

        inputHandler.addKeyAction(KeyCode.A,
                () -> keyPressACount.incrementAndGet(),
                () -> keyReleaseACount.incrementAndGet());

        inputHandler.onMouseDrag(MouseButton.PRIMARY,
                (x, y) -> primaryButtonStartDrag.set(new Point2D(x, y)),
                (x, y) -> primaryButtonDragging.set(new Point2D(x, y)),
                (x, y) -> primaryButtonEndDrag.set(new Point2D(x, y)));

        inputHandler.addKeyAction(KeyCode.L,
                () -> keyPressLCount.incrementAndGet(),
                () -> keyReleaseLCount.incrementAndGet());

        inputHandler.onMouseDrag(MouseButton.SECONDARY,
                (x, y) -> secondaryButtonStartDrag.set(new Point2D(x, y)),
                (x, y) -> secondaryButtonDragging.set(new Point2D(x, y)),
                (x, y) -> secondaryButtonEndDrag.set(new Point2D(x, y)));

        Point2D primaryDragStart = Point2D.ZERO;
        Point2D primaryDragEnd = new Point2D(stageWidth / 2, stageHeight / 2);
        Point2D secondaryDragStart = new Point2D(stageWidth / 2, stageHeight / 2);
        Point2D secondaryDragEnd = new Point2D(stageWidth - 1, stageHeight - 1);

        robot.clickOn(stagePosition.add(primaryDragEnd.add(1, 0)), Motion.DIRECT, MouseButton.PRIMARY);
        robot.clickOn(stagePosition.add(primaryDragEnd.add(1, 1)), Motion.DIRECT, MouseButton.MIDDLE);
        robot.clickOn(stagePosition.add(primaryDragEnd.add(1, 0)), Motion.DIRECT, MouseButton.SECONDARY);

        robot.type(KeyCode.A);

        robot.moveTo(stagePosition.add(primaryDragStart), Motion.DIRECT);
        robot.press(MouseButton.PRIMARY);
        robot.moveBy(stageWidth / 2, stageHeight / 2,  Motion.DIRECT);
        robot.clickOn(MouseButton.SECONDARY);
        robot.type(KeyCode.L);
        robot.clickOn(MouseButton.MIDDLE);
        robot.clickOn(MouseButton.SECONDARY);
        robot.release(MouseButton.PRIMARY);

        robot.type(KeyCode.A);

        robot.clickOn(stagePosition.add(primaryDragEnd.add(0, -1)), Motion.DIRECT, MouseButton.PRIMARY);
        robot.clickOn(stagePosition.add(primaryDragEnd.add(-1, -1)), Motion.DIRECT, MouseButton.MIDDLE);
        robot.clickOn(stagePosition.add(primaryDragEnd.add(-1, 0)), Motion.DIRECT, MouseButton.SECONDARY);

        robot.moveTo(stagePosition.add(secondaryDragStart), Motion.DIRECT);
        robot.press(MouseButton.SECONDARY);
        robot.moveTo(stagePosition.add(secondaryDragEnd),  Motion.DIRECT);
        robot.clickOn(MouseButton.PRIMARY);
        robot.clickOn(MouseButton.MIDDLE);
        robot.type(KeyCode.L);
        robot.clickOn(MouseButton.PRIMARY);
        robot.release(MouseButton.SECONDARY);

        robot.type(KeyCode.A);

        robot.clickOn(stagePosition.add(secondaryDragStart), Motion.DIRECT, MouseButton.PRIMARY);
        robot.clickOn(stagePosition.add(secondaryDragStart), Motion.DIRECT, MouseButton.MIDDLE);
        robot.clickOn(stagePosition.add(secondaryDragStart), Motion.DIRECT, MouseButton.SECONDARY);

        Assertions.assertEquals(3, keyPressACount.get(),
                "onKeyPress 'A' presses count isn't accurate.");
        Assertions.assertEquals(3, keyReleaseACount.get(),
                "onKeyRelease 'A' presses count isn't accurate.");

        Assertions.assertEquals(2, keyPressLCount.get(),
                "onKeyPress 'L' presses count isn't accurate.");
        Assertions.assertEquals(2, keyReleaseLCount.get(),
                "onKeyRelease 'L' presses count isn't accurate.");

        Assertions.assertEquals(3, primaryButtonClickCount.get(),
                "onMouseClick primary clicks count isn't accurate.");
        Assertions.assertEquals(3, middleButtonClickCount.get(),
                "onMouseClick middle clicks count isn't accurate.");
        Assertions.assertEquals(3, secondaryButtonClickCount.get(),
                "onMouseClick secondary clicks count isn't accurate.");

        Assertions.assertEquals(primaryDragStart, primaryButtonStartDrag.get(),
                "onMouseDrag primary start isn't accurate.");
        Assertions.assertEquals(primaryDragEnd, primaryButtonEndDrag.get(),
                "onMouseDrag primary end isn't accurate.");
        Assertions.assertEquals(primaryButtonEndDrag.get(), primaryButtonDragging.get(),
                "onMouseDrag primary isn't accurate.");

        Assertions.assertEquals(secondaryDragStart, secondaryButtonStartDrag.get(),
                "onMouseDrag secondary start isn't accurate.");
        Assertions.assertEquals(secondaryDragEnd, secondaryButtonEndDrag.get(),
                "onMouseDrag secondary end isn't accurate.");
        Assertions.assertEquals(secondaryButtonEndDrag.get(), secondaryButtonDragging.get(),
                "onMouseDrag secondary isn't accurate.");
    }

    @RepeatedTest(3)
    @DisplayName("Testing all input methods but disabled")
    void testInputsDisabled(FxRobot robot) {
        inputHandler.setDisableInput(true);
        Assertions.assertTrue(inputHandler.getDisableInput(), "Input wasn't disabled.");

        AtomicInteger keyPressACount = new AtomicInteger(0);
        AtomicInteger keyReleaseACount = new AtomicInteger(0);
        AtomicInteger keyPressLCount = new AtomicInteger(0);
        AtomicInteger keyReleaseLCount = new AtomicInteger(0);

        AtomicInteger primaryButtonClickCount = new AtomicInteger(0);
        AtomicInteger middleButtonClickCount = new AtomicInteger(0);
        AtomicInteger secondaryButtonClickCount = new AtomicInteger(0);

        AtomicReference<Point2D> primaryButtonStartDrag = new AtomicReference<>(null);
        AtomicReference<Point2D> secondaryButtonStartDrag = new AtomicReference<>(null);

        AtomicReference<Point2D> primaryButtonDragging = new AtomicReference<>(null);
        AtomicReference<Point2D> secondaryButtonDragging = new AtomicReference<>(null);

        AtomicReference<Point2D> primaryButtonEndDrag = new AtomicReference<>(null);
        AtomicReference<Point2D> secondaryButtonEndDrag = new AtomicReference<>(null);

        inputHandler.onMouseClick(MouseButton.PRIMARY, (x, y) -> primaryButtonClickCount.incrementAndGet());
        inputHandler.onMouseClick(MouseButton.MIDDLE, (x, y) -> middleButtonClickCount.incrementAndGet());
        inputHandler.onMouseClick(MouseButton.SECONDARY, (x, y) -> secondaryButtonClickCount.incrementAndGet());

        inputHandler.addKeyAction(KeyCode.A,
                () -> keyPressACount.incrementAndGet(),
                () -> keyReleaseACount.incrementAndGet());

        inputHandler.onMouseDrag(MouseButton.PRIMARY,
                (x, y) -> primaryButtonStartDrag.set(new Point2D(x, y)),
                (x, y) -> primaryButtonDragging.set(new Point2D(x, y)),
                (x, y) -> primaryButtonEndDrag.set(new Point2D(x, y)));

        inputHandler.addKeyAction(KeyCode.L,
                () -> keyPressLCount.incrementAndGet(),
                () -> keyReleaseLCount.incrementAndGet());

        inputHandler.onMouseDrag(MouseButton.SECONDARY,
                (x, y) -> secondaryButtonStartDrag.set(new Point2D(x, y)),
                (x, y) -> secondaryButtonDragging.set(new Point2D(x, y)),
                (x, y) -> secondaryButtonEndDrag.set(new Point2D(x, y)));

        Point2D primaryDragStart = new Point2D(stageWidth - 1, 0);
        Point2D primaryDragEnd = new Point2D(stageWidth / 2, stageHeight / 2);
        Point2D secondaryDragStart = new Point2D(stageWidth / 2, stageHeight / 2);
        Point2D secondaryDragEnd = new Point2D(0, stageHeight - 1);

        robot.clickOn(stagePosition.add(primaryDragEnd.add(1, 0)), Motion.DIRECT, MouseButton.PRIMARY);
        robot.clickOn(stagePosition.add(primaryDragEnd.add(1, 1)), Motion.DIRECT, MouseButton.MIDDLE);
        robot.clickOn(stagePosition.add(primaryDragEnd.add(1, 0)), Motion.DIRECT, MouseButton.SECONDARY);

        robot.type(KeyCode.A);

        robot.moveTo(stagePosition.add(primaryDragStart), Motion.DIRECT);
        robot.press(MouseButton.PRIMARY);
        robot.moveBy(-1 * (stageWidth / 2), stageHeight / 2,  Motion.DIRECT);
        robot.clickOn(MouseButton.SECONDARY);
        robot.type(KeyCode.L);
        robot.clickOn(MouseButton.MIDDLE);
        robot.clickOn(MouseButton.SECONDARY);
        robot.release(MouseButton.PRIMARY);

        robot.type(KeyCode.A);

        robot.clickOn(stagePosition.add(primaryDragEnd.add(0, -1)), Motion.DIRECT, MouseButton.PRIMARY);
        robot.clickOn(stagePosition.add(primaryDragEnd.add(-1, -1)), Motion.DIRECT, MouseButton.MIDDLE);
        robot.clickOn(stagePosition.add(primaryDragEnd.add(-1, 0)), Motion.DIRECT, MouseButton.SECONDARY);

        robot.moveTo(stagePosition.add(secondaryDragStart), Motion.DIRECT);
        robot.press(MouseButton.SECONDARY);
        robot.moveTo(stagePosition.add(secondaryDragEnd),  Motion.DIRECT);
        robot.clickOn(MouseButton.PRIMARY);
        robot.clickOn(MouseButton.MIDDLE);
        robot.type(KeyCode.L);
        robot.clickOn(MouseButton.PRIMARY);
        robot.release(MouseButton.SECONDARY);

        robot.type(KeyCode.A);

        robot.clickOn(stagePosition.add(secondaryDragStart), Motion.DIRECT, MouseButton.PRIMARY);
        robot.clickOn(stagePosition.add(secondaryDragStart), Motion.DIRECT, MouseButton.MIDDLE);
        robot.clickOn(stagePosition.add(secondaryDragStart), Motion.DIRECT, MouseButton.SECONDARY);

        inputHandler.setDisableInput(false);
        Assertions.assertFalse(inputHandler.getDisableInput(), "Input wasn't enabled.");

        Assertions.assertEquals(0, keyPressACount.get(),
                "onKeyPress 'A' presses count isn't accurate.");
        Assertions.assertEquals(0, keyReleaseACount.get(),
                "onKeyRelease 'A' presses count isn't accurate.");

        Assertions.assertEquals(0, keyPressLCount.get(),
                "onKeyPress 'L' presses count isn't accurate.");
        Assertions.assertEquals(0, keyReleaseLCount.get(),
                "onKeyRelease 'L' presses count isn't accurate.");

        Assertions.assertEquals(0, primaryButtonClickCount.get(),
                "onMouseClick primary clicks count isn't accurate.");
        Assertions.assertEquals(0, middleButtonClickCount.get(),
                "onMouseClick middle clicks count isn't accurate.");
        Assertions.assertEquals(0, secondaryButtonClickCount.get(),
                "onMouseClick secondary clicks count isn't accurate.");

        Assertions.assertNull(primaryButtonStartDrag.get(),
                "onMouseDrag primary start isn't accurate.");
        Assertions.assertNull(primaryButtonDragging.get(),
                "onMouseDrag primary isn't accurate.");
        Assertions.assertNull(primaryButtonEndDrag.get(),
                "onMouseDrag primary end isn't accurate.");

        Assertions.assertNull( secondaryButtonStartDrag.get(),
                "onMouseDrag secondary start isn't accurate.");
        Assertions.assertNull(secondaryButtonDragging.get(),
                "onMouseDrag secondary isn't accurate.");
        Assertions.assertNull(secondaryButtonEndDrag.get(),
                "onMouseDrag secondary end isn't accurate.");
    }

}
