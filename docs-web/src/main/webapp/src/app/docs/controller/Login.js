'use strict';

/**
 * Login controller.
 */
angular.module('docs').controller('Login', function(Restangular, $scope, $rootScope, $state, $stateParams, $dialog, User, $translate, $uibModal) {
  $scope.codeRequired = false;
  $scope.showRegistrationForm = false;
  $scope.registration = {
    username: '',
    email: '',
    password: ''
  };

  // Get the app configuration
  Restangular.one('app').get().then(function(data) {
    $rootScope.app = data;
  });

  // Login as guest
  $scope.loginAsGuest = function() {
    $scope.user = {
      username: 'guest',
      password: ''
    };
    $scope.login();
  };
  
  // Login
  $scope.login = function() {
    User.login($scope.user).then(function() {
      User.userInfo(true).then(function(data) {
        $rootScope.userInfo = data;
      });

      if($stateParams.redirectState !== undefined && $stateParams.redirectParams !== undefined) {
        $state.go($stateParams.redirectState, JSON.parse($stateParams.redirectParams))
          .catch(function() {
            $state.go('document.default');
          });
      } else {
        $state.go('document.default');
      }
    }, function(data) {
      if (data.data.type === 'ValidationCodeRequired') {
        // A TOTP validation code is required to login
        $scope.codeRequired = true;
      } else {
        // Login truly failed
        var title = $translate.instant('login.login_failed_title');
        var msg = $translate.instant('login.login_failed_message');
        var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
        $dialog.messageBox(title, msg, btns);
      }
    });
  };

  // Submit registration request
  $scope.submitRegistrationRequest = function() {
    Restangular.one('user').customPOST($.param($scope.registration), 'register', {}, {'Content-Type': 'application/x-www-form-urlencoded'})
      .then(function() {
        var title = $translate.instant('login.registration_request_sent_title');
        var msg = $translate.instant('login.registration_request_sent_message');
        var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
        $dialog.messageBox(title, msg, btns).then(function() {
          $scope.showRegistrationForm = false;
          $scope.registration = {
            username: '',
            email: '',
            password: ''
          };
        });
      }, function(data) {
        var title = $translate.instant('login.registration_request_error_title');
        var msg = $translate.instant('login.registration_request_error_message');
        if (data.data.type === 'AlreadyExistingUsername') {
          msg = $translate.instant('login.registration_request_error_username_exists');
        } else if (data.data.type === 'AlreadyExistingEmail') {
          msg = $translate.instant('login.registration_request_error_email_exists');
        } else if (data.data.type === 'AlreadyExistingRequest') {
          msg = $translate.instant('login.registration_request_error_request_exists');
        }
        var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
        $dialog.messageBox(title, msg, btns);
      });
  };

  // Password lost
  $scope.openPasswordLost = function () {
    $uibModal.open({
      templateUrl: 'partial/docs/passwordlost.html',
      controller: 'ModalPasswordLost'
    }).result.then(function (username) {
      if (username === null) {
        return;
      }

      // Send a password lost email
      Restangular.one('user').post('password_lost', {
        username: username
      }).then(function () {
        var title = $translate.instant('login.password_lost_sent_title');
        var msg = $translate.instant('login.password_lost_sent_message', { username: username });
        var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
        $dialog.messageBox(title, msg, btns);
      }, function () {
        var title = $translate.instant('login.password_lost_error_title');
        var msg = $translate.instant('login.password_lost_error_message');
        var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
        $dialog.messageBox(title, msg, btns);
      });
    });
  };
});