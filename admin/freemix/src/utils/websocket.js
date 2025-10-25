// utils/websocket.js
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import store from '@/stores/index.js'; // 导入store以获取用户信息
import { isDesktop } from './device.js';
import { getLocalStorageDesktopToken } from './desktopToken.js';
import { getToken } from './tokenUtils.js';

let stompClient = null;
let retryCount = 0;
const MAX_RETRIES = 5;

export async function connect() {
    // 修复WebSocket连接URL配置
    const baseUrl = import.meta.env.PROD ? 'http://8.148.242.131' : 'http://localhost:5173';
    
    // 获取token用于WebSocket连接认证
    const token = await getToken();
    
    // 通过URL参数传递认证信息，确保在WebSocket握手时能正确传递
    let wsUrl = baseUrl + '/ws';
    if (token) {
        if (isDesktop()) {
            // 桌面端使用deskToken参数
            const desktopToken = getLocalStorageDesktopToken();
            wsUrl += `?deskToken=${encodeURIComponent(desktopToken || token)}`;
        } else {
            // 移动端使用token参数
            wsUrl += `?token=${encodeURIComponent(token)}`;
        }
    }
    
    // 创建SockJS连接
    const socket = new SockJS(wsUrl);
    
    stompClient = Stomp.over(socket);
    
    // 连接时不需要传递headers，因为认证信息已经在URL中了
    stompClient.connect({}, function (frame) {
        console.log('Connected to server: ' + frame);
        retryCount = 0; // 重置重试计数
        
        // // 显示连接成功的提示（仅在生产环境）
        // if (import.meta.env.PROD) {
        //     // 创建一个临时的div来显示提示信息
        //     const notification = document.createElement('div');
        //     notification.innerHTML = `WebSocket连接成功<br><small>连接地址: ${wsUrl}</small>`;
        //     notification.style.cssText = `
        //         position: fixed;
        //         top: 20px;
        //         right: 20px;
        //         background-color: #4CAF50;
        //         color: white;
        //         padding: 16px;
        //         border-radius: 4px;
        //         z-index: 9999;
        //         font-family: Arial, sans-serif;
        //         font-size: 14px;
        //         box-shadow: 0 2px 8px rgba(0,0,0,0.2);
        //         max-width: 400px;
        //         word-break: break-all;
        //     `;
        //     document.body.appendChild(notification);
        //
        //     // 3秒后自动移除提示
        //     setTimeout(() => {
        //         if (notification.parentNode) {
        //             notification.parentNode.removeChild(notification);
        //         }
        //     }, 3000);
        // }
        
        // 从store中获取当前用户名
        const user = store.state.user;
        console.log('Current user object from store:', user);
        const username = user?.username || 'unknown';
        console.log('Current username for subscription:', username);
        
        if (username === 'unknown') {
            console.warn('User not logged in, skipping subscription');
            return;
        }
        
        // 订阅服务器广播消息的主题
        // 使用动态用户目的地订阅，确保与后端配置匹配
        const subscriptionPath = `/user/${username}/queue/private`;
        console.log('Subscribing to path:', subscriptionPath);
        
        stompClient.subscribe(subscriptionPath, function (message) {
            // 当收到消息时，调用回调函数，例如更新页面数据
            console.log('Received from server: ' + message.body);
            console.log('Full message object:', message);
            
            // 检查window.handleWebSocketMessage是否存在
            if (window.handleWebSocketMessage) {
                window.handleWebSocketMessage(message.body);
            } else {
                // 如果window.handleWebSocketMessage不存在，等待一段时间后重试
                console.warn('window.handleWebSocketMessage is not defined, retrying in 1 second...');
                setTimeout(() => {
                    if (window.handleWebSocketMessage) {
                        console.log('Calling window.handleWebSocketMessage (retry)');
                        window.handleWebSocketMessage(message.body);
                    } else {
                        console.error('window.handleWebSocketMessage is still not defined after retry!');
                    }
                }, 1000);
            }
        });
        
        console.log('Successfully subscribed to private messages');
    }, function(error) {
        // console.error('WebSocket connection error:', error);
        //
        // // 显示连接失败的提示（仅在生产环境）
        // if (import.meta.env.PROD) {
        //     // 创建一个临时的div来显示错误信息
        //     const notification = document.createElement('div');
        //     notification.innerHTML = `WebSocket连接失败，正在重试...<br><small>连接地址: ${wsUrl}</small><br><small>错误信息: ${error.message || error}</small>`;
        //     notification.style.cssText = `
        //         position: fixed;
        //         top: 20px;
        //         right: 20px;
        //         background-color: #f44336;
        //         color: white;
        //         padding: 16px;
        //         border-radius: 4px;
        //         z-index: 9999;
        //         font-family: Arial, sans-serif;
        //         font-size: 14px;
        //         box-shadow: 0 2px 8px rgba(0,0,0,0.2);
        //         max-width: 400px;
        //         word-break: break-all;
        //     `;
        //     document.body.appendChild(notification);
        //
        //     // 3秒后自动移除提示
        //     setTimeout(() => {
        //         if (notification.parentNode) {
        //             notification.parentNode.removeChild(notification);
        //         }
        //     }, 3000);
        // }
        
        // 实现重连机制
        if (retryCount < MAX_RETRIES) {
            retryCount++;
            console.log(`Retrying connection (${retryCount}/${MAX_RETRIES}) in 5 seconds...`);
            setTimeout(connect, 5000);
        } else {
            // // 显示最终连接失败的提示（仅在生产环境）
            // if (import.meta.env.PROD) {
            //     // 创建一个临时的div来显示错误信息
            //     const notification = document.createElement('div');
            //     notification.textContent = 'WebSocket连接失败，请刷新页面重试';
            //     notification.style.cssText = `
            //         position: fixed;
            //         top: 20px;
            //         right: 20px;
            //         background-color: #f44336;
            //         color: white;
            //         padding: 16px;
            //         border-radius: 4px;
            //         z-index: 9999;
            //         font-family: Arial, sans-serif;
            //         font-size: 14px;
            //         box-shadow: 0 2px 8px rgba(0,0,0,0.2);
            //     `;
            //     document.body.appendChild(notification);
            //
            //     // 不自动移除，让用户看到错误信息
            // }
        }
    });
}

// 保留getAuthHeaders函数以保持向后兼容性，但不再使用
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