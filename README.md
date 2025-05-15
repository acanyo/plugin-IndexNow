# IndexNow插件

IndexNow Halo 插件支持自动将 Halo 网站的 URL 提交到多个搜索引擎，无需注册和验证您的网站。安装后，该插件将自动生成 API 密钥并将其托管在您的网站上。它会检测 Halo 中的页面创建/更新/删除，并在后台自动提交 URL。这确保搜索引擎始终掌握您网站的最新动态。

该插件还包含一些其他方便的功能：
- 切换自动提交功能。
- 手动向 IndexNow 提交 URL。
- 查看插件最近提交的 URL 列表。
- 从最近提交列表中重试任何失败的提交。
- 下载最近的 URL 提交以供分析。
- 最近成功和失败的提交状态
- 该插件是由 Handsome 倾注心血开发的。

## 什么是 IndexNow？
IndexNow 是一种让网站所有者立即能够将其网站的最新内容更改告知搜索引擎的简单方式。
在其最简单的形式中，IndexNow 是一个简单的 Ping，
以使搜索引擎了解一个 URL 及其内容已被添加、更新或删除，使搜索引擎能够在其搜索结果中快速反映这一更改。

如果没有 IndexNow，搜索引擎可能需要几天到几周的时间才能发现内容已经改更改，
因为搜索引擎不会经常爬网每个 URL。有了IndexNow，搜索引擎立即知道“URL 已更改，
帮助它们优先爬网这些 URL，从而限制有机爬网以发现新内容”。
## 开发环境

插件开发的详细文档请查阅：<https://docs.halo.run/developer-guide/plugin/introduction>

所需环境：

1. Java 17
2. Node 20
3. pnpm 9
4. Docker (可选)

克隆项目：

```bash
git clone git@github.com:acanyo/plugin-IndexNow.git

# 或者当你 fork 之后

git clone git@github.com:{your_github_id}/plugin-IndexNow.git
```

```bash
cd path/to/plugin-IndexNow
```

### 运行方式 1（推荐）

> 此方式需要本地安装 Docker

```bash
# macOS / Linux
./gradlew pnpmInstall

# Windows
./gradlew.bat pnpmInstall
```

```bash
# macOS / Linux
./gradlew haloServer

# Windows
./gradlew.bat haloServer
```

执行此命令后，会自动创建一个 Halo 的 Docker 容器并加载当前的插件，更多文档可查阅：<https://docs.halo.run/developer-guide/plugin/basics/devtools>

### 运行方式 2

> 此方式需要使用源码运行 Halo

编译插件：

```bash
# macOS / Linux
./gradlew build

# Windows
./gradlew.bat build
```

修改 Halo 配置文件：

```yaml
halo:
  plugin:
    runtime-mode: development
    fixedPluginPath:
      - "/path/to/plugin-IndexNow"
```

最后重启 Halo 项目即可。
