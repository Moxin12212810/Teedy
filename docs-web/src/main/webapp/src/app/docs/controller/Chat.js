'use strict';

/**
 * Chat controller.
 */
angular.module('docs').controller('Chat', function ($scope, $timeout, Chat, Restangular) {
    // 使用对象来存储消息，以便更好的双向绑定
    $scope.chatData = {
        messages: Chat.messages
    };
    $scope.currentChat = null;
    $scope.message = '';
    $scope.currentUser = null;
    $scope.users = [];

    // 监听消息数组的变化
    $scope.$watch('chatData.messages', function (newVal, oldVal) {
        if (newVal !== oldVal) {
            console.log('消息数组已更新:', newVal);
            // 强制更新视图
            if (!$scope.$$phase) {
                $scope.$apply();
            }
        }
    }, true);

    // 获取当前用户信息
    Restangular.one('user').get().then(function (data) {
        $scope.currentUser = data.username;
        console.log('当前用户:', $scope.currentUser);
        // 获取到用户名后再建立WebSocket连接
        Chat.connect($scope.currentUser);
        // 加载同组用户列表
        loadGroupUsers();
    });

    // 获取同组用户列表
    function loadGroupUsers() {
        Restangular.one('user/list').get().then(function (data) {
            // 过滤掉当前用户
            $scope.users = data.users.filter(function (user) {
                return user.username !== $scope.currentUser;
            });
            console.log('在线用户列表:', $scope.users);
        });
    }

    // 选择聊天对象
    $scope.selectChat = function (user) {
        console.log('选择聊天对象:', user);
        $scope.currentChat = user;
        Chat.currentChat = user;

        // 加载与该用户的历史消息
        Chat.loadMessages(user.username).then(function () {
            console.log('加载历史消息:', Chat.messages);
            // 更新本地消息数组
            $scope.chatData.messages = Chat.messages;
            // 标记所有收到的消息为已读
            $scope.chatData.messages.forEach(function (msg) {
                if (msg.from === user.username && msg.status === 'RECEIVED') {
                    Chat.markAsRead(msg.id);
                    msg.status = 'READ';
                }
            });
            // 强制更新视图
            if (!$scope.$$phase) {
                $scope.$apply();
            }
        });
    };

    // 发送消息
    $scope.sendMessage = function () {
        console.log('尝试发送消息:', $scope.message, '到用户:', $scope.currentChat);

        if (!$scope.message || !$scope.currentChat) {
            console.log('消息为空或未选择聊天对象');
            return;
        }

        var messageText = $scope.message;
        $scope.message = ''; // 清空输入框

        // 检查WebSocket连接状态
        if (!Chat.ws || Chat.ws.readyState !== WebSocket.OPEN) {
            console.error('WebSocket未连接，重新连接...');
            Chat.connect($scope.currentUser);
            $scope.message = messageText; // 恢复消息文本
            return;
        }

        console.log('开始发送消息...');
        Chat.send($scope.currentChat.username, messageText)
            .then(function (success) {
                console.log('消息发送结果:', success);
                if (!success) {
                    console.error('发送失败，恢复消息文本');
                    $scope.message = messageText;
                } else {
                    // 确保消息显示在界面上
                    $timeout(function () {
                        if (!$scope.$$phase) {
                            $scope.$apply();
                        }
                    });
                }
            })
            .catch(function (error) {
                console.error('发送消息时出错:', error);
                $scope.message = messageText;
            });
    };

    // 获取消息状态的显示文本
    $scope.getMessageStatus = function (message) {
        if (message.from === 'me') {
            switch (message.status) {
                case 'SENT': return '已发送';
                case 'RECEIVED': return '已送达';
                case 'READ': return '已读';
                default: return '';
            }
        }
        return '';
    };

    // 在控制器销毁时清理
    $scope.$on('$destroy', function () {
        Chat.disconnect();
    });
});