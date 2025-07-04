package tianpan.mcpalarmcenter.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tianpan.mcpalarmcenter.mcp.tools.AlarmTools;
import tianpan.mcpalarmcenter.service.AlarmService;
import org.springframework.beans.factory.annotation.Autowired;
import tianpan.mcpalarmcenter.mcp.tools.McpToolRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RestController
@RequiredArgsConstructor
public class McpController {
    
    private final AlarmService alarmService;
    private final AlarmTools alarmTools;
    
    // 存储SSE连接
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final AtomicLong messageIdCounter = new AtomicLong(1);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private McpToolRegistry mcpToolRegistry;
    
    /**
     * MCP SSE端点 - 用于Cursor等客户端连接
     */
    @RequestMapping(value = "/sse", method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sse() {
        String clientId = "client_" + System.currentTimeMillis();
        log.info("=== SSE连接建立 ===");
        log.info("客户端ID: {}", clientId);
        log.info("当前连接数: {}", emitters.size() + 1);
        
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(clientId, emitter);
        
        emitter.onCompletion(() -> {
            log.info("=== SSE连接完成 ===");
            log.info("客户端ID: {}", clientId);
            emitters.remove(clientId);
            log.info("剩余连接数: {}", emitters.size());
        });
        
        emitter.onTimeout(() -> {
            log.warn("=== SSE连接超时 ===");
            log.warn("客户端ID: {}", clientId);
            emitters.remove(clientId);
            log.info("剩余连接数: {}", emitters.size());
        });
        
        emitter.onError((ex) -> {
            log.error("=== SSE连接错误 ===");
            log.error("客户端ID: {}", clientId);
            log.error("错误信息: {}", ex.getMessage(), ex);
            emitters.remove(clientId);
            log.info("剩余连接数: {}", emitters.size());
        });
        
        try {
            // 发送MCP协议初始化消息
            Map<String, Object> initMessage = new HashMap<>();
            initMessage.put("jsonrpc", "2.0");
            initMessage.put("id", messageIdCounter.getAndIncrement());
            initMessage.put("method", "initialize");
            
            Map<String, Object> params = new HashMap<>();
            params.put("protocolVersion", "2024-11-05");
            params.put("capabilities", new HashMap<>());
            params.put("clientInfo", Map.of("name", "cursor", "version", "1.0.0"));
            
            initMessage.put("params", params);
            
            String initMessageJson = objectMapper.writeValueAsString(initMessage);
            log.info("=== 发送初始化消息 ===");
            log.info("消息内容: {}", initMessageJson);
            
            emitter.send(SseEmitter.event()
                .name("message")
                .data(initMessageJson));
            
            // 发送初始化完成消息
            Map<String, Object> initializedMessage = new HashMap<>();
            initializedMessage.put("jsonrpc", "2.0");
            initializedMessage.put("id", messageIdCounter.getAndIncrement());
            initializedMessage.put("method", "initialized");
            initializedMessage.put("params", new HashMap<>());
            
            String initializedMessageJson = objectMapper.writeValueAsString(initializedMessage);
            log.info("=== 发送初始化完成消息 ===");
            log.info("消息内容: {}", initializedMessageJson);
            
            emitter.send(SseEmitter.event()
                .name("message")
                .data(initializedMessageJson));
                
            log.info("=== SSE连接建立完成 ===");
                
        } catch (IOException e) {
            log.error("=== SSE连接建立失败 ===");
            log.error("错误信息: {}", e.getMessage(), e);
            emitter.completeWithError(e);
        }
        
        return emitter;
    }
    
    /**
     * MCP SSE消息处理端点
     */
    @PostMapping(value = "/sse", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void handleSseMessage(@RequestBody Map<String, Object> message) {
        log.info("=== 收到SSE消息 ===");
        log.info("消息内容: {}", message);
        
        try {
            String method = (String) message.get("method");
            Map<String, Object> params = (Map<String, Object>) message.get("params");
            Object id = message.get("id");
            
            log.info("方法: {}, ID: {}, 参数: {}", method, id, params);
            
            Map<String, Object> response = new HashMap<>();
            response.put("jsonrpc", "2.0");
            response.put("id", id);
            
            switch (method) {
                case "tools/list":
                    log.info("=== 处理工具列表请求 ===");
                    List<Map<String, Object>> tools = mcpToolRegistry.getToolDescriptions();
                    log.info("可用工具数量: {}", tools.size());
                    for (Map<String, Object> tool : tools) {
                        log.info("工具: {} - {}", tool.get("name"), tool.get("description"));
                    }
                    
                    Map<String, Object> toolsResult = new HashMap<>();
                    toolsResult.put("tools", tools);
                    response.put("result", toolsResult);
                    break;
                    
                case "tools/call":
                    log.info("=== 处理工具调用请求 ===");
                    String toolName = (String) params.get("name");
                    Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
                    log.info("调用工具: {}, 参数: {}", toolName, arguments);
                    
                    Object result = mcpToolRegistry.invokeTool(toolName, arguments);
                    log.info("工具调用结果: {}", result);
                    response.put("result", result);
                    break;
                    
                case "resources/list":
                    log.info("=== 处理资源列表请求 ===");
                    Map<String, Object> resourcesResult = new HashMap<>();
                    resourcesResult.put("resources", new HashMap<>());
                    response.put("result", resourcesResult);
                    break;
                    
                case "prompts/list":
                    log.info("=== 处理提示词列表请求 ===");
                    Map<String, Object> promptsResult = new HashMap<>();
                    promptsResult.put("prompts", new HashMap<>());
                    response.put("result", promptsResult);
                    break;
                    
                default:
                    log.warn("=== 未知方法 ===");
                    log.warn("未知方法: {}", method);
                    Map<String, Object> error = new HashMap<>();
                    error.put("code", -32601);
                    error.put("message", "Method not found: " + method);
                    response.put("error", error);
            }
            
            String responseJson = objectMapper.writeValueAsString(response);
            log.info("=== 发送响应 ===");
            log.info("响应内容: {}", responseJson);
            log.info("当前连接数: {}", emitters.size());
            
            // 向所有连接的客户端发送响应
            int sentCount = 0;
            for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
                try {
                    entry.getValue().send(SseEmitter.event()
                        .name("message")
                        .data(responseJson));
                    sentCount++;
                    log.info("成功发送到客户端: {}", entry.getKey());
                } catch (IOException e) {
                    log.error("发送到客户端失败: {}, 错误: {}", entry.getKey(), e.getMessage());
                    // 移除失效的连接
                    emitters.remove(entry.getKey());
                }
            }
            log.info("成功发送到 {} 个客户端", sentCount);
            
        } catch (Exception e) {
            log.error("=== 处理SSE消息失败 ===");
            log.error("错误信息: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("jsonrpc", "2.0");
            errorResponse.put("id", message.get("id"));
            Map<String, Object> error = new HashMap<>();
            error.put("code", -32603);
            error.put("message", "Internal error: " + e.getMessage());
            errorResponse.put("error", error);
            
            try {
                String errorJson = objectMapper.writeValueAsString(errorResponse);
                log.info("发送错误响应: {}", errorJson);
                
                for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
                    try {
                        entry.getValue().send(SseEmitter.event()
                            .name("message")
                            .data(errorJson));
                    } catch (IOException ex) {
                        log.error("发送错误响应失败: {}, 错误: {}", entry.getKey(), ex.getMessage());
                        emitters.remove(entry.getKey());
                    }
                }
            } catch (Exception ex) {
                log.error("序列化错误响应失败: {}", ex.getMessage(), ex);
            }
        }
    }
    
    /**
     * MCP 消息端点
     */
    @PostMapping(value = "/mcp/message", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> handleMcpMessage(@RequestBody Map<String, Object> message) {
        log.info("=== 收到MCP消息 ===");
        log.info("消息内容: {}", message);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String method = (String) message.get("method");
            Map<String, Object> params = (Map<String, Object>) message.get("params");
            Object id = message.get("id");
            
            log.info("方法: {}, ID: {}, 参数: {}", method, id, params);
            
            response.put("jsonrpc", "2.0");
            response.put("id", id);
            
            switch (method) {
                case "tools/list":
                    log.info("=== 处理工具列表请求 ===");
                    List<Map<String, Object>> tools = mcpToolRegistry.getToolDescriptions();
                    log.info("可用工具数量: {}", tools.size());
                    for (Map<String, Object> tool : tools) {
                        log.info("工具: {} - {}", tool.get("name"), tool.get("description"));
                    }
                    
                    Map<String, Object> toolsResult = new HashMap<>();
                    toolsResult.put("tools", tools);
                    response.put("result", toolsResult);
                    break;
                    
                case "tools/call":
                    log.info("=== 处理工具调用请求 ===");
                    String toolName = (String) params.get("name");
                    Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
                    log.info("调用工具: {}, 参数: {}", toolName, arguments);
                    
                    Object result = mcpToolRegistry.invokeTool(toolName, arguments);
                    log.info("工具调用结果: {}", result);
                    response.put("result", result);
                    break;
                    
                case "resources/list":
                    log.info("=== 处理资源列表请求 ===");
                    Map<String, Object> resourcesResult = new HashMap<>();
                    resourcesResult.put("resources", new HashMap<>());
                    response.put("result", resourcesResult);
                    break;
                    
                case "prompts/list":
                    log.info("=== 处理提示词列表请求 ===");
                    Map<String, Object> promptsResult = new HashMap<>();
                    promptsResult.put("prompts", new HashMap<>());
                    response.put("result", promptsResult);
                    break;
                    
                default:
                    log.warn("=== 未知方法 ===");
                    log.warn("未知方法: {}", method);
                    Map<String, Object> error = new HashMap<>();
                    error.put("code", -32601);
                    error.put("message", "Method not found: " + method);
                    response.put("error", error);
            }
        } catch (Exception e) {
            log.error("=== 处理MCP消息失败 ===");
            log.error("错误信息: {}", e.getMessage(), e);
            
            response.put("jsonrpc", "2.0");
            response.put("id", message.get("id"));
            Map<String, Object> error = new HashMap<>();
            error.put("code", -32603);
            error.put("message", "Internal error: " + e.getMessage());
            response.put("error", error);
        }
        
        log.info("=== 返回MCP响应 ===");
        log.info("响应内容: {}", response);
        return response;
    }
    
    /**
     * MCP 工具列表端点
     */
    @GetMapping(value = "/mcp/tools", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getTools() {
        log.info("=== 收到工具列表GET请求 ===");
        
        Map<String, Object> response = new HashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", messageIdCounter.getAndIncrement());
        
        List<Map<String, Object>> tools = mcpToolRegistry.getToolDescriptions();
        log.info("可用工具数量: {}", tools.size());
        for (Map<String, Object> tool : tools) {
            log.info("工具: {} - {}", tool.get("name"), tool.get("description"));
        }
        
        response.put("result", Map.of("tools", tools));
        log.info("返回工具列表: {}", response);
        return response;
    }
    
    /**
     * MCP 资源列表端点
     */
    @GetMapping(value = "/mcp/resources", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getResources() {
        log.info("=== 收到资源列表GET请求 ===");
        
        Map<String, Object> response = new HashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", messageIdCounter.getAndIncrement());
        response.put("result", Map.of("resources", new HashMap<>()));
        
        log.info("返回资源列表: {}", response);
        return response;
    }
    
    /**
     * MCP 提示词端点
     */
    @GetMapping(value = "/mcp/prompts", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getPrompts() {
        log.info("=== 收到提示词列表GET请求 ===");
        
        Map<String, Object> response = new HashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", messageIdCounter.getAndIncrement());
        response.put("result", Map.of("prompts", new HashMap<>()));
        
        log.info("返回提示词列表: {}", response);
        return response;
    }
    
    /**
     * MCP 健康检查端点
     */
    @GetMapping(value = "/mcp/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> health() {
        log.info("=== 收到健康检查请求 ===");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "alarm-center-mcp");
        response.put("version", "1.0.0");
        response.put("activeConnections", emitters.size());
        response.put("availableTools", mcpToolRegistry.getToolNames().size());
        
        log.info("健康检查响应: {}", response);
        return response;
    }
} 