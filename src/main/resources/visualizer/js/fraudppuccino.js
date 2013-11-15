var width = 960, height = 500;

var color = d3.scale.linear().domain([ 0, 5, 10, 20 ]).range(
		[ "green", "yellow", "orange", "red" ]);

var transactionValue = d3.scale.log().domain([ 1, 1000 ]).range([ 0, 20 ]);

var force = d3.layout.force().charge(-500).linkDistance(50).size(
		[ width, height ]);

var svg = d3.select("svg#patternVisualizer");
svg.attr("pointer-events", "all").call(
		d3.behavior.zoom().on("zoom", transformation));

var graphVisualization = svg.append('svg:g').attr("id", "graphVisualization");

var reports = {}
var reportsCounter = 0 // supplier of unique report IDs

var graph = {
	"nodes" : [],
	"links" : []
}

var editorDSL = CodeMirror.fromTextArea($("textarea#query")[0], {
	mode : "text/x-yaml"
});

$(function() {
	$("#tabs").tabs(); // Initialize Tabs
});

$('.navTab a').click(function(e) {
	$(this).parent().siblings().removeClass('active');
	$(this).parent().addClass('active');
})

function transformation() {
	graphVisualization.attr("transform", "translate(" + d3.event.translate
			+ ")" + " scale(" + d3.event.scale + ")");
}

function updateVisualization() {

	$("g#visualizerPlaceholder").remove();
	$("g#graphVisualization").empty();

	force.nodes(graph.nodes).links(graph.links).start();

	graphVisualization.append("svg:defs").append("svg:marker").attr("id",
			"transaction").attr("viewBox", "0 -5 10 10").attr("refX", 13).attr(
			"refY", 0).attr("markerWidth", 6).attr("markerHeight", 6).attr(
			"orient", "auto").append("svg:path").attr("d", "M0,-5L10,0L0,5");

	var link = graphVisualization.append("svg:g").selectAll(".link").data(
			graph.links).enter().append("svg:path").attr("class", "link").attr(
			"marker-end", "url(#transaction)")

	var node = graphVisualization.append("svg:g").selectAll(".node").data(
			graph.nodes).enter().append("circle").attr("class", "node").attr(
			"r", function(d) {
				return Math.min(20, transactionValue(d.value));
			}).style("fill", function(d) {
		return color(d.group);
	}).call(force.drag).on("click", function(node) {
		showDetailsForNode(node);
	});

	force.on("tick", function() {
		link.attr("d", function(d) {
			var dx = d.target.x - d.source.x, dy = d.target.y - d.source.y;
			return "M" + d.source.x + "," + d.source.y + "L" + d.target.x + ","
					+ d.target.y;
		});

		node.attr("cx", function(d) {
			return d.x;
		}).attr("cy", function(d) {
			return d.y;
		});
	});

}

function showDetailsForNode(node) {
	console.log(node);

	$('#inspectorNavTab :first-child').click(); // open the inspector tab

	if (node.account) {
		var details = '<h1>Account #' + node.name + '</h1>'
				+ '<table class="table table-striped">'
				+ '<tr><td># Transactions in</td><td>'
				+ node.account["in-count"] + '</tr>'
				+ '<tr><td># Transactions out</td><td>'
				+ node.account["out-count"] + '</td></tr>'
				+ '<tr><td>BTC Transactions in</td><td>' + node.account["in"]
				/ 100000000 + ' BTC</td></tr>'
				+ '<tr><td>BTC Transactions out</td><td>' + node.account["out"]
				/ 100000000 + ' BTC</td></tr>' + '</table>'
		$('#inspector-content').empty().append(details);
	}

	else if (node.transaction) {
		var transactionDate = new Date(node.transaction.time * 1000);
		var details = '<h1>Transaction #' + Math.abs(node.name) + '</h1>'
				+ '<table class="table table-striped">'
				+ '<tr><td>BTC Transaction Value</td><td>'
				+ node.transaction.value / 100000000 + ' BTC</td></tr>'
				+ '<tr><td>Time</td><td>'
				+ transactionDate.toLocaleDateString() + ' '
				+ transactionDate.toLocaleTimeString() + '</td></tr>'
				+ '<tr><td>Source</td><td>' + node.transaction.src
				+ '</td></tr>' + '<tr><td>Target</td><td>'
				+ node.transaction.target + '</td></tr>'
				+ '<tr><td>Cross Coutry</td><td>' + node.transaction.xCountry
				+ '</td></tr>' + '</table>'
		$('#inspector-content').empty().append(details);
	}
}

function appendReport(report) {
	reports[reportsCounter] = report
	var startDate = new Date(report.start);
	var endDate = new Date(report.end);
	var reportListEntry = '<li data-component-id="'
			+ reportsCounter
			+ '" class="list-group-item"><span class="glyphicon glyphicon-remove remove-report"></span>'
			+ startDate.toLocaleDateString()
			+ '-'
			+ endDate.toLocaleDateString()
			+ '<br/>'
			+ 'BTC '
			+ Math.round(report.flow / 100000000 * 10000)
			/ 10000
			+ ', '
			+ report.members.length
			+ ' transactions<br/>'
			+ '<button type="button" class="btn btn-default btn-xs showAccountGraph">Account Graph</button> <button type="button" class="btn btn-default btn-xs showTransactionGraph">Transaction Graph</button></li>';
	reportsCounter++;
	$("div #reportsList").append(reportListEntry);
	updateReportsCount();
}

$(document).on('click', '.showAccountGraph', function() {
	var parent = $(this).parent();
	parent.siblings().removeClass('active');
	parent.addClass('active');
	loadAccountGraph(parent.attr("data-component-id"));
});

$(document).on('click', '.showTransactionGraph', function() {
	var parent = $(this).parent();
	parent.siblings().removeClass('active');
	parent.addClass('active');
	loadTransactionGraph(parent.attr("data-component-id"));
});

$(document).on('click', '.remove-report', function() {
	var idOfReport = $(this).parent().attr('data-component-id');
	$(this).parent().fadeOut(300, function() {
		$(this).remove();
	});
	delete reports[idOfReport];
	updateReportsCount();
});

$(document).on('click', '#runAlgorithm', function() {
	websocket.send(editorDSL.getValue());
});

$(document).on(
		'click',
		'#exportReports',
		function() {
			window.open("data:application/json;charset=utf-8,"
					+ escape(JSON.stringify(reports)));
		});

$(document).on('click', '#updateSettings', function() {
	var newWebSocketURI = $('#wsURI').val();
	initializeWebSocket(newWebSocketURI);
});

$(document).on('click', '#importReports', function() {
	var file = $('input#fileInput').get(0).files[0];
	var textType = /json/;

	if (file.type.match(textType)) {
		var reader = new FileReader();

		reader.onload = function(e) {
			var parsedResults = jQuery.parseJSON(reader.result);
			console.log(parsedResults);
			$.each(parsedResults, function(id, report) {
				appendReport(report);
			});
		}

		reader.readAsText(file);
	} else {
		console.log(file.type)
	}
});

function updateReportsCount() {
	$("div#reportsTitle span").text(
			"Reports(" + Object.keys(reports).length + ")");
}

function displayStatus(message) {
	$("div#notifications").empty();
	$("div#notifications").append(
			'<div id="computationStatus" class="alert alert-info">'
					+ message.msg + '</div>');
	setTimeout(function() {
		$('div#computationStatus').fadeOut('fast');
	}, 1500);
}

function updateProgess(progress) {
	$("div#computationProgress").width(progress + "%");
}

function loadTransactionGraph(id) {

	graph.nodes = [];
	graph.links = [];

	var transactionLookUp = {}; // Index on transactionId

	reports[id].members.forEach(function(transaction, index) {
		var tx = {
			"name" : transaction.id,
			"group" : transaction.depth,
			"transaction" : transaction,
			"value" : 1 + (transaction.value / 100000000),
		};
		transactionLookUp[transaction.id] = tx;
		graph.nodes.push(tx);
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

function loadAccountGraph(id) {

	graph.nodes = [];
	graph.links = [];

	var accountsLookup = {}; // Index on accountId

	reports[id].members
			.forEach(function(transaction, index) {

				if (!accountsLookup[transaction.src]) {
					var source = {
						"name" : transaction.src,
						"group" : 2,
						"account" : {
							"in" : 0,
							"out" : 0,
							"in-count" : 0,
							"out-count" : 0
						},
						"value" : 10
					};
					accountsLookup[transaction.src] = source;
					graph.nodes.push(source);
				}
				accountsLookup[transaction.src].account["out"] = accountsLookup[transaction.src].account["out"]
						+ transaction.value;
				accountsLookup[transaction.src].account["out-count"]++;

				if (!accountsLookup[transaction.target]) {
					var target = {
						"name" : transaction.target,
						"group" : 2,
						"account" : {
							"in" : 0,
							"out" : 0,
							"in-count" : 0,
							"out-count" : 0
						},
						"value" : 10
					};
					accountsLookup[transaction.target] = target;
					graph.nodes.push(target);
				}
				accountsLookup[transaction.target].account["in"] = accountsLookup[transaction.target].account["in"]
						+ transaction.value;
				accountsLookup[transaction.target].account["in-count"]++;

				var link = {
					"source" : accountsLookup[transaction.src],
					"target" : accountsLookup[transaction.target],
					"value" : 1
				};
				graph.links.push(link);
			});

	updateVisualization();
}

/**
 * Websocket Communication
 */
var wsUri = "ws://localhost:8888/websocket/";

var websocket = null

function initializeWebSocket(wsUri) {
	websocket = new WebSocket(wsUri);

	websocket.onopen = function(e) {
		websocket.send("getpreviousresults");
	}

	websocket.onmessage = function(msg) {
		message = JSON.parse(msg.data);
		if (message instanceof Array) {
			$.each(message, function(i, m) {
				processMessage(m);
			});
		} else {
			processMessage(message)
		}
	}

}

function processMessage(message) {
	if (message.hasOwnProperty("id") && message.hasOwnProperty("members")) {
		appendReport(message);
	} else if (message.hasOwnProperty("status")) {
		if (message.status == "progress") {
			updateProgess(message.msg);
		} else {
			displayStatus(message);
		}

	} else {
		console.log("received: " + message)
	}
}

initializeWebSocket(wsUri);
