# Joc Survival 2D

Un joc RPG de supraviețuire 2D, dezvoltat în Java, unde jucătorul trebuie să exploreze o lume generată procedural, să adune resurse, să construiască, să lupte cu inamici și să facă comerț pentru a supraviețui și a avansa în nivel.

Proiectul include și un **Map Editor** complet funcțional, care permite crearea, editarea și jucarea pe hărți personalizate.

---

## Actualizări Recente

### Grafică & Sistem de Personalizare
*   **Overhaul Vizual:** S-a început integrarea de grafică profesională, adăugând iconițe noi pentru butoane, ferestre modale stilizate și elemente de UI rafinate.
*   **Selecție Eroi:** Un nou meniu de selecție a caracterului permite alegerea între 4 clase (Knight, Lancer, Archer, Pawn).
*   **Sistem de Culori:** Posibilitatea de a schimba culoarea echipei/eroului folosind un selector tematic sub formă de săbii, care actualizează instantaneu portretele eroilor.
*   **Meniu Principal Animat:** Fundal cinematic care simulează o lume vie în spatele butoanelor din meniul principal.

### Combat & Mecanici Noi
*   **Sistem de Guard:** Jucătorul poate acum bloca atacurile inamicilor folosind tasta **SPACE**. Blocarea corectă reduce sau anulează daunele primite.
*   **Combat Manual:** Mecanica de luptă a fost schimbată; daunele nu mai sunt automate la coliziune. Jucătorul trebuie să dea click pe inamic pentru a-l lovi.
*   **Heavy Attack:** S-a adăugat un atac puternic pe **CLICK DREAPTA** care oferă și o mică propulsie (dash) în direcția atacului.
*   **Interacțiune pe Tasta E:** Utilizarea obiectelor din mână (mâncare, poțiuni) și plasarea clădirilor a fost mutată pe tasta **E**.

### Refactoring Major & Save System
*   **Sistem de Save/Load:** 3 sloturi de salvare cu nume personalizate, timestamp și screenshot-uri automate pentru fiecare sesiune.
*   **Modularitate:** Codul este împărțit în pachete specifice (`engine`, `entities`, `ui` etc.) pentru a facilita extinderea ulterioară.

---

## Controale

### În Joc (Gameplay)
*   **W / A / S / D** sau **Săgeți**: Mișcare caracter.
*   **SPACE**: Guard (Blochează atacurile inamicilor).
*   **E**: Folosește item-ul selectat (Mănâncă / Bea / Construiește) sau Interacționează.
*   **CLICK STÂNGA**: Atac Ușor (Lovituri rapide).
*   **CLICK DREAPTA**: Atac Puternic / Dash (Daune mari).
*   **1 - 5**: Selectare rapidă iteme din Hotbar.
*   **I**: Deschide/Închide Inventarul.
*   **C**: Deschide/Închide meniul de Crafting.
*   **M**: Deschide/Închide Harta Lumii (Mini-map).
*   **ESC**: Pauză / Meniu principal.

### În Map Editor
*   **W / A / S / D**: Mișcare cameră.
*   **SHIFT (Hold)**: Mișcare rapidă a camerei.
*   **CLICK STÂNGA**: Plasează obiectul selectat.
*   **SCROLL MOUSE**: Zoom In / Out sau derulare listă hărți.

---

## Mecanici de Joc

### 1. Resurse și Crafting
Folosește unelte specifice pentru a colecta: **Lemn** (Topor), **Piatră** (Târnăcop) și **Cereale** (Manual). Folosește-le în meniul de Crafting pentru a supraviețui.

### 2. Lupta Progresivă
Lupta necesită acum timing. Folosește Guard pentru a supraviețui asaltului inamicilor (Zombies, Skeletons, Hunters). Inamicii avansați lasă acum pradă valoroasă, inclusiv piese de armură.

### 3. Progresie RPG
Fiecare acțiune oferă XP. Creșterea în nivel îți deblochează atribute mai bune și îți permite să porți echipament de raritate mai mare (Common, Rare, Epic, Legendary).

---

## Tehnologii Utilizate
*   **Limbaj:** Java (JDK 8+)
*   **Grafică:** Java AWT/Swing (Fără engine extern).
*   **Stocare:** Serializare Java pentru Save-uri și Hărți.

---

**Dezvoltat în Java AWT/Swing.**
*Versiune: 0.2 (Graphics & Combat Update)*