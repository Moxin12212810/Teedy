angular.module('docs').config(function($stateProvider) {
  $stateProvider
    .state('registration_requests', {
      url: '/registration_requests',
      templateUrl: 'app/docs/view/registration_requests.html',
      controller: 'RegistrationRequests',
      resolve: {
        deps: ['$ocLazyLoad', function($ocLazyLoad) {
          return $ocLazyLoad.load('app/docs/controller/RegistrationRequests');
        }]
      }
    });
});