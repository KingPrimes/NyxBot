Options:
 -am,--also-make                        如果指定了项目列表，则构建所需的项目列表

 -amd,--also-make-dependents            如果指定了项目列表，则构建依赖于列表中的项目

 -B,--batch-mode                        以非交互式（批处理）运行模式（禁用输出颜色）

 -b,--builder <arg>                     生成策略的 id 到用

 -C,--strict-checksums                  如果校验和不匹配，则生成失败

 -c,--lax-checksums                     如果校验和不匹配，则发出警告 定义输出的颜色模式。支持有“自动”、“始终”、“从不”。
    --color <arg>

 -cpu,--check-plugin-updates            无效，仅保留以向后兼容

 -D,--define <arg>                      定义用户属性

 -e,--errors                            生成执行错误消息

 -emp,--encrypt-master-password <arg>   加密主安全密码

 -ep,--encrypt-password <arg>           加密服务器密码

 -f,--file <arg>                        强制使用备用 POM 文件（或带有 pom 的目录.xml）

 -fae,--fail-at-end                     仅在之后使生成失败;允许所有不受影响的生成继续

 -ff,--fail-fast                        在反应器化构建中停止首次失败

 -fn,--fail-never                       无论项目结果如何，都不要失败构建

 -gs,--global-settings <arg>            全局的备用路径

 -P,--activate-profiles <arg>           要激活的配置文件列表以逗号分隔

 -pl,--projects <arg>                   要构建的指定反应堆项目的逗号分隔列表，而不是所有项目。项目可以通过 [groupId]：artifactId 或其相对路径指定

 -q,--quiet                             安静输出 - 仅显示错误
 -rf,--resume-from <arg>                从指定项目恢复反应堆

 -s,--settings <arg>                    用户设置文件的备用路径

 -t,--toolchains <arg>                 用户工具链文件的备用路径

 -T,--threads <arg>                     线程数，例如 4 （int） 或 2C/2.5C（int/float），其中 C 是内核乘法

 -U,--update-snapshots                  强制检查远程存储库上缺少的版本和更新的快照

 -up,--update-plugins                   无效，仅保留以向后兼容

 -v,--version                           显示版本信息

 -V,--show-version                      在不停止构建的情况下显示版本信息

 -X,--debug                             生成执行调试输出
