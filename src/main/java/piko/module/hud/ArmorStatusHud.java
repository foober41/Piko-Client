package piko.module.hud;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import piko.module.HudModule;
import piko.setting.BooleanSetting;
import piko.setting.ModeSetting;

/**
 * Equipped armour with durability, the piece of information that decides fights.
 *
 * <p>Slots are read straight from the player inventory; nothing is cached longer than a
 * frame so a broken piece disappears immediately.</p>
 */
public class ArmorStatusHud extends HudModule {

    private static final int ICON = 16;

    private final ModeSetting direction;
    private final ModeSetting durabilityMode;
    private final BooleanSetting showCount;
    private final BooleanSetting includeHeldItem;

    public ArmorStatusHud() {
        super("Armor Status", "Equipped armour and durability", 0.90F, 0.55F);
        direction = settings.add(new ModeSetting("Direction", "Vertical", "Vertical", "Horizontal"));
        durabilityMode = settings.add(new ModeSetting("Durability", "Numbers", "None", "Numbers", "Percentage"));
        showCount = settings.add(new BooleanSetting("Item Count", true));
        includeHeldItem = settings.add(new BooleanSetting("Include Held Item", false));
        enableBackground(false);
        enableTextColor(0xFFFFFFFF);
        enableFont();
    }

    private ItemStack[] pieces() {
        if (mc.thePlayer == null) {
            return new ItemStack[0];
        }
        ItemStack[] armor = mc.thePlayer.inventory.armorInventory;
        int extra = includeHeldItem.get() && mc.thePlayer.getHeldItem() != null ? 1 : 0;
        ItemStack[] result = new ItemStack[armor.length + extra];
        // Vanilla stores boots first; helmet first reads better on a HUD.
        for (int i = 0; i < armor.length; i++) {
            result[i] = armor[armor.length - 1 - i];
        }
        if (extra == 1) {
            result[armor.length] = mc.thePlayer.getHeldItem();
        }
        return result;
    }

    private String durabilityText(ItemStack stack) {
        if (stack == null || durabilityMode.is("None") || !stack.isItemStackDamageable()) {
            return "";
        }
        int max = stack.getMaxDamage();
        int left = max - stack.getItemDamage();
        if (durabilityMode.is("Percentage")) {
            return Math.round(left * 100.0F / max) + "%";
        }
        return String.valueOf(left);
    }

    private int durabilityColor(ItemStack stack) {
        if (stack == null || !stack.isItemStackDamageable()) {
            return getTextColor();
        }
        float ratio = 1.0F - (float) stack.getItemDamage() / stack.getMaxDamage();
        if (ratio > 0.5F) {
            return 0xFF4ADE80;
        }
        if (ratio > 0.25F) {
            return 0xFFFACC15;
        }
        return 0xFFF87171;
    }

    private float longestLabel() {
        ItemStack[] pieces = pieces();
        float widest = 0;
        for (int i = 0; i < pieces.length; i++) {
            widest = Math.max(widest, textWidth(durabilityText(pieces[i])));
        }
        return widest;
    }

    @Override
    public float getWidth() {
        if (direction.is("Horizontal")) {
            return Math.max(1, count()) * (ICON + 4.0F);
        }
        return ICON + 3.0F + Math.max(20.0F, longestLabel());
    }

    @Override
    public float getHeight() {
        if (direction.is("Horizontal")) {
            return ICON + textHeight() + 1.0F;
        }
        return Math.max(1, count()) * (ICON + 2.0F);
    }

    private int count() {
        ItemStack[] pieces = pieces();
        int found = 0;
        for (int i = 0; i < pieces.length; i++) {
            if (pieces[i] != null) {
                found++;
            }
        }
        return Math.max(found, 4);
    }

    @Override
    protected void render(boolean editing) {
        drawBackground(getWidth(), getHeight());

        ItemStack[] pieces = pieces();
        if (pieces.length == 0) {
            if (editing) {
                font().drawStringWithShadow("Armor Status", 0, 0, getTextColor());
            }
            return;
        }

        boolean horizontal = direction.is("Horizontal");
        int slot = 0;
        for (int i = 0; i < pieces.length; i++) {
            ItemStack stack = pieces[i];
            if (stack == null) {
                continue;
            }
            float x = horizontal ? slot * (ICON + 4.0F) : 0;
            float y = horizontal ? 0 : slot * (ICON + 2.0F);
            drawItem(stack, x, y);

            String text = durabilityText(stack);
            if (!text.isEmpty()) {
                if (horizontal) {
                    font().drawStringWithShadow(text, x + (ICON - textWidth(text)) / 2.0F, y + ICON + 1.0F,
                            durabilityColor(stack));
                } else {
                    font().drawStringWithShadow(text, x + ICON + 3.0F, y + (ICON - textHeight()) / 2.0F + 1.0F,
                            durabilityColor(stack));
                }
            }
            slot++;
        }
    }

    private void drawItem(ItemStack stack, float x, float y) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0.0F);
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();
        mc.getRenderItem().zLevel = -50.0F;
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
        if (showCount.get()) {
            mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, stack, 0, 0);
        }
        mc.getRenderItem().zLevel = 0.0F;
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableDepth();
        GlStateManager.popMatrix();
        piko.render.RenderUtil.resetState();
    }
}
