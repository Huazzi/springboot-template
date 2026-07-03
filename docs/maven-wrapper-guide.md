# Maven Wrapper 说明

`mvnw.cmd` 是 Maven Wrapper 在 Windows 下的启动脚本。它不是业务代码，日常开发不需要读懂每一行，也不建议手工修改脚本逻辑。

## 它解决什么问题

不同开发者本机安装的 Maven 版本可能不同。Maven Wrapper 会按照 `.mvn/wrapper/maven-wrapper.properties` 中的 `distributionUrl` 自动使用项目指定的 Maven 版本。

本模板项目固定使用 Maven 3.9.16。团队成员只要运行 `mvnw.cmd`，就可以尽量避免“我这里 Maven 版本不一样导致构建失败”的问题。

## 日常怎么用

使用**Maven Wrapper 固定 Maven 版本**：

- ```bash
  # 使用 Maven Wrapper 指定 Maven 版本
  mvn wrapper:wrapper "-Dmaven=3.9.16"
  ```

- 执行完后会在根目录下生成文件：mvnw、mvnw.cmd、.mvn/wrapper/maven-wrapper.properties

- `mvnw.cmd`为 Windows 用的启动脚本 ； 

- `mvnw`为 Linux/MacOS 用的启动脚本；

- `maven-wrapper.properties`：记录要下载和使用哪个 Maven 版本。

在项目根目录执行：

```powershell
.\mvnw.cmd clean verify
```

这条命令会执行完整质量门禁，包括格式检查、静态检查、编译、测试和打包。

启动项目时执行：

```powershell
.\mvnw.cmd spring-boot:run
```

如果只想自动格式化代码，可以执行：

```powershell
.\mvnw.cmd spotless:apply
```

## 它大致做了什么

1. 读取 `.mvn/wrapper/maven-wrapper.properties`。
2. 找到项目指定的 Maven 下载地址。
3. 检查本机 `~/.m2/wrapper/dists` 下是否已经缓存过这个 Maven。
4. 如果没有缓存，就自动下载并解压。
5. 用缓存中的 Maven 执行你传入的命令，例如 `clean verify`。

## 什么时候需要改 mvnw.cmd

一般不需要改。只有在升级 Maven Wrapper 或重新生成 Wrapper 时，才应该更新它。推荐使用 Maven Wrapper 官方命令重新生成，而不是手工编辑脚本内部逻辑。
