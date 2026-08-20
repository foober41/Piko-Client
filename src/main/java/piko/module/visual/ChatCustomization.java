package piko.module.visual;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import piko.event.events.ChatEvent;
import piko.event.events.TickEvent;
import piko.event.listener.ChatListener;
import piko.event.listener.TickListener;
import piko.font.FontManager;
import piko.font.IFont;
import piko.gui.PikoChatGui;
import piko.gui.PikoIngameGui;
import piko.module.Module;
import piko.module.ModuleCategory;
import piko.setting.BooleanSetting;
import piko.setting.ColorSetting;
import piko.setting.ModeSetting;
import piko.setting.NumberSetting;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Chat appearance and timestamps.
 *
 * <p>The renderer is swapped once and then follows this module's enabled state, so
 * turning the module off restores the vanilla look without losing message history.</p>
 */
public class ChatCustomization extends Module implements ChatListener, TickListener {

    private final BooleanSetting transparentBackground;
    private final NumberSetting backgroundOpacity;
    private final ColorSetting backgroundColor;
    private final BooleanSetting timestamps;
    private final ModeSetting timestampFormat;
    private final ModeSetting font;
    private final NumberSetting scaleMultiplier;
    private final BooleanSetting smoothChat;
    private final BooleanSetting messageAnimation;
    private final BooleanSetting shadow;

    private final SimpleDateFormat shortFormat = new SimpleDateFormat("HH:mm");
    private final SimpleDateFormat longFormat = new SimpleDateFormat("HH:mm:ss");
    private final SimpleDateFormat twelveHourFormat = new SimpleDateFormat("hh:mm a");

    private boolean installed;

    public ChatCustomization() {
        super("Chat", "Chat appearance, timestamps and animation", ModuleCategory.VISUAL);
        transparentBackground = settings.add(new BooleanSetting("Transparent Background", false));
        backgroundColor = settings.add(new ColorSetting("Background Color", 0xFF07080A));
        backgroundOpacity = settings.add(new NumberSetting("Background Opacity", 0.35D, 0.0D, 1.0D, 0.05D));
        timestamps = settings.add(new BooleanSetting("Chat Timestamps", false));
        timestampFormat = settings.add(new ModeSetting("Timestamp Format", "HH:mm", "HH:mm", "HH:mm:ss", "12 Hour"));
        font = settings.add(new ModeSetting("Custom Font", "Minecraft", FontManager.options()));
        scaleMultiplier = settings.add(new NumberSetting("Chat Scale", 1.0D, 0.5D, 2.0D, 0.05D).suffix("x"));
        smoothChat = settings.add(new BooleanSetting("Smooth Chat", true));
        messageAnimation = settings.add(new BooleanSetting("Message Animation", true));
        shadow = settings.add(new BooleanSetting("Text Shadow", true));
    }

    public boolean isTransparentBackground() {
        return transparentBackground.get();
    }

    public float getBackgroundOpacity() {
        return backgroundOpacity.getFloat();
    }

    public int getBackgroundColor() {
        return backgroundColor.get();
    }

    public IFont getFont() {
        return FontManager.byName(font.get());
    }

    public float getScaleMultiplier() {
        return scaleMultiplier.getFloat();
    }

    public boolean isSmoothChat() {
        return smoothChat.get();
    }

    public boolean isMessageAnimation() {
        return messageAnimation.get();
    }

    public boolean isShadow() {
        return shadow.get();
    }

    @Override
    protected void onEnable() {
        install();
    }

    @Override
    public void onTick(TickEvent event) {
        if (!installed) {
            install();
        }
    }

    /** Installs the Piko overlay once; it defers to vanilla whenever this module is off. */
    private void install() {
        if (installed || mc.ingameGUI == null) {
            return;
        }
        PikoChatGui chat = new PikoChatGui(mc, this);
        mc.ingameGUI = new PikoIngameGui(mc, chat);
        installed = true;
    }

    @Override
    public void onChatReceived(ChatEvent event) {
        if (!timestamps.get() || event.getType() != 0 || event.getMessage() == null) {
            return;
        }
        String stamp = "\u00A78[" + format() + "\u00A78] \u00A7r";
        IChatComponent prefixed = new ChatComponentText(stamp).appendSibling(event.getMessage());
        event.setMessage(prefixed);
    }

    private String format() {
        Date now = new Date();
        if (timestampFormat.is("HH:mm:ss")) {
            return longFormat.format(now);
        }
        if (timestampFormat.is("12 Hour")) {
            return twelveHourFormat.format(now);
        }
        return shortFormat.format(now);
    }
}
