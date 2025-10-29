# ExtraPlaceholders

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-21-orange.svg)](https://www.oracle.com/java/)
[![Minecraft](https://img.shields.io/badge/minecraft-1.13+-green.svg)](https://www.minecraft.net/)

A powerful PlaceholderAPI expansion that provides custom placeholders for [Bolt](https://refinedev.xyz/resources/bolt.8/) and [Phoenix](https://refinedev.xyz/resources/phoenix.3/) plugins.

## 📋 Requirements

- **Minecraft**: 1.13 or higher (Paper/Spigot)
- **Java**: 21+
- **[PlaceholderAPI](https://modrinth.com/plugin/placeholderapi)**: Required (core dependency)
- **[Bolt](https://refinedev.xyz/resources/bolt.8/)**: Optional (for match & kit placeholders)
- **[Phoenix](https://refinedev.xyz/resources/phoenix.3/)**: Optional (for staff mode placeholders)

## 🚀 Features

- **Server Information**: Customizable date formats with multi-locale support
- **Match Management**: Real-time match winner/loser tracking for solo, team, and FFA matches
- **Kit Information**: Dynamic kit rule checking for any Bolt kit
- **Staff Tools**: Vanish and mod-mode status indicators
- **Performance**: Optimized code with minimal overhead
- **Flexible**: Works with or without optional dependencies

## 📦 Installation

1. Download the latest release from [Releases](http://github.com/sheduxdev/ExtraPlaceholders/releases)
2. Place the `.jar` file in your `plugins/` folder
3. Ensure [PlaceholderAPI](https://modrinth.com/plugin/placeholderapi) is installed
4. (Optional) Install [Bolt](https://refinedev.xyz/resources/bolt.8/) and/or [Phoenix](https://refinedev.xyz/resources/phoenix.3/)
5. Restart your server
6. Configure the plugin in `plugins/ExtraPlaceholders/config.yml`

## 📖 Available Placeholders

### Server Placeholders

| Placeholder | Example Result | Description |
|------------|----------------|-------------|
| `%extraplaceholders_server_date%` | `15 January 2024, Sun` | Current date with default locale |
| `%extraplaceholders_server_date_<locale>%` | `15 Ocak 2024, Paz` | Current date with specific locale |

**Supported Locales**: `tr`, `en`, `en-us`, `en-gb`, `de`, `fr`, `es`, `it`, `pt`, `pt-br`, `ru`, `ja`, `zh`, `zh-cn`, `zh-tw`, `ar`, `ko`, `nl`, `pl`, `sv`, `no`, `da`, `fi`

### Bolt Placeholders (Requires [Bolt](https://refinedev.xyz/resources/bolt.8/))

#### Match Information

| Placeholder | Example Result | Description |
|------------|----------------|-------------|
| `%extraplaceholders_bolt_match_winner%` | `PlayerName` | Winner(s) of the current match |
| `%extraplaceholders_bolt_match_loser%` | `PlayerName` | Loser(s) of the current match |

#### Current Kit Rules

| Placeholder | Example Result | Description |
|------------|----------------|-------------|
| `%extraplaceholders_bolt_kit_rule_<rule>%` | `true` / `false` | Check if current kit has a specific rule enabled |

#### Specific Kit Rules

| Placeholder | Example Result | Description |
|------------|----------------|-------------|
| `%extraplaceholders_bolt_kit_<kitname>_rule_<rule>%` | `true` / `false` | Check if a specific kit has a rule enabled |

**Available Rules**:
- **Gameplay**: `enabled`, `ranked`, `build`, `showhp`, `editable`, `ffa`
- **Game Modes**: `spleef`, `sumo`, `combo`, `boxing`, `bridges`, `bedfight`, `mlgrush`, `battlerush`, `fireballfight`, `pearlfight`, `stickfight`, `crystalpvp`, `cartpvp`, `tntsumo`, `oitq`
- **Combat**: `legacycombat`, `nodrop`, `nofall`, `nohunger`, `noregen`, `pearldamage`, `pearlcooldown`
- **Mechanics**: `blockremoval`, `respawnmode`, `buildheightdamage`, `voidspawn`, `liquidkill`, `portal`, `windchargemode`, `presplash`, `breakmap`
- **Party**: `partyffa`, `partysplit`, `stickspawn`
- **Special**: `topfight`, `windchargemode`

### Phoenix Placeholders (Requires [Phoenix](https://refinedev.xyz/resources/phoenix.3/))

| Placeholder | Example Result | Description |
|------------|----------------|-------------|
| `%extraplaceholders_phoenix_status%` | `[⚗] [⚙]` | Shows staff status (vanish/mod mode) |

## 🎨 Configuration

### Default Configuration
```yaml
MESSAGES:
  # Reload Messages
  RELOAD_SUCCESS: '&aConfiguration successfully reloaded in &e<duration>ms&a!'
  RELOAD_ERROR: '&cAn error occurred while reloading the configuration!'
  
  # Plugin Status Messages
  PLUGIN_ENABLED: '&aPlugin successfully enabled! &7v<version>'
  PLUGIN_DISABLED: '&cPlugin disabled.'
  EXPANSION_REGISTERED: '&aPlaceholderAPI integration completed successfully.'
  EXPANSION_FAILED: '&cPlaceholderAPI registration failed!'
  
  # Dependency Messages
  PLACEHOLDER_API_NOT_FOUND: '&cPlaceholderAPI not found! Plugin will be disabled.'
  BOLT_NOT_AVAILABLE: '&cBolt API is not available'
  PHOENIX_NOT_AVAILABLE: '&cPhoenix API is not available'
  
  # Kit Placeholder Messages
  KIT_OUT_OF_MATCH: '&7Out of match'
  KIT_LOADING: '&eKit loading'
  KIT_INVALID: '&cInvalid kit'
  KIT_DEFAULT: '&7-'
  
  # Date Placeholder Messages
  INVALID_LOCALE: '&cInvalid locale format!'

DATE:
  # Available locales: tr, en, de, fr, es, it, pt, ru, ja, zh, ar, ko, nl, pl, sv, no, da, fi
  # Format pattern: d MMMM yyyy, EEE
  DEFAULT_LOCALE: 'tr-TR'
  DATE_PATTERN: 'd MMMM yyyy, EEE'

PHOENIX:
  # Phoenix Status Prefixes
  DEFAULT_STATUS: '&f'
  VANISHED_PREFIX: '<#9e9e9e>[⚗] '
  MOD_MODE_PREFIX: '<#ffc430>[⚙] '

LOGGER:
  # Logger Format Settings
  PREFIX: '&8[&6<plugin>&8]'
  INFO_COLOR: '&b'
  SUCCESS_COLOR: '&a'
  ERROR_COLOR: '&c'
```

### Color Support

The plugin supports both legacy color codes (`&a`, `&c`) and modern hex colors:
- **Hex Colors**: `<#RRGGBB>` (e.g., `<#FF5733>`)
- **Gradients**: `<gradient:#START:#END>text</gradient>`

## 🎯 Usage Examples

### Chat Format
```yaml
# Using Phoenix status in chat format
prefix: "%extraplaceholders_phoenix_status% &ePlayerName"
# Result: [⚗] [⚙] PlayerName (if vanished and in mod mode)
```

### Scoreboard
```yaml
scoreboard:
  lines:
    - "&8&m--------------------"
    - "&eDate: &f%extraplaceholders_server_date%"
    - "&eRanked: &f%extraplaceholders_bolt_kit_rule_ranked%"
    - "&eMatch Winner: &f%extraplaceholders_bolt_match_winner%"
    - "&8&m--------------------"
```

### TAB Plugin
```yaml
header:
  - "&6&lMY SERVER"
  - "&7%extraplaceholders_server_date%"
  - ""
  - "&eStatus: %extraplaceholders_phoenix_status%"
```

### Chat Reaction Plugin
```yaml
placeholders:
  - placeholder: "%extraplaceholders_bolt_kit_rule_sumo%"
    value: "true"
    reaction: "This is a sumo kit!"
```

## 🔧 Commands

| Command | Permission | Description |
|---------|-----------|-------------|
| `/extraplaceholders` | `extraplaceholders.admin` | Main command |
| `/extraplaceholders reload` | `extraplaceholders.admin` | Reload configuration |
| `/extraplaceholders info` | `extraplaceholders.admin` | Show plugin information |

**Aliases**: `/ep`

## 🏗️ Building from Source

### Prerequisites
- Java Development Kit (JDK) 21+
- Git

### Build Steps
```bash
# Clone the repository
git clone https://github.com/sheduxdev/ExtraPlaceholders.git
cd ExtraPlaceholders

# Build with Gradle (Windows)
gradlew.bat shadowJar

# Build with Gradle (Linux/Mac)
./gradlew shadowJar

# Output: build/libs/ExtraPlaceholders-v0.1.jar
```

## 🔗 Dependencies

### Required
- **[PlaceholderAPI](https://modrinth.com/plugin/placeholderapi)** - Core placeholder system

### Optional
- **[Bolt](https://refinedev.xyz/resources/bolt.8/)** - Practice plugin ([API](https://github.com/RefineDevelopment/BoltAPI))
- **[Phoenix](https://refinedev.xyz/resources/phoenix.3/)** - Staff suite plugin ([API](https://github.com/RefineDevelopment/PhoenixAPI))

### Build Dependencies
- [Lombok](https://projectlombok.org/) - Code generation
- [CommandAPI by j4c0b3y](https://github.com/j4c0b3y/CommandAPI) - Command framework
- [ConfigAPI by j4c0b3y](https://github.com/j4c0b3y/ConfigAPI) - Configuration system

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👤 Author
**sheduxdev**

- GitHub: [@sheduxdev](https://github.com/sheduxdev)

## 🙏 Acknowledgments
- [RefineDevelopment](https://refinedev.xyz/) - For Bolt and Phoenix plugins
- [PlaceholderAPI Team](https://github.com/PlaceholderAPI) - For the excellent placeholder system
- [j4c0b3y](https://github.com/j4c0b3y) - For CommandAPI and ConfigAPI libraries
---