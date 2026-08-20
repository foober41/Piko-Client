package piko.module.hud;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import piko.module.HudModule;
import piko.setting.BooleanSetting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Active potion effects with level and remaining time, for example {@code Speed II 1:24}. */
public class PotionEffectsHud extends HudModule {

    private static final ResourceLocation INVENTORY_TEXTURE =
            new ResourceLocation("textures/gui/container/inventory.png");
    private static final String[] ROMAN = {"", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

    private final BooleanSetting showIcons;
    private final BooleanSetting showDuration;
    private final BooleanSetting showLevel;

    public PotionEffectsHud() {
        super("Potion Effects", "Active effects with duration", 0.74F, 0.10F);
        showIcons = settings.add(new BooleanSetting("Icons", true));
        showDuration = settings.add(new BooleanSetting("Duration", true));
        showLevel = settings.add(new BooleanSetting("Effect Level", true));
        enableBackground(false);
        enableTextColor(0xFFFFFFFF);
        enableFont();
    }

    private List<PotionEffect> effects(boolean editing) {
        List<PotionEffect> result = new ArrayList<PotionEffect>();
        if (editing || mc.thePlayer == null) {
            result.add(new PotionEffect(Potion.moveSpeed.id, 1680, 1));
            result.add(new PotionEffect(Potion.damageBoost.id, 840, 0));
            return result;
        }
        Collection<PotionEffect> active = mc.thePlayer.getActivePotionEffects();
        result.addAll(active);
        return result;
    }

    private String describe(PotionEffect effect) {
        Potion potion = Potion.potionTypes[effect.getPotionID()];
        StringBuilder builder = new StringBuilder(I18n.format(potion.getName()));
        if (showLevel.get() && effect.getAmplifier() > 0 && effect.getAmplifier() < ROMAN.length) {
            builder.append(' ').append(ROMAN[effect.getAmplifier()]);
        }
        if (showDuration.get()) {
            builder.append(' ').append(Potion.getDurationString(effect));
        }
        return builder.toString();
    }

    private float rowHeight() {
        return showIcons.get() ? Math.max(textHeight(), 10.0F) + 3.0F : textHeight() + 2.0F;
    }

    @Override
    public float getWidth() {
        List<PotionEffect> effects = effects(true);
        float widest = 0;
        for (int i = 0; i < effects.size(); i++) {
            widest = Math.max(widest, textWidth(describe(effects.get(i))));
        }
        return widest + (showIcons.get() ? 12.0F : 0.0F);
    }

    @Override
    public float getHeight() {
        return Math.max(1, effects(true).size()) * rowHeight();
    }

    @Override
    protected void render(boolean editing) {
        List<PotionEffect> effects = effects(editing);
        if (effects.isEmpty()) {
            return;
        }
        drawBackground(getWidth(), getHeight());

        float rowHeight = rowHeight();
        for (int i = 0; i < effects.size(); i++) {
            PotionEffect effect = effects.get(i);
            Potion potion = Potion.potionTypes[effect.getPotionID()];
            float y = i * rowHeight;
            float textX = 0;

            if (showIcons.get() && potion.hasStatusIcon()) {
                drawIcon(potion.getStatusIconIndex(), 0, y);
                textX = 12.0F;
            }
            font().drawStringWithShadow(describe(effect), textX, y + 1.0F, getTextColor());
        }
    }

    private void drawIcon(int iconIndex, float x, float y) {
        mc.getTextureManager().bindTexture(INVENTORY_TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        // The status icons live in an 18 by 18 grid starting at 0,198 in the inventory sheet.
        piko.render.RenderUtil.drawTexturedRect(x, y - 0.5F, 10.0F, 10.0F,
                iconIndex % 8 * 18, 198 + iconIndex / 8 * 18, 18, 18, 256.0F, 256.0F);
        piko.render.RenderUtil.resetState();
    }
}
