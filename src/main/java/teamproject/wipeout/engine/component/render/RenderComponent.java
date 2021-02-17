package teamproject.wipeout.engine.component.render;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import teamproject.wipeout.engine.component.GameComponent;

public class RenderComponent implements GameComponent {
    
    private List<Renderable> renderables;

    public RenderComponent() {
        this.renderables = new ArrayList<Renderable>();
    }

    /**
     * Takes any number of renderable objects to render for this object to render.
     * @param newRenderables An arbitrary number of renderable objects.
     */
    public RenderComponent(Renderable... newRenderables) {
        renderables = List.of(newRenderables);
    }

    /**
     * Checks to see if this component has a given renderable.
     * @param renderableObj The renderable to check.
     * @return Boolean representing whether this component has the given renderable.
     */
    public boolean hasRenderable(Renderable renderableObj) {
        return renderables.contains(renderableObj);
    }

    /**
     * Adds the given renderable to the list of renderables.
     * @param renderableObj The renderable to add.
     */
    public void addRenderable(Renderable renderableObj) {
        renderables.add(renderableObj);
    }

    /**
     * Removes the given renderable from the list of renderables.
     * @param renderableObj The renderable to remove.
     */
    public void removeRenderable(Renderable renderableObj) {
        renderables.remove(renderableObj);
    }
    
    /**
     * Returns the maximum width of this RenderComponent
     * based on the width of all Renderables it stores
     * 
     * @return The width of this RenderComponent
     */
    public double getWidth() {
        double width = 0;

        for (Renderable r : renderables) {
            double rWidth = r.getWidth();
            
            if (rWidth > width) {
                width = rWidth;
            }
        }

        return width;
    }

    /**
     * Returns the maximum height of this RenderComponent
     * based on the height of all Renderables it stores
     * 
     * @return The height of this RenderComponent
     */
    public double getHeight() {
        double height = 0;

        for (Renderable r : renderables) {
            double rHeight = r.getHeight();
            
            if (rHeight > height) {
                height = rHeight;
            }
        }

        return height;
    }

    /**
     * Calls render on all stored renderables.
     * @param gc Graphics context to render to the screen.
     * @param x X co-ordinate.
     * @param y Y co-ordinate.
     * @param scale The scale to render at (1 for normal size)
     */
    public void render(GraphicsContext gc, double x, double y, double scale) {
        for (Renderable r : renderables) {
            r.render(gc, x, y, scale);
        }
    }

    public String getType() {
        return "render";
    }


}