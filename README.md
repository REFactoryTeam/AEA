<div >

**English** | [简体中文](README_zh_CN.md)

</div>

# [AEA]

An Applied Energistics 2 (AE2) addon and Quality of Life (QoL) mod designed to enhance pattern encoding, improve crafting UI, and provide seamless integrations with other popular mods.

## ✨ Features

### 🧩 Pattern & Encoding Enhancements
* **Pattern Encoding Modifier:
    * Supports independent Input and Output blacklists.
    * Adjust global I/O multipliers.
    * Customize ingredient merging rules: `Global` -> `Adjacent` -> `None`.
* **Flexible Pattern Rotation:** Easily cycle and reverse-cycle main input ingredients, and reverse-cycle main outputs directly in the encoding interface.
* **Dyeable Patterns:** Patterns can now be dyed for better organization. Furthermore, dyed patterns now support self-looping (recursive) recipe calculations.
* **Mirror Pattern Provider:** Added a new "Mirror" provider to duplicate and synchronize pattern operations.

### 🖥️ AE2 System & UI QoL
* **Enhanced Crafting Status:** Added more detailed states to the AE2 crafting monitor, including **"Blocked"** status and **"Pattern Times"** (dispatch counts).
* **Extended Pattern Access Terminal (EAE):** Added a stylish rainbow border effect to the Extended Pattern Access Terminal UI.
* **Quartz Cutting Knife Utility:** You can now use the Quartz Cutting Knife to quickly copy the name of an Item or Block to your clipboard.

### 📡 Wireless Connectivity
* **Wireless Connection Provider:** Wirelessly connects to any AE network node.
* **Normal Version:** Functions as a single Dense Cable connection.
* **Extended Version:** Provides 6 connections (one for each face).
* **Advanced Version:** Per-channel connectivity; each channel acts as a full Dense Cable.

### 🤝 Mod Integrations
* **Just Enough Items (JEI) Interaction:** Forward actions from JEI bookmarks directly to AE2 terminals. Clicking a JEI bookmark now behaves exactly like clicking the item inside the AE2 UI.
* **Building Gadgets 2 Integration:** Effortlessly convert the required material list from a BG2 "Copy-Paste Tool" directly into an AE2 Processing Pattern.
* **FTB Ultimine & Memory Card Compatibility:** Added support for FTB Ultimine when using the AE2 Memory Card, allowing you to chain-paste (vein-paste) configurations across multiple machines at once.

### 🛠️ Bug Fixes & Rendering
* **JEI Rendering Fix:** Corrected the rendering issues of Wrapped Generic Stacks inside JEI (AE/JEI).
* **Inscriber Slot Semantics:** Fixed and corrected the slot semantics for Inscriber Recipes to ensure proper recipe handling in Encoding.

## 📦 Dependencies
To use all the features of this mod, ensure you have the following mods installed:
* [Applied Energistics 2](https://www.curseforge.com/minecraft/mc-mods/applied-energistics-2) (Required)
* [ExtendedAE (EAE)](https://www.curseforge.com/minecraft/mc-mods/ex-pattern-provider) (Rainbow Border)
* [Just Enough Items (JEI)](https://www.curseforge.com/minecraft/mc-mods/jei) (Highly Recommended)
* [Building Gadgets 2](https://www.curseforge.com/minecraft/mc-mods/building-gadgets) (For Copy-Paste Tool integration)
* [FTB Ultimine](https://www.curseforge.com/minecraft/mc-mods/ftb-ultimine-forge) (For chain-copying features)
