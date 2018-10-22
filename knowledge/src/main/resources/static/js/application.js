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

$(document).ready(function () {
    if (window.numberOfReleasesByYear !== undefined) {
        displayNumberOfReleasesByYear();

        window.addEventListener("resize", displayNumberOfReleasesByYear);
    }
});