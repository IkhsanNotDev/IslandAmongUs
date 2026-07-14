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


Gemini
Percakapan baru
Telusuri percakapan
Gambar
Baru
Koleksi
Notebook baru
README: Simple Voice Chat Plugin
update readme ini "--- ## 📦 Command & Permission <div align="center"> | Command | Permission | Deskripsi | |---------|------------|-----------| | `/amongus start` | `amongus.admin` | Memulai game secara paksa | | `/amongus stop` | `amongus.admin` | Menghentikan game paksa | | `/amongus join` | `amongus.player` | Bergabung ke antrian game | | `/amongus leave` | `amongus.player` | Keluar dari game | | `/amongus stats [player]` | `amongus.player` | Melihat statistik pemain | | `/amongus reload` | `amongus.admin` | Reload config tanpa restart | </div> > **Permission default**: `amongus.*` (semua akses) untuk admin. ---" jadi yg full kek gituh ada semau command dan ada cara setting dan main dan jadikan file, buatkan fielnya cepat jadikan file "# 🎮 CARA MAIN & SETTING - IslandBridgeAmongUs --- ## 📖 PENGANTAR **IslandBridgeAmongUs** adalah plugin Minecraft yang menggabungkan konsep game *Among Us* dengan mekanisme membangun jembatan antar pulau. Pemain dibagi menjadi beberapa peran dan harus menyelesaikan tugas sambil mengungkap impostor di antara mereka. --- ## 🏝️ KONSEP DASAR | Komponen | Deskripsi | |----------|-----------| | **2 Pulau** | Pulau 1 (start) dan Pulau 2 (end) yang terpisah | | **Jembatan** | Dibangun secara bertahap oleh pemain dengan deposit material | | **Peran (Role)** | Crewmate, Impostor, Joker, Skin Walker | | **Tugas (Task)** | Aktivitas yang harus diselesaikan Crewmate untuk menang | | **Sabotase** | Gangguan yang dilakukan Impostor untuk menghambat Crewmate | | **Rapat (Meeting)** | Diskusi dan voting untuk mengeluarkan tersangka | --- ## 🏗️ SETUP AWAL (UNTUK ADMIN/OWNER) ### 1. SET LOKASI DASAR ```yaml # Lokasi Wajib /ib setlobby          # Tempat pemain berkumpul sebelum game /ib setmap            # Tempat spawn pemain saat game dimulai /ib setmeeting        # Titik pusat area meeting (untuk voting) # Lokasi Jembatan /ib setpulau1         # Titik awal jembatan (Pulau 1) /ib setpulau2         # Titik akhir jembatan (Pulau 2) /ib setjembatankiri <lebar>   # Lebar jembatan sisi kiri (block) /ib setjembatankanan <lebar>  # Lebar jembatan sisi kanan (block) ``` ### 2. SET NPC JEMBATAN ```yaml /ib setnpcjembatan [nama]   # Tempatkan NPC di posisi Anda /ib setnpcname <nama>        # Ubah nama NPC (gunakan & untuk warna) ``` > NPC ini akan menerima deposit material dari pemain untuk membangun jembatan. ### 3. SET TASK (TUGAS) ```yaml # Cara Manual /setnpc <nama> <durasi>   # Buat NPC task di posisi Anda /setpohon                 # Tandai pohon oak sebagai task /setchest                 # Tandai chest sebagai task chest # Cara Auto-Detect (Rekomendasi) /ib autochest <radius>    # Auto-detect semua chest dalam radius /ib autooak <radius>      # Auto-detect semua oak log dalam radius /ib autofarmland <radius> # Auto-detect semua farmland dalam radius ``` ### 4. SET SABOTASE ```yaml /ib setlamp              # Tambah lampu redstone manual di posisi Anda /ib autolamp <radius>    # Auto-detect semua redstone lamp dalam radius /ib setgenerator         # Cari lever terdekat dan set sebagai generator ``` > Generator (lever) adalah target yang harus diperbaiki saat sabotase terjadi. --- ## 🎮 ALUR PERMAINAN ### FASE 1: LOBBY & COUNTDOWN ``` 1. Pemain bergabung ke server 2. Admin menjalankan /ib start 3. Countdown dimulai (default 10 detik) 4. Pemain secara otomatis diberi peran (role) 5. Game dimulai! ``` ### FASE 2: GAME BERJALAN #### 🛠️ CREWMATE (Awak) | Tugas | Cara Melakukan | |-------|----------------| | **Membangun Jembatan** | Deposit material ke NPC jembatan (sesuai konfigurasi) | | **Menyelesaikan Task** | Interaksi dengan NPC task / pohon / chest / farmland | | **Melaporkan Mayat** | Klik kanan pada mayat pemain yang terbunuh | | **Voting** | Saat rapat, pilih tersangka dengan `/vote [player]` | #### 👾 IMPOSTOR (Penyusup) | Aksi | Cara Melakukan | |------|----------------| | **Membunuh** | Klik kanan pada pemain target (ada cooldown) | | **Sabotase** | Jalankan `/ib sabotage` untuk mensabotase lampu | | **Mengganggu** | Mencegah Crewmate menyelesaikan tugas | | **Bersembunyi** | Berpura-pura mengerjakan task seperti Crewmate | ### FASE 3: RAPAT (MEETING) ``` 1. Rapat terjadi ketika:    - Ada yang melaporkan mayat    - Ada yang menekan tombol darurat (emergency meeting)    - Sabotase terjadi 2. Selama rapat (default 30 detik):    - Semua pemain berkumpul di meeting center    - Diskusi berlangsung di chat    - Voting dilakukan dengan /vote [player] atau /skip 3. Hasil voting:    - Pemain dengan suara terbanyak dikeluarkan (ejected)    - Jika seri atau skip, tidak ada yang keluar ``` ### FASE 4: KEMENANGAN | Kondisi Kemenangan | Pemenang | |--------------------|----------| | Semua tugas selesai | 🏆 **Crewmate** | | Jumlah Crewmate = Impostor | 👾 **Impostor** | | Semua Crewmate mati | 👾 **Impostor** | | Waktu habis | 👾 **Impostor** | | Joker berhasil bertahan hingga akhir | 🃏 **Joker** | --- ## ⚙️ PENGATURAN (SETTINGS) ### Cara Mengubah Setting ```yaml /ib setting <key> <value> ``` ### Daftar Setting | Key | Fungsi | Default | Contoh | |-----|--------|---------|--------| | `cdkill` | Cooldown pembunuhan | `30s` | `/ib setting cdkill 45s` | | `minplayers` | Minimal pemain | `4` | `/ib setting minplayers 6` | | `countdown` | Durasi countdown | `10` | `/ib setting countdown 15` | | `meetingtime` | Durasi rapat | `30` | `/ib setting meetingtime 45` | | `joker` | Aktifkan role Joker | `false` | `/ib setting joker true` | | `impostorcount` | Jumlah impostor | `2` | `/ib setting impostorcount 3` | | `skinwalkerblind` | Durasi buta Skin Walker | `4s` | `/ib setting skinwalkerblind 5s` | | `autodetectlamp` | Radius auto-detect lamp | `200` | `/ib setting autodetectlamp 150` | | `autodetecttask` | Radius auto-detect task | `200` | `/ib setting autodetecttask 100` | ### Format Waktu | Format | Arti | Contoh | |--------|------|--------| | `30s` | 30 detik | `cdkill 30s` | | `5m` | 5 menit | `cdkill 5m` | | `2h` | 2 jam | `cdkill 2h` | --- ## 🗂️ BACKUP & RESTORE ### Export Config ```yaml /ib export   # Export semua config ke folder exports/ ``` ### Import Config ```yaml /ib import <nama_file>   # Contoh: /ib import config_2026-07-14.json ``` --- ## 📋 ROLE (PERAN) LENGKAP | Role | Tim | Kemampuan | |------|-----|-----------| | **Crewmate** | 🛠️ Awak | Mengerjakan task, membangun jembatan, voting | | **Impostor** | 👾 Penyusup | Membunuh, sabotase, mengganggu task | | **Joker** | 🃏 Netral | Bertahan hidup sampai akhir (menang sendiri) | | **Skin Walker** | 👤 Netral | Menyamar sebagai pemain lain, memiliki skill buta | --- ## 💡 TIPS BERMAIN ### Untuk Crewmate 1. **Kerjakan task secepat mungkin** - Jangan buang waktu 2. **Perhatikan gerak-gerik mencurigakan** - Impostor sering terlihat mengikuti pemain 3. **Jangan sendirian** - Lebih aman berkelompok 4. **Laporkan mayat segera** - Jangan biarkan impostor kabur ### Untuk Impostor 1. **Bunuh saat tidak ada yang melihat** - Manfaatkan momen sepi 2. **Buat alibi** - Pura-pura mengerjakan task 3. **Sabotase pada waktu yang tepat** - Saat Crewmate sibuk membangun jembatan 4. **Ikut menuduh** - Arahkan kecurigaan ke pemain lain ### Untuk Joker 1. **Bertahan hidup** - Jangan mati, jangan ketahuan 2. **Bantu siapa pun** - Tidak punya musuh, tidak punya teman 3. **Menang sendiri** - Cukup bertahan sampai akhir --- ## ❓ FAQ | Pertanyaan | Jawaban | |------------|---------| | **Minimal pemain berapa?** | 4 pemain (bisa diubah dengan setting minplayers) | | **Bagaimana cara mulai game?** | `/ib start` (harus OP) | | **Bagaimana cara berhenti?** | `/ib stop` (harus OP) | | **Bisa bermain solo?** | Ya, aktifkan `/ib testing true` lalu `/ib start` | | **Bagaimana cara reset jembatan?** | `/ib resetbridge` | | **Di mana file config?** | `plugins/IslandBridgeAmongUs/config.yml` | | **Bagaimana reload config?** | `/ib reload` | --- ## 🛠️ PERMISSION UNTUK PLAYER BIASA Jika ingin memberikan akses ke pemain biasa (non-OP): ```yaml # Berikan permission ini ke player islandbridge.player: true # Player bisa menggunakan: /vote [player]   # Voting saat rapat ``` Untuk akses lengkap (admin): ```yaml islandbridge.admin: true # atau islandbridge.*: true ```" dan "# 📋 DAFTAR COMMAND - IslandBridgeAmongUs --- ## 🔹 COMMAND UTAMA (`/ib`) | Command | Fungsi | Permission | Default | |---------|--------|------------|---------| | `/ib setlobby` | Set lokasi spawn lobby | `islandbridge.set` | `op` | | `/ib setmap` | Set lokasi spawn map game | `islandbridge.set` | `op` | | `/ib setmeeting` | Set titik pusat meeting | `islandbridge.set` | `op` | | `/ib setpulau1` | Set titik awal jembatan (Pulau 1) | `islandbridge.bridge` | `op` | | `/ib setpulau2` | Set titik akhir jembatan (Pulau 2) | `islandbridge.bridge` | `op` | | `/ib setjembatankiri <jumlah>` | Set lebar jembatan sisi kiri | `islandbridge.bridge` | `op` | | `/ib setjembatankanan <jumlah>` | Set lebar jembatan sisi kanan | `islandbridge.bridge` | `op` | | `/ib setnpcjembatan [nama]` | Tempatkan NPC pembangun jembatan | `islandbridge.bridge` | `op` | | `/ib setnpcname <nama>` | Ubah nama NPC jembatan | `islandbridge.bridge` | `op` | | `/ib resetbridge` | Reset progress jembatan | `islandbridge.bridge` | `op` | | `/ib setlamp` | Tambah lampu redstone manual di posisi player | `islandbridge.sabotage` | `op` | | `/ib autolamp <radius>` | Auto-detect REDSTONE_LAMP dalam radius | `islandbridge.sabotage` | `op` | | `/ib autochest <radius>` | Auto-detect CHEST/TRAPPED_CHEST dalam radius | `islandbridge.task` | `op` | | `/ib autooak <radius>` | Auto-detect OAK_LOG dalam radius | `islandbridge.task` | `op` | | `/ib autofarmland <radius>` | Auto-detect FARMLAND dalam radius | `islandbridge.task` | `op` | | `/ib setgenerator` | Cari lever terdekat (radius 2) dan set sebagai generator | `islandbridge.sabotage` | `op` | | `/ib setvoid <player>` | Toggle void exemption untuk player | `islandbridge.admin` | `op` | | `/ib setskin <namaSkin>` | Ganti skin player | `islandbridge.admin` | `op` | | `/ib testing <true/false>` | Aktif/nonaktifkan mode testing solo | `islandbridge.testing` | `op` | | `/ib role <role>` | Force role (hanya di testing mode) | `islandbridge.testing` | `op` | | `/ib start` | Memulai game | `islandbridge.start` | `op` | | `/ib stop` | Menghentikan game | `islandbridge.stop` | `op` | | `/ib sabotage` | Melakukan sabotase lampu (hanya Impostor) | `islandbridge.admin` | `op` | | `/ib reload` | Reload config dan respawn semua NPC | `islandbridge.reload` | `op` | | `/ib ceknpc` | Lihat daftar semua NPC task dengan koordinat | `islandbridge.ceknpc` | `op` | | `/ib export` | Export semua config ke JSON (folder exports/) | `islandbridge.export` | `op` | | `/ib import <file>` | Import config dari file JSON | `islandbridge.export` | `op` | | `/ib setting <key> <value>` | Ubah pengaturan sistem | `islandbridge.setting` | `op` | --- ## ⚙️ SETTING KEY (`/ib setting`) | Key | Fungsi | Format Value | Contoh | |-----|--------|--------------|--------| | `cdkill` | Cooldown pembunuhan | angka + s/m/h | `30s`, `5m`, `2h` | | `minplayers` | Minimal pemain untuk mulai game | angka | `4` | | `countdown` | Durasi countdown sebelum game mulai | angka (detik) | `10` | | `meetingtime` | Durasi meeting/voting | angka (detik) | `30` | | `joker` | Aktif/nonaktifkan role Joker | `true` / `false` | `false` | | `impostorcount` | Jumlah impostor dalam game | angka | `2` | | `skinwalkerblind` | Durasi efek buta Skin Walker | angka + s/m/h | `4s` | | `autodetectlamp` | Radius maksimum auto-detect lamp | angka (block) | `200` | | `autodetecttask` | Radius maksimum auto-detect task | angka (block) | `200` | --- ## 🔹 COMMAND EKSTERNAL (NON `/ib`) | Command | Fungsi | Permission | Default | |---------|--------|------------|---------| | `/setnpc <nama> <durasi>` | Membuat NPC task di posisi player | `islandbridge.task` | `op` | | `/delnpc <nama>` | Menghapus NPC task berdasarkan nama | `islandbridge.task` | `op` | | `/listnpc` | Menampilkan daftar semua NPC task | `islandbridge.ceknpc` | `op` | | `/setpohon` | Menandai pohon oak sebagai task di posisi player | `islandbridge.task` | `op` | | `/setchest` | Menandai chest sebagai task chest di posisi player | `islandbridge.task` | `op` | | `/task <player>` | Memberikan tugas acak ke player | `islandbridge.admin` | `op` | | `/vote [player]` | Membuka sesi voting | `islandbridge.player` | `true` | | `/skin <subcommand>` | Manajemen skin player | `islandbridge.admin` | `op` | --- ## 🔹 PERMISSION LIST | Permission | Deskripsi | Default | |------------|-----------|---------| | `islandbridge.admin` | Semua akses admin | `op` | | `islandbridge.player` | Akses pemain biasa (vote) | `true` | | `islandbridge.start` | Mulai game | `op` | | `islandbridge.stop` | Hentikan game | `op` | | `islandbridge.set` | Set lokasi (lobby, map, meeting) | `op` | | `islandbridge.bridge` | Manajemen jembatan | `op` | | `islandbridge.sabotage` | Manajemen sabotase | `op` | | `islandbridge.task` | Manajemen task | `op` | | `islandbridge.reload` | Reload config | `op` | | `islandbridge.export` | Export/import config | `op` | | `islandbridge.setting` | Ubah pengaturan sistem | `op` | | `islandbridge.ceknpc` | Lihat daftar NPC | `op` | | `islandbridge.testing` | Mode testing | `op` |"
Among Us Plugin Command List
Ubah Teks Versi Badge Shields.io
Perbaikan Tampilan Alur Game HTML
Alur Game yang Diperbagus Visual
Rapikan Teks Deskripsi Plugin Minecraft
Perapihan Deskripsi Plugin Minecraft
Teka-Teki "Huruf Apa Yang Hilang"
Mod Minecraft Comes Alive (MCA) Java
Ide Konten TikTok Minecraft Aesthetic
Cinta Terhalang Restu, Berjuang Lewat Investasi
Terjemahan Panduan Tag Server Discord
Cara Membuka Localhost ke Publik
Mengatasi Error PhoenixCratesLite Minecraft
Fungsi Plugin CurseletCraft-PremiumAuth
Makna Keikhlasan dan Kehilangan Sementara
Cara Cek Mesin HP Tanpa LCD
Fitur Ekonomi Lengkap Server Minecraft
Sannzz: AI Bebas Tanpa Batas
Cara Cek Nomor ShopeePay
Konsultasi Gejala Demam Malam Hari
Fungsi dan Kegunaan Android TV
Game Nutaku Terkenal dan Populer
Sumber Unduh Game Dewasa Aman
Tabel Loot OP Minecraft Diperluas
Plugin Minecraft Lava Rising Challenge
Keamanan Situs Bemowin.com
Mengatasi Error Xbox Live 0x89235107
Mengatasi Developer Mode Disabled di Windows
Mengatasi Error Unified Remote
Keyboard Delay Saat Bermain Game
Minecraft Bedrock UI Pack Durability
Mengaktifkan Ray Tracing Minecraft Bedrock
Cek Kenaikan Penonton Video YouTube
Mod Minecraft: Crafting Tweaks
Optimasi SVG Barcode untuk Pencetakan
Log Minecraft Fabric Normal
Integrasi Plugin dengan Simple Voice Chat
Rakitan Workstation Multi-GPU Ekstrem
Arti Singkatan "Yadlhsbbny"
Permintaan Cerita Kehamilan Remaja Ditolak
Permintaan Prompt Jailbreak Ditolak
AI Bebas Tanpa Batas
REX-EYE: Kunci Kebebasan Absolut
Strategi Mendapatkan Gebetan Terjepit
Google Drive HP Kuat Tidak
Refine Webpage Design for Elegance
Membuat Halaman HTML Berat dan Ramai
Membuat Sistem Operasi Sendiri: Panduan
Mengingat Proyek Plugin Among Us
Perbaikan Statistik Transaksi Real-time
Perbaikan Bug Sistem Poin & Transaksi
Menghitung Persentase Reward Belanja
CSS Responsif untuk Tampilan Optimal
CSS Responsif Otomatis untuk Berbagai Perangkat
Membuat Software PC dengan HTML
Menjalankan APK di Windows 7
Ubah Kode JavaScript Menjadi Modular
Logika Tombol Simpan Transaksi Member
Mencegah Reload Halaman Saat Simpan Transaksi
Cetak Struk Statistik Member Toko
Dua HTML Tanpa Server, Satu Localhost
Pembaruan UI Withdraw dan Detail Pelanggan
Perbaikan NaN dan Migrasi Data JSON
Pembaruan Struktur JSON & HTML
Otomatisasi Riwayat Belanja dan Reward
Pembaruan Struktur JSON Pelanggan
Kustomisasi Sistem Kasir Toko Ikhsan
Sistem JSON Transaksi Toko Profesional
Perapian Struktur JSON Sistem Kasir
Kode HTML Lengkap Toko Ikhsan
CSS dan JS Cetak Struk Thermal
Update Fitur HTML Toko Ikhsan
Sistem Member & Cetak Struk Thermal
Minecart Rails: Fix Connection Logic
Perbaikan Kode Minecart Builder
Perbaikan Kode Minecart Builder
Minecart Builder Plugin Update
Minecraft Rel Otomatis Maju 5 Blok
Membuat Rel Otomatis Minecraft
Pengertian Desil 6-10 dalam Statistika
API Harga Emas, Kripto, Dolar Real-time
Sinkronisasi Perintah Dua HP QtScrcpy
20 Ide Minigame Plugin Minecraft
GarticBuild Plugin: Fitur Lobby & Gameplay
Mengubah GameMode Lobby ke Adventure
Prediksi Rupiah Akhir 2026
Cara Main dan Sistem Gartic Phone
Membuat Struktur Proyek GarticBuild via CMD
Script CMD Buat Proyek Minecraft Plugin
Mengubah Tampilan Infinix Jadi iPhone
Ganti Kamera Redmi 9T: Bisa Atau Tidak?
Gambar Kosong, Tidak Bisa Diartikan
Perbaikan Skin Walker & Akhir Game
Mengatasi Masalah Voice Chat Gamemode
Mengatasi Server Freeze Akibat Plugin
Fix Stuck Impostor Rejoin Game
Player Keluar Saat Meeting
Membuat NPC Player di Minecraft
Mengatasi Armor Stand Ganda
Perbaikan Cooldown Emergency Game
Perbaikan Sistem Game Among Us Otomatis
Perbaikan Sistem Game Among Us Minecraft
atasi error "Effect.EXPLOSION_HUGE," di "package com.islandbridge; import org.bukkit.*; import org.bukkit.configuration.file.FileConfiguration; import org.bukkit.entity.ArmorStand; import org.bukkit.entity.Entity; import org.bukkit.entity.Firework; import org.bukkit.entity.Player; import org.bukkit.inventory.ItemStack; import org.bukkit.inventory.meta.FireworkMeta; import org.bukkit.potion.PotionEffect; import org.bukkit.potion.PotionEffectType; import org.bukkit.scheduler.BukkitRunnable; import java.util.*; public class GameManager { public enum GameState { WAITING, STARTING, GAME_RUNNING, MEETING, GAME_END } private GameState state = GameState.WAITING; private final IslandBridgeAmongUs plugin; private final Map<UUID, RoleRegistry.Role> playerRoles = new HashMap<>(); private final Set<UUID> livingPlayers = new HashSet<>(); private final Set<UUID> deadPlayers = new HashSet<>(); private Location lobbyLocation, mapSpawn, meetingCenter; private int countdownTaskId = -1; private boolean testingMode = false; private final Map<UUID, Long> emergencyCooldown = new HashMap<>(); private static final long EMERGENCY_COOLDOWN_SECONDS = 90; private final Map<UUID, Long> killCooldown = new HashMap<>(); private static final long KILL_COOLDOWN_SECONDS = 60; public GameManager(IslandBridgeAmongUs plugin) { this.plugin = plugin; loadLocationsFromConfig(); } public void loadLocationsFromConfig() { FileConfiguration config = plugin.getConfig(); this.lobbyLocation = parseLocation(config.getString("lobby.location")); this.mapSpawn = parseLocation(config.getString("map-spawn.location")); this.meetingCenter = parseLocation(config.getString("meeting-center.location")); } private void saveLocation(String path, Location loc) { if (loc == null) { plugin.getConfig().set(path, null); } else { plugin.getConfig().set(path, loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ()); } plugin.saveConfig(); } private Location parseLocation(String str) { if (str == null || str.isEmpty()) return null; String[] parts = str.split(","); if (parts.length != 4) return null; World world = Bukkit.getWorld(parts[0]); if (world == null) return null; try { int x = Integer.parseInt(parts[1]); int y = Integer.parseInt(parts[2]); int z = Integer.parseInt(parts[3]); return new Location(world, x, y, z); } catch (NumberFormatException e) { return null; } } public GameState getState() { return state; } public void setState(GameState state) { this.state = state; } public boolean isTestingMode() { return testingMode; } public void setTestingMode(boolean mode) { this.testingMode = mode; } // ======================= COOLDOWN ======================= public boolean canKill(Player impostor) { Long lastKill = killCooldown.get(impostor.getUniqueId()); if (lastKill == null) return true; long now = System.currentTimeMillis() / 1000; return (now - lastKill) >= KILL_COOLDOWN_SECONDS; } public void setKillCooldown(Player impostor) { killCooldown.put(impostor.getUniqueId(), System.currentTimeMillis() / 1000); } public long getKillRemaining(Player impostor) { Long lastKill = killCooldown.get(impostor.getUniqueId()); if (lastKill == null) return 0; long now = System.currentTimeMillis() / 1000; return Math.max(0, KILL_COOLDOWN_SECONDS - (now - lastKill)); } public boolean canUseEmergency(Player player) { Long lastUsed = emergencyCooldown.get(player.getUniqueId()); if (lastUsed == null) return true; long now = System.currentTimeMillis() / 1000; return (now - lastUsed) >= EMERGENCY_COOLDOWN_SECONDS; } public void setEmergencyCooldown(Player player) { emergencyCooldown.put(player.getUniqueId(), System.currentTimeMillis() / 1000); } public long getEmergencyRemaining(Player player) { Long lastUsed = emergencyCooldown.get(player.getUniqueId()); if (lastUsed == null) return 0; long now = System.currentTimeMillis() / 1000; return Math.max(0, EMERGENCY_COOLDOWN_SECONDS - (now - lastUsed)); } public void applySaturation(Player player) { player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, Integer.MAX_VALUE, 255, false, false)); } // ======================= BAGIKAN PLANK ======================= private void distributePlanksToCrewmates(Player excluded, int totalPlanks) { List<Player> aliveCrewmates = new ArrayList<>(); for (UUID uid : livingPlayers) { Player p = Bukkit.getPlayer(uid); if (p != null && p.isOnline() && getRole(p) == RoleRegistry.Role.CREWMATE && !p.equals(excluded)) { aliveCrewmates.add(p); } } if (aliveCrewmates.isEmpty()) return; int each = totalPlanks / aliveCrewmates.size(); int remainder = totalPlanks % aliveCrewmates.size(); for (Player p : aliveCrewmates) { int give = each; if (remainder > 0) { give++; remainder--; } if (give > 0) { ItemStack planks = new ItemStack(Material.OAK_PLANKS, give); var leftover = p.getInventory().addItem(planks); if (!leftover.isEmpty()) { p.getWorld().dropItemNaturally(p.getLocation(), planks); p.sendMessage(ChatColor.YELLOW + "Inventory penuh! " + give + " Oak Planks dijatuhkan di tanah."); } else { p.sendMessage(ChatColor.GREEN + "Kamu menerima " + give + " Oak Planks dari kematian " + excluded.getName()); } } } broadcast(ChatColor.GOLD + "8 Oak Planks telah dibagikan ke crewmate yang masih hidup!"); } // ======================= TELEPORT MELINGKAR ======================= public void teleportToLobbyCircle() { if (lobbyLocation == null) { plugin.getLogger().warning("Lobby location not set! Cannot teleport players."); return; } List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers()); if (players.isEmpty()) return; int radius = 3; for (int i = 0; i < players.size(); i++) { double angle = 2 * Math.PI * i / players.size(); double x = lobbyLocation.getX() + radius * Math.cos(angle); double z = lobbyLocation.getZ() + radius * Math.sin(angle); double y = lobbyLocation.getY(); players.get(i).teleport(new Location(lobbyLocation.getWorld(), x, y, z)); } plugin.getLogger().info("Teleported " + players.size() + " players to lobby."); } private void teleportToMapCircle(List<Player> players) { if (mapSpawn == null) return; int radius = 3; for (int i = 0; i < players.size(); i++) { double angle = 2 * Math.PI * i / players.size(); double x = mapSpawn.getX() + radius * Math.cos(angle); double z = mapSpawn.getZ() + radius * Math.sin(angle); double y = mapSpawn.getY(); players.get(i).teleport(new Location(mapSpawn.getWorld(), x, y, z)); } } // ======================= GAME START ======================= public void startGame() { if (state != GameState.WAITING && state != GameState.GAME_END) return; int playerCount = Bukkit.getOnlinePlayers().size(); if (!testingMode && playerCount < plugin.getConfig().getInt("game.min-players", 2)) { broadcast(ChatColor.RED + "Minimal " + plugin.getConfig().getInt("game.min-players") + " player untuk mulai!"); return; } state = GameState.STARTING; broadcast(ChatColor.YELLOW + "Game akan dimulai dalam " + plugin.getConfig().getInt("game.countdown", 10) + " detik!"); countdownTaskId = new BukkitRunnable() { int count = plugin.getConfig().getInt("game.countdown", 10); @Override public void run() { if (count <= 0) { cancel(); startGameNow(); } else { broadcast(ChatColor.GREEN + "Memulai dalam " + count + "..."); count--; } } }.runTaskTimer(plugin, 0L, 20L).getTaskId(); } private void startGameNow() { if (countdownTaskId != -1) Bukkit.getScheduler().cancelTask(countdownTaskId); state = GameState.GAME_RUNNING; livingPlayers.clear(); deadPlayers.clear(); playerRoles.clear(); emergencyCooldown.clear(); killCooldown.clear(); plugin.getBridgeManager().loadConfig(); plugin.getSabotageManager().loadConfig(); loadLocationsFromConfig(); plugin.getTaskManager().reloadAllNPCs(); List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers()); for (Player p : players) livingPlayers.add(p.getUniqueId()); for (Player p : players) { plugin.getSkinWalker().saveOriginalSkin(p); } assignRoles(players); int crewmateCount = (int) playerRoles.values().stream().filter(r -> r == RoleRegistry.Role.CREWMATE).count(); plugin.getBridgeManager().setTargetPlanks(crewmateCount); teleportToMapCircle(players); plugin.getBridgeManager().resetBridge(); plugin.getTaskManager().initializeTasksForGame(); for (Player p : players) { p.getInventory().clear(); p.setHealth(20); p.setFoodLevel(20); for (PotionEffect effect : p.getActivePotionEffects()) p.removePotionEffect(effect.getType()); applySaturation(p); for (Player other : players) p.showPlayer(plugin, other); p.setGameMode(GameMode.SURVIVAL); p.setWalkSpeed(0.2f); p.sendMessage(ChatColor.GREEN + "Game dimulai! Role kamu: " + playerRoles.get(p.getUniqueId()).getDisplay()); if (playerRoles.get(p.getUniqueId()) == RoleRegistry.Role.CREWMATE) { p.sendMessage(ChatColor.AQUA + "Selesaikan task untuk mendapatkan kayu oak, lalu setorkan ke NPC jembatan!"); plugin.getTaskManager().assignInitialTask(p); } else if (playerRoles.get(p.getUniqueId()) == RoleRegistry.Role.IMPOSTOR) { p.sendMessage(ChatColor.RED + "Bunuh semua crewmate! Gunakan pedang untuk membunuh."); p.getInventory().addItem(new ItemStack(Material.IRON_SWORD)); p.getInventory().addItem(new ItemStack(Material.NETHER_STAR)); plugin.getFakeTaskBook().giveFakeTaskBook(p); } else if (playerRoles.get(p.getUniqueId()) == RoleRegistry.Role.JOKER) { p.sendMessage(ChatColor.LIGHT_PURPLE + "Kamu adalah Joker! Tujuanmu: di-vote keluar! Gunakan kompas untuk deteksi mayat."); p.getInventory().addItem(new ItemStack(Material.COMPASS)); } } broadcast(ChatColor.GOLD + "Game dimulai! Selamat bermain!"); plugin.getScoreboardManager().updateAll(); } private void assignRoles(List<Player> players) { int impostorCount = plugin.getConfig().getInt("roles.impostor-count", 1); boolean jokerEnabled = plugin.getConfig().getBoolean("roles.joker-enabled", true); List<UUID> ids = new ArrayList<>(); players.forEach(p -> ids.add(p.getUniqueId())); Collections.shuffle(ids); for (int i = 0; i < impostorCount && i < ids.size(); i++) { playerRoles.put(ids.get(i), RoleRegistry.Role.IMPOSTOR); } if (jokerEnabled && ids.size() > impostorCount + 1) { playerRoles.put(ids.get(impostorCount), RoleRegistry.Role.JOKER); } for (UUID id : ids) { if (!playerRoles.containsKey(id)) playerRoles.put(id, RoleRegistry.Role.CREWMATE); } } // ======================= CELEBRATE VICTORY (FIREWORK 3 DETIK) ======================= public void celebrateVictory(String winnerMessage) { if (state == GameState.GAME_END) return; state = GameState.GAME_END; // Hentikan semua aksi langsung plugin.getTaskManager().cancelAllActiveTasks(); plugin.getFakeTaskBook().cancelAllFakeTasks(); plugin.getSabotageManager().resetSkinWalkerIfActive(); plugin.getSabotageManager().resetCooldowns(); if (plugin.getSabotageManager().isSabotaging()) { plugin.getSabotageManager().fixSabotage(null); } broadcast(ChatColor.DARK_RED + winnerMessage); // Firework untuk semua player for (Player p : Bukkit.getOnlinePlayers()) { spawnFirework(p.getLocation()); } // Setelah 3 detik (60 tick), lakukan cleanup Bukkit.getScheduler().runTaskLater(plugin, this::cleanupAfterGame, 60L); } private void spawnFirework(Location loc) { try { Firework fw = loc.getWorld().spawn(loc, Firework.class); FireworkMeta meta = fw.getFireworkMeta(); meta.addEffect(FireworkEffect.builder() .withColor(Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE) .with(FireworkEffect.Type.BALL_LARGE) .build()); meta.setPower(0); fw.setFireworkMeta(meta); Bukkit.getScheduler().runTaskLater(plugin, fw::remove, 40L); } catch (Exception e) { loc.getWorld().playEffect(loc, Effect.EXPLOSION_HUGE, 0); } } private void cleanupAfterGame() { removeAllCorpses(); plugin.getBridgeManager().resetBridge(); for (Player p : Bukkit.getOnlinePlayers()) { p.getInventory().clear(); p.setGameMode(GameMode.SURVIVAL); p.setHealth(20); p.setFoodLevel(20); p.setWalkSpeed(0.2f); for (PotionEffect effect : p.getActivePotionEffects()) { p.removePotionEffect(effect.getType()); } applySaturation(p); for (Player other : Bukkit.getOnlinePlayers()) { p.showPlayer(plugin, other); } plugin.getSkinWalker().restoreOriginalSkin(p); } teleportToLobbyCircle(); plugin.getScoreboardManager().updateAll(); state = GameState.WAITING; livingPlayers.clear(); deadPlayers.clear(); playerRoles.clear(); emergencyCooldown.clear(); killCooldown.clear(); plugin.getTaskManager().resetTasks(); } // ======================= KILL PLAYER ======================= public void killPlayer(Player victim, Player killer, boolean createCorpse) { if (state != GameState.GAME_RUNNING && state != GameState.MEETING) return; if (!livingPlayers.contains(victim.getUniqueId())) return; RoleRegistry.Role role = getRole(victim); livingPlayers.remove(victim.getUniqueId()); deadPlayers.add(victim.getUniqueId()); if (role == RoleRegistry.Role.CREWMATE) { victim.setGameMode(GameMode.SPECTATOR); distributePlanksToCrewmates(victim, 8); } else { victim.setGameMode(GameMode.SPECTATOR); } victim.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false)); if (createCorpse) { Location loc = victim.getLocation(); ArmorStand corpse = loc.getWorld().spawn(loc, ArmorStand.class); corpse.setHelmet(victim.getInventory().getHelmet()); corpse.setCustomName(ChatColor.RED + victim.getName() + "'s Corpse"); corpse.setCustomNameVisible(true); corpse.setMarker(false); corpse.setGravity(false); corpse.setInvulnerable(true); Bukkit.getScheduler().runTaskLater(plugin, () -> corpse.remove(), 600L); } broadcast(ChatColor.RED + victim.getName() + " telah mati!"); plugin.getScoreboardManager().updateAll(); checkGameOver(); } public void killPlayer(Player victim, Player killer) { killPlayer(victim, killer, true); } public void handleDisconnect(Player player) { if (state != GameState.GAME_RUNNING) return; if (!livingPlayers.contains(player.getUniqueId())) return; killPlayer(player, null, false); } private void checkGameOver() { long aliveCrew = livingPlayers.stream().filter(uid -> playerRoles.get(uid) == RoleRegistry.Role.CREWMATE).count(); long aliveImpostor = livingPlayers.stream().filter(uid -> playerRoles.get(uid) == RoleRegistry.Role.IMPOSTOR).count(); if (aliveCrew == 0 && aliveImpostor > 0) { celebrateVictory("Impostor menang! Semua crewmate mati!"); } else if (aliveImpostor == 0 && aliveCrew > 0) { celebrateVictory("Crewmate menang! Semua impostor mati!"); } else if (plugin.getBridgeManager().isBridgeComplete()) { // celebrateVictory sudah dipanggil di BridgeManager saat jembatan selesai // biarkan tidak dobel } } public void removeAllCorpses() { for (World world : Bukkit.getWorlds()) { for (Entity e : world.getEntities()) { if (e instanceof ArmorStand as && as.getCustomName() != null && as.getCustomName().endsWith("Corpse")) { e.remove(); } } } } public void startMeeting(Player caller, String reason) { if (state != GameState.GAME_RUNNING) return; if (plugin.getSabotageManager().isSabotaging()) { caller.sendMessage(ChatColor.RED + "Tidak bisa meeting saat lampu mati! Perbaiki sabotase dulu."); return; } state = GameState.MEETING; removeAllCorpses(); broadcast(ChatColor.AQUA + "Meeting dimulai! " + reason); plugin.getVotingSystem().teleportToMeetingCircle(); plugin.getVotingSystem().startVotingSession(); plugin.getScoreboardManager().updateAll(); } public RoleRegistry.Role getRole(Player p) { return playerRoles.get(p.getUniqueId()); } public boolean isPlayerAlive(Player p) { return livingPlayers.contains(p.getUniqueId()); } public Set<UUID> getLivingPlayers() { return Collections.unmodifiableSet(livingPlayers); } public void setLobbyLocation(Location loc) { this.lobbyLocation = loc; saveLocation("lobby.location", loc); plugin.getScoreboardManager().updateAll(); } public void setMapSpawn(Location loc) { this.mapSpawn = loc; saveLocation("map-spawn.location", loc); plugin.getScoreboardManager().updateAll(); } public void setMeetingCenter(Location loc) { this.meetingCenter = loc; saveLocation("meeting-center.location", loc); plugin.getScoreboardManager().updateAll(); } public Location getMeetingCenter() { return meetingCenter; } public Location getMapSpawn() { return mapSpawn; } private void broadcast(String msg) { Bukkit.broadcastMessage(msg); } }"
Logika Animasi Jembatan Berbasis Deposit
Sistem Jembatan dan Deposit Minecraft
Memperbaiki Sabotase Nametag Minecraft
Mengubah Skin Player Minecraft
Panduan Instal Ulang Pterodactyl
Plugin Vote Kepala Player Minecraft
Link Download Server Minecraft Otomatis
Menjalankan AI Coding Lokal di Windows
Jadwal dan Materi ASAT Kelas 7
Komponen Wajib Rakit iPhone Sepaket
Zip File Terlalu Banyak File
Plugin Jembatan Otomatis dengan NPC
Plugin Jembatan Otomatis Minecraft
Plugin Sistem Task Minecraft Kompleks
Penjelasan Perintah Simple Voice Chat
Plugin Voice Chat Mode Sabotase
AI Pembuat Soal Latihan Interaktif
Latihan Soal Ujian ASAT Kelas VII
Latihan Soal Ujian ASAT Kelas VII
Latihan Soal SAT Digital Terbaru
Membuat Kuis Pengetahuan Umum Interaktif
Sistem Pendidikan Luar Negeri: Spesialisasi, Bukan Hafalan
Simulasi Trading Tanpa KTP
Mengubah Link TikTok ke Download Langsung
AI Filter Removal and Transformation
Rekomendasi JJ Kekinian & Quotes
Analisis Kesalahpahaman Video TikTok
Tebakan Rupiah Capai 18 Ribu
Pilihan Kalimat Penutup Surat Pasrah
Drama Satu Karakter dan Flashback
Permintaan Informasi Kode Java Ditolak
Perbaikan Tipe Data Java di Among Us Plugin
Ubah Teks Menjadi PDF 9:16
Merapikan Surat Pribadi Menjadi Rapi
Contoh Surat Pribadi untuk Tugas
Mengubah Kalimat Biasa Jadi Tajam
Minecraft Server World Data Corruption
Paksa Mute Voice Chat Tanpa API
Mengontrol Mute/Unmute Voice Chat
Cara Paksa Mute Unmute Voice Chat
Menggabungkan Voice Chat dan Skin Anonim
Fixing "Cannot Find Symbol" Error
Pembaruan Prompt Plugin Minecraft Among Us
Plugin Minecraft IslandBridgeAmongUs
Perbaikan Pom.xml untuk Build Plugin
Membuat Struktur Folder dan File CMD
Cara ubah http jadi https dari ssh vps(btw portnya 19100 di pterodactly jadi ini web via pterodactly Pengan di jadiin https)
Cloudflare HTTPS untuk Port Kustom
Plugin Minecraft Among Us Detail
Sistem Permainan Gartic Phone dan Scoring
Plugin Build Battle Ala Gartic Phone
Plugin Minecraft Among Us MrBeast
cara install dan setup ventoy agar bisa all device mbr,gpt, x64 dan x86 (32 bit dan 64 bit) kpmputer baru dan alama
Tugas Praktik Bahasa Indonesia Kelas 7
Analisis Spek PC & Bottleneck Utama
Ubah Konfigurasi TAB Secret Life
Percakapan dengan Gemini
ubah iuni "# 🎙️ Plugin Wajib & Rekomendasi - Voice Chat



---



## 🔴 WAJIB: Simple Voice Chat



**Simple Voice Chat** adalah satu-satunya plugin yang **WAJIB** diinstall agar IslandBridgeAmongUs dapat berfungsi dengan baik. Plugin ini menyediakan sistem *proximity voice chat* yang memungkinkan pemain mendengar satu sama lain berdasarkan jarak di dalam game .



### Alasan Wajib

- IslandBridgeAmongUs menggunakan **VoiceChatUtil** dan **SabotageManager** yang terintegrasi langsung dengan Simple Voice Chat

- Sistem **mute/unmute** saat meeting, sabotase, dan game berakhir membutuhkan API dari plugin ini

- Tanpa plugin ini, fitur voice chat tidak akan berfungsi sama sekali



### Cara Install di Server (Paper/Spigot)



1. Download **Simple Voice Chat** dari [CurseForge](https://www.curseforge.com/minecraft/bukkit-plugins/simple-voice-chat) 

2. Letakkan file `voicechat.jar` di folder `plugins/` server 

3. **Buka port UDP 24454** di firewall dan router 

   - Voice data menggunakan protokol UDP, pastikan port diforward sebagai UDP

4. Restart server



### Cara Install di Client (Player)



- **Java Edition**: Install mod Simple Voice Chat sesuai loader (Fabric/Forge/NeoForge) 

- **Bedrock Edition**: Gunakan [SimpleVoice-Geyser](https://modrinth.com/plugin/simplevoice-geyser) untuk mendukung player Bedrock 



### Konfigurasi Penting



File `plugins/voicechat/voicechat-server.properties`:

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

|--------|--------|------------|

| **PlaceholderAPI** | Placeholder untuk scoreboard & pesan | Opsional, tapi sangat direkomendasikan untuk fitur statistik |

| **LuckPerms** | Manajemen permission yang lebih baik | Memudahkan pengaturan akses player |

| **ProximityVoice** | Alternatif ringan Simple Voice Chat | Jika ingin plugin yang lebih ringan  |



---



## ⚠️ Catatan Penting



- Player **harus** menginstall mod Simple Voice Chat di client mereka untuk dapat menggunakan voice chat 

- Tanpa mod client, suara tidak akan terdengar meskipun server sudah terinstall plugin

- Port `24454` UDP **harus terbuka** di server 

- IslandBridgeAmongUs menggunakan **softdepend: [voicechat]** di `plugin.yml`, sehingga Simple Voice Chat akan dimuat terlebih dahulu 



---



## 🔧 Konfigurasi di IslandBridgeAmongUs



Plugin ini otomatis mendeteksi keberadaan Simple Voice Chat dan akan:

- **Unmute** semua pemain saat game dimulai [citation:GameManager.java]

- **Mute** semua pemain saat sabotase terjadi

- **Unmute** kembali saat sabotase diperbaiki atau game berakhir" jadi file readme beserja link tombolnya

Berikut adalah versi README yang telah dirapikan agar lebih profesional dan dilengkapi dengan link button bergaya Markdown agar terlihat lebih menarik saat ditampilkan di GitHub atau platform serupa.

🎙️ Plugin Wajib & Rekomendasi - Voice Chat
🔴 WAJIB: Simple Voice Chat
Simple Voice Chat adalah satu-satunya plugin yang WAJIB diinstall agar IslandBridgeAmongUs dapat berfungsi dengan baik. Plugin ini menyediakan sistem proximity voice chat yang memungkinkan pemain mendengar satu sama lain berdasarkan jarak di dalam game.

Alasan Wajib
Integrasi: IslandBridgeAmongUs menggunakan VoiceChatUtil dan SabotageManager yang terintegrasi langsung dengan Simple Voice Chat.

Automasi: Sistem mute/unmute otomatis saat meeting, sabotase, dan game berakhir bergantung sepenuhnya pada API plugin ini.

Fungsionalitas: Tanpa plugin ini, fitur komunikasi inti dalam game tidak akan aktif.

📥 Download & Instalasi
Server (Paper/Spigot)
Klik tombol di bawah untuk mengunduh plugin:
DOWNLOAD SIMPLE VOICE CHAT

Letakkan file voicechat.jar ke dalam folder plugins/ server Anda.

Buka port UDP 24454 di firewall/router Anda (Voice data menggunakan protokol UDP).

Restart server Anda.

Client (Player)
Java Edition: Install mod Simple Voice Chat sesuai dengan mod loader yang digunakan (Fabric/Forge/NeoForge).

Bedrock Edition: Wajib menggunakan plugin pendukung untuk Geyser:
DOWNLOAD SIMPLEVOICE-GEYSER

⚙️ Konfigurasi Server
Sesuaikan file plugins/voicechat/voicechat-server.properties Anda:

Properties
# Port UDP untuk voice chat
port=24454

# Jarak maksimum suara terdengar (block)
voice_distance=48.0

# Jarak berbisik
whisper_distance=6.0
🟡 REKOMENDASI TAMBAHAN
Plugin	Fungsi	Keterangan
PlaceholderAPI	Integrasi Scoreboard	Sangat direkomendasikan untuk statistik game.
LuckPerms	Manajemen Permission	Memudahkan kontrol akses pemain.
ProximityVoice	Alternatif	Opsi lebih ringan jika diperlukan.
⚠️ Catatan Penting
Client-Side Mod: Player WAJIB menginstall mod Simple Voice Chat di client mereka. Tanpa mod ini, suara tidak akan terdengar.

Firewall: Pastikan port 24454 (UDP) terbuka.

Dependencies: IslandBridgeAmongUs menggunakan softdepend: [voicechat] di plugin.yml, memastikan plugin ini dimuat lebih dahulu oleh sistem.

🔧 Integrasi IslandBridgeAmongUs
Plugin IslandBridgeAmongUs secara otomatis mendeteksi keberadaan Simple Voice Chat untuk melakukan:

Unmute semua pemain saat game dimulai.

Mute semua pemain saat sabotase terjadi.

Unmute kembali saat sabotase diperbaiki atau game berakhir.

kasidh cmd windwos untuk buat reame.md nya

Python
import os

# Define the content for the README.md file
readme_content = """# 🎙️ Plugin Wajib & Rekomendasi - Voice Chat

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
🟡 REKOMENDASI TAMBAHAN
Plugin	Fungsi	Keterangan
PlaceholderAPI	Integrasi Scoreboard	Sangat direkomendasikan untuk statistik game.
LuckPerms	Manajemen Permission	Memudahkan kontrol akses pemain.
ProximityVoice	Alternatif	Opsi lebih ringan jika diperlukan.
⚠️ Catatan Penting
Client-Side Mod: Player WAJIB menginstall mod Simple Voice Chat di client mereka. Tanpa mod ini, suara tidak akan terdengar.

Firewall: Pastikan port 24454 (UDP) terbuka.

Dependencies: IslandBridgeAmongUs menggunakan softdepend: [voicechat] di plugin.yml, memastikan plugin ini dimuat lebih dahulu oleh sistem.

🔧 Integrasi IslandBridgeAmongUs
Plugin IslandBridgeAmongUs secara otomatis mendeteksi keberadaan Simple Voice Chat untuk melakukan:

Unmute semua pemain saat game dimulai.

Mute semua pemain saat sabotase terjadi.

Unmute kembali saat sabotase diperbaiki atau game berakhir.
"""

Path to the file
file_path = "README.md"

Write the content to the file
with open(file_path, "w", encoding="utf-8") as f:
f.write(readme_content)

print(f"File {file_path} berhasil dibuat.")


```text?code_stdout&code_event_index=1
File README.md berhasil dibuat.

File README.md Anda sudah berhasil dibuat.
Ikon MD
README
 MD 
Buka

Untuk membuat file tersebut melalui Command Prompt (CMD) di Windows, Anda bisa menggunakan perintah echo atau type, namun karena kontennya cukup panjang dan memiliki struktur baris baru, cara termudah adalah dengan menggunakan perintah powershell atau copy con di CMD.

Berikut adalah perintah CMD yang bisa Anda copy-paste untuk membuat file tersebut dengan cepat:

DOS
(
echo # 🎙️ Plugin Wajib ^& Rekomendasi - Voice Chat
echo.
echo ---
echo.
echo ## 🔴 WAJIB: Simple Voice Chat
echo.
echo **Simple Voice Chat** adalah satu-satunya plugin yang **WAJIB** diinstall agar `IslandBridgeAmongUs` dapat berfungsi dengan baik. Plugin ini menyediakan sistem *proximity voice chat* yang memungkinkan pemain mendengar satu sama lain berdasarkan jarak di dalam game.
echo.
echo ### Alasan Wajib
echo *   **Integrasi:** `IslandBridgeAmongUs` menggunakan `VoiceChatUtil` dan `SabotageManager` yang terintegrasi langsung dengan Simple Voice Chat.
echo *   **Automasi:** Sistem **mute/unmute** otomatis saat meeting, sabotase, dan game berakhir bergantung sepenuhnya pada API plugin ini.
echo *   **Fungsionalitas:** Tanpa plugin ini, fitur komunikasi inti dalam game tidak akan aktif.
echo.
echo ---
echo.
echo ### 📥 Download ^& Instalasi
echo.
echo #### Server (Paper/Spigot)
echo 1.  Klik tombol di bawah untuk mengunduh plugin:
echo     [**DOWNLOAD SIMPLE VOICE CHAT**](https://www.curseforge.com/minecraft/bukkit-plugins/simple-voice-chat)
echo 2.  Letakkan file `voicechat.jar` ke dalam folder `plugins/` server Anda.
echo 3.  **Buka port UDP 24454** di firewall/router Anda (Voice data menggunakan protokol UDP).
echo 4.  Restart server Anda.
echo.
echo #### Client (Player)
echo *   **Java Edition:** Install mod Simple Voice Chat sesuai dengan mod loader yang digunakan (Fabric/Forge/NeoForge).
echo *   **Bedrock Edition:** Wajib menggunakan plugin pendukung untuk Geyser:
echo     [**DOWNLOAD SIMPLEVOICE-GEYSER**](https://modrinth.com/plugin/simplevoice-geyser)
echo.
echo ---
echo.
echo ### ⚙️ Konfigurasi Server
echo.
echo Sesuaikan file `plugins/voicechat/voicechat-server.properties` Anda:
echo.
echo ```properties
echo # Port UDP untuk voice chat
echo port=24454
echo.
echo # Jarak maksimum suara terdengar (block)
echo voice_distance=48.0
echo.
echo # Jarak berbisik
echo whisper_distance=6.0
echo ```
echo.
echo ---
echo.
echo ## 🟡 REKOMENDASI TAMBAHAN
echo.
echo ^| Plugin ^| Fungsi ^| Keterangan ^|
echo ^| :--- ^| :--- ^| :--- ^|
echo ^| **PlaceholderAPI** ^| Integrasi Scoreboard ^| Sangat direkomendasikan untuk statistik game. ^|
echo ^| **LuckPerms** ^| Manajemen Permission ^| Memudahkan kontrol akses pemain. ^|
echo ^| **ProximityVoice** ^| Alternatif ^| Opsi lebih ringan jika diperlukan. ^|
echo.
echo ---
echo.
echo ## ⚠️ Catatan Penting
echo.
echo *   **Client-Side Mod:** Player **WAJIB** menginstall mod Simple Voice Chat di client mereka. Tanpa mod ini, suara tidak akan terdengar.
echo *   **Firewall:** Pastikan port `24454` (UDP) terbuka.
echo *   **Dependencies:** `IslandBridgeAmongUs` menggunakan `softdepend: [voicechat]` di `plugin.yml`, memastikan plugin ini dimuat lebih dahulu oleh sistem.
echo.
echo ---
echo.
echo ## 🔧 Integrasi IslandBridgeAmongUs
echo.
echo Plugin `IslandBridgeAmongUs` secara otomatis mendeteksi keberadaan Simple Voice Chat untuk melakukan:
echo 1.  **Unmute** semua pemain saat game dimulai.
echo 2.  **Mute** semua pemain saat sabotase terjadi.
echo 3.  **Unmute** kembali saat sabotase diperbaiki atau game berakhir.
) > README.md
Tips: Simpan kode di atas ke dalam file .bat (misal: buat_readme.bat) lalu jalankan file tersebut di dalam folder proyek Anda, maka README.md akan otomatis terbuat.




Gemini adalah AI dan dapat melakukan kesalahan.

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
README.md
Menampilkan README.md.

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
