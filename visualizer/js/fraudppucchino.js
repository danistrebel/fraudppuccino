$(function() {
	$("#tabs").tabs();
});

$('.navTab a').click(function(e) {
	$(this).parent().siblings().removeClass('active');
	$(this).parent().addClass('active');
})

function updateReports() {
	$("div #reportsList").empty();
	reports.forEach(function(report, index) {
		appendReport(report);
	});
}

function appendReport(report) {
	var index = reports.length;
	reports.push(report);
	var date = new Date(report.start);
	var reportListEntry = '<a href="#" data-component-id="' + index
			+ '" class="list-group-item">' + date.toLocaleDateString()
			+ '<br/>' + report.members.length + ' Transactions<br/>$'
			+ report.flow + '</a>';
	$("div #reportsList").append(reportListEntry);
	$("div #reportsHeader").text("Reports("+ reports.length +")");
}

$(document).on('click', '.list-group-item', function() {
	$(this).siblings().removeClass('active');
	$(this).addClass('active');
	loadComponentMembers($(this).attr("data-component-id"));
});

function loadComponentMembers(id) {

	graph.nodes = [];
	graph.links = [];
	
	var transactionLookUp = {}; //Index on transactionId

	reports[id].members.forEach(function(transaction, index) {
		var tx = {"name" : transaction.id, "group" : 1}
		transactionLookUp[transaction.id] = tx
		graph.nodes.push(tx)
	});
	
	reports[id].members.forEach(function(transaction, index) {		
		transaction.successor.forEach(function(linkTarget) {
			var link = {
				"source" : transactionLookUp[transaction.id],
				"target" : transactionLookUp[linkTarget],
				"value" : 1
			};
			graph.links.push(link);
		});
	});

	updateVisualization();
}

/**
 * Websocket Communication
 */
var wsUri = "ws://localhost:8888/websocket/";
var websocket = new WebSocket(wsUri);

websocket.onmessage = function(msg) {
	graph.nodes = [];
	graph.links = [];
	$('div #patternVisualizer').empty();
	report = JSON.parse(msg.data);
	appendReport(report);
}
