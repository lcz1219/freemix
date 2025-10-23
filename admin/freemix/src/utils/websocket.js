// utils/websocket.js
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import { isDesktop } from './device.js';
import { getLocalStorageDesktopToken } from './desktopToken.js';
import { getToken } from './tokenUtils.js';

let stompClient = null;

export async function connect() {
    // 修复WebSocket连接URL配置
    const baseUrl = import.meta.env.PROD ? 'http://8.148.242.131' : 'http://localhost:8888';
    
    // 获取token用于WebSocket连接认证
    const token = await getToken();
    
    // 创建SockJS连接时添加认证头
    const socket = new SockJS(baseUrl + '/ws', null, {
        // 添加认证头信息
        headers: getAuthHeaders(token)
    });
    
    stompClient = Stomp.over(socket);
    
    stompClient.connect(getAuthHeaders(token), function (frame) {
        console.log('Connected to server: ' + frame);
        // 订阅服务器广播消息的主题
        // 修复订阅路径，与后端MessageController中的配置保持一致
        stompClient.subscribe('/user/topic/private', function (message) {
            // 当收到消息时，调用回调函数，例如更新页面数据
            console.log('Received from server: ' + message.body);
            // 这里通常会将消息传递给Vue组件，用于更新界面
            if (window.handleWebSocketMessage) {
                window.handleWebSocketMessage(message.body);
            }
        });
    });
}

// 获取认证头信息
function getAuthHeaders(token) {
    const headers = {};
    
    if (token) {
        if (isDesktop()) {
            // 桌面端使用X-Desktop-Token头
            const desktopToken = getLocalStorageDesktopToken();
            headers['X-Desktop-Token'] = desktopToken || token;
        } else {
            // 移动端使用Authorization头
            headers['Authorization'] = `Bearer ${token}`;
        }
    }
    
    return headers;
}

export function sendMessageWeb(message) {
    if (stompClient && stompClient.connected) {
        console.log('Sending message: ' + message);
        // 发送消息到服务器端的 /app/chat.private，与后端@MessageMapping注解匹配
        stompClient.send("/app/chat.private", {}, message);
    }
}

export function disconnect() {
    if (stompClient) {
        stompClient.disconnect();
    }
}