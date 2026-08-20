package piko.gui.components;

import piko.font.FontManager;
import piko.gui.Theme;
import piko.render.ColorUtil;
import piko.render.RenderUtil;
import piko.setting.NumberSetting;
import piko.util.MathUtil;

/** Slider bound directly to a {@link NumberSetting}; dragging changes the value live. */
public class SliderComponent extends Component {

    private static final float TRACK_HEIGHT = 3.0F;

    private final NumberSetting setting;
    private boolean dragging;

    public SliderComponent(NumberSetting setting) {
        this.setting = setting;
        this.height = 24.0F;
    }

    @Override
    public void draw(float mouseX, float mouseY) {
        if (!visible) {
            return;
        }
        FontManager.regular().drawString(setting.getName(), x, y, Theme.TEXT);
        String value = setting.displayValue();
        FontManager.regular().drawString(value, x + width - FontManager.regular().getStringWidth(value), y, Theme.accent());

        float trackY = y + 15.0F;
        RenderUtil.drawRoundedRect(x, trackY, width, TRACK_HEIGHT, TRACK_HEIGHT / 2.0F, Theme.OFF);

        float progress = (float) setting.getProgress();
        float filled = width * progress;
        RenderUtil.drawRoundedRect(x, trackY, filled, TRACK_HEIGHT, TRACK_HEIGHT / 2.0F, Theme.accent());
        RenderUtil.drawCircle(x + filled, trackY + TRACK_HEIGHT / 2.0F, dragging ? 4.5F : 3.5F, 0xFFFFFFFF);
        RenderUtil.drawCircle(x + filled, trackY + TRACK_HEIGHT / 2.0F, dragging ? 3.0F : 2.2F,
                ColorUtil.alpha(Theme.accent(), 1.0F));
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, int button) {
        if (!visible || button != 0) {
            return false;
        }
        // Generous vertical hit box so the thin track is easy to grab.
        if (mouseX >= x - 4 && mouseX <= x + width + 4 && mouseY >= y + 8 && mouseY <= y + 24) {
            dragging = true;
            apply(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public void update(float mouseX, float mouseY) {
        if (dragging) {
            apply(mouseX);
        }
    }

    private void apply(float mouseX) {
        setting.setProgress(MathUtil.clamp((mouseX - x) / width, 0.0F, 1.0F));
    }

    @Override
    public void mouseReleased(float mouseX, float mouseY, int button) {
        dragging = false;
    }
}
