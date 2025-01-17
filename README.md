# E-commerce Android Application

## Description
A native Android e-commerce application that allows users to manage products and categories with a user-friendly interface. The application features a secure admin section, product management system, and category organization capabilities.

## Table of Contents
- [Features](#features)
- [Technologies Used](#technologies-used)
- [Installation](#installation)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [Screenshots](#screenshots)
- [Contributing](#contributing)
- [License](#license)

## Features
- Product Management System
  - Add new products with images
  - Set product prices and quantities
  - Categorize products
  - Search functionality
  - View all products
  
- Category Management
  - Create new categories
  - Delete existing categories
  - View all categories
  
- Admin Authentication
  - Secure admin access
  - Protected admin features
  
- User Interface
  - Navigation drawer
  - Responsive design
  - Image handling
  - Product filtering by category

## Technologies Used
- **Backend**:
  - SQLite Database
  - Android SDK
  - Java
  
- **Frontend**:
  - XML Layouts
  - Material Design Components
  - Glide for image loading
  
- **Development Tools**:
  - Android Studio
  - Gradle Build System
  - AndroidX Libraries

## Installation

1. Clone the repository:
    git clone https://github.com/brahimakil/androidproject.git


2. Open the project in Android Studio

3. Configure the project:
- Minimum SDK: 29
- Target SDK: 34
- Compile SDK: 34

4. Install dependencies:
    implementation 'com.github.bumptech.glide:glide:4.15.1'
    implementation 'androidx.navigation:navigation-fragment:2.6.0'
    implementation 'androidx.navigation:navigation-ui:2.6.0'

5. Build and run the project



## Project Structure

app/
├── src/
│ ├── main/
│ │ ├── java/
│ │ │ └── com/example/ecommercecollegeproject3/
│ │ │ ├── ui/
│ │ │ │ ├── home/
│ │ │ │ ├── products/
│ │ │ │ └── categories/
│ │ │ └── MainActivity.java
│ │ └── res/
│ │ ├── layout/
│ │ ├── drawable/
│ │ ├── values/
│ │ └── navigation/
└── build.gradle



## Database Schema

### Categories Table
sql
CREATE TABLE categories (
id INTEGER PRIMARY KEY AUTOINCREMENT,
category_name TEXT NOT NULL
)


### Products Table

sql
CREATE TABLE products (
id INTEGER PRIMARY KEY AUTOINCREMENT,
product_name TEXT NOT NULL,
product_price REAL NOT NULL,
product_quantity INTEGER NOT NULL,
category_id INTEGER NOT NULL,
image_path TEXT,
FOREIGN KEY (category_id) REFERENCES categories(id)
)


## Required Permissions

xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.INTERNET" />



## Contributing
1. Fork the repository
2. Create a new branch (`git checkout -b feature/improvement`)
3. Make your changes
4. Commit your changes (`git commit -am 'Add new feature'`)
5. Push to the branch (`git push origin feature/improvement`)
6. Create a Pull Request

## License
This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Contact
- Developer: Ibrahim Akil
- Email: brhimakil.1@gmail.com
- Project Link: [https://github.com/brahimakil/androidproject.git]

## Acknowledgments
- Material Design Components
- Glide Image Loading Library
- Android Navigation Component
