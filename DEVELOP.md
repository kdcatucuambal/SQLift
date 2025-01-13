Aquí tienes las modificaciones actualizadas para los comandos, reflejando el cambio hacia el uso de un **JAR estándar**
en lugar del plugin de GraalVM en Gradle, y delegando la compilación nativa a `native-image` como una herramienta
externa.

---

# Development Guide (Actualizado)

## Build Commands

| Command                                                                 | Description |
|-------------------------------------------------------------------------|-------------|
| `./gradlew build`                                                       | Builds the project normally and generates a JAR file in `build/libs/`. |
| `native-image -jar app/build/libs/app.jar`                              | Compiles the generated JAR into a native executable using GraalVM's `native-image` tool. |
| `native-image -jar app/build/libs/app.jar -H:Name=sqlift --no-fallback` | Generates a native executable with additional configuration options (e.g., `--no-fallback`). |

---

## Installation

| Command                                   | Description |
|-------------------------------------------|-------------|
| `mv sqlift /usr/local/bin/`               | Move the compiled executable to the PATH for system-wide access. |
| `rm /usr/local/bin/sqlift`                | Remove the executable from the PATH if you need to recompile or fix errors. |

---

## Environment Setup

| Command                               | Description |
|---------------------------------------|-------------|
| `nano ~/.zshrc`                       | Open the Zsh configuration file to edit environment variables. |
| `export GRAALVM_HOME=/path/to/graalvm` | Set the GraalVM home directory in your environment variables. |
| `export PATH=$GRAALVM_HOME/bin:$PATH`  | Add GraalVM to the system PATH. |
| `source ~/.zshrc`                     | Apply changes made to `.zshrc` without restarting the terminal. |
| `Ctrl + O`                            | Save changes in the Nano editor. |
| `Ctrl + X`                            | Exit the Nano editor. |

---

## Utility Commands

| Command                                   | Description |
|-------------------------------------------|-------------|
| `which sqlift`                            | Find the location of the compiled executable in the system. |
| `sudo rm /usr/local/bin/sqlift`           | Remove a specific executable from `/usr/local/bin/`. |
| `native-image --version`                  | Verify that the `native-image` tool is installed and accessible. |

---

## Git Commands

### Eliminar un tag y volver a generarlo

```bash
# Eliminar tag local
git tag -d v1.0.0

# Eliminar tag remoto
git push origin :refs/tags/v1.0.0

# Agregar tag local
git tag -a v1.0.0 -m "First release with Gradle migration"

# Subir tag remoto
git push origin v1.0.0
```

---
