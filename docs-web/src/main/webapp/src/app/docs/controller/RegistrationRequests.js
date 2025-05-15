'use strict';

/**
 * Registration requests controller.
 */
angular.module('docs').controller('RegistrationRequests', function ($scope, $translate, $uibModal, Restangular) {
  // Load registration requests
  $scope.loadRequests = function () {
    Restangular.one('user/registration_requests').get().then(function (data) {
      $scope.requests = data.requests;
    });
  };

  // Process a request
  $scope.processRequest = function (request, action) {
    var modalInstance = $uibModal.open({
      template: '<div class="modal-header">' +
        '<h3 class="modal-title">{{ title | translate }}</h3>' +
        '</div>' +
        '<div class="modal-body">' +
        '<p>{{ message | translate }}</p>' +
        '</div>' +
        '<div class="modal-footer">' +
        '<button class="btn btn-default" ng-click="cancel()">{{ "cancel" | translate }}</button>' +
        '<button class="btn btn-primary" ng-click="ok()">{{ "ok" | translate }}</button>' +
        '</div>',
      controller: function ($scope, $uibModalInstance, title, message) {
        $scope.title = title;
        $scope.message = message;
        $scope.ok = function () {
          $uibModalInstance.close();
        };
        $scope.cancel = function () {
          $uibModalInstance.dismiss('cancel');
        };
      },
      resolve: {
        title: function () {
          return 'registration_requests.confirm_title';
        },
        message: function () {
          return 'registration_requests.confirm_message';
        }
      }
    });

    modalInstance.result.then(function () {
      Restangular.one('user/registration_requests/' + request.id).post('', {
        action: action,
        comment: $scope.comment
      }).then(function () {
        $scope.loadRequests();
        $scope.comment = '';
      });
    });
  };

  // Load requests on page load
  $scope.loadRequests();
});