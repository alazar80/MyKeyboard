package com.example.mykeyboard;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.content.Context;

public class AmharicKeyboard extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {

    private static final int KEY_LANG_SWITCH = -101; // 🌐 key code
    private static final int KEY_NUM_SWITCH  = Keyboard.KEYCODE_MODE_CHANGE; // optional

    private KeyboardView kv;
    private Keyboard keyboard;

    private boolean isLatin = false;
    private boolean useNumbers = false;

    @Override
    public View onCreateInputView() {
        kv = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard_view, null);
        kv.setOnKeyboardActionListener(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        isLatin = prefs.getBoolean("pref_enable_latin", false);
        useNumbers = prefs.getBoolean("pref_switch_to_numbers", false);

        keyboard = new Keyboard(this,
                useNumbers ? R.xml.geez_numbers
                        : (isLatin ? R.xml.latin_keyboard : R.xml.full_fidel_keyboard));

        kv.setKeyboard(keyboard);
        kv.setPreviewEnabled(true);
        kv.setSoundEffectsEnabled(prefs.getBoolean("pref_enable_sound", true));
        kv.setPopupParent(kv);
        return kv;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        isLatin = prefs.getBoolean("pref_enable_latin", false);
        useNumbers = prefs.getBoolean("pref_switch_to_numbers", false);

        keyboard = new Keyboard(this,
                useNumbers ? R.xml.geez_numbers
                        : (isLatin ? R.xml.latin_keyboard : R.xml.full_fidel_keyboard));
        kv.setKeyboard(keyboard);
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        // 1) layout switching keys
        if (primaryCode == KEY_LANG_SWITCH) {
            isLatin = !isLatin;
            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit().putBoolean("pref_enable_latin", isLatin).apply();
            keyboard = new Keyboard(this, isLatin ? R.xml.latin_keyboard : R.xml.full_fidel_keyboard);
            kv.setKeyboard(keyboard);
            return;
        }
        if (primaryCode == KEY_NUM_SWITCH) {
            useNumbers = !useNumbers;
            keyboard = new Keyboard(this, useNumbers ? R.xml.geez_numbers
                    : (isLatin ? R.xml.latin_keyboard : R.xml.full_fidel_keyboard));
            kv.setKeyboard(keyboard);
            return;
        }

        // 2) normal input
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean phonetic = prefs.getBoolean("pref_enable_phonetic", false);

        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE:
                ic.deleteSurroundingText(1, 0);
                break;
            case Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            default:
                char code = (char) primaryCode;
                // Map only when user chose Latin+phonetic
                String output = (phonetic && isLatin) ? mapPhonetic(code) : String.valueOf(code);
                ic.commitText(output, 1);

                if (prefs.getBoolean("pref_enable_vibration", true)) {
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (v != null && v.hasVibrator()) v.vibrate(30);
                }
                break;
        }
    }

    private String mapPhonetic(char input) {
        switch (Character.toLowerCase(input)) {
            case 'h': return "ሀ";
            case 'l': return "ለ";
            case 'm': return "መ";
            case 's': return "ሰ";
            case 'r': return "ረ";
            case 'b': return "በ";
            case 't': return "ተ";
            case 'n': return "ነ";
            case 'k': return "ከ";
            case 'w': return "ወ";
            case 'z': return "ዘ";
            case 'y': return "የ";
            case 'd': return "ደ";
            case 'g': return "ገ";
            case 'f': return "ፈ";
            case 'p': return "ፐ";
            default:  return String.valueOf(input);
        }
    }

    @Override public void onText(CharSequence text) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null && text != null) ic.commitText(text, 1);
    }

    @Override public void onPress(int primaryCode) {}
    @Override public void onRelease(int primaryCode) {}
    @Override public void swipeLeft() {}
    @Override public void swipeRight() {}
    @Override public void swipeDown() {}
    @Override public void swipeUp() {}
}
