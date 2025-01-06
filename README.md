# Development Guide

## Build Commands

| Command                         | Description |
|---------------------------------|-------------|
| `./gradlew nativeCompile`       | Compiles the project using GraalVM to generate a native executable. |
| `./gradlew build`               | Builds the project normally (without native compilation). |

## Installation

| Command                                   | Description |
|-------------------------------------------|-------------|
| `mv app/build/native/nativeCompile/sqlift /usr/local/bin/` | Move the compiled executable to PATH for system-wide access. |
| `rm /usr/local/bin/sqlift`                | Remove the executable from PATH if you need to recompile or fix errors. |

## Environment Setup

| Command                               | Description |
|---------------------------------------|-------------|
| `nano ~/.zshrc`                       | Open Zsh configuration file to edit environment variables. |
| `export GRAALVM_HOME=/path/to/graalvm` | Set the GraalVM home directory in your environment variables. |
| `export PATH=$GRAALVM_HOME/bin:$PATH`  | Add GraalVM to the system PATH. |
| `source ~/.zshrc`                     | Apply changes made to `.zshrc` without restarting the terminal. |
| `Ctrl + O`                            | Save changes in Nano editor. |
| `Ctrl + X`                            | Exit Nano editor. |

## Utility Commands

| Command                         | Description |
|---------------------------------|-------------|
| `which sqlift`                  | Find the location of an executable in the system. |
| `sudo rm /usr/local/bin/sqlift` | Remove a specific executable from `/usr/local/bin/`. |

## Git Commands

1. Primero eliminar el tag tanto local como remoto:

```bash
# Eliminar tag local
git tag -d v1.0.0

# Eliminar tag remoto
git push origin :refs/tags/v1.0.0

# Agregar tag local
git tag -a v1.0.0 -m "First release with Gradle migration"

# Agregar tag remoto
git push origin v1.0.0
