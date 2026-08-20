package piko.module.pvp;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import piko.event.events.AttackEvent;
import piko.event.events.TickEvent;
import piko.event.listener.AttackListener;
import piko.event.listener.TickListener;
import piko.module.HudModule;
import piko.module.ModuleCategory;
import piko.setting.BooleanSetting;
import piko.setting.NumberSetting;

/**
 * Counts consecutive hits on the same opponent.
 *
 * <p>Purely a readout: the counter observes attacks that already happened and never
 * influences combat in any way.</p>
 */
public class ComboCounter extends HudModule implements AttackListener, TickListener {

    private final NumberSetting resetDelay;
    private final BooleanSetting playersOnly;
    private final BooleanSetting hideAtZero;

    private int combo;
    private Entity lastTarget;
    private long lastHit;

    public ComboCounter() {
        super("Combo Counter", "Consecutive hits on one target", ModuleCategory.PVP, 0.5F, 0.30F, false);
        resetDelay = settings.add(new NumberSetting("Reset Delay", 3.0D, 0.5D, 10.0D, 0.5D).suffix("s"));
        playersOnly = settings.add(new BooleanSetting("Players Only", true));
        hideAtZero = settings.add(new BooleanSetting("Hide At Zero", true));
        enableBackground(true);
        enableTextColor(0xFFFFFFFF);
        enableFont();
    }

    @Override
    public void onAttack(AttackEvent event) {
        Entity target = event.getTarget();
        if (!(target instanceof EntityLivingBase)) {
            return;
        }
        if (playersOnly.get() && !(target instanceof EntityPlayer)) {
            return;
        }
        if (target != lastTarget) {
            combo = 0;
            lastTarget = target;
        }
        combo++;
        lastHit = System.currentTimeMillis();
    }

    @Override
    public void onTick(TickEvent event) {
        if (combo == 0) {
            return;
        }
        boolean expired = System.currentTimeMillis() - lastHit > (long) (resetDelay.get() * 1000.0D);
        boolean targetGone = lastTarget == null || lastTarget.isDead
                || (lastTarget instanceof EntityLivingBase && ((EntityLivingBase) lastTarget).getHealth() <= 0);
        if (expired || targetGone) {
            combo = 0;
            lastTarget = null;
        }
    }

    @Override
    protected void onDisable() {
        combo = 0;
        lastTarget = null;
    }

    private String text(boolean editing) {
        return "Combo: " + (editing ? 6 : combo);
    }

    @Override
    public float getWidth() {
        return textWidth(text(true));
    }

    @Override
    public float getHeight() {
        return textHeight();
    }

    @Override
    protected void render(boolean editing) {
        if (!editing && combo == 0 && hideAtZero.get()) {
            return;
        }
        String text = text(editing);
        drawBackground(textWidth(text), textHeight());
        font().drawStringWithShadow(text, 0, 0, getTextColor());
    }
}
