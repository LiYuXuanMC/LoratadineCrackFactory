# Loratadine Crack Factory

A tools for crack Loratadine dll automatic

This tool is based on the [DeobfuscatorBase](https://github.com/EscapistKid/DeobfuscatorBase) project.

Required java17

## Usage

Download our [Pre Build Environment](https://github.com/LiYuXuanMC/LoratadineCrackFactory/releases/download/pre-build/LoratadineCrackFactory.zip)

Delete `LoratadineCrackFactory.exe`, it is a pre build jar packed with exe4j

To run this tool, you need to have these requirements installed:

### Azul Java 17

Can be found in: https://www.azul.com/downloads/?version=java-17-lts&package=jdk#zulu

### Visual Studio 2022

Available at: https://visualstudio.microsoft.com/

### Libraries

Get flow libraries from anywhere:

- client-1.18.1-20211210.034407-srg.jar
- fastutil-8.5.6.jar
- gson-2.8.8.jar
- netty-all-4.1.68.Final.jar
- rt.jar

put to `libs` folder

### Example Argument

```bash
-input Loratadine#2024825_1.2.0.dll -output cracked.dll -msBuildPath "C:\Program Files\Microsoft Visual Studio\2022\Community\MSBuild\Current\Bin\MSBuild.exe" -libPath libs -fakeThread
```