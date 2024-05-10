# Directories
SRC_DIR = src
BIN_DIR = bin
OUT_DIR = out

# Java compiler
JAVAC = javac
JAVA = java
JFLAGS = -d $(BIN_DIR) -cp $(BIN_DIR)

# Default target
all: $(BIN_DIR)/App.class
	$(JAVA) -cp $(BIN_DIR) App

# Create bin and out directories
$(BIN_DIR):
	mkdir -p $(BIN_DIR)

$(OUT_DIR):
	mkdir -p $(OUT_DIR)

# Compilation recipes
$(BIN_DIR)/Config.class: $(SRC_DIR)/Config.java | $(BIN_DIR)
	$(JAVAC) $(JFLAGS) $<

$(BIN_DIR)/Recipe.class: $(SRC_DIR)/Recipe.java | $(BIN_DIR)
	$(JAVAC) $(JFLAGS) $<

$(BIN_DIR)/Day.class: $(SRC_DIR)/Day.java | $(BIN_DIR)
	$(JAVAC) $(JFLAGS) $<

$(BIN_DIR)/ShoppingList.class: $(SRC_DIR)/ShoppingList.java $(BIN_DIR)/Recipe.class | $(BIN_DIR)
	$(JAVAC) $(JFLAGS) $<

$(BIN_DIR)/PrepareInAdvance.class: $(SRC_DIR)/PrepareInAdvance.java $(BIN_DIR)/Recipe.class $(BIN_DIR)/Config.class | $(BIN_DIR)
	$(JAVAC) $(JFLAGS) $<

$(BIN_DIR)/RecipesParser.class: $(SRC_DIR)/RecipesParser.java $(BIN_DIR)/Recipe.class | $(BIN_DIR)
	$(JAVAC) $(JFLAGS) $<

$(BIN_DIR)/MealPlan.class: $(SRC_DIR)/MealPlan.java $(BIN_DIR)/Recipe.class $(BIN_DIR)/Day.class $(BIN_DIR)/Config.class | $(BIN_DIR)
	$(JAVAC) $(JFLAGS) $<

$(BIN_DIR)/Agenda.class: $(SRC_DIR)/Agenda.java $(BIN_DIR)/MealPlan.class $(BIN_DIR)/Recipe.class $(BIN_DIR)/ShoppingList.class $(BIN_DIR)/PrepareInAdvance.class | $(BIN_DIR)
	$(JAVAC) $(JFLAGS) $<

$(BIN_DIR)/App.class: $(SRC_DIR)/App.java $(BIN_DIR)/Agenda.class $(BIN_DIR)/RecipesParser.class | $(BIN_DIR)
	$(JAVAC) $(JFLAGS) $<

# Run the template application
template: $(BIN_DIR)/Agenda.class | $(OUT_DIR)
	$(JAVA) -cp $(BIN_DIR) Agenda

# Clean up
clean:
	rm -rf $(BIN_DIR)/*.class

purge: clean
	rm -rf $(BIN_DIR)/*
	rm -rf $(OUT_DIR)/*

# Help
help:
	@echo "Makefile for Java Application"
	@echo ""
	@echo "Usage:"
	@echo "  make [target]"
	@echo ""
	@echo "Targets:"
	@echo "  all       Builds all classes"
	@echo "  clean     Removes all compiled classes"
	@echo "  purge     Removes all compiled classes and cleans bin and out directories"
	@echo "  template  Runs the Agenda application"

.PHONY: all clean purge help template
