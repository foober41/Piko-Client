package piko.gui.components;

import piko.setting.StringSetting;

/** Text field bound to a {@link StringSetting}. */
public class StringFieldComponent extends TextFieldComponent {

    public StringFieldComponent(final StringSetting setting) {
        super("...");
        withLabel(setting.getName());
        maxLength(setting.getMaxLength());
        setText(setting.get());
        onChange(new ChangeHandler() {
            @Override
            public void onTextChanged(String text) {
                setting.set(text);
            }
        });
    }
}
