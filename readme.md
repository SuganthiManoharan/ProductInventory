1	Product Management Service
A RESTful microservice for managing product inventory, built with Spring Boot and PostgreSQL, containerized with Docker.
2	 Setup and Prerequisites
1.	Java Development Kit (JDK 17 or later): Ensure you have the JDK installed.
2.	Maven: For project building.
3.	Docker & Docker Compose: Required to run the application and database containers.


Build and Run the Project

Unzip the zip file ProductInventory.zip
cd to ProductInventory
2.	Build the application 
./mvnw clean install -DskipTests
Run the Test 
./mvnw clean -e test

3. Stop and remove all containers defined in the docker-compose file

docker-compose down

4.	Run with Docker Compose: This command will build the Docker image, start the PostgreSQL container, and start the Spring Boot service.
Bash
docker-compose up --build

4.	Access: The application will be running at http://localhost:8080. The Swagger UI for documentation is at http://localhost:8080/swagger-ui.html.

Example using Postman (or similar GUI client)
Field	Value
Method	POST
URL	http://localhost:8080/products
Headers	Content-Type: application/json
Body	Select raw and choose JSON from the dropdown.
{
  "name": "iphone 16 e",
  "quantity": 25,
  "price": 619.99
}

If the request is successful,should return Status Code201
The complete JSON object of the newly created product, including the generated id.
{
  "id": 1,
  "name": "iphone 16 e",
  "quantity": 25,
  "price": 619.99
}

Example 2 using Postman for searching a product
Method	GET
URL (Example: Search for products containing "lap")
http://localhost:8080/products/search?name=lap
Expect	200 OK status code and a list of matching product objects (e.g., "Laptop Pro 14").

Example 3

Detail	Value
Method	PUT
URL (Assuming ID 1 was created)	http://localhost:8080/products/1/quantity
Headers	Content-Type: application/json
Body	{"quantity": 55}
Expect	200 OK status code and the updated product object (quantity should now be 55).

Example 4
DELETE /products/{id} (Delete Product)

Method	DELETE
URL	http://localhost:8080/products/1
Headers	None
Body	None
Expect	204 No Content status code. 

Example 5
GET /products (Pagination and Sorting)

Method	GET
URL	http://localhost:8080/products
Query Parameters	page=1 
size=5 (limit to 5 products per page)
sort=price,desc 
Full URL	http://localhost:8080/products?page=1&size=5&sort=price,desc
Headers	(None required)
Body	(None required)
Response	200 OK status code and a paginated JSON response.

GET 
url /products/summary (Inventory Statistics)
Response	200 OK status code and a JSON object containing the summary fields.







