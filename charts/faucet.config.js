let targetBaseDir = "./target/classes/static"
const path = require('path');

module.exports = {
    js: [{
        source: "./src/main/assets/javascripts/application.js",
        target: targetBaseDir + "/javascripts/application.js"
    }],
    sass: [{
        source: "./src/main/assets/stylesheets/application.scss",
        target: targetBaseDir + "/stylesheets/application.css"
    }],
    manifest: {
        target: "./target/classes/manifest.json",
        key: 'short',
        webRoot: targetBaseDir
    }
};