# Proteus

A shared UI toolkit for Olympus mods: animated widgets, themes and toasts.

Describe what the mod does here, what makes it worth installing and how to use it.

## Supported versions

| Minecraft | Branch | Fabric | Forge | NeoForge |
|-----------|--------|:------:|:-----:|:--------:|
| 1.20-1.20.2 | `1.20` | ✅ | ✅ | n/a |
| 1.20.3-1.20.4 | `1.20` | ✅ | ✅ | ✅ |
| 1.20.5-1.20.6 | `1.20` | ✅ | ✅ | ✅ |
| 1.21-1.21.1 | `1.21` | ✅ | ✅ | ✅ |
| 1.21.2-1.21.3 | `1.21` | ✅ | 1.21.3 | 1.21.3 |
| 1.21.4-1.21.5 | `1.21` | ✅ | ✅ | ✅ |
| 1.21.6-1.21.8 | `1.21` | ✅ | ✅ | ✅ |
| 1.21.9-1.21.10 | `1.21` | ✅ | ✅ | ✅ |
| 1.21.11 | `1.21` | ✅ | ✅ | ✅ |
| 26.1-26.1.2 | `26.1` | ✅ | ✅ | ✅ |
| 26.2-26.3 | `main` | ✅ | ✅ | ✅ |

Each Minecraft line lives on its own branch and `main` is always the newest one.

Fabric needs Fabric API. NeoForge has no stable release for 1.20.2 or 1.21.2 and Forge has none for 1.21.2.


## Installing

1. Download the jar for your loader and Minecraft version from [Modrinth](https://modrinth.com/mod/proteus) or [Releases](https://github.com/OlympusMods/Proteus/releases).
2. Put it in your `mods` folder. Fabric also needs Fabric API.

## Building

Every loader and Minecraft version is its own Gradle project, driven from one root build. Use the JDK each Minecraft version needs (the root `build.gradle` shows which; Fabric projects need Java 25 to run Gradle itself).

```sh
# one target
./gradlew :Fabric-1.21.1:build

# one loader, or everything
./gradlew buildFabric
./gradlew build
```

Finished jars are collected into `build/dist` and named `proteus-<loader>-<minecraft versions>-<version>.jar`. The mod version is set in the root `gradle.properties`.

## Publishing

Releases go to Modrinth and CurseForge from the same build. Put `modrinthToken` and `curseforgeToken` in `~/.gradle/gradle.properties` (or set `MODRINTH_TOKEN` and `CURSEFORGE_TOKEN`), set the two project ids at the top of `publish.gradle`, and add a `## <version>` entry to `CHANGELOG.md`, which becomes the changelog of every upload.

```sh
# print what would be uploaded, send nothing
./gradlew publishMods -PdryRun=true

# one site, or both
./gradlew publishModrinth
./gradlew publishCurseforge
./gradlew publishMods

# one loader, or one jar
./gradlew publishFabric
./gradlew :NeoForge-1.21.4:publishModrinth
```

Without any token the tasks only do a dry run. The Minecraft versions each jar is uploaded for come from `publish_minecraft_versions` in that project's `gradle.properties`. Modrinth keeps the environment on each version, so `publishModrinth` finishes by setting the new version to client only; change that in `publish.gradle` if the mod also runs on servers.

## Project layout

This branch holds Minecraft 26.1 to 26.1.2. The other lines are on the branches listed above.

```
common/
  core/        Loader- and version-independent logic
  client/      Code shared by every build
  mc26/       Minecraft-specific glue and mixins for 26.1-26.1.2
  resources/   Language file and icon
Fabric-*/  Forge-*/  NeoForge-*/    Thin per-loader projects
publish.gradle   Modrinth and CurseForge setup shared by every project
```


Most behavior should live in `common/core`, which has no Minecraft dependency. The version folders only adapt it to each game version, so a change to the logic usually only touches `core`.

When a Minecraft update breaks the shared code, copy the newest `common/mc*` folder, fix what the compiler reports, and add a project for each loader. Each folder covers the range of versions where its code compiles and its mixin targets still exist.

## Contributing

Bug reports and pull requests are welcome. For a bug, include your Minecraft version, loader and `latest.log`. Commit messages follow `type: short description` (`feat`, `fix`, `chore`, `refactor`).

## License

[GPL-3.0](LICENSE)
