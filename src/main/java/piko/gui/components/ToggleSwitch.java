package piko.gui.components;

import piko.animation.Animation;
import piko.gui.Theme;
import piko.render.ColorUtil;
import piko.render.RenderUtil;

/** Animated on/off switch bound to a boolean supplier and consumer. */
public class ToggleSwitch extends Component {

    public interface State {
        boolean get();

        void set(boolean value);
    }

    private final State state;
    private final Animation knob;

    public ToggleSwitch(State state) {
        this.state = state;
        this.knob = new Animation(state.get() ? 1.0F : 0.0F, Theme.animationDuration(180.0F));
        this.width = 22.0F;
        this.height = 11.0F;
    }

    @Override
    public void draw(float mouseX, float mouseY) {
        if (!visible) {
            return;
        }
        knob.setTarget(state.get() ? 1.0F : 0.0F);
        float progress = knob.getValue();

        int track = ColorUtil.mix(Theme.OFF, Theme.accent(), progress);
        RenderUtil.drawRoundedRect(x, y, width, height, height / 2.0F, ColorUtil.alpha(track, 0.35F + 0.5F * progress));
        RenderUtil.drawRoundedBorder(x, y, width, height, height / 2.0F, 1.0F, ColorUtil.alpha(track, 0.9F));

        float radius = height / 2.0F - 1.5F;
        float knobX = x + 1.5F + radius + (width - height) * progress;
        RenderUtil.drawCircle(knobX, y + height / 2.0F, radius, ColorUtil.mix(Theme.TEXT_SECONDARY, 0xFFFFFFFF, progress));
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, int button) {
        if (!visible || button != 0 || !isHovered(mouseX, mouseY)) {
            return false;
        }
        state.set(!state.get());
        return true;
    }
}
