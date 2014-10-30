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
    var feedSocket = new WS("ws://"+window.location.hostname+":6969/feed/ws")
	
	feedSocket.onopen = function(event) {
		
		var msg = '{ "id":"ID-1" , "msgType":"FEED" , "payload":"Omg, wtf, WTF ???" }';
//		var msgJson = JSON.parse(msg);
		
		feedSocket.send(msg)
		log(msg)
	}

	
	feedSocket.onmessage = function(event) {
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
