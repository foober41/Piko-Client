package piko.font;

/** Common surface for the vanilla font and the Piko font so modules can swap between them. */
public interface IFont {

    float drawString(String text, float x, float y, int color);

    float drawStringWithShadow(String text, float x, float y, int color);

    float drawCenteredString(String text, float centerX, float y, int color);

    float getStringWidth(String text);

    float getHeight();
}
