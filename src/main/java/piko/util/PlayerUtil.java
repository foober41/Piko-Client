package piko.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;

public final class PlayerUtil {

    private PlayerUtil() {
    }

    public static boolean inGame() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.thePlayer != null && mc.theWorld != null;
    }

    /** Server latency in milliseconds, or 0 when it is not known yet. */
    public static int getPing() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.getNetHandler() == null) {
            return 0;
        }
        NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
        return info == null ? 0 : Math.max(0, info.getResponseTime());
    }

    /** Compass direction of the given yaw, for example {@code NE}. */
    public static String getDirection(float yaw) {
        int index = (int) Math.floor(((MathUtil.wrapDegrees(yaw) + 180.0F) / 45.0F) + 0.5D) & 7;
        switch (index) {
            case 0:
                return "N";
            case 1:
                return "NE";
            case 2:
                return "E";
            case 3:
                return "SE";
            case 4:
                return "S";
            case 5:
                return "SW";
            case 6:
                return "W";
            default:
                return "NW";
        }
    }
}
