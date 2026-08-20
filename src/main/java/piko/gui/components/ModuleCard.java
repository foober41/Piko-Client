package piko.gui.components;

import piko.animation.Animation;
import piko.font.FontManager;
import piko.gui.Theme;
import piko.module.Module;
import piko.render.ColorUtil;
import piko.render.RenderUtil;

/**
 * Card representing one module in the settings screen: name, short description and an
 * ON/OFF switch. Clicking anywhere but the switch opens the detailed settings.
 */
public class ModuleCard extends Component {

    public interface OpenHandler {
        void open(Module module);
    }

    private final Module module;
    private final ToggleSwitch toggle;
    private final OpenHandler openHandler;
    private final Animation hover = new Animation(0.0F, Theme.animationDuration(150.0F));

    public ModuleCard(final Module module, OpenHandler openHandler) {
        this.module = module;
        this.openHandler = openHandler;
        this.toggle = new ToggleSwitch(new ToggleSwitch.State() {
            @Override
            public boolean get() {
                return module.isEnabled();
            }

            @Override
            public void set(boolean value) {
                module.setEnabled(value);
            }
        });
        this.height = 42.0F;
    }

    public Module getModule() {
        return module;
    }

    @Override
    public void draw(float mouseX, float mouseY) {
        if (!visible) {
            return;
        }
        boolean hovered = isHovered(mouseX, mouseY);
        hover.setTarget(hovered ? 1.0F : 0.0F);
        float progress = hover.getValue();

        RenderUtil.drawRoundedRect(x, y, width, height, Theme.cornerRadius(),
                ColorUtil.mix(Theme.PANEL, Theme.PANEL_HOVER, progress));
        RenderUtil.drawRoundedBorder(x, y, width, height, Theme.cornerRadius(), 1.0F,
                ColorUtil.alpha(Theme.accent(), (module.isEnabled() ? 0.35F : 0.10F) + 0.35F * progress));

        // Accent bar on the left edge shows the enabled state at a glance.
        if (module.isEnabled()) {
            RenderUtil.drawRoundedRect(x + 1.0F, y + 6.0F, 2.0F, height - 12.0F, 1.0F, Theme.accent());
        }

        FontManager.medium().drawString(module.getName(), x + 9.0F, y + 8.0F, Theme.TEXT);
        FontManager.small().drawString(trim(module.getDescription(), width - 20.0F), x + 9.0F, y + 21.0F,
                Theme.TEXT_SECONDARY);

        if (module.hasKeybind() && module.getKeybind().isBound()) {
            String bind = "[" + module.getKeybind().displayValue() + "]";
            FontManager.small().drawString(bind, x + width - 34.0F - FontManager.small().getStringWidth(bind),
                    y + height - 13.0F, Theme.TEXT_DISABLED);
        }

        toggle.setPosition(x + width - 30.0F, y + height - 17.0F);
        toggle.draw(mouseX, mouseY);
    }

    private static String trim(String text, float maxWidth) {
        if (FontManager.small().getStringWidth(text) <= maxWidth) {
            return text;
        }
        String result = text;
        while (result.length() > 3 && FontManager.small().getStringWidth(result + "...") > maxWidth) {
            result = result.substring(0, result.length() - 1);
        }
        return result + "...";
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, int button) {
        if (!visible || !isHovered(mouseX, mouseY)) {
            return false;
        }
        if (toggle.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 0 && openHandler != null) {
            openHandler.open(module);
            return true;
        }
        return true;
    }
}
