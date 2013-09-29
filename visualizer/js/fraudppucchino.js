$(function() {
    $( "#tabs" ).tabs();
	updateReports();
});

$('.navTab a').click(function (e) {
  $(this).parent().siblings().removeClass('active');
  $(this).parent().addClass('active');
})

function updateReports() {
	reports.components.forEach(function(report, index) {
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
	console.log(reports.components[id]);
	
	graph.nodes =[];
	graph.links = [];
	
	reports.components[id].forEach(function(transaction, index){
		var tx = {"name": transaction.id, "group":1 };
		transaction.successor.forEach(function(linkTarget){
			var link = {"source":index,"target":linkTarget,"value":1};
			graph.links.push(link);
		}); 
		graph.nodes.push(tx);
	});
	
	//set graph
    force
        .nodes(n)
        .links(links)
        .start();
	
}

