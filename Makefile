run: ./lib/agenda.md ./bin ./out bin/App.class
	java -cp bin App

./out:
	mkdir -p out

./bin:
	mkdir -p bin

./bin/Config.class: ./bin ./out src/Config.java
	javac -d bin -cp bin src/Config.java

./bin/Recipe.class: ./bin ./out src/Recipe.java
	javac -d bin -cp bin src/Recipe.java

./bin/Day.class: ./bin ./out src/Day.java
	javac -d bin -cp bin src/Day.java

./bin/ShoppingList.class: ./bin ./out src/ShoppingList.java bin/Recipe.class
	javac -d bin -cp bin src/ShoppingList.java

./bin/PrepareInAdvance.class: ./bin ./out src/PrepareInAdvance.java bin/Recipe.class bin/Config.class
	javac -d bin -cp bin src/PrepareInAdvance.java

./bin/RecipesParser.class: ./bin ./out src/RecipesParser.java bin/Recipe.class
	javac -d bin -cp bin src/RecipesParser.java

./bin/MealPlan.class: ./bin ./out src/MealPlan.java bin/Recipe.class bin/Day.class bin/Config.class
	javac -d bin -cp bin src/MealPlan.java

./bin/Agenda.class: ./bin ./out src/Agenda.java bin/MealPlan.class bin/Recipe.class bin/ShoppingList.class bin/PrepareInAdvance.class
	javac -d bin -cp bin src/Agenda.java

bin/App.class: ./bin ./out src/App.java bin/Agenda.class bin/RecipesParser.class
	javac -d bin -cp bin src/App.java

template: ./out ./bin bin/Agenda.class
	java -cp bin Agenda

clean:
	rm -rf bin/*.class

purge: clean
	rm -rf bin/*
	rm -rf out/*

