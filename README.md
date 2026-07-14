<div align="center">

# 🚀 Among Us Island

### Minecraft Plugin - Social Deduction Game

[![Spigot](https://img.shields.io/badge/Spigot-1.16.5--1.20-yellow?style=for-the-badge&logo=spigotmc)](https://www.spigotmc.org/)
[![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java)](https://adoptium.net/)
[![Version](https://img.shields.io/badge/Version-1.0.0-blue?style=for-the-badge)](https://github.com/yourusername/AmongUsIsland/releases)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](https://opensource.org/licenses/MIT)
[![Discord](https://img.shields.io/badge/Discord-Join-7289DA?style=for-the-badge&logo=discord)](https://discord.gg/yourinvite)
[![Download](https://img.shields.io/badge/Download-Latest-ff69b4?style=for-the-badge&logo=github)](https://github.com/yourusername/AmongUsIsland/releases/latest)

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

## 🧩 Alur Game

<div align="center">

```mermaid
flowchart TD
    A[🎮 Game Dimulai] --> B{👥 Bagi Role}
    B --> C[🛠️ Crewmate: Dapat Tugas]
    B --> D[👾 Impostor: Dapat Cooldown Bunuh]
    C --> E[📋 Kerjakan Tugas]
    D --> F[⚡ Sabotase / 🔪 Bunuh]
    E --> G{✅ Semua Tugas Selesai?}
    G -->|Ya| H[🏆 Crewmate Menang]
    G -->|Tidak| I[📢 Laporkan Mayat / Darurat]
    I --> J[🗳️ Rapat & Voting]
    J --> K{⚖️ Terdakwa Digantung?}
    K -->|Ya| L[💀 Eliminasi Pemain]
    K -->|Tidak| M[▶️ Lanjutkan Game]
    L --> N{📊 Jumlah Crewmate = Impostor?}
    N -->|Ya| O[👾 Impostor Menang]
    N -->|Tidak| E
    F --> P{🛠️ Sabotase Diperbaiki?}
    P -->|Tidak & ⏰ Waktu Habis| Q[👾 Impostor Menang]
    P -->|Ya| E

</div>

---

## 📸 Screenshot

<div align="center">

| 🖥️ GUI Tugas | 📢 Pertemuan Darurat | ⚡ Sabotase Reaktor |
|:---:|:---:|:---:|
| *[Screenshot 1]* | *[Screenshot 2]* | *[Screenshot 3]* |

</div>

---

## 📦 Command & Permission

<div align="center">

| Command | Permission | Deskripsi |
|---------|------------|-----------|
| `/amongus start` | `amongus.admin` | Memulai game secara paksa |
| `/amongus stop` | `amongus.admin` | Menghentikan game paksa |
| `/amongus join` | `amongus.player` | Bergabung ke antrian game |
| `/amongus leave` | `amongus.player` | Keluar dari game |
| `/amongus stats [player]` | `amongus.player` | Melihat statistik pemain |
| `/amongus reload` | `amongus.admin` | Reload config tanpa restart |

</div>

> **Permission default**: `amongus.*` (semua akses) untuk admin.

---

## ⚙️ Konfigurasi (config.yml)

# ═══════════════════════════════════════
#  Among Us Island - Config.yml
# ═══════════════════════════════════════

# ─── Pengaturan Game ───
game:
  min-players: 4           # Minimal pemain untuk mulai
  max-players: 12          # Maksimal pemain
  impostor-count: 2        # Jumlah impostor
  game-time-seconds: 300   # Durasi game (detik)
  task-amount: 8           # Jumlah tugas per crewmate
  meeting-cooldown: 30     # Cooldown rapat (detik)
  kill-cooldown: 20        # Cooldown bunuh (detik)
  emergency-meetings: 1    # Jumlah tombol darurat

# ─── Daftar Tugas ───
tasks:
  - type: "WIRING"
    description: "Perbaiki panel kabel yang rusak"
  - type: "SCAN"
    description: "Pindai DNA di laboratorium"
  - type: "FUEL"
    description: "Isi bahan bakar pesawat"
  - type: "OXYGEN"
    description: "Isi ulang tabung oksigen"
  - type: "REACTOR"
    description: "Stabilkan reaktor nuklir"

# ─── Sabotase ───
sabotages:
  - reactor:
      repair-time: 30
      effect: "explosion"
  - lights:
      effect: "blindness"
  - oxygen:
      repair-time: 45
      effect: "drowning"

# ─── Pesan ───
messages:
  prefix: "&8[&cAmongUs&8]&r "
  game-start: "&aGame dimulai! Selamat bermain!"
  crewmate-win: "&a🏆 Crewmate menang! Tugas selesai semua!"
  impostor-win: "&c👾 Impostor menang! Semua crewmate mati!"
  task-complete: "&a✅ Tugas selesai! Tersisa {remaining} tugas"
  killed: "&c💀 Kamu dibunuh oleh Impostor!"
  voted-out: "&e🗳️ {player} dikeluarkan dari pesawat!"

---

## 🛠️ Instalasi

<div align="center">

### 📥 Langkah-langkah Instalasi

</div>

1. **Download** file `AmongUsIsland.jar` dari [**Release Terbaru**](https://github.com/yourusername/AmongUsIsland/releases/latest)
2. **Letakkan** di folder `plugins/` server Spigot/Paper (versi 1.16.5+)
3. **Restart** server (disarankan) atau gunakan `/reload`
4. **Konfigurasi** file `plugins/AmongUsIsland/config.yml` sesuai keinginan
5. **Start game** dengan `/amongus start` (pastikan minimal 4 pemain online)

> ⚠️ **Catatan Penting**  
> Plugin ini membutuhkan **Java 17** dan **Spigot/Paper 1.16.5 ke atas**

---

## 🧑‍💻 Development

<div align="center">

### 🔧 Build dari Source

</div>

# Clone repository
git clone https://github.com/yourusername/AmongUsIsland.git

# Masuk ke direktori
cd AmongUsIsland

# Build dengan Maven
mvn clean package

# Hasil .jar ada di folder target/

**Teknologi yang digunakan:**
- [Spigot API](https://www.spigotmc.org/) - Framework utama
- [Configurate](https://github.com/SpongePowered/Configurate) - Konfigurasi advanced
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) - Dukungan placeholder

---

## 🤝 Kontribusi

<div align="center">

[![Issues](https://img.shields.io/badge/Report_Bug-FF5722?style=for-the-badge&logo=github&logoColor=white)](https://github.com/yourusername/AmongUsIsland/issues)
[![Pull Requests](https://img.shields.io/badge/Pull_Request-4CAF50?style=for-the-badge&logo=github&logoColor=white)](https://github.com/yourusername/AmongUsIsland/pulls)
[![Discord](https://img.shields.io/badge/Join_Discord-7289DA?style=for-the-badge&logo=discord&logoColor=white)](https://discord.gg/yourinvite)

</div>

Ditemukan bug? Ada saran fitur?  
Silakan buka **Issue** atau kirim **Pull Request** — kontribusi sangat terbuka! 🤝

---

## 📜 Lisensi

<div align="center">

MIT License

Copyright (c) 2024 [Nama Kamu]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions...

</div>

---

## 🙏 Kredit

<div align="center">

| | |
|---|---|
| **Dikembangkan oleh** | **[Nama Kamu]** |
| **Inspirasi** | [Among Us](https://www.innersloth.com/games/among-us/) oleh Innersloth |
| **Kontributor** | Semua kontributor dan tester |

</div>

---

<div align="center">

### ⭐ Jangan lupa beri bintang di GitHub jika plugin ini bermanfaat! ⭐

---

**Made with ❤️ by [Nama Kamu]**

</div>
