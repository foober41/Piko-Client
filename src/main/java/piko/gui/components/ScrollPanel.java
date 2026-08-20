package piko.gui.components;

import net.minecraft.client.renderer.GlStateManager;
import piko.gui.Theme;
import piko.render.ColorUtil;
import piko.render.RenderUtil;
import piko.util.MathUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Clipped, scrollable container.
 *
 * <p>Children keep absolute coordinates inside the content space; the panel only shifts
 * them while drawing and feeds the shifted mouse position back into the children, which
 * keeps hit testing correct without every widget knowing about scrolling.</p>
 */
public class ScrollPanel extends Component {

    private final List<Component> children = new ArrayList<Component>();
    private float scroll;
    private float smoothScroll;
    private float contentHeight;

    public void clear() {
        children.clear();
        contentHeight = 0;
    }

    public void addChild(Component child) {
        children.add(child);
    }

    public List<Component> getChildren() {
        return children;
    }

    public void setContentHeight(float contentHeight) {
        this.contentHeight = contentHeight;
        clampScroll();
    }

    public float getScroll() {
        return smoothScroll;
    }

    public void resetScroll() {
        scroll = 0;
        smoothScroll = 0;
    }

    /** One wheel notch moves roughly one and a half rows. */
    public void scroll(int notches) {
        if (notches == 0 || contentHeight <= height) {
            return;
        }
        scroll -= notches * 18.0F;
        clampScroll();
    }

    private void clampScroll() {
        float max = Math.max(0.0F, contentHeight - height);
        scroll = MathUtil.clamp(scroll, 0.0F, max);
    }

    @Override
    public void draw(float mouseX, float mouseY) {
        if (!visible) {
            return;
        }
        // Ease towards the target offset so the wheel feels smooth instead of jumping.
        smoothScroll += (scroll - smoothScroll) * 0.35F;
        if (Math.abs(scroll - smoothScroll) < 0.1F) {
            smoothScroll = scroll;
        }

        RenderUtil.enableScissor(x, y, width, height);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, -smoothScroll, 0.0F);
        float childMouseY = mouseY + smoothScroll;
        for (int i = 0; i < children.size(); i++) {
            Component child = children.get(i);
            child.update(mouseX, childMouseY);
            child.draw(mouseX, childMouseY);
        }
        GlStateManager.popMatrix();
        RenderUtil.disableScissor();

        drawScrollbar();
    }

    private void drawScrollbar() {
        if (contentHeight <= height) {
            return;
        }
        float trackX = x + width - 2.5F;
        float ratio = height / contentHeight;
        float barHeight = Math.max(16.0F, height * ratio);
        float progress = smoothScroll / (contentHeight - height);
        float barY = y + (height - barHeight) * progress;
        RenderUtil.drawRoundedRect(trackX, y, 2.0F, height, 1.0F, ColorUtil.alpha(0xFF000000, 0.25F));
        RenderUtil.drawRoundedRect(trackX, barY, 2.0F, barHeight, 1.0F, ColorUtil.alpha(Theme.accent(), 0.7F));
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, int button) {
        if (!visible || !isHovered(mouseX, mouseY)) {
            return false;
        }
        float childMouseY = mouseY + smoothScroll;
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).mouseClicked(mouseX, childMouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void mouseReleased(float mouseX, float mouseY, int button) {
        float childMouseY = mouseY + smoothScroll;
        for (int i = 0; i < children.size(); i++) {
            children.get(i).mouseReleased(mouseX, childMouseY, button);
        }
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).keyTyped(typedChar, keyCode)) {
                return true;
            }
        }
        return false;
    }
}
