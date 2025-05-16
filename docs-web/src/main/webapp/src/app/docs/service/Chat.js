'use strict';

/**
 * Chat service.
 */
angular.module('docs').factory('Chat', function ($rootScope, Restangular) {
    var service = {
        messages: [],
        ws: null,
        currentChat: null,

        // 加载历史消息
        loadMessages: function (withUser) {
            console.log('加载与用户的历史消息:', withUser);
            return Restangular.one('message', $rootScope.userInfo.username)
                .get({
                    with: withUser,
                    limit: 50
                }).then(function (data) {
                    console.log('获取到历史消息:', data.messages);
                    service.messages = data.messages;
                });
        },

        // 连接WebSocket
        connect: function (username) {
            if (service.ws) {
                console.log('关闭现有WebSocket连接');
                service.ws.close();
            }

            // 连接到WebSocket服务器
            var wsUrl = window.location.protocol.replace('http', 'ws') + '//' +
                window.location.host + '/docs-web/ws/chat/' + username;
            console.log('连接WebSocket:', wsUrl);

            try {
                service.ws = new WebSocket(wsUrl);

                service.ws.onopen = function () {
                    console.log('WebSocket连接已建立');
                };

                service.ws.onmessage = function (event) {
                    if (event.data === 'ping') {
                        service.ws.send('pong');
                        return;
                    }

                    try {
                        var msg = JSON.parse(event.data);
                        console.log('收到消息:', msg);

                        // 更新消息状态为"已接收"
                        if (msg.from !== 'me' && msg.id) {
                            Restangular.one('message', msg.id)
                                .post('', { status: 'RECEIVED' });
                        }

                        service.messages.push(msg);
                        if (!$rootScope.$$phase) {
                            $rootScope.$apply();
                        }
                    } catch (e) {
                        console.error('解析消息失败:', e);
                    }
                };

                service.ws.onclose = function (event) {
                    console.log('WebSocket连接已关闭, code:', event.code, '原因:', event.reason);
                    // 5秒后尝试重新连接
                    setTimeout(function () {
                        service.connect(username);
                    }, 5000);
                };

                service.ws.onerror = function (error) {
                    console.error('WebSocket错误:', error);
                };
            } catch (e) {
                console.error('创建WebSocket连接失败:', e);
            }
        },

        // 断开连接
        disconnect: function () {
            if (service.ws) {
                console.log('主动断开WebSocket连接');
                service.ws.close();
            }
        },

        // 发送消息
        send: function (to, message) {
            console.log('准备发送消息给:', to, '内容:', message);

            if (!service.ws || service.ws.readyState !== WebSocket.OPEN) {
                console.error('WebSocket未连接');
                return Promise.reject(new Error('WebSocket未连接'));
            }

            try {
                // 先保存消息到数据库
                console.log('保存消息到数据库');
                return Restangular.one('message').post({
                    fromUser: $rootScope.userInfo.username,
                    toUser: to,
                    content: message,
                    status: 'SENT'
                }).then(function (response) {
                    console.log('消息已保存到数据库，ID:', response.id);

                    // 构建WebSocket消息
                    var msg = {
                        id: response.id,
                        to: to,
                        content: message,
                        timestamp: new Date().getTime(),
                        from: $rootScope.userInfo.username
                    };

                    console.log('发送WebSocket消息:', msg);
                    service.ws.send(JSON.stringify(msg));

                    // 添加到本地消息列表
                    var localMsg = {
                        id: response.id,
                        from: 'me',
                        to: to,
                        content: message,
                        timestamp: msg.timestamp,
                        status: 'SENT'
                    };
                    console.log('添加到本地消息列表:', localMsg);
                    service.messages.push(localMsg);

                    if (!$rootScope.$$phase) {
                        $rootScope.$apply();
                    }

                    return true;
                }).catch(function (error) {
                    console.error('保存消息失败:', error);
                    return false; // Indicate failure without stopping promise chain
                });
            } catch (e) {
                console.error('发送消息失败:', e);
                return Promise.reject(e);
            }
        },

        // 标记消息为已读
        markAsRead: function (messageId) {
            console.log('标记消息为已读:', messageId);
            return Restangular.one('message', messageId)
                .post('', { status: 'READ' });
        }
    };

    return service;
});