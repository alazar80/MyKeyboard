package com.example.mykeyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class AmharicKeyboard extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {

    private static final int KEY_LANG_SWITCH = -101;                         // 🌐
    private static final int KEY_NUM_SWITCH  = Keyboard.KEYCODE_MODE_CHANGE; // -2
    private static final int KEY_SHIFT       = Keyboard.KEYCODE_SHIFT;       // -1

    private static final String PREF_ENABLE_LATIN     = "pref_enable_latin";
    private static final String PREF_START_ON_NUMBERS = "pref_switch_to_numbers";
    private static final String PREF_ENABLE_SOUND     = "pref_enable_sound";
    private static final String PREF_ENABLE_VIBRATION = "pref_enable_vibration";
    private static final String PREF_ENABLE_PHONETIC  = "pref_enable_phonetic";

    private KeyboardView kv;

    private Keyboard kAmharicLetters;   // R.xml.full_fidel_keyboard
    private Keyboard kEnglishLetters;   // R.xml.latin_keyboard (the XML above)
    private Keyboard kGeezNumbers;      // R.xml.geez_numbers
    private Keyboard kLatinSymbols;     // R.xml.kbd_symbols_latin

    private boolean isLatin   = false;  // false=Amharic, true=English
    private boolean isSymbols = false;  // false=letters, true=numbers/symbols
    private boolean shiftOn   = false;  // Shift/Caps state for Latin letters

    @Override
    public View onCreateInputView() {
        kv = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard_view, null);
        kv.setOnKeyboardActionListener(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        isLatin   = prefs.getBoolean(PREF_ENABLE_LATIN, false);
        isSymbols = prefs.getBoolean(PREF_START_ON_NUMBERS, false);

        kAmharicLetters = new Keyboard(this, R.xml.full_fidel_keyboard);
        kEnglishLetters = new Keyboard(this, R.xml.latin_keyboard);
        kGeezNumbers    = new Keyboard(this, R.xml.geez_numbers);
        kLatinSymbols   = new Keyboard(this, R.xml.kbd_symbols_latin);

        kv.setKeyboard(pickKeyboard());
        kv.setPreviewEnabled(true);
        kv.setSoundEffectsEnabled(prefs.getBoolean(PREF_ENABLE_SOUND, true));
        kv.setPopupParent(kv);
        return kv;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        isLatin   = prefs.getBoolean(PREF_ENABLE_LATIN, false);
        isSymbols = prefs.getBoolean(PREF_START_ON_NUMBERS, false);

        kv.setKeyboard(pickKeyboard());
        applyShiftVisual();
        kv.invalidateAllKeys();
    }

    private Keyboard pickKeyboard() {
        if (isSymbols) {
            return isLatin ? kLatinSymbols : kGeezNumbers;
        } else {
            return isLatin ? kEnglishLetters : kAmharicLetters;
        }
    }

    private void toggleSymbols() {
        isSymbols = !isSymbols;
        kv.setKeyboard(pickKeyboard());
        // when leaving symbols, keep previous shift state on Latin letters
        applyShiftVisual();
        kv.invalidateAllKeys();
    }

    private void toggleLanguage() {
        isLatin = !isLatin;
        kv.setKeyboard(pickKeyboard());
        applyShiftVisual();
        kv.invalidateAllKeys();
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit().putBoolean(PREF_ENABLE_LATIN, isLatin).apply();
    }

    private void toggleShift() {
        // Only meaningful on Latin letters page
        if (!isSymbols && isLatin) {
            shiftOn = !shiftOn;
            applyShiftVisual();
        }
    }

    private void applyShiftVisual() {
        // Reflect shift in the view so key labels draw uppercase
        Keyboard current = pickKeyboard();
        current.setShifted(shiftOn && !isSymbols && isLatin);
        kv.setKeyboard(current);
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        if (primaryCode == KEY_LANG_SWITCH) { toggleLanguage(); return; }
        if (primaryCode == KEY_NUM_SWITCH)  { toggleSymbols();  return; }
        if (primaryCode == KEY_SHIFT)       { toggleShift();    return; }

        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;

        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE:
                ic.deleteSurroundingText(1, 0);
                break;

            case Keyboard.KEYCODE_DONE:
                handleEnterAction(ic);
                break;

            default:
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                boolean phonetic = prefs.getBoolean(PREF_ENABLE_PHONETIC, false);

                char ch = (char) primaryCode;

                String out;
                if (!isSymbols && isLatin) {
                    // Latin letters page
                    if (phonetic) {
                        // Phonetic mapping ignores case; Shift doesn't change target fidel
                        out = mapPhonetic(Character.toLowerCase(ch));
                    } else {
                        // Regular Latin typing: apply Shift/Caps to letters
                        out = Character.isLetter(ch) && shiftOn ?
                                String.valueOf(Character.toUpperCase(ch)) :
                                String.valueOf(ch);
                    }
                } else {
                    // Symbols page or Amharic letters page: just commit the char
                    out = String.valueOf(ch);
                }

                ic.commitText(out, 1);

                // Optional: if you want one-shot Shift, uncomment below
                // if (shiftOn && isLatin && !isSymbols && !kEnglishLetters.isShiftedSticky) { shiftOn = false; applyShiftVisual(); }

                doHaptic(prefs);
                break;
        }
    }

    private void handleEnterAction(InputConnection ic) {
        EditorInfo ei = getCurrentInputEditorInfo();
        if (ei != null) {
            int action = ei.imeOptions & EditorInfo.IME_MASK_ACTION;
            if (action == EditorInfo.IME_ACTION_GO
                    || action == EditorInfo.IME_ACTION_SEARCH
                    || action == EditorInfo.IME_ACTION_SEND
                    || action == EditorInfo.IME_ACTION_NEXT
                    || action == EditorInfo.IME_ACTION_DONE) {
                ic.performEditorAction(action);
                return;
            }
        }
        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
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

    private void doHaptic(SharedPreferences prefs) {
        if (!prefs.getBoolean(PREF_ENABLE_VIBRATION, true)) return;
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v == null || !v.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(30);
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