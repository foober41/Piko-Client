package piko.gui;

import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import piko.PikoClient;
import piko.animation.Animation;
import piko.font.FontManager;
import piko.gui.components.PikoButton;
import piko.render.ColorUtil;
import piko.render.RenderUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Piko branded replacement for the 1.8.9 main menu.
 *
 * <p>The background animation is a handful of drifting dots whose positions are derived
 * from the clock, so it costs a few dozen triangles per frame and allocates nothing.</p>
 */
public class PikoMainMenu extends GuiScreen {

    private static final int DOT_COUNT = 28;

    private final List<PikoButton> buttons = new ArrayList<PikoButton>();
    private final Animation intro = new Animation(0.0F, 400.0F);
    private final float[] dotSeedX = new float[DOT_COUNT];
    private final float[] dotSeedY = new float[DOT_COUNT];
    private final float[] dotSpeed = new float[DOT_COUNT];
    private final float[] dotSize = new float[DOT_COUNT];

    public PikoMainMenu() {
        java.util.Random random = new java.util.Random(0x50494B4FL);
        for (int i = 0; i < DOT_COUNT; i++) {
            dotSeedX[i] = random.nextFloat();
            dotSeedY[i] = random.nextFloat();
            dotSpeed[i] = 0.15F + random.nextFloat() * 0.5F;
            dotSize[i] = 0.6F + random.nextFloat() * 1.4F;
        }
    }

    @Override
    public void initGui() {
        buttons.clear();
        intro.snapTo(0.0F);
        intro.setTarget(1.0F);

        float buttonWidth = 150.0F;
        float buttonX = (width - buttonWidth) / 2.0F;
        float buttonY = height / 2.0F - 12.0F;

        buttons.add(button("MULTIPLAYER", buttonX, buttonY, buttonWidth,
                () -> mc.displayGuiScreen(new GuiMultiplayer(this))));
        buttons.add(button("SINGLEPLAYER", buttonX, buttonY + 22.0F, buttonWidth,
                () -> mc.displayGuiScreen(new GuiSelectWorld(this))));

        PikoButton settings = button("PIKO SETTINGS", buttonX, buttonY + 44.0F, buttonWidth,
                () -> mc.displayGuiScreen(new PikoMenu()));
        settings.accented();
        buttons.add(settings);

        buttons.add(button("OPTIONS", buttonX, buttonY + 66.0F, buttonWidth / 2.0F - 3.0F,
                () -> mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings))));
        PikoButton quit = new PikoButton("QUIT", () -> mc.shutdown());
        quit.setBounds(buttonX + buttonWidth / 2.0F + 3.0F, buttonY + 66.0F, buttonWidth / 2.0F - 3.0F, 18.0F);
        buttons.add(quit);
    }

    private PikoButton button(String text, float x, float y, float buttonWidth, PikoButton.Action action) {
        PikoButton button = new PikoButton(text, action);
        button.setBounds(x, y, buttonWidth, 18.0F);
        return button;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float progress = intro.getValue();

        RenderUtil.drawGradientRect(0, 0, width, height, Theme.BLACK, 0xFF0D1017);
        drawDots();

        // Soft accent glow behind the wordmark.
        RenderUtil.drawRoundedRect(width / 2.0F - 90.0F, height / 4.0F - 6.0F, 180.0F, 46.0F, 10.0F,
                ColorUtil.alpha(Theme.accent(), 0.05F * progress));

        String title = "PIKO";
        float titleWidth = FontManager.title().getStringWidth(title);
        FontManager.title().drawString(title, (width - titleWidth) / 2.0F, height / 4.0F,
                ColorUtil.fade(Theme.TEXT, progress));
        RenderUtil.drawRoundedRect((width - 54.0F) / 2.0F, height / 4.0F + 28.0F, 54.0F, 2.0F, 1.0F,
                ColorUtil.alpha(Theme.accent(), progress));

        String tagline = "COMPETITIVE PVP CLIENT";
        FontManager.small().drawString(tagline, (width - FontManager.small().getStringWidth(tagline)) / 2.0F,
                height / 4.0F + 34.0F, ColorUtil.fade(Theme.TEXT_SECONDARY, progress));

        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).draw(mouseX, mouseY);
        }

        FontManager.small().drawString("Piko Client " + PikoClient.VERSION, 6.0F, height - 20.0F, Theme.TEXT_SECONDARY);
        FontManager.small().drawString("Minecraft " + PikoClient.MINECRAFT_VERSION, 6.0F, height - 11.0F,
                Theme.TEXT_DISABLED);

        String profile = PikoClient.getInstance().getProfileManager().getActiveProfileName();
        String right = "Profile: " + profile;
        FontManager.small().drawString(right, width - FontManager.small().getStringWidth(right) - 6.0F,
                height - 11.0F, Theme.TEXT_DISABLED);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawDots() {
        float time = (System.currentTimeMillis() % 100000L) / 1000.0F;
        for (int i = 0; i < DOT_COUNT; i++) {
            float x = dotSeedX[i] * width;
            float y = ((dotSeedY[i] - time * dotSpeed[i] * 0.02F) % 1.0F + 1.0F) % 1.0F * height;
            float alpha = 0.10F + 0.10F * (float) Math.sin(time * dotSpeed[i] + i);
            RenderUtil.drawCircle(x, y, dotSize[i], ColorUtil.alpha(Theme.accent(), alpha));
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (int i = 0; i < buttons.size(); i++) {
            if (buttons.get(i).mouseClicked(mouseX, mouseY, mouseButton)) {
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
