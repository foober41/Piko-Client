package piko.gui.components;

import org.lwjgl.input.Keyboard;
import piko.font.FontManager;
import piko.gui.Theme;
import piko.render.ColorUtil;
import piko.render.RenderUtil;

/** Single line text input used for search, profile names and string settings. */
public class TextFieldComponent extends Component {

    public interface ChangeHandler {
        void onTextChanged(String text);
    }

    private final String placeholder;
    private String text = "";
    private boolean focused;
    private int maxLength = 48;
    private ChangeHandler changeHandler;
    private String label;

    public TextFieldComponent(String placeholder) {
        this.placeholder = placeholder;
        this.height = 16.0F;
    }

    public TextFieldComponent withLabel(String label) {
        this.label = label;
        this.height = 26.0F;
        return this;
    }

    public TextFieldComponent onChange(ChangeHandler handler) {
        this.changeHandler = handler;
        return this;
    }

    public TextFieldComponent maxLength(int maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public boolean isFocused() {
        return focused;
    }

    @Override
    public void draw(float mouseX, float mouseY) {
        if (!visible) {
            return;
        }
        float boxY = y;
        if (label != null) {
            FontManager.regular().drawString(label, x, y, Theme.TEXT);
            boxY = y + 11.0F;
        }
        RenderUtil.drawRoundedRect(x, boxY, width, 14.0F, Theme.cornerRadius(), Theme.PANEL_LIGHT);
        RenderUtil.drawRoundedBorder(x, boxY, width, 14.0F, Theme.cornerRadius(), 1.0F,
                ColorUtil.alpha(Theme.accent(), focused ? 0.85F : 0.18F));

        String shown = text.isEmpty() && !focused ? placeholder : text;
        int color = text.isEmpty() && !focused ? Theme.TEXT_DISABLED : Theme.TEXT;
        RenderUtil.enableScissor(x + 2, boxY, width - 4, 14.0F);
        FontManager.regular().drawString(shown, x + 5.0F, boxY + 3.5F, color);
        if (focused && (System.currentTimeMillis() / 500L) % 2 == 0) {
            float caretX = x + 5.0F + FontManager.regular().getStringWidth(text) + 1.0F;
            RenderUtil.drawRect(caretX, boxY + 3.0F, 1.0F, 9.0F, Theme.accent());
        }
        RenderUtil.disableScissor();
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, int button) {
        if (!visible) {
            return false;
        }
        boolean inside = isHovered(mouseX, mouseY);
        focused = inside && button == 0;
        return inside;
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {
        if (!focused) {
            return false;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            focused = false;
            return true;
        }
        if (keyCode == Keyboard.KEY_RETURN) {
            focused = false;
            return true;
        }
        if (keyCode == Keyboard.KEY_BACK) {
            if (!text.isEmpty()) {
                text = text.substring(0, text.length() - 1);
                fireChanged();
            }
            return true;
        }
        if (net.minecraft.util.ChatAllowedCharacters.isAllowedCharacter(typedChar) && text.length() < maxLength) {
            text += typedChar;
            fireChanged();
            return true;
        }
        return true;
    }

    private void fireChanged() {
        if (changeHandler != null) {
            changeHandler.onTextChanged(text);
        }
    }
}
