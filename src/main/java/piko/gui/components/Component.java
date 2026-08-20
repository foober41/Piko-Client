package piko.gui.components;

/** Base class for every Piko widget. Coordinates are absolute GUI pixels. */
public abstract class Component {

    protected float x;
    protected float y;
    protected float width;
    protected float height;
    protected boolean visible = true;

    public void setBounds(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isHovered(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public abstract void draw(float mouseX, float mouseY);

    /** @return true when the click was consumed */
    public boolean mouseClicked(float mouseX, float mouseY, int button) {
        return false;
    }

    public void mouseReleased(float mouseX, float mouseY, int button) {
    }

    /** @return true when the key was consumed */
    public boolean keyTyped(char typedChar, int keyCode) {
        return false;
    }

    /** Called every frame so widgets can follow the cursor while a drag is in progress. */
    public void update(float mouseX, float mouseY) {
    }
}
