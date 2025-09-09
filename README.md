# MyKeyboard (Android — Java IME)

> Offline **Amharic / አማርኛ** keyboard for Android. Built in **Java** using Android’s **InputMethodService**. Includes Ethiopic (Geʽez) layout, symbols, numbers, and optional phonetic typing.

---

## ✨ Features

- ፊደል layout optimized for Amharic typing
- Shift layer + numbers/symbols page
- Long-press for alternates/diacritics
- Optional **phonetic mode** (latin → ፊደል) *(if enabled in code)*
- Haptic sound/vibration (toggleable)
- Works fully **offline**

> Tip: Remove “phonetic mode” in this list if your build doesn’t include it yet.

---

## 🧱 Tech Stack

- **Android Studio** (Gradle Wrapper)
- **Java**, **InputMethodService**, **Keyboard** / **KeyboardView**
- XML keyboard layouts in `res/xml/`

---
(./Amharic Keyboard.jpg)
---

## 🚀 Quick Start (Build)

1. **Clone**
   ```bash
   git clone https://github.com/alazar80/Amharickeyboard.git
   cd Amharickeyboard
