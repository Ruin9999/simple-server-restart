# Simple Server Restart
A lightweight server plugin to add scheduled restart functionality with enhanced security features. 
Defaults to relying on the host restarting the server when stopped.

[![](https://dcbadge.limes.pink/api/server/https://discord.gg/uyyxyzVq75)](https://discord.gg/https://discord.gg/uyyxyzVq75)
[![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)](https://github.com/Ruin9999/simple-server-restart)

## 🔒 Security Features
This mod includes comprehensive security protections:
- **Path Traversal Protection** - Restart scripts are validated to stay within the server directory
- **Input Validation** - All configuration values are thoroughly validated
- **Error Recovery** - Robust error handling prevents crashes from invalid configurations

## How to use
Scheduled server restarts can be configured in the config file.

<code>/restart</code> to instantly restart the server.   
<code>/restart delay [seconds]</code> to restart the server with a delay.
<code>/restart time [HH:MM]</code> to restart the server at a specific time.

## ⚙️ Configuration Security
- Restart script paths are validated to prevent directory traversal attacks
- Configuration validation prevents invalid time formats and intervals
- Comprehensive error logging helps troubleshoot issues safely

## Frequently Asked Questions

**Q: My server isn't restarting when It's supposed to.**   
A: Ensure that `runRestartScript = true` in the config and that your restart script has proper permissions.

**Q: Will you be adding support for different platforms?**   
A: Yes, I am planning to add support for Quilt and Forge

**Q: Is this mod secure for production servers?**   
A: Yes! The mod includes comprehensive security protections against common vulnerabilities like path traversal and command injection.

**Q: What if my restart script path is invalid?**   
A: The mod will log detailed error messages and prevent execution of invalid scripts to protect your server.

## 🛠️ Technical Requirements
- Minecraft 1.21.5
- Fabric Loader
- Java 21
- Fabric API

## 🔧 Development Status
This mod has undergone comprehensive security review and includes:
- ✅ Path traversal protection
- ✅ Input validation and sanitization  
- ✅ Comprehensive error handling
- ✅ Cross-platform compatibility
- ✅ Production-ready security features

I'm still very new to plugin development so if you have any feedback or additional problems,
please feel free to contact me via Discord or Github :D
