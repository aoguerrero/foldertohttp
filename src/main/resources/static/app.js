'use strict';

var app = angular.module("foldertohttpApp", []);
app.controller("mainController", ["$scope", "$http", function ($scope, $http) {

  $scope.token = "";

  $scope.goList = false;

  var currentPath = [];

  $scope.backend = "";

  /* ******************************************************************************** */

  $scope.loginData = {
    username: "",
    password: ""
  }

  /* ******************************************************************************** */

  $scope.login = function () {
    $http({
      method: "POST",
      url: $scope.backend+"/login",
      data: $scope.loginData
    }).then(function (response) {
      $scope.token = response.data.token;
      $scope.goList = true;
      $scope.list();
    }, function (error) {

    });
  }

  $scope.list = function (path = "") {
    if (path != "")
      currentPath.push(path);
    $http({
      method: "GET",
      url: $scope.backend+"/list" + $scope.printCurrentPath(),
      headers: {
        "token": $scope.token
      }
    }).then(function (response) {
      $scope.dirs = [];
      $scope.files = [];
      var items = response.data;
      items.map(function (item) {
        if (item.type == 'D') {
          $scope.dirs.push({ name: item.name });
        } else {
          $scope.files.push({ name: item.name });
        }
      })

    }, function (error) {

    });
  }

  $scope.back = function () {
    if (currentPath.length > 0) {
      currentPath.pop();
    }
    $scope.list();
  }

  $scope.logout = function () {
    $scope.token = "";
    $scope.goList = false;
    var currentPath = [];
    $scope.loginData = {
      username: "",
      password: ""
    }
  }

  /* ******************************************************************************** */

  $scope.printCurrentPath = function () {
    var result = "";
    if (currentPath) {
      for (var i = 0; i < currentPath.length; i++) {
        result += "/" + currentPath[i];
      }
    }
    return result;
  }

}])

