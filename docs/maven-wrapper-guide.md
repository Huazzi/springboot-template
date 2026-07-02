# Maven Wrapper 中文说明

`mvnw.cmd` 是 Maven Wrapper 在 Windows 下的启动脚本。它不是业务代码，日常开发不需要读懂每一行，也不建议手工修改脚本逻辑。

## 它解决什么问题

不同开发者本机安装的 Maven 版本可能不同。Maven Wrapper 会按照 `.mvn/wrapper/maven-wrapper.properties` 中的 `distributionUrl` 自动使用项目指定的 Maven 版本。

本项目固定使用 Maven 3.9.16。团队成员只要运行 `mvnw.cmd`，就可以尽量避免“我这里 Maven 版本不一样导致构建失败”的问题。

## 日常怎么用

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

## 为什么不直接在 mvnw.cmd 里写大量中文注释

`mvnw.cmd` 是一个特殊的混合脚本：前半部分给 Windows Batch 执行，后半部分给 PowerShell 执行。这个脚本还会读取自身内容并重新解析。

在这种文件里加入中文注释，可能因为编码、Batch 解析或 PowerShell 自解析导致脚本失效。因此本项目保持 `mvnw.cmd` 本体尽量接近 Maven Wrapper 原始脚本，只在脚本顶部放一行英文提示，并把中文解释放到当前文档中。

## 什么时候需要改 mvnw.cmd

一般不需要改。

只有在升级 Maven Wrapper 或重新生成 Wrapper 时，才应该更新它。推荐使用 Maven Wrapper 官方命令重新生成，而不是手工编辑脚本内部逻辑。
