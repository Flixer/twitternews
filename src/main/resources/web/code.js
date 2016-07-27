var cy;
var graphData = [];
var numberOfGraphResLoaded = 0;
var wordCloudData;
var wordFrequencyList;

// word-cloud values
var fill;
var fontSize;
var fillColor;
var size = [ 400, 200 ];

$(function() {
	reloadInformation();

	// get the account list for the account specific filter
	$.getJSON("resources/accounts.txt", success = function(data) {
		var options = "";

		for (var i = 0; i < data.length; i++) {
			options += "<option value='" + data[i].name + "'>" + data[i].name
					+ "</option>";
		}

		$("#twitterAccountList").append(options);
	});

	initMenu();
	// create the datepicker
	var pickerFrom = new Pikaday({
		field : $('#datepickerFrom')[0],
		format : 'YYYY-MM-DD',
	});
	var pickerTo = new Pikaday({
		field : $('#datepickerTo')[0],
		format : 'YYYY-MM-DD',
	});

	accountSpecificFilter();
});

function reloadInformation() {
	fill = d3.scale.category20();
	fontSize = d3.scale.log().range([ 15, 100 ]);
	fillColor = d3.scale.category20();

	numberOfGraphResLoaded = 0;
	graphData = [];
	$.get("resources/edges.txt", graphResDataParser, "text");
	$.get("resources/nodes.txt", graphResDataParser, "text");
}

var wordCloudParser = function(data) {
	// draw new tag cloud -> delete old clouds
	$("#tag-cloud").html("");

	wordCloudData = jQuery.parseJSON("[" + data.substr(0, data.length - 3)
			+ "]");

	for (var i = 0; i < wordCloudData.length; i++) {
		var wordslist = [];
		var wordSize = 0;

		for (var j = 0; j < wordCloudData[i].words.length; j++) {
			var sizeMultiplier = wordFrequencyList[wordCloudData[i].words[j]];
			if (!sizeMultiplier) {
				sizeMultiplier = 0.1;
			}
			wordslist.push({
				'text' : wordCloudData[i].words[j],
				'size' : 5 * Math.pow(sizeMultiplier, 0.5)
			});
		}
		d3.layout.cloud().size(size).words(wordslist).font("Impact").fontSize(
				function(d) {
					return fontSize(+d.size);
				}).on("end", drawTagCloud).start();
	}

	$(".word-cloud-wrapper").click(function() {
		var elm = $(this);
		if (!elm.hasClass("cloud-expanded")) {
			$(".word-cloud-wrapper").removeClass(
					"cloud-expanded");
			elm.addClass("cloud-expanded");
			var svg = $(".cloud-expanded").find("svg");
			svg.width(svg.width() * 2.5);
			svg.height(svg.height() * 2.5);
			svg.find("g").attr("transform", "translate(500,250) scale(2.5)");
			
			if (!elm.hasClass("has-additional-info")) {
				elm.addClass("has-additional-info");
				var cloudSourceHtml = "TODO - Make as chart: <br/><br/>";
				for (var j = 0; j < wordCloudData[elm.index()].sources.length; j++) {
					var source = wordCloudData[elm.index()].sources[j];
					cloudSourceHtml += "<b>" + source.name + ":</b> " + source.count + "<br/>"
				}
				elm.prepend("<div class=\"cloud-sources\">" + cloudSourceHtml + "</div>");
				elm.prepend("<div class=\"cloud-header\"><span class=\"fa fa-times config-toggle close-word-cloud\"></span></div>");
				$(".close-word-cloud").click(
						function() {
							$(".word-cloud-wrapper").removeClass(
									"cloud-expanded");
							svg.width(svg.width() / 2.5);
							svg.height(svg.height() / 2.5);
							svg.find("g").attr("transform", "translate(200,100) scale(1)");
							return false;
						});
			}
		}
	});
};

var drawTagCloud = function(words) {
	wordcloud = d3.select("#tag-cloud").append("div").attr("class",
			"word-cloud-wrapper").append("svg").attr("width", size[0]).attr(
			"height", size[1]).append("g").attr("transform",
			"translate(" + (size[0] / 2) + "," + (size[1] / 2) + ")");

	wordcloud.selectAll("text").data(words).enter().append("text").style(
			"font-size", function(d) {
				console.log(d.size);
				return d.size + "px";
			}).style("fill", function(d) {
		return fill(d.text.toLowerCase());
	}).style("font-family", "Impact").attr("text-anchor", "middle").attr(
			"transform",
			function(d) {
				return "translate(" + [ d.x, d.y ] + ") rotate(" + d.rotate
						+ ")";
			}).text(function(d) {
		return d.text;
	});
}

var graphResDataParser = function(data) {
	var loadedData = jQuery.parseJSON("[" + data.substr(0, data.length - 2)
			+ "]");
	if (loadedData.length && loadedData[0].group == "nodes") {
		// wait for loading cluster information until node information are loaded
		$.get("resources/cluster.txt", wordCloudParser, "text");
		
		wordFrequencyList = {};
		for (var n in loadedData) {
			var nodeData = loadedData[n].data;
			wordFrequencyList[nodeData.name] = nodeData.score;
		}
	}
	graphData = $.merge(graphData, loadedData);
	numberOfGraphResLoaded++;
	if (numberOfGraphResLoaded == 2) {
		initCY();
	}
};

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

	$('#config .param').remove();
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

	$('#config-toggle').unbind("click").on('click', function() {
		$('body').toggleClass('config-closed');

		cy.resize();
	});

}); // on dom ready

$(function() {
	FastClick.attach(document.body);
});

// the Twitter Account Dropdown is only enabled if the search should be account
// specific
function accountSpecificFilter() {
	if ($("#accountSpecificFilterDropdown").val() === "0") {
		$("#twitterAccountFilter").hide();
	} else {
		$("#twitterAccountFilter").show();
	}
}

function applyFilter() {
	var dateFrom = $("#datepickerFrom").val();
	var dateTo = $("#datepickerTo").val();

	var url = "/analyze?";
	if ($("#accountSpecificFilterDropdown").val() === "1") {
		url += "source=" + $("#twitterAccountList").val() + "&";
	}
	if (dateFrom) {
		url += "dateFrom=" + dateFrom + " 00:00:00&";
	}
	if (dateTo) {
		url += "dateTo=" + dateTo + " 23:59:59&";
	}

	var loadingText = '<div class="cssload-loader"><div class="cssload-inner cssload-one"></div><div class="cssload-inner cssload-two"></div><div class="cssload-inner cssload-three"></div></div><h1>Twitter Daten werden analyisiert...</h1>';
	$.blockUI({
		message : loadingText
	});
	$.get(url, function(res) {
		if (res != "200") {
			alert("Es ist ein Fehler aufgetreten");
		} else {
			reloadInformation();
		}
		$.unblockUI();
	}).fail(function() {
		alert("Es ist ein Fehler aufgetreten");
		$.unblockUI();
	});
}
initMenu = function() {
	$("#header-tabs a").click(function() {
		var li = $(this).parent();
		$("#header-tabs li").removeClass('active');
		li.addClass('active');

		$(".tab-container").hide();
		$(".tab-container#" + li.data('container')).show();
		return false;
	});
}
