package piko.module.hud;

import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Mouse;
import piko.animation.Animation;
import piko.font.IFont;
import piko.gui.Theme;
import piko.module.HudModule;
import piko.render.ColorUtil;
import piko.render.RenderUtil;
import piko.setting.BooleanSetting;
import piko.setting.ColorSetting;
import piko.setting.NumberSetting;
import piko.util.ClickCounters;

/**
 * PvP style keystroke overlay.
 *
 * <pre>
 *     W
 * A   S   D
 *  LMB RMB
 * SPACE
 * </pre>
 *
 * <p>Each key fades between the released and pressed colour instead of snapping, which is
 * what makes the overlay readable during fast combos.</p>
 */
public class KeystrokesHud extends HudModule {

    private static final int W = 0;
    private static final int A = 1;
    private static final int S = 2;
    private static final int D = 3;
    private static final int LMB = 4;
    private static final int RMB = 5;
    private static final int SPACE = 6;

    private final BooleanSetting showWasd;
    private final BooleanSetting showLeft;
    private final BooleanSetting showRight;
    private final BooleanSetting showSpace;
    private final BooleanSetting mouseCps;
    private final NumberSetting keySize;
    private final NumberSetting keySpacing;
    private final NumberSetting corner;
    private final NumberSetting backgroundAlpha;
    private final ColorSetting pressedColor;
    private final ColorSetting releasedColor;

    private final Animation[] press = new Animation[7];

    public KeystrokesHud() {
        super("Keystrokes", "Shows the keys you are pressing", 0.015F, 0.55F);
        showWasd = settings.add(new BooleanSetting("WASD", true));
        showLeft = settings.add(new BooleanSetting("LMB", true));
        showRight = settings.add(new BooleanSetting("RMB", true));
        showSpace = settings.add(new BooleanSetting("Spacebar", true));
        mouseCps = settings.add(new BooleanSetting("CPS In Mouse Buttons", true));
        keySize = settings.add(new NumberSetting("Key Size", 16.0D, 10.0D, 26.0D, 1.0D));
        keySpacing = settings.add(new NumberSetting("Key Spacing", 2.0D, 0.0D, 8.0D, 1.0D));
        corner = settings.add(new NumberSetting("Rounded Corners", 3.0D, 0.0D, 8.0D, 0.5D));
        backgroundAlpha = settings.add(new NumberSetting("Background Opacity", 0.6D, 0.0D, 1.0D, 0.05D));
        pressedColor = settings.add(new ColorSetting("Pressed Color", 0xFF55CCFF));
        releasedColor = settings.add(new ColorSetting("Unpressed Color", 0xFF14171D));
        enableTextColor(0xFFFFFFFF);
        enableFont();

        for (int i = 0; i < press.length; i++) {
            press[i] = new Animation(0.0F, 110.0F);
        }
    }

    private float size() {
        return keySize.getFloat();
    }

    private float spacing() {
        return keySpacing.getFloat();
    }

    private float rowWidth() {
        return size() * 3 + spacing() * 2;
    }

    @Override
    public float getWidth() {
        return rowWidth();
    }

    @Override
    public float getHeight() {
        float height = 0;
        if (showWasd.get()) {
            height += size() * 2 + spacing();
        }
        if (showLeft.get() || showRight.get()) {
            height += size() + spacing();
        }
        if (showSpace.get()) {
            height += size() * 0.55F + spacing();
        }
        return Math.max(height, size());
    }

    private boolean isDown(int index, boolean editing) {
        if (editing) {
            return index == W || index == LMB;
        }
        switch (index) {
            case W:
                return down(mc.gameSettings.keyBindForward);
            case A:
                return down(mc.gameSettings.keyBindLeft);
            case S:
                return down(mc.gameSettings.keyBindBack);
            case D:
                return down(mc.gameSettings.keyBindRight);
            case LMB:
                return Mouse.isButtonDown(0);
            case RMB:
                return Mouse.isButtonDown(1);
            default:
                return down(mc.gameSettings.keyBindJump);
        }
    }

    private static boolean down(KeyBinding binding) {
        return binding != null && binding.isKeyDown();
    }

    @Override
    protected void render(boolean editing) {
        float size = size();
        float spacing = spacing();
        float cursorY = 0;

        if (showWasd.get()) {
            drawKey(W, size + spacing, cursorY, size, size, "W", editing);
            cursorY += size + spacing;
            drawKey(A, 0, cursorY, size, size, "A", editing);
            drawKey(S, size + spacing, cursorY, size, size, "S", editing);
            drawKey(D, (size + spacing) * 2, cursorY, size, size, "D", editing);
            cursorY += size + spacing;
        }

        if (showLeft.get() || showRight.get()) {
            float mouseWidth = showLeft.get() && showRight.get()
                    ? (rowWidth() - spacing) / 2.0F
                    : rowWidth();
            float mouseX = 0;
            if (showLeft.get()) {
                drawKey(LMB, mouseX, cursorY, mouseWidth, size, label("LMB", ClickCounters.left(), editing, 8), editing);
                mouseX += mouseWidth + spacing;
            }
            if (showRight.get()) {
                drawKey(RMB, mouseX, cursorY, mouseWidth, size, label("RMB", ClickCounters.right(), editing, 3), editing);
            }
            cursorY += size + spacing;
        }

        if (showSpace.get()) {
            drawKey(SPACE, 0, cursorY, rowWidth(), size * 0.55F, "____", editing);
        }
    }

    private String label(String name, int cps, boolean editing, int demoCps) {
        if (!mouseCps.get()) {
            return name;
        }
        return String.valueOf(editing ? demoCps : cps);
    }

    private void drawKey(int index, float x, float y, float width, float height, String text, boolean editing) {
        press[index].setTarget(isDown(index, editing) ? 1.0F : 0.0F);
        float progress = press[index].getValue();

        int background = ColorUtil.mix(
                ColorUtil.alpha(releasedColor.get(), backgroundAlpha.getFloat()),
                ColorUtil.alpha(pressedColor.get(), Math.min(1.0F, backgroundAlpha.getFloat() + 0.45F)),
                progress);
        RenderUtil.drawRoundedRect(x, y, width, height, corner.getFloat(), background);
        RenderUtil.drawRoundedBorder(x, y, width, height, corner.getFloat(), 1.0F,
                ColorUtil.alpha(Theme.accent(), 0.15F + 0.55F * progress));

        IFont font = font();
        int textColor = ColorUtil.mix(getTextColor(), 0xFF0B0C0F, progress * 0.85F);
        font.drawString(text, x + (width - font.getStringWidth(text)) / 2.0F,
                y + (height - font.getHeight()) / 2.0F + 0.5F, textColor);
    }
}
