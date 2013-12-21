var app = angular.module('app',[]);

/* AngularJS wrapper for Socket.io */
app.factory('socket', function ($rootScope) {
  var socket = io.connect();
  return {
    on: function (eventName, callback) {
      socket.on(eventName, function () {  
        var args = arguments;
        $rootScope.$apply(function () {
          callback.apply(socket, args);
        });
      });
    },
    emit: function (eventName, data, callback) {
      socket.emit(eventName, data, function () {
        var args = arguments;
        $rootScope.$apply(function () {
          if (callback) {
            callback.apply(socket, args);
          }
        });
      })
    }
  };
});

/* Main controller */
app.controller('mainCtrl', function($scope, socket){
  $scope.foods = {
    macroCount: 5,
    numFoods: 0,
    foodList: [],
    desiredMacrosList: [] 
  }

  $scope.results = [];

  $scope.foodMeta = {
    number: 0,
    macrosString: "",
    desiredMacrosString: ""
  }

	socket.on('connect', function(){
		console.log("connected!");
	});

  socket.on('javaData', function(data){
    console.log("Got data from java:");
    console.log(data);
    $scope.results = data.split(" ");
  });

  /* Send JSON object to Node.js */
  $scope.sendData = function(num, ms, dms){
    $scope.convertStringMacros(num, ms, dms);

    var dataObj = {
      macroCount: $scope.foods.macroCount.toString(),
      numFoods: $scope.foods.numFoods.toString(),
      foodList: $scope.foods.foodList.join("|"),
      desiredMacrosList: $scope.foods.desiredMacrosList.join("|")
    }

    socket.emit('sendData', dataObj);
  }

  $scope.sendDummyData = function(){
    socket.emit('sendData', {data: " hello there!"});
  }

  $scope.convertStringMacros = function(num, ms, dms){
    var arrs = ms.split(" ");
    var count = 0;
    $scope.foods.numFoods = parseInt(num, 10);

    //initialize arrays
    for(var i=0; i<$scope.foods.numFoods; i++){
      $scope.foods.foodList[i] = [];
    }

    //take foods horizontally
    var msList = ms.split("\n");
    for(var i=0; i<$scope.foods.numFoods; i++){
      var ml = msList[i].split(" ");
      for(var j=0; j<$scope.foods.macroCount; j++){
        $scope.foods.foodList[i][j] = parseInt(ml[j], 10);
      }
    }

    //take desired macro list
    var dml = dms.split(" ");
    $scope.foods.desi
    for(var i=0; i<$scope.foods.macroCount; i++){
      $scope.foods.desiredMacrosList[i] = parseInt(dml[i], 10);
    }

    console.log($scope.foods.foodList);
    console.log($scope.foods.desiredMacrosList);
  }

  $scope.objToArr = function(obj){
    /* Iterate through the object properties and send them individually to Java */
    var jObjs = [];
    counter = 0;
    for(var prop in obj){
      jObjs[counter] = obj[prop];
      console.log(jObjs[counter]);
      counter++;
    }
  }

  $scope.printData = function(data){
    console.log(data);
  }
});