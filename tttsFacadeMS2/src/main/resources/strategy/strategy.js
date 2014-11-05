function log(msg) {
//	$("#log").prepend("<p>"+msg+"</p>")
	$("#log").append("<p>"+msg+"</p>")
	var objDiv = document.getElementById("log");
	objDiv.scrollTop = objDiv.scrollHeight;
}
$(document).ready(function() {
	
	// hideable alert thanks to Twitter Bootstrap
	$(".alert").alert()
	// open a WebSocket
	var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket
    var strategySocket = new WS("ws://"+window.location.hostname+":6969/strategy/ws")
	
	strategySocket.onopen = function(event) {
		
		var msg = '{ "msgType":"STRATEGY_REQ" , "payload":"" }';
//		var msgJson = JSON.parse(msg);
		
		strategySocket.send(msg)
		log(msg)
	}

	
	strategySocket.onmessage = function(event) {
		log(event.data)
    }
	// if errors on websocket
	var onalert = function(event) {
        $(".alert").removeClass("hide")
        $("#map").addClass("hide")
        log("websocket connection closed or lost")
    }
	feedSocket.onerror = onalert
	feedSocket.onclose = onalert
})
