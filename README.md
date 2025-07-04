# MCP Alarm Center

这是一个基于Spring Boot的MCP（Model Context Protocol）告警中心服务，提供告警查询和管理功能。

## 功能特性

1. **最新告警查询**：获取最新的告警信息（最多10条）
2. **告警数量统计**：查询指定时间范围的告警数量
3. **告警忽略**：将指定告警设置为忽略状态

## 技术栈

- Spring Boot 3.5.3
- Spring AI MCP Client 1.0.0-M6
- MongoDB
- Java 21

## 快速开始

### 1. 环境要求

- Java 21+
- MongoDB 4.0+
- Maven 3.6+

### 2. 配置MongoDB

确保MongoDB服务正在运行，默认配置：
- 主机：localhost
- 端口：27017
- 数据库：alarm_center

如需修改配置，请编辑 `src/main/resources/application.properties` 文件。

### 3. 启动服务

```bash
# 编译项目
mvn clean compile

# 启动服务
mvn spring-boot:run
```

服务将在 `http://localhost:8080` 启动。

### 4. 验证服务

访问健康检查接口：
```bash
curl http://localhost:8080/api/alarms/health
```

## MCP服务使用

### 在Dify中使用

1. 在Dify中配置MCP工具
2. 输入服务器地址：`http://localhost:8080`
3. 点击授权，系统会自动获取可用工具列表

### 在Cursor中使用

1. 在Cursor中配置MCP SSE工具
2. 输入端口地址：`http://localhost:8080`
3. 点击授权，系统会自动获取可用工具列表

## 可用工具

### 1. get_latest_alarms
获取最新的告警信息

**参数：**
- `limit` (int): 限制数量，最大10条

**示例：**
```
获取最新的5条告警
```

### 2. get_alarm_count
获取指定时间范围的告警数量

**参数：**
- `timeRange` (string): 时间范围，支持：
  - `today`: 今天
  - `yesterday`: 昨天
  - `last7days`: 最近7天
  - `last30days`: 最近30天
  - `lastmonth`: 最近一个月
  - `last3months`: 最近三个月

**示例：**
```
查询今天的告警数量
查询最近7天的告警数量
```

### 3. ignore_alarm
忽略指定的告警

**参数：**
- `alertId` (string): 告警ID

**示例：**
```
忽略告警ID为"ALM001"的告警
```

## HTTP API接口

### 获取最新告警
```bash
GET /api/alarms/latest?limit=5
```

### 获取告警数量
```bash
GET /api/alarms/count?timeRange=today
```

### 忽略告警
```bash
POST /api/alarms/{alertId}/ignore
```

### 健康检查
```bash
GET /api/alarms/health
```

## 数据库结构

项目使用MongoDB存储告警数据，主要集合包括：

- `alarm_info`: 告警主体信息
- `alarm_ignore`: 告警忽略记录

详细的数据表结构请参考 `src/main/resources/告警表.md` 文件。

## 开发说明

### 项目结构
```
src/main/java/tianpan/mcpalarmcenter/
├── config/          # 配置类
├── controller/      # REST控制器
├── dto/            # 数据传输对象
├── entity/         # MongoDB实体
├── mcp/tools/      # MCP工具
├── repository/     # 数据访问层
└── service/        # 业务逻辑层
```

### 添加新功能

1. 在 `service` 包中添加业务逻辑
2. 在 `mcp/tools` 包中添加MCP工具
3. 在 `controller` 包中添加HTTP接口（可选）

## 故障排除

### 常见问题

1. **MongoDB连接失败**
   - 检查MongoDB服务是否启动
   - 验证连接配置是否正确

2. **MCP工具无法识别**
   - 确保Spring AI MCP依赖已正确添加
   - 检查工具类是否正确标注了 `@Tool` 注解

3. **端口冲突**
   - 修改 `application.properties` 中的 `server.port` 配置

## 许可证

本项目采用MIT许可证。 