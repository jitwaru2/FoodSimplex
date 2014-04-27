var frontendPort = 5005;
var backendPort = 4000;
var app = require('express')();
var server = require('http').createServer(app);
var io = require('socket.io').listen(server);

/* Set level for log messages */
io.set('log level', 1);

/* Listen on port */
server.listen(frontendPort);
console.log("Connected to frontend port: " + frontendPort);
console.log(__dirname);

/* Routing */
app.get('/', function (req, res) {
  res.sendfile(__dirname + '/frontend/index.html');
});

app.get('/angular_app.js', function (req, res) {
	res.sendfile(__dirname + '/frontend/angular_app.js');
})

/* Set up communication with Java backend */
var net = require('net');
var backend = net.connect(backendPort, 'localhost');
console.log("Connected to backend port: " + backendPort);

/* Socket connection with backend */
backend.on('connect', function(){
	console.log("'connect' emitted from Java backend!");
})

var counter = 0;
var frontendSocket;


/*
 * Protocol.noSolution
 * Protocol.noSolutionMsg
 * Protocol.noFoodComboMsg
 * */

/*
* Protocol.solution
* Protocol.leastSquares/noLeastSquares
* (least squares b vector)
* solution vector
* Protocol.simplex/noSimplex
* Protocol.simplexMsg/noSimplexMsg/deadSolutionMsg
* */

/*
 * Protocol.solution
 * Protocol.leastSquares/noLeastSquares
 * (Protocol.leastSquaresMsg)
 * (least squares b vector)
 * solution vector
 * Protocol.simplex/noSimplex
 * Protocol.simplexMsg
 * */

backend.on('data', function(data){
	/* Convert the char array data to a string */ 
	var jstring = charArrtoString(data);
	console.log("Received message " + ++counter+ " from Java:");
	console.log(data);
	console.log(jstring);

	var dataArr = jstring.split("\n");
	var dataObj = {};
	dataObj.solns = {};

	console.log(dataArr);

	var i = 0;
	var end = false;
	while(end==false){
		switch(dataArr[i++]){

			case Protocol.solution:
				var hash = dataArr[i++];
				dataObj.solns[hash] = {};
				dataObj.solns[hash].hasSolution = true;
				dataObj.solns[hash].deadSolution = false;
				if(dataArr[i++]==Protocol.leastSquares){
					dataObj.solns[hash].leastSquares = true;
					dataObj.solns[hash].leastSquaresMsg = dataArr[i++];
					dataObj.solns[hash].leastSquaresVectorB = dataArr[i++];
				} else {
					dataObj.solns[hash].leastSquares = false;
					dataObj.solns[hash].leastSquaresMsg = null;
					dataObj.solns[hash].leastSquaresVectorB = null;
				}
				dataObj.solns[hash].solution = dataArr[i++];
				if(dataArr[i++]==Protocol.simplex){
					dataObj.solns[hash].simplex = true;
				} else {
					dataObj.solns[hash].simplex = false;
				}
				dataObj.solns[hash].msg = dataArr[i++];
				break;
			case Protocol.noSolution:
				var hash = dataArr[i++];
				dataObj.solns[hash] = {};
				dataObj.solns[hash].hasSolution = false;
				dataObj.solns[hash].deadSolution = false;
				dataObj.solns[hash].leastSquares = false;
				dataObj.solns[hash].leastSquaresMsg = null;
				dataObj.solns[hash].leastSquaresVectorB = null;
				dataObj.solns[hash].solution = dataArr[i++];
				dataObj.solns[hash].msg = dataArr[i++];
				break;
			case Protocol.deadSolution:
				var hash = dataArr[i++];
				dataObj.solns[hash] = {};
				dataObj.solns[hash].hasSolution = true;
				dataObj.solns[hash].deadSolution = true;
				if(dataArr[i++]==Protocol.leastSquares){
					dataObj.solns[hash].leastSquares = true;
					dataObj.solns[hash].leastSquaresMsg = dataArr[i++];
					dataObj.solns[hash].leastSquaresVectorB = dataArr[i++];
				} else {
					dataObj.solns[hash].leastSquares = false;
					dataObj.solns[hash].leastSquaresVectorB = null;
				}
				dataObj.solns[hash].solution = dataArr[i++];
				dataObj.solns[hash].msg = dataArr[i++];
				break;
			case Protocol.endTransmission:
				end = true;
		}
	}


	/* Send data to AngularJS */
	frontendSocket.emit('javaData', dataObj);

})

var Protocol = {
	beginSystem: "111",
	endSystem: "112",
	endTransmission: "113",
	endMsg: "99",
	solution: "222",
	noSolution: "223",
	deadSolution: "224",
	leastSquares: "230",
	noLeastSquares: "231",
	simplex: "311",
	noSimplex: "312",
	endLine: "\n"
}

/* Socket.io connection with frontend */
io.sockets.on('connection', function (socket) {
	frontendSocket = socket;

	console.log("angularjs connected!");

	var datacounter = 0;
	socket.on('sendData', function(dataObj){
		console.log("sendData event received");
		datacounter++;

		/* Write object to Java */
		backend.writeObj(dataObj);
		backend.write(Protocol.endMsg+'\n');

	})
})

backend.writeObj = function(obj){
	backend.write(Protocol.beginSystem + '\n');

	var counter = 0;
	for(var prop in obj) {
		backend.write(obj[prop]);
		backend.write(Protocol.endLine);
	}

	backend.write(Protocol.endSystem+'\n');
}

backend.writeProtocol = function(pr){
	backend.write(pr);
}

backend.writeMsg = function(pr, msg){
	backend.write(pr);
	backend.write(msg);
	backend.write(Protocol.endLine);	
}

function charArrtoString(chars){
	var data = "";
	//the last character is newline, so skip that
	for(var i=0; i<chars.length-1; i++){
		data += String.fromCharCode(chars[i]);
		// console.log("converting char: " + chars[i]);
	}
	return data;
}


/* Threading/child processes */
//var spawn = require('child_process').spawn;
//var deploySh = spawn('sh', [ 'first_script.sh' ]);

