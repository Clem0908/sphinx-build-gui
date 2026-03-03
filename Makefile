build:
	mvn clean package jpackage:jpackage
run:
	./target/dist/sphinx-build-gui/bin/sphinx-build-gui
