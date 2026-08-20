package piko.gui.components;

import piko.font.FontManager;
import piko.gui.Theme;
import piko.setting.BooleanSetting;

/** Label plus toggle switch row for a {@link BooleanSetting}. */
public class BooleanRow extends Component {

    private final BooleanSetting setting;
    private final ToggleSwitch toggle;

    public BooleanRow(final BooleanSetting setting) {
        this.setting = setting;
        this.toggle = new ToggleSwitch(new ToggleSwitch.State() {
            @Override
            public boolean get() {
                return setting.get();
            }

            @Override
            public void set(boolean value) {
                setting.set(value);
            }
        });
        this.height = 16.0F;
    }

    @Override
    public void draw(float mouseX, float mouseY) {
        if (!visible) {
            return;
        }
        FontManager.regular().drawString(setting.getName(), x, y + 2.5F, Theme.TEXT);
        toggle.setPosition(x + width - toggle.getWidth(), y + 1.0F);
        toggle.draw(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, int button) {
        if (!visible || button != 0 || !isHovered(mouseX, mouseY)) {
            return false;
        }
        // The whole row is clickable, not just the small switch.
        setting.toggle();
        return true;
    }
}
