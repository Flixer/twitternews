var cy;
var graphData = [];
var numberOfGraphResLoaded = 0;

//$('#date').datepicker({ });

var graphResDataParser = function(data) {
	var loadedData = jQuery.parseJSON("[" + data.substr(0, data.length - 2)
			+ "]");
	graphData = $.merge(graphData, loadedData);
	numberOfGraphResLoaded++;
	if (numberOfGraphResLoaded == 2) {
		initCY();
	}
};
$.get("resources/edges.txt", graphResDataParser, "text");
$.get("resources/nodes.txt", graphResDataParser, "text");


var initCY = (function() { // on dom ready

	cy = cytoscape({
		container : document.getElementById('cy'),

		style : [ {
			"selector" : "core",
			"style" : {
				"selection-box-color" : "#AAD8FF",
				"selection-box-border-color" : "#8BB0D0",
				"selection-box-opacity" : "0.5"
			}
		}, {
			"selector" : "node",
			"style" : {
				"width" : "mapData(score, 0, 0.006769776522008331, 20, 60)",
				"height" : "mapData(score, 0, 0.006769776522008331, 20, 60)",
				"content" : "data(name)",
				"font-size" : "12px",
				"text-valign" : "center",
				"text-halign" : "center",
				"background-color" : "#555",
				"text-outline-color" : "#555",
				"text-outline-width" : "2px",
				"color" : "#fff",
				"overlay-padding" : "6px",
				"z-index" : "10"
			}
		}, {
			"selector" : "node[?attr]",
			"style" : {
				"shape" : "rectangle",
				"background-color" : "#aaa",
				"text-outline-color" : "#aaa",
				"width" : "16px",
				"height" : "16px",
				"font-size" : "6px",
				"z-index" : "1"
			}
		}, {
			"selector" : "node[?query]",
			"style" : {
				"background-clip" : "none",
				"background-fit" : "contain"
			}
		}, {
			"selector" : "node:selected",
			"style" : {
				"border-width" : "6px",
				"border-color" : "#AAD8FF",
				"border-opacity" : "0.5",
				"background-color" : "#77828C",
				"text-outline-color" : "#77828C"
			}
		}, {
			"selector" : "edge",
			"style" : {
				"curve-style" : "haystack",
				"haystack-radius" : "0.5",
				"opacity" : "0.4",
				"line-color" : "#bbb",
				"width" : "mapData(weight, 0, 1, 1, 8)",
				"overlay-padding" : "3px"
			}
		}, {
			"selector" : "node.unhighlighted",
			"style" : {
				"opacity" : "0.2"
			}
		}, {
			"selector" : "edge.unhighlighted",
			"style" : {
				"opacity" : "0.05"
			}
		}, {
			"selector" : ".highlighted",
			"style" : {
				"z-index" : "999999"
			}
		}, {
			"selector" : "node.highlighted",
			"style" : {
				"border-width" : "6px",
				"border-color" : "#AAD8FF",
				"border-opacity" : "0.5",
				"background-color" : "#394855",
				"text-outline-color" : "#394855",
				"shadow-blur" : "12px",
				"shadow-color" : "#000",
				"shadow-opacity" : "0.8",
				"shadow-offset-x" : "0px",
				"shadow-offset-y" : "4px"
			}
		}, {
			"selector" : "edge.filtered",
			"style" : {
				"opacity" : "0"
			}
		} ],

		elements : graphData
	});

	var params = {
		name : 'cola',
		nodeSpacing : 8,
		edgeLengthVal : 65,
		animate : true,
		randomize : false,
		maxSimulationTime : 1500
	};
	var layout = makeLayout();
	var running = false;

	cy.on('layoutstart', function() {
		running = true;
	}).on('layoutstop', function() {
		running = false;
	});

	layout.run();

	var $config = $('#config');
	var $btnParam = $('<div class="param"></div>');
	$config.append($btnParam);

	var sliders = [ {
		label : 'Edge length',
		param : 'edgeLengthVal',
		min : 1,
		max : 200
	},

	{
		label : 'Node spacing',
		param : 'nodeSpacing',
		min : 1,
		max : 50
	} ];

	var buttons = [ {
		label : '<i class="fa fa-random"></i>',
		layoutOpts : {
			randomize : true,
			flow : null
		}
	},

	{
		label : '<i class="fa fa-long-arrow-down"></i>',
		layoutOpts : {
			flow : {
				axis : 'y',
				minSeparation : 30
			}
		}
	} ];

	sliders.forEach(makeSlider);
	buttons.forEach(makeButton);

	function makeLayout(opts) {
		params.randomize = false;
		params.edgeLength = function(e) {
			return params.edgeLengthVal / e.data('weight');
		};

		for ( var i in opts) {
			params[i] = opts[i];
		}

		return cy.makeLayout(params);
	}

	function makeSlider(opts) {
		var $input = $('<input></input>');
		var $param = $('<div class="param"></div>');

		$param.append('<span class="label label-default">' + opts.label
				+ '</span>');
		$param.append($input);

		$config.append($param);

		var p = $input.slider({
			min : opts.min,
			max : opts.max,
			value : params[opts.param]
		}).on('slide', _.throttle(function() {
			params[opts.param] = p.getValue();

			layout.stop();
			layout = makeLayout();
			layout.run();
		}, 16)).data('slider');
	}

	function makeButton(opts) {
		var $button = $('<button class="btn btn-default">' + opts.label
				+ '</button>');

		$btnParam.append($button);

		$button.on('click', function() {
			layout.stop();

			if (opts.fn) {
				opts.fn();
			}

			layout = makeLayout(opts.layoutOpts);
			layout.run();
		});
	}

	cy.nodes().forEach(function(n) {
		var g = n.data('name');

		n.qtip({
			content : g,
			position : {
				my : 'top center',
				at : 'bottom center'
			},
			style : {
				classes : 'qtip-bootstrap',
				tip : {
					width : 16,
					height : 8
				}
			}
		});
	});

	$('#config-toggle').on('click', function() {
		$('body').toggleClass('config-closed');

		cy.resize();
	});

}); // on dom ready


$(function() {
	FastClick.attach(document.body);
});


$(document).ready(function(){
	// get the account list for the account specific filter
	$.getJSON("test.json", success = function(data){
		var options = "";
		
		for(var i = 0; i < data.length; i++)
			{
				options += "<option value='" + data[i].name + "'>" + data[i].name + "</option>";
			}

		$("#twitterAccountList").append(options);
	});
	
	//create the datepicker
	var pickerFrom = new Pikaday({ field: $('#datepickerFrom')[0] });
	console.log(pickerFrom);
	
	var pickerTo = new Pikaday({ field: $('#datepickerTo')[0] });
	console.log(pickerTo);
	
});

// the filter relevant variables
var accountSpecificFilterList;
var accountFilter;
var twitterAccountList;
var twitterAccount;
var dateFrom;
var dateTo;


// the Twitter Account Dropdown is only enabled if the search should be account specific
function accountSpecificFilter(){
	accountSpecificFilterList = document.getElementById("accountSpecificFilterDropdown");
	accountFilter = accountSpecificFilterList.options[accountSpecificFilterList.selectedIndex].value;
	
	if(accountFilter === "0"){
		//document.getElementById("twitterAccountFilter").classList.toggle("show")
		document.getElementById("twitterAccountList").disabled=true;
	} else {
		document.getElementById("twitterAccountList").disabled=false;
	}
}

// gets the chosen Twitter Account from the Account Dropdown
function filterTwitterAccounts(){
	twitterAccountList = document.getElementById("twitterAccountList");
	twitterAccount = twitterAccountList.options[twitterAccountList.selectedIndex].text;
	}

function applyFilter(){
	
	// TODO: implement the filter functionality and delete the "alert-functionality" 
	
	dateFrom = document.getElementById("datepickerFrom").value;
	dateTo = document.getElementById("datepickerTo").value;
	
	if(accountFilter === "1"){
		alert("The chosen filters are: \r\n" 
					+ "Account: " + twitterAccount + "\r\n"
					+ "Date from: " + dateFrom + "\r\n"
					+ "Date to: " + dateTo)
	} else {
		alert("The chosen filters are: \r\n" 
				+ "Date from: " + dateFrom + "\r\n"
				+ "Date to: " + dateTo)
	}
}
