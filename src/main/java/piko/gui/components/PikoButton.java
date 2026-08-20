package piko.gui.components;

import piko.animation.Animation;
import piko.font.FontManager;
import piko.gui.Theme;
import piko.render.ColorUtil;
import piko.render.RenderUtil;

/** Rounded button with a smooth hover fade. */
public class PikoButton extends Component {

    public interface Action {
        void run();
    }

    private String text;
    private final Action action;
    private final Animation hover = new Animation(0.0F, Theme.animationDuration(140.0F));
    private boolean accented;
    private boolean enabled = true;

    public PikoButton(String text, Action action) {
        this.text = text;
        this.action = action;
    }

    public PikoButton accented() {
        this.accented = true;
        return this;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void draw(float mouseX, float mouseY) {
        if (!visible) {
            return;
        }
        boolean hovered = enabled && isHovered(mouseX, mouseY);
        hover.setTarget(hovered ? 1.0F : 0.0F);
        float progress = hover.getValue();

        int base = accented ? ColorUtil.alpha(Theme.accent(), 0.16F) : Theme.PANEL_LIGHT;
        int hoverColor = accented ? ColorUtil.alpha(Theme.accent(), 0.32F) : Theme.PANEL_HOVER;
        RenderUtil.drawRoundedRect(x, y, width, height, Theme.cornerRadius(), ColorUtil.mix(base, hoverColor, progress));
        RenderUtil.drawRoundedBorder(x, y, width, height, Theme.cornerRadius(), 1.0F,
                ColorUtil.alpha(Theme.accent(), accented ? 0.55F + 0.35F * progress : 0.12F + 0.38F * progress));

        int textColor = !enabled ? Theme.TEXT_DISABLED
                : (accented ? Theme.accent() : ColorUtil.mix(Theme.TEXT_SECONDARY, Theme.TEXT, progress));
        FontManager.regular().drawString(text,
                x + (width - FontManager.regular().getStringWidth(text)) / 2.0F,
                y + (height - FontManager.regular().getHeight()) / 2.0F + 1.0F,
                textColor);
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, int button) {
        if (!visible || !enabled || button != 0 || !isHovered(mouseX, mouseY)) {
            return false;
        }
        if (action != null) {
            action.run();
        }
        return true;
    }
}
