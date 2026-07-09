build:
	mvn clean package 
	mkdir -p target/jar target/jpackage
	cp target/sphinx*.jar target/jar 
	mvn jpackage:jpackage
run:
	./target/dist/sphinx-build-gui/bin/sphinx-build-gui
