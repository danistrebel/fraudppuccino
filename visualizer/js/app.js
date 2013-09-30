var width = 960,
    height = 500;

var color = d3.scale.category20();

var force = d3.layout.force()
    .charge(-5000)
    .linkDistance(15)
    .size([width, height]);

var svg = d3.select("div#visualizer").append("svg")
	.attr("id", "patternVisualizer")
    .attr("width", width)
    .attr("height", height);

var reports = {
	"components":[
	{
		"start":1380470421000,
		"flow":300,
		"depth":3,
		"members":[{"id":1,"value":300.00,"time":1328530643,"successor":[1]},
		{"id":2,"value":300.00,"time":1328530643,"successor":[2,3]},
		{"id":3,"value":100.00,"time":1328530643,"successor":[]},
		{"id":4,"value":100.00,"time":1328530643,"successor":[]}]	
	},
	{
		"start":1380230421000,
		"flow":8000,
		"depth":4,
		"members":[{"id":1,"value":8000.00,"time":1328530643,"successor":[1]},
		{"id":2,"value":8000.00,"time":1328530643,"successor":[2]},
		{"id":3,"value":8000.00,"time":1328530643,"successor":[3]},
		{"id":4,"value":8000.00,"time":1328530643,"successor":[4]},	
		{"id":5,"value":8000.00,"time":1328530643,"successor":[]}]	
		
	}
	]
}

	
var graph = {
  "nodes":[],
  "links":[]
}

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