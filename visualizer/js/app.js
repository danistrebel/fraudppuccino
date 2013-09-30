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
  

  var link = svg.selectAll(".link")
      .data(graph.links)
    .enter().append("line")
      .attr("class", "link")
      .style("stroke-width", function(d) { return Math.sqrt(d.value); });

  var node = svg.selectAll(".node")
      .data(graph.nodes)
    .enter().append("circle")
      .attr("class", "node")
      .attr("r", 5)
      .style("fill", function(d) { return color(d.group); })
      .call(force.drag);

  node.append("title")
      .text(function(d) { return d.name; });

  force.on("tick", function() {
    link.attr("x1", function(d) { return d.source.x; })
        .attr("y1", function(d) { return d.source.y; })
        .attr("x2", function(d) { return d.target.x; })
        .attr("y2", function(d) { return d.target.y; });

    node.attr("cx", function(d) { return d.x; })
        .attr("cy", function(d) { return d.y; });
  });
  
}