function displayNumberOfReleasesByYear() {
    const data = numberOfReleasesByYear.map(r => ({
        group: "" + r.decade,
        name: r.year,
        value: r.numberOfReleases
    }));

    const color = d3.scaleOrdinal().range(d3.schemeCategory10);
    const container = d3.select('#container');
    const width = container.node().getBoundingClientRect().width;
    const height = container.node().getBoundingClientRect().height;

    const root = d3.pack()
        .size([width - 2, height - 2])
        .padding(3)
        (d3.hierarchy({children: data})
            .sum(d => d.value));

    container.selectAll("*").remove();

    const svg = container
        .append("svg")
        .attr("id", "x")
        .style("width", "100%")
        .style("height", "100%")
        .style("border", "1px;")
        .attr("font-size", 10)
        .attr("font-family", "sans-serif")
        .attr("text-anchor", "middle");

    const leaf = svg.selectAll("g")
        .data(root.leaves())
        .enter()
        .append("g")
        .attr("transform", d => `translate(${d.x + 1},${d.y + 1})`)
        .append("a")
        .attr("xlink:href", d => "/albums/by-year?year=" + d.data.name);

    leaf
        .append("circle")
        .attr("r", d => d.r)
        .attr("fill-opacity", 0.7)
        .attr("fill", d => color(d.data.group));

    leaf.append("text").selectAll("tspan")
        .data(d => d.data.name.split(/(?=[A-Z][^A-Z])/g))
        .enter().append("tspan")
        .attr("x", 0)
        .attr("y", (d, i, nodes) => `${i - nodes.length / 2 + 0.8}em`)
        .text(d => d);
}

function displayMicrogenres() {

    const forceSimulation = function (nodes, links) {
        return d3.forceSimulation(nodes)
            .force("link", d3.forceLink(links).id(d => d.id))
            .force("charge", d3.forceManyBody())
            .force("x", d3.forceX())
            .force("y", d3.forceY());
    };

    const drag = function (simulation) {
        function dragstarted(d) {
            if (!d3.event.active) simulation.alphaTarget(0.3).restart();
            d.fx = d.x;
            d.fy = d.y;
        }

        function dragged(d) {
            d.fx = d3.event.x;
            d.fy = d3.event.y;
        }

        function dragended(d) {
            if (!d3.event.active) simulation.alphaTarget(0);
            d.fx = null;
            d.fy = null;
        }

        return d3.drag()
            .on("start", dragstarted)
            .on("drag", dragged)
            .on("end", dragended);
    };
    const color = d3.scaleOrdinal(d3.schemeCategory10);

    const links = [];
    const nodes = [];

    for (let i = 0; i < microgenres.length; ++i) {
        const g = microgenres[i];
        nodes.push({id: g.name, group: 'microgenre'});

        const maxFrequency = Math.max(...g.albums.map(a => a.frequency));

        for (let j = 0; j < g.albums.length; ++j) {
            const album = g.albums[j].name;
            const frequency = g.albums[j].frequency;
            nodes.push({id: album, group: 'album', size: Math.max(3, 6 / maxFrequency * frequency)});
            links.push({source: g.name, target: album});
        }
    }

    const simulation = forceSimulation(nodes, links).on("tick", ticked);
    const container = d3.select('#container');
    const width = container.node().getBoundingClientRect().width / 0.85;
    const height = container.node().getBoundingClientRect().height / 0.85;

    container.selectAll("*").remove();

    const svg = container
        .append("svg")
        .attr("viewBox", [-width / 2, -height / 2, width, height])
        .attr("id", "x")
        .style("width", "100%")
        .style("height", "100%")
        .style("border", "1px;")
        .attr("font-size", 10)
        .attr("font-family", "sans-serif")
        .attr("text-anchor", "middle")
        .call(d3.zoom().on("zoom", function () {
            svg.attr("transform", d3.event.transform)
        })).append("g");

    const link = svg.append("g")
        .attr("stroke", "#999")
        .attr("stroke-opacity", 0.6)
        .selectAll("line")
        .data(links)
        .enter().append("line");

    const node = svg.append("g")
        .attr("stroke", "#fff")
        .attr("stroke-width", 1.5)
        .selectAll("circle")
        .data(nodes)
        .enter()
        .append("circle")
        .attr("r", function (d) {
            return d.group === 'album' ? d.size : 8;
        })
        .attr("fill", d => color(d.group))
        .call(drag(simulation));

    node.append("title")
        .text(d => d.id);

    function ticked() {
        link
            .attr("x1", d => d.source.x)
            .attr("y1", d => d.source.y)
            .attr("x2", d => d.target.x)
            .attr("y2", d => d.target.y);

        node
            .attr("cx", d => d.x)
            .attr("cy", d => d.y);
    }
}

function displayVenues() {
    const view = new ol.View({
        center: ol.proj.fromLonLat([6.08342, 50.77664]),
        zoom: 4
    })

    const starStyle = new ol.style.Style({
        image: new ol.style.RegularShape({
            fill: new ol.style.Fill({color: 'red'}),
            stroke: new ol.style.Stroke({color: 'black', width: 2}),
            points: 5,
            radius: 10,
            radius2: 4,
            angle: 0
        })
    });


    const vectorSource = new ol.source.Vector();
    const map = new ol.Map({
        target: 'map',
        layers: [
            new ol.layer.Tile({
                source: new ol.source.OSM()
            }),
            new ol.layer.Vector({source: vectorSource})
        ],
        view: view
    });

    const info = $('#info');
    info.tooltip({
        animation: false,
        trigger: 'manual',
        title: function () {
            return $(this).data('title')
        }
    });

    const displayFeatureInfo = function (pixel) {
        info.tooltip('hide').css({
            left: pixel[0] + 'px',
            top: (pixel[1] - 15) + 'px'
        });

        var feature = map.forEachFeatureAtPixel(pixel, function (feature) {
            return feature;
        });
        if (feature) {
            info.data('title', feature.get('name')).tooltip('show');
        }
    };

    map.on('pointermove', function (evt) {
        if (evt.dragging) {
            info.tooltip('hide');
            return;
        }
        displayFeatureInfo(map.getEventPixel(evt.originalEvent));
    });

    const distanceBetweenPoints = function (start, end) {
        const line = new ol.geom.LineString([start, end]);
        return Math.round(line.getLength() * 100) / 100;
    };

    const venuesUrl = $('#map').data('venuesUrl');
    const loadVenues = function (url) {
        const center = ol.proj.toLonLat(map.getView().getCenter());
        const extent = map.getView().calculateExtent(map.getSize());
        const topLeft = ol.extent.getTopLeft(extent);
        const topRight = ol.extent.getTopRight(extent);
        const bottomLeft = ol.extent.getBottomLeft(extent);
        const radius = Math.max(distanceBetweenPoints(topLeft, topRight), distanceBetweenPoints(topLeft, bottomLeft)) / 2.0;
        $.ajax({
            url: url,
            data: {"latitude": center[1], "longitude": center[0], "distanceInMeter": radius},
            success: function (response) {
                vectorSource.clear();

                const hasDetailedInformation = response.venues !== undefined;
                const venues = hasDetailedInformation ? response.venues : response;
                const markers = venues.map(function (mv) {
                    let marker = new ol.Feature({
                        geometry: new ol.geom.Point(ol.proj.fromLonLat([mv.location.x, mv.location.y])),
                        name: mv.label !== undefined ? mv.label : mv.name
                    });
                    marker.setStyle(starStyle);
                    return marker
                });

                vectorSource.addFeatures(markers);
            },
        });
    }

    map.on('moveend', function (evt) {
        if(venuesUrl) {
            loadVenues(venuesUrl);
        }
    });

    $('#venues-url-changer').change(function() {
        loadVenues($(this).val())
    });
}

$(document).ready(function () {

    if (window.numberOfReleasesByYear !== undefined) {
        displayNumberOfReleasesByYear();
        window.addEventListener("resize", displayNumberOfReleasesByYear);
    }

    if (window.microgenres !== undefined) {
        displayMicrogenres();
        window.addEventListener("resize", displayMicrogenres);
    }

    if ($('#map')) {
        displayVenues();
    }
});

hljs.initHighlightingOnLoad();