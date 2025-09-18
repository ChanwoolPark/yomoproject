# Project Name: Yomozomo

(A platform where users can freely rent items or talents from others and receive a fee)


## 2. Role Distribution

- **Chanwool Park (myself)**: Team Leader, Main Page (Home), Product List, Product Detail Page, Rental Reservation Page  
- **Yundong Sung**: Payment Integration, Payment Page, Admin Page  
- **Seungjoo Kim**: Customer Center, Chatbot Page, 1:1 Chat Function  
- **Hyeeun Jung**: Login Function, My Page  


## 3. Technologies Used
- **Frontend**: HTML, CSS, JavaScript, Thymeleaf  
- **Backend**: Java (17), Spring Boot, Spring Security (6.x), Python  
- **Database**: Oracle, SQL Developer  
- **Others**: GitHub, Kakao Map API, Toss Payments API  


## 4. My Responsibilities


### Team Leader
- Checked each member’s weekly schedule  
- Supported implementation of chatroom using WebSocket  
- Mediated and accepted team members’ opinions  
- Drafted and led planning documents and presentation materials  


### Main Page Design & Implementation
- Implemented search functionality with filtering by main categories  
- Displayed popular and recent searches based on the current logged-in user’s session  
- Created advertisement banner UI for revenue generation  
  - Auto-sliding images using swiper functionality  
- Developed header & footer pages  
  - Dynamic rendering depending on login state (e.g., “My Info” if logged in, “Login/Sign Up” if not)  
  - Implemented main category list with subcategories displayed on hover  


### Product List Page Design & Implementation
- Displayed product list according to the selected main category  
- Designed board-style product listing page with:  
  - Thumbnail, title, price/deposit, registration date  
  - Product posting function available only for logged-in users  
- Designed product list crawling using Python  
- Added sorting UI (latest, views, price)  
- Implemented subcategory display on the left side panel  
- Created subcategory filter buttons and sidebar for favorites/recently viewed products (connected to local storage & DB)  


### Product Detail Page Design & Implementation
- Built DB-based product information page including:  
  - Product name, price, deposit, description, views, registration date, transaction status  
  - Data retrieved and displayed from the database  
- Implemented map API based on lender’s address  
- Displayed other products from the same seller  
  - Randomly retrieved N similar products by subcategory and displayed at the bottom (via API integration)  
- Enabled conditional post edit/delete for lender or admin  
- Implemented favorite product saving linked with user & product information  

### Rental Reservation Page Design & Implementation
- Implemented rental scheduling using FullCalendar modal  
- Matched and saved schedules via custom API  
- Stored rental data in the database linked with buyer & product (seller) information  
- Disabled past dates and already confirmed reservations based on system date  
