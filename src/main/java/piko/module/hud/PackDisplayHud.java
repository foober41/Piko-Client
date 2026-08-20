package piko.module.hud;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.ResourcePackRepository;
import piko.module.HudModule;
import piko.setting.BooleanSetting;

import java.util.List;

/** Shows the resource pack that is currently on top of the stack. */
public class PackDisplayHud extends HudModule {

    private static final long REFRESH_INTERVAL = 2000L;

    private final BooleanSetting showIcon;
    private final BooleanSetting showName;
    private final BooleanSetting showPrefix;

    private String cachedName = "Default";
    private ResourcePackRepository.Entry cachedEntry;
    private long lastRefresh;

    public PackDisplayHud() {
        super("Pack Display", "Selected resource pack", 0.86F, 0.85F);
        showIcon = settings.add(new BooleanSetting("Show Icon", true));
        showName = settings.add(new BooleanSetting("Show Pack Name", true));
        showPrefix = settings.add(new BooleanSetting("Prefix", true));
        enableBackground(false);
        enableTextColor(0xFFFFFFFF);
        enableFont();
    }

    /**
     * Resource pack lookups walk a list and touch the repository, so the result is cached
     * for a couple of seconds instead of being resolved every frame.
     */
    private void refresh() {
        long now = System.currentTimeMillis();
        if (now - lastRefresh < REFRESH_INTERVAL) {
            return;
        }
        lastRefresh = now;
        List<ResourcePackRepository.Entry> entries = mc.getResourcePackRepository().getRepositoryEntries();
        if (entries.isEmpty()) {
            cachedName = "Default";
            cachedEntry = null;
        } else {
            cachedEntry = entries.get(entries.size() - 1);
            cachedName = cachedEntry.getResourcePackName();
            if (cachedName.toLowerCase().endsWith(".zip")) {
                cachedName = cachedName.substring(0, cachedName.length() - 4);
            }
        }
    }

    private String text() {
        String name = showName.get() ? cachedName : "";
        return showPrefix.get() ? ("Pack: " + name).trim() : name;
    }

    @Override
    public float getWidth() {
        refresh();
        return textWidth(text()) + (showIcon.get() ? 14.0F : 0.0F);
    }

    @Override
    public float getHeight() {
        return Math.max(textHeight(), showIcon.get() ? 12.0F : 0.0F);
    }

    @Override
    protected void render(boolean editing) {
        refresh();
        drawBackground(getWidth(), getHeight());

        float textX = 0;
        if (showIcon.get()) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            if (cachedEntry != null) {
                cachedEntry.bindTexturePackIcon(mc.getTextureManager());
            } else {
                mc.getTextureManager().bindTexture(new net.minecraft.util.ResourceLocation("pack.png"));
            }
            piko.render.RenderUtil.drawTexture(0, (getHeight() - 12.0F) / 2.0F, 12.0F, 12.0F);
            piko.render.RenderUtil.resetState();
            textX = 14.0F;
        }
        font().drawStringWithShadow(text(), textX, (getHeight() - textHeight()) / 2.0F, getTextColor());
    }
}
