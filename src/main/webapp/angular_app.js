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
  function foodMacros(){
    this.calories = {
      name: "calories",
      value: 0,
      concerned: 0
    };
    this.carbs = {
      name: "carbs",
      value: 0,
      concerned: 0
    };
    this.fat = {
      name: "fat",
      value: 0,
      concerned: 0
    };    
    this.protein =  {
      name: "protein",
      value: 0,
      concerned: 0
    };
  }

  /* Contains the basic food macros */
  $scope.desiredMacros = new foodMacros();

  /* List of all user-inputted food items, consisting of Food objects
    Values are keyed by $scope.foodCounter */
  $scope.foods = {};

  /* Basically contains misc data that can't be stored anywhere else */
  $scope.foodsMeta = {
    numKeys: 0,
    numMacros: 4,
  }

  /* Increased when the user inputs a new food item
    This doesn't account for item deletion; it only ensures that each food has a unique key */
  $scope.foodCounter = 1;

  /* Food object definition */
  function Food(id){
    this.id = id;
    this.maximize = 0;
    this.macrosString = ""; 
    this.macros = new foodMacros();
  }

  /* Add a new food item to foods */
  $scope.addFood = function(){
    $scope.foods[$scope.foodCounter] = new Food($scope.foodCounter);
    $scope.foodsMeta.numKeys++;
    $scope.foodCounter++;
  }

  /* Remove a food item from foods */
  $scope.removeFood = function(id){
    delete $scope.foods[id];
    $scope.foodsMeta.numKeys--;
  }

  /* Constructs data object out of user data and sends object to Node.js
  Format:
    macroCount
    numFoods
    foodList
    desiredMacrosList

    A single food is encoded as 0 or 1 for maximization flag, followed by the macros,
    all separated by spaces.
    The elements of desiredMacrosList are also separated by spaces
   */

  /* Send data to Java via Node.js */
  $scope.sendData = function(){
    var foodstr = "";
    var concerns = {};
    var desiredMacrosList = "";
    var macroCount = 0;

    for(var m in $scope.desiredMacros){
      console.log("dms:" + m);
      concerns[$scope.desiredMacros[m].name] = $scope.desiredMacros[m].value;
      desiredMacrosList += $scope.desiredMacros[m].value + " ";
      macroCount++;
    }

    var atLeastOneMax = false;
    for(var f in $scope.foods){
      //make sure at least one food is selected to be maximized
      if($scope.foods[f].maximize==1){
        atLeastOneMax = true;
      }

      foodstr += $scope.foods[f].maximize + " "
      for(var m in concerns){
        console.log("fm: "+ $scope.foods[f].macros[m].name);
        foodstr += $scope.foods[f].macros[m].value + " "
      }
      console.log("|");
      foodstr += "|";
    }

    console.log(foodstr);

    var dataObj = {
      macroCount: macroCount.toString(),
      numFoods: $scope.foodsMeta.numKeys.toString(),
      foodList: foodstr,
      desiredMacrosList: desiredMacrosList
    }

    console.log(dataObj);

    if(macroCount==0 || $scope.foodsMeta.numKeys==0 || atLeastOneMax==false){
      alert("Make sure you have:\n1. Added at least 1 food\n2. Selected to maximize at least 1 food\n3. Selected at least 1 macro");
    } else {
      socket.emit('sendData', dataObj);
    }
  }

  $scope.printData = function(data){
    console.log(data);
  }

  /* Data from backend */
  $scope.backend = {
    currentSolution: 1111
  };

  $scope.updateSolution = function (){
    console.log("updateSolution");

    var hashStr = "";
    for(var prop in $scope.desiredMacros){
      console.log(prop);
      hashStr += $scope.desiredMacros[prop].concerned + "";
    }

    console.log('-'+hashStr+"-");
;
    $scope.backend.currentSolution = hashStr;
  }

  /** Socket events **/
  socket.on('connect', function(){
    console.log("Connected to Node.js!");
  });

  socket.on('javaData', function(data){
    console.log(data);
    $scope.backend = data;
    $scope.backend.currentSolution = 0;

    $scope.updateSolution();
  });

});

/*
  hasSolution
  focused macros
  deadSolution
  (leastSquares)
  (leastSquaresMsg)
  (leastSquaresVectorB)
  simplex
  solution
  msg

*/