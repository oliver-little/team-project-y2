package teamproject.wipeout.engine.component;

import teamproject.wipeout.engine.input.InputMouseAction;

public class Clickable implements GameComponent {

    public InputMouseAction onLeftClick;
    public InputMouseAction onMiddleClick;
    public InputMouseAction onRightClick;

    public Clickable(InputMouseAction onLeftClick, InputMouseAction onMiddleClick, InputMouseAction onRightClick) {
        this.onLeftClick = onLeftClick;
        this.onMiddleClick = onMiddleClick;
        this.onRightClick = onRightClick;
    }

    public String getType() {
        return "clickable";
    }
}
