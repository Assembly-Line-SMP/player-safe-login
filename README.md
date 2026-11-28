> [!NOTE]
> **This is a fork of the original mod**
# 🛡️ Player Safe Login


**Player Safe Login** is a Minecraft mod designed to enhance security for **offline mode servers** by adding a client-server password authentication system.


## ✨ Features

- Secure password creation and storage on the client (SHA-256)
- Server-side validation with automatic kick on mismatch
- Admin command to reset player registration
- GUI with random password generator
- Brute-force protection and cooldown
- Logging of failed login attempts

## 🛠 Commands

- `/playersafelogin deleteaccount <player>` – Deletes a player's password record (admin only)
- `/playersafelogin resetpassword <new password>` – Changes local password and sync with server (must be connected to server)

## ⚙ Requirements

- Minecraft 1.21.1
- NeoForge 1.21.1+
- Installed on **both client and server**

---

## 🏗 Build Instructions

To build this mod from source:

```bash
git clone https://github.com/youruser/PlayerSafeLogin.git
cd PlayerSafeLogin
./gradlew build
