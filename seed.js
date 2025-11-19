// Import necessary libraries
const admin = require("firebase-admin");
const fs = require("fs");
const csv = require("csv-parser");

// Import your service account key
const serviceAccount = require("./serviceAccountKey.json");

// Initialize the Firebase Admin SDK
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

// Get a reference to the Firestore database
const db = admin.firestore();

// File paths for your CSVs
const categoriesFile = "./Categories.csv";
const productsFile = "./Products.csv";
const ordersFile = "./Orders.csv";
const orderItemsFile = "./Order_Items.csv";
const usersFile = "./Users.csv"; // <-- Includes Users

/**
 * Uploads data from a CSV file to a specified Firestore collection.
 * This is a generic helper function.
 * @param {string} filePath - Path to the CSV file.
 * @param {string} collectionName - Name of the Firestore collection.
 * @param {string} docIdField - The CSV column name to use as the document ID.
 * @param {function} [transformFn] - Optional function to transform a row before upload.
 */
async function uploadCsvToFirestore(filePath, collectionName, docIdField, transformFn = (row) => row) {
  console.log(`Uploading ${collectionName}...`);
  const processedIds = new Set();

  return new Promise((resolve, reject) => {
    fs.createReadStream(filePath)
      .pipe(csv())
      .on("data", async (row) => {
        try {
          const docId = row[docIdField];
          if (!docId) {
            console.warn(`Skipping row in ${collectionName}, missing ${docIdField}:`, row);
            return;
          }
          if (processedIds.has(docId)) return; // Avoids duplicate writes from stream
          processedIds.add(docId);

          // Apply the transformation function to the row
          const data = transformFn(row);

          // Remove the ID field from the data object, as it's the doc ID
          delete data[docIdField];

          // Use .set() to create or overwrite the document
          await db.collection(collectionName).doc(docId).set(data);
        } catch (error) {
          console.error(`Error uploading ${collectionName} ${row[docIdField]}:`, error.message);
        }
      })
      .on("end", () => {
        console.log(`✅ ${collectionName} CSV successfully processed.`);
        resolve();
      })
      .on("error", (error) => reject(error));
  });
}

/**
 * Uploads Order Items to their parent Order's subcollection.
 * This is a special function because it writes to a subcollection.
 */
async function uploadOrderItems() {
  console.log("Uploading Order Items (as subcollections)...");
  const processedIds = new Set();

  return new Promise((resolve, reject) => {
    fs.createReadStream(orderItemsFile)
      .pipe(csv())
      .on("data", async (row) => {
        try {
          const docId = row.Order_Item_ID;
          const orderId = row.Order_ID;

          if (!docId || !orderId) {
            console.warn("Skipping order item row, missing ID:", row);
            return;
          }
          if (processedIds.has(docId)) return;
          processedIds.add(docId);

          // Prepare the data for the subcollection document
          const itemData = {
            Product_ID: row.Product_ID,
            Quantity: parseInt(row.Quantity, 10), // Convert to number
            Price_at_Purchase: parseFloat(row.Price_at_Purchase) // Convert to number
          };

          // This path finds the parent Order, then its 'Order_Items' subcollection
          await db.collection("Orders").doc(orderId).collection("Order_Items").doc(docId).set(itemData);
        } catch (error) {
          console.error(`Error uploading order item ${row.Order_Item_ID}:`, error.message);
        }
      })
      .on("end", () => {
        console.log("✅ Order Items CSV successfully processed.");
        resolve();
      })
      .on("error", (error) => reject(error));
  });
}

/**
 * Main function to run all seeders
 */
async function main() {
  try {
    // --- Define Data Transformations (to fix data types from CSV) ---

    const categoryTransform = (row) => ({
      Name: row.Name,
      Description: row.Description,
      is_active: (row.is_active.toLowerCase() === 'true'),
      created_at: row.created_at,
      updated_at: row.updated_at,
    });

    const productTransform = (row) => ({
      Category_ID: row.Category_ID,
      Brand: row.Brand,
      Name: row.Name,
      Description: row.Description,
      Ingredients: row.Ingredients,
      Price: parseFloat(row.Price),
      Size: row.Size,
      Stock: parseInt(row.Stock, 10),
      Image_URL: row.Image_URL,
      Recommended_Usage: row.Recommended_Usage,
      is_active: (row.is_active.toLowerCase() === 'true'),
      created_at: row.created_at,
      updated_at: row.updated_at,
      Keywords: row.Keywords.split(',').map(keyword => keyword.trim())
    });

    const orderTransform = (row) => ({
      User_ID: row.User_ID,
      Order_Date: row.Order_Date,
      Subtotal: parseFloat(row.Subtotal),
      Shipping_Cost: parseFloat(row.Shipping_Cost),
      Tax: parseFloat(row.Tax),
      Total_Price: parseFloat(row.Total_Price),
      Payment_Info: row.Payment_Info,
      Order_Status: row.Order_Status,
      Shipping_Street_Number: row.Shipping_Street_Number,
      Shipping_Street_Name: row.Shipping_Street_Name,
      Shipping_Apartment_Number: row.Shipping_Apartment_Number,
      Shipping_City: row.Shipping_City,
      Shipping_State: row.Shipping_State,
      Shipping_Zip_Code: row.Shipping_Zip_Code,
      Shipping_Country: row.Shipping_Country,
      Billing_Street_Number: row.Billing_Street_Number,
      Billing_Street_Name: row.Billing_Street_Name,
      Billing_Apartment_Number: row.Billing_Apartment_Number,
      Billing_City: row.Billing_City,
      Billing_State: row.Billing_State, // Corrected from your ERD
      Billing_Zip_Code: row.Billing_Zip_Code,
      Billing_Country: row.Billing_Country,
      Last_Updated: row.Last_Updated
    });

    const userTransform = (row) => ({
      First_Name: row.First_Name,
      Last_Name: row.Last_Name,
      Email: row.Email,
      Date_Of_Birth: row.Date_Of_Birth,
      Street: row.Street,
      Apartment_Number: row.Apartment_Number,
      City: row.City,
      Zip_Code: row.Zip_Code,
      State: row.State,
      Country: row.Country,
      Role: row.Role, // "admin" or "customer"
      created_at: row.created_at
    });

    // --- Run All Uploads in Order ---

    await uploadCsvToFirestore(categoriesFile, "Categories", "Category_ID", categoryTransform);
    await uploadCsvToFirestore(productsFile, "Products", "Product_ID", productTransform);
    await uploadCsvToFirestore(ordersFile, "Orders", "Order_ID", orderTransform);
    await uploadOrderItems(); // This one is special (subcollection)
    await uploadCsvToFirestore(usersFile, "Users", "User_ID", userTransform); // <-- ADDS THE USERS

    console.log("Database seeding finished successfully!");

  } catch (error) {
    console.error("Error seeding database:", error);
  }
}

// Run the main function
main();

