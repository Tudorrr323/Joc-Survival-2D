# Joc Survival 2D

Un joc RPG de supraviețuire 2D, dezvoltat în Java, unde jucătorul trebuie să exploreze o lume generată procedural, să adune resurse, să construiască, să lupte cu inamici și să facă comerț pentru a supraviețui și a avansa în nivel.

Proiectul include și un **Map Editor** complet funcțional, care permite crearea, editarea și jucarea pe hărți personalizate.

---

## Galerie Imagini

### Gameplay & Interfață
| | |
|:---:|:---:|
| ![Gameplay](Poze%20Joc/Screenshot%202026-01-19%20191139.png) <br> *Explorare Lume* | ![Gameplay](Poze%20Joc/Screenshot%202026-01-19%20191729.png) <br> *Explorare Lume* | 
| ![Menu](Poze%20Joc/Screenshot%202026-01-19%20191035.png) <br> *Meniu Principal* | ![Combat](Poze%20Joc/Screenshot%202026-01-19%20203753.png) <br> *Luptă cu Inamici și Level Up* | 
| ![Inventory](Poze%20Joc/Screenshot%202026-01-19%20191325.png) <br> *Inventar* | ![Inventory](Poze%20Joc/Screenshot%202026-01-19%20191512.png) <br> *Inventar* | 
| ![Crafting](Poze%20Joc/Screenshot%202026-01-19%20191228.png) <br> *Crafting Station* | ![Crafting](Poze%20Joc/Screenshot%202026-01-19%20191351.png) <br> *Folosire Monument* | 
| ![Crafting](Poze%20Joc/Screenshot%202026-01-19%20191401.png) <br> *Cooldown Monument* | ![Crafting](Poze%20Joc/Screenshot%202026-01-19%20191446.png) <br> *Hunter Camp* | 
| ![Shop](Poze%20Joc/Screenshot%202026-01-19%20191744.png) <br> *Vendor Shop* | ![World Map](Poze%20Joc/Screenshot%202026-01-19%20192656.png) <br> *World Map* | 
| ![World Map](Poze%20Joc/Screenshot%202026-01-19%20191216.png) <br> *World Map* | ![Menu In-Game](Poze%20Joc/Screenshot%202026-01-19%20191155.png) <br> *Meniu Principal in Timpul Jocului* | |

### Map Editor - Funcționalități
| | |
|:---:|:---:|
| ![Map List](Poze%20Joc/Screenshot%202026-01-19%20192012.png) <br> *Listă Hărți* | ![Map List](Poze%20Joc/Screenshot%202026-01-19%20204400.png) <br> *Listă Hărți* |
| ![Editor Overview](Poze%20Joc/Screenshot%202026-01-19%20203142.png) <br> *Interfață Editor* | ![Editor Overview](Poze%20Joc/Screenshot%202026-01-19%20205739.png) <br> *Interfață Editor* | |

### Altele
| | |
|:---:|:---:|
| ![Delete Confirm](Poze%20Joc/Screenshot%202026-01-19%20205422.png) <br> *Pop-up Ștergere* | ![RIP Scene](Poze%20Joc/Screenshot%202026-01-19%20191958.png) <br> *Death Moment* |

---

## Controale

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

## Mecanici de Joc

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
*   **Hunter:** Inamic avansat, care are o șansă de a lăsa pradă (drop) **piese de armură aleatorii** (Helmet, Chestplate, Pants, Boots) la înfrângere.

Sistemul de luptă se bazează pe atac și apărare (Defense). Armurile reduc daunele primite.

### 3. Economie și Shop
Pe hartă sau în sate (plasate în editor) poți găsi un **Vendor (Negustor)**.
*   Interacționează cu el atingându-l.
*   Poți **vinde** resurse și echipament vechi pentru Aur (Gold).
*   Poți **cumpăra** arme și unelte speciale, inclusiv iteme **Golden (Aurii)**, care sunt mult mai puternice și eficiente decât variantele standard de fier sau piatră.

### 4. Progresie RPG
*   **XP & Level:** Fiecare inamic învins și resursă colectată oferă XP. Creșterea în nivel mărește viața maximă și daunele.
*   **Echipament:** Există sloturi pentru Coif, Platoșă, Pantaloni și Cizme.

---

## Map Editor

Jocul include un editor puternic care permite:
*   **Creare Hărți:** Hărți personalizate de dimensiunea 100x100.
*   **Paletă Obiecte:** Plasare de teren, apă, copaci, roci, inamici, clădiri, negustori și punctul de spawn al jucătorului.
*   **Sistem de Salvare:** Hărțile sunt salvate local și pot fi jucate oricând.
*   **Management:** Posibilitatea de a edita sau șterge hărți existente (cu confirmare).

---

## Tehnologii Utilizate

Acest proiect a fost dezvoltat de la zero folosind limbajul **Java**, fără a utiliza motoare de joc externe (cum ar fi Unity sau Godot).

*   **Limbaj:** Java (JDK 8+)
*   **Grafică & UI:** Java AWT (Abstract Window Toolkit) și Java Swing pentru randare grafică 2D, gestionare ferestre și input.
*   **Concepte:**
    *   **Programare Orientată pe Obiecte (OOP):** Structură modulară cu clase pentru Entități, Hartă, Jucător, Inamici.
    *   **Generare Procedurală:** Hărțile de joc sunt generate aleatoriu la fiecare "New Game".
    *   **Serializare:** Salvarea și încărcarea hărților personalizate folosind `Serializable`.
    *   **Game Loop:** Implementarea unui ciclu de joc clasic (Update -> Render) folosind `javax.swing.Timer`.

---

## Cum să rulezi jocul

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
