# Easyshop2.0

 ## Description

 This Application Program Interface (API) is a connector program that bridges a back-end SQL database with a front-end HTML User Interface (UI),
 that not only allows the user to browse a store's entire inventory, but also add products to an online cart and make a purchase.  

 ## Features

 The UI storefront has many interactables for the users:
 - a button for logging into an account
 - a combo box for filtering through Categories
 - 2 price sliders to filter by a price range
 - a combo box to filter by color


## Screenshots of output/progression

 ### This is a screenshot of the Home Screen menu with working Categories
 - Upon launch of the site, there was a notice saying that "Categories failed to load" which was my first fix.
 - - Fixed the issue by developing the Category-related classes and made sure they connect to the database.
 ![HomeScreenw/CategoriesFixed](https://github.com/MarqAlejandro/Easyshop2.0/blob/main/screenshots/CategoriesFixed.png)
 
 ### The price sliders before the fix.
 Users have reported that the product search functionality is returning incorrect results.
 Found the issue in not only the HTML, but also the API. 
 Not only was the HTML displaying 2 "Minimum Price" sliders, but also the API wasn't using max price.
 ![before the fix](https://github.com/MarqAlejandro/Easyshop2.0/blob/main/screenshots/ErrorOfMinSliders.png)

 ### The price sliders after the fix!
 I made sure the HTML reflected the proper labels, 
 and made sure that the logic would create a proper price range.
 ![after the fix](https://github.com/MarqAlejandro/Easyshop2.0/blob/main/screenshots/usageOfMin_MaxSliders2.png)
 
 ### This shows there's an Error with Admin Update Permissions
 Some users have also noticed that some of the products seem to be duplicated.  
 For example, a laptop is listed 3 times, and it appears to be the same product, but there are slight differences.  
 It appears that instead of updating the product, each time you tried to update, it added a new product to the database. 
 ![updates create new products](https://github.com/MarqAlejandro/Easyshop2.0/blob/main/screenshots/before%20bug%20fix.png)
 
 ### This screenshot shows post-fix to the Admin Update Permissions
 I inspected the Product-related classes and found that the update method was adding a new product instead of updating it.
 Now the update command will properly update a product as scene in the screenshot, __(Specifically look at the right-most Tea Kettle product.)__
 ![post-fix](https://github.com/MarqAlejandro/Easyshop2.0/blob/main/screenshots/after%20bug%20fix.png)
 

 ### Ran PostMan Script Test - All Pass
 To verify that the API is working properly, I ran a series of postman scripts and tests that would prove that the code is working as intended. 
 ![postmanTesting](https://github.com/MarqAlejandro/Easyshop2.0/blob/main/screenshots/postman-easyshop%20script%20all%20pass.png)
 

 ## My Favorite Block of Code(s)

 

 

 

 
 
