# Meal Planner Generator

## Build a meal planner (.md) from

- recipes.csv: Specify name, ingredients, preparation steps. [Sample](./lib/recipes.csv).
  - Instructions not supported. This program only does a few things and does them well---help you plan ahead (shopping and completing preparation steps). It is not a recipe book.
- agenda.md: Specify the week's meals in a markdown table. [Sample](./lib/agenda.md).
- stock.csv (optional): Specify the stock of ingredients in your home. [Sample](./lib/stock.csv).
  - Recommended to only have durable goods recorded, e.g. spices, sauces, etc.
  - For other ingredients, produce a meal planner first and check the ingredients you have on the fly. Your changes will be saved in the next meal planner, when you feed the program with the modified agenda.

[Sample output meal planner](./out/agenda-week19_05-10T13:04:47.md).

## Minimal dependencies

- [OpenJDK 17](https://openjdk.java.net/projects/jdk/17/)
- [Make](https://www.gnu.org/software/make/)
- [Git](https://git-scm.com/)

It is also recommended to have the following installed:

- [Any Markdown Viewer](https://www.typora.io/)
- Any CSV Editor

## Get started

- Clone this repository
- Run `make` in the root directory: This will take the pre-existing sample [`agenda.md`](./lib/agenda.md), [`recipes.csv`](./lib/recipes.csv), and [`stock.csv`](./lib/stock.csv) to generate the meal planner in the `out` directory.

## Build your own meal planner

By modifying the sample files: [`agenda.md`](./lib/agenda.md), [`recipes.csv`](./lib/recipes.csv), and [`stock.csv`](./lib/stock.csv). Be sure to adhere to the syntax and format of the sample files strictly. The parser is not designed to handle any deviations from the sample files. 

- Be aware of whitespace, line breaks, and delimiters (`,`, `|`, `:`, `;`).
- Use [ISO-8601 duration format `PnDTnHnMn.nS`](https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#parse-java.lang.CharSequence-) to specify durations (how much time ahead) for preparation steps.
