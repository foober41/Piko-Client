package piko.gui.hud;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import piko.PikoClient;
import piko.font.FontManager;
import piko.gui.Theme;
import piko.gui.components.PikoButton;
import piko.module.HudModule;
import piko.module.gui.HudEditorModule;
import piko.render.ColorUtil;
import piko.render.RenderUtil;
import piko.setting.SettingManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Drag and drop HUD layout editor.
 *
 * <p>Elements can be moved, resized, toggled and reset. While dragging, Piko snaps to the
 * screen edges, the screen centre and the edges of the other HUD elements, and draws a
 * temporary guide line for whichever alignment is currently active.</p>
 */
public class HudEditorScreen extends GuiScreen {

    private static final float HANDLE_SIZE = 5.0F;

    private final GuiScreen parent;
    private final List<PikoButton> buttons = new ArrayList<PikoButton>();

    private HudModule dragging;
    private HudModule resizing;
    private float dragOffsetX;
    private float dragOffsetY;
    private float resizeStartScale;
    private float resizeStartDistance;

    private boolean snapping = true;
    private boolean snappingInitialised;
    private boolean showCenterGuideX;
    private boolean showCenterGuideY;
    private float guideX = -1;
    private float guideY = -1;

    public HudEditorScreen(GuiScreen parent) {
        this.parent = parent;
    }

    private HudEditorModule options() {
        return PikoClient.getInstance().getModuleManager().getModule(HudEditorModule.class);
    }

    private float snapDistance() {
        HudEditorModule options = options();
        return options == null ? 4.0F : options.getSnapDistance();
    }

    private boolean guidesEnabled() {
        HudEditorModule options = options();
        return options == null || options.areGuidesEnabled();
    }

    @Override
    public void initGui() {
        buttons.clear();

        if (!snappingInitialised) {
            HudEditorModule options = options();
            snapping = options == null || options.isSnapEnabled();
            snappingInitialised = true;
        }

        float buttonY = height - 24.0F;
        PikoButton snap = new PikoButton(snapLabel(), null) {
            @Override
            public boolean mouseClicked(float mouseX, float mouseY, int button) {
                if (button == 0 && isHovered(mouseX, mouseY)) {
                    snapping = !snapping;
                    setText(snapLabel());
                    return true;
                }
                return false;
            }
        };
        snap.setBounds(width / 2.0F - 122.0F, buttonY, 78.0F, 16.0F);
        buttons.add(snap);

        PikoButton resetAll = new PikoButton("RESET ALL", () -> {
            List<HudModule> hudModules = PikoClient.getInstance().getModuleManager().getHudModules();
            for (int i = 0; i < hudModules.size(); i++) {
                hudModules.get(i).resetPosition();
                hudModules.get(i).getScaleSetting().reset();
            }
            SettingManager.markDirty();
        });
        resetAll.setBounds(width / 2.0F - 40.0F, buttonY, 78.0F, 16.0F);
        buttons.add(resetAll);

        PikoButton done = new PikoButton("DONE", () -> mc.displayGuiScreen(parent));
        done.setBounds(width / 2.0F + 42.0F, buttonY, 78.0F, 16.0F);
        done.accented();
        buttons.add(done);
    }

    private String snapLabel() {
        return snapping ? "SNAP: ON" : "SNAP: OFF";
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        ScaledResolution resolution = new ScaledResolution(mc);

        showCenterGuideX = false;
        showCenterGuideY = false;
        guideX = -1;
        guideY = -1;

        if (dragging != null) {
            moveDragged(resolution, mouseX, mouseY);
        }
        if (resizing != null) {
            applyResize(resolution, mouseX, mouseY);
        }

        List<HudModule> hudModules = PikoClient.getInstance().getModuleManager().getHudModules();
        for (int i = 0; i < hudModules.size(); i++) {
            HudModule hud = hudModules.get(i);
            hud.renderAt(resolution, true);
        }

        for (int i = 0; i < hudModules.size(); i++) {
            drawOutline(resolution, hudModules.get(i), mouseX, mouseY);
        }

        drawGuides(resolution);
        drawToolbar(mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawOutline(ScaledResolution resolution, HudModule hud, int mouseX, int mouseY) {
        float x = hud.getPixelX(resolution);
        float y = hud.getPixelY(resolution);
        float elementWidth = hud.getScaledWidth();
        float elementHeight = hud.getScaledHeight();
        boolean hovered = isOver(hud, resolution, mouseX, mouseY);
        boolean active = hud == dragging || hud == resizing;

        int outline = hud.isEnabled()
                ? ColorUtil.alpha(Theme.accent(), active ? 1.0F : (hovered ? 0.8F : 0.45F))
                : ColorUtil.alpha(Theme.TEXT_DISABLED, hovered ? 0.8F : 0.35F);
        RenderUtil.drawRoundedBorder(x - 2.0F, y - 2.0F, elementWidth + 4.0F, elementHeight + 4.0F, 3.0F, 1.0F, outline);

        if (hovered || active) {
            RenderUtil.drawRoundedRect(x + elementWidth + 2.0F - HANDLE_SIZE, y + elementHeight + 2.0F - HANDLE_SIZE,
                    HANDLE_SIZE, HANDLE_SIZE, 1.0F, Theme.accent());
            String label = hud.getName() + "  " + String.format("%.2fx", hud.getScale());
            FontManager.small().drawString(label, x - 2.0F, y - 12.0F,
                    hud.isEnabled() ? Theme.TEXT : Theme.TEXT_DISABLED);
        }
        if (!hud.isEnabled()) {
            RenderUtil.drawRoundedRect(x - 2.0F, y - 2.0F, elementWidth + 4.0F, elementHeight + 4.0F, 3.0F,
                    ColorUtil.alpha(0xFF000000, 0.45F));
        }
    }

    private void drawGuides(ScaledResolution resolution) {
        if (!guidesEnabled()) {
            return;
        }
        int guideColor = ColorUtil.alpha(Theme.CYAN, 0.85F);
        if (showCenterGuideX) {
            RenderUtil.drawDashedLine(resolution.getScaledWidth() / 2.0F, 0,
                    resolution.getScaledWidth() / 2.0F, resolution.getScaledHeight(), 4.0F, guideColor);
        }
        if (showCenterGuideY) {
            RenderUtil.drawDashedLine(0, resolution.getScaledHeight() / 2.0F,
                    resolution.getScaledWidth(), resolution.getScaledHeight() / 2.0F, 4.0F, guideColor);
        }
        if (guideX >= 0) {
            RenderUtil.drawDashedLine(guideX, 0, guideX, resolution.getScaledHeight(), 3.0F,
                    ColorUtil.alpha(Theme.accent(), 0.7F));
        }
        if (guideY >= 0) {
            RenderUtil.drawDashedLine(0, guideY, resolution.getScaledWidth(), guideY, 3.0F,
                    ColorUtil.alpha(Theme.accent(), 0.7F));
        }
    }

    private void drawToolbar(int mouseX, int mouseY) {
        float panelWidth = 260.0F;
        float panelX = (width - panelWidth) / 2.0F;
        RenderUtil.drawRoundedRect(panelX, 10.0F, panelWidth, 44.0F, Theme.cornerRadius() + 1.0F, Theme.BACKGROUND);
        RenderUtil.drawRoundedBorder(panelX, 10.0F, panelWidth, 44.0F, Theme.cornerRadius() + 1.0F, 1.0F,
                ColorUtil.alpha(Theme.accent(), 0.5F));
        FontManager.medium().drawString("PIKO HUD EDITOR", panelX + 10.0F, 17.0F, Theme.TEXT);
        FontManager.small().drawString("Drag to move  |  Corner handle or scroll to scale", panelX + 10.0F, 30.0F,
                Theme.TEXT_SECONDARY);
        FontManager.small().drawString("Right click to toggle  |  R resets the hovered element", panelX + 10.0F, 40.0F,
                Theme.TEXT_SECONDARY);

        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).draw(mouseX, mouseY);
        }
    }

    private boolean isOver(HudModule hud, ScaledResolution resolution, int mouseX, int mouseY) {
        float x = hud.getPixelX(resolution);
        float y = hud.getPixelY(resolution);
        return mouseX >= x - 2 && mouseX <= x + hud.getScaledWidth() + 2
                && mouseY >= y - 2 && mouseY <= y + hud.getScaledHeight() + 2;
    }

    private HudModule findHovered(ScaledResolution resolution, int mouseX, int mouseY) {
        List<HudModule> hudModules = PikoClient.getInstance().getModuleManager().getHudModules();
        // Iterate backwards so the element drawn last wins overlapping hit tests.
        for (int i = hudModules.size() - 1; i >= 0; i--) {
            if (isOver(hudModules.get(i), resolution, mouseX, mouseY)) {
                return hudModules.get(i);
            }
        }
        return null;
    }

    private void moveDragged(ScaledResolution resolution, int mouseX, int mouseY) {
        float targetX = mouseX - dragOffsetX;
        float targetY = mouseY - dragOffsetY;

        float elementWidth = dragging.getScaledWidth();
        float elementHeight = dragging.getScaledHeight();
        float screenWidth = resolution.getScaledWidth();
        float screenHeight = resolution.getScaledHeight();

        if (snapping) {
            // Screen edges.
            if (Math.abs(targetX) < snapDistance()) {
                targetX = 0;
            }
            if (Math.abs(targetY) < snapDistance()) {
                targetY = 0;
            }
            if (Math.abs(targetX + elementWidth - screenWidth) < snapDistance()) {
                targetX = screenWidth - elementWidth;
            }
            if (Math.abs(targetY + elementHeight - screenHeight) < snapDistance()) {
                targetY = screenHeight - elementHeight;
            }

            // Screen centre.
            float centeredX = (screenWidth - elementWidth) / 2.0F;
            if (Math.abs(targetX - centeredX) < snapDistance()) {
                targetX = centeredX;
                showCenterGuideX = true;
            }
            float centeredY = (screenHeight - elementHeight) / 2.0F;
            if (Math.abs(targetY - centeredY) < snapDistance()) {
                targetY = centeredY;
                showCenterGuideY = true;
            }

            // Edges of the other elements.
            List<HudModule> hudModules = PikoClient.getInstance().getModuleManager().getHudModules();
            for (int i = 0; i < hudModules.size(); i++) {
                HudModule other = hudModules.get(i);
                if (other == dragging) {
                    continue;
                }
                float otherX = other.getPixelX(resolution);
                float otherY = other.getPixelY(resolution);
                if (Math.abs(targetX - otherX) < snapDistance()) {
                    targetX = otherX;
                    guideX = otherX;
                }
                if (Math.abs(targetY - otherY) < snapDistance()) {
                    targetY = otherY;
                    guideY = otherY;
                }
            }
        }

        targetX = Math.max(0, Math.min(screenWidth - elementWidth, targetX));
        targetY = Math.max(0, Math.min(screenHeight - elementHeight, targetY));
        dragging.setPixelPosition(resolution, targetX, targetY);

        float centerX = targetX + elementWidth / 2.0F;
        if (Math.abs(centerX - screenWidth / 2.0F) < 1.0F) {
            showCenterGuideX = true;
        }
        float centerY = targetY + elementHeight / 2.0F;
        if (Math.abs(centerY - screenHeight / 2.0F) < 1.0F) {
            showCenterGuideY = true;
        }
    }

    private void applyResize(ScaledResolution resolution, int mouseX, int mouseY) {
        float originX = resizing.getPixelX(resolution);
        float originY = resizing.getPixelY(resolution);
        float distance = (float) Math.sqrt(Math.pow(mouseX - originX, 2) + Math.pow(mouseY - originY, 2));
        if (resizeStartDistance <= 0.01F) {
            return;
        }
        resizing.setScale(resizeStartScale * (distance / resizeStartDistance));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (int i = 0; i < buttons.size(); i++) {
            if (buttons.get(i).mouseClicked(mouseX, mouseY, mouseButton)) {
                return;
            }
        }

        ScaledResolution resolution = new ScaledResolution(mc);
        HudModule hovered = findHovered(resolution, mouseX, mouseY);
        if (hovered == null) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }

        if (mouseButton == 1) {
            hovered.setEnabled(!hovered.isEnabled());
            return;
        }
        if (mouseButton != 0) {
            return;
        }

        float x = hovered.getPixelX(resolution);
        float y = hovered.getPixelY(resolution);
        float handleX = x + hovered.getScaledWidth() + 2.0F - HANDLE_SIZE;
        float handleY = y + hovered.getScaledHeight() + 2.0F - HANDLE_SIZE;
        if (mouseX >= handleX && mouseY >= handleY) {
            resizing = hovered;
            resizeStartScale = hovered.getScale();
            resizeStartDistance = (float) Math.sqrt(Math.pow(mouseX - x, 2) + Math.pow(mouseY - y, 2));
            return;
        }

        dragging = hovered;
        dragOffsetX = mouseX - x;
        dragOffsetY = mouseY - y;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (dragging != null || resizing != null) {
            SettingManager.markDirty();
        }
        dragging = null;
        resizing = null;
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) {
            return;
        }
        ScaledResolution resolution = new ScaledResolution(mc);
        int mouseX = Mouse.getEventX() * resolution.getScaledWidth() / mc.displayWidth;
        int mouseY = resolution.getScaledHeight() - Mouse.getEventY() * resolution.getScaledHeight() / mc.displayHeight - 1;
        HudModule hovered = findHovered(resolution, mouseX, mouseY);
        if (hovered != null) {
            hovered.setScale(hovered.getScale() + (wheel > 0 ? 0.05F : -0.05F));
            SettingManager.markDirty();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_R) {
            ScaledResolution resolution = new ScaledResolution(mc);
            int mouseX = Mouse.getX() * resolution.getScaledWidth() / mc.displayWidth;
            int mouseY = resolution.getScaledHeight() - Mouse.getY() * resolution.getScaledHeight() / mc.displayHeight - 1;
            HudModule hovered = findHovered(resolution, mouseX, mouseY);
            if (hovered != null) {
                hovered.resetPosition();
                hovered.getScaleSetting().reset();
                SettingManager.markDirty();
            }
            return;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        PikoClient.getInstance().getConfigManager().saveOnScreenClose();
        super.onGuiClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
