# Pocket-Library

TODO:
1) Implement layout with Jetpack Compose
   - An activity for the favourites
   - Online book search and a list of books from search will be in main activity page
   - An activity that displays details of a book

2) Online book searching through API
   - Use Open Library API
   - Results from search has to display at least title, author, year and a cover image
   - Cover images are fetched using cover_i from Open Library

3) Manually enter book details to save as favourites into a local Room database
   - Favourites are saved to a favourites list to be viewed even when offline
   - Toggle star button to update and delete favourites
   - Carry out book search within the favourites list by typing in keyword (title/author)
   - The favourites list has to be fully accessible when offline

4) Add a personal photo of her copy using the camera
   - Must have permission to access the camera of her phone
   
5) Share book recommendations with her contacts
   - Must have permission to access contacts
   
6) Implement Firebase Firestore
   - Used to store the saved favourites on top of the local Room database
   - Upload books that are saved locally to the firebase
   - When app opens, favourites should be fetched from firebase

7) Usual configuration things
   - Smooth scrolling and scroll position has to preserve across config changes
   - Ensure device rotation and app restart does not change scroll position
   - Ensure app restart does not reset search query
   - Books that are saved offline must auto-upload to Firebase when back online
   - Use vertical list in portrait and grid layout in landscape
   - For large devices (>= 600dp width),show results list on the left and details on the right