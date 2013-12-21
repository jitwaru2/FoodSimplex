var frontendPort = 5004;
var backendPort = 8007;
var app = require('express')();
var server = require('http').createServer(app);
var io = require('socket.io').listen(server);

/* Set level for log messages */
io.set('log level', 1);

/* Listen on port */
server.listen(frontendPort);
console.log("Connected to frontend port: " + frontendPort);

/* Routing */
app.get('/', function (req, res) {
  res.sendfile(__dirname + '/index.html');
});

app.get('/angular_app.js', function (req, res) {
	res.sendfile(__dirname + '/angular_app.js');
})

/* Set up communication with Java backend */
var net = require('net');
var backend = net.connect(backendPort, 'localhost');
console.log("Connected to backend port: " + backendPort);

/* Socket connection with backend */
backend.on('connect', function(){
	console.log("Connected with Java backend");
})

var counter = 0;
var frontendSocket;

backend.on('data', function(data){
	/* Convert the char array data to a string */ 
	var jstring = charArrtoString(data);
	console.log("Received message " + ++counter+ " from Java:");
	console.log(data);
	console.log(jstring);

	/* Send data to AngularJS */
	frontendSocket.emit('javaData', jstring);

})

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
		backend.write(Protocol.endMsg);

	})
})

var Protocol = {
	beginSystem: "111\n",
	endSystem: "112\n",
	endMsg: "99\n",
	endLine: "\n"
}

backend.writeObj = function(obj){
	backend.write(Protocol.beginSystem);

	var counter = 0;
	for(var prop in obj) {
		backend.write(obj[prop]);
		backend.write(Protocol.endLine);
	}

	backend.write(Protocol.endSystem);
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

