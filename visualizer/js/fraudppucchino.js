$(function() {
    $( "#tabs" ).tabs();
});

$('.navTab a').click(function (e) {
  $(this).parent().siblings().removeClass('active');
  $(this).parent().addClass('active');
})

function updateReports() {
	$("div #reportsList").empty();
	reports.forEach(function(report, index) {
		var date = new Date(report.start);
		var reportListEntry = '<a href="#" data-component-id="'+index+'" class="list-group-item">' + date.toLocaleDateString() +'<br/>' + report.members.length +' Transactions<br/>$'+report.flow+'</a>';
		$("div #reportsList").append(reportListEntry);
	});
}

$(document).on('click','.list-group-item',function(){
    $(this).siblings().removeClass('active');
    $(this).addClass('active');
	loadComponentMembers($(this).attr("data-component-id"));
});

function loadComponentMembers(id) {
	
	graph.nodes =[];
	graph.links = [];
	
	reports[id].members.forEach(function(transaction, index){
		var tx = {"name": transaction.id, "group":1 };
		transaction.successor.forEach(function(linkTarget){
			var link = {"source":index,"target":linkTarget,"value":1};
			graph.links.push(link);
		}); 
		graph.nodes.push(tx);
	});
	
    updateVisualization();
}

/**
* Websocket Communication
*/
var wsUri = "ws://localhost:8888/websocket/"; 
var websocket = new WebSocket(wsUri);

	 
 websocket.onmessage = function(msg) {
	 console.log(msg);
	 if(msg.data=="updateReports") { 
		reports=[]
		graph.nodes =[];
		graph.links = [];
		$('div #patternVisualizer').empty();
		d3.json("http://localhost:8888/components.json", function(error, json) {
			reports = json.components; 
			updateReports();
		});
	 }
 }




