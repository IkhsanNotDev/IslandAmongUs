<div align="center">

# 🚀 Among Us Island

### Minecraft Plugin - Social Deduction Game

[![Version](https://img.shields.io/badge/Version-release-blue?style=for-the-badge)](https://github.com/IkhsanNotDev/IslandAmongUs/releases)
[![Download](https://img.shields.io/badge/Download-Latest-ff69b4?style=for-the-badge&logo=github)](https://github.com/IkhsanNotDev/IslandAmongUs/releases/latest)

> **Plugin Minecraft bergaya Among Us yang berjalan di dalam satu pulau (Island)**  
> *Rasakan pengalaman bermain social deduction dengan tugas-tugas unik, pertemuan darurat, dan sistem sabotase—semuanya dalam dunia blok!*

</div>

---

## 📖 Deskripsi

**Among Us Island** adalah plugin yang mengubah server Minecraft-mu menjadi arena permainan *Among Us* versi *survival*. Para pemain akan dibagi menjadi dua tim:

| Tim | Peran |
|-----|-------|
| 🛠️ **Crewmate (Awak)** | Bertugas menyelesaikan semua misi/tugas di pulau sebelum waktu habis |
| 👾 **Impostor (Penyusup)** | Menyamar sebagai awak, menyabotase fasilitas, dan membunuh crewmate tanpa ketahuan |

Game berlangsung di satu pulau dengan area-area khusus (kantin, reaktor, laboratorium, dll.) yang bisa kamu kustomisasi.

---

## ✨ Fitur Unggulan

<br>

<div align="center">

| Fitur | Keterangan |
|-------|------------|
| 🎮 **Sistem Role Otomatis** | Pemain dibagi secara acak menjadi Crewmate atau Impostor di setiap ronde |
| 📋 **Daftar Tugas Interaktif** | Setiap Crewmate mendapat daftar tugas unik dengan GUI/action interaktif |
| 🔪 **Mekanisme Pembunuhan** | Impostor bisa membunuh dengan cooldown, meninggalkan mayat yang bisa dilaporkan |
| ⚙️ **Sabotase Global** | Impostor bisa memicu sabotase yang harus segera diperbaiki bersama |
| 🗳️ **Sistem Voting & Rapat** | Laporkan mayat atau tekan tombol darurat untuk rapat dan voting |
| 🎯 **Penanda Visual** | Efek partikel, scoreboard, dan action bar untuk status game real-time |
| ⚡ **Auto-Reset Game** | Game otomatis reset setelah Crewmate atau Impostor menang |
| 📊 **Statistik Pemain** | Menyimpan data kemenangan, pembunuhan, dan tugas per pemain |
| 🛠️ **Customizable Config** | Semua pengaturan bisa diubah lewat `config.yml` |

</div>

---

## 🧩 Alur Mekanisme Permainan

```
flowchart TD
    Start((🎮 Game Dimulai)) --> Countdown[⏳ Countdown 10 Detik]
    Countdown --> Role{👥 Penentuan Role}
    
    Role --> Crew[🛠️ Crewmate]
    Role --> Imp[👾 Impostor]
    Role --> Joker[🃏 Joker]
    Role --> SkinWalker[👤 Skin Walker]
    
    Crew --> CrewTasks[📋 Mengerjakan Task]
    CrewTasks --> BuildBridge[🏗️ Deposit Kayu ke NPC Jembatan]
    BuildBridge --> CheckBridge{🌉 Jembatan Selesai?}
    CheckBridge -- Ya --> WinCrew[🏆 Crewmate Menang]
    CheckBridge -- Tidak --> CheckTask{✅ Semua Task Selesai?}
    CheckTask -- Ya --> WinCrew
    CheckTask -- Tidak --> Meeting[📢 Rapat Darurat / Voting]
    
    Imp --> Kill[🔪 Membunuh Crewmate]
    Imp --> Sabotage[⚡ Sabotase Lampu / Generator]
    Kill --> CheckAlive{☠️ Semua Crewmate Mati?}
    Sabotage --> CheckSabotage{🛠️ Sabotase Diperbaiki?}
    CheckSabotage -- Tidak & Waktu Habis --> WinImp[👾 Impostor Menang]
    CheckSabotage -- Ya --> CrewTasks
    CheckAlive -- Ya --> WinImp
    
    Joker --> JokerTask[📋 Mengerjakan Task]
    JokerTask --> JokerGoal{🗳️ Berhasil Di-Vote Keluar?}
    JokerGoal -- Ya --> WinJoker[🃏 Joker Menang]
    JokerGoal -- Tidak --> CheckAliveJoker{☠️ Crewmate/Impostor Menang?}
    CheckAliveJoker -- Ya --> LoseJoker[💀 Joker Kalah]
    
    SkinWalker --> SkinTask[📋 Mengerjakan Task]
    SkinTask --> SkinBlind[👤 Skill: Membutakan Pemain]
    SkinBlind --> SkinGoal{🗳️ Berhasil Di-Vote Keluar?}
    SkinGoal -- Ya --> WinSkinWalker[👤 Skin Walker Menang]
    SkinGoal -- Tidak --> CheckAliveSkin{☠️ Crewmate/Impostor Menang?}
    CheckAliveSkin -- Ya --> LoseSkin[💀 Skin Walker Kalah]
    
    Meeting --> Vote{🗳️ Hasil Voting}
    Vote -- Eliminasi --> Eject[💀 Pemain Tereliminasi]
    Vote -- Skip / Seri --> Continue[▶️ Lanjutkan Permainan]
    
    Eject --> CheckEject{⚖️ Jumlah Crew = Impostor?}
    CheckEject -- Ya --> WinImp
    CheckEject -- Tidak --> CheckCrewAlive{☠️ Semua Crewmate Mati?}
    CheckCrewAlive -- Ya --> WinImp
    CheckCrewAlive -- Tidak --> CrewTasks
    
    Continue --> CrewTasks

```

---

### 📝 Penjelasan Singkat Alur

| Fase | Deskripsi |
|------|-----------|
| **Countdown** | Pemain diberi waktu 10 detik sebelum game dimulai, role akan ditampilkan setelah countdown |
| **Role Assignment** | Pemain dibagi menjadi: Crewmate, Impostor, Joker, atau Skin Walker (bergantung konfigurasi) |
| **Crewmate** | Mengerjakan task untuk mendapatkan Oak Planks, deposit ke NPC jembatan hingga jembatan selesai |
| **Impostor** | Membunuh crewmate diam-diam dan melakukan sabotase lampu untuk menghambat progres |
| **Joker** | Mengerjakan task seperti crewmate, tetapi tujuan utamanya adalah di-vote keluar oleh pemain lain |
| **Skin Walker** | Mengerjakan task dan memiliki skill untuk membutakan pemain lain dalam radius tertentu |
| **Meeting** | Terjadi saat ada yang melaporkan mayat atau tombol darurat ditekan. Semua pemain teleport ke meeting center dan voting dilakukan |
| **Voting** | Pemain memilih tersangka dengan `/vote [player]` atau `/skip`. Jika mayoritas setuju, pemain dieliminasi |
| **Kemenangan Crewmate** | Jembatan selesai dibangun ATAU semua tugas selesai ATAU semua Impostor mati |
| **Kemenangan Impostor** | Jumlah Crewmate = Impostor ATAU semua Crewmate mati |
| **Kemenangan Joker** | Berhasil di-vote keluar oleh pemain lain |
| **Kemenangan Skin Walker** | Berhasil di-vote keluar oleh pemain lain |

---

# Dokumentasi lengkap plugin.

## 📚 Daftar Isi
- Cara Setting
- Cara Main
- Command
- Permission
- Role
- Setting
- FAQ

---

# 🏗️ Cara Setting

## Set Lokasi
```text
/ib setlobby
/ib setmap
/ib setmeeting
/ib setpulau1
/ib setpulau2
/ib setjembatankiri <lebar>
/ib setjembatankanan <lebar>
```

## NPC
```text
/ib setnpcjembatan [nama]
/ib setnpcname <nama>
```

## Task
```text
/setnpc <nama> <durasi>
/setpohon
/setchest
/ib autochest <radius>
/ib autooak <radius>
/ib autofarmland <radius>
```

## Sabotase
```text
/ib setlamp
/ib autolamp <radius>
/ib setgenerator
```

---

# 🎮 Cara Main

1. Jalankan `/ib start`
2. Role dibagikan otomatis.
3. Crewmate menyelesaikan task dan membangun jembatan.
4. Impostor membunuh serta melakukan sabotase.
5. Meeting dilakukan dengan `/vote`.
6. Tim yang memenuhi syarat kemenangan akan menang.

---

# 📋 Command

| Command | Fungsi |
|---|---|
| `/ib start` | Mulai game |
| `/ib stop` | Stop game |
| `/ib reload` | Reload plugin |
| `/ib export` | Export config |
| `/ib import <file>` | Import config |
| `/ib setting <key> <value>` | Ubah setting |
| `/ib testing <true/false>` | Testing mode |
| `/ib role <role>` | Force role |
| `/ib sabotage` | Sabotase |
| `/vote <player>` | Vote |
| `/skip` | Skip vote |

---

# 🔐 Permission

| Permission |
|---|
| islandbridge.* |
| islandbridge.admin |
| islandbridge.player |
| islandbridge.bridge |
| islandbridge.task |
| islandbridge.sabotage |
| islandbridge.setting |
| islandbridge.reload |
| islandbridge.testing |

---

# 👥 Role

- Crewmate
- Impostor
- Joker
- Skin Walker

---

# ⚙️ Setting

Gunakan:

```text
/ib setting <key> <value>
```

Key:
- cdkill
- minplayers
- countdown
- meetingtime
- joker
- impostorcount
- skinwalkerblind
- autodetectlamp
- autodetecttask

---

# ❓ FAQ

- Minimal player: 4
- Config: `plugins/IslandBridgeAmongUs/config.yml`
- Reload: `/ib reload`

---

## 🛠️ Instalasi

<div align="center">

### 📥 Langkah-langkah Instalasi

</div>

1. **Download** file `AmongUsIsland.jar` dari [**Release Terbaru**](https://github.com/IkhsanNotDev/IslandAmongUs/releases/latest)
2. **Letakkan** di folder `plugins/` server Spigot/Paper (versi 1.16.5+)
3. **Restart** server (disarankan) atau gunakan `/reload`
4. **Konfigurasi** file `plugins/AmongUsIsland/config.yml` sesuai keinginan
5. **Start game** dengan `/amongus start` (pastikan minimal 4 pemain online)

> ⚠️ **Catatan Penting**  
> Plugin ini membutuhkan **Java 17** dan **Spigot/Paper 1.16.5 ke atas**

---

# 🎙️ Plugin Wajib & Rekomendasi - Voice Chat

---

## 🔴 WAJIB: Simple Voice Chat

**Simple Voice Chat** adalah satu-satunya plugin yang **WAJIB** diinstall agar `IslandBridgeAmongUs` dapat berfungsi dengan baik. Plugin ini menyediakan sistem *proximity voice chat* yang memungkinkan pemain mendengar satu sama lain berdasarkan jarak di dalam game.

### Alasan Wajib
*   **Integrasi:** `IslandBridgeAmongUs` menggunakan `VoiceChatUtil` dan `SabotageManager` yang terintegrasi langsung dengan Simple Voice Chat.
*   **Automasi:** Sistem **mute/unmute** otomatis saat meeting, sabotase, dan game berakhir bergantung sepenuhnya pada API plugin ini.
*   **Fungsionalitas:** Tanpa plugin ini, fitur komunikasi inti dalam game tidak akan aktif.

---

### 📥 Download & Instalasi

#### Server (Paper/Spigot)
1.  Klik tombol di bawah untuk mengunduh plugin:
    [**DOWNLOAD SIMPLE VOICE CHAT**](https://www.curseforge.com/minecraft/bukkit-plugins/simple-voice-chat)
2.  Letakkan file `voicechat.jar` ke dalam folder `plugins/` server Anda.
3.  **Buka port UDP 24454** di firewall/router Anda (Voice data menggunakan protokol UDP).
4.  Restart server Anda.

#### Client (Player)
*   **Java Edition:** Install mod Simple Voice Chat sesuai dengan mod loader yang digunakan (Fabric/Forge/NeoForge).
*   **Bedrock Edition:** Wajib menggunakan plugin pendukung untuk Geyser:
    [**DOWNLOAD SIMPLEVOICE-GEYSER**](https://modrinth.com/plugin/simplevoice-geyser)

---

### ⚙️ Konfigurasi Server

Sesuaikan file `plugins/voicechat/voicechat-server.properties` Anda:

```properties
# Port UDP untuk voice chat
port=24454

# Jarak maksimum suara terdengar (block)
voice_distance=48.0

# Jarak berbisik
whisper_distance=6.0
```

---

## 🟡 REKOMENDASI TAMBAHAN

| Plugin | Fungsi | Keterangan |
| :--- | :--- | :--- |
| **PlaceholderAPI** | Integrasi Scoreboard | Sangat direkomendasikan untuk statistik game. |
| **LuckPerms** | Manajemen Permission | Memudahkan kontrol akses pemain. |
| **ProximityVoice** | Alternatif | Opsi lebih ringan jika diperlukan. |

---

## ⚠️ Catatan Penting

*   **Client-Side Mod:** Player **WAJIB** menginstall mod Simple Voice Chat di client mereka. Tanpa mod ini, suara tidak akan terdengar.
*   **Firewall:** Pastikan port `24454` (UDP) terbuka.
*   **Dependencies:** `IslandBridgeAmongUs` menggunakan `softdepend: [voicechat]` di `plugin.yml`, memastikan plugin ini dimuat lebih dahulu oleh sistem.

---

## 🔧 Integrasi IslandBridgeAmongUs

Plugin `IslandBridgeAmongUs` secara otomatis mendeteksi keberadaan Simple Voice Chat untuk melakukan:
1.  **Unmute** semua pemain saat game dimulai.
2.  **Mute** semua pemain saat sabotase terjadi.
3.  **Unmute** kembali saat sabotase diperbaiki atau game berakhir.

---

## 🧑‍💻 Development

<div align="center">

### 🔧 Build dari Source

</div>

```bash
# Clone repository
git clone https://github.com/IkhsanNotDev/IslandAmongUs.git

# Masuk ke direktori
cd IslandAmongUs

# Build dengan Maven
mvn clean package

# Hasil .jar ada di folder target/
```

**Teknologi yang digunakan:**
- [Spigot API](https://www.spigotmc.org/) - Framework utama
- [Configurate](https://github.com/SpongePowered/Configurate) - Konfigurasi advanced
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) - Dukungan placeholder

---

## 🤝 Kontribusi

<div align="center">

[![Issues](https://img.shields.io/badge/Report_Bug-FF5722?style=for-the-badge&logo=github&logoColor=white)](https://github.com/IkhsanNotDev/IslandAmongUs/issues)
[![Pull Requests](https://img.shields.io/badge/Pull_Request-4CAF50?style=for-the-badge&logo=github&logoColor=white)](https://github.com/IkhsanNotDev/IslandAmongUs/pulls)

</div>

Ditemukan bug? Ada saran fitur?  
Silakan buka **Issue** atau kirim **Pull Request** — kontribusi sangat terbuka! 🤝

---

## 📜 Lisensi

<div align="center">

MIT License

Copyright (c) 2026 **[IkhsanNotDev]**

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions...

</div>

---

## 🛠️ Kredit & Referensi

<div align="center">

### 👤 Kredit
| Peran | Nama |
| :--- | :--- |
| **Pengembang Utama** | [IkhsanNotDev](https://github.com/IkhsanNotDev) |

### 📚 Referensi & Inspirasi
| Kategori | Sumber |
| :--- | :--- |
| **Gameplay preview** | [Corazon & Lapar Gang](https://youtu.be/jjPeCMMWEoQ?si=cIcW8FIizzehxTcW) |
| **Inspirasi Utama** | [MrBeast - Among Us Series](https://youtu.be/FO9kMeIQI7M?si=p3rZaYegUn7uurkz) |
| **Referensi Tambahan** | [Innersloth - Among Us](https://www.innersloth.com/games/among-us/) |
| **Referensi Tambahan** | [Video Eksplorasi Tantangan](https://youtu.be/nH9R0Jpqeqc?si=z34mcQpbmojCuSLC) |

</div>

---

<div align="center">

### ⭐ Jangan lupa beri bintang di GitHub jika plugin ini bermanfaat! ⭐

---

**Made with ❤️ by [IkhsanNotDev]**

</div>
