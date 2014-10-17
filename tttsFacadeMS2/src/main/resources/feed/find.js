function log(msg) {
	$("#log").prepend("<p>"+msg+"</p>")
}
$(document).ready(function() {
	
//	alert("ws://"+window.location.hostname+":6969/feed/ws")
	// hideable alert thanks to Twitter Bootstrap
	$(".alert").alert()
	// open a WebSocket
	var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket
    var mapSocket = new WS("ws://"+window.location.hostname+":6969/feed/ws")
	
	mapSocket.onmessage = function(event) {
		log(event.data)
        var msg = JSON.parse(event.data)
        if (msg.infa != null) $("#log").prepend("<p>"+msg.infa+"</p>")
    }
	// if errors on websocket
	var onalert = function(event) {
        $(".alert").removeClass("hide")
        $("#map").addClass("hide")
        log("websocket connection closed or lost")
    }
	mapSocket.onerror = onalert
	mapSocket.onclose = onalert
})
