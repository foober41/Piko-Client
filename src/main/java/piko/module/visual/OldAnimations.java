package piko.module.visual;

import piko.module.Module;
import piko.module.ModuleCategory;
import piko.setting.BooleanSetting;

/**
 * Optional 1.7 style first person animations for Minecraft 1.8.9.
 *
 * <p>Every option here changes what the client draws and nothing else. Attack speed,
 * reach, damage, item use timing and the packets sent to the server stay exactly as
 * vanilla 1.8.9 produces them, which keeps the module usable on normal servers.</p>
 */
public class OldAnimations extends Module {

    private final BooleanSetting blockHit;
    private final BooleanSetting itemHolding;
    private final BooleanSetting bow;
    private final BooleanSetting rod;
    private final BooleanSetting eating;
    private final BooleanSetting swing;

    public OldAnimations() {
        super("1.7 Animations", "Old style first person animations", ModuleCategory.VISUAL);
        blockHit = settings.add(new BooleanSetting("1.7 Block Hit", true));
        blockHit.describe("Keeps the arm swinging while blocking, the way 1.7 did");
        itemHolding = settings.add(new BooleanSetting("1.7 Item Holding", true));
        bow = settings.add(new BooleanSetting("1.7 Bow Animation", true));
        rod = settings.add(new BooleanSetting("1.7 Rod Animation", true));
        eating = settings.add(new BooleanSetting("1.7 Eating Animation", true));
        swing = settings.add(new BooleanSetting("1.7 Swing Animation", true));
    }

    public boolean isOldBlockHit() {
        return blockHit.get();
    }

    public boolean isOldItemHolding() {
        return itemHolding.get();
    }

    public boolean isOldBow() {
        return bow.get();
    }

    public boolean isOldRod() {
        return rod.get();
    }

    public boolean isOldEating() {
        return eating.get();
    }

    public boolean isOldSwing() {
        return swing.get();
    }
}
