---
name: apple-design
description: Apple's approach to interface design and fluid, physical motion, translated for the web and mobile platforms. Use when building or reviewing gesture-driven UI, spring animations, drag/swipe/sheet interactions, momentum and interruptible transitions, translucent materials and depth, typography (optical sizing, tracking, leading), reduced-motion, or the design foundations (feedback, spatial consistency, restraint) behind Apple-style interfaces.
---

# Apple Design

## Initial Response

When this skill is first invoked without a specific question, respond only with:

> I'm ready to help you build fluid, Apple-style interfaces on the web and mobile, my knowledge comes from Apple's WWDC design talks, translated for modern platforms.

Do not provide any other information until the user asks a question.

How Apple builds interfaces that stop feeling like a computer and start feeling like an extension of you. This knowledge comes from Apple's WWDC design talks — chiefly *Designing Fluid Interfaces* (WWDC 2018) — distilled and translated into modern interactive UI (Jetpack Compose, CSS, Pointer Events, spring physics, tactile haptics).

The through-line: **an interface feels alive when motion starts from the current on-screen value, inherits the user's velocity, projects momentum forward, and can be grabbed and reversed at any instant.** Springs are the tool that makes all of this natural, because they are inherently interruptible and velocity-aware.

## The Core Idea

> "When we align the interface to the way we think and move, something magical happens — it stops feeling like a computer and starts feeling like a seamless extension of us."

An interface is fluid when it behaves like the physical world: things respond instantly, move continuously, carry momentum, resist at boundaries, and can be redirected mid-motion. Everything below is a way to get closer to that.

Apple frames design as serving four human needs: **safety/predictability, understanding, achievement, and joy.** Every rule here serves one of them.

## 1. Response — kill latency

The moment lag appears, the feeling of directness "falls off a cliff." Response is the foundation everything else is built on.

- **Respond on pointer-down, not on release.** Highlight a button the instant it's pressed. Waiting for `click`/touch-up to show feedback feels dead.
- **Be vigilant about every latency.** Audit debounces, artificial timers, transition waits, and tap delays. Anything on the input path that isn't essential is a regression.
- **Feedback must be continuous *during* the interaction, not just at the end.** For a drag, slider, or drawer, update the UI 1:1 with the pointer the whole way through — never animate only when the gesture completes.

## 2. Direct manipulation — 1:1 tracking

> "Touch and content should move together."

When the user drags something, it must stay glued to the finger — and respect the offset from *where they grabbed it*. Snapping to the element's center on grab breaks the illusion immediately.

## 3. Interruptibility — the single most important principle

> "The thought and the gesture happen in parallel."

Every animation must be interruptible and redirectable at any moment. A user must be able to grab a moving element mid-flight and reverse it without waiting for the animation to finish.

- **Never lock out input during a transition.**
- **Always animate from the *presentation* (current) value, never the target value.**
- **When a gesture reverses, blend velocity — don't hard-cut it.**

## 4. Behavior over animation — use springs

> "Think of animation as a conversation between you and the object, not something prescribed by the interface."

Apple parameters:
- **Damping ratio** — controls overshoot. `1.0` = critically damped, no bounce, smooth settle. `< 1.0` = overshoots and oscillates. Lower = bouncier.
- **Response** — how quickly the value reaches the target, in seconds. Lower = snappier.

**Defaults:**
- Start most UI at **damping `1.0`** (critically damped) — graceful and non-distracting.
- Add bounce (**damping ~`0.8`**) **only when the gesture itself carried momentum** (a flick, a throw, a drag release).

## 5. Materials & depth — translucency conveys hierarchy

- **Build nav/toolbars/sheets as translucent layers** with content scrolling underneath.
- **Material weight encodes hierarchy:** darker/heavier materials separate structural regions; lighter materials draw attention to interactive elements.
- **Vibrancy keeps text legible over changing backgrounds.**
- **Hairline top border reflects ambient light catching the glass.**

## 6. Multimodal feedback — motion + sound + haptics

1. **Causality** — it must be obvious what caused the feedback.
2. **Harmony** — visual and haptic must fire on the same frame.
3. **Utility** — add feedback only where it earns its place.

## 7. Typography — optical sizing, tracking, leading

- **Tracking (letter-spacing) is size-specific:** Large display text wants negative tracking (`-0.02em` / `-0.5sp`); small text wants slightly positive tracking for legibility. Tighten headings, leave body near `0`.
- **Leading (line-height) tracks size inversely:** Tight on large headings, looser on body copy.
