```markdown
# 🚀 Among Us Island - Minecraft Plugin

[![Spigot](https://img.shields.io/badge/Spigot-1.16.5--1.20-yellow)](https://www.spigotmc.org/)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://adoptium.net/)
[![Version](https://img.shields.io/badge/Version-1.0.0-blue)]()
[![License](https://img.shields.io/badge/License-MIT-green)]()

> **Plugin Minecraft bergaya Among Us yang berjalan di dalam satu pulau (Island).**  
> Rasakan pengalaman bermain *social deduction* dengan tugas-tugas unik, pertemuan darurat, dan sistem sabotase—semuanya dalam dunia blok!

---

## 📖 Deskripsi

**Among Us Island** adalah plugin yang mengubah server Minecraft-mu menjadi arena permainan *Among Us* versi *survival*. Para pemain akan dibagi menjadi dua tim:

- **Crewmate (Awak)** — Bertugas menyelesaikan semua misi/tugas di pulau sebelum waktu habis.
- **Impostor (Penyusup)** — Menyamar sebagai awak, menyabotase fasilitas, dan membunuh crewmate tanpa ketahuan.

Game berlangsung di satu pulau dengan area-area khusus (kantin, reaktor, laboratorium, dll.) yang bisa kamu kustomisasi.

---

## ✨ Fitur Unggulan

| Fitur | Keterangan |
|-------|------------|
| 🎮 **Sistem Role Otomatis** | Pemain dibagi secara acak menjadi Crewmate atau Impostor di setiap ronde. |
| 📋 **Daftar Tugas Interaktif** | Setiap Crewmate mendapat daftar tugas unik (memperbaiki panel, mengisi bahan bakar, memindai, dll.) dengan GUI/action. |
| 🔪 **Mekanisme Pembunuhan** | Impostor bisa membunuh dengan cooldown, meninggalkan mayat yang bisa dilaporkan. |
| ⚙️ **Sabotase Global** | Impostor bisa memicu sabotase (reaktor meledak, listrik padam, O2 bocor) yang harus segera diperbaiki bersama. |
| 🗳️ **Sistem Voting & Rapat** | Pemain bisa melaporkan mayat atau menekan tombol darurat untuk memulai rapat darurat. Diskusi + voting untuk mengeluarkan tersangka. |
| 🎯 **Penanda Visual** | Efek partikel, scoreboard, dan action bar untuk menunjukkan status game (tugas tersisa, cooldown, dll.). |
| ⚡ **Auto-Reset Game** | Setelah Crewmate menang (tugas selesai) atau Impostor menang (jumlah crewmate = impostor), game otomatis reset. |
| 📊 **Statistik Pemain** | Menyimpan data kemenangan, pembunuhan, dan tugas yang diselesaikan per pemain (opsional dengan database). |
| 🛠️ **Customizable Config** | Semua pengaturan (durasi game, cooldown, jumlah tugas, dll.) bisa diubah lewat `config.yml`. |

---

## 📸 Screenshot

| GUI Tugas | Pertemuan Darurat | Sabotase Reaktor |
|-----------|-------------------|------------------|
| *[Tambahkan screenshot GUI tugas di sini]* | *[Tambahkan screenshot rapat darurat di sini]* | *[Tambahkan screenshot sabotase di sini]* |

---

## 🧩 Cara Kerja Alur Game

```mermaid
flowchart TD
    A[Game Dimulai] --> B{Bagi Role}
    B --> C[Crewmate: Dapat Tugas]
    B --> D[Impostor: Dapat Cooldown Bunuh]
    C --> E[Kerjakan Tugas]
    D --> F[Sabotase / Bunuh]
    E --> G{Semua Tugas Selesai?}
    G -->|Ya| H[Crewmate Menang]
    G -->|Tidak| I[Laporkan Mayat / Darurat]
    I --> J[Rapat & Voting]
    J --> K{Terdakwa Digantung?}
    K -->|Ya| L[Eliminasi Pemain]
    K -->|Tidak| M[Lanjutkan Game]
    L --> N{Jumlah Crewmate = Impostor?}
    N -->|Ya| O[Impostor Menang]
    N -->|Tidak| E
    F --> P{Sabotase Diperbaiki?}
    P -->|Tidak & Waktu Habis| Q[Impostor Menang]
    P -->|Ya| E
```

---

## 📦 Command & Permission

| Command | Permission | Deskripsi |
|---------|------------|-----------|
| `/amongus start` | `amongus.admin` | Memulai game secara paksa |
| `/amongus stop` | `amongus.admin` | Menghentikan game paksa |
| `/amongus join` | `amongus.player` | Bergabung ke antrian game |
| `/amongus leave` | `amongus.player` | Keluar dari game |
| `/amongus stats [player]` | `amongus.player` | Melihat statistik pemain |
| `/amongus reload` | `amongus.admin` | Reload config tanpa restart |

> **Permission default**: `amongus.*` (semua akses) untuk admin.

---

## ⚙️ Konfigurasi (config.yml)

```yaml
# config.yml - Contoh pengaturan
game:
  min-players: 4
  max-players: 12
  impostor-count: 2
  game-time-seconds: 300
  task-amount: 8
  meeting-cooldown: 30
  kill-cooldown: 20
  emergency-meetings: 1

tasks:
  - type: "WIRING"
    description: "Perbaiki panel kabel yang rusak"
  - type: "SCAN"
    description: "Pindai DNA di laboratorium"
  - type: "FUEL"
    description: "Isi bahan bakar pesawat"

sabotages:
  - reactor:
      repair-time: 30
      effect: "explosion"
  - lights:
      effect: "blindness"

messages:
  prefix: "&8[&cAmongUs&8]&r "
  game-start: "&aGame dimulai! Selamat bermain!"
  crewmate-win: "&aCrewmate menang! Tugas selesai semua!"
  impostor-win: "&cImpostor menang! Semua crewmate mati!"
```

---

## 🛠️ Instalasi

1. **Download** file `AmongUsIsland.jar` dari rilis terbaru.
2. **Letakkan** di folder `plugins/` server Spigot/Paper (versi 1.16.5+).
3. **Restart** atau `/reload` server (disarankan restart).
4. **Konfigurasi** `plugins/AmongUsIsland/config.yml` sesuai keinginan.
5. **Start game** dengan `/amongus start` (pastikan minimal 4 pemain online).

> ⚠️ **Catatan**: Plugin ini membutuhkan **Java 17** dan **Spigot/Paper 1.16.5 ke atas**.

---

## 🧑‍💻 Development & Build (untuk Developer)

```bash
# Clone repository
git clone https://github.com/username/AmongUsIsland.git

# Build dengan Maven
mvn clean package

# Hasil .jar ada di folder target/
```

**Teknologi yang digunakan:**
- [Spigot API](https://www.spigotmc.org/) - Framework utama
- [Configurate](https://github.com/SpongePowered/Configurate) - Konfigurasi lebih advanced (opsional)
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) - Dukungan placeholder untuk plugin lain

---

## 🐛 Issue & Kontribusi

Ditemukan bug? Ada saran fitur?  
Silakan buka **Issue** di [GitHub Issues](https://github.com/username/AmongUsIsland/issues) atau kirim **Pull Request**.

Kontribusi sangat terbuka! 🤝

---

## 📜 Lisensi

Proyek ini dilisensikan di bawah **MIT License** — bebas digunakan, dimodifikasi, dan didistribusikan.

---

## 🙏 Kredit

- Dikembangkan oleh: **[Nama Kamu]**
- Inspirasi dari game [Among Us](https://www.innersloth.com/games/among-us/) oleh Innersloth
- Terima kasih untuk semua kontributor dan tester!

---

⭐ **Jangan lupa beri bintang di GitHub jika plugin ini bermanfaat!** ⭐
```