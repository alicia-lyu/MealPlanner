

template:
	java -cp bin Agenda

run: ./lib/agenda.md
	java -cp bin App

clean:

purge: clean
	rm -rf bin/*
	rm -rf out/*

