# How To Compile Segrada

## Compiling Java Sources

Use maven to build Segrada:

```bash
mvn clean package
```

To skip tests, do the following:

```bash
clean package -Dmaven.test.skip=true
```

## Creating CSS

Compile with less:

```bash
cd src/main/resources/less/single
lessc --plugin=less-plugin-clean-css vis.less > ../../../webapp/css/vis.css
lessc --plugin=less-plugin-clean-css segrada.less > ../../../webapp/css/segrada.css
```

## Uglify JavaScript

Compile using uglify:

```bash
cd src/main/resources/js
uglifyjs -cm < segrada.js > ../../webapp/js/segrada.min.js
uglifyjs -cm < segrada_leaflet.js > ../../webapp/js/segrada_leaflet.min.js
```

## Compiling Segrada Query Builder

The query builder is a modern Vue component. It can be compiled with npm.

```bash
cd src/main/resources/js/segrada-query-builder
npm i
npm run build
```
