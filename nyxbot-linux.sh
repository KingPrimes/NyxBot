#!/bin/bash

# ============================================================================
# NyxBot 启动脚本 (Linux) - 改进版
# 版本: 2.0.0
# 功能: 自动安装JDK 21、下载最新版本NyxBot并启动
# 改进: SHA256校验、版本检查、增强错误处理
# ============================================================================

set -euo pipefail  # 严格模式：遇到错误立即退出

# 设置变量
readonly SCRIPT_VERSION="2.0.0"
readonly API_URL="https://api.github.com/repos/KingPrimes/depot/releases/latest"
readonly MIRROR_PREFIX="https://ghproxy.com/"
readonly DOWNLOAD_DIR="./nyxbot_data"
readonly NYXBOT_JAR="$DOWNLOAD_DIR/NyxBot.jar"
readonly VERSION_FILE="$DOWNLOAD_DIR/.version"
readonly LOG_FILE="$DOWNLOAD_DIR/install.log"

# 颜色定义
readonly RED='\033[0;31m'
readonly GREEN='\033[0;32m'
readonly YELLOW='\033[1;33m'
readonly CYAN='\033[0;36m'
readonly NC='\033[0m' # No Color

# 命令行参数
FORCE_UPDATE=false
SKIP_JAVA_INSTALL=false
CUSTOM_MIRROR=""

# 错误处理
trap 'error_handler $? $LINENO' ERR

error_handler() {
    local exit_code=$1
    local line_number=$2
    echo -e "${RED}✗ 错误发生在第 ${line_number} 行，退出码: ${exit_code}${NC}" | tee -a "$LOG_FILE"
    echo "详细日志请查看: $LOG_FILE"
    exit "$exit_code"
}

# 日志函数
log_info() {
    echo -e "${CYAN}[INFO]${NC} $1" | tee -a "$LOG_FILE"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1" | tee -a "$LOG_FILE"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1" | tee -a "$LOG_FILE"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1" | tee -a "$LOG_FILE"
}

# 创建下载目录和日志文件
mkdir -p "$DOWNLOAD_DIR"
echo "=== NyxBot安装日志 $(date '+%Y-%m-%d %H:%M:%S') ===" > "$LOG_FILE"

echo -e "${CYAN}=== NyxBot启动脚本(Linux) v${SCRIPT_VERSION} ===${NC}"

# 解析命令行参数
parse_arguments() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            --force-update)
                FORCE_UPDATE=true
                shift
                ;;
            --skip-java)
                SKIP_JAVA_INSTALL=true
                shift
                ;;
            --mirror)
                CUSTOM_MIRROR="$2"
                shift 2
                ;;
            --version)
                echo "NyxBot启动脚本版本: $SCRIPT_VERSION"
                exit 0
                ;;
            --help)
                show_help
                exit 0
                ;;
            *)
                log_error "未知参数: $1"
                show_help
                exit 1
                ;;
        esac
    done
}

show_help() {
    cat << EOF
用法: $0 [选项]

选项:
  --force-update    强制更新，即使本地已是最新版本
  --skip-java       跳过Java环境检查和安装
  --mirror <url>    使用自定义镜像前缀
  --version         显示脚本版本
  --help            显示此帮助信息

示例:
  $0                          # 正常安装
  $0 --force-update           # 强制更新到最新版本
  $0 --skip-java              # 跳过Java安装（假设已安装JDK 21）
  $0 --mirror https://mirror.ghproxy.com/  # 使用自定义镜像
EOF
}

# 检查Java版本，如果没有安装JDK 21则自动安装
check_and_install_jdk21() {
    if [ "$SKIP_JAVA_INSTALL" = true ]; then
        log_warning "跳过Java环境检查"
        return 0
    fi

    log_info "检查JDK 21环境..."

    if command -v java &> /dev/null; then
        local java_version_output
        java_version_output=$(java -version 2>&1)
        
        # 更精确的版本匹配
        if echo "$java_version_output" | grep -qE '(openjdk|java) version "21\.|openjdk 21\.'; then
            log_success "JDK 21已安装"
            java -version 2>&1 | head -1 | tee -a "$LOG_FILE"
            return 0
        else
            log_warning "检测到Java，但不是版本21:"
            echo "$java_version_output" | head -1 | tee -a "$LOG_FILE"
        fi
    fi

    # 未安装JDK 21，尝试自动安装
    log_info "未检测到JDK 21，尝试自动安装..."

    # 检查是否有sudo权限
    if ! sudo -n true 2>/dev/null; then
        log_warning "需要sudo权限来安装JDK 21，可能会要求输入密码"
    fi

    if [ -f /etc/debian_version ]; then
        # Debian/Ubuntu系统
        log_info "检测到Debian/Ubuntu系统，使用apt安装OpenJDK 21..."
        sudo apt update || log_warning "apt update失败，继续尝试安装"
        sudo apt install -y openjdk-21-jdk
    elif [ -f /etc/redhat-release ]; then
        # RHEL/CentOS/Fedora系统
        log_info "检测到RHEL/CentOS/Fedora系统，使用dnf/yum安装OpenJDK 21..."
        if command -v dnf &> /dev/null; then
            sudo dnf install -y java-21-openjdk-devel
        else
            sudo yum install -y java-21-openjdk-devel
        fi
    elif [ -f /etc/arch-release ]; then
        # Arch Linux
        log_info "检测到Arch Linux，使用pacman安装OpenJDK 21..."
        sudo pacman -Syu --noconfirm jre-openjdk jdk-openjdk
    elif [ -f /etc/alpine-release ]; then
        # Alpine Linux
        log_info "检测到Alpine Linux，使用apk安装OpenJDK 21..."
        sudo apk add openjdk21
    else
        log_error "无法确定Linux发行版，无法自动安装JDK 21"
        log_info "请手动安装OpenJDK 21，或参考以下命令："
        echo "  Ubuntu/Debian: sudo apt install openjdk-21-jdk"
        echo "  CentOS/RHEL: sudo yum install java-21-openjdk-devel"
        echo "  Fedora: sudo dnf install java-21-openjdk-devel"
        exit 1
    fi

    # 验证安装
    if command -v java &> /dev/null && java -version 2>&1 | grep -qE '(openjdk|java) version "21\.|openjdk 21\.'; then
        log_success "OpenJDK 21安装成功"
        java -version 2>&1 | head -1 | tee -a "$LOG_FILE"
    else
        log_error "OpenJDK 21安装失败，请手动安装"
        exit 1
    fi
}

# 检测 OneBot 实现是否安装
check_onebot_implementation() {
    log_info "检查 OneBot 协议实现..."
    
    echo ""
    echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${YELLOW}                  重要提示 - OneBot 协议实现                     ${NC}"
    echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
    echo -e "${CYAN}NyxBot 需要 OneBot 协议实现才能连接 QQ${NC}"
    echo "支持的实现："
    echo "  • LLOneBot  - QQ 官方客户端插件"
    echo "  • NapCatQQ  - 独立 QQ 客户端"
    echo ""
    
    read -p "您是否已安装并配置 LLOneBot 或 NapCatQQ？(y/n): " -n 1 -r
    echo ""
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        log_success "OneBot 实现已确认安装"
        return 0
    fi
    
    # 未安装，显示安装菜单
    show_onebot_install_menu
}

# 显示 OneBot 安装菜单
show_onebot_install_menu() {
    echo ""
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${CYAN}               OneBot 协议实现安装引导                            ${NC}"
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
    echo "请选择要安装的实现："
    echo ""
    echo -e "${GREEN}  1)${NC} LLOneBot ${YELLOW}(推荐 - 适合已有 QQ 的用户)${NC}"
    echo "     ├─ 作为 QQ 插件运行"
    echo "     ├─ 无需额外客户端"
    echo "     ├─ 使用已有 QQ 账号"
    echo "     └─ 文档：${CYAN}https://llonebot.com/guide/getting-started${NC}"
    echo ""
    echo -e "${GREEN}  2)${NC} NapCatQQ ${YELLOW}(推荐 - 独立运行)${NC}"
    echo "     ├─ 独立的 QQ 客户端"
    echo "     ├─ 适合服务器部署"
    echo "     ├─ 支持 Docker"
    echo "     └─ 文档：${CYAN}https://napneko.github.io/guide/boot/Shell${NC}"
    echo ""
    echo -e "${GREEN}  3)${NC} 我已安装，跳过此步骤"
    echo ""
    echo -e "${GREEN}  4)${NC} 退出脚本"
    echo ""
    
    read -p "请选择 [1-4]: " choice
    
    case $choice in
        1)
            show_llonebot_guide
            ;;
        2)
            show_napcat_guide
            ;;
        3)
            log_info "跳过 OneBot 检测"
            return 0
            ;;
        4)
            log_info "退出脚本"
            exit 0
            ;;
        *)
            log_error "无效选择，请重新选择"
            show_onebot_install_menu
            ;;
    esac
}

# LLOneBot 安装指引
show_llonebot_guide() {
    echo ""
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${GREEN}                   LLOneBot 安装指引                              ${NC}"
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
    echo "LLOneBot 是 QQ 的插件，安装步骤："
    echo ""
    echo -e "${CYAN}步骤 1:${NC} 下载并安装 QQ 官方客户端（如果未安装）"
    echo "   Windows: https://im.qq.com/pcqq"
    echo "   Linux:   https://im.qq.com/linuxqq"
    echo ""
    echo -e "${CYAN}步骤 2:${NC} 访问 LLOneBot 官网获取最新版本"
    echo "   ${CYAN}https://llonebot.com/guide/getting-started${NC}"
    echo ""
    echo -e "${CYAN}步骤 3:${NC} 按照文档说明安装 LLOneBot 插件"
    echo "   • 下载对应平台的 LLOneBot 插件"
    echo "   • 将插件放入 QQ 的插件目录"
    echo "   • 重启 QQ 客户端"
    echo ""
    echo -e "${CYAN}步骤 4:${NC} 配置 OneBot 连接信息"
    echo "   • 打开 QQ 设置中的 LLOneBot 配置"
    echo "   • 设置 HTTP/WebSocket 服务端口（默认 3000）"
    echo "   • 确保与 NyxBot 配置匹配"
    echo ""
    echo -e "${YELLOW}⚠️  重要提示:${NC}"
    echo "   • 确保 LLOneBot 服务已启动"
    echo "   • 记录配置的端口号"
    echo "   • 在 NyxBot 配置中填写相同端口"
    echo ""
    
    # 尝试打开浏览器
    if command -v xdg-open &> /dev/null; then
        read -p "是否在浏览器中打开文档？(y/n): " -n 1 -r
        echo ""
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            xdg-open "https://llonebot.com/guide/getting-started" 2>/dev/null || log_warning "无法自动打开浏览器，请手动访问上述链接"
        fi
    elif command -v gnome-open &> /dev/null; then
        read -p "是否在浏览器中打开文档？(y/n): " -n 1 -r
        echo ""
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            gnome-open "https://llonebot.com/guide/getting-started" 2>/dev/null || log_warning "无法自动打开浏览器，请手动访问上述链接"
        fi
    fi
    
    wait_for_installation_complete
}

# NapCatQQ 安装指引
show_napcat_guide() {
    echo ""
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${GREEN}                   NapCatQQ 安装指引                              ${NC}"
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
    echo "NapCatQQ 是独立的 QQ 客户端，安装步骤："
    echo ""
    echo -e "${CYAN}步骤 1:${NC} 访问 NapCatQQ 官网"
    echo "   ${CYAN}https://napneko.github.io/guide/boot/Shell${NC}"
    echo ""
    echo -e "${CYAN}步骤 2:${NC} 下载适合您系统的版本"
    echo "   • Linux:   Shell 脚本 / Docker"
    echo "   • macOS:   Shell 脚本"
    echo "   • Windows: 安装包 / Shell 脚本"
    echo ""
    echo -e "${CYAN}步骤 3:${NC} 安装方法（以 Linux Shell 为例）"
    echo "   ${YELLOW}# 下载安装脚本${NC}"
    echo "   curl -o napcat.sh https://nclatest.znin.net/NapCat/NapCat-Installer/main/script/install.sh"
    echo ""
    echo "   ${YELLOW}# 添加执行权限${NC}"
    echo "   chmod +x napcat.sh"
    echo ""
    echo "   ${YELLOW}# 运行安装脚本${NC}"
    echo "   sudo ./napcat.sh"
    echo ""
    echo -e "${CYAN}步骤 4:${NC} 配置 NapCatQQ"
    echo "   • 编辑配置文件 napcat.json"
    echo "   • 设置 HTTP/WebSocket 端口（默认 3000）"
    echo "   • 配置 QQ 账号信息"
    echo ""
    echo -e "${CYAN}步骤 5:${NC} 启动 NapCatQQ"
    echo "   ${YELLOW}# 启动服务${NC}"
    echo "   napcat start"
    echo ""
    echo "   ${YELLOW}# 扫码登录 QQ${NC}"
    echo ""
    echo -e "${YELLOW}⚠️  重要提示:${NC}"
    echo "   • 首次启动需要扫码登录"
    echo "   • 记录配置的端口号"
    echo "   • 确保 NapCat 服务持续运行"
    echo ""
    
    # 尝试打开浏览器
    if command -v xdg-open &> /dev/null; then
        read -p "是否在浏览器中打开文档？(y/n): " -n 1 -r
        echo ""
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            xdg-open "https://napneko.github.io/guide/boot/Shell" 2>/dev/null || log_warning "无法自动打开浏览器，请手动访问上述链接"
        fi
    elif command -v gnome-open &> /dev/null; then
        read -p "是否在浏览器中打开文档？(y/n): " -n 1 -r
        echo ""
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            gnome-open "https://napneko.github.io/guide/boot/Shell" 2>/dev/null || log_warning "无法自动打开浏览器，请手动访问上述链接"
        fi
    fi
    
    wait_for_installation_complete
}

# 等待用户完成安装
wait_for_installation_complete() {
    echo ""
    log_info "请按照上述步骤完成安装"
    echo ""
    read -p "完成安装后按任意键继续..." -n 1 -r
    echo ""
    echo ""
    
    # 再次确认
    read -p "确认已完成 OneBot 实现的安装和配置？(y/n): " -n 1 -r
    echo ""
    
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_warning "未完成安装，返回菜单"
        show_onebot_install_menu
    else
        log_success "OneBot 实现配置完成，继续启动 NyxBot"
    fi
}

# 下载文件函数（带重试和校验）
download_file() {
    local url=$1
    local destination=$2
    local description=$3
    local expected_sha256=$4  # 可选参数

    log_info "下载 $description..."
    
    # 使用自定义镜像（如果提供）
    local mirror_prefix="${CUSTOM_MIRROR:-$MIRROR_PREFIX}"
    
    # 首次尝试直接下载
    if curl -L -o "$destination" "$url" --connect-timeout 10 --max-time 300 --retry 3 -H "User-Agent: Mozilla/5.0" 2>> "$LOG_FILE"; then
        log_success "$description 下载完成"
        
        # 验证SHA256（如果提供）
        if [ -n "$expected_sha256" ]; then
            verify_sha256 "$destination" "$expected_sha256" "$description"
        fi
        return 0
    else
        log_warning "直接下载失败，尝试使用镜像..."
        local mirror_url="${mirror_prefix}${url#https://}"
        
        if curl -L -o "$destination" "$mirror_url" --connect-timeout 10 --max-time 300 --retry 3 -H "User-Agent: Mozilla/5.0" 2>> "$LOG_FILE"; then
            log_success "$description (镜像) 下载完成"
            
            # 验证SHA256（如果提供）
            if [ -n "$expected_sha256" ]; then
                verify_sha256 "$destination" "$expected_sha256" "$description"
            fi
            return 0
        else
            log_error "无法下载 $description"
            return 1
        fi
    fi
}

# SHA256校验函数
verify_sha256() {
    local file=$1
    local expected_sha256=$2
    local description=$3

    log_info "验证 $description 的SHA256校验和..."
    
    if command -v sha256sum &> /dev/null; then
        local actual_sha256
        actual_sha256=$(sha256sum "$file" | awk '{print $1}')
        
        if [ "$actual_sha256" = "$expected_sha256" ]; then
            log_success "SHA256校验通过"
            return 0
        else
            log_error "SHA256校验失败！"
            log_error "期望: $expected_sha256"
            log_error "实际: $actual_sha256"
            log_warning "文件可能已被篡改，删除下载的文件"
            rm -f "$file"
            return 1
        fi
    else
        log_warning "sha256sum命令不可用，跳过校验"
        return 0
    fi
}

# 从API获取最新release信息
get_latest_release() {
    log_info "获取最新release信息..."
    
    local api_response
    api_response=$(curl -s -H "User-Agent: Mozilla/5.0" -H "Accept: application/vnd.github.v3+json" "$API_URL" --connect-timeout 10 --retry 3 2>> "$LOG_FILE")

    if [ $? -ne 0 ] || [ -z "$api_response" ] || [[ "$api_response" == *"Not Found"* ]]; then
        log_warning "API请求失败，尝试使用镜像..."
        local mirror_prefix="${CUSTOM_MIRROR:-$MIRROR_PREFIX}"
        local api_url_mirror="${mirror_prefix}${API_URL#https://}"
        api_response=$(curl -s -H "User-Agent: Mozilla/5.0" -H "Accept: application/vnd.github.v3+json" "$api_url_mirror" --connect-timeout 10 --retry 3 2>> "$LOG_FILE")
    fi

    if [ -z "$api_response" ] || [[ "$api_response" == *"Not Found"* ]]; then
        log_error "无法获取最新release信息"
        echo "$api_response" >> "$LOG_FILE"
        exit 1
    fi

    # 解析JSON获取信息
    if command -v jq &> /dev/null; then
        DOWNLOAD_URL=$(echo "$api_response" | jq -r '.assets[] | select(.name | endswith(".jar")) | .browser_download_url' | head -1)
        ASSET_NAME=$(echo "$api_response" | jq -r '.assets[] | select(.name | endswith(".jar")) | .name' | head -1)
        RELEASE_TAG=$(echo "$api_response" | jq -r '.tag_name')
        
        # 尝试获取SHA256（如果存在.sha256文件）
        SHA256_URL=$(echo "$api_response" | jq -r '.assets[] | select(.name | endswith(".jar.sha256")) | .browser_download_url' | head -1)
    else
        # 备用方法解析JSON
        DOWNLOAD_URL=$(echo "$api_response" | grep -o '"browser_download_url":\s*"[^"]*\.jar"' | head -1 | sed -E 's/.*"([^"]+\.jar)".*/\1/')
        ASSET_NAME=$(echo "$api_response" | grep -o '"name":\s*"[^"]*\.jar"' | head -1 | sed -E 's/.*"([^"]+\.jar)".*/\1/')
        RELEASE_TAG=$(echo "$api_response" | grep -o '"tag_name":\s*"[^"]*"' | head -1 | sed -E 's/.*"([^"]+)".*/\1/')
        SHA256_URL=$(echo "$api_response" | grep -o '"browser_download_url":\s*"[^"]*\.jar\.sha256"' | head -1 | sed -E 's/.*"([^"]+\.jar\.sha256)".*/\1/')
    fi

    if [ -z "$DOWNLOAD_URL" ] || [ "$DOWNLOAD_URL" = "null" ]; then
        log_error "无法解析下载URL"
        echo "$api_response" >> "$LOG_FILE"
        exit 1
    fi

    log_success "找到最新构建: $ASSET_NAME (版本: $RELEASE_TAG)"
    echo "下载URL: $DOWNLOAD_URL" >> "$LOG_FILE"
}

# 检查版本是否需要更新
check_version() {
    if [ "$FORCE_UPDATE" = true ]; then
        log_info "强制更新模式，将重新下载"
        return 1  # 需要更新
    fi

    if [ ! -f "$VERSION_FILE" ]; then
        log_info "未找到版本信息，将下载最新版本"
        return 1  # 需要更新
    fi

    local current_version
    current_version=$(cat "$VERSION_FILE" 2>/dev/null || echo "unknown")
    
    if [ "$current_version" = "$RELEASE_TAG" ]; then
        log_success "本地版本 ($current_version) 已是最新版本"
        return 0  # 不需要更新
    else
        log_info "发现新版本: $RELEASE_TAG (当前: $current_version)"
        return 1  # 需要更新
    fi
}

# 下载SHA256校验和
download_sha256() {
    if [ -z "$SHA256_URL" ] || [ "$SHA256_URL" = "null" ]; then
        log_warning "Release中未提供SHA256文件，跳过校验"
        return 1
    fi

    local sha256_file="$DOWNLOAD_DIR/NyxBot.jar.sha256"
    
    if download_file "$SHA256_URL" "$sha256_file" "SHA256校验文件" ""; then
        # 读取SHA256值
        EXPECTED_SHA256=$(cat "$sha256_file" | awk '{print $1}')
        log_info "获取到SHA256: $EXPECTED_SHA256"
        return 0
    else
        log_warning "无法下载SHA256文件，将跳过校验"
        return 1
    fi
}

# 主流程
main() {
    parse_arguments "$@"
    
    check_and_install_jdk21
    
    # 检查 OneBot 实现
    check_onebot_implementation
    
    get_latest_release
    
    # 检查是否需要更新
    if [ -f "$NYXBOT_JAR" ] && check_version; then
        log_info "使用现有版本，直接启动..."
    else
        # 下载SHA256（如果存在）
        EXPECTED_SHA256=""
        download_sha256 && true  # 即使失败也继续
        
        # 备份旧版本（如果存在）
        if [ -f "$NYXBOT_JAR" ]; then
            local backup_file="${NYXBOT_JAR}.bak"
            log_info 
    get_latest_release
    
    # 检查是否需要更新
    if [ -f "$NYXBOT_JAR" ] && check_version; then
        log_info "使用现有版本，直接启动..."
    else
        # 下载SHA256（如果存在）
        EXPECTED_SHA256=""
        download_sha256 && true  # 即使失败也继续
        
        # 备份旧版本（如果存在）
        if [ -f "$NYXBOT_JAR" ]; then
            local backup_file="${NYXBOT_JAR}.bak"
            log_info "备份旧版本到 $backup_file"
            mv "$NYXBOT_JAR" "$backup_file"
        fi
        
        # 下载NyxBot.jar
        if ! download_file "$DOWNLOAD_URL" "$NYXBOT_JAR" "NyxBot.jar" "$EXPECTED_SHA256"; then
            log_error "下载失败"
            
            # 如果有备份，恢复备份
            if [ -f "${NYXBOT_JAR}.bak" ]; then
                log_info "恢复备份版本..."
                mv "${NYXBOT_JAR}.bak" "$NYXBOT_JAR"
            fi
            exit 1
        fi
        
        # 验证JAR文件完整性
        if command -v file &> /dev/null; then
            if ! file "$NYXBOT_JAR" | grep -q "Java archive data"; then
                log_error "下载的文件不是有效的JAR文件"
                rm -f "$NYXBOT_JAR"
                
                # 恢复备份
                if [ -f "${NYXBOT_JAR}.bak" ]; then
                    log_info "恢复备份版本..."
                    mv "${NYXBOT_JAR}.bak" "$NYXBOT_JAR"
                fi
                exit 1
            fi
        fi
        
        # 保存版本信息
        echo "$RELEASE_TAG" > "$VERSION_FILE"
        log_success "版本信息已保存: $RELEASE_TAG"
        
        # 删除备份
        rm -f "${NYXBOT_JAR}.bak"
    fi
    
    # 启动程序
    log_info "启动 NyxBot..."
    if ! java -jar "$NYXBOT_JAR"; then
        log_error "NyxBot启动失败"
        exit 1
    fi
    
    log_success "脚本执行完成"
}

# 执行主函数
main "$@"