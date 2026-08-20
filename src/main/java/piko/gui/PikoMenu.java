package piko.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import piko.PikoClient;
import piko.animation.Animation;
import piko.font.FontManager;
import piko.gui.components.Component;
import piko.gui.components.KeybindComponent;
import piko.gui.components.ModuleCard;
import piko.gui.components.PikoButton;
import piko.gui.components.ScrollPanel;
import piko.gui.components.SettingComponents;
import piko.gui.components.TextFieldComponent;
import piko.gui.components.ToggleSwitch;
import piko.gui.hud.HudEditorScreen;
import piko.module.HudModule;
import piko.module.Module;
import piko.module.ModuleCategory;
import piko.render.ColorUtil;
import piko.render.RenderUtil;
import piko.setting.Setting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Piko mod menu, opened with Right Shift by default.
 *
 * <p>A sidebar selects the category, the content area shows one card per module and
 * clicking a card opens its settings. Everything drawn here is bound directly to the live
 * setting objects, so a change is applied the moment it is made.</p>
 */
public class PikoMenu extends GuiScreen {

    private enum View {
        MODULES,
        MODULE_DETAIL,
        PROFILES
    }

    private static final float SIDEBAR_WIDTH = 92.0F;
    private static final float HEADER_HEIGHT = 34.0F;
    private static final float PADDING = 8.0F;

    // Remembered between openings so the menu returns where the player left it.
    private static ModuleCategory lastCategory = ModuleCategory.HUD;
    private static String lastSearch = "";

    private final Animation fade = new Animation(0.0F, Theme.animationDuration(200.0F));
    private final GuiScreen parent;

    private float panelX;
    private float panelY;
    private float panelWidth;
    private float panelHeight;

    private View view = View.MODULES;
    private ModuleCategory category = lastCategory;
    private Module detailModule;

    private TextFieldComponent searchField;
    private ScrollPanel content;
    private final List<Component> chrome = new ArrayList<Component>();
    private final List<PikoButton> categoryButtons = new ArrayList<PikoButton>();
    private TextFieldComponent profileNameField;

    public PikoMenu() {
        this(null);
    }

    /** Constructor used by the Forge mod list config button. */
    public PikoMenu(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        panelWidth = Math.min(width - 30.0F, 440.0F);
        panelHeight = Math.min(height - 30.0F, 250.0F);
        panelX = (width - panelWidth) / 2.0F;
        panelY = (height - panelHeight) / 2.0F;

        fade.snapTo(0.0F);
        fade.setTarget(1.0F);

        searchField = new TextFieldComponent("Search modules...");
        searchField.setText(lastSearch);
        searchField.onChange(text -> {
            lastSearch = text;
            rebuildContent();
        });

        content = new ScrollPanel();
        buildChrome();
        rebuildContent();
    }

    private void buildChrome() {
        chrome.clear();
        categoryButtons.clear();

        float buttonX = panelX + 8.0F;
        float buttonWidth = SIDEBAR_WIDTH - 16.0F;
        float buttonY = panelY + 52.0F;

        for (final ModuleCategory value : ModuleCategory.values()) {
            PikoButton button = new PikoButton(value.getDisplayName().toUpperCase(), () -> {
                category = value;
                lastCategory = value;
                view = View.MODULES;
                detailModule = null;
                rebuildContent();
            });
            button.setBounds(buttonX, buttonY, buttonWidth, 16.0F);
            categoryButtons.add(button);
            chrome.add(button);
            buttonY += 19.0F;
        }

        PikoButton profiles = new PikoButton("PROFILES", () -> {
            view = View.PROFILES;
            detailModule = null;
            rebuildContent();
        });
        profiles.setBounds(buttonX, buttonY + 6.0F, buttonWidth, 16.0F);
        chrome.add(profiles);

        PikoButton hudEditor = new PikoButton("HUD EDITOR", () ->
                mc.displayGuiScreen(new HudEditorScreen(this)));
        hudEditor.setBounds(buttonX, panelY + panelHeight - 24.0F, buttonWidth, 16.0F);
        chrome.add(hudEditor);

        searchField.setBounds(panelX + SIDEBAR_WIDTH + PADDING, panelY + 12.0F,
                panelWidth - SIDEBAR_WIDTH - PADDING * 2 - 96.0F, 14.0F);
        chrome.add(searchField);
    }

    private void rebuildContent() {
        content.clear();
        content.resetScroll();

        float contentX = panelX + SIDEBAR_WIDTH + PADDING;
        float contentY = panelY + HEADER_HEIGHT;
        float contentWidth = panelWidth - SIDEBAR_WIDTH - PADDING * 2;
        float contentHeight = panelHeight - HEADER_HEIGHT - PADDING;
        content.setBounds(contentX, contentY, contentWidth, contentHeight);

        switch (view) {
            case MODULE_DETAIL:
                buildDetail(contentX, contentY, contentWidth);
                break;
            case PROFILES:
                buildProfiles(contentX, contentY, contentWidth);
                break;
            default:
                buildModuleGrid(contentX, contentY, contentWidth);
                break;
        }
    }

    private void buildModuleGrid(float contentX, float contentY, float contentWidth) {
        List<Module> modules;
        String query = searchField.getText();
        if (query != null && !query.trim().isEmpty()) {
            modules = PikoClient.getInstance().getModuleManager().search(query);
        } else {
            modules = PikoClient.getInstance().getModuleManager().getModules(category);
        }

        float gap = 6.0F;
        float cardWidth = (contentWidth - gap - 4.0F) / 2.0F;
        float cursorY = contentY;
        for (int i = 0; i < modules.size(); i++) {
            ModuleCard card = new ModuleCard(modules.get(i), module -> {
                detailModule = module;
                view = View.MODULE_DETAIL;
                rebuildContent();
            });
            float column = i % 2;
            card.setBounds(contentX + column * (cardWidth + gap), cursorY, cardWidth, 42.0F);
            content.addChild(card);
            if (column == 1) {
                cursorY += 48.0F;
            }
        }
        if (modules.size() % 2 == 1) {
            cursorY += 48.0F;
        }
        content.setContentHeight(Math.max(0.0F, cursorY - contentY));
    }

    private void buildDetail(float contentX, float contentY, float contentWidth) {
        if (detailModule == null) {
            view = View.MODULES;
            buildModuleGrid(contentX, contentY, contentWidth);
            return;
        }

        float cursorY = contentY;
        List<Setting> settings = detailModule.getSettings();
        for (int i = 0; i < settings.size(); i++) {
            Setting setting = settings.get(i);
            Component component = SettingComponents.create(setting);
            if (component == null) {
                continue;
            }
            component.setBounds(contentX + 2.0F, cursorY, contentWidth - 12.0F, component.getHeight());
            content.addChild(component);
            cursorY += component.getHeight() + 8.0F;
        }

        PikoButton reset = new PikoButton("RESET SETTINGS", () -> {
            detailModule.resetSettings();
            rebuildContent();
        });
        reset.setBounds(contentX + 2.0F, cursorY, 100.0F, 16.0F);
        content.addChild(reset);

        if (detailModule instanceof HudModule) {
            final HudModule hud = (HudModule) detailModule;
            PikoButton resetPosition = new PikoButton("RESET POSITION", () -> {
                hud.resetPosition();
                piko.setting.SettingManager.markDirty();
            });
            resetPosition.setBounds(contentX + 108.0F, cursorY, 100.0F, 16.0F);
            content.addChild(resetPosition);
        }
        cursorY += 24.0F;

        content.setContentHeight(Math.max(0.0F, cursorY - contentY));
    }

    private void buildProfiles(float contentX, float contentY, float contentWidth) {
        final piko.profile.ProfileManager profiles = PikoClient.getInstance().getProfileManager();
        float cursorY = contentY;

        // Client hotkeys live here because they belong to Piko itself, not to a module.
        KeybindComponent menuKey = new KeybindComponent(PikoClient.getInstance().getKeybindManager().getMenuKey());
        menuKey.setBounds(contentX + 2.0F, cursorY, contentWidth - 12.0F, 16.0F);
        content.addChild(menuKey);
        cursorY += 18.0F;

        KeybindComponent editorKey = new KeybindComponent(
                PikoClient.getInstance().getKeybindManager().getHudEditorKey());
        editorKey.setBounds(contentX + 2.0F, cursorY, contentWidth - 12.0F, 16.0F);
        content.addChild(editorKey);
        cursorY += 24.0F;

        profileNameField = new TextFieldComponent("New profile name");
        profileNameField.setBounds(contentX + 2.0F, cursorY, contentWidth - 90.0F, 14.0F);
        content.addChild(profileNameField);

        PikoButton create = new PikoButton("CREATE", () -> {
            String name = profileNameField.getText().trim();
            if (!name.isEmpty() && profiles.createProfile(name)) {
                profileNameField.setText("");
                rebuildContent();
            }
        });
        create.setBounds(contentX + contentWidth - 84.0F, cursorY, 52.0F, 14.0F);
        content.addChild(create);
        cursorY += 22.0F;

        List<String> names = profiles.listProfiles();
        for (int i = 0; i < names.size(); i++) {
            final String name = names.get(i);
            boolean active = name.equalsIgnoreCase(profiles.getActiveProfileName());

            PikoButton load = new PikoButton(active ? "ACTIVE" : "LOAD", () -> {
                profiles.switchTo(name);
                rebuildContent();
            });
            load.setBounds(contentX + contentWidth - 152.0F, cursorY, 44.0F, 14.0F);
            if (active) {
                load.accented();
            }
            content.addChild(load);

            PikoButton rename = new PikoButton("RENAME", () -> {
                String newName = profileNameField.getText().trim();
                if (!newName.isEmpty() && profiles.renameProfile(name, newName)) {
                    profileNameField.setText("");
                    rebuildContent();
                }
            });
            rename.setBounds(contentX + contentWidth - 106.0F, cursorY, 44.0F, 14.0F);
            content.addChild(rename);

            PikoButton export = new PikoButton("EXPORT", () -> profiles.exportProfile(name));
            export.setBounds(contentX + contentWidth - 60.0F, cursorY, 40.0F, 14.0F);
            content.addChild(export);

            PikoButton delete = new PikoButton("X", () -> {
                if (profiles.deleteProfile(name)) {
                    rebuildContent();
                }
            });
            delete.setBounds(contentX + contentWidth - 18.0F, cursorY, 14.0F, 14.0F);
            content.addChild(delete);

            ProfileLabel label = new ProfileLabel(name, active);
            label.setBounds(contentX + 2.0F, cursorY, 120.0F, 14.0F);
            content.addChild(label);

            cursorY += 18.0F;
        }

        cursorY += 6.0F;
        List<java.io.File> importable = profiles.listImportable();
        for (int i = 0; i < importable.size(); i++) {
            final java.io.File file = importable.get(i);
            PikoButton importButton = new PikoButton("IMPORT " + file.getName(), () -> {
                if (profiles.importProfile(file)) {
                    rebuildContent();
                }
            });
            importButton.setBounds(contentX + 2.0F, cursorY, contentWidth - 20.0F, 14.0F);
            content.addChild(importButton);
            cursorY += 18.0F;
        }

        content.setContentHeight(Math.max(0.0F, cursorY - contentY));
    }

    /** Static text row used inside the profile list. */
    private static final class ProfileLabel extends Component {
        private final String name;
        private final boolean active;

        private ProfileLabel(String name, boolean active) {
            this.name = name;
            this.active = active;
        }

        @Override
        public void draw(float mouseX, float mouseY) {
            FontManager.regular().drawString(name, x, y + 3.0F, active ? Theme.accent() : Theme.TEXT);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        float alpha = fade.getValue();

        GlStateManager.pushMatrix();
        // Subtle scale up while the menu fades in.
        float scale = 0.98F + 0.02F * alpha;
        GlStateManager.translate(width / 2.0F, height / 2.0F, 0.0F);
        GlStateManager.scale(scale, scale, 1.0F);
        GlStateManager.translate(-width / 2.0F, -height / 2.0F, 0.0F);

        drawPanel(alpha);
        drawSidebar(alpha);

        if (view == View.MODULE_DETAIL && detailModule != null) {
            drawDetailHeader();
        } else if (view == View.PROFILES) {
            FontManager.medium().drawString("PROFILES", panelX + SIDEBAR_WIDTH + PADDING + 2.0F,
                    panelY + HEADER_HEIGHT - 14.0F, Theme.TEXT);
        } else {
            searchField.draw(mouseX, mouseY);
            String label = searchField.getText().trim().isEmpty()
                    ? category.getDescription()
                    : "Search results";
            FontManager.small().drawString(label,
                    panelX + panelWidth - PADDING - FontManager.small().getStringWidth(label),
                    panelY + 16.0F, Theme.TEXT_SECONDARY);
        }

        content.update(mouseX, mouseY);
        content.draw(mouseX, mouseY);

        for (int i = 0; i < chrome.size(); i++) {
            Component component = chrome.get(i);
            if (component == searchField) {
                continue;
            }
            component.draw(mouseX, mouseY);
        }

        // Highlight the selected category button.
        for (int i = 0; i < categoryButtons.size(); i++) {
            if (ModuleCategory.values()[i] == category && view != View.PROFILES) {
                PikoButton button = categoryButtons.get(i);
                RenderUtil.drawRoundedRect(button.getX() - 4.0F, button.getY() + 3.0F, 2.0F, 10.0F, 1.0F,
                        Theme.accent());
            }
        }

        GlStateManager.popMatrix();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawPanel(float alpha) {
        RenderUtil.drawRoundedRect(panelX, panelY, panelWidth, panelHeight, Theme.cornerRadius() + 2.0F,
                ColorUtil.alpha(Theme.BACKGROUND, Theme.panelOpacity() * alpha));
        RenderUtil.drawRoundedBorder(panelX, panelY, panelWidth, panelHeight, Theme.cornerRadius() + 2.0F, 1.0F,
                ColorUtil.alpha(Theme.accent(), 0.55F * alpha));
        RenderUtil.drawRect(panelX + SIDEBAR_WIDTH, panelY + 6.0F, 1.0F, panelHeight - 12.0F,
                ColorUtil.alpha(Theme.DIVIDER, alpha));
    }

    private void drawSidebar(float alpha) {
        FontManager.title().drawString("PIKO", panelX + 10.0F, panelY + 12.0F, ColorUtil.fade(Theme.TEXT, alpha));
        RenderUtil.drawRoundedRect(panelX + 10.0F, panelY + 34.0F, 26.0F, 2.0F, 1.0F,
                ColorUtil.alpha(Theme.accent(), alpha));
        FontManager.small().drawString("v" + PikoClient.VERSION + "  MC " + PikoClient.MINECRAFT_VERSION,
                panelX + 10.0F, panelY + 39.0F, ColorUtil.fade(Theme.TEXT_SECONDARY, alpha));

        String profile = "Profile: " + PikoClient.getInstance().getProfileManager().getActiveProfileName();
        FontManager.small().drawString(profile, panelX + 10.0F, panelY + panelHeight - 36.0F,
                ColorUtil.fade(Theme.TEXT_DISABLED, alpha));
    }

    private void drawDetailHeader() {
        float headerX = panelX + SIDEBAR_WIDTH + PADDING;
        FontManager.medium().drawString(detailModule.getName().toUpperCase(), headerX + 14.0F,
                panelY + 12.0F, Theme.TEXT);
        FontManager.small().drawString(detailModule.getDescription(), headerX + 14.0F, panelY + 24.0F,
                Theme.TEXT_SECONDARY);
        FontManager.medium().drawString("<", headerX + 2.0F, panelY + 12.0F, Theme.accent());
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (view == View.MODULE_DETAIL) {
            float headerX = panelX + SIDEBAR_WIDTH + PADDING;
            if (mouseX >= headerX && mouseX <= headerX + 12.0F && mouseY >= panelY + 8.0F && mouseY <= panelY + 26.0F) {
                view = View.MODULES;
                detailModule = null;
                rebuildContent();
                return;
            }
        }

        if (content.mouseClicked(mouseX, mouseY, mouseButton)) {
            return;
        }
        for (int i = 0; i < chrome.size(); i++) {
            if (chrome.get(i).mouseClicked(mouseX, mouseY, mouseButton)) {
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        content.mouseReleased(mouseX, mouseY, state);
        for (int i = 0; i < chrome.size(); i++) {
            chrome.get(i).mouseReleased(mouseX, mouseY, state);
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            content.scroll(wheel > 0 ? 1 : -1);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (content.keyTyped(typedChar, keyCode)) {
            return;
        }
        for (int i = 0; i < chrome.size(); i++) {
            if (chrome.get(i).keyTyped(typedChar, keyCode)) {
                return;
            }
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        lastSearch = searchField == null ? "" : searchField.getText();
        PikoClient.getInstance().getConfigManager().saveOnScreenClose();
        super.onGuiClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    /** Exposed so other Piko screens can reuse the switch state helper. */
    public static ToggleSwitch.State stateOf(final piko.setting.BooleanSetting setting) {
        return new ToggleSwitch.State() {
            @Override
            public boolean get() {
                return setting.get();
            }

            @Override
            public void set(boolean value) {
                setting.set(value);
            }
        };
    }

    /** True while any keybind widget is waiting for a key, so hotkeys stay inert. */
    public boolean isCapturingKeybind() {
        for (int i = 0; i < content.getChildren().size(); i++) {
            Component component = content.getChildren().get(i);
            if (component instanceof KeybindComponent && ((KeybindComponent) component).isListening()) {
                return true;
            }
        }
        return false;
    }
}
