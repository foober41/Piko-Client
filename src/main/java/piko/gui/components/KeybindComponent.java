package piko.gui.components;

import org.lwjgl.input.Keyboard;
import piko.font.FontManager;
import piko.gui.Theme;
import piko.render.ColorUtil;
import piko.render.RenderUtil;
import piko.setting.KeybindSetting;

/** Click to listen, then press any key. Escape clears the binding. */
public class KeybindComponent extends Component {

    private final KeybindSetting setting;
    private boolean listening;

    public KeybindComponent(KeybindSetting setting) {
        this.setting = setting;
        this.height = 16.0F;
    }

    public boolean isListening() {
        return listening;
    }

    @Override
    public void draw(float mouseX, float mouseY) {
        if (!visible) {
            return;
        }
        FontManager.regular().drawString(setting.getName(), x, y + 3.0F, Theme.TEXT);

        String label = listening ? "PRESS A KEY" : setting.displayValue();
        float boxWidth = Math.max(44.0F, FontManager.regular().getStringWidth(label) + 12.0F);
        float boxX = x + width - boxWidth;
        boolean hovered = mouseX >= boxX && mouseX <= x + width && mouseY >= y && mouseY <= y + 13.0F;

        RenderUtil.drawRoundedRect(boxX, y, boxWidth, 13.0F, Theme.cornerRadius(),
                hovered || listening ? Theme.PANEL_HOVER : Theme.PANEL_LIGHT);
        RenderUtil.drawRoundedBorder(boxX, y, boxWidth, 13.0F, Theme.cornerRadius(), 1.0F,
                ColorUtil.alpha(Theme.accent(), listening ? 0.9F : (hovered ? 0.5F : 0.2F)));
        FontManager.regular().drawString(label,
                boxX + (boxWidth - FontManager.regular().getStringWidth(label)) / 2.0F, y + 3.0F,
                listening ? Theme.accent() : Theme.TEXT);
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, int button) {
        if (!visible) {
            return false;
        }
        float boxWidth = Math.max(44.0F, FontManager.regular().getStringWidth(setting.displayValue()) + 12.0F);
        float boxX = x + width - boxWidth;
        if (mouseX >= boxX && mouseX <= x + width && mouseY >= y && mouseY <= y + 13.0F) {
            if (button == 0) {
                listening = !listening;
            } else if (button == 1) {
                setting.set(Keyboard.KEY_NONE);
                listening = false;
            }
            return true;
        }
        if (listening && button >= 0) {
            // Any mouse button pressed elsewhere while listening becomes the binding.
            setting.set(piko.util.KeyUtil.fromMouseButton(button));
            listening = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {
        if (!listening) {
            return false;
        }
        setting.set(keyCode == Keyboard.KEY_ESCAPE ? Keyboard.KEY_NONE : keyCode);
        listening = false;
        return true;
    }
}
