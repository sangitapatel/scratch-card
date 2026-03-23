<div align="center">

# ✨ MagicScratchCard

**Android Scratch Card Library · Scratch View · Foil Reveal · Kotlin Custom View**

[![JitPack](https://jitpack.io/v/sangitapatel/MagicScratchCard.svg)](https://jitpack.io/#sangitapatel/MagicScratchCard)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)](https://android-arsenal.com/api?level=21)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-purple.svg)](https://kotlinlang.org)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Author](https://img.shields.io/badge/Author-Sangita%20Patel-orange.svg)](https://github.com/sangitapatel)

A lightweight, zero-dependency **Android Scratch Card library** that adds a beautiful scratchable foil layer to any View — solid colours, local drawables, and remote images.
Built entirely from scratch by [Sangita Patel](https://github.com/sangitapatel).

**The easiest scratch card view for Android. Drop it in. It just works.**

<p align="center">
  <img src="assets/ss1.png" width="30%" alt="Android scratch card library demo — solid foil" />
  &nbsp;&nbsp;&nbsp;
  <img src="assets/ss2.png" width="30%" alt="Android scratch card view — drawable foil demo" />
</p>

</div>

---

## 🔍 What is MagicScratchCard?

**MagicScratchCard** is an Android library that lets you add a **scratch card effect** to any view in your app — just like lottery scratch cards, reward reveals, and coupon scratch-offs. Users scratch with their finger to reveal a hidden prize, message, or content underneath.

> **Search keywords:** android scratch card library, scratch view android, scratch card kotlin, android scratchable view, foil reveal android, lottery scratch card android, scratch off view android, android scratch card open source

---

## ✅ Features

| Feature | Details |
|---|---|
| 🎨 Solid colour foil | Any hex / `@color` resource |
| 🖼️ Local drawable foil | `@drawable` or resource id as texture |
| 🌐 Remote image foil | Load any URL bitmap via Glide or custom loader |
| 🖌️ Configurable brush | `smv_brushRadius` in dp — small for precision, large for fun |
| 📊 Reveal threshold | `smv_threshold` — auto-complete at 0–100% |
| ✨ Auto-reveal animation | Smooth radial wipe with `EaseOutQuart` curve |
| 📡 Progress callback | `onProgress(view, percent)` — update progress bars live |
| ✔️ Completion callback | `onDone(view)` — trigger rewards, confetti, navigation |
| 🎮 Programmatic control | `reveal()` / `reset()` — server-controlled outcomes |
| 📜 ScrollView safe | Blocks parent touch interception automatically |
| 📱 API 21+ | Works on all modern Android devices |
| 🚫 Zero dependencies | No Glide required — bring your own image loader |

---

## 🚀 Installation

### Step 1 — Add JitPack repository

**`settings.gradle.kts`** (Gradle 7+):
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }   // ← add this
    }
}
```

<details>
<summary>📄 Using older Groovy DSL? Click here</summary>

In your **project-level** `build.gradle`:
```groovy
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }  // ← add this
    }
}
```
</details>

### Step 2 — Add dependency

**`build.gradle.kts`** (app module):
```kotlin
dependencies {
    implementation("com.github.sangitapatel:MagicScratchCard:1.0.0")
}
```

---

## ⚡ Quick Start

### 1. Add to XML layout

Place `ScratchMagicView` on top of your prize content inside a `FrameLayout`:

```xml
<FrameLayout
    android:layout_width="match_parent"
    android:layout_height="200dp">

    <!-- Your hidden prize / content below the foil -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:textSize="24sp"
        android:text="🎉 You Won ₹500!" />

    <!-- ScratchMagicView sits on top as the scratch foil layer -->
    <com.sangitapatel.scratchmagic.ScratchMagicView
        android:id="@+id/scratchView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:smv_foilColor="#9E9E9E"
        app:smv_brushRadius="44dp"
        app:smv_threshold="60"
        app:smv_animateReveal="true"
        app:smv_animateDuration="450" />

</FrameLayout>
```

### 2. Add listener + controls (Kotlin)

```kotlin
val scratchView = findViewById<ScratchMagicView>(R.id.scratchView)

scratchView.listener = object : ScratchMagicView.ScratchListener {
    override fun onProgress(view: ScratchMagicView, percent: Float) {
        progressBar.progress = percent.toInt()
        tvPercent.text = "${percent.toInt()}% scratched"
    }
    override fun onDone(view: ScratchMagicView) {
        Toast.makeText(this@MainActivity, "🎉 Revealed!", Toast.LENGTH_SHORT).show()
        // Trigger confetti, navigate to reward screen, call your API, etc.
    }
}

btnReveal.setOnClickListener { scratchView.reveal() }
btnReset.setOnClickListener  { scratchView.reset()  }
```

---

## 🎨 Foil Modes

### 1. Solid colour foil
The simplest option — any color directly in XML or Kotlin:

```xml
app:smv_foilColor="#9E9E9E"
```
```kotlin
scratchView.foilColor = Color.parseColor("#FFD700")  // gold foil
```

---

### 2. Local drawable foil
Use any drawable as the foil texture — gradients, patterns, custom designs:

```xml
app:smv_foilDrawable="@drawable/my_foil_texture"
```
```kotlin
scratchView.setFoilDrawable(R.drawable.my_foil_texture)
```

---

### 3. Remote image foil (URL)
Load any remote image as the foil using Glide or any custom loader:

```kotlin
// Example with Glide
Glide.with(this)
    .asBitmap()
    .load("https://example.com/foil.png")
    .into(object : CustomTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            scratchView.setFoilBitmap(resource)
        }
        override fun onLoadCleared(placeholder: Drawable?) {}
    })
```

---

## 🛡️ Edge Cases & Behaviour

| Situation | Behaviour |
|---|---|
| `reveal()` called before threshold | Animates full reveal instantly — `onDone` fires after animation |
| `reset()` called mid-scratch | Foil fully restored — scratch progress reset to 0% |
| `sampleStep = 1` | Exact pixel-perfect progress tracking (slower on large views) |
| `sampleStep = 8` | Fast approximate tracking — ideal for large full-screen foils |
| Inside `ScrollView` | Touch interception blocked automatically — no config needed |

---

## 📐 XML Attributes Reference

| Attribute | Type | Default | Description |
|---|---|---|---|
| `smv_foilColor` | color | `#BDBDBD` | Solid colour foil |
| `smv_foilDrawable` | reference | — | Drawable resource as foil texture |
| `smv_brushRadius` | dimension | `44dp` | Finger brush radius |
| `smv_threshold` | float | `60` | Scratch % to trigger `onDone` + auto-reveal |
| `smv_animateReveal` | boolean | `true` | Animate final reveal at threshold |
| `smv_animateDuration` | integer (ms) | `450` | Auto-reveal animation duration |

---

## 🔧 Full Public API

```kotlin
// ── Configuration ──────────────────────────────────────────────
scratchView.brushRadius     = 44f           // pixels
scratchView.threshold       = 60f           // 0–100 %
scratchView.foilColor       = Color.GRAY
scratchView.foilDrawable    = myDrawable
scratchView.animateReveal   = true
scratchView.animateDuration = 450L          // ms
scratchView.sampleStep      = 4             // 1 = exact, 8 = fast
scratchView.listener        = myListener

// ── Setters ────────────────────────────────────────────────────
scratchView.setFoilBitmap(bitmap)           // raw Bitmap (from Glide, Picasso, etc.)
scratchView.setFoilDrawable(R.drawable.x)   // drawable resource id

// ── Control ────────────────────────────────────────────────────
scratchView.reveal(animate = true)          // programmatic full reveal
scratchView.reset()                         // restore foil, reset progress to 0

// ── Read-only ──────────────────────────────────────────────────
val percent : Float   = scratchView.revealPercent   // 0–100
val done    : Boolean = scratchView.isFullyRevealed
```

---

## 💡 Use Cases

- 🎰 **Lottery & reward apps** — scratch to reveal prize amounts
- 🛍️ **E-commerce coupons** — scratch to unlock discount codes
- 🎮 **Games** — scratch to reveal game outcomes or bonuses
- 📦 **Surprise reveals** — unbox animations, gift card reveals
- 📣 **Marketing campaigns** — interactive scratch-off promotions

---


## 🔗 More Libraries by Sangita Patel

| Library | Description |
|---|---|
| [kotlin-spin-wheel](https://github.com/sangitapatel/kotlin-spin-wheel) | Android Lucky Wheel / Prize Wheel View |
| [MagicScratchCard](https://github.com/sangitapatel/MagicScratchCard) | Android Scratch Card Foil View ← you are here |

---

## 📄 License

```
MIT License — Copyright (c) 2026 Sangita Patel
https://github.com/sangitapatel
```

See [LICENSE](LICENSE) for full text.
