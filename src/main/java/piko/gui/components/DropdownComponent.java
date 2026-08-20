package piko.gui.components;

import piko.animation.Animation;
import piko.font.FontManager;
import piko.gui.Theme;
import piko.render.ColorUtil;
import piko.render.RenderUtil;
import piko.setting.ModeSetting;

/** Dropdown for {@link ModeSetting} values. Expands in place with an animated height. */
public class DropdownComponent extends Component {

    private static final float ROW_HEIGHT = 12.0F;

    private final ModeSetting setting;
    private final Animation expansion = new Animation(0.0F, Theme.animationDuration(160.0F));
    private boolean open;

    public DropdownComponent(ModeSetting setting) {
        this.setting = setting;
        this.height = 26.0F;
    }

    public boolean isOpen() {
        return open;
    }

    /** Height including the expanded list, so the parent panel can reserve space. */
    @Override
    public float getHeight() {
        return 26.0F + expansion.getValue() * setting.getModes().length * ROW_HEIGHT;
    }

    @Override
    public void draw(float mouseX, float mouseY) {
        if (!visible) {
            return;
        }
        expansion.setTarget(open ? 1.0F : 0.0F);

        FontManager.regular().drawString(setting.getName(), x, y, Theme.TEXT);

        float boxY = y + 11.0F;
        boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= boxY && mouseY <= boxY + 13.0F;
        RenderUtil.drawRoundedRect(x, boxY, width, 13.0F, Theme.cornerRadius(),
                hovered ? Theme.PANEL_HOVER : Theme.PANEL_LIGHT);
        RenderUtil.drawRoundedBorder(x, boxY, width, 13.0F, Theme.cornerRadius(), 1.0F,
                ColorUtil.alpha(Theme.accent(), hovered ? 0.5F : 0.2F));
        FontManager.regular().drawString(setting.get(), x + 5.0F, boxY + 3.0F, Theme.TEXT);
        FontManager.regular().drawString(open ? "-" : "+", x + width - 9.0F, boxY + 3.0F, Theme.accent());

        float progress = expansion.getValue();
        if (progress <= 0.01F) {
            return;
        }

        String[] modes = setting.getModes();
        float listHeight = modes.length * ROW_HEIGHT * progress;
        float listY = boxY + 14.0F;
        RenderUtil.drawRoundedRect(x, listY, width, listHeight, Theme.cornerRadius(), Theme.PANEL);
        RenderUtil.drawRoundedBorder(x, listY, width, listHeight, Theme.cornerRadius(), 1.0F,
                ColorUtil.alpha(Theme.accent(), 0.3F));

        RenderUtil.enableScissor(x, listY, width, listHeight);
        for (int i = 0; i < modes.length; i++) {
            float rowY = listY + i * ROW_HEIGHT;
            boolean rowHovered = mouseX >= x && mouseX <= x + width && mouseY >= rowY && mouseY <= rowY + ROW_HEIGHT;
            boolean selected = i == setting.getIndex();
            if (rowHovered) {
                RenderUtil.drawRect(x + 1, rowY, width - 2, ROW_HEIGHT, ColorUtil.alpha(Theme.accent(), 0.14F));
            }
            FontManager.regular().drawString(modes[i], x + 5.0F, rowY + 2.0F,
                    selected ? Theme.accent() : Theme.TEXT_SECONDARY);
        }
        RenderUtil.disableScissor();
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, int button) {
        if (!visible || button != 0) {
            return false;
        }
        float boxY = y + 11.0F;
        if (mouseX >= x && mouseX <= x + width && mouseY >= boxY && mouseY <= boxY + 13.0F) {
            open = !open;
            return true;
        }
        if (open) {
            String[] modes = setting.getModes();
            float listY = boxY + 14.0F;
            for (int i = 0; i < modes.length; i++) {
                float rowY = listY + i * ROW_HEIGHT;
                if (mouseX >= x && mouseX <= x + width && mouseY >= rowY && mouseY <= rowY + ROW_HEIGHT) {
                    setting.setIndex(i);
                    open = false;
                    return true;
                }
            }
            open = false;
        }
        return false;
    }
}
