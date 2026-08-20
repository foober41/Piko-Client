package piko.module.pvp;

import piko.module.Module;
import piko.module.ModuleCategory;
import piko.setting.BooleanSetting;
import piko.setting.ModeSetting;
import piko.setting.NumberSetting;

/**
 * Visual styling of the sword block animation and the held item pose.
 *
 * <p>The sliders move and scale what is drawn in the player's hand. None of it changes
 * blocking behaviour, damage reduction, attack speed or anything the server sees.</p>
 */
public class BlockHitAnimation extends Module {

    private final ModeSetting style;
    private final BooleanSetting swingWhileBlocking;
    private final NumberSetting swingSpeed;
    private final NumberSetting offsetX;
    private final NumberSetting offsetY;
    private final NumberSetting offsetZ;
    private final NumberSetting itemScale;

    public BlockHitAnimation() {
        super("Block Hit Animation", "Style of the block and swing animation", ModuleCategory.PVP);
        style = settings.add(new ModeSetting("Style", "1.7", "Default", "1.7", "Slide", "Swing", "Push", "Piko"));
        swingWhileBlocking = settings.add(new BooleanSetting("Swing While Blocking", true));
        swingSpeed = settings.add(new NumberSetting("Swing Speed", 1.0D, 0.5D, 2.0D, 0.05D).suffix("x"));
        offsetX = settings.add(new NumberSetting("Item Position X", 0.0D, -0.5D, 0.5D, 0.01D));
        offsetY = settings.add(new NumberSetting("Item Position Y", 0.0D, -0.5D, 0.5D, 0.01D));
        offsetZ = settings.add(new NumberSetting("Item Position Z", 0.0D, -0.5D, 0.5D, 0.01D));
        itemScale = settings.add(new NumberSetting("Item Scale", 1.0D, 0.5D, 1.5D, 0.01D).suffix("x"));
    }

    public String getStyle() {
        return style.get();
    }

    public boolean swingsWhileBlocking() {
        return swingWhileBlocking.get() && !style.is("Default");
    }

    public float getSwingSpeed() {
        return swingSpeed.getFloat();
    }

    public float getOffsetX() {
        return offsetX.getFloat();
    }

    public float getOffsetY() {
        return offsetY.getFloat();
    }

    public float getOffsetZ() {
        return offsetZ.getFloat();
    }

    public float getItemScale() {
        return itemScale.getFloat();
    }
}
