# Not for Goblins 🏰

**Not for Goblins** is a 2D tower defense game for Android with a whimsical goblin theme. Players place towers, fend off waves of enemies, and choose strategic cards to boost their defenses.

---

## Current Prototype ✅

- **Wave-based gameplay**: enemies move along a path; waves progressively get harder  
- **Tower placement system**: place towers on fixed slots to defend your base  
- **Interactive card system**: choose bonus cards after each wave (e.g., new towers, upgrades, or gold)  
- **Combat mechanics**: towers automatically attack enemies; projectiles travel along trajectories  
- **HUD & UI**: base health, gold, current wave, and score displayed  
- **Game Over & Restart**: overlay and controls to restart after defeat  

---

## Vision / Planned Features 🚀

- Multiple tower types with unique abilities and upgrade paths  
- Various enemy types with different behaviors and speeds  
- Strategic card system for tactical gameplay  
- Fun sound effects and background music  
- Achievements, leaderboards, and unlockable towers/skins  
- Dynamic gameplay where card choices impact tower defense strategy  

---

## Tech Stack 🛠️

- **Kotlin + Jetpack Compose**  
- **Compose Canvas** for 2D rendering  
- **State management** using `mutableStateOf` and `SnapshotStateList`  
- **Clean architecture**: separation of `GameState`, `WaveManager`, `CardManager`, and domain models for enemies, towers, and projectiles  

---

This project is a work-in-progress prototype. Contributions, feedback, and ideas are welcome!
