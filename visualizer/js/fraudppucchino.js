var width = 960,
    height = 500;

var color = d3.scale.category20();

var force = d3.layout.force()
    .charge(-500)
    .linkDistance(5)
    .size([width, height]);

var svg = d3.select("svg#patternVisualizer");

var reports = []
	
var graph = {
  "nodes":[],
  "links":[]
}

$(function() {
	$("#tabs").tabs(); // Initialize Tabs
});

$('.navTab a').click(function(e) {
	$(this).parent().siblings().removeClass('active');
	$(this).parent().addClass('active');
})

function updateVisualization() {
   
	$('div #patternVisualizer').empty();
  
  force
      .nodes(graph.nodes)
      .links(graph.links)
      .start();
	  
  svg.append("svg:defs").append("svg:marker")
	      .attr("id", "transaction")
	      .attr("viewBox", "0 -5 10 10")
	      .attr("refX", 13)
	      .attr("refY", 0)
	      .attr("markerWidth", 6)
	      .attr("markerHeight", 6)
	      .attr("orient", "auto")
	    .append("svg:path")
	      .attr("d", "M0,-5L10,0L0,5");
  

  var link = svg.append("svg:g").selectAll(".link")
      .data(graph.links)
	  .enter().append("svg:path")
	      .attr("class", "link")
	      .attr("marker-end", "url(#transaction)")
    

  var node = svg.append("svg:g").selectAll(".node")
      .data(graph.nodes)
    .enter().append("circle")
      .attr("class", "node")
      .attr("r", 5)
      .style("fill", function(d) { return color(d.group); })
      .call(force.drag);

  node.append("title")
      .text(function(d) { return d.name; });

  force.on("tick", function() {
    link.attr("d", function(d) {
    var dx = d.target.x - d.source.x,
        dy = d.target.y - d.source.y;
    return "M" + d.source.x + "," + d.source.y + "L" + d.target.x + "," + d.target.y;
  });

    node.attr("cx", function(d) { return d.x; })
        .attr("cy", function(d) { return d.y; });
  });
  
}

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
	var reportListEntry = '<li data-component-id="' + index
			+ '" class="list-group-item"><span class="glyphicon glyphicon-remove remove-report"></span>' + date.toLocaleDateString()
			+ '<br/>' + report.members.length + ' Transactions<br/>$'
			+ report.flow + '</li>';
	
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
	
	var transactionLookUp = {}; // Index on transactionId

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
