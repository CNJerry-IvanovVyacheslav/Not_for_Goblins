# Not for Goblins 🏰

**Not for Goblins** is a 2D Tower Defense for Android with **Roguelite elements**. Players place towers, fend off waves of goblins, and choose one of three cards after each level-up to bolster their defenses and survive as long as possible.

---

## Current Features ✅

- **Core Tower Defense Mechanics**
  - Enemies move along a set path.
  - Towers are placed on slots to defend the base.

- **Roguelite Progression**
  - Players gain **XP** for defeating enemies and level up.
  - On each level-up, players are offered a choice of **3 random cards**.
  - Cards include:
    - **Global buffs** (damage, crit chance, attack speed)  
    - **One-shot effects** (stun all, damage all)  
    - **Gold rewards**  
    - **Unlocking new tower types**

- **Varied Towers and Enemies**
  - **4 unique tower types**: `BasicBallista`, `SplashTower`, `SniperTower`, `SlowTower`.
  - **3 enemy types**: `BasicGoblin`, `FastGoblin`, `TankGoblin`.

- **Upgrade System**
  - Players can upgrade any tower by tapping on it.
  - Upgrades cost gold and increase **damage, range, and attack speed**.

- **Advanced Combat System**
  - Multiple projectile types (**splash damage**, **slow**).
  - **Critical hit mechanic**.
  - **Global and individual debuffs** (**slow**, **stun**).

- **Advanced UI and Graphics**
  - Full app navigation: **Main Menu**, **Game Screen**, and a **Shop placeholder**.
  - Fully custom rendering on **Compose Canvas** with animated towers, unique goblin designs, and a base/castle.
  - Informative **HUD** showing level, XP, gold, base health, and time to the next wave.

 <img src="https://github.com/CNJerry-IvanovVyacheslav/Not_for_Goblins/blob/b6fc23f8349ec88120e4f302981f324a6de6df19/screenshots/gif1.gif" width="300">

---

## Future Plans 🚀

- **Persistent Upgrades**: Implement the Shop for purchasing upgrades that persist between game sessions.  
- **More Content**: Add new tower types (e.g., auras, chain lightning) and enemies (flying, healers).  
- **Audio**: Add sound effects for shots, hits, deaths, and background music.  
- **Balancing**: Hand-craft waves and fine-tune the difficulty curve.  
- **Meta-Game**: Achievements, high scores, and unlockable content.

---

## Tech Stack 🛠️

- **Kotlin + Jetpack Compose**  
- **Jetpack Navigation**: For navigating between screens (Menu, Game, Shop).  
- **Compose Canvas**: For all 2D game rendering, including custom drawing of animated enemies, paths, and tower effects.  
- **Polymorphic State Management**: Clean architecture using base classes (`BaseTower`, `BaseEnemy`) to manage game state within a `mutableStateListOf`.

---

This project is **actively developing**. Feedback and ideas are welcome!
