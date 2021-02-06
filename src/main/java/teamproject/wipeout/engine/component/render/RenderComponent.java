package teamproject.wipeout.engine.component.render;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import teamproject.wipeout.engine.component.GameComponent;

public class RenderComponent implements GameComponent {
    
    private List<Renderable> renderables;

    public RenderComponent() {

        renderables = new ArrayList<Renderable>();

    }

    public RenderComponent(Renderable... newRenderables) {
        renderables = List.of(newRenderables);
    }

    public boolean hasRenderable(Renderable renderableObj) {
        return renderables.contains(renderableObj);
    }

    public void addRenderable(Renderable renderableObj) {
        renderables.add(renderableObj);
    }

    public void removeRenderable(Renderable renderableObj) {
        renderables.remove(renderableObj);
    }

    public void render(GraphicsContext gc, double x, double y) {
        for (Renderable r : renderables) {
            r.render(gc, x, y);
        }
    }

    public String getType() {
        return "render";
    }


}
