'use strict';

/**
 * Registration requests controller.
 */
angular.module('docs').controller('RegistrationRequests', function($scope, $translate, $dialog, Restangular) {
  // Load registration requests
  $scope.loadRequests = function() {
    Restangular.one('user/registration_requests').get().then(function(data) {
      $scope.requests = data.requests;
    });
  };
  
  // Process a request
  $scope.processRequest = function(request, action) {
    var title = $translate.instant('registration_requests.confirm_title');
    var msg = $translate.instant('registration_requests.confirm_message');
    var btns = [
      {result: 'cancel', label: $translate.instant('cancel')},
      {result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}
    ];
    
    $dialog.messageBox(title, msg, btns).then(function(result) {
      if (result === 'ok') {
        Restangular.one('user/registration_requests/' + request.id).post('', {
          action: action,
          comment: $scope.comment
        }).then(function() {
          $scope.loadRequests();
          $scope.comment = '';
        });
      }
    });
  };
  
  // Load requests on page load
  $scope.loadRequests();
});