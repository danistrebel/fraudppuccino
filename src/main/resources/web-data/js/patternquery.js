scc.defaults.PatternQuery = {};

scc.modules.PatternQuery = function() {
  this.requires = ["patternquery"];
  this.onopen = function(e) {
    scc.order({"provider": "patternquery"})
  }
  this.onmessage = function(j) {
    console.log(j);
  }
  this.onclose = function(e) {
    
  }
  this.notready = function(e) {
    
  }
};