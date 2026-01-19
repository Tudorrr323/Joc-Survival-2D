# 🌲 Ultimate Survival 2D

Un joc RPG de supraviețuire 2D, dezvoltat în Java, unde jucătorul trebuie să exploreze o lume generată procedural, să adune resurse, să construiască, să lupte cu inamici și să facă comerț pentru a supraviețui și a avansa în nivel.

Proiectul include și un **Map Editor** complet funcțional, care permite crearea, editarea și jucarea pe hărți personalizate.

---

## 📸 Galerie Imagini

### Gameplay & Interfață
| | |
|:---:|:---:|
| ![Gameplay](Poze%20Joc/Screenshot%202026-01-19%20191035.png) <br> *Explorare Lume* | ![Menu](Poze%20Joc/Screenshot%202026-01-19%20191139.png) <br> *Meniu Principal* |
| ![Combat](Poze%20Joc/Screenshot%202026-01-19%20191155.png) <br> *Luptă cu Inamici* | ![Inventory](Poze%20Joc/Screenshot%202026-01-19%20191228.png) <br> *Inventar* |
| ![Crafting](Poze%20Joc/Screenshot%202026-01-19%20191325.png) <br> *Crafting Station* | ![Shop](Poze%20Joc/Screenshot%202026-01-19%20191351.png) <br> *Vendor Shop* |
| ![Grain Gameplay](Poze%20Joc/Screenshot%202026-01-19%20191958.png) <br> *Resurse (Cereale)* | |

### Map Editor - Funcționalități
| | |
|:---:|:---:|
| ![Map List](Poze%20Joc/Screenshot%202026-01-19%20191401.png) <br> *Listă Hărți* | ![Editor Overview](Poze%20Joc/Screenshot%202026-01-19%20191216.png) <br> *Interfață Editor* |
| ![Editor Water](Poze%20Joc/Screenshot%202026-01-19%20191729.png) <br> *Plasare Apă* | ![Editor Vendor](Poze%20Joc/Screenshot%202026-01-19%20191744.png) <br> *Plasare Vendor* |
| ![Editor Grain](Poze%20Joc/Screenshot%202026-01-19%20192012.png) <br> *Plasare Resurse* | ![Editor Spawn](Poze%20Joc/Screenshot%202026-01-19%20192656.png) <br> *Spawn Point Modern* |

### Sistem Confirmări
| | |
|:---:|:---:|
| ![Delete Confirm](Poze%20Joc/Screenshot%202026-01-19%20191446.png) <br> *Pop-up Ștergere* | ![Save Confirm](Poze%20Joc/Screenshot%202026-01-19%20191512.png) <br> *Confirmare Salvare* |

---

## 🎮 Controale

### În Joc (Gameplay)
*   **W / A / S / D** sau **Săgeți**: Mișcare caracter.
*   **SPACE**: Folosește item-ul din mână (Mănâncă pâine, Bea poțiune) sau Interacționează (Ridicp clădiri).
*   **CLICK STÂNGA**: Atacă inamici / Colectează resurse / Interacționează cu butoane.
*   **1 - 5**: Selectare rapidă iteme din Hotbar.
*   **I**: Deschide/Închide Inventarul.
*   **C**: Deschide/Închide meniul de Crafting.
*   **M**: Deschide/Închide Harta Lumii (Mini-map).
*   **ESC**: Pauză / Meniu principal.

### În Map Editor
*   **W / A / S / D** sau **Săgeți**: Mișcare cameră pe hartă.
*   **CLICK STÂNGA**: Plasează obiectul selectat din paletă.
*   **CLICK PE BUTOANE SĂGEȚI**: Mișcare cameră (alternativă).
*   **SCROLL MOUSE**: Derulare listă de hărți (în meniul de selecție).

---

## 🛠️ Mecanici de Joc

### 1. Resurse și Crafting
Jucătorul poate aduna resurse din lume folosind unelte specifice:
*   **Lemn (Wood):** Obținut din copaci (necesită Topor/Axe).
*   **Piatră (Stone):** Obținută din roci (necesită Târnăcop/Pickaxe).
*   **Cereale (Grain):** Obținute din plante (se pot culege cu mâna).

Aceste resurse sunt folosite în meniul de **Crafting [C]** pentru a crea:
*   **Pâine (Bread):** Restabilește 30 HP.
*   **Monumente:** Oferă bonus permanent de atac (+5 DMG).
*   **Fântâni (Fountains):** Restabilesc complet viața jucătorului.

### 2. Lupta și Inamicii
Lumea este populată de diverse creaturi ostile:
*   **Zombie:** Inamic de bază, urmărește jucătorul.
*   **Schelete:** Mai rapid și mai periculos.
*   **Hunter:** Inamic avansat, poate lăsa pradă legendară (armuri).

Sistemul de luptă se bazează pe atac și apărare (Defense). Armurile reduc daunele primite.

### 3. Economie și Shop
Pe hartă sau în sate (plasate în editor) poți găsi un **Vendor (Negustor)**.
*   Interacționează cu el atingându-l.
*   Poți **vinde** resurse și echipament vechi pentru Aur (Gold).
*   Poți **cumpăra** arme mai bune (ex: Iron Sword, Golden Axe), armuri și poțiuni de viață.

### 4. Progresie RPG
*   **XP & Level:** Fiecare inamic învins și resursă colectată oferă XP. Creșterea în nivel mărește viața maximă și daunele.
*   **Echipament:** Există sloturi pentru Coif, Platoșă, Pantaloni și Cizme.

---

## 🗺️ Map Editor

Jocul include un editor puternic care permite:
*   **Creare Hărți:** Hărți personalizate de dimensiunea 100x100.
*   **Paletă Obiecte:** Plasare de teren, apă, copaci, roci, inamici, clădiri, negustori și punctul de spawn al jucătorului.
*   **Sistem de Salvare:** Hărțile sunt salvate local și pot fi jucate oricând.
*   **Management:** Posibilitatea de a edita sau șterge hărți existente (cu confirmare).

---

## 🚀 Cum să rulezi jocul

Asigură-te că ai **Java 8** sau mai nou instalat.

1.  **Compilare:**
    Deschide un terminal în folderul rădăcină și rulează:
    ```bash
    javac --release 8 -d bin -sourcepath src src/*.java
    ```

2.  **Rulare:**
    După compilare, pornește jocul cu:
    ```bash
    java -cp bin Main
    ```

---

**Dezvoltat în Java AWT/Swing.**
*Versiune: 15.0 Complete*
