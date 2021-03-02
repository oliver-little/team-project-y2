package teamproject.wipeout.engine.component.ui;

import teamproject.wipeout.engine.component.GameComponent;

public class UIComponent implements GameComponent {
    
    private DialogUIComponent node;

    public UIComponent(DialogUIComponent node) {
        this.node = node;
    }

    public DialogUIComponent getUI() {
        return this.node;
    }

    public String getType() {
        return "ui";
    }
}
