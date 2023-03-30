package Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeAction;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.CreateTableResponse;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

public class TwinderDAO {
  private DynamoDbClient dbClient;
  public final String userTableName = "Users";
  public final String userID = "userID";
  public final String numOfLikes = "numOfLikes";
  public final String numOfDislikes = "numOfDislikes";
  public final String top100Likes = "top100Likes";

  public TwinderDAO(DynamoDbClient client) {
    this.dbClient = client;
  }

  public void createTables() {
    createSwipesTable();
    createUsersTable();
  }

  private String createSwipesTable() {
    return "";
  }

  private String createUsersTable() {
    DynamoDbWaiter dbWaiter = dbClient.waiter();
    CreateTableRequest request = CreateTableRequest.builder()
        .attributeDefinitions(AttributeDefinition.builder()
            .attributeName(userID)
            .attributeType(ScalarAttributeType.S)
            .build())
        .keySchema(KeySchemaElement.builder()
            .attributeName(userID)
            .keyType(KeyType.HASH)
            .build())
        .provisionedThroughput(ProvisionedThroughput.builder()
            .readCapacityUnits(new Long(10))
            .writeCapacityUnits(new Long(10))
            .build())
        .tableName(userTableName)
        .build();

    String newTable ="";
    try {
      CreateTableResponse response = dbClient.createTable(request);
      DescribeTableRequest tableRequest = DescribeTableRequest.builder()
          .tableName(userTableName)
          .build();

      // Wait until the Amazon DynamoDB table is created.
      WaiterResponse<DescribeTableResponse> waiterResponse = dbWaiter.waitUntilTableExists(tableRequest);
      waiterResponse.matched().response().ifPresent(System.out::println);
      newTable = response.tableDescription().tableName();
      return newTable;

    } catch (DynamoDbException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    return "";
  }

  public void sendSwipeToDB(String message) {
    sendToSwipesTable(message);
    sendToUsersTable(message);
  }



  private void sendToSwipesTable(String message) {

  }

  private void sendToUsersTable(String message) {
    String[] data = message.split(",");
    String userIDValue = data[1];
    AttributeValue idAttribute = AttributeValue.builder().s(userIDValue).build();

    // Create a GetItemRequest to get the item from the table
    GetItemRequest getItemRequest = GetItemRequest.builder()
        .tableName(userTableName)
        .key(
            Map.of(
                userID, idAttribute
            )
        )
        .build();

    // Get the item from the table
    GetItemResponse getItemResponse = dbClient.getItem(getItemRequest);

    // Check if the item exists
    boolean itemExists = getItemResponse.hasItem();

    if(itemExists) {
      updateDataToUserTable(data, getItemResponse.item());
    }else {
      putDataToUserTable(data);
    }
  }

  private void putDataToUserTable(String[] data) {
    // Convert the list to a list of AttributeValue objects
    List<AttributeValue> attributeValues = new ArrayList<>();

    HashMap<String,AttributeValue> itemValues = new HashMap<>();
    itemValues.put(userID, AttributeValue.builder().s(data[1]).build());
    // like
    if(data[0].equals("right")) {
      itemValues.put(numOfLikes, AttributeValue.builder().n("1").build());
      itemValues.put(numOfDislikes, AttributeValue.builder().n("0").build());
      AttributeValue attributeValue = AttributeValue.builder()
          .s(data[2])
          .build();
      attributeValues.add(attributeValue);
    }else {
      // dislike
      itemValues.put(numOfLikes, AttributeValue.builder().n("0").build());
      itemValues.put(numOfDislikes, AttributeValue.builder().n("1").build());
    }

    itemValues.put(top100Likes, AttributeValue.builder().l(attributeValues).build());

    PutItemRequest request = PutItemRequest.builder()
        .tableName(userTableName)
        .item(itemValues)
        .build();

    try {
      PutItemResponse response = dbClient.putItem(request);
      System.out.println(userTableName +" was successfully updated. The request id is "+response.responseMetadata().requestId());

    } catch (ResourceNotFoundException e) {
      System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", userTableName);
      System.err.println("Be sure that it exists and that you've typed its name correctly!");
      System.exit(1);
    } catch (DynamoDbException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
  }

  private void updateDataToUserTable(String[] data, Map<String,AttributeValue> item) {
    // Get previous values
    Integer numOfLikesForUser = Integer.valueOf(item.get(numOfLikes).n());
    Integer numOfDislikesForUser = Integer.valueOf(item.get(numOfDislikes).n());
    List<AttributeValue> top100Users = new ArrayList<>(item.get(top100Likes).l());

    // Update values
    // like
    if(data[0].equals("right")) {
      if(numOfLikesForUser == 100) {
        top100Users.remove(0);
      }else {
        numOfLikesForUser++;
      }
      AttributeValue attributeValue = AttributeValue.builder()
          .s(data[2])
          .build();
      top100Users.add(attributeValue);
    }else {
      numOfDislikesForUser++;
    }

    HashMap<String,AttributeValue> itemKey = new HashMap<>();
    itemKey.put(userID, AttributeValue.builder()
        .s(data[1])
        .build());

    HashMap<String, AttributeValueUpdate> updatedValues = new HashMap<>();
    updatedValues.put(numOfLikes, AttributeValueUpdate.builder()
        .value(AttributeValue.builder().n(String.valueOf(numOfLikesForUser)).build())
        .action(AttributeAction.PUT)
        .build());
    updatedValues.put(numOfDislikes, AttributeValueUpdate.builder()
        .value(AttributeValue.builder().n(String.valueOf(numOfDislikesForUser)).build())
        .action(AttributeAction.PUT)
        .build());
    updatedValues.put(top100Likes, AttributeValueUpdate.builder()
        .value(AttributeValue.builder().l(top100Users).build())
        .action(AttributeAction.PUT)
        .build());


    UpdateItemRequest requestForStats = UpdateItemRequest.builder()
        .tableName(userTableName)
        .key(itemKey)
        .attributeUpdates(updatedValues)
        .build();

    try {
      dbClient.updateItem(requestForStats);
    } catch (ResourceNotFoundException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    } catch (DynamoDbException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    System.out.println("The Amazon DynamoDB table was updated!");

  }
  public List<Integer> getStats(String userIDValue) {
    List<Integer> stats = new ArrayList<>();
    QueryResponse response = getResponse(userIDValue);
    stats.add(getLikesOfUser(response));
    stats.add(getDislikesOfUser(response));
    return stats;
  }

  public int getLikesOfUser(QueryResponse response) {
    return Integer.parseInt(response.items().get(0).get(numOfLikes).n());
  }
  public int getDislikesOfUser(QueryResponse response) {
    return Integer.parseInt(response.items().get(0).get(numOfDislikes).n());
  }

  public List<String> getTop100OfUser(String userIDValue) {
    QueryResponse response = getResponse(userIDValue);
    ArrayList<AttributeValue> reponseList = new ArrayList<AttributeValue>(response.items().get(0).get(top100Likes).l());
    List<String> result = reponseList.stream().map(AttributeValue::s).collect(Collectors.toList());
    return result;
  }

  public QueryResponse getResponse(String userIDValue) {
    // Set up an alias for the partition key name in case it's a reserved word.
    HashMap<String,String> attrNameAlias = new HashMap<String,String>();
    String partitionAlias = "#qq";
    attrNameAlias.put(partitionAlias, userID);

    // Set up mapping of the partition name with the value.
    HashMap<String, AttributeValue> attrValues = new HashMap<>();

    attrValues.put(":"+userID, AttributeValue.builder()
        .s(userIDValue)
        .build());

    QueryRequest queryReq = QueryRequest.builder()
        .tableName(userTableName)
        .keyConditionExpression(partitionAlias + " = :" + userID)
        .expressionAttributeNames(attrNameAlias)
        .expressionAttributeValues(attrValues)
        .build();

    try {
      QueryResponse response = dbClient.query(queryReq);
      return response;

    } catch (DynamoDbException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    return null;
  }
}
