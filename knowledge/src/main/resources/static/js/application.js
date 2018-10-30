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

function displaySubgenres() {

    const forceSimulation = function(nodes, links) {
        return d3.forceSimulation(nodes)
            .force("link", d3.forceLink(links).id(d => d.id))
            .force("charge", d3.forceManyBody())
            .force("x", d3.forceX())
            .force("y", d3.forceY());
    };

    const drag = function(simulation)  {
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

    for (let i = 0; i < subgenres.length; ++i) {
        const g = subgenres[i];
        nodes.push({id: g.name, group: 'subgrene'});

        const maxFrequency = Math.max(...g.albums.map(a => a.frequency));

        for (let j = 0; j < g.albums.length; ++j) {
            const album = g.albums[j].name;
            const frequency = g.albums[j].frequency;
            nodes.push({id: album, group: 'album', size: Math.max(3, 6/maxFrequency * frequency)});
            links.push({source: g.name, target: album});
        }
    }

    const simulation = forceSimulation(nodes, links).on("tick", ticked);
    const container = d3.select('#container');
    const width = container.node().getBoundingClientRect().width/0.85;
    const height = container.node().getBoundingClientRect().height/0.85;

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
        .attr("r", function(d) { return d.group === 'album' ? d.size : 8; })
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

$(document).ready(function () {

    if (window.numberOfReleasesByYear !== undefined) {
        displayNumberOfReleasesByYear();
        window.addEventListener("resize", displayNumberOfReleasesByYear);
    }

    if (window.subgenres !== undefined) {
        displaySubgenres();
        window.addEventListener("resize", displaySubgenres);
    }
});