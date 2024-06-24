# Amazon's IceCreamParlorService

## Introduction

We are returning again to the `IceCreamParlorService` you have seen in previous lessons.

A brief overview of `IceCreamParlorService` is provided as a reminder.

**Key models:**
- `Carton`: A carton of ice cream of a particular flavor that is available in the
  parlor. If the carton `isEmpty()`, however, there is none of that flavor left!
- `Flavor`: A particular flavor of ice cream. Each flavor has a unique combination
  of `Ingredient`s, which are specified by a `Recipe`.
- `Ingredient`: An ingredient needed to make ice cream. Some ingredients are
  shared across flavors, some are unique to a flavor.
- `Recipe`: For a given flavor, the recipe indicates the ingredients to add to the
  mixture (as well as the order in which they should be added, in the form of a
  `Queue`).
- `Sundae`: What our customers order! They contain a list of scoops of `Flavor`
  objects representing each scoop in the sundae.

![Ice Cream Parlor Service Models class diagram.](./resources/IceCreamParlorModels_CD.png)

[Ice Cream Parlor Service Models diagram PlantUML source](./resources/IceCreamParlorModels_CD.puml)

The `IceCreamParlorService` depends on:
- two DAOs, `CartonDao` and `RecipeDao`, for
  accessing `Carton`s and `Recipe`s, respectively.
- `IceCreamMaker`, which accepts ingredients and then does the mixing and
  freezing.

![Ice Cream Parlor Service class diagram, showing the service class's dependencies.
The IceCreamParlorService depends on two DAOs, CartonDao and RecipeDao, for accessing
Cartons and Recipes, respectively. It also depends on IceCreamMaker, which accepts
ingredients and then does the mixing and freezing.](./resources/IceCreamParlorService_CD.png)

[Ice Cream Parlor Service class diagram PlantUML source](./resources/IceCreamParlorService_CD.puml)

The owners of `IceCreamParlorService` have decided to manage more of their data in DynamoDB tables. It will
be your job to help them set up the tables they require.

## Phase 0: Deploy `Recipe` table

The `Recipes` table has already been [designed](./resources/Recipes-tabledesign.txt) and [implemented
in CloudFormation](cloudformation/dynamodbtabledesign/RecipesTable.yaml),
but it has not yet been deployed. Your first task will be to deploy the `Recipes` table to your
AWS account and see that it is working. (The `cloudformation` directory is in the top level directory of
this snippets package)

1. Make sure `ada` is running with the credentials for your IAM account.
1. Create the tables we'll be using for this activity by running these aws CLI commands:
   ```none
   aws cloudformation create-stack --region us-west-2 --stack-name dynamodbtabledesign-recipestable01 --template-body file://cloudformation/dynamodbtabledesign/RecipesTable.yaml --capabilities CAPABILITY_IAM
   ```
1. Make sure the above `aws cloudformation` command runs without error.
1. Log into your AWS account console and verify that the table exists and has
   sample data.
    *   You can check the status of your new CloudFormation stack at
    https://us-west-2.console.aws.amazon.com/cloudformation/home?region=us-west-2#/stacks, which will include
    links to the resources the template defined.
1. Discuss the different attributes with your team to make sure you all understand
   what they represent.
1. Answer within your team: what is the primary key structure for this table?

**GOAL:** `Recipes` table is created in your AWS Account and the attributes make sense.

Phase 0 is complete when:
- `Recipes` table exists in your AWS account with some sample data
- You understand the `Recipes` table attributes for the sample items in the table

## Phase 1: Implement `Cartons` table

Now create the `Cartons` table to manage the `Carton`s that `IceCreamParlorService` uses.
The table has already been designed in `./resources/Cartons-tabledesign.txt`.

You need to:
1. Create a new file `cloudformation/dynamodbtabledesign/CartonsTable.yaml` with your
   CloudFormation definition of the `Cartons` table.
  * If using `RecipesTable.yaml` as a guide, remove the line starting with `LambdaRole:` and every
    line below it.
    * (The other resources for populating the `Recipes` table with sample items, but you can add items to
      `Cartons` manually to test your table.)
    * if you want practice later, you could try to re-add the relevant lines of the cloud formation
      template to populate sample data (delete the table from DynamoDB first, then rerun the `aws`
      command)
1. Deploy your new table to your AWS account by running
   ```
   aws cloudformation create-stack --region us-west-2 --stack-name dynamodbtabledesign-cartonstable01 --template-body file://cloudformation/dynamodbtabledesign/CartonsTable.yaml --capabilities CAPABILITY_IAM
   ```
1. Confirm your table is provisioned matching the design provided.
1. Create a sample Carton item in your table using the DynamoDB Console. Be sure to use
   the attributes specified in the design file

**GOAL:** `Cartons` table CloudFormation definition is written and deployed to your AWS account.

Phase 1 is complete when:
- You have created file `cloudformation/dynamodbtabledesign/CartonsTable.yaml` with a CloudFormation
  implementation of the design from `Cartons-tabledesign.txt`
- You have deployed the `Cartons` table to your AWS account
- You have created one sample Carton in your table

## Phase 2: Design `Receipts` table

The `IceCreamParlorService` team would like to start tracking customer purchases in a new table, `Receipts`.
The `Receipt` POJO class does not exist yet. The team plans to create the `Receipt` class based on the data
model that you design.

The use cases relevant to the design of the `Receipts` table are:

1. The service must be able to get the total income over a specified date range.
   1. Always in day-size chunks, never need a smaller size or different time ranges
   1. Most often a single day at a time
   1. At most 30 days at a time (and these are for monthly reports, where it is ok
      for the computation to take "a while" (several seconds) to complete)
1. The service must be able to retrieve a receipt for a given customer on a given date.
   1. To simplify, let's assume each customer makes a purchase at most once per day
1. A receipt must include the total amount paid and a list of names of all the sundaes purchased.
1. In the future (perhaps after the DynamoDB Index lesson), the shop may want to
   get all receipts for a given customer, but do not design for this case in your table now.

Your job is to design the key schema and attributes for the `Receipts` table based upon
these use cases. Add your design to `./resources/Receipts-tabledesign.txt` and follow
the format used in `Cartons-tabledesign.txt` and `Recipes-tablesdesign.txt`. In particular,
be sure to indicate your key structure.

**GOAL:** `Receipts` table design exists in file `./resources/Receipts-tabledesign.txt`

Phase 2 is complete when:
- Your team have all designed the attributes and key schema for the `Receipts` table
- You have captured your design in file `./resources/Receipts-tabledesign.txt`

## Phase 3: Implement `Receipts` table

Now that you've designed the `Receipts` table, it's time to implement it!

You need to:
1. Create a new CloudFormation `.yaml` file defining the `Receipts` table
1. Deploy the `Receipts` table to your AWS account (figure out the command to run based on
   the examples you've already run above)
1. Confirm the created `Receipts` table matches your design

**GOAL:** `Receipts` table CloudFormation definition is written and deployed to your AWS account.

Phase 3 is complete when:
- You have created file `cloudformation/dynamodbtabledesign/ReceiptsTable.yaml` with a CloudFormation
  implementation of your design from Phase 2
- You have deployed the `Receipts` table to your AWS account

## Extension: Use the tables!

The current implementation of `IceCreamParlorService` (the solution to the Creating Exceptions lesson from Unit 4)
has been provided in this directory. Try updating the service to use the new tables deployed to your AWS account.

**Note:** You will need to manually add items to the `Cartons` table for testing. These should include
the existing values from `CartonDao`.
