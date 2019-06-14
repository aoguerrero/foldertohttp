'use strict';

var app = angular.module("foldertohttpApp", []);
app.controller("mainController", ["$scope", "$http", function ($scope, $http) {

  $scope.goList = false;

  $scope.loginData = {
    username: "",
    password: ""
  }

  $scope.token = "";

  $scope.login = function () {
    $http({
      method: "POST",
      url: "http://localhost:8080/login",
      data: $scope.loginData
    }).then(function (response) {
      console.log(response);
    }, function (error) {
      console.log(error);
    });
  }

  $scope.list = function (path, token) {
  }

  $scope.download = function (path) {
  }

  $scope.logout = function () {
  }

}])

