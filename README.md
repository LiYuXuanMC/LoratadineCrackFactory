# Loratadine Crack Factory

A tools for crack Loratadine dll automatic

This tool is based on the [DeobfuscatorBase](https://github.com/EscapistKid/DeobfuscatorBase) project.

Required java17

## Usage

Download our [Pre Build Environment](https://github.com/LiYuXuanMC/LoratadineCrackFactory/releases/download/pre-build/LoratadineCrackFactory.zip)

You can delete `LoratadineCrackFactory.exe`, it is a pre build jar packed with exe4j

Place jar built by yourself here our just use `LoratadineCrackFactory.exe`

To run this tool, you need to have these requirements installed:

### Azul Java 17

Can be found in: https://www.azul.com/downloads/?version=java-17-lts&package=jdk#zulu

### Visual Studio 2022

Available at: https://visualstudio.microsoft.com/

### Forge 1.18.1 srg binary

- Install Minecraft 1.18.1 with Forge.
- Locate the `client-1.18.1-20211210.034407-srg` file in the `.minecraft/libraries` directory.

You can also download it in https://github.com/LiYuXuanMC/LoratadineCrackFactory/releases/download/pre-build/client-1.18.1-20211210.034407-srg.jar

### rt.jar

Obtain this from any Java 1.8 `lib` directory.

### Example Argument

```bash
-input Loratadine#2024825_1.2.0.dll -output cracked.dll -msBuildPath "C:\Program Files\Microsoft Visual Studio\2022\Community\MSBuild\Current\Bin\MSBuild.exe" -rtPath rt.jar -binPath client-1.18.1-20211210.034407-srg.jar
```