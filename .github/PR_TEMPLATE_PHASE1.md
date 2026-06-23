## 概述

Phase 1 绘图插件接入重构完成。将绘图插件依赖从单体 `draw-image-plugin:1.1.0` 升级为模块化 `runtime + native-jna:2.0.0`，并接入 `SwitchableDrawImagePlugin` 热切换代理。

## 改动清单

### pom.xml
- `draw-image-plugin:1.1.0` → `draw-image-plugin-runtime:2.0.0` + `draw-image-plugin-native-jna:2.0.0`
- runtime 传递依赖 core，native-jna 提供 JNA 桥接支持 .dll/.so/.dylib

### DrawImagePluginManagerConfig
- 使用 `JnaNativePluginLoader` 同时支持 jar + native 插件加载
- 新增 `SwitchableDrawImagePlugin` bean，所有业务 Bean 通过 volatile 代理无感切换
- 移除 `registerDrawImagePlugin()` 静态方法

### DrawImagePluginController
- `/load`: 改用 `activePlugin.switchTo()` 原子切换，替代 destroy/register
- `/current`: 改用 `activePlugin.getCurrent()`
- `/reload`: 保留当前已选插件名，重新匹配
- `savePluginSelection()`: 修复为 upsert 语义

### 附加清理
- `translateDuvalierCycle()`: 去掉冗余 `.peek()` + `.toList()` + `setCategoryChoices()`
- `WorldStateFullPipelineTest`: 补 weaponsRepo H2 数据 stub，验证武器翻译

## 影响范围
- 35+ 业务 Bean 通过 `DrawImagePlugin` 接口注入，**零改动**
- 运行时切换无需重新注入 Spring Bean
- 重启后恢复到最后选择的插件

## 验证清单
- [x] pom.xml 编译通过
- [x] Config 返回 `SwitchableDrawPlugin` 代理
- [x] Controller 切换使用 switchTo 而非 destroy/register
- [x] savePluginSelection upsert 语义
- [x] jar + native 插件同时加载
