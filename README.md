# ✨ ScratchMagic

[![](https://jitpack.io/v/sangitapatel/ScratchMagic.svg)](https://jitpack.io/#sangitapatel/ScratchMagic)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)](https://android-arsenal.com/api?level=21)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Author](https://img.shields.io/badge/Author-Sangita%20Patel-orange.svg)](https://github.com/sangitapatel)

**ScratchMagic** is a lightweight Android library that adds a scratchable foil
layer to any View — built entirely from scratch by
[Sangita Patel](https://github.com/sangitapatel).

---

## Features

| Feature | Details |
|---|---|
| Solid colour foil | Any hex / `@color` |
| Local drawable foil | `@drawable` or resource id |
| URL image foil | Glide extension `loadFoil()` / `loadFoilUrl()` |
| Configurable brush | `smv_brushRadius` in dp |
| Reveal threshold | `smv_threshold` — 0 to 100 % |
| Auto-reveal animation | Smooth radial wipe with `EaseOutQuart` curve |
| Progress callback | `onProgress(view, percent)` |
| Completion callback | `onDone(view)` |
| Programmatic control | `reveal()` / `reset()` |
| ScrollView safe | Blocks parent touch interception automatically |
| API 21+ | Works on all modern Android devices |

---

## Installation

### Step 1 — `settings.gradle.kts`

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }   // ← add this
    }
}
```

### Step 2 — `build.gradle.kts` (app module)

```kotlin
dependencies {
    implementation("com.github.sangitapatel:ScratchMagic:1.0.0")

    // Required only if you use loadFoil() / loadFoilUrl() Glide extensions
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")
}
```

---

## Quick Start

### 1. Add to XML

```xml
<FrameLayout
    android:layout_width="match_parent"
    android:layout_height="200dp">

    <!-- Your hidden prize / content here -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:text="You Won ₹500!" />

    <!-- ScratchMagicView sits on top as the foil layer -->
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

### 2. Set listener + controls (Kotlin)

```kotlin
val scratchView = findViewById<ScratchMagicView>(R.id.scratchView)

scratchView.listener = object : ScratchMagicView.ScratchListener {
    override fun onProgress(view: ScratchMagicView, percent: Float) {
        progressBar.progress = percent.toInt()
        tvPercent.text = "${percent.toInt()}% scratched"
    }
    override fun onDone(view: ScratchMagicView) {
        Toast.makeText(this@MainActivity, "Revealed!", Toast.LENGTH_SHORT).show()
    }
}

btnReveal.setOnClickListener { scratchView.reveal() }
btnReset.setOnClickListener  { scratchView.reset()  }
```

---

## Foil Modes

### Solid colour

```kotlin
scratchView.foilColor = Color.parseColor("#9E9E9E")
// or in XML: app:smv_foilColor="#9E9E9E"
```

### Local drawable

```kotlin
scratchView.setFoilDrawable(R.drawable.my_foil_texture)
// or in XML: app:smv_foilDrawable="@drawable/my_foil_texture"
```

### URL image — Glide extension

```kotlin
import com.sangitapatel.scratchmagic.loadFoilUrl
import com.sangitapatel.scratchmagic.loadFoil

// From URL
scratchView.loadFoilUrl("https://example.com/foil.png")

// From URL with placeholder
scratchView.loadFoilUrl(
    url         = "https://example.com/foil.png",
    placeholder = R.drawable.my_placeholder
)

// Any Glide model (Uri, File, @DrawableRes, …)
scratchView.loadFoil(R.drawable.my_foil)
scratchView.loadFoil(uri, R.drawable.placeholder)
```

### Raw Bitmap (custom Glide load)

```kotlin
Glide.with(this)
    .asBitmap()
    .load(url)
    .into(object : CustomTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            scratchView.setFoilBitmap(resource)
        }
        override fun onLoadCleared(placeholder: Drawable?) {}
    })
```

---

## XML Attributes

| Attribute | Type | Default | Description |
|---|---|---|---|
| `smv_foilColor` | color | `#BDBDBD` | Solid colour foil |
| `smv_foilDrawable` | reference | — | Drawable as foil |
| `smv_brushRadius` | dimension | `44dp` | Finger brush radius |
| `smv_threshold` | float | `60` | Reveal % to trigger `onDone` |
| `smv_animateReveal` | boolean | `true` | Auto-animate reveal at threshold |
| `smv_animateDuration` | integer (ms) | `450` | Animation duration |

---

## Full Public API

```kotlin
// ── Configuration ──────────────────────────────────────────
scratchView.brushRadius      = 44f          // pixels
scratchView.threshold        = 60f          // 0–100
scratchView.foilColor        = Color.GRAY
scratchView.foilDrawable     = myDrawable
scratchView.animateReveal    = true
scratchView.animateDuration  = 450L
scratchView.sampleStep       = 4            // pixel sampling grid (1=exact, 8=fast)
scratchView.listener         = myListener

// ── Setters ────────────────────────────────────────────────
scratchView.setFoilBitmap(bitmap)           // raw bitmap (e.g. from Glide)
scratchView.setFoilDrawable(R.drawable.x)  // resource id

// ── Glide extensions (import required) ────────────────────
scratchView.loadFoil(model)
scratchView.loadFoil(model, placeholderResId)
scratchView.loadFoilUrl(url)
scratchView.loadFoilUrl(url, placeholderResId)

// ── Control ────────────────────────────────────────────────
scratchView.reveal(animate = true)   // reveal card
scratchView.reset()                  // cover again

// ── Read-only ──────────────────────────────────────────────
val pct      : Float   = scratchView.revealPercent    // 0–100
val done     : Boolean = scratchView.isFullyRevealed
```

---

## Publish to JitPack — Step by Step

### Step 1 — Push to GitHub

```bash
git init
git add .
git commit -m "feat: ScratchMagic v1.0.0"
git branch -M main
git remote add origin https://github.com/sangitapatel/ScratchMagic.git
git push -u origin main
```

### Step 2 — Create a version tag

```bash
git tag 1.0.0
git push origin 1.0.0
```

Or on GitHub: **Releases → Draft a new release → Tag: `1.0.0` → Publish release**

### Step 3 — Trigger JitPack build

Open in browser:
```
https://jitpack.io/#sangitapatel/ScratchMagic/1.0.0
```

Click **"Get it"** → wait for green ✅ build log → library is live!

### Step 4 — Anyone can now add it

```kotlin
implementation("com.github.sangitapatel:ScratchMagic:1.0.0")
```

---

## License

```
MIT License — Copyright (c) 2024 Sangita Patel
https://github.com/sangitapatel
```

See [LICENSE](LICENSE) for full text.
